package uk.gov.hmcts.reform.orgrolemapping.servicebus;


import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.MessageHandlerOptions;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.BulkAssignmentOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RoleAssignmentService;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.deserializer.OrmDeserializer;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.messaging.MessagingConfig;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class TopicConsumer {

    @Value("${amqp.host}")
    String host;
    @Value("${amqp.topic}")
    String topic;
    @Value("${amqp.subscription}")
    String subscription;
    @Value("${amqp.sharedAccessKeyName}")
    String sharedAccessKeyName;
    @Value("${amqp.sharedAccessKeyValue}")
    String sharedAccessKeyValue;

    private BulkAssignmentOrchestrator bulkAssignmentOrchestrator;
    private OrmDeserializer deserializer;

    @Autowired
    private RoleAssignmentService roleAssignmentService;

    public TopicConsumer(BulkAssignmentOrchestrator bulkAssignmentOrchestrator,
                         OrmDeserializer deserializer) {
        this.bulkAssignmentOrchestrator = bulkAssignmentOrchestrator;
        this.deserializer = deserializer;

    }

    @Bean
    public SubscriptionClient getSubscriptionClient() throws URISyntaxException, ServiceBusException,
            InterruptedException {
        log.info("Printing env variables");
        log.info("SB_ACCESS_KEY :" + System.getenv("SB_ACCESS_KEY"));
        log.info("SB_NAMESPACE :" + System.getenv("SB_NAMESPACE"));
        log.info("SB_SUB_NAME :" + System.getenv("SB_SUB_NAME"));
        log.info("SB_TOPIC_CONN_STRING :" + System.getenv("SB_TOPIC_CONN_STRING"));
        log.info("AMQP_SUB_NAME :" + System.getenv("AMQP_SUB_NAME"));
        log.info("AMQP_SHARED_ACCESS_KEY_VALUE :" + System.getenv("AMQP_SHARED_ACCESS_KEY_VALUE"));


        log.info("End printing variables.");
        String env = System.getenv("LAUNCH_DARKLY_ENV");
        if (StringUtils.isNotEmpty(env) && env.toLowerCase().startsWith("pr")) {
            host = MessagingConfig.getHostName();
        }
        URI endpoint = new URI("sb://" + host);
        log.info("Destination is " + topic.concat("/subscriptions/").concat(subscription));
        String destination = topic.concat("/subscriptions/").concat(subscription);

        ConnectionStringBuilder connectionStringBuilder = new ConnectionStringBuilder(
                endpoint,
                destination,
                sharedAccessKeyName,
                sharedAccessKeyValue);
        connectionStringBuilder.setOperationTimeout(Duration.ofMinutes(10));
        return new SubscriptionClient(connectionStringBuilder, ReceiveMode.PEEKLOCK);
    }

    @Bean
    CompletableFuture<Void> registerMessageHandlerOnClient(@Autowired SubscriptionClient receiveClient)
            throws ServiceBusException, InterruptedException {

        log.info("    Calling registerMessageHandlerOnClient ");

        IMessageHandler messageHandler = new IMessageHandler() {
            // callback invoked when the message handler loop has obtained a message
            @SneakyThrows
            public CompletableFuture<Void> onMessageAsync(IMessage message) {
                log.info("    Calling onMessageAsync.....{}", message);
                List<byte[]> body = message.getMessageBody().getBinaryData();
                try {
                    log.info("    Locked Until Utc : {}", message.getLockedUntilUtc());
                    log.info("    Delivery Count is : {}", message.getDeliveryCount());
                    AtomicBoolean result = new AtomicBoolean();
                    processMessage(body, result);
                    if (result.get()) {
                        return receiveClient.completeAsync(message.getLockToken());
                    }


                    log.info("    getLockToken......{}", message.getLockToken());

                } catch (Exception e) { // java.lang.Throwable introduces the Sonar issues
                    throw new InvalidRequest("Some Network issue");
                }
                log.info("Finally getLockedUntilUtc" + message.getLockedUntilUtc());
                return null;

            }

            public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {
                log.error("Exception occurred.");
                log.error(exceptionPhase + "-" + throwable.getMessage());
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        receiveClient.registerMessageHandler(
                messageHandler, new MessageHandlerOptions(1,
                        false, Duration.ofHours(1), Duration.ofMinutes(5)),
                executorService);
        return null;

    }

    private void processMessage(List<byte[]> body, AtomicBoolean result) {

        log.info("    Parsing the message");
        UserRequest request = deserializer.deserialize(body);
        try {
            ResponseEntity<Object> response = bulkAssignmentOrchestrator.createBulkAssignmentsRequest(request);
            log.info("----Role Assignment Service Response {}", response.getStatusCode());
            result.set(Boolean.TRUE);
        } catch (Exception e) {
            log.error("Exception from RAS service : {}", e.getMessage());
            throw e;
        }
    }


}

