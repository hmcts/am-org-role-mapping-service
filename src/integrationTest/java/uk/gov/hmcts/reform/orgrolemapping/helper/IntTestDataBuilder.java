package uk.gov.hmcts.reform.orgrolemapping.helper;

import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class IntTestDataBuilder {

    private static String id_1 = "7c12a4bc-450e-4290-8063-b387a5d5e0b7";
    private static String id_2 = "21334a2b-79ce-44eb-9168-2d49a744be9c";

    private static final String PROCESS_ID = "staff-organisational-role-mapping";
    private static final String ROLE_NAME_TCW = "tribunal-caseworker";

    private IntTestDataBuilder() {
    }

    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    public static UserInfo buildUserInfo(String uuid) {
        List<String> list = new ArrayList<>();
        return UserInfo.builder().sub("sub").uid(uuid)
                .name("James").givenName("007").familyName("Bond").roles(list).build();
    }

    public static UserRequest buildUserRequest() {
        ArrayList<String> users = new ArrayList<>();
        users.add(id_1);
        users.add(id_2);
        return UserRequest.builder().users(users).build();
    }

    public static UserProfile.BaseLocation buildBaseLocation(boolean primaryLocation) {
        return UserProfile.BaseLocation.builder().primary(primaryLocation)
                .location("Aberdeen Tribunal Hearing Centre").locationId("219164")
                .createdTime(LocalDateTime.now()).lastUpdateTime(LocalDateTime.now().minusDays(1L))
                .build();
    }

    public static List<UserProfile.BaseLocation> buildListOfBaseLocations(boolean enableLocationList,
                                                                          boolean primaryLocation1,
                                                                          boolean primaryLocation2) {
        List<UserProfile.BaseLocation> baseLocationList = new ArrayList<>();
        if (enableLocationList) {
            baseLocationList.add(buildBaseLocation(primaryLocation1));
            baseLocationList.add(buildBaseLocation(primaryLocation2));
        }
        return baseLocationList;
    }

    public static UserProfile.WorkArea buildWorkArea(String area, String serviceCode) {
        return UserProfile.WorkArea.builder().areaOfWork(area).serviceCode(serviceCode)
                .createdTime(LocalDateTime.now()).lastUpdateTime(LocalDateTime.now().minusDays(1L))
                .build();
    }

    public static List<UserProfile.WorkArea> buildListOfWorkAreas() {
        List<UserProfile.WorkArea> workAreaList = new ArrayList<>();
        workAreaList.add(buildWorkArea("1","BFA1"));
        workAreaList.add(buildWorkArea("2", "BFA2"));
        return workAreaList;
    }

    public static UserProfile.Role buildRole(String id, boolean primaryRole) {
        return UserProfile.Role.builder().roleId(id).primary(primaryRole)
                .roleName(ROLE_NAME_TCW)
                .createdTime(LocalDateTime.now()).lastUpdateTime(LocalDateTime.now().minusDays(1L))
                .build();
    }

    public static List<UserProfile.Role> buildListOfRoles(boolean multiRole) {
        List<UserProfile.Role> roles = new ArrayList<>();
        roles.add(buildRole("1",true));
        if (multiRole) {
            roles.add(buildRole("2", false));
        }
        return roles;
    }

    public static UserProfile buildUserProfile(String id, boolean multiRole,
                                               boolean enableLocationList,
                                               boolean primaryLocation1,
                                               boolean primaryLocation2,
                                               boolean deleteFlag) {
        return UserProfile.builder()
                .id(id)
                .firstName("James").lastName("Bond").emailId("007@MI6.gov")
                .baseLocation(buildListOfBaseLocations(enableLocationList, primaryLocation1, primaryLocation2))
                .workArea(buildListOfWorkAreas())
                .createdTime(LocalDateTime.now())
                .lastUpdateTime(LocalDateTime.now().minusDays(1L))
                .region("London").regionId(1234L)
                .userType("Secret Agent")
                .userTypeId("007")
                .deleteFlag(deleteFlag)
                .role(buildListOfRoles(multiRole))
                .build();
    }

    public static List<UserProfile> buildListOfUserProfiles(boolean multiProfiles,
                                                            boolean multiRole,
                                                            boolean enableLocationList,
                                                            boolean primaryLocation1,
                                                            boolean primaryLocation2,
                                                            boolean deleteFlag) {
        List<UserProfile> userProfiles = new ArrayList<>();
        userProfiles.add(buildUserProfile(id_1, multiRole, enableLocationList, primaryLocation1, primaryLocation2,
                deleteFlag));
        if (multiProfiles) {
            userProfiles.add(buildUserProfile(id_2, multiRole, enableLocationList, primaryLocation1, primaryLocation2,
                    deleteFlag));
        }
        return userProfiles;
    }

    public static UserAccessProfile buildUserAccessProfile(boolean deleteFlag) {
        return UserAccessProfile.builder().id(id_1).deleteFlag(deleteFlag).areaOfWorkId("London")
                .primaryLocationId("LDN").primaryLocationName("London").roleId(RoleType.ORGANISATION.toString())
                .serviceCode("ServiceCode").roleName(ROLE_NAME_TCW).build();
    }

    public static Set<UserAccessProfile> buildUserAccessProfileSet(boolean deleteFlag1, boolean deleteFlag2) {
        Set<UserAccessProfile> userAccessProfileSet = new HashSet<>();
        userAccessProfileSet.add(buildUserAccessProfile(deleteFlag1));
        userAccessProfileSet.add(buildUserAccessProfile(deleteFlag2));
        return userAccessProfileSet;
    }

    public static Map<String, Set<UserAccessProfile>> buildUserAccessProfileMap(boolean deleteFlag1,
                                                                                boolean deleteFlag2) {

        HashMap<String, Set<UserAccessProfile>> userAccessProfiles = new HashMap<>();
        userAccessProfiles.put(id_1, buildUserAccessProfileSet(deleteFlag1, deleteFlag2));
        return userAccessProfiles;
    }

}
