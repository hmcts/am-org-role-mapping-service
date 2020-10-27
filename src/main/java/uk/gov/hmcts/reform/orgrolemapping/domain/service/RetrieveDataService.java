package uk.gov.hmcts.reform.orgrolemapping.domain.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.convertUserProfileToUserAccessProfile;

@Service
@Slf4j
public class RetrieveDataService {
    /*
    //1. Fetching multiple case-worker user details from CRD
        //a. Create a new class UserProfile - similar to expected response from CRD(refer LLD)
        //b. Create a new model class UserAccessProfile(id, roleId, roleName, primaryLocationId,
        // primaryLocationName, areaOfWorkId, serviceCode, deleteFlag) (which will flatten the User Profile
        //into multiple
        // userAccessProfile instances based upon roleId X serviceCode).
    //2. Use CRDFeignClient to integrate with CRD and extend the fallback (to prepare some dummy userProfile and
        // userProfileAccess objects).
    //3. Call the parseRequestService to receive UserProfile and apply Validation wherever required.
    //4. Check for multiple Role and serviceCode, If yes prepare cartision product of R X S for UserAccessProfile
    //2. Fetching multiple judicial user details from JRD

     */

    private final CRDFeignClient crdFeignClient;
    private final ParseRequestService parseRequestService;

    public RetrieveDataService(CRDFeignClient crdFeignClient,
                                ParseRequestService parseRequestService) {
        this.crdFeignClient = crdFeignClient;
        this.parseRequestService = parseRequestService;
    }


    public Map<String,Set<UserAccessProfile>> retrieveCaseWorkerProfiles(UserRequest userRequest) {
        ResponseEntity<List<UserProfile>> responseEntity = crdFeignClient.createRoleAssignment(userRequest);

        List<UserProfile> userProfiles = responseEntity.getBody();
        parseRequestService.validateUserProfiles(userProfiles, userRequest);

        Map<String, Set<UserAccessProfile>> usersAccessProfiles = new HashMap<>();
        userProfiles.stream().forEach(userProfile -> usersAccessProfiles.put(userProfile.getId(),
                convertUserProfileToUserAccessProfile(userProfile)));
        return usersAccessProfiles;
    }
}
