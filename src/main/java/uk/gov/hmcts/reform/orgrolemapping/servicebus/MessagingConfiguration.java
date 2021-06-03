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
@Service(value = "MessagingConfiguration")
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

    @Bean
    public ServiceBusSenderClient getServiceBusSenderClient() {
        log.info("Getting the ServiceBusSenderClient");
        setVariablesForPreviewEnv();

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

    public static String getHostName() {
        log.info("Getting Host Name");
        String hostName = System.getenv("AMQP_HOST");
        if (StringUtils.isEmpty(hostName)) {
            throw new IllegalArgumentException("The Host Name is empty");
        }

        return hostName.concat(".servicebus.windows.net");
    }

    public void setVariablesForPreviewEnv() {

        String env = System.getenv("LAUNCH_DARKLY_ENV");
        log.info("Env is: " + env);
        if (StringUtils.isNotEmpty(env) && env.toLowerCase().startsWith("pr")) {
            log.info("Setting env variables for the preview.");

            sharedAccessKeyValue = System.getenv("AMQP_SHARED_ACCESS_KEY_VALUE");
            host = getHostName();
            subscription = System.getenv("SUBSCRIPTION_NAME");
            log.info("sharedAccessKeyValue : " + sharedAccessKeyValue);
            log.info("host : " + getHostName());
            log.info("Topic Name is :" + topic);

            log.info("Printing env variables");
            log.info("AMQP_SHARED_ACCESS_KEY_NAME :" + System.getenv("AMQP_SHARED_ACCESS_KEY_NAME"));
            log.info("AMQP_HOST :" + System.getenv("AMQP_HOST"));
            log.info("SUBSCRIPTION_NAME :" + System.getenv("SUBSCRIPTION_NAME"));
            log.info("SB_TOPIC_CONN_STRING :" + System.getenv("SB_TOPIC_CONN_STRING"));
            log.info("SUBSCRIPTION_NAME :" + System.getenv("SUBSCRIPTION_NAME"));
            log.info("AMQP_SHARED_ACCESS_KEY_VALUE :" + System.getenv("AMQP_SHARED_ACCESS_KEY_VALUE"));
            log.info("End printing variables.");

            if (StringUtils.isEmpty(sharedAccessKeyValue) || StringUtils.isEmpty(host) || StringUtils.isEmpty(topic)) {
                throw new IllegalArgumentException("The Host, Topic Name or Shared Access Key is not available.");
            }
        }
    }
}