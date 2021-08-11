package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
@Slf4j
@Primary
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

    @Bean("crdPublisher")
    public ServiceBusSenderClient getServiceBusSenderClient() {
        log.debug("Getting the ServiceBusSenderClient in CRD");
        logServiceBusVariables();
        String connectionString = "Endpoint=sb://"
                + host + ";SharedAccessKeyName=" + sharedAccessKeyName + ";SharedAccessKey=" + sharedAccessKeyValue;

        log.info("CRD Topic Name is " + topic);

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
            sharedAccessKeyValue = System.getenv("AMQP_CRD_SHARED_ACCESS_KEY_VALUE");
            subscription = System.getenv("CRD_SUBSCRIPTION_NAME");

            log.info("sharedAccessKeyName : " + sharedAccessKeyName);
            log.info("subscription Name is :" + subscription);
            log.info("host : " + host);
            log.info("Topic Name is :" + topic);
            log.info("subscription Name is :" + subscription);

            if (StringUtils.isEmpty(sharedAccessKeyValue) || StringUtils.isEmpty(host) || StringUtils.isEmpty(topic)) {
                throw new IllegalArgumentException("The Host, Topic Name or Shared Access Key is not available.");
            }
        }

    }
}