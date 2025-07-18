package uk.gov.hmcts.reform.orgrolemapping.config.servicebus;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.config.EnvironmentConfiguration;

import java.time.Duration;
import java.util.function.Consumer;

@Getter
@Configuration
@Slf4j
@Primary
public class JRDMessagingConfiguration {

    @Value("${amqp.host}")
    String host;
    @Value("${amqp.sharedAccessKeyName}")
    String sharedAccessKeyName;
    @Value("${amqp.jrd.topic}")
    String topic;
    @Value("${amqp.jrd.sharedAccessKeyValue}")
    String sharedAccessKeyValue;
    @Value("${amqp.jrd.subscription}")
    String subscription;

    @Autowired
    private EnvironmentConfiguration environmentConfiguration;

    @Bean("jrdPublisher")
    @ConditionalOnExpression("${testing.support.enabled} && ${amqp.jrd.enabled}")
    public ServiceBusSenderClient getServiceBusSenderClient() {
        log.debug("Getting the ServiceBusSenderClient in JRD");
        logServiceBusVariables();
        var connectionString = "Endpoint=sb://"
                + host + ";SharedAccessKeyName=" + sharedAccessKeyName + ";SharedAccessKey=" + sharedAccessKeyValue;

        log.info("JRD Topic Name is " + topic);

        return new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .retryOptions(new AmqpRetryOptions())
                .sender()
                .topicName(topic)
                .buildClient();
    }

    public ServiceBusProcessorClient getServiceBusProcessorClient(
            Consumer<ServiceBusReceivedMessageContext> processMessage,
            Consumer<ServiceBusErrorContext> processError) {

        AmqpRetryOptions amqpRetryOptions = new AmqpRetryOptions();
        amqpRetryOptions.setDelay(Duration.ofMinutes(1));
        amqpRetryOptions.setMaxRetries(10);
        amqpRetryOptions.setMode(AmqpRetryMode.FIXED);

        var connectionString = "Endpoint=sb://"
                + host + ";SharedAccessKeyName=" + sharedAccessKeyName + ";SharedAccessKey=" + sharedAccessKeyValue;

        return new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .retryOptions(amqpRetryOptions)
                .processor()
                .topicName(topic)
                .subscriptionName(subscription)
                .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                .disableAutoComplete()
                .processMessage(processMessage)
                .processError(processError)
                .buildProcessorClient();
    }

    public void logServiceBusVariables() {
        log.debug("Env is: " + environmentConfiguration.getEnvironment());
        if (environmentConfiguration.getEnvironment().equalsIgnoreCase("pr")) {
            sharedAccessKeyValue = System.getenv("AMQP_JRD_SHARED_ACCESS_KEY_VALUE");
            subscription = System.getenv("JRD_SUBSCRIPTION_NAME");

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