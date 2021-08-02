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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.CRDMessagingConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.TopicPublisher;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.CRDTopicConsumer;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.JRDTopicConsumer;

import java.io.IOException;
import java.util.Map;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;



@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "am_roleAssignment_queryAssignment")
@PactFolder("pacts")
@SpringBootTest
public class OrgRoleMappingConsumerTestForSearchQuery {

    private static final String ACTOR_ID = "234873";
    private static final String ACTOR_ID_ADV = "14a21569-eb80-4681-b62c-6ae2ed069e5f";
    private static final String RAS_SEARCH_QUERY_ROLE_ASSIGNMENT_URL = "/am/role-assignments/query";
    private static final String TRIBUNAL_CASEWORKER = "tribunal-caseworker";
    public static final String SERVICE = "application/vnd.uk.gov.hmcts.role-assignment-service";
    public static final String POST_ASSIGNMENTS = SERVICE
            + ".post-assignment-query-request+json;charset=UTF-8;version=2.0";

    @MockBean
    CRDTopicConsumer topicConsumer;

    @MockBean
    JRDTopicConsumer jrdTopicConsumer;



    @MockBean
    CRDMessagingConfiguration crdMessagingConfiguration;

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

    private String createRoleAssignmentRequestSearchQuery() {
        return "{\n"
                + "\"actorId\": ["
                + ACTOR_ID + "]\n"
                + "}";
    }

    @Pact(provider = "am_roleAssignment_queryAssignment", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact executeSearchQueryRoleAssignmentAndGet200(PactDslWithProvider builder) {

        return builder
                .given("A list of role assignments for the search query")
                .uponReceiving("RAS takes s2s/auth token and returns search query results")
                .path(RAS_SEARCH_QUERY_ROLE_ASSIGNMENT_URL)
                .method(HttpMethod.POST.toString())
                .body(createRoleAssignmentRequestSearchQuery(), String.valueOf(ContentType.JSON))
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createRoleAssignmentResponseSearchQuery())
                .toPact();
    }

    private String createRoleAssignmentRequestSearchQueryByRoleName() {
        return "{\n"
                + "\"roleName\": [\"senior-tribunal-caseworker\"]\n"
                + "}";
    }

    @Pact(provider = "am_roleAssignment_queryAssignment", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact executeSearchQueryRoleAssignmentByRoleNameAndGet200(PactDslWithProvider builder) {

        return builder
                .given("A list of role assignments for the search query by role name")
                .uponReceiving("RAS takes s2s/auth token and returns search query results")
                .path(RAS_SEARCH_QUERY_ROLE_ASSIGNMENT_URL)
                .method(HttpMethod.POST.toString())
                .body(createRoleAssignmentRequestSearchQueryByRoleName(), String.valueOf(ContentType.JSON))
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createRoleAssignmentResponseSearchQuery())
                .toPact();
    }

    private String createRoleAssignmentRequestSearchQueryByAttributes() {
        return "{\n"
                + "\"attributes\": {\n"
                + "\"primaryLocation\": [\"500A2S\"],\n"
                + "\"jurisdiction\": [\"IA\"]\n"
                + "}\n"
                + "}";
    }

    @Pact(provider = "am_roleAssignment_queryAssignment", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact executeSearchQueryRoleAssignmentByAttributesAndGet200(PactDslWithProvider builder) {

        return builder
                .given("A list of role assignments for the search query by attributes")
                .uponReceiving("RAS takes s2s/auth token and returns search query results")
                .path(RAS_SEARCH_QUERY_ROLE_ASSIGNMENT_URL)
                .method(HttpMethod.POST.toString())
                .body(createRoleAssignmentRequestSearchQueryByAttributes(), String.valueOf(ContentType.JSON))
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createRoleAssignmentResponseSearchQuery())
                .toPact();
    }

    private String createRoleAssignmentRequestSearchQueryMultipleRoleAssignments() {
        return "{\n"
                + "\"roleType\": [\"ORGANISATION\"],\n"
                + "\"roleName\": [\"tribunal-caseworker\",\"senior-tribunal-caseworker\"],\n"
                + "\"roleCategory\": [\"LEGAL_OPERATIONS\"],\n"
                + "\"classification\": [\"PUBLIC\",\"PRIVATE\"],\n"
                + "\"grantType\": [\"STANDARD\"],\n"
                + "\"validAt\": \"2021-12-04T00:00:00Z\",\n"
                + "\"attributes\": {\n"
                + "\"primaryLocation\": [\"219ASA\"],\n"
                + "\"jurisdiction\": [\"IA\"]\n"
                + "}\n"
                + "}";
    }

