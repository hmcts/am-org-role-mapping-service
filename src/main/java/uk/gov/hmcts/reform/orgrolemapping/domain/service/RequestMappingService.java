package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.kie.api.runtime.StatelessKieSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.UnprocessableEntityException;
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
public class RequestMappingService {
    //1. This will receive the single/multiple userAccessProfile from Orchestrator.
    //2. For Each UserAccessProfile:
    //a. Check if the delete flag set to true, if yes then prepare AssignmentRequest with Empty requestedRole List and skip drools.
    //b. Else Check if there is multiple roles and service codes in User object --> Shifted to retrieveDataService
    //  If yes prepare more user instances(same userId) but with unique combinations of roleId and serviceCodeId
    //  Else simply prepare single user instance
    //c. Prepare the pre-filled requestedRole(leaving its roleName and JID) object for each userAccessProfile instance.
    //d.  add both userAccessProfile and corresponding requestedRole objects in Drools.
    //a. Invoke Drool execution.
    //b. Execute each service specific rules one by one and set RoleName/JID in requestedRole object.
    //e. Check if requestedRole.roleName is not null then prepare AssignmentRequest with requestedRole object
    //a. Else ignore the requestedRole Object and user object with some logging and 422.
    //g. For valid AssignmentRequest, invoke createRequest API of RoleAssignmentService through RAS Feign client.
    //h. Log returned response and send the responseEntity to Orchestrator.

    private RoleAssignmentService roleAssignmentService;
    private StatelessKieSession kieSession;

    public RequestMappingService(RoleAssignmentService roleAssignmentService, StatelessKieSession kieSession) {
        this.roleAssignmentService = roleAssignmentService;
        this.kieSession = kieSession;
    }


    public ResponseEntity<Object> createCaseWorkerAssignments(Map<String, List<UserAccessProfile>> userAccessProfiles) {
        //Receive List<UserAccessProfiles> userAccessProfiles
        //Create List<UserAccessProfile> based on  the list based on user id
        return checkUserProfiles(userAccessProfiles);


    }


    private AssignmentRequest createAssignmentRequest(Boolean replaceExisting) {
        AssignmentRequest assignmentRequest = AssignmentRequest.builder().build();
        Request request = AssignmentRequestBuilder.buildRequest(true);
        assignmentRequest.setRequest(request);
        assignmentRequest.setRequestedRoles(Collections.emptyList());
        return assignmentRequest;

    }


    // We are process List<UserAccessProfile> having same UserId
    private ResponseEntity<Object> checkUserProfiles(Map<String, List<UserAccessProfile>> userAccessProfiles) {

        List<RoleAssignment> roleAssignments = new ArrayList<>();


        AtomicBoolean deleteFlag = new AtomicBoolean(false);
        List<Object> finalResponse = new ArrayList<>();


        userAccessProfiles.entrySet().stream().forEach(entry -> {
            AssignmentRequest assignmentRequest = createAssignmentRequest(true);
            assignmentRequest.getRequest().setReference(entry.getKey());
            deleteFlag.set(entry.getValue().get(0).isDeleteFlag());
            if (!deleteFlag.get()) {
                entry.getValue().stream().forEach(userAccessProfile -> {

                    RoleAssignment roleAssignment = AssignmentRequestBuilder.buildRoleAssignmentForStaff();
                    roleAssignment.setActorId(userAccessProfile.getId());
                    roleAssignment.getAttributes().put("primaryLocation"
                            , convertObjectIntoJsonNode(userAccessProfile.getPrimaryLocationId()));

                    droolValidation(userAccessProfile, roleAssignment);
                    if (StringUtils.isNotEmpty(roleAssignment.getRoleName())) {
                        roleAssignments.add(roleAssignment);
                    } else {
                        // logging for fail validation roleID and service Code
                        log.error("User Access profiles {} has not been validated by drool for the service code {} : "
                                , userAccessProfile.getId(), userAccessProfile.getServiceCode());
                    }
                });
                assignmentRequest.setRequestedRoles(roleAssignments);

            }
            // to validate prepared assignment request
            ResponseEntity<Object> response = validatePreparedAssignmentRequest(deleteFlag.get(), assignmentRequest);
            // Prepare final response for all entry
            finalResponse.add(response.getBody());
            //clear the list for second entry
            roleAssignments.clear();


        });


        return ResponseEntity.status(HttpStatus.OK).body(finalResponse);
    }

    private ResponseEntity<Object> validatePreparedAssignmentRequest(boolean deleteFlag, AssignmentRequest assignmentRequest) {


        if (Objects.equals(deleteFlag, false) && assignmentRequest.getRequestedRoles().size() == 0) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("UserAccess Profile could not be mapped");
            //throw new UnprocessableEntityException("UserAccessProfiles is not validated by drool");
        } else {
            //feign client call
            return roleAssignmentService.createRoleAssignment(assignmentRequest);

        }

    }

    private void droolValidation(UserAccessProfile userAccessProfile, RoleAssignment roleAssignment) {
        Set<Object> facts = new HashSet<>();
        facts.add(userAccessProfile);
        facts.add(roleAssignment);
        // Run the rules
        kieSession.execute(facts);

    }


}
