package uk.gov.hmcts.reform.orgrolemapping;

import feign.Feign;
import feign.jackson.JacksonEncoder;
import lombok.NoArgsConstructor;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Tag;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.AUTHORIZATION;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.BEARER;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SERVICE_AUTHORIZATION;

@RunWith(SpringIntegrationSerenityRunner.class)
@NoArgsConstructor
@WithTags({@WithTag("testType:Smoke")})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SmokeTest {
    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    @Value("${launchdarkly.sdk.user}")
    private String userName;

    @Value("${launchdarkly.sdk.key}")
    private String sdkKey;

    UserTokenProviderConfig config;
    String accessToken;
    String serviceAuth;

    RestTemplate restTemplate = new RestTemplate();

    private static final Logger log = LoggerFactory.getLogger(SmokeTest.class);

    @Rule
    public FeatureFlagToggleEvaluator featureFlagToggleEvaluator = new FeatureFlagToggleEvaluator(this);

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


    @Tag("smoke")
    @Test
    @FeatureFlagToggle("orm-base-flag")
    public void should_receive_response_for_welcomeAPI() {

        String targetInstance = config.getOrgRoleMappingUrl() + "/welcome";
        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
                .given()
                .relaxedHTTPSValidation()
                .when()
                .get(targetInstance)
                .andReturn();
        response.then().assertThat().statusCode(HttpStatus.OK.value());
    }

    @Tag("smoke")
    @Test
    @FeatureFlagToggle("orm-base-flag")
    public void should_receive_response_for_create_org_mapping() {

        String targetInstance = config.getOrgRoleMappingUrl() + "/am/role-mapping/staff/users";
        RestAssured.useRelaxedHTTPSValidation();

        String requestBody = "{\"users\" : [\"123e4567-e89b-42d3-a456-55664200020a\"] }";

        Response response = SerenityRest
                .given()
                .relaxedHTTPSValidation()
                .header("Content-Type", "application/json")
                .header(SERVICE_AUTHORIZATION, BEARER + serviceAuth)
                .header(AUTHORIZATION, BEARER + accessToken)
                .body(requestBody)
                .when()
                .post(targetInstance)
                .andReturn();
        response.then().assertThat().statusCode(HttpStatus.OK.value());
    }

    public String getEnvironment() {
        return environment;
    }

    public String getUserName() {
        return userName;
    }

    public String getSdkKey() {
        return sdkKey;
    }

    private ServiceAuthorisationApi generateServiceAuthorisationApi(final String s2sUrl) {
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .contract(new SpringMvcContract())
                .target(ServiceAuthorisationApi.class, s2sUrl);
    }

    private ServiceAuthTokenGenerator authTokenGenerator(
            final String secret,
            final String microService,
            final ServiceAuthorisationApi serviceAuthorisationApi) {
        return new ServiceAuthTokenGenerator(secret, microService, serviceAuthorisationApi);
    }

    private String searchUserByUserId(UserTokenProviderConfig config) {
        TokenRequest request = config.prepareTokenRequest();
        new ResponseEntity<>(HttpStatus.OK);
        ResponseEntity<TokenResponse> response;
        HttpHeaders headers = new HttpHeaders();
        try {
            String url = String.format(
                    "%s/o/token?client_id=%s&client_secret=%s&grant_type=%s&scope=%s&username=%s&password=%s",
                    config.getIdamURL(),
                    request.getClientId(),
                    config.getClientSecret(),
                    request.getGrantType(),
                    "openid+roles+profile+authorities",
                    request.getUsername(),
                    request.getPassword()
            );

            headers.setContentType(MediaType.parseMediaType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
            HttpEntity<?> entity = new HttpEntity<>(headers);
            response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    TokenResponse.class
            );

            if (HttpStatus.OK.equals(response.getStatusCode())) {
                log.info("Positive response");
                return response.getBody().accessToken;
            } else {
                log.error("There is some problem in fetching access token {}", response
                        .getStatusCode());
                throw new ResourceNotFoundException("Not Found");
            }
        } catch (HttpClientErrorException exception) {
            log.error("HttpClientErrorException {}", exception.getMessage());
            throw new BadRequestException("Unable to fetch access token");

        }
    }
}

