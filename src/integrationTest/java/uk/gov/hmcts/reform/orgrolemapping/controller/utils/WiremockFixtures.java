package uk.gov.hmcts.reform.orgrolemapping.controller.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Appointment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBookingResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.azure.core.http.ContentType.APPLICATION_JSON;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.resetAllRequests;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.orgrolemapping.controller.BaseTest.WIRE_MOCK_SERVER;

public class WiremockFixtures {

    public static final ObjectMapper OBJECT_MAPPER = new Jackson2ObjectMapperBuilder()
            .modules(new Jdk8Module(), new JavaTimeModule())
            .build();

    public WiremockFixtures() {
        configureFor(WIRE_MOCK_SERVER.port());
    }

    public void resetRequests() {
        resetAllRequests();
    }

    public void stubRoleAssignmentServiceWithStatus(HttpStatus status) throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(post(urlEqualTo("/am/role-assignments"))
                .willReturn(aResponse()
                        .withStatus(status.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(OBJECT_MAPPER.writeValueAsString(getRoleAssignmentRequestResource()))));
    }

    private RoleAssignmentRequestResource getRoleAssignmentRequestResource() {
        var roleAssignmentRequestResource =
                new RoleAssignmentRequestResource(AssignmentRequestBuilder.buildAssignmentRequest(false));
        roleAssignmentRequestResource.getRoleAssignmentRequest().getRequest().setReference("test ref");
        return roleAssignmentRequestResource;
    }

