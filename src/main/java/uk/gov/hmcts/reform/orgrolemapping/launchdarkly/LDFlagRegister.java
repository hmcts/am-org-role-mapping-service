package uk.gov.hmcts.reform.orgrolemapping.launchdarkly;


import com.launchdarkly.sdk.LDUser;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureConditionEvaluator.SERVICE_NAME;

@Component
public class LDFlagRegister implements CommandLineRunner {

    @Autowired
    FlagEventListener flagEventListener;

    @Autowired
    FeatureConditionEvaluator featureConditionEvaluator;

    @Autowired
    FlagRefreshService flagRefreshService;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    @Getter
    public static final ConcurrentHashMap<String,Boolean> droolFlagStates = new ConcurrentHashMap<>();

        @Override
        public void run(String... args) throws Exception {
            LDUser user = new LDUser.Builder(environment)
                    .firstName("orm")
                    .lastName("am")
                    .custom(SERVICE_NAME, "am_org_role_mapping_service")
                    .build();
        //This if config file flow
        for (FlagConfig flag : FlagConfigs.getFlagConfigs().getValues()) {

            flagEventListener.logWheneverOneFlagChangesForOneUser(flag.getName(), user);
            flagRefreshService.initFlagState(flag);

        }
    }
}

