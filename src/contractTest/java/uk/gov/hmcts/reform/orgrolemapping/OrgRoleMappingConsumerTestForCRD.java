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
public class OrgRoleMappingConsumerTestForCRD {

    private static final String USER_ID = "234873";
    private static final String CRD_GET_ROLE_ASSIGNMENT_URL = "/refdata/case-worker/users/fetchUsersById";

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    private String createCRDUserRequest() {
        return "{\n"
                + "\"userIds\": ["
                + USER_ID + "]\n"
                + "}";
    }

    @Pact(provider = "crd_case_worker_ref_service", consumer = "am_org_role_mapping")
    public RequestResponsePact executeGetCRDProfileUsingFetchByUserIdAndGet200(PactDslWithProvider builder)
            throws IOException {

        return builder
                .given("A list of users for CRD request")
                .uponReceiving("CRD takes s2s/auth token and returns user profiles")
                .path(CRD_GET_ROLE_ASSIGNMENT_URL)
                .method( HttpMethod.POST.toString())
                .body(createCRDUserRequest(), String.valueOf( ContentType.JSON))
                .willRespondWith()
                .status( HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createCRDResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetCRDProfileUsingFetchByUserIdAndGet200")
    void getCRDProfileUsingFetchByUserIdAndGet200Test(MockServer mockServer)
            throws JSONException, IOException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .body(createCRDUserRequest())
                        .post(mockServer.getUrl() + CRD_GET_ROLE_ASSIGNMENT_URL)
                        .then()
                        .log().all().extract().asString();

        JSONObject jsonResponse = new JSONObject(actualResponseBody);
        JSONArray crdResponse = (JSONArray)jsonResponse.get("role");
        JSONObject first = (JSONObject)crdResponse.get(0);
        assertThat(first.get("“role_id”"), equalTo("1"));
    }

    private DslPart createCRDResponse() {
        return newJsonBody(o -> o
                    .stringType("id", "91e07fe0-9575-472b-bd1f-33be2944c1f4")
                    .stringValue("“idamRoles”", null)
                    .stringValue("“first_name”", "testUser1")
                    .stringValue("roleType", "ORGANISATION")
                    .stringValue("roleName", "senior-tribunal-caseworker")
                    .stringValue("classification", "PRIVATE")
                    .stringValue("grantType", "STANDARD")
                    .stringValue("roleCategory", "STAFF")
                    .booleanValue("readOnly", false)
                    .object("attributes", attribute -> attribute
                            .stringType("jurisdiction", "IA")
                            .stringType("primaryLocation", "500A2S"))
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

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }

}
