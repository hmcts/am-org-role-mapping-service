package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.MessageHandlerOptions;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.BulkAssignmentOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.deserializer.OrmDeserializer;

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
public class CRDTopicConsumer extends CRDMessagingConfiguration {

    private BulkAssignmentOrchestrator bulkAssignmentOrchestrator;
    private OrmDeserializer deserializer;


    public CRDTopicConsumer(BulkAssignmentOrchestrator bulkAssignmentOrchestrator,
                         OrmDeserializer deserializer) {
        this.bulkAssignmentOrchestrator = bulkAssignmentOrchestrator;
        this.deserializer = deserializer;

    }

    @Bean
    @Qualifier("crdConsumer")
    @ConditionalOnProperty(name = "amqp.crd.enabled", havingValue = "true")
    public SubscriptionClient getSubscriptionClient() throws URISyntaxException, ServiceBusException,
            InterruptedException {
        logServiceBusVariables();
        URI endpoint = new URI("sb://" + host);
        log.debug("CRD Destination is " + topic.concat("/subscriptions/").concat(subscription));

        var destination = topic.concat("/subscriptions/").concat(subscription);

        ConnectionStringBuilder connectionStringBuilder = new ConnectionStringBuilder(
                endpoint,
                destination,
                sharedAccessKeyName,
                sharedAccessKeyValue);
        connectionStringBuilder.setOperationTimeout(Duration.ofMinutes(10));
        return new SubscriptionClient(connectionStringBuilder, ReceiveMode.PEEKLOCK);
    }

    @Bean
    @Qualifier("crdConsumer")
    @ConditionalOnProperty(name = "amqp.crd.enabled", havingValue = "true")
    CompletableFuture<Void> registerCRDMessageHandlerOnClient(@Autowired @Qualifier("crdConsumer")
                                                                   SubscriptionClient receiveClient)
            throws ServiceBusException, InterruptedException {

        log.debug("    Calling registerMessageHandlerOnClient in CRD ");

        IMessageHandler messageHandler = new IMessageHandler() {
            // callback invoked when the message handler loop has obtained a message
            @SneakyThrows
            @WithSpan(value = "CRD Azure Service Bus Topic", kind = SpanKind.SERVER)
            public CompletableFuture<Void> onMessageAsync(IMessage message) {
                log.debug("    Calling onMessageAsync in CRD.....{}", message);
                List<byte[]> body = message.getMessageBody().getBinaryData();
                try {
                    log.debug("    Locked Until Utc : {}", message.getLockedUntilUtc());
                    log.info("    Delivery Count is : {}", message.getDeliveryCount());
                    AtomicBoolean result = new AtomicBoolean();
                    processMessage(body, result);
                    if (result.get()) {
                        return receiveClient.completeAsync(message.getLockToken());
                    }

                    log.debug("    getLockToken......{}", message.getLockToken());

                } catch (Exception e) { // java.lang.Throwable introduces the Sonar issues
                    throw new InvalidRequest("Some Network issue");
                }
                log.debug("Finally getLockedUntilUtc" + message.getLockedUntilUtc());
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

        log.info("    Parsing the message in CRD");
        UserRequest request = deserializer.deserialize(body);
        try {
            ResponseEntity<Object> response = bulkAssignmentOrchestrator.createBulkAssignmentsRequest(request,
                    UserType.CASEWORKER);
            log.info("----Role Assignment Service Response CRD  {}", response.getStatusCode());
            result.set(Boolean.TRUE);
        } catch (Exception e) {
            log.error("Exception from RAS service : {}", e.getMessage());
            throw e;
        }
    }


}

