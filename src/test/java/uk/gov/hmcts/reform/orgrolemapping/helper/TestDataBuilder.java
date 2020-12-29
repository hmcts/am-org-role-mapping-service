package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Setter
public class TestDataBuilder {

    private static String id_1 = "7c12a4bc-450e-4290-8063-b387a5d5e0b7";
    private static String id_2 = "21334a2b-79ce-44eb-9168-2d49a744be9c";

    private static final String PROCESS_ID = "staff-organisational-role-mapping";
    private static final String ROLE_NAME_STCW = "senior-tribunal-caseworker";
    private static final String ROLE_NAME_TCW = "tribunal-caseworker";

    private TestDataBuilder() {
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
        return UserRequest.builder().userIds(users).build();
    }

    public static UserAccessProfile buildUserAccessProfile1(boolean deleteFlag) {
        return UserAccessProfile.builder().id(id_1).deleteFlag(deleteFlag).areaOfWorkId("London")
                .primaryLocationId("123456").primaryLocationName("south-east").roleId("1")
                .serviceCode("BFA1").roleName(ROLE_NAME_STCW).build();
    }

    public static UserAccessProfile buildUserAccessProfile2(boolean deleteFlag) {
        return UserAccessProfile.builder().id(id_2).deleteFlag(deleteFlag).areaOfWorkId("London")
                .primaryLocationId("123457").primaryLocationName("south-east").roleId("2")
                .serviceCode("BFA2").roleName(ROLE_NAME_TCW).build();
    }

    public static Set<UserAccessProfile> buildUserAccessProfileSet(boolean deleteFlag1, boolean deleteFlag2) {
        Set<UserAccessProfile> userAccessProfileSet = new HashSet<>();
        userAccessProfileSet.add(buildUserAccessProfile1(deleteFlag1));
        userAccessProfileSet.add(buildUserAccessProfile2(deleteFlag2));
        return userAccessProfileSet;
    }

    public static Map<String, Set<UserAccessProfile>> buildUserAccessProfileMap(boolean deleteFlag1,
                                                                                boolean deleteFlag2) {

        HashMap<String, Set<UserAccessProfile>> userAccessProfiles = new HashMap<>();
        userAccessProfiles.put(id_1, buildUserAccessProfileSet(deleteFlag1, deleteFlag2));
        userAccessProfiles.put(id_2, buildUserAccessProfileSet(deleteFlag1, deleteFlag2));
        return userAccessProfiles;
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

    public static List<UserProfile.WorkArea> buildListOfWorkAreas(boolean enableWorkAreaList,
                                                                  String workArea1,
                                                                  String workArea2) {
        List<UserProfile.WorkArea> workAreaList = new ArrayList<>();
        if (enableWorkAreaList) {
            workAreaList.add(buildWorkArea(workArea1, "BFA1"));
            workAreaList.add(buildWorkArea(workArea2, "BFA2"));
        }
        return workAreaList;
    }

    public static UserProfile.Role buildRole(String id, boolean primaryRole, String roleName) {
        return UserProfile.Role.builder().roleId(id).primary(primaryRole)
                .roleName(roleName)
                .createdTime(LocalDateTime.now()).lastUpdateTime(LocalDateTime.now().minusDays(1L))
                .build();
    }

    public static List<UserProfile.Role> buildListOfRoles(boolean multiRole, String roleId1,
                                                          String roleId2, String roleName1, String roleName2) {
        List<UserProfile.Role> roles = new ArrayList<>();
        roles.add(buildRole(roleId1,true, roleName1));
        if (multiRole) {
            roles.add(buildRole(roleId2, false, roleName2));
        }
        return roles;
    }

    public static UserProfile buildUserProfile(String id,
                                               boolean multiRole,
                                               String roleId1,
                                               String roleId2,
                                               String roleName1,
                                               String roleName2,
                                               boolean enableLocationList,
                                               boolean primaryLocation1,
                                               boolean primaryLocation2,
                                               boolean enableWorkAreaList,
                                               String workArea1,
                                               String workArea2,
                                               boolean deleteFlag) {
        return UserProfile.builder()
                .id(id)
                .firstName("James").lastName("Bond").emailId("007@MI6.gov")
                .baseLocation(buildListOfBaseLocations(enableLocationList, primaryLocation1, primaryLocation2))
                .workAreas(buildListOfWorkAreas(enableWorkAreaList, workArea1, workArea2))
                .createdTime(LocalDateTime.now())
                .lastUpdateTime(LocalDateTime.now().minusDays(1L))
                .region("London").regionId(1234L)
                .userType("Secret Agent")
                .userTypeId("007")
                .deleteFlag(deleteFlag)
                .roles(buildListOfRoles(multiRole, roleId1, roleId2, roleName1, roleName2))
                .build();
    }

    public static List<UserProfile> buildListOfUserProfiles(boolean multiProfiles,
                                                            boolean multiRole,
                                                            String roleId1,
                                                            String roleId2,
                                                            String roleName1,
                                                            String roleName2,
                                                            boolean enableLocationList,
                                                            boolean primaryLocation1,
                                                            boolean primaryLocation2,
                                                            boolean enableWorkAreaList,
                                                            String workArea1,
                                                            String workArea2,
                                                            boolean deleteFlag) {
        List<UserProfile> userProfiles = new ArrayList<>();
        userProfiles.add(buildUserProfile(id_1, multiRole, roleId1, roleId2, roleName1, roleName2,
                enableLocationList, primaryLocation1, primaryLocation2,
                enableWorkAreaList, workArea1, workArea2, deleteFlag));
        if (multiProfiles) {
            userProfiles.add(buildUserProfile(id_2, multiRole, roleId1, roleId2, roleName1, roleName2,
                    enableLocationList, primaryLocation1, primaryLocation2,
                    enableWorkAreaList, workArea1, workArea2, deleteFlag));
        }
        return userProfiles;
    }

    public static JsonNode buildAttributesFromFile() {
        try (InputStream inputStream =
                     AssignmentRequestBuilder.class.getClassLoader().getResourceAsStream("attributes.json")) {
            assert inputStream != null;
            JsonNode result = new ObjectMapper().readValue(inputStream, new TypeReference<>() {
            });
            inputStream.close();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}