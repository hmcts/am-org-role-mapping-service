package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.BulkAssignmentOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureConditionEvaluator;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.deserializer.OrmDeserializer;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Slf4j
@Component
public class JRDTopicConsumerNew extends JRDMessagingConfiguration {

    private BulkAssignmentOrchestrator bulkAssignmentOrchestrator;
    private OrmDeserializer deserializer;

    @Autowired
    private FeatureConditionEvaluator featureConditionEvaluator;

    public JRDTopicConsumerNew(BulkAssignmentOrchestrator bulkAssignmentOrchestrator,
                               OrmDeserializer deserializer) {
        this.bulkAssignmentOrchestrator = bulkAssignmentOrchestrator;
        this.deserializer = deserializer;
    }

    @Bean
    @Qualifier("jrdConsumer")
    //@ConditionalOnProperty(name = "amqp.jrd.enabled", havingValue = "true")
    CompletableFuture<Void> registerJRDMessageHandlerOnClient()
            throws ServiceBusException, InterruptedException {
        log.error("Inside registerJRDMessageHandlerOnClient");

        // Sample code that processes a single message
        Consumer<ServiceBusReceivedMessageContext> processMessage = messageContext -> {
            System.out.println(messageContext.getMessage().getMessageId());
            byte[] body = messageContext.getMessage().getBody().toBytes();
            processMessage(body);
            // other message processing code
        };

        log.error("Inside registerJRDMessageHandlerOnClient Before processError");
        // Sample code that gets called if there's an error
        Consumer<ServiceBusErrorContext> processError = errorContext -> {
            // logError(errorContext.getException());
            // metrics.recordError(errorContext.getException());
            Exception e = (Exception) errorContext.getException();
            log.error("Error processing JRD message from service bus : {}", e.getMessage());
            throw new InvalidRequest("Error processing message from service bus", e);
        };

        var connectionString = "Endpoint=sb://"
                + host + ";SharedAccessKeyName=" + sharedAccessKeyName + ";SharedAccessKey=" + sharedAccessKeyValue;

        log.error("Inside registerJRDMessageHandlerOnClient Before processorClient");
        // create the processor client via the builder and its sub-builder
        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .processor()
                .topicName(topic)
                .subscriptionName(subscription)
                .processMessage(processMessage)
                .processError(processError)
                .buildProcessorClient();

        log.error("Inside registerJRDMessageHandlerOnClient after processorClient");
        // Starts the processor in the background and returns immediately
        processorClient.start();
        log.error("Inside registerJRDMessageHandlerOnClient after start");

        return null;
    }

    public IMessageHandler getMessageHandler(SubscriptionClient receiveClient) {
        log.info("    Calling registerMessageHandlerOnClient in JRD ");
        return new IMessageHandler() {
            // callback invoked when the message handler loop has obtained a message
            @SneakyThrows
            @WithSpan(value = "JRD Azure Service Bus Topic", kind = SpanKind.SERVER)
            public CompletableFuture<Void> onMessageAsync(IMessage message) {
                log.debug("    Calling onMessageAsync in JRD.....{}", message);
                List<byte[]> body = message.getMessageBody().getBinaryData();
                try {
                    log.debug("    Locked Until Utc : {}", message.getLockedUntilUtc());
                    AtomicBoolean result = new AtomicBoolean();
                    if (featureConditionEvaluator.isFlagEnabled("am_org_role_mapping_service",
                            "orm-jrd-org-role")) {
                        processMessage(body, result);
                        if (result.get()) {
                            return receiveClient.completeAsync(message.getLockToken());
                        }

                        log.debug("    getLockToken......{}", message.getLockToken());
                    } else {
                        log.info("The JRD feature flag is currently disabled. This message would be suppressed");
                        return receiveClient.completeAsync(message.getLockToken());
                    }

                } catch (Exception e) { // java.lang.Throwable introduces the Sonar issues
                    log.error("Error processing JRD message from service bus : {}", e.getMessage());
                    throw new InvalidRequest("Error processing message from service bus", e);
                }
                log.debug("Finally getLockedUntilUtc" + message.getLockedUntilUtc());
                return null;

            }

            public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {
                log.error(
                        "An error occurred when Calling onMessageAsync in JRD. Phase: {}",
                        exceptionPhase,
                        throwable
                );
            }
        };
    }

    private void processMessage(List<byte[]> body, AtomicBoolean result) {
        UserRequest request = deserializer.deserialize(body);
        log.debug("Parsing the message from JRD with size :: {}", request.getUserIds().size());

        ResponseEntity<Object> response = bulkAssignmentOrchestrator.createBulkAssignmentsRequest(request,
                UserType.JUDICIAL);

        log.debug("Role Assignment Service Response JRD: {}", response.getStatusCode());
        result.set(Boolean.TRUE);
    }

    @WithSpan(value = "JRD Azure Service Bus Topic AzureV2", kind = SpanKind.SERVER)
    private void processMessage(byte[] body) {
        UserRequest request = deserializer.deserializeBytes(body);
        log.debug("Parsing the message from JRD with size :: {}", request.getUserIds().size());

        ResponseEntity<Object> response = bulkAssignmentOrchestrator.createBulkAssignmentsRequest(request,
                UserType.JUDICIAL);

        log.debug("Role Assignment Service Response JRD: {}", response.getStatusCode());
    }


}
