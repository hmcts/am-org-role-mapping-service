package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TopicConsumerTest {

    private static final String MESSAGE = "message";
    private static final int MAX_RETRY = 3;

    private TopicConsumer topicConsumer;

    @Before
    public void setup() {
        topicConsumer = new TopicConsumer(MAX_RETRY);
    }

    @Test
    public void processMessageWithRetry() {
        topicConsumer.onMessage(MESSAGE);
    }

}