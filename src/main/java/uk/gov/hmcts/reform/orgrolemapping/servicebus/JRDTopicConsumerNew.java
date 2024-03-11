package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class JRDTopicConsumerNew extends JRDMessagingConfiguration {

    private TopicConsumer topicConsumer;

    public JRDTopicConsumerNew(TopicConsumer topicConsumer) {
        this.topicConsumer = topicConsumer;
    }

    @Bean
    @Qualifier("jrdConsumer")
    //@ConditionalOnProperty(name = "amqp.jrd.enabled", havingValue = "true")
    @ConditionalOnExpression("${amqp.jrd.enabled} && ${amqp.jrd.newAsb}")
    CompletableFuture<Void> registerJRDMessageHandlerOnClient()
            throws ServiceBusException, InterruptedException {

        log.error("Inside registerJRDMessageHandlerOnClient Before processorClient");
        ServiceBusProcessorClient processorClient = getServiceBusProcessorClient(
                messageContext -> topicConsumer.processMessage(messageContext, UserType.JUDICIAL),
                TopicConsumer::processError);

        log.error("Inside registerJRDMessageHandlerOnClient after processorClient");
        // Starts the processor in the background and returns immediately
        processorClient.start();
        log.error("Inside registerJRDMessageHandlerOnClient after start");

        return null;
    }

}
