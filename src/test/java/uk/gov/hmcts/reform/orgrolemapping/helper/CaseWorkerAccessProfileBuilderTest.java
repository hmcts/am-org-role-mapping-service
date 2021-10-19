package uk.gov.hmcts.reform.orgrolemapping.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

class CaseWorkerAccessProfileBuilderTest {

    private static String id_1 = "7c12a4bc-450e-4290-8063-b387a5d5e0b7";
    private static String id_2 = "21334a2b-79ce-44eb-9168-2d49a744be9c";

    @Test
    void buildUserAccessProfiles() {
        assertNotNull(UserAccessProfileBuilder.buildUserAccessProfiles());
    }

    @Test
    void buildUserAccessProfileForRoleId1() {
        assertNotNull(UserAccessProfileBuilder.buildUserAccessProfileForRoleId1());
    }

    @Test
    void buildUserAccessProfileForRoleId2() {
        assertNotNull(UserAccessProfileBuilder.buildUserAccessProfileForRoleId2());
    }

    @Test
    void buildUserAccessProfileForRoleId3() {
        assertNotNull(UserAccessProfileBuilder.buildUserAccessProfileForRoleId3());
    }

    @Test
    void buildUserAccessProfileForRoleId4() {
        assertNotNull(UserAccessProfileBuilder.buildUserAccessProfileForRoleId4());
    }

    @Test
    void buildUserProfiles() {
        List<CaseWorkerProfile> caseWorkerProfiles =
                UserAccessProfileBuilder.buildUserProfile(TestDataBuilder.buildUserRequest(),
                        "userProfileSample.json");
        assertNotNull(caseWorkerProfiles);
        assertEquals(2, caseWorkerProfiles.size());
        assertEquals(id_1, caseWorkerProfiles.get(0).getId());
        assertEquals(id_2, caseWorkerProfiles.get(1).getId());
    }

    @Test
    void buildUserProfiles_BadRequest() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        assertThrows(BadRequestException.class, () -> UserAccessProfileBuilder
                .buildUserProfile(userRequest, ""));
    }

    @Test
    void buildJudicialProfilesTest() {
        List<JudicialProfile> judicialProfiles =
                UserAccessProfileBuilder.buildJudicialProfile(TestDataBuilder.buildUserRequest(),
                        "judicialProfileSample.json");
        assertNotNull(judicialProfiles);
        assertEquals(2, judicialProfiles.size());
        assertEquals(id_1, judicialProfiles.get(0).getIdamId());
        assertEquals(id_2, judicialProfiles.get(1).getIdamId());
    }

    @Test
    void buildJudicialProfiles_BadRequest() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        assertThrows(BadRequestException.class, () -> UserAccessProfileBuilder
                .buildJudicialProfile(userRequest, ""));
    }
}