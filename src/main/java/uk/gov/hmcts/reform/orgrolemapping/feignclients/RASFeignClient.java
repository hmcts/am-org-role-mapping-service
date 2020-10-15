package uk.gov.hmcts.reform.orgrolemapping.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.RASFeignClientFallback;

@FeignClient(value = "roleassignmentclient", url = "${feign.client.config.roleassignmentclient.url}",
             configuration = FeignClientConfiguration.class,
             fallback = RASFeignClientFallback.class)

public interface RASFeignClient {

    @GetMapping(value = "/")
    public String getServiceStatus();

    @PostMapping (value = "/am/role-assignments", headers = "x-correlation-id")
    public ResponseEntity<Object> createRoleAssignment(@RequestBody AssignmentRequest assignmentRequest);

}
