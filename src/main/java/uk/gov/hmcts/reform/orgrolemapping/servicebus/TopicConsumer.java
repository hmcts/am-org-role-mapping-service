package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.BulkAssignmentOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.deserializer.OrmDeserializer;

import static java.lang.String.format;

@Slf4j
@Component
@Lazy(false)
public class TopicConsumer {

    private final Integer maxRetryAttempts;

    private BulkAssignmentOrchestrator bulkAssignmentOrchestrator;

    private final OrmDeserializer ormDeserializer;

    public TopicConsumer(@Value("${send-letter.maxRetryAttempts}") Integer maxRetryAttempts,
                         BulkAssignmentOrchestrator bulkAssignmentOrchestrator,
                         OrmDeserializer ormDeserializer) {
        this.maxRetryAttempts = maxRetryAttempts;
        this.bulkAssignmentOrchestrator = bulkAssignmentOrchestrator;
        this.ormDeserializer = ormDeserializer;

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
            log.info("CRDTopicConsumer - Message received from the CRD subscription : {}", message);
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
        UserRequest userRequest = ormDeserializer.deserialize(message);
        log.info("CRDTopicConsumer:Deserializer - userRequest received from from the CRD subscription : {}",
                userRequest);
        if (userRequest != null) {
            ResponseEntity<Object> response = bulkAssignmentOrchestrator.createBulkAssignmentsRequest(userRequest);
            log.info("The Organisation roles for received users: {} are updated with consolidated response : {}",
                    userRequest,response.getStatusCode());
        }

    }
}
