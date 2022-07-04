package uk.gov.hmcts.reform.orgrolemapping.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.orgrolemapping.data.FlagConfig;
import uk.gov.hmcts.reform.orgrolemapping.data.FlagConfigRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.FeatureFlagEnum;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Slf4j
public class DBFlagConfigurtion implements CommandLineRunner {

    @Autowired
    FlagConfigRepository flagConfigRepository;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    @Value("${dbFeature.flags.enable}")
    private String dbFeature2Enable;

    @Value("${dbFeature.flags.disable}")
    private String dbFeature2Disable;

    private static ConcurrentHashMap<String, Boolean> droolFlagStates = new ConcurrentHashMap<>();


    public static ConcurrentHashMap<String, Boolean> getDroolFlagStates() {
        return droolFlagStates;
    }

    @Override
    public void run(String... args) {
        if (!dbFeature2Enable.isEmpty()) {
            updateFeatureFlag(dbFeature2Enable, Boolean.TRUE);
        }
        if (!dbFeature2Disable.isEmpty()) {
            updateFeatureFlag(dbFeature2Disable, Boolean.FALSE);
        }
        for (FeatureFlagEnum featureFlagEnum : FeatureFlagEnum.values()) {
            var status = flagConfigRepository
                    .findByFlagNameAndEnv(featureFlagEnum.getValue(), environment).getStatus();
            droolFlagStates.put(featureFlagEnum.getValue(), status);
            log.info("The DB feature flag {} is set to: {}",featureFlagEnum.getValue(),status);
        }

    }

    private void updateFeatureFlag(String featureFlag, Boolean status) {
        FlagConfig flagConfig = flagConfigRepository.findByFlagNameAndEnv(featureFlag, environment);
        flagConfig.setStatus(status);
        flagConfigRepository.save(flagConfig);
    }
}
