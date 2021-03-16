package uk.gov.hmcts.reform.orgrolemapping.servicebus.messaging;

import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import javax.jms.ConnectionFactory;
import javax.net.ssl.SSLContext;

@Configuration
@Slf4j
public class PublisherMessagingConfig {

    @Bean
    public String jmsUrlString(@Value("${jrd-consumer.host}") final String host) {
        return String.format("amqps://%1s?amqp.idleTimeout=3600000", host);
    }

    @Bean
    public ConnectionFactory jmsConnectionFactory(
            @Value("${spring.application.name}") final String clientId,
            @Value("${jrd-consumer.sharedAccessKeyName}") final String username,
            @Value("${jrd-consumer.sharedAccessKeyValue}") final String password,
            @Autowired final String jmsUrlString,
            @Autowired(required = false) final SSLContext jmsSslContext,
            @Value("${jrd-consumer.trustAllCerts}") final boolean trustAllCerts) {

        JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(jmsUrlString);
        jmsConnectionFactory.setUsername(username);
        jmsConnectionFactory.setPassword(password);
        jmsConnectionFactory.setClientID(clientId);
        jmsConnectionFactory.setReceiveLocalOnly(true);
        if (trustAllCerts && jmsSslContext != null) {
            jmsConnectionFactory.setSslContext(jmsSslContext);
        }
        return new CachingConnectionFactory(jmsConnectionFactory);
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory jmsConnectionFactory) {
        JmsTemplate returnValue = new JmsTemplate();
        returnValue.setConnectionFactory(jmsConnectionFactory);
        returnValue.setMessageConverter(new MappingJackson2MessageConverter());
        returnValue.setSessionTransacted(true);
        return returnValue;
    }

    @Bean // Serialize message content to json using TextMessage
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        return converter;
    }

}
