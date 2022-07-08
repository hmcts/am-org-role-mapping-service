package uk.gov.hmcts.reform.orgrolemapping.helper;

import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
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
        List<String> users = List.of(id_1,id_2);
        return UserRequest.builder().userIds(users).build();
    }

    public static CaseWorkerProfile.BaseLocation buildBaseLocation(boolean primaryLocation) {
        return CaseWorkerProfile.BaseLocation.builder().primary(primaryLocation)
                .location("Aberdeen Tribunal Hearing Centre").locationId("219164")
                .createdTime(LocalDateTime.now()).lastUpdatedTime(LocalDateTime.now().minusDays(1L))
                .build();
    }

    public static List<CaseWorkerProfile.BaseLocation> buildListOfBaseLocations(boolean enableLocationList,
                                                                                boolean primaryLocation1,
                                                                                boolean primaryLocation2) {
        List<CaseWorkerProfile.BaseLocation> baseLocationList = new ArrayList<>();
        if (enableLocationList) {
            baseLocationList.add(buildBaseLocation(primaryLocation1));
            baseLocationList.add(buildBaseLocation(primaryLocation2));
        }
        return baseLocationList;
    }

    public static CaseWorkerProfile.WorkArea buildWorkArea(String area, String serviceCode) {
        return CaseWorkerProfile.WorkArea.builder().areaOfWork(area).serviceCode(serviceCode)
                .createdTime(LocalDateTime.now()).lastUpdatedTime(LocalDateTime.now().minusDays(1L))
                .build();
    }

    public static List<CaseWorkerProfile.WorkArea> buildListOfWorkAreas(boolean enableWorkAreaList,
                                                                        String workArea1,
                                                                        String workArea2) {
        List<CaseWorkerProfile.WorkArea> workAreaList = new ArrayList<>();
        if (enableWorkAreaList) {
            workAreaList.add(buildWorkArea(workArea1, "BFA1"));
            workAreaList.add(buildWorkArea(workArea2, "BFA2"));
        }
        return workAreaList;
    }

    public static CaseWorkerProfile.Role buildRole(String id, boolean primaryRole, String roleName) {
        return CaseWorkerProfile.Role.builder().roleId(id).primary(primaryRole)
                .roleName(roleName)
                .createdTime(LocalDateTime.now()).lastUpdatedTime(LocalDateTime.now().minusDays(1L))
                .build();
    }

    public static List<CaseWorkerProfile.Role> buildListOfRoles(boolean multiRole, String roleId1,
                                                                String roleId2, String roleName1, String roleName2) {
        List<CaseWorkerProfile.Role> roles = new ArrayList<>();
        roles.add(buildRole(roleId1,true, roleName1));
        if (multiRole) {
            roles.add(buildRole(roleId2, false, roleName2));
        }
        return roles;
    }

    public static CaseWorkerProfile buildUserProfile(String id, boolean multiRole,
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
                                                     boolean suspended) {
        return CaseWorkerProfile.builder()
                .id(id)
                .firstName("James").lastName("Bond").emailId("007@MI6.gov")
                .baseLocation(buildListOfBaseLocations(enableLocationList, primaryLocation1, primaryLocation2))
                .workArea(buildListOfWorkAreas(enableWorkAreaList, workArea1, workArea2))
                .createdTime(LocalDateTime.now())
                .lastUpdatedTime(LocalDateTime.now().minusDays(1L))
                .region("London").regionId(1234L)
                .userType("Secret Agent")
                .userTypeId("007")
                .suspended(suspended)
                .role(buildListOfRoles(multiRole, roleId1, roleId2, roleName1, roleName2))
                .build();
    }

    public static List<CaseWorkerProfile> buildListOfUserProfiles(boolean multiProfiles,
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
                                                                  boolean suspended) {
        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        caseWorkerProfiles.add(buildUserProfile(id_1, multiRole, roleId1, roleId2, roleName1, roleName2,
                enableLocationList, primaryLocation1, primaryLocation2,
                enableWorkAreaList, workArea1, workArea2, suspended));
        if (multiProfiles) {
            caseWorkerProfiles.add(buildUserProfile(id_2, multiRole, roleId1, roleId2, roleName1, roleName2,
                    enableLocationList, primaryLocation1, primaryLocation2,
                    enableWorkAreaList, workArea1, workArea2, suspended));
        }
        return caseWorkerProfiles;
    }

    public static CaseWorkerAccessProfile buildUserAccessProfile(boolean suspended) {
        return CaseWorkerAccessProfile.builder().id(id_1).suspended(suspended).areaOfWorkId("London")
                .primaryLocationId("LDN").primaryLocationName("London").roleId(RoleType.ORGANISATION.toString())
                .serviceCode("ServiceCode").roleName(ROLE_NAME_TCW).build();
    }

    public static Set<CaseWorkerAccessProfile> buildUserAccessProfileSet(boolean suspended1, boolean suspended2) {
        Set<CaseWorkerAccessProfile> caseWorkerAccessProfileSet = new HashSet<>();
        caseWorkerAccessProfileSet.add(buildUserAccessProfile(suspended1));
        caseWorkerAccessProfileSet.add(buildUserAccessProfile(suspended2));
        return caseWorkerAccessProfileSet;
    }

    public static Map<String, Set<CaseWorkerAccessProfile>> buildUserAccessProfileMap(boolean suspended1,
                                                                                      boolean suspended2) {

        HashMap<String, Set<CaseWorkerAccessProfile>> userAccessProfiles = new HashMap<>();
        userAccessProfiles.put(id_1, buildUserAccessProfileSet(suspended1, suspended2));
        return userAccessProfiles;
    }

    public static List<CaseWorkerProfilesResponse> buildListOfUserProfilesResponse(String service,
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
                                                                                   boolean suspended) {
        CaseWorkerProfile profile = buildUserProfile(id_1, multiRole, roleId1, roleId2, roleName1, roleName2,
                enableLocationList, primaryLocation1, primaryLocation2,
                enableWorkAreaList, workArea1, workArea2, suspended);
        List<CaseWorkerProfilesResponse> userProfiles =
                List.of(CaseWorkerProfilesResponse.builder().serviceName(service).userProfile(profile).build());

        return userProfiles;
    }
}
