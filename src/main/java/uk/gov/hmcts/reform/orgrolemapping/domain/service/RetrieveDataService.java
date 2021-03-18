package uk.gov.hmcts.reform.orgrolemapping.domain.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.CRDFeignClientFallback;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.JRDFeignClientFallback;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.convertUserProfileToJudicialAccessProfile;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.convertUserProfileToUserAccessProfile;

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
    private final CRDFeignClientFallback crdFeignClient;
    private final JRDFeignClientFallback jrdFeignClient;


    public Map<String, Set<CaseWorkerAccessProfile>> retrieveCaseWorkerProfiles(UserRequest userRequest, UserType userType) {
        long startTime = System.currentTimeMillis();

         AtomicInteger invalidUserProfilesCount = new AtomicInteger();
         Set<Object> invalidProfiles = new HashSet<>();
        Map<String, Set<CaseWorkerAccessProfile>> usersAccessProfiles = new HashMap<>();

        if (userType.equals(UserType.CASEWORKER)) {
            ResponseEntity<List<CaseWorkerProfile>> caseworkerResponse = crdFeignClient.getCaseworkerDetailsById(userRequest);

            log.info(
                    "Execution time of CRD Response : {} ms",
                    (Math.subtractExact(System.currentTimeMillis(), startTime))
            );

            if (!CollectionUtils.isEmpty(caseworkerResponse.getBody())) {
                // no of userProfiles from CRD  responseEntity.getBody().size()
                log.info("Number of CaseWorkerProfile received from CRD : {} ",
                        caseworkerResponse.getBody().size());

                parseRequestService.validateUserProfiles(caseworkerResponse.getBody(), userRequest, invalidUserProfilesCount,
                        invalidProfiles, userType);

                List<CaseWorkerProfile> validCaseWorkerProfiles = requireNonNull(caseworkerResponse.getBody()).stream()
                        .filter(userProfile -> !invalidProfiles
                                .contains(userProfile)).collect(Collectors.toList());


                if (!CollectionUtils.isEmpty(validCaseWorkerProfiles)) {
                    validCaseWorkerProfiles.forEach(userProfile -> usersAccessProfiles.put(userProfile.getId(),
                            convertUserProfileToUserAccessProfile(userProfile)));
                }
                Map<String, Integer> userAccessProfileCount = new HashMap<>();
                usersAccessProfiles.forEach((k, v) -> {
                            userAccessProfileCount.put(k, v.size());
                            log.debug("UserId {} having the corresponding UserAccessProfile {}", k,
                                    v);
                        }
                );
                log.info("Count of UserAccessProfiles corresponding to the userIds {} ::", userAccessProfileCount);


            } else {
                log.info("Number of UserProfile received from CRD : {} ", 0);
            }


        }

        // no of user profile successfully validated
        if (invalidUserProfilesCount.get() > 0) {
            log.info("Number of invalid CaseWorkerProfile Count : {} ", invalidUserProfilesCount.get());
        }


        log.info(
                "Execution time of retrieveCaseWorkerProfiles() : {} ms",
                (Math.subtractExact(System.currentTimeMillis(), startTime))
        );
        return usersAccessProfiles;
    }



    public Map<String, Set<JudicialAccessProfile>> retrieveJudicialProfiles(UserRequest userRequest, UserType userType) {


        AtomicInteger invalidUserProfilesCount = new AtomicInteger();
        Set<Object> invalidProfiles = new HashSet<>();
        Map<String, Set<JudicialAccessProfile>> judicialAccessProfiles = new HashMap<>();


        ResponseEntity<List<JudicialProfile>> judicialResponse = jrdFeignClient.getJudicialDetailsById(userRequest);

        if (!CollectionUtils.isEmpty(judicialResponse.getBody())) {
            // no of userProfiles from CRD  responseEntity.getBody().size()
            log.info("Number of JudicialProfile received from JRD : {} ",
                    judicialResponse.getBody().size());

            parseRequestService.validateUserProfiles(judicialResponse.getBody(), userRequest, invalidUserProfilesCount,
                    invalidProfiles, userType);

            List<JudicialProfile> validJudicialProfiles = requireNonNull(judicialResponse.getBody()).stream()
                    .filter(userProfile -> !invalidProfiles
                            .contains(userProfile)).collect(Collectors.toList());


            if (!CollectionUtils.isEmpty(validJudicialProfiles)) {
                validJudicialProfiles.forEach(userProfile -> judicialAccessProfiles.put(userProfile.getElinkId(),
                        convertUserProfileToJudicialAccessProfile(userProfile)));
            }
            Map<String, Integer> userAccessProfileCount = new HashMap<>();
            judicialAccessProfiles.forEach((k, v) -> {
                        userAccessProfileCount.put(k, v.size());
                        log.debug("UserId {} having the corresponding JudicialAccessProfile {}", k,
                                v);
                    }
            );
            log.info("Count of JudicialAccessProfiles corresponding to the userIds {} ::", userAccessProfileCount);


        } else {
            log.info("Number of JudicialProfile received from JRD : {} ", 0);
        }


        // no of user profile successfully validated
        if (invalidUserProfilesCount.get() > 0) {
            log.info("Number of invalid JudicialProfileCount : {} ", invalidUserProfilesCount.get());
        }

        return judicialAccessProfiles;
    }
}
