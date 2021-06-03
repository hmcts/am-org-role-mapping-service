package uk.gov.hmcts.reform.orgrolemapping;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.MessagingConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.TopicConsumer;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.TopicPublisher;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "am_roleAssignment_deleteAssignment")
@PactFolder("pacts")
@SpringBootTest
public class OrgRoleMappingConsumerTestForDelete {

    private static final String ACTOR_ID = "704c8b1c-e89b-436a-90f6-953b1dc40157";
    private static final String AM_RAS_URL = "/am/role-assignments";
    private static final String QUERY_PARAMS = "process=p2&reference=r2";
    private static final String RAS_DELETE_ACTOR_BY_ID = AM_RAS_URL + "/" + ACTOR_ID;
    private static final String RAS_DELETE_ACTOR_BY_PR = AM_RAS_URL + "?" + QUERY_PARAMS;

    @MockBean
    TopicConsumer topicConsumer;

    @MockBean
    TopicPublisher topicPublisher;

    @MockBean
    MessagingConfiguration messagingConfiguration;

    @MockBean
    ServiceBusSenderClient serviceBusSenderClient;

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "am_roleAssignment_deleteAssignment", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact executeDeleteActorByPrAndGet204(PactDslWithProvider builder) {

        return builder
                .given("An actor with provided process & reference is available in role assignment service")
                .uponReceiving("RAS takes s2s/auth token and returns actor information")
                .path(AM_RAS_URL)
                .query(QUERY_PARAMS)
                .method(HttpMethod.DELETE.toString())
                .willRespondWith()
                .status(HttpStatus.NO_CONTENT.value())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeDeleteActorByPrAndGet204")
    void deleteActorByPrAndGet204Test(MockServer mockServer) throws IOException {
        HttpResponse httpResponse =
                Request.Delete(mockServer.getUrl() + RAS_DELETE_ACTOR_BY_PR).execute().returnResponse();
        assertEquals(204, httpResponse.getStatusLine().getStatusCode());
    }

    @Pact(provider = "am_roleAssignment_deleteAssignment", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact executeDeleteActorByIdAndGet204(PactDslWithProvider builder) {

        return builder
                .given("An actor with provided id is available in role assignment service")
                .uponReceiving("RAS takes s2s/auth token and returns actor information")
                .path(RAS_DELETE_ACTOR_BY_ID)
                .method(HttpMethod.DELETE.toString())
                .willRespondWith()
                .status(HttpStatus.NO_CONTENT.value())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeDeleteActorByIdAndGet204")
    void deleteActorByIdAndGet204Test(MockServer mockServer) throws IOException {
        HttpResponse httpResponse =
                Request.Delete(mockServer.getUrl() + RAS_DELETE_ACTOR_BY_ID).execute().returnResponse();
        assertEquals(204, httpResponse.getStatusLine().getStatusCode());
    }
}