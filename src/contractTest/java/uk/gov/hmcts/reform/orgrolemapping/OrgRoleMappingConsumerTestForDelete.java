package uk.gov.hmcts.reform.orgrolemapping;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "am_roleAssignment_deleteAssignment", pactVersion = PactSpecVersion.V3)
@PactFolder("pacts")
@TestPropertySource(properties = {
    "idam.api.url=http://localhost:5000",
    "spring.cache.type=simple"
})
public class OrgRoleMappingConsumerTestForDelete extends BaseTestContract {

    private static final String ACTOR_ID = "704c8b1c-e89b-436a-90f6-953b1dc40157";
    private static final String AM_RAS_URL = "/am/role-assignments";
    private static final String QUERY_PARAMS = "process=p2&reference=r2";
    private static final String RAS_DELETE_ACTOR_BY_ID = AM_RAS_URL + "/" + ACTOR_ID;
    private static final String RAS_DELETE_ACTOR_BY_PR = AM_RAS_URL + "?" + QUERY_PARAMS;

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
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
    @PactTestFor(pactMethod = "executeDeleteActorByPrAndGet204", pactVersion = PactSpecVersion.V3)
    void deleteActorByPrAndGet204Test(MockServer mockServer) throws IOException {
        HttpResponse httpResponse =
                Request.delete(mockServer.getUrl() + RAS_DELETE_ACTOR_BY_PR).execute().returnResponse();
        assertEquals(204, httpResponse.getCode());
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
    @PactTestFor(pactMethod = "executeDeleteActorByIdAndGet204", pactVersion = PactSpecVersion.V3)
    void deleteActorByIdAndGet204Test(MockServer mockServer) throws IOException {
        HttpResponse httpResponse =
                Request.delete(mockServer.getUrl() + RAS_DELETE_ACTOR_BY_ID).execute().returnResponse();
        assertEquals(204, httpResponse.getCode());
    }
}