package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.RASFeignClient;

@Service
public class RoleAssignmentService {
    //This class is reserved to extract the RAS response and play with the resource object.
    private final RASFeignClient rasFeignClient;

    public RoleAssignmentService(RASFeignClient rasFeignClient) {
        this.rasFeignClient = rasFeignClient;
    }

    public ResponseEntity<Object> createRoleAssignment(AssignmentRequest assignmentRequest) {
        return rasFeignClient.createRoleAssignment(assignmentRequest);
    }

    public String getServiceStatus(){
        return rasFeignClient.getServiceStatus();
    }
}
