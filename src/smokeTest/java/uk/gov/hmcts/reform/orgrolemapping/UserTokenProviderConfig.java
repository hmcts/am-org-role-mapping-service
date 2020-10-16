package uk.gov.hmcts.reform.orgrolemapping;

import lombok.Getter;
import uk.gov.hmcts.befta.util.EnvironmentVariableUtils;

@Getter
public class UserTokenProviderConfig {

    private final String idamURL;
    private final String roleAssignmentUrl;
    private final String secret;
    private final String microService;
    private final String s2sUrl;

    private final String clientId;
    private final String clientSecret;
    private final String username;
    private final String password;
    private final String scope;
    private static final String MICRO_SERVICE_NAME = "am_role_assignment_service";
    private static final String USER_NAME = "befta.caseworker.2.solicitor.2@gmail.com";

    public UserTokenProviderConfig() {

        idamURL = EnvironmentVariableUtils.getRequiredVariable("IDAM_URL");
        roleAssignmentUrl = EnvironmentVariableUtils.getRequiredVariable("TEST_URL");
        secret = EnvironmentVariableUtils.getRequiredVariable("AM_ROLE_ASSIGNMENT_SERVICE_SECRET");
        microService = MICRO_SERVICE_NAME;
        s2sUrl = EnvironmentVariableUtils.getRequiredVariable("IDAM_S2S_URL");
        clientSecret = EnvironmentVariableUtils.getRequiredVariable("ROLE_ASSIGNMENT_IDAM_CLIENT_SECRET");
        clientId = EnvironmentVariableUtils.getRequiredVariable("IDAM_CLIENT_ID");
        username = USER_NAME;
        password = EnvironmentVariableUtils.getRequiredVariable("CCD_BEFTA_CASEWORKER_2_SOLICITOR_2_PWD");
        scope = EnvironmentVariableUtils.getRequiredVariable("OPENID_SCOPE_VARIABLES");
    }

    //need to add code
    public void prepareTokenRequest() {

    }
}
