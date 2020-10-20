package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentResource;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.RASFeignClient;

import java.util.LinkedHashMap;

@Service
@Slf4j
public class RoleAssignmentService {
    private final RASFeignClient rasFeignClient;

    public RoleAssignmentService(RASFeignClient rasFeignClient) {
        this.rasFeignClient = rasFeignClient;
    }

    public ResponseEntity<Object> createRoleAssignment (AssignmentRequest assignmentRequest) {
        return rasFeignClient.createRoleAssignment(assignmentRequest);
    }
}
