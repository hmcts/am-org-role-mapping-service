package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;

import java.io.IOException;

@Slf4j
@Service
public class BulkAssignmentOrchestrator {
    //0. Orchestrator would receive list of userIds from ASB/API.
    //1. Call parse request service to extract userId List and their validations.
    //2. Call retrieveDataService to fetch the single or multiple user profiles(from CRD) and
    // validate the data through parse request. This might require a stub
    //3. Call request mapping service to apply the mapping rules for each user
    //   a) prepare role assignment requests
    //   b)Invoke RoleAssignmentService and audit the response.
    @Autowired
    private ParseRequestService parseRequestService;
    @Autowired
    private RetrieveDataService retrieveDataService;
    @Autowired
    private RequestMappingService requestMappingService;
    @Autowired
    private RoleAssignmentService roleAssignmentService;



    public ResponseEntity<Object> createBulkAssignmentsRequest(UserRequest userRequest) throws IOException {
        //1.
        //parseRequestService.validateUserRequest(userRequest);
        //2.
        //List<UserAccessProfiles> userAccessProfiles =  retrieveDataService.retrieveCaseWorkerProfiles(userRequest);
        //3.
        //requestMappingService.createCaseWorkerAssignments(userAccessProfiles);

        AssignmentRequest assignmentRequest = AssignmentRequestBuilder
                .buildAssignmentRequest(false);
        ResponseEntity<Object> response = null;
        response = roleAssignmentService.createRoleAssignment(assignmentRequest);
        return response;
    }

}
