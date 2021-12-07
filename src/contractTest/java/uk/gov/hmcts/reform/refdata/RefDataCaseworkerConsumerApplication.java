package uk.gov.hmcts.reform.refdata;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
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

    @MockBean
    IdamClient idamClient;

    @MockBean
    SecurityUtils securityUtils;

    @MockBean
    IdamRepository idamRepository;

    @MockBean
    OIdcAdminConfiguration oIdcAdminConfiguration;
}