    public void stubRoleAssignmentServiceError() throws JsonProcessingException {
        AssignmentRequest request = AssignmentRequestBuilder.buildAssignmentRequest(false);
        request.getRequest().setReference("test ref");

        WIRE_MOCK_SERVER.stubFor(post(urlEqualTo("/am/role-assignments"))
                .willReturn(aResponse()
                        .withStatus(UNPROCESSABLE_ENTITY.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(OBJECT_MAPPER.writeValueAsString(request))));
    }

    public void stubForGetCaseworkerDetailsById(UserRequest userRequest,
                                                List<CaseWorkerProfile> response) throws JsonProcessingException {

        WIRE_MOCK_SERVER.stubFor(post(urlPathMatching("/refdata/case-worker/users/fetchUsersById"))
                .withRequestBody(new EqualToJsonPattern(
                        OBJECT_MAPPER.writeValueAsString(userRequest),
                        true,
                        true)
                ).willReturn(aResponse()
                        .withStatus(CREATED.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(OBJECT_MAPPER.writeValueAsString(response))));
    }

    public void stubForGetCaseworkerDetailsByServiceName(List<CaseWorkerProfilesResponse> response)
            throws JsonProcessingException {

        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching("/refdata/internal/staff/usersByServiceName"))
                .withQueryParam("ccd_service_names", equalTo("IA"))
                .withQueryParam("page_size", equalTo("400"))
                .withQueryParam("page_number", equalTo("0"))
                .withQueryParam("sort_direction", equalTo("ASC"))
                .withQueryParam("sort_column", equalTo(""))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withHeader("total_records", "2")
                        .withBody(OBJECT_MAPPER.writeValueAsString(response))));
    }

    public void stubForFailureGetCaseworkerDetailsByServiceName() throws JsonProcessingException {

        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching("/refdata/internal/staff/usersByServiceName"))
                .withQueryParam("ccd_service_names", equalTo("IA"))
                .withQueryParam("page_size", equalTo("400"))
                .withQueryParam("page_number", equalTo("0"))
                .withQueryParam("sort_direction", equalTo("ASC"))
                .withQueryParam("sort_column", equalTo(""))
                .willReturn(badRequest().withBody("user not found")));
    }

    public void stubForFailureRetryAndSuccessGetCaseworkerDetailsByServiceName(
            List<CaseWorkerProfilesResponse> response) throws JsonProcessingException {

        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching("/refdata/internal/staff/usersByServiceName"))
                .withQueryParam("ccd_service_names", equalTo("IA"))
                .withQueryParam("page_size", equalTo("400"))
                .withQueryParam("page_number", equalTo("0"))
                .withQueryParam("sort_direction", equalTo("ASC"))
                .withQueryParam("sort_column", equalTo(""))
                .inScenario("multiple retries")
                .whenScenarioStateIs(STARTED)
                .willSetStateTo("secondRetry")
                .willReturn(badRequest().withBody("user not found")));

        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching("/refdata/internal/staff/usersByServiceName"))
                .withQueryParam("ccd_service_names", equalTo("IA"))
                .withQueryParam("page_size", equalTo("400"))
                .withQueryParam("page_number", equalTo("0"))
                .withQueryParam("sort_direction", equalTo("ASC"))
                .withQueryParam("sort_column", equalTo(""))
                .inScenario("multiple retries")
                .whenScenarioStateIs("secondRetry")
                .willSetStateTo("success")
                .willReturn(badRequest().withBody("user not found")));

        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching("/refdata/internal/staff/usersByServiceName"))
                .withQueryParam("ccd_service_names", equalTo("IA"))
                .withQueryParam("page_size", equalTo("400"))
                .withQueryParam("page_number", equalTo("0"))
                .withQueryParam("sort_direction", equalTo("ASC"))
                .withQueryParam("sort_column", equalTo(""))
                .inScenario("multiple retries")
                .whenScenarioStateIs("success")
                .willSetStateTo(STARTED)
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withHeader("total_records", "2")
                        .withBody(OBJECT_MAPPER.writeValueAsString(response))));
    }

    public void stubForGetJudicialDetailsById(HttpStatus status, String userId) throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(post(urlPathMatching("/refdata/judicial/users"))
                .willReturn(aResponse()
                        .withStatus(status.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withHeader("total_records", "1")
                        .withBody(OBJECT_MAPPER.writeValueAsString(buildJudicialProfilesResponse(userId)))));
    }

    private List<JudicialProfile> buildJudicialProfilesResponse(String userId) {
        List<JudicialProfile> bookings = new ArrayList<>();
        bookings.add(JudicialProfile.builder()
                .sidamId(userId)
                .appointments(
                        List.of(Appointment.builder()
                                        .appointment("Tribunal Judge")
                                        .appointmentType("Fee Paid")
                                        .build()
                        )
                )
                .build());
        return bookings;
    }

    public void stubForGetJudicialDetailsByIdFailure(HttpStatus status) throws JsonProcessingException {
        Map<String, String> body = Map.of(
                "errorDescription", "The User Profile data could not be found",
                "status", "Not Found");

        WIRE_MOCK_SERVER.stubFor(post(urlPathMatching("/refdata/judicial/users"))
                .willReturn(aResponse()
                        .withStatus(status.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(OBJECT_MAPPER.writeValueAsString(body))));

    }

    public void stubForGetJudicialBookingByUserIds(String... userIds) throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(post(urlPathMatching("/am/bookings/query"))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withHeader("total_records", "1")
                        .withBody(OBJECT_MAPPER.writeValueAsString(buildJudicialBookingResponse(userIds)))));
    }

    private JudicialBookingResponse buildJudicialBookingResponse(String... userIds) {
        List<JudicialBooking> bookings = new ArrayList<>();

        for (String userId : userIds) {
            bookings.add(
                    JudicialBooking.builder()
                            .beginTime(ZonedDateTime.now())
                            .endTime(ZonedDateTime.now().plusDays(5))
                            .userId(userId)
                            .locationId("location")
                            .regionId("region")
                            .build()
            );
        }

        return new JudicialBookingResponse(bookings);
    }

    public void stubIdam() throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(post(urlPathMatching("/o/token"))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(OBJECT_MAPPER.writeValueAsString(getTokenResponse()))));

        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching("/o/userinfo"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(OBJECT_MAPPER.writeValueAsString(getUserInfoResponse()))
                        .withTransformers("external_user-token-response")));

    }

    private Map<String, Object> getUserInfoResponse() {
        LinkedHashMap<String,Object> data1 = new LinkedHashMap<>();

        data1.put("id","%s");
        data1.put("uid","%s");
        data1.put("forename","Super");
        data1.put("surname","User");
        data1.put("email","dummy@email.com");
        data1.put("roles", List.of("%s"));

        return data1;
    }

    private TokenResponse getTokenResponse() {
        return new TokenResponse(
                "user1",
                "expiresInValue",
                "idTokenValue",
                "refreshTokenValue",
                "scopeValue",
                "tokenTypeValue"
        );
    }
}
