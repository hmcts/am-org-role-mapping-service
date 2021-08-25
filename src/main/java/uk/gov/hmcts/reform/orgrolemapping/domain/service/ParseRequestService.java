package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.NUMBER_TEXT_HYPHEN_PATTERN;

@Service
@Slf4j
public class ParseRequestService implements ParseRequestBase<Object> {
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
    @SuppressWarnings("unchecked")
    public void validateUserProfiles(List profiles, UserRequest userRequest, AtomicInteger invalidUserProfilesCount,
                                     Set invalidProfiles, UserType userType) {

        if (Collections.isEmpty(profiles)) {
            throw new ResourceNotFoundException("The user profiles couldn't be found");
        }
        if (CollectionUtils.isNotEmpty(userRequest.getUserIds())
                && userRequest.getUserIds().size() != profiles.size()) {
            List<String> userProfileIds = new ArrayList<>();

            if (userType.equals(UserType.CASEWORKER)) {
                List<CaseWorkerProfile> userProfiles = profiles;
                userProfiles.forEach(userProfile -> userProfileIds.add(userProfile.getId()));
            } else if (userType.equals(UserType.JUDICIAL)) {
                List<JudicialProfile> judicialProfileList = profiles;
                judicialProfileList.forEach(judicialProfile -> userProfileIds.add(judicialProfile.getIdamId()));
            }
            List<String> userIdsNotInCRDResponse = userRequest.getUserIds().stream().filter(userId -> !userProfileIds
                    .contains(userId)).collect(Collectors.toList());


            log.error("Some of the user profiles couldn't be found for the userIds {} in "
                    + " Response", userIdsNotInCRDResponse);

        }
        if (userType.equals(UserType.CASEWORKER)) {
            caseworkerProfileValidation(profiles, invalidUserProfilesCount, invalidProfiles);
        } else if (userType.equals(UserType.JUDICIAL)) {

            //Validation of judicial profile
            judicialValidation(profiles, invalidUserProfilesCount, invalidProfiles);

        }

    }

    private void caseworkerProfileValidation(List<CaseWorkerProfile> profiles, AtomicInteger invalidUserProfilesCount,
                                             Set<Object> invalidCaseWorkerProfiles) {

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

    private void judicialValidation(List<JudicialProfile> profiles, AtomicInteger invalidUserProfilesCount,
                                    Set<Object> invalidJudicialProfiles) {

        profiles.forEach(userProfile -> {
            AtomicBoolean isInvalid = new AtomicBoolean(false);
            if (CollectionUtils.isEmpty(userProfile.getAppointments())) {
                log.error("appointment is not available for the judicialProfile {} ", userProfile.getIdamId());
                invalidJudicialProfiles.add(userProfile);
                isInvalid.set(true);
            } else {
                userProfile.getAppointments().forEach(appointment -> {
                    if (StringUtils.isEmpty(appointment.getContractTypeId())
                            || StringUtils.isEmpty(appointment.getRoleId())
                            || StringUtils.isEmpty(appointment.getBaseLocationId())
                            || StringUtils.isEmpty(appointment.getLocationId())

                    ) {
                        log.error("appointment is not valid for the judicialProfile id {} having roleId {} ",
                                userProfile.getIdamId(), appointment.getRoleId());
                        invalidJudicialProfiles.add(userProfile);
                        isInvalid.set(true);
                    }
                });
            }
            checkUserAuthorisations(invalidJudicialProfiles, userProfile, isInvalid);


            if (isInvalid.get()) {
                invalidUserProfilesCount.getAndIncrement();
            }
        });
    }

    private void checkUserAuthorisations(Set<Object> invalidJudicialProfiles, JudicialProfile userProfile,
                                         AtomicBoolean isInvalid) {
        if (CollectionUtils.isEmpty(userProfile.getAuthorisations())) {
            log.error("The authorisation is not available for the judicialProfile {} ", userProfile.getIdamId());
            invalidJudicialProfiles.add(userProfile);
            isInvalid.set(true);
        } else {
            userProfile.getAuthorisations().forEach(authorisation -> {
                if (StringUtils.isEmpty(authorisation.getAuthorisationId())) {
                    log.error("The authorisation is not valid for the judicialProfile {} ",
                            userProfile.getIdamId());
                    invalidJudicialProfiles.add(userProfile);
                    isInvalid.set(true);
                }
            });
        }
    }


}
