package uk.gov.hmcts.reform.refdata;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.oidc.IdamRepository;
import uk.gov.hmcts.reform.orgrolemapping.oidc.OIdcAdminConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

@SpringBootApplication
@EnableFeignClients(clients = {
    CRDFeignClient.class
})
public class RefDataCaseworkerConsumerApplication {

    @MockitoBean
    IdamClient idamClient;

    @MockitoBean
    SecurityUtils securityUtils;

    @MockitoBean
    IdamRepository idamRepository;

    @MockitoBean
    OIdcAdminConfiguration oidcAdminConfiguration;

    @MockitoBean
    IdamApi idamApi;

    @Bean
    public IdamApi idamApiMock() {
        return Mockito.mock(IdamApi.class);
    }

    @Bean
    public SecurityUtils securityUtilsMock() {
        return Mockito.mock(SecurityUtils.class);
    }

    @Bean
    public IdamRepository idamRepositoryMock() {
        return Mockito.mock(IdamRepository.class);
    }

    @Bean
    public OIdcAdminConfiguration oidcAdminConfigurationMock() {
        return Mockito.mock(OIdcAdminConfiguration.class);
    }

    @Bean
    public IdamClient idamClientMock() {
        return Mockito.mock(IdamClient.class);
    }
}
