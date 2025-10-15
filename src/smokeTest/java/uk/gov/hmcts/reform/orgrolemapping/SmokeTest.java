package uk.gov.hmcts.reform.orgrolemapping;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.NoArgsConstructor;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.rest.SerenityRest;

import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

@ExtendWith(SerenityJUnit5Extension.class)
@NoArgsConstructor
@WithTags({@WithTag("testType:Smoke")})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SmokeTest {
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

    @BeforeEach
    void setUp() {
        config = new UserTokenProviderConfig();
    }

    @Tag("smoke")
    @Test
    void should_receive_response_for_welcomeAPI() {
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
