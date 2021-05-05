package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;
import uk.gov.hmcts.reform.orgrolemapping.util.ValidationUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@AllArgsConstructor
public class RefreshOrchestrator {

    private final RetrieveDataService retrieveDataService;

    private final RequestMappingService requestMappingService;

    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> refresh(String roleCategory,
                                          String jurisdiction,
                                          List<String> retryUserIds) {

        long startTime = System.currentTimeMillis();


        ValidationUtil.compareRoleCategory(roleCategory);
        ResponseEntity<Object> responseEntity = null;
        // if retryUserid is not empty then call existing crd getcaseworkerbyuserID without pagination
        //else retryUserid is empty then call new crd refresh api with pagination


        //Create userAccessProfiles based upon roleId and service codes
        Map<String, Set<?>> userAccessProfiles = retrieveDataService
                .retrieveProfilesByServiceName(roleCategory,jurisdiction);

        if (roleCategory.equals(RoleCategory.LEGAL_OPERATIONS.name())) {
            //call the requestMapping service to determine role name and create role assignment requests
            responseEntity    = requestMappingService.createAssignments(userAccessProfiles, UserType.CASEWORKER);
            
        } else if (roleCategory.equals(RoleCategory.JUDICIAL.name())) {
           responseEntity = requestMappingService.createAssignments(userAccessProfiles, UserType.JUDICIAL);
            
        }

        log.info(
                "Execution time of createBulkAssignmentsRequest() : {} ms",
                (Math.subtractExact(System.currentTimeMillis(), startTime))
        );



        return responseEntity;
    }

   
}
