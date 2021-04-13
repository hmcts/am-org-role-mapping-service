package uk.gov.hmcts.reform.orgrolemapping.launchdarkly;


import com.launchdarkly.sdk.LDUser;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureConditionEvaluator.SERVICE_NAME;

@Component
public class LDEventListener implements CommandLineRunner {

    @Autowired
    FeatureFlagListener featureFlagListener;

    @Autowired
    FeatureConditionEvaluator featureConditionEvaluator;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    @Getter
    public static final Map<String,Boolean> droolFlagStates = new HashMap<>();

    @Override
    public void run(String... args) throws Exception {
        LDUser user = new LDUser.Builder(environment)
                .firstName("orm")
                .lastName("am")
                .custom(SERVICE_NAME, "am_org_role_mapping_service")
                .build();
        //This if config file flow
        for (FlagConfig flag : FlagConfigs.getFlagConfigs().getValues()) {
            featureFlagListener.logWheneverOneFlagChangesForOneUser(flag.getName(), user);
            //1) Check if role-refresh-enabled flag is true and proceed with DB operation.
            //add the logic to insert the flag with default status if it doesn't exist in the state table and insert refresh jobs
            //code
            //add the flag with default status in the static map.
            droolFlagStates.put(flag.getName(),flag.getDefaultValue());
        }

        /*for (FeatureFlagEnum flag : FeatureFlagEnum.values()) {
            droolFlagStates.put(flag.getValue(),featureConditionEvaluator.isFlagEnabled(flag.getValue()));
            featureFlagListener.logWheneverOneFlagChangesForOneUser(flag.getValue(), user);
        }*/
    }
}

