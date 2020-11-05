package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.kie.api.runtime.StatelessKieSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Request;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.convertObjectIntoJsonNode;

@Service
@Slf4j
@AllArgsConstructor
public class RequestMappingService {
    /*
    //1. This will receive the single/multiple userAccessProfile from Orchestrator.
    //2. For Each UserAccessProfile:
        //a. Check if the delete flag set to true, if yes then prepare AssignmentRequest with Empty
        // requestedRole List and skip drools.
        //b. Else Check if there is multiple roles and service codes in User object --> Shifted to retrieveDataService
            //  If yes prepare more user instances(same userId) but with unique combinations of roleId
            //  and serviceCodeId
            //  Else simply prepare single user instance
        //c. Prepare the pre-filled requestedRole(leaving its roleName and JID) object for each userAccessProfile
            // instance.
        //d. Call validation model service to add both userAccessProfile and corresponding requestedRole objects
            // in Drools.
            //a. Invoke Drool execution.
            //b. Execute each service specific rules one by one and set RoleName/JID in requestedRole object.
        //e. Check if requestedRole.roleName is not null then prepare AssignmentRequest with requestedRole object
            //a. Else ignore the requestedRole Object and user object with some logging and 422.
        //g. For valid AssignmentRequest, invoke createRequest API of RoleAssignmentService through RAS Feign client.
        //h. Log returned response and send the responseEntity to Orchestrator.
    */

    private RoleAssignmentService roleAssignmentService;
    private StatelessKieSession kieSession;




    public ResponseEntity<Object> createCaseWorkerAssignments(Map<String, Set<UserAccessProfile>> usersAccessProfiles) {

        AtomicBoolean deleteFlag = new AtomicBoolean(false);
        //prepare an empty list of responses
        List<Object> finalResponse = new ArrayList<>();


        usersAccessProfiles.entrySet().stream().forEach(entry -> {
            AssignmentRequest assignmentRequest = createAssignmentRequest();
            // set the request reference as per user IDAM Id
            assignmentRequest.getRequest().setReference(entry.getKey());
            // the delete flag would be set for an user profile and it would be same for all its userAccessProfile
            deleteFlag.set(entry.getValue().stream().findFirst().get().isDeleteFlag());
            // Create a role assignment record if delete flag = false
            if (!deleteFlag.get()) {
                //add all identified requestedRoles to the request
                assignmentRequest.setRequestedRoles(mapUserAccessProfiles(entry.getValue()));
            }
            // to validate prepared assignment request
            ResponseEntity<Object> response = sendAssignmentRequestToRAS(deleteFlag.get(), assignmentRequest);
            // Prepare final response for all entry

            finalResponse.add(response.getBody());
        });


        return ResponseEntity.status(HttpStatus.OK).body(finalResponse);
    }


    private List<RoleAssignment> mapUserAccessProfiles(Set<UserAccessProfile> userAccessProfiles) {
        //prepare an empty list of role assignments
        List<RoleAssignment> requestedRoles = new ArrayList<>();

        userAccessProfiles.stream().forEach(userAccessProfile -> {
            //prepare an envelop requestedRole corresponding to an userAccessProfile
            RoleAssignment requestedRole = AssignmentRequestBuilder.buildRequestedRoleForStaff();
            //Copy necessary fields from the userAccessProfile
            requestedRole.setActorId(userAccessProfile.getId());
            requestedRole.getAttributes().put("primaryLocation",
                    convertObjectIntoJsonNode(userAccessProfile.getPrimaryLocationId()));

            //call the drool rules for determining the role name
            droolValidation(userAccessProfile, requestedRole);
            // add requestedRole to the list if role name is found as per mapping rules.
            if (StringUtils.isNotEmpty(requestedRole.getRoleName())) {
                requestedRoles.add(requestedRole);
            } else {
                // logging for fail validation roleID and service Code
                log.error("User Access profiles {} has not been validated by drool for the service code {} : ",
                        userAccessProfile.getId(), userAccessProfile.getServiceCode());
            }
        });
        return requestedRoles;
    }

    private AssignmentRequest createAssignmentRequest() {
        AssignmentRequest assignmentRequest = AssignmentRequest.builder().build();
        //prepare an envelop request with default values
        Request request = AssignmentRequestBuilder.buildRequest(true);
        assignmentRequest.setRequest(request);
        assignmentRequest.setRequestedRoles(Collections.emptyList());
        return assignmentRequest;

    }


    private ResponseEntity<Object> sendAssignmentRequestToRAS(boolean deleteFlag,
                                                              AssignmentRequest assignmentRequest) {
        if (Objects.equals(deleteFlag, false) && assignmentRequest.getRequestedRoles().size() == 0) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("Users AccessProfile could not be mapped");
        } else {
            //feign client call
            return roleAssignmentService.createRoleAssignment(assignmentRequest);
        }
    }

    private void droolValidation(UserAccessProfile userAccessProfile, RoleAssignment requestRole) {
        Set<Object> facts = new HashSet<>();
        facts.add(userAccessProfile);
        facts.add(requestRole);
        // Run the rules
        kieSession.execute(facts);
    }


}
