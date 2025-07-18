package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

    private final ParseRequestService parseRequestService;

    private final RetrieveDataService retrieveDataService;

    private final RequestMappingService<UserAccessProfile> requestMappingService;

    private final  JudicialBookingService judicialBookingService;

    private final boolean includeJudicialBookings;

    @Autowired
    public BulkAssignmentOrchestrator(ParseRequestService parseRequestService,
                                      RetrieveDataService retrieveDataService,
                                      RequestMappingService<UserAccessProfile> requestMappingService,
                                      JudicialBookingService judicialBookingService,
                                      @Value("${refresh.BulkAssignment.includeJudicialBookings}")
                                      Boolean includeJudicialBookings) {
        this.parseRequestService = parseRequestService;
        this.retrieveDataService = retrieveDataService;
        this.requestMappingService = requestMappingService;
        this.judicialBookingService = judicialBookingService;
        this.includeJudicialBookings = BooleanUtils.isTrue(includeJudicialBookings);
    }

    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> createBulkAssignmentsRequest(UserRequest userRequest, UserType userType) {

        // Extract and Validate received users List
        parseRequestService.validateUserRequest(userRequest);
        log.info("Validated {} userIds of userType {} :: {}",
                userRequest.getUserIds().size(), userType, userRequest.getUserIds());
        long startTime = System.currentTimeMillis();
        Map<String, Set<UserAccessProfile>> userAccessProfiles;
        // Create userAccessProfiles based upon roleId and service codes
        try {
            userAccessProfiles = retrieveDataService
                    .retrieveProfiles(userRequest, userType);
        } catch (FeignException.NotFound feignClientException) {
            log.error("Feign Exception :: {} ", feignClientException.contentUTF8());
            throw new ResourceNotFoundException("User details couldn't be found in RD :: " + userRequest.getUserIds());
        }

        // Call the requestMapping service to determine role name and create role assignment requests
        ResponseEntity<Object> responseEntity;
        if (userType.equals(UserType.JUDICIAL)) {
            List<JudicialBooking> judicialBookings = Collections.emptyList();

            if (includeJudicialBookings) {
                judicialBookings = judicialBookingService.fetchJudicialBookings(userRequest);

                log.info("Judicial Refresh for {} users(s) got {} booking(s)", userRequest.getUserIds().size(),
                        judicialBookings.size());
            }

            responseEntity = requestMappingService.createJudicialAssignments(userAccessProfiles, judicialBookings);
        } else {
            responseEntity = requestMappingService.createCaseworkerAssignments(userAccessProfiles);
        }

        log.debug("Execution time of createBulkAssignmentsRequest() : {} ms",
                (Math.subtractExact(System.currentTimeMillis(), startTime)));

        List<Object> roleAssignmentResponses = new ArrayList<>();
        ((List<ResponseEntity<Object>>)
                Objects.requireNonNull(responseEntity.getBody())).forEach(entity ->
                roleAssignmentResponses.add(entity.getBody()));

        return ResponseEntity.ok(roleAssignmentResponses);
    }

}
