package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JRDTopicPublisher {

    private final JmsTemplate jmsTemplate;
    private final String destination;

    @Autowired
    public JRDTopicPublisher(JmsTemplate jmsTemplateJRD,
                             @Value("${aws-consumer.jrd.topic}") final String destination) {
        this.jmsTemplate = jmsTemplateJRD;
        this.destination = destination;
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 3))
    public void sendMessage(Object message) {
        log.info("Publishing message to service bus topic:: Job Id is: {}");
        if (message instanceof PublishCaseWorkerData) {
            log.info("Job Id is: Count of User Ids is: {} ",
                    ((PublishCaseWorkerData) message).getUserIds() != null
                            ? ((PublishCaseWorkerData) message).getUserIds().size() : null);
        }

        jmsTemplate.convertAndSend(destination, message);
    }

    @Recover
    public void recoverMessage(Throwable ex) throws Throwable {
        log.error("TopicPublisher.recover(): Send message failed with exception: ", ex);
        throw ex;
    }
}

