package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
@Slf4j
public class MessagingConfiguration {

    @Value("${amqp.host}")
    String host;
    @Value("${amqp.topic}")
    String topic;
    @Value("${amqp.sharedAccessKeyName}")
    String sharedAccessKeyName;
    @Value("${amqp.sharedAccessKeyValue}")
    String sharedAccessKeyValue;
    @Value("${amqp.subscription}")
    String subscription;
    @Value("${launchdarkly.sdk.environment}")
    String environment;

    @Bean
    public ServiceBusSenderClient getServiceBusSenderClient() {
        log.info("Getting the ServiceBusSenderClient");
        logServiceBusVariables();
        String connectionString = "Endpoint=sb://"
                + host + ";SharedAccessKeyName=" + sharedAccessKeyName + ";SharedAccessKey=" + sharedAccessKeyValue;
        log.info("Connection String is: " + connectionString);
        log.info("Topic Name is " + topic);

        return new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .retryOptions(new AmqpRetryOptions())
                .sender()
                .topicName(topic)
                .buildClient();
    }


    public void logServiceBusVariables() {
        log.debug("Env is: " + environment);
        log.debug("sharedAccessKeyName : " + sharedAccessKeyName);
        log.debug("host : " + host);
        log.debug("Topic Name is :" + topic);

        if (StringUtils.isEmpty(sharedAccessKeyValue) || StringUtils.isEmpty(host) || StringUtils.isEmpty(topic)) {
            throw new IllegalArgumentException("The Host, Topic Name or Shared Access Key is not available.");
        }

    }
}