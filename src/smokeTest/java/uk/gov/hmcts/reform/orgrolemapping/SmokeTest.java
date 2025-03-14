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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.orgrolemapping.config.EnvironmentConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.config.servicebus.CRDMessagingConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.config.servicebus.JRDMessagingConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.CRDTopicConsumerNew;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.CRDTopicPublisher;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.JRDTopicConsumerNew;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.JRDTopicPublisher;

@RunWith(SpringIntegrationSerenityRunner.class)
@NoArgsConstructor
@WithTags({@WithTag("testType:Smoke")})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SmokeTest {
    @Autowired
    private EnvironmentConfiguration environmentConfiguration;

    UserTokenProviderConfig config;

    @MockBean
    private CRDTopicConsumerNew crdTopicConsumerNew;

    @MockBean
    private JRDTopicConsumerNew jrdTopicConsumerNew;

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
        return environmentConfiguration.getEnvironment();
    }

}
