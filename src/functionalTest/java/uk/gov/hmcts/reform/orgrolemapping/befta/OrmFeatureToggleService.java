package uk.gov.hmcts.reform.orgrolemapping.befta;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import uk.gov.hmcts.befta.TestAutomationConfig;
import uk.gov.hmcts.befta.featuretoggle.FeatureToggleService;
import uk.gov.hmcts.befta.util.EnvironmentVariableUtils;

public class OrmFeatureToggleService implements FeatureToggleService<String, Boolean> {

    @Override
    public Boolean getToggleStatusFor(String flagId) {
        RestAssured.useRelaxedHTTPSValidation();

        RestAssured.baseURI = TestAutomationConfig.INSTANCE.getTestUrl();

        var path = EnvironmentVariableUtils.getRequiredVariable("TEST_URL") + "/"
            + EnvironmentVariableUtils.getRequiredVariable("EXTERNAL_FLAG_QUERY_PATH") + flagId;
        Response response = RestAssured.get(path);

        if (response.getStatusCode() == HttpStatus.SC_OK) {
            return response.getBody().as(Boolean.class);
        }
        return false;
    }
}

