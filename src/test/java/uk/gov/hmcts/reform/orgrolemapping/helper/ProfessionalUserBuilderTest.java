package uk.gov.hmcts.reform.orgrolemapping.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUserData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersOrganisationInfo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalUserServiceTest.buildProfessionalUser;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalUserServiceTest.buildUsersOrganisationInfo;
import static uk.gov.hmcts.reform.orgrolemapping.helper.ProfessionalUserBuilder.fromProfessionalUserAndOrganisationInfo;

@ExtendWith(MockitoExtension.class)
class ProfessionalUserBuilderTest {

    @Test
    void fromProfessionalUserAndOrganisationInfoTest() {
        ProfessionalUser user = buildProfessionalUser(1);
        UsersOrganisationInfo organisationInfo = buildUsersOrganisationInfo(123, List.of(user));

        ProfessionalUserData professionalUserData = fromProfessionalUserAndOrganisationInfo(user, organisationInfo);
        assertNotNull(professionalUserData.getUserLastUpdated());
        assertNotNull(professionalUserData.getDeleted());
        assertNotNull(professionalUserData.getAccessTypes());
        assertEquals("123", professionalUserData.getOrganisationId());
        assertEquals("ACTIVE", professionalUserData.getOrganisationStatus());
        assertEquals("SOLICITOR_PROFILE", professionalUserData.getOrganisationProfileIds());
    }

}
