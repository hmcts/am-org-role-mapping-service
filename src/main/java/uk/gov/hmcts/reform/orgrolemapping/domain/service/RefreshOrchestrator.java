package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;
import uk.gov.hmcts.reform.orgrolemapping.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@AllArgsConstructor
public class RefreshOrchestrator {

    private final RetrieveDataService retrieveDataService;

    private final RequestMappingService requestMappingService;
    private final ParseRequestService parseRequestService;
    private final CRDService crdService;
    private final PersistenceService persistenceService;


    public ResponseEntity<Object> refresh(Long jobId, UserRequest userRequest) {


        long startTime = System.currentTimeMillis();

        Map<String, String> responseCodeWithUserId = new HashMap<>();
        ResponseEntity<Object> responseEntity = null;

        //fetch the entity based on jobId
        Optional<RefreshJobEntity> refreshJobEntity = persistenceService.fetchRefreshJobById(jobId);


        if (userRequest != null && CollectionUtils.isNotEmpty(userRequest.getUserIds())) {
            //Extract and Validate received users List
            parseRequestService.validateUserRequest(userRequest);
            log.info("Validated userIds {}", userRequest.getUserIds());

            //Create userAccessProfiles based upon userIds
            Map<String, Set<UserAccessProfile>> userAccessProfiles = retrieveDataService
                    .retrieveCaseWorkerProfiles(userRequest);

            //prepare the response code
            responseEntity = prepareResponseCodes(responseCodeWithUserId, userAccessProfiles);

            //build success and failure list
            buildSuccessAndFailureBucket(responseCodeWithUserId, refreshJobEntity);


        } else {

            // replace the records by service name api
            responseEntity = refreshJobByServiceName(
                    responseCodeWithUserId, refreshJobEntity);


        }


        log.info(
                "Execution refresh() : {} ms",
                (Math.subtractExact(System.currentTimeMillis(), startTime))
        );

        return responseEntity;
    }


    private ResponseEntity<Object> refreshJobByServiceName(
            Map<String, String> responseCodeWithUserId,
            Optional<RefreshJobEntity> refreshJobEntity) {

        int PAGE_SIZE = 2;
        String SORT_DIRECTION = "ASC";
        String SORT_COLUMN = "roleName";
        ResponseEntity<Object> responseEntity = null;

        //validate the role Category
        ValidationUtil.compareRoleCategory(refreshJobEntity.get().getRoleCategory());

        //Call the CRD Service to retrieve the caseworker profiles base on service name
        ResponseEntity<List<UserProfilesResponse>> response = crdService
                .fetchCaseworkerDetailsByServiceName(refreshJobEntity.get().getJurisdiction(), PAGE_SIZE, 1,
                        SORT_DIRECTION, SORT_COLUMN);


        // 2 step to find out the total number of records
        String total_records = response.getHeaders().getFirst("total_records");
        int pageNumber = (Integer.parseInt(total_records) / PAGE_SIZE);


        //call to CRD
        for (int page = 1; page <= pageNumber; page++) {
            ResponseEntity<List<UserProfilesResponse>> userProfilesResponse = crdService
                    .fetchCaseworkerDetailsByServiceName(refreshJobEntity.get().getJurisdiction(), PAGE_SIZE, page,
                            SORT_DIRECTION, SORT_COLUMN);
            Map<String, Set<UserAccessProfile>> userAccessProfiles = retrieveDataService
                    .getUserAccessProfile(userProfilesResponse);

            responseEntity = prepareResponseCodes(responseCodeWithUserId, userAccessProfiles);


        }

        //build the success and failure list
        buildSuccessAndFailureBucket(responseCodeWithUserId, refreshJobEntity);
        return responseEntity;
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Object> prepareResponseCodes(Map<String, String> responseCodeWithUserId, Map<String,
            Set<UserAccessProfile>> userAccessProfiles) {
        ResponseEntity<Object> responseEntity = requestMappingService.createCaseWorkerAssignments(userAccessProfiles);

        ((List<ResponseEntity>)
                Objects.requireNonNull(responseEntity.getBody())).forEach(entity -> {
            RoleAssignmentRequestResource resource = JacksonUtils
                    .convertRoleAssignmentResource(entity.getBody());

            responseCodeWithUserId.put(resource.getRoleAssignmentRequest()
                    .getRequestedRoles().stream().findFirst().get().getActorId(), entity.getStatusCode().toString());
        });


        log.info("Status code map {} ", responseCodeWithUserId);
        return responseEntity;
    }


    private void buildSuccessAndFailureBucket(Map<String, String> responseCodeWithUserId,
                                              Optional<RefreshJobEntity> refreshJobEntity) {

        List<String> successUserIds = new ArrayList<>();
        List<String> failureUserIds = new ArrayList<>();
        responseCodeWithUserId.forEach((K, V) -> {
            if (!V.equalsIgnoreCase("201 CREATED")) {
                failureUserIds.add(K);
            } else {
                successUserIds.add(K);
            }

        });

        //update the job status
        updateJobStatus(successUserIds, failureUserIds, refreshJobEntity);
    }

    private void updateJobStatus(List<String> successUserIds, List<String> failureUserIds,
                                 Optional<RefreshJobEntity> refreshJobEntity) {

        if (CollectionUtils.isNotEmpty(failureUserIds) && refreshJobEntity.isPresent()) {
            RefreshJobEntity refreshJob = refreshJobEntity.get();
            refreshJob.setStatus("ABORTED");
            refreshJob.setUserIds(failureUserIds.toArray(new String[0]));
            refreshJob.setCreated(LocalDateTime.now());
            persistenceService.persistRefreshJob(refreshJob);

        } else if (CollectionUtils.isEmpty(failureUserIds) && CollectionUtils.isNotEmpty(successUserIds)
                && refreshJobEntity.isPresent()) {
            RefreshJobEntity refreshJob = refreshJobEntity.get();
            refreshJob.setStatus("COMPLETED");
            refreshJob.setCreated(LocalDateTime.now());
            persistenceService.persistRefreshJob(refreshJob);
        }
    }


}
