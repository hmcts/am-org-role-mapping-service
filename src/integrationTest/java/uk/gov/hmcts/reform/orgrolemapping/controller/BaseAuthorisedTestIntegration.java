package uk.gov.hmcts.reform.orgrolemapping.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.JOSEException;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;

import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_XUI;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures.ACTOR_ID1;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith({SerenityJUnit5Extension.class, SpringExtension.class})
@WithTags({@WithTag("testType:Integration")})
public abstract class BaseAuthorisedTestIntegration extends BaseTestIntegration {

    protected static final String BASEURL = "http://localhost";
    private static final long WAIT_TIME_MS = 1000;

    private WiremockFixtures wiremockFixtures;

    @LocalServerPort
    private int serverPort;

    protected RequestSpecification getRequestSpecification()
            throws JOSEException, JsonProcessingException, InterruptedException {
        return getRequestSpecification(S2S_XUI, ACTOR_ID1);
    }

    protected RequestSpecification getRequestSpecification(String serviceName, String actorId)
            throws JOSEException, JsonProcessingException, InterruptedException {
        resetWiremockServer(serviceName, actorId);
        return SerenityRest.given()
                .relaxedHTTPSValidation()
                .baseUri(BASEURL)
                .port(serverPort)
                .headers(MockUtils.getHttpHeaders(serviceName));
    }

    protected void resetWiremockServer(String serviceName, String actorId)
            throws JsonProcessingException, InterruptedException {
        // Clear the stubs and requests
        WIRE_MOCK_SERVER.resetAll();
        // Recreate the stubs
        wiremockFixtures = new WiremockFixtures();
        wiremockFixtures.stubIdamConfig();
        wiremockFixtures.stubAuthorisationDetails(serviceName);
        wiremockFixtures.stubAuthorisationUserInfo(actorId);
        // Allow some time for Wiremock to reset
        Thread.sleep(WAIT_TIME_MS);
    }
}
