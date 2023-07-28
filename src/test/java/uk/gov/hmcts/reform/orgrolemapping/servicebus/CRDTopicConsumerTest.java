
package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.SubscriptionClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CRDTopicConsumerTest {

    @Mock
    private BulkAssignmentOrchestrator bulkAssignmentOrchestrator;

    @Mock
    private OrmDeserializer deserializer;

    @Mock
    public SubscriptionClient subscriptionClient;

    CRDTopicConsumer sut;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        sut = new CRDTopicConsumer(bulkAssignmentOrchestrator, deserializer);
    }

    //@Test
    void getSubscriptionClientThrowsServiceBusException() {
        sut.host = "http://test.com";
        sut.subscription = "test";
        sut.environment = "pr";
        sut.topic = "test1";

        assertThrows(IllegalArgumentException.class, () -> sut.getSubscriptionClient());
    }

    @Test
    void registerMessageHandlerOnClientTest() throws Exception {
        CompletableFuture<Void> voidCompletableFuture = sut.registerCRDMessageHandlerOnClient(subscriptionClient);
        assertNull(voidCompletableFuture);
    }

    @Test
    void shouldThrowExceptionWhenMessageReceivedWithIncorrectFormat() {
        when(deserializer.deserialize(any())).thenThrow(RuntimeException.class);

        IMessageHandler handler = sut.getMessageHandler(subscriptionClient);

        InvalidRequest exception =
                assertThrows(InvalidRequest.class, () -> handler.onMessageAsync(new Message("test")));
        assertThat(exception.getMessage(), containsString("Error processing message from service bus"));
    }

    @Test
    void shouldThrowExceptionWhenCallToRoleAssignmentServiceIsUnsuccessful() {
        UserRequest userRequest = UserRequest.builder()
                .userIds(List.of("9d6089ab-0459-4bb6-9174-dc111f0f661d", "1ab7da58-fa93-43b7-8df1-8f8406fdcb5a"))
                .build();

        when(deserializer.deserialize(any())).thenReturn(userRequest);
        when(bulkAssignmentOrchestrator.createBulkAssignmentsRequest(userRequest, UserType.CASEWORKER))
            .thenThrow(RuntimeException.class);

        IMessageHandler handler = sut.getMessageHandler(subscriptionClient);

        InvalidRequest exception =
                assertThrows(InvalidRequest.class, () -> handler.onMessageAsync(new Message("test")));
        assertThat(exception.getMessage(), containsString("Error processing message from service bus"));
    }
}

