package uk.gov.hmcts.reform.orgrolemapping;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.client.fluent.Executor;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.CRDTopicConsumer;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.CRDTopicPublisher;
import uk.gov.hmcts.reform.orgrolemapping.config.JRDMessagingConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.JRDTopicConsumer;
import uk.gov.hmcts.reform.orgrolemapping.config.CRDMessagingConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.JRDTopicPublisher;


import java.util.Map;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "am_roleAssignment_getAssignment")
@PactFolder("pacts")
public class OrgRoleMappingConsumerTestForGetActorById extends BaseTestContract {

    private static final String ACTOR_ID = "23486";
    private static final String RAS_GET_ACTOR_BY_ID = "/am/role-assignments/actors/" + ACTOR_ID;

    @MockBean
    CRDTopicConsumer topicConsumer;

    @MockBean
    JRDTopicConsumer jrdTopicConsumer;



    @MockBean
    CRDMessagingConfiguration crdMessagingConfiguration;

    @MockBean
    JRDMessagingConfiguration jrdMessagingConfiguration;

    @MockBean
    JRDTopicPublisher jrdPublisher;
    @MockBean
    CRDTopicPublisher crdPublisher;

    @MockBean
    @Qualifier("crdPublisher")
    ServiceBusSenderClient serviceBusSenderClient;

    @MockBean
    @Qualifier("jrdPublisher")
    ServiceBusSenderClient serviceBusSenderClientJrd;

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "am_roleAssignment_getAssignment", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact executeGetActorByIdAndGet200(PactDslWithProvider builder) {

        return builder
                .given("An actor with provided id is available in role assignment service")
                .uponReceiving("RAS takes s2s/auth token and returns actor information")
                .path(RAS_GET_ACTOR_BY_ID)
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetActorByIdAndGet200")
    void getActorByIdAndGet200Test(MockServer mockServer) throws JSONException {
        var actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .get(mockServer.getUrl() + RAS_GET_ACTOR_BY_ID)
                        .then()
                        .log().all().extract().asString();
        JSONObject jsonResponse = new JSONObject(actualResponseBody);
        JSONArray roleAssignmentResponse = (JSONArray) jsonResponse.get("roleAssignmentResponse");
        JSONObject first = (JSONObject) roleAssignmentResponse.get(0);
        assertThat(first.get("actorId"), equalTo(ACTOR_ID));
    }

    private DslPart createResponse() {
        return newJsonBody(o -> o
                .minArrayLike("roleAssignmentResponse", 1, 1,
                    roleAssignmentResponse -> roleAssignmentResponse
                        .stringType("id", "14a21569-eb80-4681-b62c-6ae2ed069e6f")
                        .stringValue("actorIdType", "IDAM")
                        .stringValue("actorId", ACTOR_ID)
                        .stringValue("roleType", "ORGANISATION")
                        .stringValue("roleName", "senior-tribunal-caseworker")
                        .stringValue("classification", "PRIVATE")
                        .stringValue("grantType", "STANDARD")
                        .stringType("roleCategory", "LEGAL_OPERATIONS")
                        .booleanValue("readOnly", false)
                        .object("attributes", attribute -> attribute
                                .stringType("jurisdiction", "IA")
                                .stringType("primaryLocation", "123456"))
                )).build();
    }

    @NotNull
    private Map<String, String> getResponseHeaders() {
        Map<String, String> responseHeaders = Map.of("Content-Type",
                "application/vnd.uk.gov.hmcts.role-assignment-service.get-assignments+json;charset=UTF-8;version=1.0");
        return responseHeaders;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }

}
