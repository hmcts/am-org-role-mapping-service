package uk.gov.hmcts.reform.orgrolemapping;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.util.logging.Slf4j;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.client.fluent.Executor;
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
import org.jetbrains.annotations.NotNull;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.io.InputStream;
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

    private static final String ACTOR_ID = "992299";
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
                .given("A list of role assignments for the search query "
                        + "false")
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

//    private String createRoleAssignmentResponseSearchQuery() throws IOException {
//        return readJsonFromFile("ResponseSearchQueryActorId");
//    }

    private String createRoleAssignmentRequestSearchQuery() throws IOException {
        return readJsonFromFile("RequestSearchQueryActorId");
    }

    private DslPart createRoleAssignmentResponseSearchQuery() {
        return newJsonBody((o) -> {
            o
                    .minArrayLike("links", 0, 0,
                            link -> link.stringType("urk", "http")
                    )
                    .object("roleAssignmentResponse", ob -> ob
                            .object("roleRequest",
                                    roleRequest -> roleRequest
                                            .stringType("id", "14a21569-eb80-4681-b62c-6ae2ed069e1f")
                                            .stringType("authenticatedUserId",
                                                    "14a21569-eb80-4681-b62c-6ae2ed069e2f")
                                            .stringType("correlationId",
                                                    "14a21569-eb80-4681-b62c-6ae2ed069e3f")
                                            .stringType("assignerId",
                                                    "14a21569-eb80-4681-b62c-6ae2ed069e4f")
                                            .stringValue("requestType", "CREATE")
                                            .stringValue("process", "staff-organisational-role-mapping")
                                            .stringValue("reference",
                                                    "14a21569-eb80-4681-b62c-6ae2ed069e5f")
                                            .booleanValue("replaceExisting", false)
                                            .stringValue("status", "APPROVED")
                                            .stringType("log", "Request has been Approved")
                            )

                            .minArrayLike("requestedRoles", 1, 1,
                                    requestedRoles -> requestedRoles
                                            .stringType("id", "14a21569-eb80-4681-b62c-6ae2ed069e6f")
                                            .stringValue("actorIdType", "IDAM")
                                            .stringType("actorId",
                                                    "14a21569-eb80-4681-b62c-6ae2ed069e5f")
                                            .stringValue("roleType", "ORGANISATION")
                                            .stringValue("roleName", "judge")
                                            .stringValue("classification", "PUBLIC")
                                            .stringValue("grantType", "STANDARD")
                                            .stringValue("roleCategory", "JUDICIAL")
                                            .stringValue("process", "staff-organisational-role-mapping")
                                            .stringValue("reference",
                                                    "14a21569-eb80-4681-b62c-6ae2ed069e5f")
                                            .stringValue("status", "LIVE")
                                            .stringType("log",
                                                    "Create approved : "
                                                            + "judicial_organisational_role_mapping_service_create\\"
                                                            + "nApproved "
                                                            + ": validate_role_assignment_against_patterns")
                                            .object("attributes", attribute -> attribute
                                                    .stringType("jurisdiction", "divorce")
                                                    .stringType("region", "south-east")
                                                    .stringType("contractType", "SALARIED")
                                                    .stringType("caseId", "1234567890123456")
                                            )
                            )
                    )
            ;

        }).build();
    }


    @NotNull
    private Map<String, String> getResponseHeaders() {
        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type",
                "application/vnd.uk.gov.hmcts.role-assignment-service.post-assignment-query-request+json;charset=UTF-8;version=1.0");
        return responseHeaders;
    }

    private String readJsonFromFile(String fileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = OrgRoleMappingConsumerTestForSearchQuery.class
                .getResourceAsStream(String.format("/%s.json", fileName));
        Object json = mapper.readValue(is, Object.class);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }
}
