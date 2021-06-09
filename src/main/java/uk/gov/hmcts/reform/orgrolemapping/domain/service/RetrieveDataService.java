package uk.gov.hmcts.reform.orgrolemapping.domain.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import static java.util.Objects.requireNonNull;
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
    private final CRDFeignClient crdFeignClient;


    public Map<String, Set<UserAccessProfile>> retrieveCaseWorkerProfiles(UserRequest userRequest) {
        long startTime = System.currentTimeMillis();

        ResponseEntity<List<UserProfile>> responseEntity = crdFeignClient.getCaseworkerDetailsById(userRequest);

        log.debug("Execution time of CRD Response : {} ms",
                (Math.subtractExact(System.currentTimeMillis(), startTime)));

        List<UserProfile> userProfiles = responseEntity.getBody();
        if (!CollectionUtils.isEmpty(userProfiles)) {
            // no of userProfiles from CRD  responseEntity.getBody().size()
            log.info("Number of UserProfile received from CRD : {} ",
                    userProfiles.size());
        } else {
            log.info("Number of UserProfile received from CRD : {} ", 0);
        }

        AtomicInteger invalidUserProfilesCount = new AtomicInteger();
        Set<UserProfile> invalidUserProfiles = new HashSet<>();
        parseRequestService.validateUserProfiles(userProfiles, userRequest, invalidUserProfilesCount,
                invalidUserProfiles);


        // no of user profile successfully validated
        if (invalidUserProfilesCount.get() > 0) {
            log.info("Number of invalid UserProfileCount : {} ", invalidUserProfilesCount.get());
        }

        // filter the valid userProfiles.
        List<UserProfile> validUserProfiles = requireNonNull(userProfiles).stream()
                .filter(userProfile -> !invalidUserProfiles
                        .contains(userProfile)).collect(Collectors.toList());

        Map<String, Set<UserAccessProfile>> usersAccessProfiles = new HashMap<>();

        if (!CollectionUtils.isEmpty(validUserProfiles)) {
            validUserProfiles.forEach(userProfile -> usersAccessProfiles.put(userProfile.getId(),
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

        log.debug("Execution time of retrieveCaseWorkerProfiles() : {} ms",
                (Math.subtractExact(System.currentTimeMillis(), startTime)));
        return usersAccessProfiles;
    }
}
