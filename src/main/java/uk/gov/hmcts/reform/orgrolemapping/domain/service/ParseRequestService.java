package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.util.ValidationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.NUMBER_TEXT_HYPHEN_PATTERN;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.PredicateValidator.nameContains;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.PredicateValidator.objectPredicates;

@Service
@Slf4j
public class ParseRequestService implements ParseRequestBase<Object> {
    //1. This will parse the list of userIds.
    //2. This will parse and validate the user details.

    public void validateUserRequest(UserRequest userRequest) {

        if (userRequest == null || CollectionUtils.isEmpty(userRequest.getUserIds())) {
            throw new BadRequestException("Empty user request");
        }
        //parse the user List and validate each user Id to be valid string
        userRequest.getUserIds().forEach(user ->
                ValidationUtil.validateId(NUMBER_TEXT_HYPHEN_PATTERN, user));
    }

    //this method is common across all service
    @Override
    @SuppressWarnings("unchecked")
    public void validateUserProfiles(List retrievedProfiles, UserRequest userRequest,
                                     AtomicInteger invalidUserProfilesCount,
                                     Set invalidProfiles, UserType userType) {

        if (Collections.isEmpty(retrievedProfiles)) {
            throw new ResourceNotFoundException("The user profiles couldn't be found");
        }
        if (CollectionUtils.isNotEmpty(userRequest.getUserIds())
                && userRequest.getUserIds().size() != retrievedProfiles.size()) {
            List<String> userIdsRetrieved = new ArrayList<>();

            if (nameContains(UserType.CASEWORKER.toString()).test(userType))  {
                List<CaseWorkerProfile> caseworkerUserProfiles = retrievedProfiles;
                caseworkerUserProfiles.forEach(userProfile -> userIdsRetrieved.add(userProfile.getId()));
            } else if (nameContains(UserType.JUDICIAL.toString()).test(userType))  {
                List<JudicialProfile> judicialUserProfiles = retrievedProfiles;
                judicialUserProfiles.forEach(judicialProfile -> userIdsRetrieved.add(judicialProfile.getSidamId()));
            }
            List<String> userIdsNotRetrieved = userRequest.getUserIds().stream().filter(userId -> !userIdsRetrieved
                    .contains(userId)).toList();
            if (nameContains(UserType.JUDICIAL.toString()).test(userType))  {
                userIdsNotRetrieved.forEach(o -> invalidProfiles.add(JudicialProfile.builder().sidamId(o).build()));
            }
            log.error("User profiles couldn't be found for the following userIds :: {}", userIdsNotRetrieved);
        }

        if (nameContains(UserType.CASEWORKER.toString()).test(userType))  {
            caseworkerProfileValidation(retrievedProfiles, invalidUserProfilesCount, invalidProfiles);
        } else if (nameContains(UserType.JUDICIAL.toString()).test(userType))  {
            judicialProfileValidation(retrievedProfiles, invalidUserProfilesCount, invalidProfiles);
        }
    }

    private void caseworkerProfileValidation(List<CaseWorkerProfile> profiles,
                                             AtomicInteger invalidUserProfilesCount,
                                             Set<Object> invalidCaseWorkerProfiles) {

        profiles.forEach(userProfile -> {
            boolean isInvalid = false;
            if (objectPredicates.test(java.util.Collections.singletonList(userProfile.getBaseLocation()))) {
                log.error("The base location is not available for the userProfile {} ", userProfile.getId());
                invalidCaseWorkerProfiles.add(userProfile);
                isInvalid = true;
            }
            if (objectPredicates.test(java.util.Collections.singletonList(userProfile.getWorkArea()))) {
                log.error("The work area is not available for the userProfile {} ", userProfile.getId());
                invalidCaseWorkerProfiles.add(userProfile);
                isInvalid = true;
            }
            if (objectPredicates.test(java.util.Collections.singletonList(userProfile.getRole()))) {
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

    private void judicialProfileValidation(List<JudicialProfile> judicialProfiles,
                                           AtomicInteger invalidUserProfilesCount,
                                           Set<Object> invalidJudicialProfiles) {

        judicialProfiles.forEach(userProfile -> {
            AtomicBoolean isInvalid = new AtomicBoolean(false);
            if (CollectionUtils.isEmpty(userProfile.getAppointments())) {
                log.error("Appointment is not available for the judicialUserProfile :: {}", userProfile.getSidamId());
                invalidJudicialProfiles.add(userProfile);
                isInvalid.set(true);
            }
            if (isInvalid.get()) {
                invalidUserProfilesCount.getAndIncrement();
            }
        });
    }

}
