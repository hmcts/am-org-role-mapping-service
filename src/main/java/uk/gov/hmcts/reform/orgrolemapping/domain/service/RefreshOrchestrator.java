package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.UnauthorizedServiceException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.UnprocessableEntityException;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;
import uk.gov.hmcts.reform.orgrolemapping.util.UtilityFunctions;
import uk.gov.hmcts.reform.orgrolemapping.util.ValidationUtil;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.ABORTED;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.COMPLETED;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.FAILED_JOB;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.NEW;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SUCCESS_JOB;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.PredicateValidator.NullCheckBiPredicate;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.PredicateValidator.nullCheckPredicate;
import static uk.gov.hmcts.reform.orgrolemapping.v1.V1.Error.UNAUTHORIZED_SERVICE;

@Service
@Slf4j
public class RefreshOrchestrator {
    private final RetrieveDataService retrieveDataService;
    private final RequestMappingService<UserAccessProfile> requestMappingService;
    private final ParseRequestService parseRequestService;
    private final CRDService crdService;
    private final PersistenceService persistenceService;
    private final SecurityUtils securityUtils;
    private final JudicialBookingService judicialBookingService;
    private final String sortDirection;
    private final String sortColumn;
    private final List<String> authorisedServices;
    private final boolean includeJudicialBookings;
    String pageSize;

    @Autowired
    public RefreshOrchestrator(RetrieveDataService retrieveDataService,
                               RequestMappingService<UserAccessProfile> requestMappingService,
                               ParseRequestService parseRequestService,
                               CRDService crdService, PersistenceService persistenceService,
                               SecurityUtils securityUtils,
                               JudicialBookingService judicialBookingService,
                               @Value("${refresh.Job.pageSize}") String pageSize,
                               @Value("${refresh.Job.sortDirection}") String sortDirection,
                               @Value("${refresh.Job.sortColumn}") String sortColumn,
                               @Value("${refresh.Job.authorisedServices}") List<String> authorisedServices,
                               @Value("${refresh.Job.includeJudicialBookings}") Boolean  includeJudicialBookings) {
        this.retrieveDataService = retrieveDataService;
        this.requestMappingService = requestMappingService;
        this.parseRequestService = parseRequestService;
        this.crdService = crdService;
        this.persistenceService = persistenceService;
        this.securityUtils = securityUtils;
        this.judicialBookingService = judicialBookingService;
        this.pageSize = pageSize;
        this.sortDirection = sortDirection;
        this.sortColumn = sortColumn;
        this.authorisedServices = authorisedServices;
        this.includeJudicialBookings = BooleanUtils.isTrue(includeJudicialBookings);
    }

    public void validate(Long jobId, UserRequest userRequest) {
        // Ensure only permitted service is invoking caseworker refresh API
        final String serviceName = securityUtils.getServiceName();
        if (!authorisedServices.contains(serviceName)) {
            throw new UnauthorizedServiceException(UNAUTHORIZED_SERVICE);
        }

        if (jobId == null) {
            throw new BadRequestException("Invalid JobId request");
        }

        if (userRequest != null && nullCheckPredicate.test(userRequest.getUserIds())) {
            // Extract and Validate received users List
            parseRequestService.validateUserRequest(userRequest);
            log.info("Validated userIds {}", userRequest.getUserIds());
        }
    }

