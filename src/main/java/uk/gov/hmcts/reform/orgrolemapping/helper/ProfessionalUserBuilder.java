package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUserData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersOrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;

import java.io.File;

public class ProfessionalUserBuilder {

    public static ProfessionalUserData fromProfessionalUserAndOrganisationInfo(ProfessionalUser user,
                                                                               UsersOrganisationInfo organisationInfo) {
        ProfessionalUserData userData = new ProfessionalUserData();
        userData.setUserId(user.getUserIdentifier());
        userData.setLastUpdated(user.getLastUpdated());
        userData.setDeleted(user.getDeleted());
        userData.setAccessTypes(JacksonUtils.convertObjectToString(user.getUserAccessTypes()));
        userData.setOrganisationId(organisationInfo.getOrganisationIdentifier());
        userData.setOrganisationStatus(organisationInfo.getStatus());
        userData.setOrganisationProfileIds(String.join(",", organisationInfo.getOrganisationProfileIds()));

        return userData;
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
