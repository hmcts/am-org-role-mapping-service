package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.BulkAssignmentOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.deserializer.OrmDeserializer;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Slf4j
@Component
public class JRDTopicConsumerNew extends JRDMessagingConfiguration {

    private BulkAssignmentOrchestrator bulkAssignmentOrchestrator;
    private OrmDeserializer deserializer;

    public JRDTopicConsumerNew(BulkAssignmentOrchestrator bulkAssignmentOrchestrator,
                               OrmDeserializer deserializer) {
        this.bulkAssignmentOrchestrator = bulkAssignmentOrchestrator;
        this.deserializer = deserializer;
    }

    Consumer<ServiceBusReceivedMessageContext> processMessage = messageContext -> {
        System.out.println(messageContext.getMessage().getMessageId());
        byte[] body = messageContext.getMessage().getBody().toBytes();
        processMessage(body);
        // other message processing code
    };

    Consumer<ServiceBusErrorContext> processError = errorContext -> {
        Exception e = (Exception) errorContext.getException();
        log.error("Error processing JRD message from service bus : {}", e.getMessage());
        throw new InvalidRequest("Error processing message from service bus", e);
    };

    @Bean
    @Qualifier("jrdConsumer")
    //@ConditionalOnProperty(name = "amqp.jrd.enabled", havingValue = "true")
    @ConditionalOnExpression("${amqp.jrd.enabled} && ${amqp.jrd.newAsb}")
    CompletableFuture<Void> registerJRDMessageHandlerOnClient()
            throws ServiceBusException, InterruptedException {

        log.error("Inside registerJRDMessageHandlerOnClient Before processorClient");
        ServiceBusProcessorClient processorClient = getServiceBusProcessorClient(processMessage, processError);

        log.error("Inside registerJRDMessageHandlerOnClient after processorClient");
        // Starts the processor in the background and returns immediately
        processorClient.start();
        log.error("Inside registerJRDMessageHandlerOnClient after start");

        return null;
    }

    @WithSpan(value = "JRD Azure Service Bus Topic AzureV2", kind = SpanKind.CONSUMER)
    private void processMessage(byte[] body) {
        UserRequest request = deserializer.deserializeBytes(body);
        log.debug("Parsing the message from JRD with size :: {}", request.getUserIds().size());

        ResponseEntity<Object> response = bulkAssignmentOrchestrator.createBulkAssignmentsRequest(request,
                UserType.JUDICIAL);

        log.debug("Role Assignment Service Response JRD: {}", response.getStatusCode());
    }

}
