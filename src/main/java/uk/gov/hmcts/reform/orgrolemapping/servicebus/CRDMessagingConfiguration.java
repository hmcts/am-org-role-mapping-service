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
public class CRDMessagingConfiguration {

    @Value("${amqp.host}")
    String host;
    @Value("${amqp.sharedAccessKeyName}")
    String sharedAccessKeyName;
    @Value("${amqp.crd.topic}")
    String topic;
    @Value("${amqp.crd.sharedAccessKeyValue}")
    String sharedAccessKeyValue;
    @Value("${amqp.crd.subscription}")
    String subscription;
    @Value("${launchdarkly.sdk.environment}")
    String environment;

    @Bean
    public ServiceBusSenderClient getServiceBusSenderClient() {
        log.debug("Getting the ServiceBusSenderClient in CRD");
        logServiceBusVariables();
        String connectionString = "Endpoint=sb://"
                + host + ";SharedAccessKeyName=" + sharedAccessKeyName + ";SharedAccessKey=" + sharedAccessKeyValue;

        log.debug("CRD Topic Name is " + topic);

        return new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .retryOptions(new AmqpRetryOptions())
                .sender()
                .topicName(topic)
                .buildClient();
    }


    public void logServiceBusVariables() {
        log.debug("Env is: " + environment);
        if (environment.equalsIgnoreCase("pr")) {
            host = System.getenv("AMQP_HOST").concat(".servicebus.windows.net");
            sharedAccessKeyValue = System.getenv("AMQP_SHARED_ACCESS_KEY_VALUE");
            subscription = System.getenv("SUBSCRIPTION_NAME");

            log.debug("sharedAccessKeyName : " + sharedAccessKeyName);
            log.debug("subscription Name is :" + subscription);
            log.debug("host : " + host);
            log.debug("Topic Name is :" + topic);
            log.debug("subscription Name is :" + subscription);

            if (StringUtils.isEmpty(sharedAccessKeyValue) || StringUtils.isEmpty(host) || StringUtils.isEmpty(topic)) {
                throw new IllegalArgumentException("The Host, Topic Name or Shared Access Key is not available.");
            }
        }

    }
}