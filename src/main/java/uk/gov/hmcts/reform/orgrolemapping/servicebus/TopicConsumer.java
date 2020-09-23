package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Slf4j
@Component
@Lazy(false)
public class TopicConsumer {

    private final Integer maxRetryAttempts;

    public TopicConsumer(@Value("${send-letter.maxRetryAttempts}") Integer maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;

    }

    @JmsListener(
            destination = "${amqp.topic}",
            containerFactory = "topicJmsListenerContainerFactory",
            subscription = "${amqp.subscription}"
    )
    public void onMessage(String message) {
        processMessageWithRetry(message, 1);
    }

    private void processMessageWithRetry(String message, int retry) {
        try {
            log.info("Message received from the service bus by ORM service");
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
        log.info("We received message from queue :: " + message);

    }
}
