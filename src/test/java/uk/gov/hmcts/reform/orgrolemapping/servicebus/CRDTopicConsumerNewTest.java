package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.orgrolemapping.config.servicebus.CRDMessagingConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.BulkAssignmentOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.deserializer.OrmDeserializer;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CRDTopicConsumerNewTest {

    @Mock
    private BulkAssignmentOrchestrator bulkAssignmentOrchestrator;

    @Mock
    private OrmDeserializer deserializer;

    @Mock
    CRDMessagingConfiguration configuration;

    @Mock
    private ServiceBusProcessorClient processorClient;

    CRDTopicConsumerNew sut;

    TopicConsumer topicConsumer;

    ObjectMapper mapper = new ObjectMapper();

    @Captor
    ArgumentCaptor<Consumer<ServiceBusReceivedMessageContext>> processMessageCaptor;

    @Captor
    ArgumentCaptor<Consumer<ServiceBusErrorContext>> processErrorCaptor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        topicConsumer = Mockito.spy(new TopicConsumer(bulkAssignmentOrchestrator, deserializer));
        sut = new CRDTopicConsumerNew(topicConsumer, configuration);
        when(configuration.getServiceBusProcessorClient(any(), any())).thenReturn(processorClient);
    }

    @Test
    void registerErrorHandlerOnClientTest() throws Exception {

        // WHEN
        sut.startCRDProcessorClient();
        var errorHandler = getProcessErrorHandler();

        // THEN
        assertNotNull(errorHandler);
    }

    @Test
    void registerMessageHandlerOnClientTest() throws Exception {

        // WHEN
        sut.startCRDProcessorClient();
        var messageHandler = getProcessMessageHandler();

        // THEN
        assertNotNull(messageHandler);
    }

    @Test
    void shouldProcessMessage() throws Exception {

        // GIVEN
        UserRequest userRequest = UserRequest.builder()
                .userIds(List.of("9d6089ab-0459-4bb6-9174-dc111f0f661d", "1ab7da58-fa93-43b7-8df1-8f8406fdcb5a"))
                .build();
        when(deserializer.deserializeBytes(any())).thenReturn(userRequest);

        when(bulkAssignmentOrchestrator.createBulkAssignmentsRequest(userRequest, UserType.CASEWORKER))
                .thenReturn(ResponseEntity.ok(null));

        ServiceBusReceivedMessageContext serviceBusReceivedMessageContext
                = mockServiceBusMessage(mapper.writeValueAsString(userRequest));

        // WHEN
        sut.startCRDProcessorClient();
        getProcessMessageHandler().accept(serviceBusReceivedMessageContext);

        // THEN
        verify(topicConsumer, times(1)).processMessage(serviceBusReceivedMessageContext, UserType.CASEWORKER);
        verify(bulkAssignmentOrchestrator, times(1)).createBulkAssignmentsRequest(userRequest, UserType.CASEWORKER);
    }

    @Test
    void shouldThrowExceptionWhenMessageReceivedWithIncorrectFormat() throws InterruptedException {

        // GIVEN
        when(deserializer.deserializeBytes(any())).thenThrow(IllegalArgumentException.class);

        ServiceBusReceivedMessageContext serviceBusReceivedMessageContext =
                mockServiceBusMessage("{invalidUserRequest}");

        // WHEN / THEN
        sut.startCRDProcessorClient();
        var messageHandler = getProcessMessageHandler();
        assertThrows(IllegalArgumentException.class, () -> messageHandler.accept(serviceBusReceivedMessageContext));

        // NB: check processing stops
        verify(bulkAssignmentOrchestrator, never()).createBulkAssignmentsRequest(any(), eq(UserType.CASEWORKER));
    }

    @Test
    void shouldThrowExceptionWhenCallToRoleAssignmentServiceIsUnsuccessful()
            throws JsonProcessingException, InterruptedException {

        // WHEN
        UserRequest userRequest = UserRequest.builder()
                .userIds(List.of("9d6089ab-0459-4bb6-9174-dc111f0f661d", "1ab7da58-fa93-43b7-8df1-8f8406fdcb5a"))
                .build();
        when(deserializer.deserializeBytes(any())).thenReturn(userRequest);

        when(bulkAssignmentOrchestrator.createBulkAssignmentsRequest(userRequest, UserType.CASEWORKER))
                .thenThrow(RuntimeException.class);

        ServiceBusReceivedMessageContext serviceBusReceivedMessageContext
                = mockServiceBusMessage(mapper.writeValueAsString(userRequest));

        // WHEN / THEN
        sut.startCRDProcessorClient();
        var messageHandler = getProcessMessageHandler();
        assertThrows(RuntimeException.class, () -> messageHandler.accept(serviceBusReceivedMessageContext));
    }

    private Consumer<ServiceBusErrorContext> getProcessErrorHandler() {
        verify(configuration, times(1)).getServiceBusProcessorClient(any(), processErrorCaptor.capture());
        return processErrorCaptor.getValue();
    }

    private Consumer<ServiceBusReceivedMessageContext> getProcessMessageHandler() {
        verify(configuration, times(1)).getServiceBusProcessorClient(processMessageCaptor.capture(), any());
        return processMessageCaptor.getValue();
    }

    private ServiceBusReceivedMessageContext mockServiceBusMessage(String requestBody) {
        ServiceBusReceivedMessage message = Mockito.mock(ServiceBusReceivedMessage.class);
        // When getBody is called on the mock, return a BinaryData with the desired body
        Mockito.when(message.getBody()).thenReturn(BinaryData.fromString(requestBody));
        ServiceBusReceivedMessageContext serviceBusReceivedMessageContext =
                Mockito.mock(ServiceBusReceivedMessageContext.class);
        // Stub the getMessage method to return the message
        Mockito.when(serviceBusReceivedMessageContext.getMessage()).thenReturn(message);

        return serviceBusReceivedMessageContext;
    }
}
