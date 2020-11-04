package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.oidc.IdamRepository;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

@Service
public class FeignClientInterceptor {

    @Autowired
    SecurityUtils securityUtils;



    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            if (!requestTemplate.url().contains("health")) {
                requestTemplate.header(Constants.SERVICE_AUTHORIZATION, "Bearer "
                        + securityUtils.getServiceAuthorizationHeader());
                requestTemplate.header(HttpHeaders.AUTHORIZATION, "Bearer " + securityUtils.getUserToken());
                requestTemplate.header(HttpHeaders.CONTENT_TYPE, "application/json");
            }
        };
    }


}
