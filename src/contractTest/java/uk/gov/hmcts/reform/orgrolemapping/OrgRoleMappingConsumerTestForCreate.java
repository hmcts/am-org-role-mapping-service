package uk.gov.hmcts.reform.orgrolemapping;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import groovy.util.logging.Slf4j;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.CRDTopicPublisher;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.JRDTopicPublisher;

import javax.sql.DataSource;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "am_roleAssignment_createAssignment", pactVersion = PactSpecVersion.V3)
@PactFolder("pacts")
@TestPropertySource(properties = {
    "idam.api.url=http://localhost:5000",
    "spring.cache.type=simple"
})
public class OrgRoleMappingConsumerTestForCreate extends BaseTestContract {

    private static final String RAS_CREATE_ROLE_ASSIGNMENT_URL = "/am/role-assignments";

    @Bean
    JRDTopicPublisher jrdPublisher() {
        return Mockito.mock(JRDTopicPublisher.class);
    }

    @Bean
    CRDTopicPublisher crdPublisher() {
        return Mockito.mock(CRDTopicPublisher.class);
    }

    @Bean
    @Qualifier("crdPublisher")
    ServiceBusSenderClient serviceBusSenderClient() {
        return Mockito.mock(ServiceBusSenderClient.class);
    }

    @Bean
    @Qualifier("jrdPublisher")
    ServiceBusSenderClient serviceBusSenderClientJrd() {
        return Mockito.mock(ServiceBusSenderClient.class);
    }

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }


    @Pact(provider = "am_roleAssignment_createAssignment", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact executeCreateRoleAssignmentReplacingExistingFalseAndGet201(PactDslWithProvider builder) {

        return builder
                .given("The assignment request is valid with one requested role and replaceExisting flag as "
                        + "false")
                .uponReceiving("role assignment service takes s2s/auth token and create a role assignment")
                .path(RAS_CREATE_ROLE_ASSIGNMENT_URL)
                .method(HttpMethod.POST.toString())
                .body(createRoleAssignmentRequest("false"), String.valueOf(ContentType.JSON))
                .willRespondWith()
                .status(HttpStatus.CREATED.value())
                .headers(getRoleAssignmentResponseHeaders())
                .body(createRoleAssignmentResponse(false))
                .toPact();
    }

    @Pact(provider = "am_roleAssignment_createAssignment", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact executeCreateRoleAssignmentOneRoleAndGet201(PactDslWithProvider builder) {

        return builder
                .given("The assignment request is valid with one requested role and replaceExisting flag as true")
                .uponReceiving("role assignment service takes s2s/auth token and create or update a role assignment")
                .path(RAS_CREATE_ROLE_ASSIGNMENT_URL)
                .method(HttpMethod.POST.toString())
                .body(createRoleAssignmentRequest("true"), String.valueOf(ContentType.JSON))
                .willRespondWith()
                .status(HttpStatus.CREATED.value())
                .headers(getRoleAssignmentResponseHeaders())
                .body(createRoleAssignmentResponse(true))
                .toPact();
    }

    @Pact(provider = "am_roleAssignment_createAssignment", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact executeCreateRoleAssignmentZeroRoleAndGet201(PactDslWithProvider builder) {

        return builder
                .given("The assignment request is valid with zero requested role and replaceExisting flag as true")
                .uponReceiving("role assignment service takes s2s/auth token and create zero role assignment")
                .path(RAS_CREATE_ROLE_ASSIGNMENT_URL)
                .method(HttpMethod.POST.toString())
                .body(createRoleAssignmentRequestZeroRole(), String.valueOf(ContentType.JSON))
                .willRespondWith()
                .status(HttpStatus.CREATED.value())
                .headers(getRoleAssignmentResponseHeaders())
                .body(createRoleAssignmentResponseZeroRole())
                .toPact();
    }


    private String createRoleAssignmentRequest(String replaceExisting) {
        var request = "";
        request = "{\n"
                + "    \"roleRequest\": {\n"
                + "        \"assignerId\": \"3168da13-00b3-41e3-81fa-cbc71ac28a0f\",\n"
                + "        \"process\": \"staff-organisational-role-mapping\",\n"
                + "        \"reference\": \"14a21569-eb80-4681-b62c-6ae2ed069e5f\",\n"
                + "        \"replaceExisting\": " + replaceExisting + "\n"
                + "    },\n"
                + "    \"requestedRoles\": [\n"
                + "        {\n"
                + "        \"actorIdType\": \"IDAM\",\n"
                + "        \"actorId\": \"14a21569-eb80-4681-b62c-6ae2ed069e5f\",\n"
                + "        \"roleType\": \"ORGANISATION\",\n"
                + "        \"roleName\": \"judge\",\n"
                + "        \"roleCategory\": \"JUDICIAL\",\n"
                + "        \"classification\": \"PUBLIC\",\n"
                + "        \"grantType\": \"STANDARD\",\n"
                + "        \"readOnly\": false,\n"
                + "        \"attributes\": {\n"
                + "            \"jurisdiction\": \"IA\",\n"
                + "            \"region\": \"south-east\",\n"
                + "        }\n"
                + "    }\n"
                + "    ]\n"
                + "}";
        return request;
    }

    private String createRoleAssignmentRequestZeroRole() {
        var request = "";
        request = "{\n"
                + "    \"roleRequest\": {\n"
                + "        \"assignerId\": \"3168da13-00b3-41e3-81fa-cbc71ac28a0f\",\n"
                + "        \"process\": \"staff-organisational-role-mapping\",\n"
                + "        \"reference\": \"14a21569-eb80-4681-b62c-6ae2ed069e5f\",\n"
                + "        \"replaceExisting\": true\n"
                + "    },\n"
                + "    \"requestedRoles\": []\n"
                + "}";
        return request;
    }

    private DslPart createRoleAssignmentResponse(boolean replaceExisting) {
        return  new PactDslJsonBody().object("roleAssignmentResponse")
                .object("roleRequest")
                .stringType("id", "14a21569-eb80-4681-b62c-6ae2ed069e1f")
                .stringType("authenticatedUserId", "14a21569-eb80-4681-b62c-6ae2ed069e2f")
                .stringType("correlationId", "14a21569-eb80-4681-b62c-6ae2ed069e3f")
                .stringType("assignerId", "14a21569-eb80-4681-b62c-6ae2ed069e4f")
                .stringValue("requestType", "CREATE")
                .stringValue("process", "staff-organisational-role-mapping")
                .stringValue("reference", "14a21569-eb80-4681-b62c-6ae2ed069e5f")
                .booleanValue("replaceExisting", replaceExisting)
                .stringValue("status", "APPROVED")
                .stringType("log", "Request has been Approved")
                .minArrayLike("requestedRoles", 1)
                    .stringType("id", "14a21569-eb80-4681-b62c-6ae2ed069e6f")
                    .stringValue("actorIdType", "IDAM")
                    .stringType("actorId", "14a21569-eb80-4681-b62c-6ae2ed069e5f")
                    .stringValue("roleType", "ORGANISATION")
                    .stringValue("roleName", "judge")
                    .stringValue("classification", "PUBLIC")
                    .stringValue("grantType", "STANDARD")
                    .stringValue("roleCategory", "JUDICIAL")
                    .stringValue("process", "staff-organisational-role-mapping")
                    .stringValue("reference", "14a21569-eb80-4681-b62c-6ae2ed069e5f")
                    .stringValue("status", "LIVE")
                    .stringType("log",
                            "Create approved : "
                                    + "judicial_organisational_role_mapping_service_create\\"
                                    + "nApproved "
                                    + ": validate_role_assignment_against_patterns")
                    .minArrayLike("attributes", 1)
                        .stringType("jurisdiction", "IA")
                        .stringType("region", "south-east")
                    .closeArray()
                .closeArray()
                .closeObject()
                .closeObject();
    }

    private DslPart createRoleAssignmentResponseZeroRole() {
        return new PactDslJsonBody()
                .object("roleAssignmentResponse")
                    .object("roleRequest")
                        .stringType("id", "14a21569-eb80-4681-b62c-6ae2ed069e1f")
                        .stringType("authenticatedUserId", "14a21569-eb80-4681-b62c-6ae2ed069e2f")
                        .stringType("correlationId", "14a21569-eb80-4681-b62c-6ae2ed069e3f")
                        .stringType("assignerId", "14a21569-eb80-4681-b62c-6ae2ed069e4f")
                        .stringValue("requestType", "CREATE")
                        .stringValue("process", "staff-organisational-role-mapping")
                        .stringValue("reference", "14a21569-eb80-4681-b62c-6ae2ed069e5f")
                        .booleanValue("replaceExisting", true)
                        .stringValue("status", "APPROVED")
                        .stringType("log", "Request has been Approved")
                    .closeObject()
                    .array("requestedRoles")
                    .closeArray()
                .closeObject();
    }

    @Test
    @PactTestFor(pactMethod = "executeCreateRoleAssignmentReplacingExistingFalseAndGet201",
            pactVersion = PactSpecVersion.V3)
    void createRoleAssignmentReplaceExistingFalseAndGet201Test(MockServer mockServer)
            throws JSONException {
        var actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .body(createRoleAssignmentRequest("false"))
                        .post(mockServer.getUrl() + RAS_CREATE_ROLE_ASSIGNMENT_URL)
                        .then()
                        .log().all().extract().asString();

        JSONObject response = new JSONObject(actualResponseBody);
        Assertions.assertThat(response).isNotNull();
        JSONObject roleRequest = response.getJSONObject("roleAssignmentResponse").getJSONObject("roleRequest");
        assertThat(roleRequest.get("status"), equalTo("APPROVED"));
        assertThat(roleRequest.get("requestType"), equalTo("CREATE"));

    }

    @Test
    @PactTestFor(pactMethod = "executeCreateRoleAssignmentOneRoleAndGet201", pactVersion = PactSpecVersion.V3)
    void createRoleAssignmentOneRoleAndGet201Test(MockServer mockServer)
            throws JSONException {
        var actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .body(createRoleAssignmentRequest("true"))
                        .post(mockServer.getUrl() + RAS_CREATE_ROLE_ASSIGNMENT_URL)
                        .then()
                        .log().all().extract().asString();

        JSONObject response = new JSONObject(actualResponseBody);
        Assertions.assertThat(response).isNotNull();
        JSONObject roleRequest = response.getJSONObject("roleAssignmentResponse").getJSONObject("roleRequest");
        assertThat(roleRequest.get("status"), equalTo("APPROVED"));
        assertThat(roleRequest.get("requestType"), equalTo("CREATE"));
        assertThat(roleRequest.get("replaceExisting"), equalTo(true));

    }

    @Test
    @PactTestFor(pactMethod = "executeCreateRoleAssignmentZeroRoleAndGet201", pactVersion = PactSpecVersion.V3)
    void createRoleAssignmentZeroRoleAndGet201Test(MockServer mockServer) throws JSONException {
        var actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .body(createRoleAssignmentRequestZeroRole())
                        .post(mockServer.getUrl() + RAS_CREATE_ROLE_ASSIGNMENT_URL)
                        .then()
                        .log().all().extract().asString();

        JSONObject response = new JSONObject(actualResponseBody);
        Assertions.assertThat(response).isNotNull();
        JSONObject roleRequest = response.getJSONObject("roleAssignmentResponse").getJSONObject("roleRequest");
        assertThat(roleRequest.get("status"), equalTo("APPROVED"));
        assertThat(roleRequest.get("requestType"), equalTo("CREATE"));
        assertThat(roleRequest.get("replaceExisting"), equalTo(true));

        JSONArray requestedRoles = response.getJSONObject("roleAssignmentResponse").getJSONArray("requestedRoles");
        //assertThat(requestedRoles.isEmpty(), equalTo(true));
    }

    @NotNull
    private Map<String, String> getRoleAssignmentResponseHeaders() {
        Map<String, String> responseHeaders =
                Map.of("Content-Type", "application/vnd.uk.gov.hmcts.role-assignment-service."
                + "create-assignments+json");
        return responseHeaders;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }
}
