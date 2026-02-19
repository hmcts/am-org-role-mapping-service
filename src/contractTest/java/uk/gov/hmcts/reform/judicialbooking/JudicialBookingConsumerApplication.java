package uk.gov.hmcts.reform.judicialbooking;


import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JBSFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.oidc.IdamRepository;
import uk.gov.hmcts.reform.orgrolemapping.oidc.OIdcAdminConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

@SpringBootApplication
@EnableFeignClients(clients = {
    JBSFeignClient.class
})
public class JudicialBookingConsumerApplication {

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
}
