package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Request;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Status;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    private static String id_3 = "invalid_id";

    private static final String PROCESS_ID = "staff-organisational-role-mapping";
    private static final String ROLE_NAME_STCW = "senior-tribunal-caseworker";
    private static final String ROLE_NAME_TCW = "tribunal-caseworker";
    private static final String ROLE_NAME_SJ = "salaried-judge";

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

    public static UserRequest buildBadUserRequest() {
        ArrayList<String> users = new ArrayList<>();
        users.add(id_1);
        users.add(id_3);
        return UserRequest.builder().userIds(users).build();
    }

    public static UserRequest buildUserRequestIndividual() {
        ArrayList<String> users = new ArrayList<>();
        users.add(id_1);
        return UserRequest.builder().userIds(users).build();
    }

    public static UserRequest buildInvalidRequest() {
        ArrayList<String> users = new ArrayList<>();
        users.add(id_1);
        users.add(id_3);
        return UserRequest.builder().userIds(users).build();
    }

    public static UserAccessProfile buildUserAccessProfile1(boolean suspended) {
        return UserAccessProfile.builder().id(id_1).suspended(suspended).areaOfWorkId("London")
                .primaryLocationId("123456").primaryLocationName("south-east").roleId("1")
                .serviceCode("BFA1").roleName(ROLE_NAME_STCW).build();
    }

    public static UserAccessProfile buildUserAccessProfile2(boolean suspended) {
        return UserAccessProfile.builder().id(id_2).suspended(suspended).areaOfWorkId("London")
                .primaryLocationId("123457").primaryLocationName("south-east").roleId("2")
                .serviceCode("BFA2").roleName(ROLE_NAME_TCW).build();
    }

    public static Set<UserAccessProfile> buildUserAccessProfileSet(boolean suspended1, boolean suspended2) {
        Set<UserAccessProfile> caseWorkerAccessProfileSet = new HashSet<>();
        caseWorkerAccessProfileSet.add(buildUserAccessProfile1(suspended1));
        caseWorkerAccessProfileSet.add(buildUserAccessProfile2(suspended2));
        return caseWorkerAccessProfileSet;
    }

    public static Map<String, Set<UserAccessProfile>> buildUserAccessProfileMap(boolean suspended1,
                                                                                boolean suspended2) {

        HashMap<String, Set<UserAccessProfile>> userAccessProfiles = new HashMap<>();
        userAccessProfiles.put(id_1, buildUserAccessProfileSet(suspended1, suspended2));
        userAccessProfiles.put(id_2, buildUserAccessProfileSet(suspended1, suspended2));
        return userAccessProfiles;
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

    public static CaseWorkerProfile buildUserProfile(String id,
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

    /*public static List<UserProfile> buildListOfUserAccessProfiles(boolean multiProfiles,
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
        List<UserProfile> caseWorkerProfiles = new ArrayList<>();
        caseWorkerProfiles.add(buildUserProfile(id_1, multiRole, roleId1, roleId2, roleName1, roleName2,
                enableLocationList, primaryLocation1, primaryLocation2,
                enableWorkAreaList, workArea1, workArea2, suspended));
        if (multiProfiles) {
            caseWorkerProfiles.add(buildUserProfile(id_2, multiRole, roleId1, roleId2, roleName1, roleName2,
                    enableLocationList, primaryLocation1, primaryLocation2,
                    enableWorkAreaList, workArea1, workArea2, suspended));
        }
        return caseWorkerProfiles;
    }*/

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

    /*public static UserProfilesResponse buildUserProfilesResponse() {
        return  UserProfilesResponse.builder()
                .serviceName("ccd_service_name")
                .userProfile(buildUserProfile(
                        "1",
                        true,
                        "1",
                        "1",
                        "roleName1",
                        "roleName2",
                        true,
                        true,
                        true,
                        true,
                        "workArea1",
                        "workArea2",
                        false))
                .build();
    }*/


    public static AssignmentRequest buildAssignmentRequest(Status requestStatus, Status roleStatus,
                                                           Boolean replaceExisting) throws IOException {
        return new AssignmentRequest(buildRequest(requestStatus, replaceExisting),
                buildRequestedRoleCollection(roleStatus));
    }

    public static Request buildRequest(Status status, Boolean replaceExisting) {
        return Request.builder()
                .id(UUID.fromString("ab4e8c21-27a0-4abd-aed8-810fdce22adb"))
                .authenticatedUserId("4772dc44-268f-4d0c-8f83-f0fb662aac84")
                .correlationId("38a90097-434e-47ee-8ea1-9ea2a267f51d")
                .assignerId("123e4567-e89b-42d3-a456-556642445678")
                .requestType(RequestType.CREATE)
                .reference("p2")
                .process(("p2"))
                .replaceExisting(replaceExisting)
                .status(status)
                .created(ZonedDateTime.now())
                .build();
    }

    public static Collection<RoleAssignment> buildRequestedRoleCollection(Status status) throws IOException {
        Collection<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(buildRoleAssignment(status));
        requestedRoles.add(buildRoleAssignment(status));
        return requestedRoles;
    }

    public static RoleAssignment buildRoleAssignment(Status status) throws IOException {
        ZonedDateTime timeStamp = ZonedDateTime.now(ZoneOffset.UTC);
        return RoleAssignment.builder()
                .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9f"))
                .actorId("21334a2b-79ce-44eb-9168-2d49a744be9c")
                .actorIdType(ActorIdType.IDAM)
                .roleType(RoleType.CASE)
                .roleName("judge")
                .classification(Classification.PUBLIC)
                .grantType(GrantType.STANDARD)
                .roleCategory(RoleCategory.JUDICIAL)
                .readOnly(true)
                .beginTime(timeStamp.plusDays(1))
                .endTime(timeStamp.plusMonths(1))
                .reference("reference")
                .process(("process"))
                .statusSequence(10)
                .status(status)
                .created(ZonedDateTime.now())
                .authorisations(Collections.emptyList())
                .build();
    }

    public static RoleAssignmentRequestResource buildRoleAssignmentRequestResource() throws IOException {
        return new RoleAssignmentRequestResource(TestDataBuilder
                .buildAssignmentRequest(Status.CREATE_REQUESTED, Status.APPROVED, true));
    }

    public static RefreshJobEntity buildRefreshJobEntity() {
        return new RefreshJobEntity().toBuilder()
                .jurisdiction("Jurisdiction")
                .roleCategory(RoleCategory.JUDICIAL.name())
                .jobId(7L)
                .created(ZonedDateTime.now())
                .linkedJobId(1L)
                .status(Status.CREATED.name())
                .userIds(new String[]{"1234"})
                .build();
    }

    public static Map<String, Set<JudicialAccessProfile>> buildJudicialAccessProfileMap() {

        HashMap<String, Set<JudicialAccessProfile>> userAccessProfiles = new HashMap<>();
        userAccessProfiles.put(id_1, buildJudicialAccessProfileSet());
        userAccessProfiles.put(id_2, buildJudicialAccessProfileSet());
        return userAccessProfiles;
    }

    public static JudicialAccessProfile buildJudicialAccessProfile() {
        return JudicialAccessProfile.builder().userId(id_1).roleId("8")
                .baseLocationId("south-east")
                .build();
    }

    public static Set<JudicialAccessProfile> buildJudicialAccessProfileSet() {
        Set<JudicialAccessProfile> judicialAccessProfileSet = new HashSet<>();
        judicialAccessProfileSet.add(buildJudicialAccessProfile());

        return judicialAccessProfileSet;
    }
}