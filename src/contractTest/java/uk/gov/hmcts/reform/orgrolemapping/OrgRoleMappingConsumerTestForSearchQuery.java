package uk.gov.hmcts.reform.orgrolemapping;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.google.common.collect.Maps;
import groovy.util.logging.Slf4j;
import io.restassured.http.ContentType;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Map;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "am_role_assignment_service_search_query")
@PactFolder("pacts")
@SpringBootTest
public class OrgRoleMappingConsumerTestForSearchQuery {

    private static final String ACTOR_ID = "23486";
    private static final String RAS_SEARCH_QUERY_ROLE_ASSIGNMENT_URL = "/am/role-assignments/query";

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "am_role_assignment_service_search_query", consumer = "am_org_role_mapping")
    public RequestResponsePact executeSearchQueryRoleAssignmentAndGet200(PactDslWithProvider builder) throws IOException {

        return builder
                .given("A list of role assignments for the search query")
                .uponReceiving("RAS takes s2s/auth token and returns search query results")
                .path(RAS_SEARCH_QUERY_ROLE_ASSIGNMENT_URL)
                .method( HttpMethod.POST.toString())
                .body(createRoleAssignmentRequestSearchQuery(), String.valueOf( ContentType.JSON))
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createRoleAssignmentResponseSearchQuery())
                .toPact();
    }

    private String createRoleAssignmentRequestSearchQuery() {
        return "";
//        return "{\n        \"actorId\": \"[\n        \"23486\"\n        ]\n}";
    }

    @Test
    @PactTestFor(pactMethod = "executeSearchQueryRoleAssignmentAndGet200")
    void getSearchQueryResultsByActorIdAndGet200Test(MockServer mockServer)
            throws JSONException, IOException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .body(createRoleAssignmentRequestSearchQuery())
                        .post(mockServer.getUrl() + RAS_SEARCH_QUERY_ROLE_ASSIGNMENT_URL)
                        .then()
                        .log().all().extract().asString();

        JSONObject jsonResponse = new JSONObject(actualResponseBody);
        JSONArray roleAssignmentResponse = (JSONArray)jsonResponse.get("roleAssignmentResponse");
        JSONObject first = (JSONObject)roleAssignmentResponse.get(0);
        assertThat(first.get("actorId"), equalTo(ACTOR_ID));
    }

    private DslPart createRoleAssignmentResponseSearchQuery() {
        return newJsonBody(o -> o
            .minArrayLike("links", 1, 1, link -> link.stringType("rel", "binary")
                    .stringType("href", "http://localhost:4096/am/role-assignments/query"))
            .minArrayLike("roleAssignmentResponse", 1, 1,
                    roleAssignmentResponse -> roleAssignmentResponse
                            .stringType("id", "14a21569-eb80-4681-b62c-6ae2ed069e6f")
                            .stringValue("actorIdType", "IDAM")
                            .stringValue("actorId", ACTOR_ID)
                            .stringValue("roleType", "ORGANISATION")
                            .stringValue("roleName", "senior-tribunal-caseworker")
                            .stringValue("classification", "PRIVATE")
                            .stringValue("grantType", "STANDARD")
                            .stringValue("roleCategory", "STAFF")
                            .booleanValue("readOnly", false)
                            .object("attributes", attribute -> attribute
                                    .stringType("jurisdiction", "IA")
                                    .stringType("primaryLocation", "500A2S"))
            )).build();
    }

    @NotNull
    private Map<String, String> getResponseHeaders() {
        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type",
                "application/vnd.uk.gov.hmcts.role-assignment-service.post-assignment-query-request+json;charset=UTF-8;version=1.0");
        return responseHeaders;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }
}
