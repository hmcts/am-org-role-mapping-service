package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.BulkAssignmentOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.deserializer.OrmDeserializer;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TopicConsumerTest {

    private BulkAssignmentOrchestrator bulkAssignmentOrchestrator;

    private OrmDeserializer deserializer;


    @Mock
    public SubscriptionClient subscriptionClient;

    TopicConsumer sut;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        sut = new TopicConsumer(bulkAssignmentOrchestrator, deserializer);
    }

    @Test
    void getSubscriptionClientThrowsServiceBusException() {
        sut.host = "http://test.com";
        sut.sharedAccessKeyName = "test";
        sut.sharedAccessKeyValue = "test";
        sut.topic = "test";

        assertThrows(ServiceBusException.class, () ->  sut.getSubscriptionClient());

    }


    @Test
    void registerMessageHandlerOnClientTest() throws Exception {
        CompletableFuture<Void> voidCompletableFuture = sut.registerMessageHandlerOnClient(
                subscriptionClient);
        assertNull(voidCompletableFuture);
    }


}
