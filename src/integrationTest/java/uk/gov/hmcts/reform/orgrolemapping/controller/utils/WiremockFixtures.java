package uk.gov.hmcts.reform.orgrolemapping.controller.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.nimbusds.jose.JOSEException;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Appointment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.azure.core.http.ContentType.APPLICATION_JSON;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.resetAllRequests;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.orgrolemapping.controller.BaseTest.WIRE_MOCK_SERVER;
import static uk.gov.hmcts.reform.orgrolemapping.util.KeyGenerator.getRsaJwk;

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

    public void stubIdamConfig() throws JsonProcessingException {

        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching("/o/.well-known/openid-configuration"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(OBJECT_MAPPER.writeValueAsString(getOpenIdResponse()))
                        ));

        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching("/o/jwks"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(getJwksResponse())
                ));

    }

    private Map<String, Object> getOpenIdResponse() {
        LinkedHashMap<String,Object> data1 = new LinkedHashMap<>();
        data1.put("issuer", "http://localhost:" + WIRE_MOCK_SERVER.port() + "/o");
        data1.put("jwks_uri", "http://localhost:" + WIRE_MOCK_SERVER.port() + "/o/jwks");

        return data1;
    }

    private String getJwksResponse() {
        try {
            return "{"
                    + "\"keys\": [" + getRsaJwk().toPublicJWK().toJSONString() + "]"
                    + "}";

        } catch (JOSEException ex) {
            throw new RuntimeException(ex);
        }

    }

    public void stubRoleAssignments(String body, int returnHttpStatus) {
        WIRE_MOCK_SERVER.stubFor(WireMock.post(urlEqualTo("/am/role-assignments"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)
                        .withStatus(returnHttpStatus)
                ));
    }
}