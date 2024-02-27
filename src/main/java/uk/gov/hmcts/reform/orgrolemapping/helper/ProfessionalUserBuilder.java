package uk.gov.hmcts.reform.orgrolemapping.helper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUserResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUserAndOrganisation;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;

import java.io.File;

public class ProfessionalUserBuilder {

    public static RefreshUserAndOrganisation getSerializedRefreshUser (RefreshUser user) {
        RefreshUserAndOrganisation userData = new RefreshUserAndOrganisation();
        userData.setUserIdentifier(user.getUserIdentifier());
        userData.setLastUpdated(user.getLastUpdated());
        userData.setUserIdentifier(user.getUserIdentifier());
        userData.setUserAccessTypes(JacksonUtils.convertObjectToString(user.getUserAccessTypes()));
        userData.setOrganisationStatus(user.getOrganisationInfo().getStatus());
        userData.setOrganisationProfileIds(String.join(",", user.getOrganisationInfo().getOrganisationProfileIds()));

        return userData;
    }

    public static RefreshUserAndOrganisation buildUsersWithSerializedAccessTypes(String resource) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(
                    new File("src/main/resources/" + resource),
                    RefreshUserAndOrganisation.class);
        } catch (Exception e) {
            throw new BadRequestException("Invalid sample json file or missing.");
        }
    }
}