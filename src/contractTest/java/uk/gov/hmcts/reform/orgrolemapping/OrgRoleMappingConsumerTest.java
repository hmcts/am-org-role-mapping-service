package uk.gov.hmcts.reform.orgrolemapping;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.google.common.collect.Maps;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.client.fluent.Executor;
import org.assertj.core.api.Assertions;
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
import static org.hamcrest.CoreMatchers.equalTo;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "am_role_assignment_service")
@PactFolder("pacts")
@SpringBootTest
public class OrgRoleMappingConsumerTest {

    private static final String RAS_GET_LIST_ROLES_URL = "/am/role-assignments/roles";
    private static final String RAS_CREATE_ROLE_ASSIGNMENT_URL = "/am/role-assignments";

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "am_role_assignment_service", consumer = "am_org_role_mapping")
    public RequestResponsePact executeGetListOfRolesAndGet200(PactDslWithProvider builder) {

        return builder
                .given("A list of roles are available in role assignment service")
                .uponReceiving("RAS takes s2s/auth token and returns list of roles")
                .path(RAS_GET_LIST_ROLES_URL)
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createResponse())
                .toPact();
    }

    @Pact(provider = "am_role_assignment_service", consumer = "am_org_role_mapping")
    public RequestResponsePact executeCreateRoleAssignmentReplacingExistingFalseAndGet201(PactDslWithProvider builder) {

        return builder
                .given("The assignment request is valid with one requested role and replaceExisting flag as false")
                .uponReceiving("role assignment service takes s2s/auth token and create a role assignment")
                .path(RAS_CREATE_ROLE_ASSIGNMENT_URL)
                .method(HttpMethod.POST.toString())
                .body(createRoleAssignmentRequestReplaceExistingFalse(), String.valueOf(ContentType.JSON))
                .willRespondWith()
                .status(HttpStatus.CREATED.value())
                .headers(getRoleAssignmentResponseHeaders())
                .body(createRoleAssignmentResponseReplaceExistingFalse())
                .toPact();
    }

    @Pact(provider = "am_role_assignment_service", consumer = "am_org_role_mapping")
    public RequestResponsePact executeCreateRoleAssignmentOneRoleAndGet201(PactDslWithProvider builder) {

        return builder
                .given("The assignment request is valid with one requested role and replaceExisting flag as true")
                .uponReceiving("role assignment service takes s2s/auth token and create or update a role assignment")
                .path(RAS_CREATE_ROLE_ASSIGNMENT_URL)
                .method(HttpMethod.POST.toString())
                .body(createRoleAssignmentRequestOneRole(), String.valueOf(ContentType.JSON))
                .willRespondWith()
                .status(HttpStatus.CREATED.value())
                .headers(getRoleAssignmentResponseHeaders())
                .body(createRoleAssignmentResponseOneRole())
                .toPact();
    }

    @Pact(provider = "am_role_assignment_service", consumer = "am_org_role_mapping")
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

    @NotNull
    private Map<String, String> getResponseHeaders() {
        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type",
                "application/vnd.uk.gov.hmcts.role-assignment-service.get-roles+json;charset=UTF-8;version=1.0");
        return responseHeaders;
    }

    @NotNull
    private Map<String, String> getRoleAssignmentResponseHeaders() {
        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type", "application/json");
        return responseHeaders;
    }

    @Test
    @PactTestFor(pactMethod = "executeGetListOfRolesAndGet200")
    void getListOfRolesAndGet200Test(MockServer mockServer)
            throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .get(mockServer.getUrl() + RAS_GET_LIST_ROLES_URL)
                        .then()
                        .log().all().extract().asString();
        JSONArray jsonArray = new JSONArray(actualResponseBody);
        JSONObject role = jsonArray.getJSONObject(0);
        assertThat(role.get("name"), equalTo("judge"));
        assertThat(role.get("description"), equalTo("Judicial office holder able to do judicial case work"));
        assertThat(role.get("label"), equalTo("Judge - Sample role (Only for Testing)"));
        assertThat(role.get("category"), equalTo("JUDICIAL"));
    }

    @Test
    @PactTestFor(pactMethod = "executeCreateRoleAssignmentReplacingExistingFalseAndGet201")
    void createRoleAssignmentReplaceExistingFalseAndGet201Test(MockServer mockServer)
            throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .body(createRoleAssignmentRequestReplaceExistingFalse())
                        .post(mockServer.getUrl() + RAS_CREATE_ROLE_ASSIGNMENT_URL)
                        .then()
                        .log().all().extract().asString();

        JSONObject response =  new JSONObject(actualResponseBody);
        Assertions.assertThat(response).isNotNull();
        JSONObject roleRequest = response.getJSONObject("roleAssignmentResponse").getJSONObject("roleRequest");
        assertThat(roleRequest.get("status"), equalTo("APPROVED"));
        assertThat(roleRequest.get("requestType"), equalTo("CREATE"));
        assertThat(roleRequest.get("process"), equalTo("S-028"));
        assertThat(roleRequest.get("reference"), equalTo("S-028"));
        assertThat(roleRequest.get("replaceExisting"), equalTo(false));

        JSONArray requestedRoles = response.getJSONObject("roleAssignmentResponse").getJSONArray("requestedRoles");
        JSONObject role1 = requestedRoles.getJSONObject(0);
        assertThat(role1.get("classification"), equalTo("PUBLIC"));
        assertThat(role1.get("roleType"), equalTo("ORGANISATION"));
        assertThat(role1.get("roleName"), equalTo("judge"));
        assertThat(role1.get("grantType"), equalTo("STANDARD"));
        assertThat(role1.get("roleCategory"), equalTo("JUDICIAL"));

    }

    @Test
    @PactTestFor(pactMethod = "executeCreateRoleAssignmentOneRoleAndGet201")
    void createRoleAssignmentOneRoleAndGet201Test(MockServer mockServer)
            throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .body(createRoleAssignmentRequestOneRole())
                        .post(mockServer.getUrl() + RAS_CREATE_ROLE_ASSIGNMENT_URL)
                        .then()
                        .log().all().extract().asString();

        JSONObject response =  new JSONObject(actualResponseBody);
        Assertions.assertThat(response).isNotNull();
        JSONObject roleRequest = response.getJSONObject("roleAssignmentResponse").getJSONObject("roleRequest");
        assertThat(roleRequest.get("status"), equalTo("APPROVED"));
        assertThat(roleRequest.get("requestType"), equalTo("CREATE"));
        assertThat(roleRequest.get("process"), equalTo("S-028"));
        assertThat(roleRequest.get("reference"), equalTo("S-028"));
        assertThat(roleRequest.get("replaceExisting"), equalTo(true));

        JSONArray requestedRoles = response.getJSONObject("roleAssignmentResponse").getJSONArray("requestedRoles");
        JSONObject role1 = requestedRoles.getJSONObject(0);
        assertThat(role1.get("classification"), equalTo("PUBLIC"));
        assertThat(role1.get("roleType"), equalTo("ORGANISATION"));
        assertThat(role1.get("roleName"), equalTo("judge"));
        assertThat(role1.get("grantType"), equalTo("STANDARD"));
        assertThat(role1.get("roleCategory"), equalTo("JUDICIAL"));

    }

    @Test
    @PactTestFor(pactMethod = "executeCreateRoleAssignmentZeroRoleAndGet201")
    void createRoleAssignmentZeroRoleAndGet201Test(MockServer mockServer)
            throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .body(createRoleAssignmentRequestZeroRole())
                        .post(mockServer.getUrl() + RAS_CREATE_ROLE_ASSIGNMENT_URL)
                        .then()
                        .log().all().extract().asString();

        JSONObject response =  new JSONObject(actualResponseBody);
        Assertions.assertThat(response).isNotNull();
        JSONObject roleRequest = response.getJSONObject("roleAssignmentResponse").getJSONObject("roleRequest");
        assertThat(roleRequest.get("status"), equalTo("APPROVED"));
        assertThat(roleRequest.get("requestType"), equalTo("CREATE"));
        assertThat(roleRequest.get("process"), equalTo("S-028"));
        assertThat(roleRequest.get("reference"), equalTo("S-028"));
        assertThat(roleRequest.get("replaceExisting"), equalTo(true));

        JSONArray requestedRoles = response.getJSONObject("roleAssignmentResponse").getJSONArray("requestedRoles");
        assertThat(requestedRoles.isEmpty(),equalTo(true));

    }

    private String createResponse() {
        String response = "";
        response = "[\n"
                + "  {\n"
                + "    \"name\": \"judge\",\n"
                + "    \"label\": \"Judge - Sample role (Only for Testing)\",\n"
                + "    \"description\": \"Judicial office holder able to do judicial case work\",\n"
                + "    \"category\": \"JUDICIAL\"\n"
                + "  },\n"
                + "  {\n"
                + "    \"name\": \"tribunal-caseworker\",\n"
                + "    \"label\": \"Tribunal Caseworker\",\n"
                + "    \"description\": \"Tribunal caseworker\",\n"
                + "    \"category\": \"STAFF\"\n"
                + "  },\n"
                + "  {\n"
                + "    \"name\": \"senior-tribunal-caseworker\",\n"
                + "    \"label\": \"Senior Tribunal Caseworker\",\n"
                + "    \"description\": \"Senior Tribunal caseworker\",\n"
                + "    \"category\": \"STAFF\"\n"
                + "  }\n"
                + "]";
        return response;
    }

    private String createRoleAssignmentRequestReplaceExistingFalse() {
        String request = "";
        request = "{\n"
                + "    \"roleRequest\": {\n"
                + "        \"assignerId\": \"3168da13-00b3-41e3-81fa-cbc71ac28a0f\",\n"
                + "        \"process\": \"S-028\",\n"
                + "        \"reference\": \"S-028\",\n"
                + "        \"replaceExisting\": false\n"
                + "    },\n"
                + "    \"requestedRoles\": [\n"
                + "        {\n"
                + "        \"actorIdType\": \"IDAM\",\n"
                + "        \"actorId\": \"3168da13-00b3-41e3-81fa-cbc71ac28a0f\",\n"
                + "        \"roleType\": \"ORGANISATION\",\n"
                + "        \"roleName\": \"judge\",\n"
                + "        \"roleCategory\": \"JUDICIAL\",\n"
                + "        \"classification\": \"PUBLIC\",\n"
                + "        \"grantType\": \"STANDARD\",\n"
                + "        \"readOnly\": false,\n"
                + "        \"beginTime\": \"2021-01-01T00:00\",\n"
                + "        \"endTime\": \"2023-01-01T00:00\",\n"
                + "        \"attributes\": {\n"
                + "            \"jurisdiction\": \"divorce\",\n"
                + "            \"region\": \"south-east\",\n"
                + "            \"contractType\": \"SALARIED\",\n"
                + "            \"caseId\": \"1234567890123456\"\n"
                + "        }\n"
                + "    }\n"
                + "    ]\n"
                + "}";
        return request;
    }

    private String createRoleAssignmentResponseReplaceExistingFalse() {
        String response = "";
        response = "{\n"
                + "    \"links\": [],\n"
                + "    \"roleAssignmentResponse\": {\n"
                + "        \"roleRequest\": {\n"
                + "            \"id\": \"c3552563-80e1-49a1-9dc9-b2625e7c44dc\",\n"
                + "            \"authenticatedUserId\": \"96983ff2-844a-4938-9905-10ac4a9bddff\",\n"
                + "            \"correlationId\": \"01f6e7e2-c66c-44a0-a7e4-73c1507c92b7\",\n"
                + "            \"assignerId\": \"3168da13-00b3-41e3-81fa-cbc71ac28a0f\",\n"
                + "            \"requestType\": \"CREATE\",\n"
                + "            \"process\": \"S-028\",\n"
                + "            \"reference\": \"S-028\",\n"
                + "            \"replaceExisting\": false,\n"
                + "            \"status\": \"APPROVED\",\n"
                + "            \"created\": \"2020-11-19T18:42:45.138597\",\n"
                + "            \"log\": \"Request has been Approved\"\n"
                + "        },\n"
                + "        \"requestedRoles\": [\n"
                + "            {\n"
                + "                \"id\": \"14a21569-eb80-4681-b62c-6ae2ed069e7f\",\n"
                + "                \"actorIdType\": \"IDAM\",\n"
                + "                \"actorId\": \"3168da13-00b3-41e3-81fa-cbc71ac28a0f\",\n"
                + "                \"roleType\": \"ORGANISATION\",\n"
                + "                \"roleName\": \"judge\",\n"
                + "                \"classification\": \"PUBLIC\",\n"
                + "                \"grantType\": \"STANDARD\",\n"
                + "                \"roleCategory\": \"JUDICIAL\",\n"
                + "                \"readOnly\": false,\n"
                + "                \"beginTime\": \"2021-01-01T00:00:00\",\n"
                + "                \"endTime\": \"2023-01-01T00:00:00\",\n"
                + "                \"process\": \"S-028\",\n"
                + "                \"reference\": \"S-028\",\n"
                + "                \"status\": \"LIVE\",\n"
                + "                \"created\": \"2020-11-19T18:42:45.138666\",\n"
                + "                \"log\": \"Create approved : judicial_organisational_role_mapping_"
                + "service_create\\nApproved "
                + ": validate_role_assignment_against_patterns\",\n"
                + "                \"attributes\": {\n"
                + "                    \"jurisdiction\": \"divorce\",\n"
                + "                    \"region\": \"south-east\",\n"
                + "                    \"contractType\": \"SALARIED\",\n"
                + "                    \"caseId\": \"1234567890123456\"\n"
                + "                },\n"
                + "                \"notes\": null\n"
                + "            }\n"
                + "        ]\n"
                + "    }\n"
                + "}\n";
        return response;
    }

    private String createRoleAssignmentRequestOneRole() {
        String request = "";
        request = "{\n"
                + "    \"roleRequest\": {\n"
                + "        \"assignerId\": \"3168da13-00b3-41e3-81fa-cbc71ac28a0f\",\n"
                + "        \"process\": \"S-028\",\n"
                + "        \"reference\": \"S-028\",\n"
                + "        \"replaceExisting\": true\n"
                + "    },\n"
                + "    \"requestedRoles\": [\n"
                + "        {\n"
                + "        \"actorIdType\": \"IDAM\",\n"
                + "        \"actorId\": \"3168da13-00b3-41e3-81fa-cbc71ac28a0f\",\n"
                + "        \"roleType\": \"ORGANISATION\",\n"
                + "        \"roleName\": \"judge\",\n"
                + "        \"roleCategory\": \"JUDICIAL\",\n"
                + "        \"classification\": \"PUBLIC\",\n"
                + "        \"grantType\": \"STANDARD\",\n"
                + "        \"readOnly\": false,\n"
                + "        \"beginTime\": \"2021-01-01T00:00\",\n"
                + "        \"endTime\": \"2023-01-01T00:00\",\n"
                + "        \"attributes\": {\n"
                + "            \"jurisdiction\": \"divorce\",\n"
                + "            \"region\": \"south-east\",\n"
                + "            \"contractType\": \"SALARIED\",\n"
                + "            \"caseId\": \"1234567890123456\"\n"
                + "        }\n"
                + "    }\n"
                + "    ]\n"
                + "}\n";
        return request;
    }

    private String createRoleAssignmentResponseOneRole() {
        String response = "";
        response = "{\n"
                + "    \"links\": [],\n"
                + "    \"roleAssignmentResponse\": {\n"
                + "        \"roleRequest\": {\n"
                + "            \"id\": \"2b8128bc-379e-4d71-97e2-45b568a3b281\",\n"
                + "            \"authenticatedUserId\": \"96983ff2-844a-4938-9905-10ac4a9bddff\",\n"
                + "            \"correlationId\": \"01f6e7e2-c66c-44a0-a7e4-73c1507c92b7\",\n"
                + "            \"assignerId\": \"3168da13-00b3-41e3-81fa-cbc71ac28a0f\",\n"
                + "            \"requestType\": \"CREATE\",\n"
                + "            \"process\": \"S-028\",\n"
                + "            \"reference\": \"S-028\",\n"
                + "            \"replaceExisting\": true,\n"
                + "            \"status\": \"APPROVED\",\n"
                + "            \"created\": \"2020-11-19T18:43:55.60211\",\n"
                + "            \"log\": \"Duplicate Request: Requested Assignments are already live.\"\n"
                + "        },\n"
                + "        \"requestedRoles\": [\n"
                + "            {\n"
                + "                \"id\": \"14a21569-eb80-4681-b62c-6ae2ed069e7f\",\n"
                + "                \"actorIdType\": \"IDAM\",\n"
                + "                \"actorId\": \"3168da13-00b3-41e3-81fa-cbc71ac28a0f\",\n"
                + "                \"roleType\": \"ORGANISATION\",\n"
                + "                \"roleName\": \"judge\",\n"
                + "                \"classification\": \"PUBLIC\",\n"
                + "                \"grantType\": \"STANDARD\",\n"
                + "                \"roleCategory\": \"JUDICIAL\",\n"
                + "                \"readOnly\": false,\n"
                + "                \"beginTime\": \"2021-01-01T00:00:00\",\n"
                + "                \"endTime\": \"2023-01-01T00:00:00\",\n"
                + "                \"process\": \"S-028\",\n"
                + "                \"reference\": \"S-028\",\n"
                + "                \"status\": \"LIVE\",\n"
                + "                \"created\": \"2020-11-19T18:42:45.20874\",\n"
                + "                \"log\": \"Create approved : judicial_organisational_role_mapping_service_"
                + "create\\nApproved : validate_role_assignment_against_patterns\",\n"
                + "                \"attributes\": {\n"
                + "                    \"contractType\": \"SALARIED\",\n"
                + "                    \"caseId\": \"1234567890123456\",\n"
                + "                    \"jurisdiction\": \"divorce\",\n"
                + "                    \"region\": \"south-east\"\n"
                + "                },\n"
                + "                \"notes\": null,\n"
                + "                \"authorisations\": []\n"
                + "            }\n"
                + "        ]\n"
                + "    }\n"
                + "}";
        return response;
    }

    private String createRoleAssignmentRequestZeroRole() {
        String request = "";
        request = "{\n"
                + "    \"roleRequest\": {\n"
                + "        \"assignerId\": \"3168da13-00b3-41e3-81fa-cbc71ac28a0f\",\n"
                + "        \"process\": \"S-028\",\n"
                + "        \"reference\": \"S-028\",\n"
                + "        \"replaceExisting\": true\n"
                + "    },\n"
                + "    \"requestedRoles\": []\n"
                + "}";
        return request;
    }

    private String createRoleAssignmentResponseZeroRole() {
        String response = "";
        response = "{\n"
                + "    \"links\": [],\n"
                + "    \"roleAssignmentResponse\": {\n"
                + "        \"roleRequest\": {\n"
                + "            \"id\": \"1d1b56da-6cd3-416e-a52e-ddc713e44c67\",\n"
                + "            \"authenticatedUserId\": \"96983ff2-844a-4938-9905-10ac4a9bddff\",\n"
                + "            \"correlationId\": \"01f6e7e2-c66c-44a0-a7e4-73c1507c92b7\",\n"
                + "            \"assignerId\": \"3168da13-00b3-41e3-81fa-cbc71ac28a0f\",\n"
                + "            \"requestType\": \"CREATE\",\n"
                + "            \"process\": \"S-028\",\n"
                + "            \"reference\": \"S-028\",\n"
                + "            \"replaceExisting\": true,\n"
                + "            \"status\": \"APPROVED\",\n"
                + "            \"created\": \"2020-11-19T18:44:52.954524\",\n"
                + "            \"log\": \"Request has been Approved\"\n"
                + "        },\n"
                + "        \"requestedRoles\": []\n"
                + "    }\n"
                + "}";
        return response;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }
}
