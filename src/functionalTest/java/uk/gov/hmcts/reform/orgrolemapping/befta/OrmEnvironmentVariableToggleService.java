package uk.gov.hmcts.reform.orgrolemapping.befta;

import uk.gov.hmcts.befta.featuretoggle.FeatureToggleService;
import uk.gov.hmcts.befta.util.EnvironmentVariableUtils;

public class OrmEnvironmentVariableToggleService implements FeatureToggleService<String, Boolean> {

    @Override
    public Boolean getToggleStatusFor(String flagId) {
        String environmentVariableString = EnvironmentVariableUtils.getOptionalVariable(flagId);
        if (environmentVariableString != null) {
            return Boolean.valueOf(environmentVariableString);
        } else {
            return false;
        }
    }
}
