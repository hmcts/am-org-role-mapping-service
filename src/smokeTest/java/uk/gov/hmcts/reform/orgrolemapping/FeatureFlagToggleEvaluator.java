package uk.gov.hmcts.reform.orgrolemapping;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

@Slf4j
public class FeatureFlagToggleEvaluator implements TestRule {

    public static final String USER = "user";
    public static final String SERVICENAME = "servicename";
    public static final String AM_ORG_ROLE_MAPPING_SERVICE = "am_org_role_mapping_service";

    private final SmokeTest smokeTest;

    public FeatureFlagToggleEvaluator(SmokeTest smokeTest) {
        this.smokeTest = smokeTest;
    }

    @SneakyThrows
    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                var isFlagEnabled = false;
                var message = "The test is ignored as the flag is false";

                FeatureFlagToggle featureFlagToggle = description.getAnnotation(FeatureFlagToggle.class);

                if (featureFlagToggle != null) {
                    if (StringUtils.isNotEmpty(featureFlagToggle.value())) {
                        isFlagEnabled = Boolean.valueOf(featureFlagToggle.value());
                    }
                    Assume.assumeTrue(message, isFlagEnabled);
                }
                base.evaluate();
            }
        };
    }
}
