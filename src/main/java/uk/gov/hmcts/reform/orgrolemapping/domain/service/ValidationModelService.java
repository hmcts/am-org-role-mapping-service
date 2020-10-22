package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;

import java.util.List;

@Service
@Slf4j
public class ValidationModelService {
    private ValidationModelService() {
    }

    //1. receive single UserAccessProfile for caseworker
    //2. receive initial requestedRole corresponding to above userAccessProfile
    //3. Run the rules for preparing the final requestedRole.
    public static void validateUserProfiles(List<UserProfile> userProfileList) {
        if (Collections.isEmpty(userProfileList)) {
            throw new ResourceNotFoundException("The user profiles couldn't be found");
        }

        userProfileList.forEach(userProfile -> {
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
