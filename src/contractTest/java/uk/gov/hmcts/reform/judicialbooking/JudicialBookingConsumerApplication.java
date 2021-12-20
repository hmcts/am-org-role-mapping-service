package uk.gov.hmcts.reform.judicialbooking;


import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
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

    @MockBean
    IdamClient idamClient;

    @MockBean
    SecurityUtils securityUtils;

    @MockBean
    IdamRepository idamRepository;

    @MockBean
    OIdcAdminConfiguration oidcAdminConfiguration;
}