package uk.gov.hmcts.reform.refdata;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.client.fluent.Executor;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JRDUserRequest;

import java.util.Map;
import java.util.Set;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonArray;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@PactTestFor(providerName = "referenceData_judicialv2", port = "8991")
@ContextConfiguration(classes = {RefDataCaseworkerConsumerApplication.class})
@TestPropertySource(properties = {"feign.client.config.crdclient.url=http://localhost:8991"})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class RefDataJudicialProfileConsumerTestV2 {

    private static final String JRD_GET_PROFILES_URL = "/refdata/judicial/users";

    private static final String SIDAM_ID = "44362987-4b00-f2e7-4ff8-761b87f16bf9";


    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "referenceData_judicialv2", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact getJrdProfilesListOfIds(PactDslWithProvider builder) throws JsonProcessingException {

        return builder
                .given("return judicial user profiles v2 along with their active appointments and authorisations")
                .uponReceiving("the api returns judicial user profiles "
                        + "based on the provided list of user ids")
                .path(JRD_GET_PROFILES_URL)
                .body(new ObjectMapper().writeValueAsString(
                        JRDUserRequest.builder().sidamIds(Set.of(SIDAM_ID)).build()))
                .method(HttpMethod.POST.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createJrdProfilesResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getJrdProfilesListOfIds")
    void executeGetJrdProfilesListOfIds(MockServer mockServer)
            throws JSONException {
        var actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .body(JRDUserRequest.builder().sidamIds(
                                Set.of(SIDAM_ID)).build())
                        .contentType(ContentType.JSON)
                        .post(mockServer.getUrl() + JRD_GET_PROFILES_URL)
                        .then()
                        .log().all().extract().asString();

        JSONArray response = new JSONArray(actualResponseBody);
        Assertions.assertThat(response).isNotNull();

    }

    @Pact(provider = "referenceData_judicialv2", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact getJrdProfilesServiceName(PactDslWithProvider builder) throws JsonProcessingException {

        return builder
                .given("return judicial user profiles v2 along with their active appointments and authorisations")
                .uponReceiving("the api returns judicial user profiles "
                        + "based on the provided service name")
                .path(JRD_GET_PROFILES_URL)
                .body(new ObjectMapper().writeValueAsString(
                        JRDUserRequest.builder().ccdServiceNames("CMC").build()))
                .method(HttpMethod.POST.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createJrdProfilesResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getJrdProfilesServiceName")
    void executeGetJrdProfilesServiceName(MockServer mockServer)
            throws JSONException {
        var actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .body(JRDUserRequest.builder().ccdServiceNames("CMC").build())
                        .contentType(ContentType.JSON)
                        .post(mockServer.getUrl() + JRD_GET_PROFILES_URL)
                        .then()
                        .log().all().extract().asString();

        JSONArray response = new JSONArray(actualResponseBody);
        Assertions.assertThat(response).isNotNull();

    }

    private DslPart createJrdProfilesResponse() {
        return newJsonArray(o -> o.object(ob -> ob
                .stringType("sidam_id", SIDAM_ID)
                .stringType("full_name", "testFullName")
                .stringType("known_as", "testKnownAs")
                .stringType("surname", "surname")
                .stringType("email_id", "test@test.com")
                .stringType("title", "Family Judge")
                .stringType("personal_code", "1234")
                .stringType("post_nominals", "Mr")
                .stringType("initials", "I N")
                .minArrayLike("appointments", 1, r -> r
                        .stringType("base_location_id")
                        .stringType("epimms_id")
                        .stringType("cft_region_id")
                        .stringType("cft_region")
                        .stringType("is_principal_appointment")
                        .date("start_date", "yyyy-MM-dd")
                        .date("end_date", "yyyy-MM-dd")
                        .stringType("appointment")
                        .stringType("appointment_type")
                        .array("service_codes", (s) -> {
                            s.stringType("BFA1");
                        })
                        .stringType("appointment_id")
                )
                .minArrayLike("authorisations", 1, r -> r
                        .stringType("jurisdiction")
                        .stringType("ticket_description")
                        .datetime("start_date", "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
                        .array("service_codes", (s) -> {
                            s.stringType("BFA1");
                        })
                        .stringType("ticket_code")
                        .datetime("end_date", "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
                        .stringType("appointment_id")
                        .stringType("authorisation_id")
                        .stringType("jurisdiction_id")
                )
                .minArrayLike("roles", 1, r -> r
                        .stringType("jurisdiction_role_name")
                        .stringType("jurisdiction_role_id")
                        .datetime("start_date", "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
                        .datetime("end_date", "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
                )
        )).build();
    }

    @NotNull
    private Map<String, String> getResponseHeaders() {
        Map<String, String> responseHeaders = Map.of("Content-Type", "application/vnd.jrd.api+json;Version=2.0");
        return responseHeaders;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        headers.add("Accept", "application/vnd.jrd.api+json;Version=2.0");
        return headers;
    }

}
