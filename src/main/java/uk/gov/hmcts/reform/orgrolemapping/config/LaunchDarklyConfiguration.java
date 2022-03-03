package uk.gov.hmcts.reform.orgrolemapping.config;

import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureConditionEvaluator;

@Configuration
public class LaunchDarklyConfiguration implements WebMvcConfigurer {

    @Value("${launchdarkly.runOnStartup:false}")
    private boolean runOnStartup;

    public LDClientInterface ldClient(@Value("${launchdarkly.sdk.key}") String sdkKey) {
        return runOnStartup ? new LDClient(sdkKey) : new LDDummyClient();
    }

    @Autowired
    private FeatureConditionEvaluator featureConditionEvaluator;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(featureConditionEvaluator).addPathPatterns("/welcome");
        registry.addInterceptor(featureConditionEvaluator).addPathPatterns("/am/role-mapping/refresh");
        registry.addInterceptor(featureConditionEvaluator).addPathPatterns("/am/role-mapping/createFeatureFlag");
        registry.addInterceptor(featureConditionEvaluator).addPathPatterns("/am/role-mapping/fetchFlagStatus");
        registry.addInterceptor(featureConditionEvaluator).addPathPatterns("/am/role-mapping/judicial/refresh");
    }
}

