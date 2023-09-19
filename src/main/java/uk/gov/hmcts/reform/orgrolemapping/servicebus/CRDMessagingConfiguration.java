package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;


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
    @ConditionalOnExpression("${testing.support.enabled} && ${amqp.crd.enabled}")
    public ServiceBusSenderClient getServiceBusSenderClient() {
        log.debug("Getting the ServiceBusSenderClient in CRD");
        logServiceBusVariables();
        var connectionString = "Endpoint=sb://"
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
            sharedAccessKeyValue = System.getenv("AMQP_CRD_SHARED_ACCESS_KEY_VALUE");
            subscription = System.getenv("CRD_SUBSCRIPTION_NAME");
            log.debug("sharedAccessKeyName : " + sharedAccessKeyName);
            log.debug("subscription Name is :" + subscription);
            log.debug("Topic Name is :" + topic);
            log.debug("subscription Name is :" + subscription);

            host = System.getenv("AMQP_HOST");
            if (!host.contains(Constants.SERVICEBUS_DOMAIN)) {
                host = host.concat(Constants.SERVICEBUS_DOMAIN);
            }
            log.debug("host : " + host);
            if (StringUtils.isEmpty(sharedAccessKeyValue)
                    || StringUtils.isEmpty(host) || StringUtils.isEmpty(topic)) {
                throw new IllegalArgumentException("The Host, Topic Name or Shared Access Key is not available.");
            }
        }

    }
}