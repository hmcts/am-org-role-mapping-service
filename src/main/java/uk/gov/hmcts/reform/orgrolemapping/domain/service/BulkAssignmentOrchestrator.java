package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;


import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class BulkAssignmentOrchestrator {
    //0. Orchestrator would receive list of userIds from ASB/API.
    //1. Call parse request service to extract userId List and their validations.
    //2. Call retrieveDataService to fetch the single or multiple user profiles(from CRD) and
    // validate the data through parse request. This might require a stub
    //3. Call request mapping service to apply the mapping rules for each user
    //   a) prepare role assignment requests
    //   b)Invoke RoleAssignmentService and audit the response.

    private final ParseRequestService parseRequestService;

    private final RetrieveDataService retrieveDataService;

    private final RequestMappingService requestMappingService;


    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> createBulkAssignmentsRequest(UserRequest userRequest, UserType userType) {
        long startTime = System.currentTimeMillis();
        //Extract and Validate received users List
        parseRequestService.validateUserRequest(userRequest);
        log.info("Validated userIds {}", userRequest.getUserIds());
        //Create userAccessProfiles based upon roleId and service codes
        Map<String, Set<?>> userAccessProfiles = retrieveDataService
                .retrieveProfiles(userRequest, userType);

        //call the requestMapping service to determine role name and create role assignment requests
        ResponseEntity<Object> responseEntity = requestMappingService.createAssignments(userAccessProfiles, userType);


        log.info(
                "Execution time of createBulkAssignmentsRequest() : {} ms",
                (Math.subtractExact(System.currentTimeMillis(), startTime))
        );
        return responseEntity;
    }

}
