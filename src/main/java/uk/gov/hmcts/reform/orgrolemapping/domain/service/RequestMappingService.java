package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RequestMappingService {
    //1. This will receive the single/multiple userAccessProfile from Orchestrator.
    //2. For Each UserAccessProfile:
    //a. Check if the delete flag set to true, if yes then prepare AssignmentRequest
    // with Empty requestedRole List and skip drools.
    //b. Else Check if there is multiple roles and service codes in User object --> Shifted to retrieveDataService
    //  If yes prepare more user instances(same userId) but with unique combinations of roleId and serviceCodeId
    //  Else simply prepare single user instance
    //c. Prepare the pre-filled requestedRole(leaving its roleName and JID) object for each userAccessProfile instance.
    //d. Call validation model service to add both userAccessProfile and corresponding requestedRole objects in Drools.
    //a. Invoke Drool execution.
    //b. Execute each service specific rules one by one and set RoleName/JID in requestedRole object.
    //e. Check if requestedRole.roleName is not null then prepare AssignmentRequest with requestedRole object
    //a. Else ignore the requestedRole Object and user object with some logging and 422.
    //g. For valid AssignmentRequest, invoke createRequest API of RoleAssignmentService through RAS Feign client.
    //h. Log returned response and send the responseEntity to Orchestrator.
    @Autowired
    private RoleAssignmentService roleAssignmentService;

    @Autowired
    private ValidationModelService validationModelService;


    public void createCaseWorkerAssignments() {
        //Receive List<UserAccessProfiles> userAccessProfiles
        checkDeleteRequest();
        createInitialRequestedRole();
        //call validation model service
        isOrgRoleMapped();
        createAssignmentRequest();
        ignoreUserAccessProfile();
        postAssignmentRequest();
        logReturnedResponse();

    }

    private void logReturnedResponse() {
    }

    private void postAssignmentRequest() {
    }

    private void ignoreUserAccessProfile() {
    }

    private void createAssignmentRequest() {
    }

    private void isOrgRoleMapped() {
    }

    private void createInitialRequestedRole() {
    }

    private void checkDeleteRequest() {
    }
}
