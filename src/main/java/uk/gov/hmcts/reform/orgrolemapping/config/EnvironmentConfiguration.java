package uk.gov.hmcts.reform.orgrolemapping.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class EnvironmentConfiguration {

    @Value("${launchdarkly.sdk.environment}")
    String launchDarklyEnvironment;

    @Value("${orm.environment:null}")
    String ormEnvironment;

    public String getEnvironment() {
        if (StringUtils.isNotEmpty(ormEnvironment)) {
            return ormEnvironment;
        } else {
            return launchDarklyEnvironment;
        }
    }

}
