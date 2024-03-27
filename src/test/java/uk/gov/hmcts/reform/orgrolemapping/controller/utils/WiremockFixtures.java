package uk.gov.hmcts.reform.orgrolemapping.controller.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.nimbusds.jose.JOSEException;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.azure.core.http.ContentType.APPLICATION_JSON;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.resetAllRequests;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static uk.gov.hmcts.reform.orgrolemapping.controller.BaseTest.WIRE_MOCK_SERVER;
import static uk.gov.hmcts.reform.orgrolemapping.util.KeyGenerator.getRsaJwk;

public class WiremockFixtures {

    public static final String RAS_CREATE_ASSIGNMENTS_URL = "/am/role-assignments";

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
        WIRE_MOCK_SERVER.stubFor(WireMock.post(urlEqualTo(RAS_CREATE_ASSIGNMENTS_URL))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)
                        .withStatus(returnHttpStatus)
                ));
    }
}