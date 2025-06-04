package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusErrorSource;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.BulkAssignmentOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.deserializer.OrmDeserializer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TopicConsumerTest {

    @Mock
    BulkAssignmentOrchestrator bulkAssignmentOrchestrator;

    @Mock
    OrmDeserializer ormDeserializer;

    @Mock
    ServiceBusErrorContext serviceBusErrorContext;

    @Mock
    ServiceBusReceivedMessageContext messageContext;

    @Mock
    UserType userType;

    @Mock
    ServiceBusReceivedMessage serviceBusReceivedMessage;

    @Mock
    BinaryData binaryData;

    @Mock
    UserRequest userRequest;

    @Mock
    ResponseEntity<Object> responseEntity;

    Logger logger;
    ListAppender<ILoggingEvent> listAppender;
    List<ILoggingEvent> logsList;

    TopicConsumer sut;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        sut = new TopicConsumer(bulkAssignmentOrchestrator, ormDeserializer);

        // attach appender to logger for assertions
        logger = (Logger) LoggerFactory.getLogger(TopicConsumer.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        logsList = listAppender.list;
    }

    @Test
    void processErrorNonServiceBusException() {
        when(serviceBusErrorContext.getException()).thenReturn(new RuntimeException("Some Runtime Exception"));

        TopicConsumer.processError(serviceBusErrorContext);

        assertEquals("Non-ServiceBusException occurred: {}", logsList.get(1).getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = AmqpErrorCondition.class, names = {"ENTITY_DISABLED_ERROR", "NOT_FOUND", "UNAUTHORIZED_ACCESS"})
    void processErrorUnrecoverableError(AmqpErrorCondition condition) {
        ServiceBusException serviceBusException = new ServiceBusException(
                createAmqpException(condition),
                ServiceBusErrorSource.UNKNOWN
        );
        when(serviceBusErrorContext.getException()).thenReturn(serviceBusException);

        TopicConsumer.processError(serviceBusErrorContext);

        assertEquals("An unrecoverable error occurred. Stopping processing with reason {}: {}",
                logsList.get(1).getMessage());
    }

    @Test
    void processErrorMessageLock() {
        ServiceBusException serviceBusException = new ServiceBusException(
                createAmqpException(AmqpErrorCondition.MESSAGE_LOCK_LOST),
                ServiceBusErrorSource.UNKNOWN
        );
        when(serviceBusErrorContext.getException()).thenReturn(serviceBusException);

        TopicConsumer.processError(serviceBusErrorContext);

        assertEquals("Message lock lost for message: {}", logsList.get(1).getMessage());
    }

    @Test
    void processErrorServiceBusy() {
        ServiceBusException serviceBusException = new ServiceBusException(
                createAmqpException(AmqpErrorCondition.SERVER_BUSY_ERROR),
                ServiceBusErrorSource.UNKNOWN
        );
        when(serviceBusErrorContext.getException()).thenReturn(serviceBusException);

        TopicConsumer.processError(serviceBusErrorContext);

        assertEquals("Service Busy error", logsList.get(1).getMessage());
    }

    @Test
    void processErrorGeneralError() {
        ServiceBusException serviceBusException = new ServiceBusException(
                createAmqpException(AmqpErrorCondition.INTERNAL_ERROR),
                ServiceBusErrorSource.UNKNOWN
        );
        when(serviceBusErrorContext.getException()).thenReturn(serviceBusException);

        TopicConsumer.processError(serviceBusErrorContext);

        assertEquals("Error source {}, reason {}, message: {}", logsList.get(1).getMessage());
    }

    @Test
    void processMessage() {
        when(messageContext.getMessage()).thenReturn(serviceBusReceivedMessage);
        when(serviceBusReceivedMessage.getMessageId()).thenReturn("1");
        when(serviceBusReceivedMessage.getBody()).thenReturn(binaryData);
        when(binaryData.toBytes()).thenReturn("some bytes".getBytes());
        when(ormDeserializer.deserializeBytes(any())).thenReturn(userRequest);
        when(bulkAssignmentOrchestrator.createBulkAssignmentsRequest(any(), any())).thenReturn(responseEntity);

        sut.processMessage(messageContext, userType);

        verify(ormDeserializer).deserializeBytes("some bytes".getBytes());
        verify(bulkAssignmentOrchestrator).createBulkAssignmentsRequest(userRequest, userType);
    }

    private AmqpException createAmqpException(AmqpErrorCondition errorCondition) {
        return new AmqpException(true, errorCondition, "AMQP test Error", null);
    }

}
