package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.util.ValidationUtil;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.NUMBER_TEXT_HYPHEN_PATTERN;

@Service
@Slf4j
public class ParseRequestService {
    //1. This will parse the list of userIds and validate them.
    //2. This will parse and validate the user details received from CRD

    public void validateUserRequest(UserRequest userRequest) {

        if (CollectionUtils.isEmpty(userRequest.getUsers())) {
            throw new BadRequestException("The UserIds  is not available in the user's request");
        }
        //parse the user List and validate each user Id to be valid string
        userRequest.getUsers().forEach(user ->
                ValidationUtil.validateId(NUMBER_TEXT_HYPHEN_PATTERN, user));
    }

    public void validateUserProfiles(List<UserProfile> userProfiles, UserRequest userRequest) {
        if (Collections.isEmpty(userProfiles)) {
            throw new ResourceNotFoundException("The user profiles couldn't be found");
        }
        if (userRequest.getUsers().size() != userProfiles.size()) {
            List<String> userIdsNotInCRDResponse = userRequest.getUsers().stream()
                    .filter(userId -> !userProfiles.contains(userId))
                    .collect(Collectors.toList());
            log.error("Some of the user profiles couldn't be found for the userIds {} in "
                    + "CRD Response",userIdsNotInCRDResponse);

        }

        userProfiles.forEach(userProfile -> {
            if (CollectionUtils.isEmpty(userProfile.getBaseLocation())) {
                throw new BadRequestException("The base location is not available");
            }
            if (CollectionUtils.isEmpty(userProfile.getWorkArea())) {
                throw new BadRequestException("The work area is not available");
            }
            if (CollectionUtils.isEmpty(userProfile.getRole())) {
                throw new BadRequestException("The role is not available");
            }
            long primaryLocation = userProfile.getBaseLocation().stream()
                    .filter(UserProfile.BaseLocation::isPrimary)
                    .count();
            if (primaryLocation != 1) {
                throw new BadRequestException(String.format("The user has %s primary location(s), only 1 is allowed",
                        primaryLocation));
            }

        });

    }
}
