package uk.gov.hmcts.reform.orgrolemapping.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JRDUserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfileV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

class CaseWorkerAccessProfileBuilderTest {

    private static final String id_1 = "7c12a4bc-450e-4290-8063-b387a5d5e0b7";
    private static final String id_2 = "21334a2b-79ce-44eb-9168-2d49a744be9c";

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
                UserAccessProfileBuilder.buildJudicialProfile(TestDataBuilder.buildRefreshRoleRequest(),
                        "judicialProfileSample.json");
        assertNotNull(judicialProfiles);
        assertEquals(2, judicialProfiles.size());
        assertThat(judicialProfiles.stream().map(JudicialProfile::getSidamId).collect(Collectors.toList()),
                containsInAnyOrder(id_1, id_2));
    }

    @Test
    void buildJudicialProfilesTestV2() {
        List<JudicialProfileV2> judicialProfiles =
                UserAccessProfileBuilder.buildJudicialProfileV2(TestDataBuilder.buildRefreshRoleRequest(),
                        "judicialProfileSampleV2.json");
        assertNotNull(judicialProfiles);
        assertEquals(2, judicialProfiles.size());
        assertThat(judicialProfiles.stream().map(JudicialProfileV2::getSidamId).collect(Collectors.toList()),
                containsInAnyOrder(id_1, id_2));
    }

    @Test
    void buildJudicialProfiles_BadRequest() {
        JRDUserRequest userRequest = TestDataBuilder.buildRefreshRoleRequest();
        assertThrows(BadRequestException.class, () -> UserAccessProfileBuilder
                .buildJudicialProfile(userRequest, ""));
    }

    @Test
    void buildJudicialProfiles_BadRequestV2() {
        JRDUserRequest userRequest = TestDataBuilder.buildRefreshRoleRequest();
        assertThrows(BadRequestException.class, () -> UserAccessProfileBuilder
                .buildJudicialProfileV2(userRequest, ""));
    }
}