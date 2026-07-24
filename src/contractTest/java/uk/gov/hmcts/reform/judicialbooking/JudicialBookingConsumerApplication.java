package uk.gov.hmcts.reform.judicialbooking;


import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JBSFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.oidc.IdamRepository;
import uk.gov.hmcts.reform.orgrolemapping.oidc.OIdcAdminConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

@SpringBootApplication
@EnableFeignClients(clients = {
    JBSFeignClient.class
})
public class JudicialBookingConsumerApplication {

    @Bean
    IdamClient idamClient() {
        return Mockito.mock(IdamClient.class);
    }

    @Bean
    SecurityUtils securityUtils() {
        return Mockito.mock(SecurityUtils.class);
    }

    @Bean
    IdamRepository idamRepository() {
        return Mockito.mock(IdamRepository.class);
    }

    @Bean
    OIdcAdminConfiguration oidcAdminConfiguration() {
        return Mockito.mock(OIdcAdminConfiguration.class);
    }
}