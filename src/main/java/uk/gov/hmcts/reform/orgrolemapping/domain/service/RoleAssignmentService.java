package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.RASFeignClient;

@Service
public class RoleAssignmentService {
    //This class is reserved to extract the RAS response and play with the resource object.
    private final RASFeignClient rasFeignClient;

    public RoleAssignmentService(RASFeignClient rasFeignClient) {
        this.rasFeignClient = rasFeignClient;
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 3))
    public ResponseEntity<Object> createRoleAssignment(AssignmentRequest assignmentRequest) {
        return rasFeignClient.createRoleAssignment(assignmentRequest, getCorrelationId());
    }

    public String getServiceStatus() {
        return rasFeignClient.getServiceStatus();
    }

    private String getCorrelationId() {
        try {
            var httpServletRequest = ((ServletRequestAttributes) RequestContextHolder
                    .currentRequestAttributes())
                    .getRequest();
            return httpServletRequest.getHeader(Constants.CORRELATION_ID_HEADER_NAME);
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
