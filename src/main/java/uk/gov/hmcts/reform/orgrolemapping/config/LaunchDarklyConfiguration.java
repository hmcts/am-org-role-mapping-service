package uk.gov.hmcts.reform.orgrolemapping.config;

import com.launchdarkly.sdk.server.LDClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class LaunchDarklyConfiguration {

    @Bean
    public LDClient ldClient(@Value("${launchdarkly.sdk.key}") String sdkKey) {
        log.error("sdkKey is :" + sdkKey);
        log.error("env variable " + System.getenv("LD_SDK_KEY"));
        return new LDClient(sdkKey);
    }

}
