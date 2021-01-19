package uk.gov.hmcts.reform.orgrolemapping;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
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
import org.junit.After;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "crd_case_worker_ref_service")
@PactFolder("pacts")
@SpringBootTest
public class OrgRoleMappingConsumerTestForCRD {

    private static final String USER_ID = "234873";
    private static final String USER_ID2 = "234879";
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

    private String createCRDMultipleUsersRequest() {
        return "{\n"
                + "\"userIds\": ["
                + USER_ID + "\n"
                + "," + "\n"
                + USER_ID2  + "]\n"
                + "}";
    }

    @Pact(provider = "crd_case_worker_ref_service", consumer = "am_org_role_mapping")
    public RequestResponsePact executeGetCRDProfileUsingFetchByUserIdAndGet200(PactDslWithProvider builder) {

        return builder
                .given("A list of users for CRD request")
                .uponReceiving("CRD takes s2s/auth token and returns user profiles")
                .path(CRD_GET_ROLE_ASSIGNMENT_URL)
                .method(HttpMethod.POST.toString())
                .body(createCRDUserRequest(),String.valueOf(ContentType.JSON))
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createCRDResponse())
                .toPact();
    }

    @Pact(provider = "crd_case_worker_ref_service", consumer = "am_org_role_mapping")
    public RequestResponsePact executeGetCRDProfileMultipleUsers_FetchByUserIdAndGet200(PactDslWithProvider builder) {

        return builder
                .given("A list of multiple users for CRD request")
                .uponReceiving("CRD takes s2s/auth token and returns user profiles")
                .path(CRD_GET_ROLE_ASSIGNMENT_URL)
                .method(HttpMethod.POST.toString())
                .body(createCRDMultipleUsersRequest(),String.valueOf(ContentType.JSON))
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createCRDResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetCRDProfileUsingFetchByUserIdAndGet200")
    void getCRDProfileUsingFetchByUserIdAndGet200Test(MockServer mockServer) {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .body(createCRDUserRequest())
                        .post(mockServer.getUrl() + CRD_GET_ROLE_ASSIGNMENT_URL)
                        .then()
                        .log().all().extract().asString();

        JSONArray jsonResponse = new JSONArray(actualResponseBody);
        Assertions.assertNotNull(jsonResponse);
    }

    @Test
    @PactTestFor(pactMethod = "executeGetCRDProfileMultipleUsers_FetchByUserIdAndGet200")
    void getCRDProfileMultipleUsersUsingFetchByUserIdAndGet200Test(MockServer mockServer) {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .body(createCRDMultipleUsersRequest())
                        .post(mockServer.getUrl() + CRD_GET_ROLE_ASSIGNMENT_URL)
                        .then()
                        .log().all().extract().asString();

        JSONArray jsonResponse = new JSONArray(actualResponseBody);
        Assertions.assertNotNull(jsonResponse);
    }

    private DslPart createCRDResponse() {
        return new PactDslJsonArray().arrayEachLike()
                .stringType("id", USER_ID)
                .stringType("first_name", "testFirstname")
                .stringType("last_name", "TestSurname")
                .stringType("email_id", "sam.test@gmail.com")
                .integerType("region_id", 1)
                .stringType("region", "National")
                .stringType("user_type", "HMCTS")
                    .eachLike("role")
                        .stringType("role_id","1")
                        .stringType("role","tribunal-caseworker")
                        .booleanValue("is_primary",true)
                    .closeObject()
                .closeArray()
                    .eachLike("base_location")
                        .integerType("location_id",219164)
                        .stringType("location","Aberdeen Tribunal Hearing Centre")
                        .booleanValue("is_primary",true)
                    .closeObject()
                .closeArray()
                    .eachLike("work_area")
                    .stringType("area_of_work","1")
                    .stringType("service_code","BFA1")
                    .closeObject()
                .closeArray();
    }

    @NotNull
    private Map<String, String> getResponseHeaders() {
        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type",
                "application/json");
        return responseHeaders;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }
}