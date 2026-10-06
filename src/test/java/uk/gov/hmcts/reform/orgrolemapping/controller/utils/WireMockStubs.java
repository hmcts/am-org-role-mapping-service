package uk.gov.hmcts.reform.orgrolemapping.controller.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static uk.gov.hmcts.reform.orgrolemapping.util.KeyGenerator.getRsaJwk;

public class WireMockStubs {

    public static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static final String SERVICE_NAME = "xui_webapp";

    private final WireMockServer wireMockServer;

    public WireMockStubs(WireMockServer wireMockServer) {
        this.wireMockServer = wireMockServer;
    }

    public void stubIdamConfig() throws JsonProcessingException {

        wireMockServer.stubFor(get(urlPathEqualTo("/o/.well-known/openid-configuration"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(OBJECT_MAPPER.writeValueAsString(getOpenIdResponse()))
                ));

        wireMockServer.stubFor(get(urlPathEqualTo("/o/jwks"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(getJwksResponse())
                ));
    }

    public void stubAuthorisationDetails(String serviceName) {
        wireMockServer.stubFor(get(urlPathEqualTo("/details"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(serviceName)
                ));
    }

    public void stubAuthorisationUserInfo(String actorId) throws JsonProcessingException {
        wireMockServer.stubFor(get(urlPathEqualTo("/o/userinfo"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(OBJECT_MAPPER.writeValueAsString(getUserInfo(actorId)))
                ));
    }

    private UserInfo getUserInfo(String actorId) {
        return UserInfo.builder()
                .uid(actorId)
                .givenName("Super")
                .familyName("User")
                .roles(List.of("someRole"))
                .build();
    }

    private Map<String, Object> getOpenIdResponse() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("issuer", "http://localhost:" + wireMockServer.port() + "/o");
        data.put("jwks_uri", "http://localhost:" + wireMockServer.port() + "/o/jwks");
        return data;
    }

    private String getJwksResponse() {
        try {
            Map<String, Object> jwks = Map.of(
                    "keys", List.of(getRsaJwk().toPublicJWK().toJSONObject())
            );
            return OBJECT_MAPPER.writeValueAsString(jwks);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}