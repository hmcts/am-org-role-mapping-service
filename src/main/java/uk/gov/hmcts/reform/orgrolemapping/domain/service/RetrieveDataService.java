package uk.gov.hmcts.reform.orgrolemapping.domain.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;

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
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.convertUserProfileToUserAccessProfile;
import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.convertInCaseWorkerProfile;
import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.convertInJudicialProfile;

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

    @SuppressWarnings("unchecked")
    public Map<String, Set<?>> retrieveProfiles(UserRequest userRequest, UserType userType) {
        long startTime = System.currentTimeMillis();


        AtomicInteger invalidUserProfilesCount = new AtomicInteger();
        Set<Object> invalidProfiles = new HashSet<>();
        Map<String, Set<?>> usersAccessProfiles = new HashMap<>();
        ResponseEntity<List<Object>> response = null;
        List<Object> profiles = new ArrayList<>();

        if (userType.equals(UserType.CASEWORKER)) {
            response = crdService.fetchUserProfiles(userRequest);

            Objects.requireNonNull(response.getBody()).forEach(o -> profiles.add(convertInCaseWorkerProfile(o)));

        } else if (userType.equals(UserType.JUDICIAL)) {
            response = jrdService.fetchJudicialProfiles(userRequest);
            Objects.requireNonNull(response.getBody()).forEach(o -> profiles.add(convertInJudicialProfile(o)));


        }

        log.debug(
                "Execution time of CRD Response : {} ms",
                (Math.subtractExact(System.currentTimeMillis(), startTime))
        );
        getAccessProfile(userRequest, userType, invalidUserProfilesCount, invalidProfiles, usersAccessProfiles,
                response, profiles);


        log.info(
                "Execution time of retrieveProfiles() : {} ms",
                (Math.subtractExact(System.currentTimeMillis(), startTime))
        );
        return usersAccessProfiles;
    }


    @SuppressWarnings("unchecked")
    public Map<String, Set<?>> retrieveProfilesByServiceName(ResponseEntity<List<Object>>
                                                                     userProfileResponsesEntity, UserType userType) {


        //check the response if it's not null
        List<CaseWorkerProfilesResponse> caseWorkerProfilesRespons = (List<CaseWorkerProfilesResponse>)
                (List<?>) Objects
                .requireNonNull(userProfileResponsesEntity.getBody());

        //Fetch the user profile from the response
        List<Object> userProfiles = new ArrayList<>();
        caseWorkerProfilesRespons.forEach(caseWorkerProfilesResponse -> userProfiles.add(caseWorkerProfilesResponse
                .getUserProfile()));

        //Collect the userIds to build the UserRequest
        UserRequest userRequest = UserRequest.builder().userIds(Collections.emptyList()).build();

        AtomicInteger invalidUserProfilesCount = new AtomicInteger();
        Set<Object> invalidProfiles = new HashSet<>();
        Map<String, Set<?>> usersAccessProfiles = new HashMap<>();


        getAccessProfile(userRequest, userType, invalidUserProfilesCount, invalidProfiles,
                usersAccessProfiles, userProfileResponsesEntity, userProfiles);


        return usersAccessProfiles;
    }

    @SuppressWarnings("unchecked")
    private void getAccessProfile(UserRequest userRequest, UserType userType, AtomicInteger invalidUserProfilesCount,
                                  Set<Object> invalidProfiles, Map<String, Set<?>> usersAccessProfiles,
                                  ResponseEntity<List<Object>> response,
                                  List<Object> profiles) {
        if (response != null && !CollectionUtils.isEmpty(profiles)) {
            // no of userProfiles from  responseEntity.getBody().size()
            log.info("Number of Profile received from upstream : {} ",
                    profiles.size());

            parseRequestService.validateUserProfiles(profiles, userRequest, invalidUserProfilesCount,
                    invalidProfiles, userType);

            List<Object> validProfiles = requireNonNull(profiles).stream()
                    .filter(userProfile -> !invalidProfiles
                            .contains(userProfile)).collect(Collectors.toList());


            if (!CollectionUtils.isEmpty(validProfiles) && userType.equals(UserType.CASEWORKER)) {

                List<CaseWorkerProfile> caseWorkerProfiles = (List<CaseWorkerProfile>) (Object) validProfiles;
                caseWorkerProfiles.forEach(userProfile -> usersAccessProfiles.put(userProfile.getId(),
                        convertUserProfileToUserAccessProfile(userProfile)));
            } else if (!CollectionUtils.isEmpty(validProfiles) && userType.equals(UserType.JUDICIAL)) {

                List<JudicialProfile> validJudicialProfiles = (List<JudicialProfile>) (Object) validProfiles;
                validJudicialProfiles.forEach(userProfile -> usersAccessProfiles.put(userProfile.getElinkId(),
                        convertProfileToJudicialAccessProfile(userProfile)));
            }
            Map<String, Integer> userAccessProfileCount = new HashMap<>();
            usersAccessProfiles.forEach((k, v) -> {
                    userAccessProfileCount.put(k, v.size());
                    log.debug("UserId {} having the corresponding UserAccessProfile {}", k,
                                v);
                }
            );
            log.info("Count of UserAccessProfiles corresponding to the userIds {} ::", userAccessProfileCount);

            // no of user profile successfully validated
            if (invalidUserProfilesCount.get() > 0) {
                log.info("Number of invalid Profile Count : {} ", invalidUserProfilesCount.get());
            }

        } else {
            log.info("Number of UserProfile received from upstream : {} ", 0);
        }
    }

}
