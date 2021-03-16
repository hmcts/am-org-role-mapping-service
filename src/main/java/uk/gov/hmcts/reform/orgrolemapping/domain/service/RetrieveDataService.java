package uk.gov.hmcts.reform.orgrolemapping.domain.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JRDFeignClient;

import java.util.Collection;
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
public class RetrieveDataService implements RetrieveProfile<String, Collection<Object>> {
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
    private final JRDFeignClient jrdFeignClient;


    public Map<String, Collection<Object>> retrieveCaseWorkerProfiles(UserRequest userRequest, UserType userType) {
        long startTime = System.currentTimeMillis();

        AtomicInteger invalidUserProfilesCount = new AtomicInteger();
        Set<Object> invalidProfiles = new HashSet<>();

       if(userType.equals(UserType.CASEWORKER)) {
           ResponseEntity<List<CaseWorkerProfile>> caseworkerResponse  = crdFeignClient.getCaseworkerDetailsById(userRequest);

           if (!CollectionUtils.isEmpty(caseworkerResponse.getBody())) {
               // no of userProfiles from CRD  responseEntity.getBody().size()
               log.info("Number of UserProfile received from CRD : {} ",
                       caseworkerResponse.getBody().size());

               parseRequestService.validateUserProfiles( caseworkerResponse.getBody(), userRequest, invalidUserProfilesCount,
                       invalidProfiles,userType);

               List<CaseWorkerProfile> validCaseWorkerProfiles = requireNonNull(caseworkerResponse.getBody()).stream()
                       .filter(userProfile -> !invalidProfiles
                               .contains(userProfile)).collect(Collectors.toList());
               Map<String, Set<CaseWorkerAccessProfile>> usersAccessProfiles = new HashMap<>();

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
               return usersAccessProfiles;

           } else {
               log.info("Number of UserProfile received from CRD : {} ", 0);
           }




       } else if(userType.equals(UserType.JUDICIAL)){
           ResponseEntity<List<JudicialProfile>> responseEntity = jrdFeignClient.getJudicialDetailsById(userRequest);


        }

        // no of user profile successfully validated
        if (invalidUserProfilesCount.get() > 0) {
            log.info("Number of invalid UserProfileCount : {} ", invalidUserProfilesCount.get());
        }
        log.info(
                "Execution time of CRD Response : {} ms",
                (Math.subtractExact(System.currentTimeMillis(),startTime))
        );


        log.info(
                "Execution time of retrieveCaseWorkerProfiles() : {} ms",
                (Math.subtractExact(System.currentTimeMillis(),startTime))
        );

    }
}