    @Async
    public ResponseEntity<Object> refresh(Long jobId, UserRequest userRequest) {

        var startTime = System.currentTimeMillis();
        Map<String, HttpStatus> responseCodeWithUserId = new HashMap<>();
        ResponseEntity<Object> responseEntity = null;
        Map<String, Set<UserAccessProfile>> userAccessProfiles;

        //fetch the entity based on jobId
        Optional<RefreshJobEntity> refreshJobEntity = persistenceService.fetchRefreshJobById(jobId);
        if (refreshJobEntity.isEmpty()) {
            throw new UnprocessableEntityException("Provided refresh job couldn't be retrieved.");
        } else if (!NEW.equalsIgnoreCase(refreshJobEntity.get().getStatus())) {
            throw new UnprocessableEntityException("Provided refresh job is in an invalid state.");
        } else {
            log.info("The refresh job {} retrieved from the DB to run {}", refreshJobEntity.get().getJobId(),
                    refreshJobEntity.get().getRoleCategory());
        }

        if (userRequest != null && nullCheckPredicate.test(userRequest.getUserIds())) {
            try {
                //Create userAccessProfiles based upon userIds

                if (refreshJobEntity.get().getRoleCategory()
                        .equals(RoleCategory.LEGAL_OPERATIONS.name())) {
                    userAccessProfiles = retrieveDataService
                            .retrieveProfiles(userRequest, UserType.CASEWORKER);
                    log.info("Total profiles received from CRD is {}", userAccessProfiles.size());
                    //prepare the response code
                    responseEntity = prepareResponseCodes(responseCodeWithUserId, userAccessProfiles,
                            UserType.CASEWORKER);
                } else if (refreshJobEntity.get().getRoleCategory()
                        .equals(RoleCategory.JUDICIAL.name())) {
                    userAccessProfiles = retrieveDataService
                            .retrieveProfiles(userRequest, UserType.JUDICIAL);
                    log.info("Total profiles received from JRD is {}", userAccessProfiles.size());
                    //prepare the response code
                    responseEntity = prepareResponseCodes(responseCodeWithUserId, userAccessProfiles,
                            UserType.JUDICIAL);
                }
            } catch (FeignException.NotFound feignClientException) {

                log.error("Feign Exception :: {} ", feignClientException.contentUTF8());
                responseCodeWithUserId.put(StringUtils.join(userRequest.getUserIds(), ","),
                        HttpStatus.resolve(feignClientException.status()));

            }

            //build success and failure list
            buildSuccessAndFailureBucket(responseCodeWithUserId, refreshJobEntity.get());

        } else {
            // replace the records by service name api
            responseEntity = refreshJobByServiceName(responseCodeWithUserId, refreshJobEntity.get(),
                     refreshJobEntity.get().getRoleCategory()
                            .equals(RoleCategory.LEGAL_OPERATIONS.name()) ? UserType.CASEWORKER : UserType.JUDICIAL);
        }


        log.debug("Execution refresh() : {} ms", (Math.subtractExact(System.currentTimeMillis(), startTime)));

        return responseEntity;
    }


    protected ResponseEntity<Object> refreshJobByServiceName(Map<String, HttpStatus> responseCodeWithUserId,
                                                             RefreshJobEntity refreshJobEntity, UserType userType) {

        ResponseEntity<Object> responseEntity = null;

        //validate the role Category
        ValidationUtil.compareRoleCategory(refreshJobEntity.getRoleCategory());
        log.info("fetching details from RD for :: {} ", userType);
        try {
            if (userType.equals(UserType.CASEWORKER)) {
                //Call to CRD Service to retrieve the total number of records in first call
                ResponseEntity<List<Object>> response = crdService
                        .fetchCaseworkerDetailsByServiceName(refreshJobEntity.getJurisdiction(),
                                Integer.parseInt(pageSize), 0,
                                sortDirection, sortColumn);

                log.info("fetching details from RD for :: {} ", userType);
                // 2 step to find out the total number of records from header
                var totalRecords = response.getHeaders().getFirst("total_records");
                assert totalRecords != null;
                double pageNumber = 0;
                if (Integer.parseInt(pageSize) > 0) {
                    pageNumber = Double.parseDouble(totalRecords) / Double.parseDouble(pageSize);
                }

                //call to CRD
                for (var page = 0; page < pageNumber; page++) {
                    ResponseEntity<List<Object>> userProfilesResponse = crdService
                            .fetchCaseworkerDetailsByServiceName(refreshJobEntity.getJurisdiction(),
                                    Integer.parseInt(pageSize), page,
                                    sortDirection, sortColumn);
                    Map<String, Set<UserAccessProfile>> userAccessProfiles = retrieveDataService
                            .retrieveProfilesByServiceName(userProfilesResponse, userType);

                    responseEntity = prepareResponseCodes(responseCodeWithUserId, userAccessProfiles, userType);
                }
            }
        } catch (FeignException.NotFound feignClientException) {
            log.error("Feign Exception :: {} ", feignClientException.contentUTF8());
            responseCodeWithUserId.put("", HttpStatus.resolve(feignClientException.status()));
        }
        log.info("fetched all records from RD  :: {} ", userType);
        //build the success and failure list
        buildSuccessAndFailureBucket(responseCodeWithUserId, refreshJobEntity);
        return responseEntity;
    }

