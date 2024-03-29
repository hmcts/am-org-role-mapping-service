package uk.gov.hmcts.reform.orgrolemapping.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientInterceptor;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.RASFeignClientFallback;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.CORRELATION_ID_HEADER_NAME;

@FeignClient(value = "roleassignmentclient", url = "${feign.client.config.roleAssignmentApp.url}",
             configuration = {FeignClientConfiguration.class, FeignClientInterceptor.class},
             fallback = RASFeignClientFallback.class)

public interface RASFeignClient {

    @GetMapping(value = "/")
    public String getServiceStatus();

    @PostMapping (value = "/am/role-assignments", headers = "x-correlation-id")
    public ResponseEntity<Object> createRoleAssignment(@RequestBody AssignmentRequest assignmentRequest,
                                               @RequestHeader(name = CORRELATION_ID_HEADER_NAME) String correlationId);

}
