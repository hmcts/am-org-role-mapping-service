package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.BulkAssignmentOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.deserializer.OrmCallbackDeserializer;

import static java.lang.String.format;

@Slf4j
@Component
@Lazy(false)
public class TopicConsumer {

    private final Integer maxRetryAttempts;

    private BulkAssignmentOrchestrator bulkAssignmentOrchestrator;

    private final OrmCallbackDeserializer deserializer;

    public TopicConsumer(@Value("${send-letter.maxRetryAttempts}") Integer maxRetryAttempts,
                         BulkAssignmentOrchestrator bulkAssignmentOrchestrator,
                         OrmCallbackDeserializer deserializer) {
        this.maxRetryAttempts = maxRetryAttempts;
        this.bulkAssignmentOrchestrator = bulkAssignmentOrchestrator;
        this.deserializer = deserializer;

    }

    /*@JmsListener(
            destination = "${amqp.topic}",
            containerFactory = "topicJmsListenerContainerFactory",
            subscription = "${amqp.subscription}"
    )*/

    public void onMessage(String message) {
        processMessageWithRetry(message, 1);
    }

    private void processMessageWithRetry(String message, int retry) {
        try {
            log.info("TopicConsumer - Message received from the service bus by ORM service {}", message);
            processMessage(message);
        } catch (Exception e) {
            if (retry > maxRetryAttempts) {
                log.error(format("Caught unknown unrecoverable error %s", e.getMessage()), e);
            } else {
                log.info(String.format("Caught recoverable error %s, retrying %s out of %s",
                        e.getMessage(), retry, maxRetryAttempts));
                processMessageWithRetry(message, retry + 1);
            }
        }
    }

    private void processMessage(String message) {
        UserRequest userRequest = deserializer.deserialize(message);
        log.info("TopicConsumer - Deserializer userRequest received from the service bus by ORM service {}",
                userRequest);
        if (userRequest != null) {
            ResponseEntity<Object> response = bulkAssignmentOrchestrator.createBulkAssignmentsRequest(userRequest);
            log.info("API Response {}", response.getStatusCode());
        }

    }
}
