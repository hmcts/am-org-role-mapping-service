package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;
import uk.gov.hmcts.reform.orgrolemapping.util.ValidationUtil;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class RefreshOrchestrator {

    private final RetrieveDataService retrieveDataService;

    private final RoleAssignmentService roleAssignmentService;

    private final ValidationModelService validationModelService;


    public ResponseEntity<Object> refresh(String roleCategory,
                                          String jurisdiction,
                                          List<String> retryUserIds) {


        ValidationUtil.compareRoleCategory(roleCategory);

        UserProfilesResponse userProfilesResponse;

        if (roleCategory.equals(RoleCategory.LEGAL_OPERATIONS.name())) {
            //call CRD and get user details
            userProfilesResponse = retrieveDataService.retrieve(
                    Arrays.asList("fpla", "iac"), 1, 1, "ASC", "firstName");
        } else if (roleCategory.equals(RoleCategory.JUDICIAL.name())) {
            //call JRD and get user details
        }

        //Map userprofiles from userProfilesResponse to userAccessProfiles and fire request

        List<AssignmentRequest> assignmentRequests =
                validationModelService.runRulesOnAccessProfiles(UserAccessProfileBuilder.buildUserAccessProfileMap
                        (false, false));

        assignmentRequests.forEach(this::tryPost);

        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

    private void tryPost(AssignmentRequest assignmentRequest) {
        ResponseEntity<Object> responseEntity;
        try {
            responseEntity = roleAssignmentService.createRoleAssignment(assignmentRequest);
            if(!responseEntity.getStatusCode().is2xxSuccessful()) {
                log.error("Assignment Request received status code: {}",
                        responseEntity.getStatusCodeValue());
                log.info("Time: {}", System.currentTimeMillis());
                log.info("Assignment Request FAILED with following Id: {}",
                        assignmentRequest.getRequest().getReference());


            }
        } catch (FeignException.FeignClientException feignClientException) {
            log.error("Handling FeignClientException UnprocessableEntity: " + feignClientException.getMessage());
        }
    }
}
