
package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.microsoft.azure.servicebus.SubscriptionClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.BulkAssignmentOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.deserializer.OrmDeserializer;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CRDTopicConsumerTest {

    private BulkAssignmentOrchestrator bulkAssignmentOrchestrator;

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
        CompletableFuture<Void> voidCompletableFuture = sut.registerCRDMessageHandlerOnClient(
                subscriptionClient);
        assertNull(voidCompletableFuture);
    }


}

