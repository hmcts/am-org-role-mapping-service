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

    public static final int SLEEP_BACK_OFF_SERVICE_BUSY_SECONDS = 1;

    private final BulkAssignmentOrchestrator bulkAssignmentOrchestrator;
    private final OrmDeserializer deserializer;

    public TopicConsumer(BulkAssignmentOrchestrator bulkAssignmentOrchestrator,
                               OrmDeserializer deserializer) {
        this.bulkAssignmentOrchestrator = bulkAssignmentOrchestrator;
        this.deserializer = deserializer;
    }

    public static void processError(ServiceBusErrorContext context) {
        log.error("Error when receiving messages from namespace: '{}'. Entity: '{}'",
                context.getFullyQualifiedNamespace(), context.getEntityPath());

        if (!(context.getException() instanceof ServiceBusException exception)) {
            var contextException = context.getException();
            log.error("Non-ServiceBusException occurred: {}", contextException.getMessage(), contextException);
            return;
        }

        ServiceBusFailureReason reason = exception.getReason();

        if (reason == ServiceBusFailureReason.MESSAGING_ENTITY_DISABLED
                || reason == ServiceBusFailureReason.MESSAGING_ENTITY_NOT_FOUND
                || reason == ServiceBusFailureReason.UNAUTHORIZED) {
            log.error("An unrecoverable error occurred. Stopping processing with reason {}: {}",
                    reason, exception.getMessage(), exception);
        } else if (reason == ServiceBusFailureReason.MESSAGE_LOCK_LOST) {
            log.error("Message lock lost for message: {}", exception.getMessage(), exception);
        } else if (reason == ServiceBusFailureReason.SERVICE_BUSY) {
            log.info("Service Busy error");
            sleepBackOff(SLEEP_BACK_OFF_SERVICE_BUSY_SECONDS);
        } else {
            log.error("Error source {}, reason {}, message: {}", context.getErrorSource(),
                    reason, exception.getMessage(), exception);
        }
    }

    public void processMessage(ServiceBusReceivedMessageContext messageContext, UserType userType) {
        byte[] body = messageContext.getMessage().getBody().toBytes();
        log.info("Delivery Count is : {}", messageContext.getMessage().getDeliveryCount());
        UserRequest request = deserializer.deserializeBytes(body);

        if (userType != null && request != null && request.getUserIds() != null) {
            log.debug("Parsing message from {} with size :: {}", userType.name(), request.getUserIds().size());
        } else {
            log.debug("Either userType, request or request's userIds is null");
        }

        ResponseEntity<Object> response = bulkAssignmentOrchestrator.createBulkAssignmentsRequest(request, userType);

        if (userType != null && response != null) {
            log.debug("Role Assignment Service Response {}: {}", userType.name(), response.getStatusCode());
        } else {
            log.debug("Either userType or response is null");
        }

        messageContext.complete();
    }

    public static void sleepBackOff(int sleepBackOffSeconds) {
        try {
            TimeUnit.SECONDS.sleep(sleepBackOffSeconds);
        } catch (InterruptedException e) {
            log.error("Unable to sleep for period of time");
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
    }

}
