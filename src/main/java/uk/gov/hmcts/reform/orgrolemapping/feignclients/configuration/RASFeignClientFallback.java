package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.RASFeignClient;

@Component
public class RASFeignClientFallback implements RASFeignClient {

    public static final String ROLE_ASSIGNMENT_SERVICE_NOT_AVAILABLE = "The data store Service is not available";

    @Override
    public String getServiceStatus() {
        return ROLE_ASSIGNMENT_SERVICE_NOT_AVAILABLE;
    }

    @Override
    public ResponseEntity<Object> createRoleAssignment(@RequestBody AssignmentRequest assignmentRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).build()/*.body(AssignmentRequest.builder())*/;
    }
}
