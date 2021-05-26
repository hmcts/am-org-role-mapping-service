package uk.gov.hmcts.reform.orgrolemapping.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureConditionEvaluator;

@Configuration
public class LaunchDarklyConfiguration implements WebMvcConfigurer {

    @Autowired
    private FeatureConditionEvaluator featureConditionEvaluator;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(featureConditionEvaluator).addPathPatterns("/am/role-mapping/refresh");
    }
}

