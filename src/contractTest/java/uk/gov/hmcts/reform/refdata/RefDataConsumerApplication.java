package uk.gov.hmcts.reform.refdata;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.orgrolemapping.oidc.IdamRepository;
import uk.gov.hmcts.reform.orgrolemapping.oidc.OIdcAdminConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

@SpringBootApplication
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.orgrolemapping.feignclients"})
public class RefDataConsumerApplication {

    @Bean
    public IdamClient idamClient() {
        return Mockito.mock(IdamClient.class);
    }

    @Bean
    public SecurityUtils securityUtils() {
        return Mockito.mock(SecurityUtils.class);
    }

    @Bean
    public IdamRepository idamRepository() {
        return Mockito.mock(IdamRepository.class);
    }

    @Bean
    public OIdcAdminConfiguration oidcAdminConfiguration() {
        return Mockito.mock(OIdcAdminConfiguration.class);
    }
}
