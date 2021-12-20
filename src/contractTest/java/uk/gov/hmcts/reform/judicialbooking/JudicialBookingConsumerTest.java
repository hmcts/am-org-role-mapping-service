package uk.gov.hmcts.reform.judicialbooking;

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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBookingRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;

import java.util.List;
import java.util.Map;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "am_judicialBooking_query")
@PactFolder("pacts")
@ContextConfiguration(classes = {JudicialBookingConsumerApplication.class})
@TestPropertySource(properties = {"feign.client.config.jbsClient.url=http://localhost:4097"})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class JudicialBookingConsumerTest {

    private static final String JUDICIAL_BOOKING_QUERY_URL = "/am/bookings/query";
    private static final String USER_ID = "5629957f-4dcd-40b8-a0b2-e64ff5898b28";
    private static final String USER_ID2 = "5629957f-4dcd-40b8-a0b2-e64ff5898b29";

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }

    @NotNull
    private Map<String, String> getResponseHeaders() {
        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type", "application/json");
        return responseHeaders;
    }

    private DslPart createJudicialBookingsResponse() {
        return newJsonBody(o -> o
                .array("bookings", rar -> rar
                        .object(ob -> ob
                                .stringType("id", "fcb4f03c-4b3f-4c3c-bf3a-662b4557b470")
                                .stringType("userId", USER_ID)
                                .stringType("locationId", "south-east")
                                .stringType("regionId", "BFA1")
                                .stringType("beginTime", "2031-01-01T00:00:00Z")
                                .stringType("endTime", "2031-09-01T00:00:00Z")
                                .stringType("created", "2021-02-23T06:37:58Z"))
                        .object(ob -> ob
                                .stringType("id", "fcb4f03c-4b3f-4c3c-bf3a-662b4557b471")
                                .stringType("userId", USER_ID2)
                                .stringType("locationId", "north-west")
                                .stringType("regionId", "BFA1")
                                .stringType("beginTime", "2032-01-01T00:00:00Z")
                                .stringType("endTime", "2032-09-01T00:00:00Z")
                                .stringType("created", "2021-03-23T06:37:58Z"))))
                .build();
    }

    private JudicialBookingRequest createJudicialBookingsQueryRequest() {
        return JudicialBookingRequest.builder()
                .queryRequest(UserRequest.builder()
                        .userIds(List.of(USER_ID, USER_ID2)).build()).build();
    }

    @Pact(provider = "am_judicialBooking_query", consumer = "accessMgmt_judicialBooking")
    public RequestResponsePact executeQueryJudicialBooking(PactDslWithProvider builder) throws JsonProcessingException {

        return builder
                .given("A query request is received with a valid userId passed")
                .uponReceiving("A query request is received with a valid userId passed")
                .path(JUDICIAL_BOOKING_QUERY_URL)
                .body(new ObjectMapper().writeValueAsString(createJudicialBookingsQueryRequest()))
                .method(HttpMethod.POST.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createJudicialBookingsResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeQueryJudicialBooking")
    void executeGetJrdProfilesListOfIds(MockServer mockServer)
            throws JSONException {
        var actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .body(createJudicialBookingsQueryRequest())
                        .contentType(ContentType.JSON)
                        .post(mockServer.getUrl() + JUDICIAL_BOOKING_QUERY_URL)
                        .then()
                        .log().all().extract().asString();

        var response = new JSONObject(actualResponseBody);
        Assertions.assertThat(response).isNotNull();

    }
}
