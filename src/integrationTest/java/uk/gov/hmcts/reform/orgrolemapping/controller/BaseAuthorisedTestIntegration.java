package uk.gov.hmcts.reform.orgrolemapping.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WireMockStubs;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"testing.support.enabled=true"})
@ExtendWith(SerenityJUnit5Extension.class)
@WithTags({@WithTag("testType:Integration")})
public abstract class BaseAuthorisedTestIntegration extends BaseTestIntegration {

    protected static final String BASEURL = "http://localhost";
    protected static final String REFRESH_URL = "/am/role-mapping/professional/refresh";

    protected static final String ACTOR_ID1 = "631d322c-eea7-4d53-bd92-e6ec51bcb390";
    protected static final String ACTOR_ID2 = "eccd490f-a40c-4a19-b063-7b107bc6480p";
    private static final long WAIT_TIME_MS = 1000;

    @LocalServerPort
    private int serverPort;

    protected RequestSpecification getRequestSpecification(String serviceName,
                                                           String actorId,
                                                           HttpHeaders httpHeaders)
            throws JsonProcessingException, InterruptedException {
        resetWiremockServer(serviceName, actorId);
        return SerenityRest.given()
                .baseUri(BASEURL)
                .port(serverPort)
                .headers(httpHeaders);
    }

    public static void resetWiremockServer(String serviceName, String actorId)
            throws JsonProcessingException, InterruptedException {

        // Clear the stubs and requests
        WIRE_MOCK_SERVER.resetAll();

        WireMockStubs wireMockStubs = new WireMockStubs(WIRE_MOCK_SERVER);

        // Recreate the stubs
        wireMockStubs.stubIdamConfig();
        wireMockStubs.stubAuthorisationDetails(serviceName);
        wireMockStubs.stubAuthorisationUserInfo(actorId);

        Thread.sleep(WAIT_TIME_MS);
    }
}