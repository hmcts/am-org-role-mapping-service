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
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.config.servicebus.CRDMessagingConfiguration;
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
public class CRDTopicConsumer {

    private BulkAssignmentOrchestrator bulkAssignmentOrchestrator;
    private OrmDeserializer deserializer;
    private CRDMessagingConfiguration configuration;

    public CRDTopicConsumer(BulkAssignmentOrchestrator bulkAssignmentOrchestrator,
                            OrmDeserializer deserializer,
                            CRDMessagingConfiguration configuration) {
        this.bulkAssignmentOrchestrator = bulkAssignmentOrchestrator;
        this.deserializer = deserializer;
        this.configuration = configuration;
    }

    @Bean
    @Qualifier("crdConsumer")
    @ConditionalOnExpression("${amqp.crd.enabled} && !${amqp.crd.newAsb}")
    public SubscriptionClient getSubscriptionClient() throws URISyntaxException, ServiceBusException,
            InterruptedException {
        configuration.logServiceBusVariables();
        URI endpoint = new URI("sb://" + configuration.getHost());
        log.debug("CRD Destination is " + configuration.getTopic().concat("/subscriptions/")
                .concat(configuration.getSubscription()));

        var destination = configuration.getTopic().concat("/subscriptions/")
                .concat(configuration.getSubscription());

        ConnectionStringBuilder connectionStringBuilder = new ConnectionStringBuilder(
                endpoint,
                destination,
                configuration.getSharedAccessKeyName(),
                configuration.getSharedAccessKeyValue());
        connectionStringBuilder.setOperationTimeout(Duration.ofMinutes(10));
        return new SubscriptionClient(connectionStringBuilder, ReceiveMode.PEEKLOCK);
    }

    @Bean
    @Qualifier("crdConsumer")
    @ConditionalOnExpression("${amqp.crd.enabled} && !${amqp.crd.newAsb}")
    CompletableFuture<Void> registerCRDMessageHandlerOnClient(@Autowired @Qualifier("crdConsumer")
                                                                   SubscriptionClient receiveClient)
            throws ServiceBusException, InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        receiveClient.registerMessageHandler(
                getMessageHandler(receiveClient),
                new MessageHandlerOptions(
                        1,
                        false,
                        Duration.ofHours(1),
                        Duration.ofMinutes(5)
                ),
                executorService
        );
        return null;

    }

    public IMessageHandler getMessageHandler(SubscriptionClient receiveClient) {
        log.debug("    Calling registerMessageHandlerOnClient in CRD ");
        return new IMessageHandler() {
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
                    log.error("Error processing CRD message from service bus : {}", e.getMessage());
                    throw new InvalidRequest("Error processing message from service bus", e);
                }
                log.debug("Finally getLockedUntilUtc" + message.getLockedUntilUtc());
                return null;
            }

            public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {
                log.error(
                        "An error occurred when Calling onMessageAsync in CRD. Phase: {}",
                        exceptionPhase,
                        throwable
                );
            }
        };
    }

    private void processMessage(List<byte[]> body, AtomicBoolean result) {
        UserRequest request = deserializer.deserialize(body);
        log.debug("Parsing the message from CRD with size :: {}", request.getUserIds().size());

        ResponseEntity<Object> response = bulkAssignmentOrchestrator.createBulkAssignmentsRequest(request,
                UserType.CASEWORKER);

        log.info("Role Assignment Service Response CRD: {}", response.getStatusCode());
        result.set(Boolean.TRUE);
    }
}
