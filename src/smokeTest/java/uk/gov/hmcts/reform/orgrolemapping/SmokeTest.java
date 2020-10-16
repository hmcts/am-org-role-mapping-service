package uk.gov.hmcts.reform.orgrolemapping;

import lombok.NoArgsConstructor;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;


@RunWith(SpringIntegrationSerenityRunner.class)
@NoArgsConstructor
@WithTags({@WithTag("testType:Smoke")})

public class SmokeTest extends BaseTest {

    public static final String ERROR_DESCRIPTION = "errorDescription";
    public static final String AUTHORIZATION = "Authorization";
    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String BEARER = "Bearer ";
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String RESOURCE_NOT_FOUND = "Resource not found";

    UserTokenProviderConfig config;
    String accessToken;
    String serviceAuth;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    @Value("${launchdarkly.sdk.user}")
    private String userName;

    @Value("${launchdarkly.sdk.key}")
    private String sdkKey;

    @Before
    public void setUp() {
        config = new UserTokenProviderConfig();
        accessToken = searchUserByUserId(config);
        serviceAuth = authTokenGenerator(
                config.getSecret(),
                config.getMicroService(),
                generateServiceAuthorisationApi(config.getS2sUrl())
        ).generate();
    }

    @Rule
    public FeatureFlagToggleEvaluator featureFlagToggleEvaluator = new FeatureFlagToggleEvaluator(this);


    public String getEnvironment() {
        return environment;
    }

    public String getUserName() {
        return userName;
    }

    public String getSdkKey() {
        return sdkKey;
    }
}

