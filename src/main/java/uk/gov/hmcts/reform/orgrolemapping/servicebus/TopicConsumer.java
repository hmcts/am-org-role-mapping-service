package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.BulkAssignmentOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.deserializer.OrmDeserializer;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class TopicConsumer {

    public static final int SLEEP_BACK_OFF_GENERAL_ERROR_SECONDS = 60;
    public static final int SLEEP_BACK_OFF_SERVICE_BUSY_SECONDS = 1;

    private BulkAssignmentOrchestrator bulkAssignmentOrchestrator;
    private OrmDeserializer deserializer;

    public TopicConsumer(BulkAssignmentOrchestrator bulkAssignmentOrchestrator,
                               OrmDeserializer deserializer) {
        this.bulkAssignmentOrchestrator = bulkAssignmentOrchestrator;
        this.deserializer = deserializer;
    }

    public static void processError(ServiceBusErrorContext context) {
        log.error("Error when receiving messages from namespace: '{}'. Entity: '{}'",
                context.getFullyQualifiedNamespace(), context.getEntityPath());

        if (!(context.getException() instanceof ServiceBusException)) {
            log.error("Non-ServiceBusException occurred: {}", context.getException());
            return;
        }

        ServiceBusException exception = (ServiceBusException) context.getException();
        ServiceBusFailureReason reason = exception.getReason();

        if (reason == ServiceBusFailureReason.MESSAGING_ENTITY_DISABLED
                || reason == ServiceBusFailureReason.MESSAGING_ENTITY_NOT_FOUND
                || reason == ServiceBusFailureReason.UNAUTHORIZED) {
            log.error("An unrecoverable error occurred. Stopping processing with reason {}: {}",
                    reason, exception.getMessage());
        } else if (reason == ServiceBusFailureReason.MESSAGE_LOCK_LOST) {
            log.error("Message lock lost for message: {}", context.getException());
        } else if (reason == ServiceBusFailureReason.SERVICE_BUSY) {
            sleepBackOff(SLEEP_BACK_OFF_SERVICE_BUSY_SECONDS);
        } else {
            log.error("Error source {}, reason {}, message: {}", context.getErrorSource(),
                    reason, context.getException());
            sleepBackOff(SLEEP_BACK_OFF_GENERAL_ERROR_SECONDS);
        }
    }

    public void processMessage(ServiceBusReceivedMessageContext messageContext, UserType userType) {
        byte[] body = messageContext.getMessage().getBody().toBytes();
        UserRequest request = deserializer.deserializeBytes(body);
        log.error("messageContext.getEntityPath : " + messageContext.getEntityPath());
        log.error("messageContext.getFullyQualifiedNamespace : " + messageContext.getFullyQualifiedNamespace());
        log.debug("Parsing the message from JRD with size :: {}", request.getUserIds().size());

        ResponseEntity<Object> response = bulkAssignmentOrchestrator.createBulkAssignmentsRequest(request, userType);

        log.debug("Role Assignment Service Response JRD: {}", response.getStatusCode());
    }

    public static void sleepBackOff(int sleepBackOffSeconds) {
        try {
            TimeUnit.SECONDS.sleep(sleepBackOffSeconds);
        } catch (InterruptedException e) {
            log.error("Unable to sleep for period of time");
        }
    }
}
