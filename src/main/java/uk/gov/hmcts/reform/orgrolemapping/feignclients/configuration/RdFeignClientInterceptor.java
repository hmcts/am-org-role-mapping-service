package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import feign.RequestInterceptor;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.oidc.IdamRepository;
import uk.gov.hmcts.reform.orgrolemapping.oidc.OIdcAdminConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

@Service
public class RdFeignClientInterceptor {

    @Autowired
    SecurityUtils securityUtils;
    @Autowired
    IdamRepository idamRepository;
    @Autowired
    OIdcAdminConfiguration oidcAdminConfiguration;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            if (!requestTemplate.url().contains("health")) {
                requestTemplate.header(Constants.SERVICE_AUTHORIZATION, "Bearer "
                        + securityUtils.getServiceAuthorizationHeader());
                requestTemplate.header(HttpHeaders.AUTHORIZATION, "Bearer "
                        + idamRepository.getManageUserToken(oidcAdminConfiguration.getUserId()));
                requestTemplate.header(HttpHeaders.CONTENT_TYPE, "application/json");
            }
        };
    }
}
