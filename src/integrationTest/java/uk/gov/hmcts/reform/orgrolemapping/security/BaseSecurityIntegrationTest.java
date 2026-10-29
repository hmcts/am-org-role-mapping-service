package uk.gov.hmcts.reform.orgrolemapping.security;

import io.restassured.specification.RequestSpecification;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseAuthorisedTestIntegration;

import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.TestAuthenticationUtils.getJwtHeaders;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.WireMockStubs.SERVICE_NAME;

public class BaseSecurityIntegrationTest extends BaseAuthorisedTestIntegration {

    protected static final String VALID_ISSUER_1 = "http://localhost:5062/o";
    protected static final String VALID_ISSUER_2 = "https://secondary-idam.platform.hmcts.net";
    protected static final String ROGUE_ISSUER = "https://rogue-issuer.com";

    protected RequestSpecification jwtRequest(
            String issuer,
            boolean expired)
            throws Exception {

        return getRequestSpecification(
            SERVICE_NAME,
                ACTOR_ID1,
                getJwtHeaders(issuer, expired));
    }

    protected RequestSpecification unexpiredJwt(
            String issuer)
            throws Exception {

        return jwtRequest(issuer, false);
    }

    protected RequestSpecification expiredJwt(
            String issuer)
            throws Exception {

        return jwtRequest(issuer, true);
    }
}