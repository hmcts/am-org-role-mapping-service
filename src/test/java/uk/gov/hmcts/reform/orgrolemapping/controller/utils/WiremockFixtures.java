package uk.gov.hmcts.reform.orgrolemapping.controller.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.nimbusds.jose.JOSEException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JRDUserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBookingRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.azure.core.http.ContentType.APPLICATION_JSON;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.resetAllRequests;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.orgrolemapping.controller.BaseTest.WIRE_MOCK_SERVER;
import static uk.gov.hmcts.reform.orgrolemapping.util.KeyGenerator.getRsaJwk;

public class WiremockFixtures {

    public static final String RAS_CREATE_ASSIGNMENTS_URL = "/am/role-assignments";
    public static final String SERVICE_NAME_EXUI = "xui_webapp";
    public static final String SERVICE_NAME_ORM = "am_org_role_mapping_service";

    public static final String ACTOR_ID1 = "631d322c-eea7-4d53-bd92-e6ec51bcb390";
    public static final String ACTOR_ID2 = "123e4567-e89b-42d3-a456-556642445678";

    private static final UUID AUTH_DETAILS_EXUI =
            UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
    private static final UUID AUTH_DETAILS_ORM =
            UUID.fromString("4a1a5f3c-8b9f-4d2e-9f7c-5f6e8c9d0a2b");
    private static final UUID AUTH_DETAILS_ACTOR1 =
            UUID.fromString("9f8c7d6e-5b4a-3c2d-1e0f-9a8b7c6d5e4f");
    private static final UUID AUTH_DETAILS_ACTOR2 =
            UUID.fromString("0f1e2d3c-4b5a-6d7e-8f9a-0b1c2d3e4f5a");

    public static final ObjectMapper OBJECT_MAPPER = new Jackson2ObjectMapperBuilder()
            .modules(new Jdk8Module(), new JavaTimeModule())
            .build();

    public WiremockFixtures() {
        configureFor(WIRE_MOCK_SERVER.port());
    }

    public void resetRequests() {
        resetAllRequests();
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

    public void stubAuthorisationDetails(String serviceName) {
        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching("/details"))
                .withId(getUuidForServiceName(serviceName))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(serviceName)
                ));
    }

    private UUID getUuidForServiceName(String serviceName) {
        switch (serviceName) {
            case SERVICE_NAME_EXUI:
                return AUTH_DETAILS_EXUI;
            case SERVICE_NAME_ORM:
                return AUTH_DETAILS_ORM;
            default:
                return UUID.randomUUID();
        }
    }

    public void stubAuthorisationUserInfo(String actorId)
            throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching("/o/userinfo"))
                .withId(getUuidForActorIdentity(actorId))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(OBJECT_MAPPER.writeValueAsString(getUserInfo(actorId)))
                ));
    }

    private UUID getUuidForActorIdentity(String actorId) {
        switch (actorId) {
            case ACTOR_ID1:
                return AUTH_DETAILS_ACTOR1;
            case ACTOR_ID2:
                return AUTH_DETAILS_ACTOR2;
            default:
                return UUID.randomUUID();
        }
    }

    public void stubIdamSystemUser() throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(post(urlPathMatching("/o/token"))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(OBJECT_MAPPER.writeValueAsString(getTokenResponse()))
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

    public void stubRoleAssignmentsBasicResponse(HttpStatus status) {
        stubRoleAssignments("{}", status.value());
    }

    @SuppressWarnings("unused") // userRequest currently unused in stub
    public void stubGetJudicialBookingByUserIds(JudicialBookingRequest userRequest, String body)  {
        WIRE_MOCK_SERVER.stubFor(post(urlPathMatching("/am/bookings/query"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(body)));
    }

    @SuppressWarnings("unused") // userRequest currently unused in stub
    public void stubGetJudicialDetailsById(JRDUserRequest userRequest, String body) {
        WIRE_MOCK_SERVER.stubFor(post(urlPathMatching("/refdata/judicial/users"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withHeader("total_records", "1")
                .withBody(body)));
    }

    public void stubRoleAssignments(String body, int returnHttpStatus) {
        WIRE_MOCK_SERVER.stubFor(WireMock.post(urlEqualTo(RAS_CREATE_ASSIGNMENTS_URL))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)
                        .withStatus(returnHttpStatus)
                ));
    }

    private UserInfo getUserInfo(String actorId) {
        return UserInfo.builder()
                .uid(actorId)
                .givenName("Super")
                .familyName("User")
                .roles(List.of("%s"))
                .build();
    }
}
