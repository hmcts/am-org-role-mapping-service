package uk.gov.hmcts.reform.orgrolemapping.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

class UserAccessProfileBuilderTest {

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
        List<UserProfile> userProfiles =
                UserAccessProfileBuilder.buildUserProfile(
                        TestDataBuilder.buildUserRequest(), "userProfileSample.json");
        assertNotNull(userProfiles);
        assertEquals(2, userProfiles.size());
        assertEquals(id_1, userProfiles.get(0).getId());
        assertEquals(id_2, userProfiles.get(1).getId());
    }

    @Test
    void buildUserProfile_BadRequestException() {
        UserRequest userProfiles = TestDataBuilder.buildBadUserRequest();
        assertThrows(BadRequestException.class,
                () -> UserAccessProfileBuilder.buildUserProfile(userProfiles, ""));
    }
}