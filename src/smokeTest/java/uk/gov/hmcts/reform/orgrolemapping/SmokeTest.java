package uk.gov.hmcts.reform.orgrolemapping;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.NoArgsConstructor;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Tag;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.orgrolemapping.config.servicebus.CRDMessagingConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.config.servicebus.JRDMessagingConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.CRDTopicConsumer;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.CRDTopicPublisher;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.JRDTopicConsumer;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.JRDTopicPublisher;

@RunWith(SpringIntegrationSerenityRunner.class)
@NoArgsConstructor
@WithTags({@WithTag("testType:Smoke")})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SmokeTest {
    @Value("${orm.environment:${launchdarkly.sdk.environment}}")
    private String environment;

    @Value("${launchdarkly.sdk.user}")
    private String userName;

    @Value("${launchdarkly.sdk.key}")
    private String sdkKey;

    UserTokenProviderConfig config;

    @MockBean
    private CRDTopicConsumer crdTopicConsumer;

    @MockBean
    private JRDTopicConsumer jrdTopicConsumer;

    @MockBean
    private CRDTopicPublisher crdTopicPublisher;

    @MockBean
    private JRDTopicPublisher jrdTopicPublisher;


    @MockBean
    private CRDMessagingConfiguration crdMessagingConfiguration;

    @MockBean
    private JRDMessagingConfiguration jrdMessagingConfiguration;

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

        var targetInstance = config.getOrgRoleMappingUrl();
        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
                .given()
                .relaxedHTTPSValidation()
                .when()
                .get(targetInstance)
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
