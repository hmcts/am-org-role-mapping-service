package uk.gov.hmcts.reform.orgrolemapping.domain.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.UnprocessableEntityException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JRDUserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.convertProfileToJudicialAccessProfile;
import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.convertInCaseWorkerProfile;
import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.convertInJudicialProfile;
import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.convertListInCaseWorkerProfileResponse;

@Service
@Slf4j
@AllArgsConstructor
public class RetrieveDataService {
    /*
    //1. Fetching multiple case-worker user details from CRD
        //a. Create a new class UserProfile - similar to expected response from CRD(refer LLD)
        //b. Create a new model class UserAccessProfile(id, roleId, roleName, primaryLocationId,
        // primaryLocationName, areaOfWorkId, serviceCode, suspended) (which will flatten the User Profile
        //into multiple
        // userAccessProfile instances based upon roleId X serviceCode).
    //2. Use CRDFeignClient to integrate with CRD and extend the fallback (to prepare some dummy userProfile and
        // userProfileAccess objects).
    //3. Call the parseRequestService to receive UserProfile and apply Validation wherever required.
    //4. Check for multiple Role and serviceCode, If yes prepare cartision product of R X S for UserAccessProfile
    //2. Fetching multiple judicial user details from JRD

     */


    private final ParseRequestService parseRequestService;
    private final CRDService crdService;
    private final JRDService jrdService;

    public Map<String, Set<UserAccessProfile>> retrieveProfiles(UserRequest userRequest, UserType userType)
            throws UnprocessableEntityException {
        long startTime = System.currentTimeMillis();


        var invalidUserProfilesCount = new AtomicInteger();
        Set<Object> invalidProfiles = new HashSet<>();
        Map<String, Set<UserAccessProfile>> usersAccessProfiles = new HashMap<>();
        ResponseEntity<List<Object>> response = null;
        List<Object> profiles = new ArrayList<>();

        Set<String> uniqueUsers = Set.copyOf(userRequest.getUserIds());
        log.info("Actual userIds {} and Unique UserIds are {} ", userRequest.getUserIds().size(), uniqueUsers.size());
        if (userType.equals(UserType.CASEWORKER)) {
            log.info("Calling CRD Service");
            response = crdService.fetchCaseworkerProfiles(
                    UserRequest.builder().userIds(List.copyOf(uniqueUsers)).build());
            log.debug(
                    "Execution time of CRD Response : {} ms",
                    (Math.subtractExact(System.currentTimeMillis(), startTime))
            );
            Objects.requireNonNull(response.getBody()).forEach(o -> profiles.add(convertInCaseWorkerProfile(o)));

        } else if (userType.equals(UserType.JUDICIAL)) {
            log.info("Calling JRD Service");
            response = jrdService.fetchJudicialProfiles(JRDUserRequest.builder().sidamIds(uniqueUsers).build());
            log.debug(
                    "Execution time of JRD Response : {} ms",
                    (Math.subtractExact(System.currentTimeMillis(), startTime))
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                Objects.requireNonNull(response.getBody()).forEach(o -> profiles.add(convertInJudicialProfile(o)));
            } else {
                log.error("Not getting {} Judicial profile", response.getBody());
                throw new UnprocessableEntityException(Constants.FAILED_ROLE_REFRESH);
            }
        }

        getAccessProfile(userRequest, userType, invalidUserProfilesCount, invalidProfiles, usersAccessProfiles,
                response, profiles);

        log.debug(
                "Execution time of retrieveProfiles() : {} ms",
                (Math.subtractExact(System.currentTimeMillis(), startTime))
        );
        return usersAccessProfiles;
    }

    public Map<String, Set<UserAccessProfile>> retrieveProfilesByServiceName(ResponseEntity<List<Object>>
                                                                     userProfileResponsesEntity, UserType userType) {
        //check the response if it's not null
        List<CaseWorkerProfilesResponse> caseWorkerProfilesResponse =
                Objects
                        .requireNonNull(convertListInCaseWorkerProfileResponse(
                                requireNonNull(userProfileResponsesEntity.getBody())));

        //Fetch the user profile from the response
        List<Object> userProfiles = new ArrayList<>();
        caseWorkerProfilesResponse.forEach(cwpr -> userProfiles.add(cwpr
                .getUserProfile()));

        //Collect the userIds to build the UserRequest
        var userRequest = UserRequest.builder().userIds(Collections.emptyList()).build();

        var invalidUserProfilesCount = new AtomicInteger();
        Set<Object> invalidProfiles = new HashSet<>();
        Map<String, Set<UserAccessProfile>> usersAccessProfiles = new HashMap<>();


        getAccessProfile(userRequest, userType, invalidUserProfilesCount, invalidProfiles,
                usersAccessProfiles, userProfileResponsesEntity, userProfiles);


        return usersAccessProfiles;
    }

    @SuppressWarnings("unchecked")
    private void getAccessProfile(UserRequest userRequest, UserType userType, AtomicInteger invalidUserProfilesCount,
                                  Set<Object> invalidProfiles, Map<String, Set<UserAccessProfile>> usersAccessProfiles,
                                  ResponseEntity<List<Object>> response,
                                  List<Object> retrievedProfiles) {
        if (response != null && !CollectionUtils.isEmpty(retrievedProfiles)) {
            // no of userProfiles from  responseEntity.getBody().size()
            log.info("Number of Profile received from RD :: {} ", retrievedProfiles.size());

            parseRequestService.validateUserProfiles(retrievedProfiles, userRequest, invalidUserProfilesCount,
                    invalidProfiles, userType);

            List<Object> validProfiles = requireNonNull(retrievedProfiles).stream()
                    .filter(userProfile -> !invalidProfiles
                            .contains(userProfile)).collect(Collectors.toList());


            if (!CollectionUtils.isEmpty(validProfiles) && userType.equals(UserType.CASEWORKER)) {

                List<CaseWorkerProfile> caseWorkerProfiles = (List<CaseWorkerProfile>) (Object) validProfiles;
                caseWorkerProfiles.forEach(userProfile -> usersAccessProfiles.put(userProfile.getId(),
                        AssignmentRequestBuilder.convertUserProfileToCaseworkerAccessProfile(userProfile)));
            } else if (!CollectionUtils.isEmpty(validProfiles) && userType.equals(UserType.JUDICIAL)) {

                List<JudicialProfile> validJudicialProfiles = (List<JudicialProfile>) (Object) validProfiles;
                validJudicialProfiles.forEach(userProfile -> usersAccessProfiles.put(userProfile.getSidamId(),
                        convertProfileToJudicialAccessProfile(userProfile)));
            }
            Map<String, Integer> userAccessProfileCount = new HashMap<>();
            usersAccessProfiles.forEach((k, v) -> {
                    userAccessProfileCount.put(k, v.size());
                    log.debug("UserId {} having the corresponding UserAccessProfile {}", k, v);
                }
            );
            log.info("Count of UserAccessProfiles corresponding to the userIds {} :: ", userAccessProfileCount);

            // no of user profile successfully validated
            if (invalidUserProfilesCount.get() > 0) {
                log.info("Number of invalid Profile Count : {} ", invalidUserProfilesCount.get());
            }

        } else {
            log.error("No UserProfile received from RD");
        }
    }

}
