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
    public RequestResponsePact executeGetCRDProfileUsingFetchByUserIdAndGet200(PactDslWithProvider builder)
            throws IOException {

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
    public RequestResponsePact executeGetCRDProfileMultipleUsersUsingFetchByUserIdAndGet200(PactDslWithProvider builder)
            throws IOException {

        return builder
                .given("A list of multiple users for CRD request")
                .uponReceiving("CRD takes s2s/auth token and returns user profiles")
                .path(CRD_GET_ROLE_ASSIGNMENT_URL)
                .method(HttpMethod.POST.toString())
                .body(createCRDMultipleUsersRequest(),String.valueOf(ContentType.JSON))
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createCRDMultipleUsersResponse())
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
        JSONArray crdResponse = (JSONArray)jsonResponse.get("");
        JSONObject firstRecord = (JSONObject)crdResponse.get(0);
        JSONArray roleObject = (JSONArray)firstRecord.get("role");
        JSONObject roleRecord = (JSONObject)roleObject.get(0);
        assertThat(roleRecord.get("role"), equalTo("senior-tribunal-caseworker"));
    }

    @Test
    @PactTestFor(pactMethod = "executeGetCRDProfileMultipleUsersUsingFetchByUserIdAndGet200")
    void getCRDProfileMultipleUsersUsingFetchByUserIdAndGet200Test(MockServer mockServer)
            throws JSONException, IOException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .body(createCRDMultipleUsersRequest())
                        .post(mockServer.getUrl() + CRD_GET_ROLE_ASSIGNMENT_URL)
                        .then()
                        .log().all().extract().asString();

        JSONObject jsonResponse = new JSONObject(actualResponseBody);
        JSONArray crdResponse = (JSONArray)jsonResponse.get("");
        JSONObject firstRecord = (JSONObject)crdResponse.get(0);
        JSONArray roleObject = (JSONArray)firstRecord.get("role");
        JSONObject roleRecord = (JSONObject)roleObject.get(0);
        assertThat(roleRecord.get("role"), equalTo("senior-tribunal-caseworker"));

        JSONObject secondRecord = (JSONObject)crdResponse.get(1);
        JSONArray roleObject2 = (JSONArray)secondRecord.get("work_area");
        JSONObject roleRecord2 = (JSONObject)roleObject2.get(0);
        assertThat(roleRecord2.get("service_code"), equalTo("BFA1"));
    }

    private DslPart createCRDResponse() {
        return newJsonBody(o -> o
                .minArrayLike("", 1, 1,
                    crdResponse -> crdResponse
                    .stringType("id", "91e07fe0-9575-472b-bd1f-33be2944c1f4")
                    .stringValue("idamRoles", null)
                    .stringValue("first_name", "testFirstname")
                    .stringValue("last_name", "TestSurname")
                    .stringValue("email_id", "sam.test@gmail.com")
                    .stringValue("regionId", "1")
                    .stringValue("region", "National")
                    .array("base_location", (bl) -> {
                        bl.object((bo) -> {
                            bo
                                .stringType("location_id", "2191654")
                                .stringType("location", "Aberdeen Tribunal Hearing Centre")
                                .booleanValue("is_primary", true);
                        });
                    })
                    .stringValue("user_type_id", "1")
                    .stringValue("user_type", "HMCTS")
                    .array("role", (r) -> {
                        r.object((ro) -> {
                            ro
                                .stringType("role_id", "1")
                                .stringType("role", "senior-tribunal-caseworker")
                                .booleanValue("is_primary", true);
                        });
                    })
                    .array("work_area", (wa) -> {
                        wa.object((wo) -> {
                            wo
                                .stringType("service_code", "BFA1")
                                .stringType("area_of_work", "1");
                        });
                    })
                    .booleanValue("suspended", false)
                )
        ).build();
    }

    private DslPart createCRDMultipleUsersResponse() {
        return newJsonBody(o -> o
                .minArrayLike("", 2, 2,
                    crdResponse -> crdResponse
                                .stringType("id", "91e07fe0-9575-472b-bd1f-33be2944c1f4")
                                .stringValue("idamRoles", null)
                                .stringValue("first_name", "testFirstname")
                                .stringValue("last_name", "TestSurname")
                                .stringValue("email_id", "sam.test@gmail.com")
                                .stringValue("regionId", "1")
                                .stringValue("region", "National")
                                .array("base_location", (bl) -> {
                                    bl.object((bo) -> {
                                        bo
                                                .stringType("location_id", "2191654")
                                                .stringType("location", "Aberdeen Tribunal Hearing Centre")
                                                .booleanValue("is_primary", true);
                                    });
                                })
                                .stringValue("user_type_id", "1")
                                .stringValue("user_type", "HMCTS")
                                .array("role", (r) -> {
                                    r.object((ro) -> {
                                        ro
                                                .stringType("role_id", "1")
                                                .stringType("role", "senior-tribunal-caseworker")
                                                .booleanValue("is_primary", true);
                                    });
                                })
                                .array("work_area", (wa) -> {
                                    wa.object((wo) -> {
                                        wo
                                                .stringType("service_code", "BFA1")
                                                .stringType("area_of_work", "1");
                                    });
                                })
                                .booleanValue("suspended", false)
                )
                .minArrayLike("", 2, 2,
                    crdResponse -> crdResponse
                                .stringType("id", "91e07fe0-9575-472b-bd1f-33be2944c1f4")
                                .stringValue("idamRoles", null)
                                .stringValue("first_name", "testFirstname")
                                .stringValue("last_name", "TestSurname")
                                .stringValue("email_id", "sam.test@gmail.com")
                                .stringValue("regionId", "1")
                                .stringValue("region", "National")
                                .array("base_location", (bl) -> {
                                    bl.object((bo) -> {
                                        bo
                                                .stringType("location_id", "2191654")
                                                .stringType("location", "Aberdeen Tribunal Hearing Centre")
                                                .booleanValue("is_primary", true);
                                    });
                                })
                                .stringValue("user_type_id", "1")
                                .stringValue("user_type", "HMCTS")
                                .array("role", (r) -> {
                                    r.object((ro) -> {
                                        ro
                                                .stringType("role_id", "1")
                                                .stringType("role", "senior-tribunal-caseworker")
                                                .booleanValue("is_primary", true);
                                    });
                                })
                                .array("work_area", (wa) -> {
                                    wa.object((wo) -> {
                                        wo
                                                .stringType("service_code", "BFA1")
                                                .stringType("area_of_work", "1");
                                    });
                                })
                                .booleanValue("suspended", false)
                )
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