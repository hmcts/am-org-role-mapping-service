package uk.gov.hmcts.reform.refdata;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.idam.client.IdamClient;
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
}
