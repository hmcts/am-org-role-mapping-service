package uk.gov.hmcts.reform.orgrolemapping.config;

import com.launchdarkly.sdk.server.LDClient;
//import org.apache.http.client.config.RequestConfig;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
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

    //@Bean
    //public RestTemplate restTemplate() {
    //    RestTemplate restTemplate = new RestTemplate();
    //    restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(getHttpClient()));
    //    return restTemplate;
    //}

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        var timeout = 10;
        return builder.requestFactory(HttpComponentsClientHttpRequestFactory.class)
                .setConnectTimeout(Duration.ofSeconds(timeout))
                .setReadTimeout(Duration.ofSeconds(timeout))
                .setBufferRequestBody(true)
                .build();
    }

    @Bean
    public LDClient ldClient(@Value("${launchdarkly.sdk.key}") String sdkKey) {
        return new LDClient(sdkKey);
    }


    //private CloseableHttpClient getHttpClient() {
    //    var timeout = 10000;
    //    RequestConfig config = RequestConfig.custom()
    //                                        .setConnectTimeout(timeout)
    //                                        .setConnectionRequestTimeout(timeout)
    //                                        .setSocketTimeout(timeout)
    //                                        .build();

    //    return HttpClientBuilder
    //        .create()
    //        .useSystemProperties()
    //        .setDefaultRequestConfig(config)
    //        .build();
    //}
}
