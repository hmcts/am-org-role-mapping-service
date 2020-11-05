package uk.gov.hmcts.reform.orgrolemapping;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.orgrolemapping.oidc.IdamRepository;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

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

    @Autowired
    SecurityUtils securityUtils;

    @Autowired
    private IdamRepository idamRepository;

    @Rule
    public FeatureFlagToggleEvaluator featureFlagToggleEvaluator = new FeatureFlagToggleEvaluator(this);

    @Before
    public void setUp() {
        config = new UserTokenProviderConfig();
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
    @FeatureFlagToggle("orm-create-flag")
    public void should_receive_response_for_create_org_mapping() {

        String targetInstance = config.getOrgRoleMappingUrl() + "/am/role-mapping/staff/users";
        RestAssured.useRelaxedHTTPSValidation();

        String requestBody = "21334a2b-79ce-44eb-9168-2d49a744be9c";

        Response response = SerenityRest
                .given()
                .relaxedHTTPSValidation()
                .header("Content-Type", "application/json")
                .header(SERVICE_AUTHORIZATION, BEARER + securityUtils.getServiceAuthorizationHeader())
                .header(AUTHORIZATION, BEARER + idamRepository.getUserToken())
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
}

