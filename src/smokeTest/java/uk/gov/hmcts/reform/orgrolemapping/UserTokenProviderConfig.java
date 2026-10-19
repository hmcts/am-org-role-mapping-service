package uk.gov.hmcts.reform.orgrolemapping;

import lombok.Getter;
import uk.gov.hmcts.befta.util.EnvironmentVariableUtils;

@Getter
public class UserTokenProviderConfig {

    private final String orgRoleMappingUrl;

    public UserTokenProviderConfig() {
        orgRoleMappingUrl = EnvironmentVariableUtils.getRequiredVariable("TEST_URL");
    }
}
