package uk.gov.hmcts.reform.orgrolemapping.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.CRDFeignClientFallback;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientConfiguration;


@FeignClient(value = "crdclient", url = "${feign.client.config.crdclient.url}",
        configuration = FeignClientConfiguration.class,
        fallback = CRDFeignClientFallback.class)

public interface CRDFeignClient {

    @GetMapping(value = "/")
    public String getServiceStatus();


    //@PostMapping (value = "/refdata/case-worker/users/fetchUsersById")
    //  public ResponseEntity<Object> createRoleAssignment(@PathVariable("caseId") String caseId AssignmentRequest);

}