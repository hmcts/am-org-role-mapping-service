package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import feign.RequestInterceptor;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${feign.client.config.jrdClient.v2Active}")
    private Boolean v2Active;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            if (!requestTemplate.url().contains("health")) {
                requestTemplate.header(Constants.SERVICE_AUTHORIZATION, "Bearer "
                        + securityUtils.getServiceAuthorizationHeader());
                requestTemplate.header(HttpHeaders.AUTHORIZATION, "Bearer "
                        + idamRepository.getManageUserToken(oidcAdminConfiguration.getUserId()));
                if (v2Active != null && v2Active && requestTemplate.url().contains("judicial")) {
                    requestTemplate.header(HttpHeaders.CONTENT_TYPE, "application/vnd.jrd.api+json;Version=2.0");
                } else {
                    requestTemplate.header(HttpHeaders.CONTENT_TYPE, "application/json");
                }
            }
        };
    }
}