    @Pact(provider = "am_roleAssignment_queryAssignment", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact executeSearchQueryRoleAssignmentMultipleRoleAssignmentsAndGet200(
            PactDslWithProvider builder) {

        return builder
                .given("A list of multiple role assignments for the search query")
                .uponReceiving("RAS takes s2s/auth token and returns search query results")
                .path(RAS_SEARCH_QUERY_ROLE_ASSIGNMENT_URL)
                .method(HttpMethod.POST.toString())
                .body(createRoleAssignmentRequestSearchQueryMultipleRoleAssignments(), String.valueOf(ContentType.JSON))
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createRoleAssignmentResponseSearchQueryMultipleRoleAssignments())
                .toPact();
    }

    @Pact(provider = "am_roleAssignment_queryAssignment", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact executeSearchQueryRoleAssignmentEmptyCollectionAndGet200(
            PactDslWithProvider builder) {

        return builder
                .given("An empty list of role assignments for the search query")
                .uponReceiving("RAS takes s2s/auth token and returns search query results")
                .path(RAS_SEARCH_QUERY_ROLE_ASSIGNMENT_URL)
                .method(HttpMethod.POST.toString())
                .body(createRoleAssignmentRequestSearchQuery(), String.valueOf(ContentType.JSON))
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createRoleAssignmentResponseSearchQueryEmptyCollection())
                .toPact();
    }

    private String createRoleAssignmentRequestAdvancedSearchQuery() {

        return "{\"queryRequests\":[{\"actorId\":[\"14a21569-eb80-4681-b62c-6ae2ed069e5f\"]},"
                + "{\"roleName\": [\"tribunal-caseworker\"]}]}";

    }

    @Pact(provider = "am_roleAssignment_queryAssignment", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact executeAdvancedSearchQueryRoleAssignmentAndGet200(PactDslWithProvider builder) {

        return builder
                .given("A list of role assignments for the advanced search query")
                .uponReceiving("RAS takes s2s/auth token and returns advanced search query results")
                .path(RAS_SEARCH_QUERY_ROLE_ASSIGNMENT_URL)
                .method(HttpMethod.POST.toString())
                .body(createRoleAssignmentRequestAdvancedSearchQuery(), POST_ASSIGNMENTS)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeadersV2())
                .body(createRoleAssignmentResponseAdvancedSearchQuery())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeAdvancedSearchQueryRoleAssignmentAndGet200")
    void getAdvancedSearchQueryResultsAndGet200Test(MockServer mockServer)
            throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(POST_ASSIGNMENTS)
                        .body(createRoleAssignmentRequestAdvancedSearchQuery())
                        .post(mockServer.getUrl() + RAS_SEARCH_QUERY_ROLE_ASSIGNMENT_URL)
                        .then()
                        .log().all().extract().asString();

        JSONObject jsonResponse = new JSONObject(actualResponseBody);
        JSONArray roleAssignmentResponse = (JSONArray) jsonResponse.get("roleAssignmentResponse");
        JSONObject first = (JSONObject) roleAssignmentResponse.get(0);
        assertThat(first.get("actorId"), equalTo(ACTOR_ID_ADV));
    }

    @Test
    @PactTestFor(pactMethod = "executeSearchQueryRoleAssignmentAndGet200")
    void getSearchQueryResultsByActorIdAndGet200Test(MockServer mockServer)
            throws JSONException {
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
        JSONArray roleAssignmentResponse = (JSONArray) jsonResponse.get("roleAssignmentResponse");
        JSONObject first = (JSONObject) roleAssignmentResponse.get(0);
        assertThat(first.get("actorId"), equalTo(ACTOR_ID));
    }

    @Test
    @PactTestFor(pactMethod = "executeSearchQueryRoleAssignmentByRoleNameAndGet200")
    void getSearchQueryResultsByRoleNameAndGet200Test(MockServer mockServer)
            throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .body(createRoleAssignmentRequestSearchQueryByRoleName())
                        .post(mockServer.getUrl() + RAS_SEARCH_QUERY_ROLE_ASSIGNMENT_URL)
                        .then()
                        .log().all().extract().asString();

