package uk.gov.hmcts.reform.orgrolemapping.controller.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.resetAllRequests;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
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

    public void stubIdam() throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(post(urlPathMatching("/o/token"))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(OBJECT_MAPPER.writeValueAsString(getTokenResponse()))));
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
