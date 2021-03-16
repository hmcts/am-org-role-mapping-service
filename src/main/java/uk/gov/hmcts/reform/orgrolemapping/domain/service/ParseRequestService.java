package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.util.ValidationUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.NUMBER_TEXT_HYPHEN_PATTERN;

@Service
@Slf4j
public class ParseRequestService extends ParseRequestBase {
    //1. This will parse the list of userIds and validate them.
    //2. This will parse and validate the user details received from CRD

    public void validateUserRequest(UserRequest userRequest) {

        if (CollectionUtils.isEmpty(userRequest.getUserIds())) {
            throw new BadRequestException("Empty user request");
        }
        //parse the user List and validate each user Id to be valid string
        userRequest.getUserIds().forEach(user ->
                ValidationUtil.validateId(NUMBER_TEXT_HYPHEN_PATTERN, user));
    }

    //this method is common across all service
    @Override
    public void validateUserProfiles(List profiles, UserRequest userRequest, AtomicInteger invalidUserProfilesCount, Set invalidProfiles, UserType userType) {

        if (Collections.isEmpty(profiles)) {
            throw new ResourceNotFoundException("The user profiles couldn't be found");
        }
        if (userRequest.getUserIds().size() != profiles.size()) {
            List<String> userProfileIds = new ArrayList<>();

            if(userType.equals(UserType.CASEWORKER)) {
            List<CaseWorkerProfile> caseWorkerProfiles  = profiles;
                caseWorkerProfiles.forEach(userProfile -> userProfileIds.add(userProfile.getId()));
            } else if(userType.equals(UserType.JUDICIAL)){
                List<JudicialProfile> judicialProfileList  = profiles;
                judicialProfileList.forEach(judicialProfile -> userProfileIds.add(judicialProfile.getElinkId()));
            }
            List<String> userIdsNotInCRDResponse = userRequest.getUserIds().stream().filter(userId -> !userProfileIds
                    .contains(userId)).collect(Collectors.toList());


            log.error("Some of the user profiles couldn't be found for the userIds {} in "
                    + "CRD Response", userIdsNotInCRDResponse);

        }
        if(userType.equals(UserType.CASEWORKER)) {
            caseworkerProfileValidation(profiles, invalidUserProfilesCount, invalidProfiles);
        } else if(userType.equals(UserType.JUDICIAL)){

            //Validation of judicial profile

        }

    }

    private void caseworkerProfileValidation(List<CaseWorkerProfile> profiles, AtomicInteger invalidUserProfilesCount, Set<Object> invalidCaseWorkerProfiles) {

        profiles.forEach(userProfile -> {
            boolean isInvalid = false;
            if (CollectionUtils.isEmpty(userProfile.getBaseLocation())) {
                log.error("The base location is not available for the userProfile {} ", userProfile.getId());
                invalidCaseWorkerProfiles.add(userProfile);
                isInvalid = true;
            }
            if (CollectionUtils.isEmpty(userProfile.getWorkArea())) {
                log.error("The work area is not available for the userProfile {} ", userProfile.getId());
                invalidCaseWorkerProfiles.add(userProfile);
                isInvalid = true;
            }
            if (CollectionUtils.isEmpty(userProfile.getRole())) {
                log.error("The role is not available for the userProfile {} ", userProfile.getId());
                invalidCaseWorkerProfiles.add(userProfile);
                isInvalid = true;
            }
            long primaryLocationCount = userProfile.getBaseLocation().stream()
                    .filter(CaseWorkerProfile.BaseLocation::isPrimary)
                    .count();
            if (primaryLocationCount != 1) {
                log.error("The userProfile {} has {} primary location(s), only 1 is allowed",
                        userProfile.getId(), primaryLocationCount);
                invalidCaseWorkerProfiles.add(userProfile);
                isInvalid = true;
            }
            if (isInvalid) {
                invalidUserProfilesCount.getAndIncrement();
            }
        });
    }


}
