package uk.gov.hmcts.reform.orgrolemapping;

import au.com.dius.pact.consumer.MockServer;
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

@Slf4j
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
    public RequestResponsePact executeCreateRoleAssignmentAndGet201(PactDslWithProvider builder) {

        return builder
                .given("The assignment request is valid")
                .uponReceiving("RAS creates role assignments")
                .path(RAS_CREATE_ROLE_ASSIGNMENT_URL)
                .method(HttpMethod.POST.toString())
                .body(createRoleAssignmentRequest(), String.valueOf(ContentType.JSON))
                .willRespondWith()
                .status(HttpStatus.CREATED.value())
                .headers(getRoleAssignmentResponseHeaders())
                .body(createRoleAssignmentResponse())
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
    @PactTestFor(pactMethod = "executeCreateRoleAssignmentAndGet201")
    void createRoleAssignmentAndGet201Test(MockServer mockServer)
            throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .contentType(ContentType.JSON)
                        .body(createRoleAssignmentRequest())
                        .post(mockServer.getUrl() + RAS_CREATE_ROLE_ASSIGNMENT_URL)
                        .then()
                        .log().all().extract().asString();

        JSONObject response =  new JSONObject(actualResponseBody);
        Assertions.assertThat(response).isNotNull();

        JSONObject roleRequest = response.getJSONObject("roleAssignmentResponse").getJSONObject("roleRequest");
        JSONArray requestedRoles = response.getJSONObject("roleAssignmentResponse").getJSONArray("requestedRoles");
        JSONObject role1 = requestedRoles.getJSONObject(0);
        JSONObject role2 = requestedRoles.getJSONObject(1);

        assertThat(roleRequest.get("status"), equalTo("APPROVED"));
        assertThat(roleRequest.get("requestType"), equalTo("CREATE"));
        assertThat(roleRequest.get("assignerId"), equalTo(roleRequest.get("authenticatedUserId")));
        assertThat(roleRequest.get("replaceExisting"), equalTo(false));
        assertThat(role1.get("classification"), equalTo("PUBLIC"));
        assertThat(role1.get("roleType"), equalTo("CASE"));
        assertThat(role1.get("roleName"), equalTo("judge"));
        assertThat(role1.get("grantType"), equalTo("SPECIFIC"));
        assertThat(role1.get("roleCategory"), equalTo("JUDICIAL"));
        assertThat(role2.get("classification"), equalTo("PRIVATE"));
        assertThat(role2.get("roleType"), equalTo("CASE"));
        assertThat(role2.get("roleName"), equalTo("judge1"));
        assertThat(role2.get("grantType"), equalTo("CHALLENGED"));
        assertThat(role2.get("roleCategory"), equalTo("JUDICIAL"));
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

    private String createRoleAssignmentRequest() {
        String request = "";
        request = "{\n" +
                "  \"roleRequest\":\n" +
                "  {\n" +
                "    \"assignerId\": \"52aa3810-af1f-11ea-b3de-0242ac130004\",\n" +
                "    \"process\": \"Process1\",\n" +
                "    \"reference\": \"03352d0-e699-48bc-b6f5-5810411e60ad\",\n" +
                "    \"replaceExisting\": false\n" +
                "  },\n" +
                "  \"requestedRoles\":\n" +
                "  [\n" +
                "    {\n" +
                "      \"actorIdType\": \"IDAM\",\n" +
                "      \"actorId\": \"003352d0-e699-48bc-b6f5-5810411e60af\",\n" +
                "      \"roleType\": \"CASE\",\n" +
                "      \"roleName\": \"JUDGE\",\n" +
                "      \"roleCategory\": \"JUDICIAL\",\n" +
                "      \"classification\": \"PUBLIC\",\n" +
                "      \"grantType\": \"SPECIFIC\",\n" +
                "      \"readOnly\": false,\n" +
                "      \"beginTime\": \"2020-01-01T00:00\",\n" +
                "      \"endTime\": \"2020-12-01T00:00\",\n" +
                "      \"attributes\":\n" +
                "      {\n" +
                "        \"jurisdiction\": \"divorce\",\n" +
                "        \"region\": \"north-east\",\n" +
                "        \"contractType\": \"SALARIED\",\n" +
                "        \"caseId\": \"1234567890123456\"\n" +
                "      },\n" +
                "      \"notes\":\n" +
                "      [\n" +
                "        {\n" +
                "          \"userId\": \"003352d0-e699-48bc-b6f5-5810411e60af\",\n" +
                "          \"time\": \"2020-01-01T00:00\",\n" +
                "          \"comment\": \"Need Access to case number 1234567890123456 for a year\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"userId\": \"52aa3810-af1f-11ea-b3de-0242ac130004\",\n" +
                "          \"time\": \"2020-01-02T00:00\",\n" +
                "          \"comment\": \"Access granted for 3 months\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"actorIdType\": \"IDAM\",\n" +
                "      \"actorId\": \"85378679-286a-4385-a6ae-442588e9789323\",\n" +
                "      \"roleType\": \"CASE\",\n" +
                "      \"roleName\": \"JUDGE\",\n" +
                "      \"roleCategory\": \"JUDICIAL\",\n" +
                "      \"classification\": \"PUBLIC\",\n" +
                "      \"grantType\": \"CHALLENGED\",\n" +
                "      \"readOnly\": true,\n" +
                "      \"endTime\": \"2050-01-01T00:00\",\n" +
                "      \"attributes\":\n" +
                "      {\n" +
                "        \"jurisdiction\": \"divorce\",\n" +
                "        \"region\": \"north-east\",\n" +
                "        \"contractType\": \"FEEPAY\",\n" +
                "        \"caseId\": \"1234567890123457\"\n" +
                "      },\n" +
                "      \"notes\":\n" +
                "      [\n" +
                "        {\n" +
                "          \"userId\": \"85378679-286a-4385-a6ae-442588e9789323\",\n" +
                "          \"time\": \"2020-01-01T00:00\",\n" +
                "          \"comment\": \"Need Access to case number 1234567890123457 for a year\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"userId\": \"52aa3810-af1f-11ea-b3de-0242ac130004\",\n" +
                "          \"time\": \"2020-01-02T00:00\",\n" +
                "          \"comment\": \"Access granted\"\n" +
                "        }\n" +
                "\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        return request;
    }

    private String createRoleAssignmentResponse() {
        String response = "";
        response = "{\n" +
                "    \"roleAssignmentResponse\": {\n" +
                "        \"roleRequest\": {\n" +
                "            \"id\": \"bb955236-944b-47ed-ba48-b692d95b45af\",\n" +
                "            \"correlationId\": \"21334a2b-79ce-44eb-9168-2d49a744be9d\",\n" +
                "            \"assignerId\": \"21334a2b-79ce-44eb-9168-2d49a744be9c\",\n" +
                "            \"authenticatedUserId\": \"21334a2b-79ce-44eb-9168-2d49a744be9c\",\n" +
                "            \"requestType\": \"CREATE\",\n" +
                "            \"status\": \"APPROVED\",\n" +
                "            \"process\": \"businessProcess1\",\n" +
                "            \"reference\": \"03352d0-e699-48bc-b6f5-5810411e60ad\",\n" +
                "            \"replaceExisting\": false,\n" +
                "            \"created\": \"2020-01-01T00:00:00\",\n" +
                "            \"log\": \"All assignments approved\"\n" +
                "        },\n" +
                "        \"requestedRoles\": [\n" +
                "            {\n" +
                "                \"id\": \"083715f6-c315-4e20-846e-4958a90c2224\",\n" +
                "                \"actorIdType\": \"IDAM\",\n" +
                "                \"actorId\": \"21334a2b-79ce-44eb-9168-2d49a744be9c\",\n" +
                "                \"roleType\": \"CASE\",\n" +
                "                \"roleName\": \"judge\",\n" +
                "                \"classification\": \"PUBLIC\",\n" +
                "                \"grantType\": \"SPECIFIC\",\n" +
                "                \"roleCategory\": \"JUDICIAL\",\n" +
                "                \"status\": \"LIVE\",\n" +
                "                \"readOnly\": false,\n" +
                "                \"beginTime\": \"2020-07-01T00:00:00\",\n" +
                "                \"endTime\": \"2020-09-01T00:00:00\",\n" +
                "                \"created\": \"2020-06-01T00:00:00\",\n" +
                "                \"process\": \"businessProcess1\",\n" +
                "                \"reference\": \"03352d0-e699-48bc-b6f5-5810411e60ad\",\n" +
                "                \"statusSequence\": 10,\n" +
                "                \"log\": \"All assignments approved\",\n" +
                "                \"attributes\": {\n" +
                "                    \"jurisdiction\": \"divorce\",\n" +
                "                    \"region\": \"north-east\",\n" +
                "                    \"contractType\": \"SALARIED\",\n" +
                "                    \"caseId\": \"1234567890123456\"\n" +
                "                },\n" +
                "                \"notes\": [\n" +
                "                    {\n" +
                "                        \"userId\": \"003352d0-e699-48bc-b6f5-5810411e60af\",\n" +
                "                        \"time\": \"2020-01-01T00:00\",\n" +
                "                        \"comment\": \"Need Access to case number 1234567890123456 for a year\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"userId\": \"52aa3810-af1f-11ea-b3de-0242ac130004\",\n" +
                "                        \"time\": \"2020-01-02T00:00\",\n" +
                "                        \"comment\": \"Access granted for 3 months\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"id\": \"083715f6-c315-4e20-846e-4958a90c2225\",\n" +
                "                \"actorIdType\": \"IDAM\",\n" +
                "                \"actorId\": \"21334a2b-79ce-44eb-9168-2d49a744be9d\",\n" +
                "                \"roleType\": \"CASE\",\n" +
                "                \"roleName\": \"judge1\",\n" +
                "                \"classification\": \"PRIVATE\",\n" +
                "                \"grantType\": \"CHALLENGED\",\n" +
                "                \"roleCategory\": \"JUDICIAL\",\n" +
                "                \"status\": \"LIVE\",\n" +
                "                \"readOnly\": true,\n" +
                "                \"beginTime\": \"2020-07-01T00:00:00\",\n" +
                "                \"endTime\": \"2020-09-01T00:00:00\",\n" +
                "                \"created\": \"2020-06-01T00:00:00\",\n" +
                "                \"process\": \"businessProcess1\",\n" +
                "                \"reference\": \"03352d0-e699-48bc-b6f5-5810411e60ad\",\n" +
                "                \"statusSequence\": 10,\n" +
                "                \"log\": \"All assignments approved\",\n" +
                "                \"attributes\": {\n" +
                "                    \"jurisdiction\": \"divorce\",\n" +
                "                    \"region\": \"north-east\",\n" +
                "                    \"contractType\": \"FEEPAY\",\n" +
                "                    \"caseId\": \"1234567890123457\"\n" +
                "                },\n" +
                "                \"notes\": [\n" +
                "                    {\n" +
                "                        \"userId\": \"21334a2b-79ce-44eb-9168-2d49a744be9d\",\n" +
                "                        \"time\": \"2020-01-01T00:00\",\n" +
                "                        \"comment\": \"Need Access to case number 1234567890123457 for 9 months\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"userId\": \"52aa3810-af1f-11ea-b3de-0242ac130004\",\n" +
                "                        \"time\": \"2020-01-02T00:00\",\n" +
                "                        \"comment\": \"Access granted\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        return response;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }
}
