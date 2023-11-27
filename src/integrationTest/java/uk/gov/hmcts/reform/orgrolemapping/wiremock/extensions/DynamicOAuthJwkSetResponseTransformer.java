package uk.gov.hmcts.reform.orgrolemapping.wiremock.extensions;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.nimbusds.jose.JOSEException;
import lombok.extern.slf4j.Slf4j;

import static uk.gov.hmcts.reform.orgrolemapping.util.KeyGenerator.getRsaJwk;

/*
 * Replaces response body with the OAuth JWK Set, i.e. the public keys used to sign the mock OAAuth token
 */
@Slf4j
public class DynamicOAuthJwkSetResponseTransformer extends AbstractDynamicResponseTransformer {

    static final String DYNAMIC_OAUTH_JWK_SET_RESPONSE_TRANSFORMER = "dynamic-oauth-jwk-set-response-transformer";
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(
        DynamicOAuthJwkSetResponseTransformer.class);

    @Override
    protected String dynamicResponse(Request request, Response response, Parameters parameters) {
        try {
            return "{"
                + "\"keys\": [" + getRsaJwk().toPublicJWK().toJSONString() + "]"
                + "}";

        } catch (JOSEException ex) {
            log.error("Failure running RSA JWK Generator", ex);
        }

        return null;
    }

    @Override
    public String getName() {
        return DYNAMIC_OAUTH_JWK_SET_RESPONSE_TRANSFORMER;
    }

}
