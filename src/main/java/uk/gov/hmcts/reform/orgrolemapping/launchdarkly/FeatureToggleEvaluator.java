package uk.gov.hmcts.reform.orgrolemapping.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;

@Component
public class FeatureToggleEvaluator {

    @Autowired
    private LDClient ldClient;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    @Value("${launchdarkly.sdk.user}")
    private String userName;

    public static final String USER = "user";
    public static final String SERVICE_NAME = "servicename";

    public boolean isFlagEnabled(String serviceName, String flagName) {
        if (!ldClient.isFlagKnown(flagName)) {
            throw new ResourceNotFoundException(String.format(
                    "The flag %s is not configured in Launch Darkly", flagName));
        }

        LDUser user = new LDUser.Builder(environment)
                .firstName(userName)
                .lastName(USER)
                .custom(SERVICE_NAME, serviceName)
                .build();

        return ldClient.boolVariation(flagName, user, false);
    }

    public void validateLdFlag(String serviceName, String flagName) {
        if (!isFlagEnabled(serviceName, flagName)) {
            throw new ResourceNotFoundException(String.format(
                    "The flag %s is not enabled in Launch Darkly", flagName));
        }
    }

}