        JSONObject jsonResponse = new JSONObject(actualResponseBody);
        JSONArray roleAssignmentResponse = (JSONArray) jsonResponse.get("roleAssignmentResponse");
        JSONObject first = (JSONObject) roleAssignmentResponse.get(0);
        assertThat(first.get("roleName"), equalTo("senior-tribunal-caseworker"));
    }

    @Test
    @PactTestFor(pactMethod = "executeSearchQueryRoleAssignmentByAttributesAndGet200")
    void getSearchQueryResultsByAttributesAndGet200Test(MockServer mockServer)
            throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .body(createRoleAssignmentRequestSearchQueryByAttributes())
                        .post(mockServer.getUrl() + RAS_SEARCH_QUERY_ROLE_ASSIGNMENT_URL)
                        .then()
                        .log().all().extract().asString();

        JSONObject jsonResponse = new JSONObject(actualResponseBody);
        JSONArray roleAssignmentResponse = (JSONArray) jsonResponse.get("roleAssignmentResponse");
        JSONObject first = (JSONObject) roleAssignmentResponse.get(0);
        JSONObject attributes = (JSONObject) first.get("attributes");
        assertThat(attributes.get("primaryLocation"), equalTo("500A2S"));
        assertThat(attributes.get("jurisdiction"), equalTo("IA"));
    }

    @Test
    @PactTestFor(pactMethod = "executeSearchQueryRoleAssignmentMultipleRoleAssignmentsAndGet200")
    void getSearchQueryResultsMultipleRoleAssignmentsAndGet200Test(MockServer mockServer)
            throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .body(createRoleAssignmentRequestSearchQueryMultipleRoleAssignments())
                        .post(mockServer.getUrl() + RAS_SEARCH_QUERY_ROLE_ASSIGNMENT_URL)
                        .then()
                        .log().all().extract().asString();

        JSONObject jsonResponse = new JSONObject(actualResponseBody);
        JSONArray roleAssignmentResponse = (JSONArray) jsonResponse.get("roleAssignmentResponse");

        JSONObject first = (JSONObject) roleAssignmentResponse.get(0);
        assertThat(first.get("actorId"), equalTo("ca93ea54-c219-4c6d-add6-e687a0f1f5f7"));
        /*JSONObject second = (JSONObject)roleAssignmentResponse.get(1);
        assertThat(second.get("actorId"), equalTo("fa4c86ba-289c-4227-b924-13f55929047c"));*/
    }

    @Test
    @PactTestFor(pactMethod = "executeSearchQueryRoleAssignmentEmptyCollectionAndGet200")
    void getSearchQueryResultsEmptyListOfRoleAssignmentsAndGet200Test(MockServer mockServer)
            throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .body(createRoleAssignmentRequestSearchQuery())
                        .post(mockServer.getUrl() + RAS_SEARCH_QUERY_ROLE_ASSIGNMENT_URL)
                        .then()
                        .statusCode(200)
                        .log().all().extract().asString();

        JSONObject jsonResponse = new JSONObject(actualResponseBody);
        JSONArray roleAssignmentResponse = (JSONArray) jsonResponse.get("roleAssignmentResponse");
        assertThat(roleAssignmentResponse.length(), equalTo(0));
    }

    private DslPart createRoleAssignmentResponseSearchQuery() {
        return newJsonBody(o -> o.minArrayLike("roleAssignmentResponse", 1, 1,
            roleAssignmentResponse -> roleAssignmentResponse
                                .stringType("id", "14a21569-eb80-4681-b62c-6ae2ed069e6f")
                                .stringValue("actorIdType", "IDAM")
                                .stringValue("actorId", ACTOR_ID)
                                .stringValue("roleType", "ORGANISATION")
                                .stringValue("roleName", "senior-tribunal-caseworker")
                                .stringValue("classification", "PRIVATE")
                                .stringValue("grantType", "STANDARD")
                                .stringValue("roleCategory", "LEGAL_OPERATIONS")
                                .booleanValue("readOnly", false)
                                .object("attributes", attribute -> attribute
                                        .stringType("jurisdiction", "IA")
                                        .stringType("primaryLocation", "500A2S"))
                )).build();
    }

    private DslPart createRoleAssignmentResponseAdvancedSearchQuery() {
        return newJsonBody(o -> o.array("roleAssignmentResponse", rar -> rar
                .object(ob -> ob
                        .stringType("id", "14a21569-eb80-4681-b62c-6ae2ed069e6f")
                        .stringValue("actorIdType", "IDAM")
                        .stringValue("actorId", ACTOR_ID_ADV)
                        .stringValue("roleType", "ORGANISATION")
                        .stringValue("roleName", "senior-tribunal-caseworker")
                        .stringValue("classification", "PRIVATE")
                        .stringValue("grantType", "STANDARD")
                        .stringValue("roleCategory", "LEGAL_OPERATIONS")
                        .booleanValue("readOnly", false)
                        .object("attributes", attribute -> attribute
                                .stringType("jurisdiction", "IA")
                                .stringType("primaryLocation", "500A2S"))
                ).object(obj -> obj
                        .stringType("id", "14a21569-eb80-4681-b62c-6ae2ed069e6f")
                        .stringValue("actorIdType", "IDAM")
                        .stringValue("actorId", ACTOR_ID_ADV)
                        .stringValue("roleType", "ORGANISATION")
                        .stringValue("roleName", TRIBUNAL_CASEWORKER)
                        .stringValue("classification", "PRIVATE")
                        .stringValue("grantType", "STANDARD")
                        .stringValue("roleCategory", "LEGAL_OPERATIONS")
                        .booleanValue("readOnly", false)
                        .object("attributes", attribute -> attribute
                                .stringType("jurisdiction", "IA")
                                .stringType("primaryLocation", "500A2S"))
                )))
                .build();
    }

    private DslPart createRoleAssignmentResponseSearchQueryMultipleRoleAssignments() {
        return newJsonBody(o -> o.minArrayLike("roleAssignmentResponse", 2, 2,
            roleAssignmentResponse -> roleAssignmentResponse
                    .stringType("id", "da3c7ad9-0be1-4f72-8224-b73e3c61d22e")
                    .stringValue("actorIdType", "IDAM")
                    .stringValue("actorId", "ca93ea54-c219-4c6d-add6-e687a0f1f5f7")
                    .stringValue("roleType", "ORGANISATION")
                    .stringValue("roleName", "senior-tribunal-caseworker")
                    .stringValue("classification", "PRIVATE")
                    .stringValue("grantType", "STANDARD")
                    .stringValue("roleCategory", "LEGAL_OPERATIONS")
                    .booleanValue("readOnly", false)
                    .object("attributes", attribute -> attribute
                            .stringType("jurisdiction", "IA")
                            .stringType("primaryLocation", "219ASA"))
                )
                .minArrayLike("roleAssignmentResponse", 2, 2,
                    roleAssignmentResponse -> roleAssignmentResponse
                            .stringType("id", "da3c7ad9-0be1-4f72-8224-b73e3c61d22e")
                            .stringValue("actorIdType", "IDAM")
                            .stringValue("actorId", "ca93ea54-c219-4c6d-add6-e687a0f1f5f7")
                            .stringValue("roleType", "ORGANISATION")
                            .stringValue("roleName", "senior-tribunal-caseworker")
                            .stringValue("classification", "PRIVATE")
                            .stringValue("grantType", "STANDARD")
                            .stringValue("roleCategory", "LEGAL_OPERATIONS")
                            .booleanValue("readOnly", false)
                            .object("attributes", attribute -> attribute
                                    .stringType("jurisdiction", "IA")
                                    .stringType("primaryLocation", "219ASA"))
                )
        ).build();
    }

    private DslPart createRoleAssignmentResponseSearchQueryEmptyCollection() {
        return newJsonBody(o -> o
                .minArrayLike("roleAssignmentResponse", 0, 0, ar -> {
                })
        ).build();
    }

    @NotNull
    private Map<String, String> getResponseHeaders() {
        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type",
                "application/vnd.uk.gov.hmcts.role-assignment-service.post-assignment-query-request+json;"
                        + "charset=UTF-8;version=1.0");
        return responseHeaders;
    }

    @NotNull
    private Map<String, String> getResponseHeadersV2() {
        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type", POST_ASSIGNMENTS);
        return responseHeaders;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }
}
