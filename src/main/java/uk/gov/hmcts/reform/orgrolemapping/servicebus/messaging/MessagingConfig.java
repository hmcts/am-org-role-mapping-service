package uk.gov.hmcts.reform.orgrolemapping.servicebus.messaging;


import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessagingConfig {

    @Value("${amqp.host}")
    String host;
    @Value("${amqp.topic}")
    String topic;
    @Value("${amqp.sharedAccessKeyName}")
    String sharedAccessKeyName;
    @Value("${amqp.sharedAccessKeyValue}")
    String sharedAccessKeyValue;

    @Bean
    public ServiceBusSenderClient getServiceBusSenderClient() {
        log.info("Getting the ServiceBusSenderClient");
        String env = System.getenv("LAUNCH_DARKLY_ENV");
        log.info("Getting the ServiceBusSenderClient . Env is: " + env);
        if (StringUtils.isNotEmpty(env) && env.toLowerCase().startsWith("pr")) {
            sharedAccessKeyValue = System.getenv("SB_ACCESS_KEY");
            topic = System.getenv("SB_NAMESPACE");

            host = getHostName();
            log.info("sharedAccessKeyValue : " + sharedAccessKeyValue);
            log.info("host : " + getHostName());
            log.info("Topic Name is :" + topic);
            if (StringUtils.isEmpty(sharedAccessKeyValue) || StringUtils.isEmpty(host) || StringUtils.isEmpty(topic)) {
                throw new IllegalArgumentException("The Host, Topic Name or Shared Access Key is not available.");
            }
        }
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
        String connectionString = System.getenv("SB_TOPIC_CONN_STRING");
        if (StringUtils.isEmpty(connectionString)) {
            throw new IllegalArgumentException("The Host Name is empty");
        }
        log.info(String.valueOf(connectionString.indexOf("//")));
        log.info(String.valueOf(connectionString.indexOf(".")));
        log.info(connectionString.substring(connectionString.indexOf("//") + 2,
                connectionString.indexOf(".")));
        return connectionString.substring(connectionString.indexOf("//") + 2,
                connectionString.indexOf(".")).concat(".servicebus.windows.net");
    }
}