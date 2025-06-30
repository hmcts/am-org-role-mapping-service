package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUserData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUserAndOrganisation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersOrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;

import java.io.File;

public class ProfessionalUserBuilder {

    public static ProfessionalUserData fromProfessionalUserAndOrganisationInfo(ProfessionalUser user,
                                                                               UsersOrganisationInfo organisationInfo) {
        ProfessionalUserData userData = new ProfessionalUserData();
        userData.setUserId(user.getUserIdentifier());
        userData.setUserLastUpdated(user.getLastUpdated());
        userData.setDeleted(user.getDeleted());
        userData.setAccessTypes(JacksonUtils.convertObjectToString(user.getUserAccessTypes()));
        userData.setOrganisationId(organisationInfo.getOrganisationIdentifier());
        userData.setOrganisationStatus(organisationInfo.getStatus());
        userData.setOrganisationProfileIds(String.join(",", organisationInfo.getOrganisationProfileIds()));

        return userData;
    }

    public static RefreshUserAndOrganisation getSerializedRefreshUser(RefreshUser user) {
        String errorMessage = "0";
        try {
            RefreshUserAndOrganisation userData = new RefreshUserAndOrganisation();
            errorMessage = "1";
            userData.setUserIdentifier(user.getUserIdentifier());
            errorMessage = "2";
            userData.setUserLastUpdated(user.getLastUpdated());
            errorMessage = "3";
            userData.setUserAccessTypes(
                JacksonUtils.convertObjectToString(user.getUserAccessTypes()));
            errorMessage = "4";
            userData.setOrganisationIdentifier(
                user.getOrganisationInfo().getOrganisationIdentifier());
            errorMessage = "5";
            userData.setOrganisationStatus(user.getOrganisationInfo().getStatus());
            errorMessage = "6 " + user.getOrganisationInfo().getOrganisationProfileIds();
            userData.setOrganisationProfileIds(
                String.join(",", user.getOrganisationInfo().getOrganisationProfileIds()));
            errorMessage = "7";

            return userData;
        } catch (Exception e) {
            throw new ServiceException(errorMessage, e);
        }
    }

    public static UsersByOrganisationResponse buildUsersByOrganisationResponse(String resource) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(
                    new File("src/main/resources/" + resource),
                    UsersByOrganisationResponse.class);
        } catch (Exception e) {
            throw new BadRequestException("Invalid sample json file or missing.");
        }
    }

}
