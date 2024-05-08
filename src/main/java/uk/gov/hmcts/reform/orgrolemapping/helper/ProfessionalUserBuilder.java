package uk.gov.hmcts.reform.orgrolemapping.helper;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUserAndOrganisation;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;

public class ProfessionalUserBuilder {

    public static RefreshUserAndOrganisation getSerializedRefreshUser(RefreshUser user) {
        RefreshUserAndOrganisation userData = new RefreshUserAndOrganisation();
        userData.setUserIdentifier(user.getUserIdentifier());
        userData.setUserLastUpdated(user.getLastUpdated());
        userData.setUserAccessTypes(JacksonUtils.convertObjectToString(user.getUserAccessTypes()));
        userData.setOrganisationIdentifier(user.getOrganisationInfo().getOrganisationIdentifier());
        userData.setOrganisationStatus(user.getOrganisationInfo().getStatus());
        userData.setOrganisationProfileIds(String.join(",", user.getOrganisationInfo().getOrganisationProfileIds()));

        return userData;
    }
}