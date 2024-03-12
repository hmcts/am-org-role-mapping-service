package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.config.CRDMessagingConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class CRDTopicConsumerNew {

    private TopicConsumer topicConsumer;

    private CRDMessagingConfiguration configuration;

    public CRDTopicConsumerNew(TopicConsumer topicConsumer, CRDMessagingConfiguration configuration) {
        this.topicConsumer = topicConsumer;
        this.configuration = configuration;
    }

    @Bean
    @Qualifier("crdConsumer")
    //@ConditionalOnProperty(name = "amqp.crd.enabled", havingValue = "true")
    @ConditionalOnExpression("${amqp.crd.enabled} && ${amqp.crd.newAsb}")
    CompletableFuture<Void> registerCRDMessageHandlerOnClient()
            throws InterruptedException {

        log.error("Inside registerCRDMessageHandlerOnClient Before processorClient");
        ServiceBusProcessorClient processorClient = configuration.getServiceBusProcessorClient(
                messageContext -> topicConsumer.processMessage(messageContext, UserType.CASEWORKER),
                TopicConsumer::processError);

        log.error("Inside registerCRDMessageHandlerOnClient after processorClient");
        // Starts the processor in the background and returns immediately
        processorClient.start();
        log.error("Inside registerCRDMessageHandlerOnClient after start");

        return null;
    }

}
