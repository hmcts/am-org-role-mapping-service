package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.util.ValidationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@AllArgsConstructor
public class RefreshOrchestrator {

    private final RetrieveDataService retrieveDataService;

    private final RequestMappingService requestMappingService;
    private final ParseRequestService parseRequestService;
    private final CRDService crdService;


    public ResponseEntity<Object> refresh(String roleCategory,
                                          String jurisdiction,
                                          List<String> retryUserIds) {

        long startTime = System.currentTimeMillis();


        ResponseEntity<Object> responseEntity = null;
        Map<String, Set<UserAccessProfile>> userAccessProfiles;
        List<String> userIds = new ArrayList<>();

        //1. Async implementation---done
        //2.Insert job status  in db
        //3. retry logic--Done
        //4. custom object
        //5. pagination logic


        if (CollectionUtils.isNotEmpty(retryUserIds)) {
            UserRequest userRequest = UserRequest.builder().userIds(retryUserIds).build();
            //Extract and Validate received users List
            parseRequestService.validateUserRequest(userRequest);
            log.info("Validated userIds {}", userRequest.getUserIds());
            //Create userAccessProfiles based upon roleId and service codes
            userAccessProfiles = retrieveDataService
                    .retrieveCaseWorkerProfiles(userRequest);


        } else {
            ValidationUtil.compareRoleCategory(roleCategory);

            ResponseEntity<List<UserProfilesResponse>> response = crdService
                    .fetchCaseworkerDetailsByServiceName(jurisdiction, 2, 1,
                            "ASC", "roleName");


            // 2 step to findout the total number of records
            String total_records = response.getHeaders().getFirst("total_records");
            int pageNumber = (Integer.parseInt(total_records) / 2);
            Map<String, String> responseCode = new HashMap<>();


            //call to CRD
            for (int page = 1; page <= pageNumber; page++) {
                ResponseEntity<List<UserProfilesResponse>> userProfilesResponse = null;
                userProfilesResponse = crdService
                        .fetchCaseworkerDetailsByServiceName(jurisdiction, 2, page,
                                "ASC", "roleName");
                userAccessProfiles = retrieveDataService
                        .getUserAccessProfile(userProfilesResponse);



                responseEntity = requestMappingService.createCaseWorkerAssignments(userAccessProfiles);

                userProfilesResponse.getBody().forEach(userProfile-> userProfile.getUserProfiles()
                        .forEach(o->userIds.add(o.getId())));

                userIds.forEach(userId->responseCode.put(userId,"200k"));



            }

            //Create userAccessProfiles based upon roleId and service codes


        }


        log.info(
                "Execution time of createBulkAssignmentsRequest() : {} ms",
                (Math.subtractExact(System.currentTimeMillis(), startTime))
        );

        return responseEntity;
    }


}