    //This service is triggered when a Refresh JOB is triggered
    @SuppressWarnings("unchecked")
    protected ResponseEntity<Object> prepareResponseCodes(Map<String, HttpStatus> responseCodeWithUserId, Map<String,
            Set<UserAccessProfile>> userAccessProfiles, UserType userType) {
        ResponseEntity<Object> responseEntity;

        if (userType.equals(UserType.JUDICIAL)) {
            List<JudicialBooking> judicialBookings = Collections.emptyList();

            if (includeJudicialBookings) {
                List<String> userIds = UtilityFunctions.getUserIdsFromJudicialAccessProfileMap(userAccessProfiles);

                judicialBookings = judicialBookingService.fetchJudicialBookingsInBatches(userIds, pageSize);

                log.info("Judicial Refresh for {} users(s) got {} booking(s)", userIds.size(), judicialBookings.size());
            }

            responseEntity = requestMappingService.createJudicialAssignments(userAccessProfiles, judicialBookings);
        } else {
            responseEntity = requestMappingService.createCaseworkerAssignments(userAccessProfiles);
        }

        ((List<ResponseEntity<Object>>)
                Objects.requireNonNull(responseEntity.getBody())).forEach(entity -> {
                    RoleAssignmentRequestResource resource = JacksonUtils
                        .convertRoleAssignmentResource(entity.getBody());

                    responseCodeWithUserId.put(resource.getRoleAssignmentRequest()
                        .getRequest().getReference(), HttpStatus.valueOf(entity.getStatusCode().value())
                    );
                });

        log.info("Status code map from RAS {} ", responseCodeWithUserId);
        return responseEntity;
    }

    protected void buildSuccessAndFailureBucket(Map<String, HttpStatus> responseCodeWithUserId,
                                                RefreshJobEntity refreshJobEntity) {

        List<String> successUserIds = new ArrayList<>();
        List<String> failureUserIds = new ArrayList<>();
        responseCodeWithUserId.forEach((k, v) -> {
            if (v != HttpStatus.CREATED) {
                failureUserIds.add(k);
            } else {
                successUserIds.add(k);
            }

        });

        //update the job status
        updateJobStatus(successUserIds, failureUserIds, refreshJobEntity);
    }

    protected void updateJobStatus(List<String> successUserIds, List<String> failureUserIds,
                                   RefreshJobEntity refreshJobEntity) {

        if (NullCheckBiPredicate.test(failureUserIds,refreshJobEntity)) {
            refreshJobEntity.setStatus(ABORTED);
            refreshJobEntity.setUserIds(failureUserIds.toArray(new String[0]));
            refreshJobEntity.setCreated(ZonedDateTime.now());
            refreshJobEntity.setLog(String.format(FAILED_JOB, failureUserIds));
            persistenceService.persistRefreshJob(refreshJobEntity);

        } else if (!nullCheckPredicate.test(failureUserIds)
            && NullCheckBiPredicate.test(successUserIds,refreshJobEntity)) {

            refreshJobEntity.setStatus(COMPLETED);
            refreshJobEntity.setCreated(ZonedDateTime.now());
            refreshJobEntity.setLog(String.format(SUCCESS_JOB, successUserIds));
            persistenceService.persistRefreshJob(refreshJobEntity);
        }
    }


}
