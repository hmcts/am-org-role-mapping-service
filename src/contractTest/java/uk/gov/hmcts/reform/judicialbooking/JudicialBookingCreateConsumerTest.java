package uk.gov.hmcts.reform.judicialbooking;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.google.common.collect.Maps;
import com.launchdarkly.shaded.org.jetbrains.annotations.NotNull;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.client.fluent.Executor;
import org.assertj.core.api.Assertions;
import org.json.JSONException;
import org.json.JSONObject;
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

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;

import java.util.Map;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "am_judicialBooking_create")
@PactFolder("pacts")
@ContextConfiguration(classes = {JudicialBookingConsumerApplication.class})
@TestPropertySource(properties = {"feign.client.config.jbsClient.url=http://localhost:4097"})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class JudicialBookingCreateConsumerTest {

    private static final String JUDICIAL_BOOKING_CREATE_URL = "/am/bookings";
    private static final String USER_ID = "5629957f-4dcd-40b8-a0b2-e64ff5898b28";
    private static final String CONTENT_TYPE = "application/vnd.uk.gov.hmcts.judicial-booking-service"
            + ".create-booking+json;charset=UTF-8;version=1.0";

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    public void teardown() {
        Executor.closeIdleConnections();
    }

    private HttpHeaders getHttpHeaders() {
        var headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }

    @NotNull
    private Map<String, String> getResponseHeaders() {
        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type", CONTENT_TYPE);
        return responseHeaders;
    }

    private DslPart createJudicialBookingsResponse() {
        return newJsonBody(o -> o
                .object("bookingResponse", br -> br
                                .stringType("id", "fcb4f03c-4b3f-4c3c-bf3a-662b4557b470")
                                .stringType("userId", USER_ID)
                                .stringType("locationId", "south-east")
                                .stringType("regionId", "BFA1")
                                .stringType("beginTime", "3031-01-01T00:00:00Z")
                                .stringType("endTime", "3031-09-01T00:00:00Z")
                                .stringType("created", "2021-02-23T06:37:58Z")
                )).build();
    }

    private String createJudicialBookingsCreateRequest() {
        return "{\n"
                + "    \"bookingRequest\": {\n"
                + "        \"userId\": \"3168da13-00b3-41e3-81fa-cbc71ac28a0f\",\n"
                + "        \"locationId\": \"south\",\n"
                + "        \"regionId\": \"BFA1\",\n"
                + "        \"beginDate\": \"3031-01-01T00:00:00Z\",\n"
                + "        \"endDate\": \"3031-09-01T00:00:00Z\"\n"
                + "    }\n"
                + "}";
    }



    @Pact(provider = "am_judicialBooking_create", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact executeCreateJudicialBooking(PactDslWithProvider builder) {

        return builder
                .given("A create request is received with valid begin and end dates")
                .uponReceiving("A create request is received with a valid userId")
                .path(JUDICIAL_BOOKING_CREATE_URL)
                .body(createJudicialBookingsCreateRequest(), String.valueOf(ContentType.JSON))
                .method(HttpMethod.POST.toString())
                .willRespondWith()
                .status(HttpStatus.CREATED.value())
                .headers(getResponseHeaders())
                .body(createJudicialBookingsResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeCreateJudicialBooking")
    void executeGetJrdProfilesListOfIds(MockServer mockServer)
            throws JSONException {
        var actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .body(createJudicialBookingsCreateRequest())
                        .contentType(ContentType.fromContentType(CONTENT_TYPE))
                        .post(mockServer.getUrl() + JUDICIAL_BOOKING_CREATE_URL)
                        .then()
                        .log().all().extract().asString();

        var response = new JSONObject(actualResponseBody);
        Assertions.assertThat(response).isNotNull();

    }
}

