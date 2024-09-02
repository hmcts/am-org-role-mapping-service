package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.microsoft.azure.servicebus.SubscriptionClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.orgrolemapping.config.servicebus.JRDMessagingConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.BulkAssignmentOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.deserializer.OrmDeserializer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class JRDTopicConsumerNewTest {

    @Mock
    private BulkAssignmentOrchestrator bulkAssignmentOrchestrator;

    @Mock
    private OrmDeserializer deserializer;

    @Mock
    public SubscriptionClient subscriptionClient;

    @Mock
    JRDMessagingConfiguration configuration;

    @Mock
    private ServiceBusProcessorClient processorClient;

    JRDTopicConsumerNew sut;

    TopicConsumer topicConsumer;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        topicConsumer = new TopicConsumer(bulkAssignmentOrchestrator, deserializer);
        sut = new JRDTopicConsumerNew(topicConsumer, configuration);
        when(configuration.getServiceBusProcessorClient(any(), any())).thenReturn(processorClient);
    }

    @Test
    void registerMessageHandlerOnClientTest() throws Exception {
        CompletableFuture<Void> voidCompletableFuture = sut.startJRDProcessorClient();
        assertNull(voidCompletableFuture);
    }

    @Test
    void shouldThrowExceptionWhenMessageReceivedWithIncorrectFormat() throws InterruptedException {
        when(deserializer.deserialize(any())).thenThrow(RuntimeException.class);

        ServiceBusReceivedMessage message = Mockito.mock(ServiceBusReceivedMessage.class);
        // When getBody is called on the mock, return a BinaryData with the desired body
        Mockito.when(message.getBody()).thenReturn(BinaryData.fromString("{invalidUserRequest}"));
        ServiceBusReceivedMessageContext serviceBusReceivedMessageContext =
                Mockito.mock(ServiceBusReceivedMessageContext.class);
        // Stub the getMessage method to return the message
        Mockito.when(serviceBusReceivedMessageContext.getMessage()).thenReturn(message);

        CompletableFuture<Void> voidCompletableFuture = sut.startJRDProcessorClient();

        try {
            topicConsumer.processMessage(serviceBusReceivedMessageContext, UserType.CASEWORKER);
        } catch (InvalidRequest exception) {
            assertThat(exception.getMessage(), containsString("Error processing message from service bus"));
        }
    }

    @Test
    void shouldThrowExceptionWhenCallToRoleAssignmentServiceIsUnsuccessful()
            throws JsonProcessingException, InterruptedException {

        UserRequest userRequest = UserRequest.builder()
                .userIds(List.of("9d6089ab-0459-4bb6-9174-dc111f0f661d", "1ab7da58-fa93-43b7-8df1-8f8406fdcb5a"))
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String request = mapper.writeValueAsString(userRequest);

        when(deserializer.deserialize(any())).thenReturn(userRequest);
        when(bulkAssignmentOrchestrator.createBulkAssignmentsRequest(userRequest, UserType.JUDICIAL))
                .thenThrow(RuntimeException.class);

        ServiceBusReceivedMessage message = Mockito.mock(ServiceBusReceivedMessage.class);
        // When getBody is called on the mock, return a BinaryData with the desired body
        Mockito.when(message.getBody()).thenReturn(BinaryData.fromString(request));
        ServiceBusReceivedMessageContext serviceBusReceivedMessageContext =
                Mockito.mock(ServiceBusReceivedMessageContext.class);
        // Stub the getMessage method to return the message
        Mockito.when(serviceBusReceivedMessageContext.getMessage()).thenReturn(message);

        CompletableFuture<Void> voidCompletableFuture = sut.startJRDProcessorClient();

        try {
            topicConsumer.processMessage(serviceBusReceivedMessageContext, UserType.CASEWORKER);
        } catch (InvalidRequest exception) {
            assertThat(exception.getMessage(), containsString("Error processing message from service bus"));
        }
    }
}
