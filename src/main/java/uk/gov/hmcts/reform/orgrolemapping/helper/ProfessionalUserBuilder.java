package uk.gov.hmcts.reform.orgrolemapping.helper;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUserData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersOrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;

public class ProfessionalUserBuilder {

    private ProfessionalUserBuilder() {
        // Hide Utility Class Constructor : Utility classes should not have a public or
        // default constructor (squid:S1118)
    }

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

    public static ProfessionalUserData fromProfessionalRefreshUser(RefreshUser user) {
        ProfessionalUserData userData = new ProfessionalUserData();
        userData.setUserId(user.getUserIdentifier());
        userData.setUserLastUpdated(user.getLastUpdated());
        userData.setDeleted(user.getDateTimeDeleted());
        userData.setAccessTypes(JacksonUtils.convertObjectToString(user.getUserAccessTypes()));
        userData.setOrganisationId(user.getOrganisationInfo().getOrganisationIdentifier());
        userData.setOrganisationStatus(user.getOrganisationInfo().getStatus());
        userData.setOrganisationProfileIds(String.join(",", user.getOrganisationInfo().getOrganisationProfileIds()));

        return userData;
    }

}
