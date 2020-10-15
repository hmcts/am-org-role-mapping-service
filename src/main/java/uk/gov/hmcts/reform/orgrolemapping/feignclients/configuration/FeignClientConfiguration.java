package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfiguration {
    @Bean
    public OkHttpClient client() {
        return new OkHttpClient();
    }

}
