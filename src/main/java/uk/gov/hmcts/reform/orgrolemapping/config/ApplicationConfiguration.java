package uk.gov.hmcts.reform.orgrolemapping.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableAsync
public class ApplicationConfiguration {

    private final String s2sSecret;
    private final String s2sMicroService;
    private final String s2sUrl;

    public ApplicationConfiguration(@Value("${idam.s2s-auth.totp_secret}") String s2sSecret,
                                    @Value("${idam.s2s-auth.microservice}") String s2sMicroService,
                                    @Value("${idam.s2s-auth.url}") String s2sUrl) {
        this.s2sSecret = s2sSecret;
        this.s2sMicroService = s2sMicroService;
        this.s2sUrl = s2sUrl;
    }

    public String getS2sSecret() {
        return s2sSecret;
    }

    public String getS2sMicroService() {
        return s2sMicroService;
    }

    public String getS2sUrl() {
        return s2sUrl;
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        return restTemplate;
    }

    private CloseableHttpClient getHttpClient() {
        var timeout = 10000;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(timeout))
                .setResponseTimeout(Timeout.ofMilliseconds(timeout))
                .build();

        return HttpClientBuilder
            .create()
            .useSystemProperties()
            .setDefaultRequestConfig(config)
            .build();
    }
}
