package uk.gov.hmcts.reform.orgrolemapping.helper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AppointmentV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUserResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBookingResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfileV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUserData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUserAndOrganisation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessTypes;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersOrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class IntTestDataBuilder {

    private static final String ID_1 = "7c12a4bc-450e-4290-8063-b387a5d5e0b7";
    private static final String ID_2 = "21334a2b-79ce-44eb-9168-2d49a744be9c";
    private static final String ROLE_NAME_TCW = "tribunal-caseworker";
    private static final String LONDON = "London";

    public static final String SOLICITOR_PROFILE = "SOLICITOR_PROFILE";

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

    public static String[] buildUserIdList(int size) {
        String[] ids = new String[size];
        for (int i = 0; i < size; i++) {
            ids[i] = generateUniqueId();
        }
        return ids;
    }

    public static UserRequest buildUserRequest() {
        List<String> users = List.of(ID_1, ID_2);
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
                .region(LONDON).regionId(1234L)
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
        caseWorkerProfiles.add(buildUserProfile(ID_1, multiRole, roleId1, roleId2, roleName1, roleName2,
                enableLocationList, primaryLocation1, primaryLocation2,
                enableWorkAreaList, workArea1, workArea2, suspended));
        if (multiProfiles) {
            caseWorkerProfiles.add(buildUserProfile(ID_2, multiRole, roleId1, roleId2, roleName1, roleName2,
                    enableLocationList, primaryLocation1, primaryLocation2,
                    enableWorkAreaList, workArea1, workArea2, suspended));
        }
        return caseWorkerProfiles;
    }

    public static CaseWorkerAccessProfile buildUserAccessProfile(boolean suspended) {
        return CaseWorkerAccessProfile.builder().id(ID_1).suspended(suspended).areaOfWorkId(LONDON)
                .primaryLocationId("LDN").primaryLocationName(LONDON).roleId(RoleType.ORGANISATION.toString())
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
        userAccessProfiles.put(ID_1, buildUserAccessProfileSet(suspended1, suspended2));
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
        CaseWorkerProfile profile = buildUserProfile(ID_1, multiRole, roleId1, roleId2, roleName1, roleName2,
                enableLocationList, primaryLocation1, primaryLocation2,
                enableWorkAreaList, workArea1, workArea2, suspended);
        return List.of(CaseWorkerProfilesResponse.builder().serviceName(service).userProfile(profile).build());
    }

    public static ResponseEntity<List<JudicialProfileV2>> buildJudicialProfilesResponseV2(String... userIds) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("total_records", "" + userIds.length);
        List<JudicialProfileV2> bookings = new ArrayList<>();
        for (var userId:userIds) {
            bookings.add(JudicialProfileV2.builder().sidamId(userId)
                    .appointments(List.of(AppointmentV2.builder().appointment("Tribunal Judge")
                            .appointmentType("Fee Paid").build())).build());
        }
        return new ResponseEntity<>(bookings, headers, HttpStatus.OK);
    }

    public static ResponseEntity<JudicialBookingResponse> buildJudicialBookingsResponse(String... userIds) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("total_records", "" + userIds.length);
        List<JudicialBooking> bookings = new ArrayList<>();
        for (var userId:userIds) {
            bookings.add(JudicialBooking.builder().beginTime(ZonedDateTime.now())
                    .endTime(ZonedDateTime.now().plusDays(5)).userId(userId)
                    .locationId("location").regionId("region").build());
        }
        return new ResponseEntity<>(new JudicialBookingResponse(bookings), headers, HttpStatus.OK);
    }

    public static OrganisationInfo buildOrganisationInfo(int i) {
        return OrganisationInfo.builder()
                .organisationIdentifier("" + i)
                .status("ACTIVE")
                .organisationLastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE"))
                .build();
    }

    public static OrganisationByProfileIdsResponse buildOrganisationByProfileIdsResponse(OrganisationInfo orgInfo,
                                                                                         String lastRecord,
                                                                                         boolean moreAvailable) {
        return OrganisationByProfileIdsResponse.builder()
                .organisationInfo(List.of(orgInfo))
                .lastRecordInPage(lastRecord)
                .moreAvailable(moreAvailable)
                .build();
    }

    public static UsersOrganisationInfo buildUsersOrganisationInfo(int i, ProfessionalUser user) {
        return UsersOrganisationInfo.builder()
                .organisationIdentifier("" + i)
                .status("ACTIVE")
                .organisationProfileIds(List.of(SOLICITOR_PROFILE))
                .users(List.of(user))
                .build();
    }

    public static ProfessionalUser buildProfessionalUser(int i) {
        return ProfessionalUser.builder()
                .userIdentifier("" + i)
                .firstName("fName " + i)
                .lastName("lName " + i)
                .email("user" + i + "@mail.com")
                .lastUpdated(LocalDateTime.now())
                .deleted(LocalDateTime.now())
                .userAccessTypes(Collections.emptyList())
                .build();
    }

    public static UsersByOrganisationResponse buildUsersByOrganisationResponse(UsersOrganisationInfo organisationInfo,
                                                                               String lastOrgInPage,
                                                                               String lastUserInPage,
                                                                               Boolean moreAvailable) {
        return UsersByOrganisationResponse.builder()
                .organisationInfo(List.of(organisationInfo))
                .lastOrgInPage(lastOrgInPage)
                .lastUserInPage(lastUserInPage)
                .moreAvailable(moreAvailable)
                .build();
    }

    public static ProfessionalUserData buildProfessionalUserData(int i) {
        return ProfessionalUserData.builder()
                .userId("" + i)
                .userLastUpdated(LocalDateTime.now())
                .deleted(LocalDateTime.now())
                .accessTypes("{}")
                .organisationId("org " + i)
                .organisationStatus("ACTIVE")
                .organisationProfileIds(SOLICITOR_PROFILE)
                .build();
    }

    public static RefreshUser refreshUser(int i) {
        return RefreshUser.builder()
                .userIdentifier("" + i)
                .lastUpdated(LocalDateTime.now())
                .userAccessTypes(List.of(userAccessTypes(1)))
                .organisationInfo(buildOrganisationInfo(1))
                .build();
    }

    public static UserAccessTypes userAccessTypes(int i) {
        return UserAccessTypes.builder()
                .jurisdictionId("" + i)
                .organisationProfileId("" + i)
                .accessTypeId("" + i)
                .enabled("true")
                .build();
    }

    public static RefreshUserAndOrganisation refreshUserAndOrganisationsList(int i) {
        return RefreshUserAndOrganisation.builder()
                .userIdentifier("" + i)
                .userLastUpdated(LocalDateTime.now())
                .userAccessTypes("" + i)
                .organisationIdentifier("" + i)
                .organisationStatus("ACTIVE")
                .organisationProfileIds("SOLICITOR_PROFILE")
                .build();
    }

    public static GetRefreshUserResponse buildRefreshUserResponse(RefreshUser user,
                                                                  String lastRecord,
                                                                  boolean moreAvailable) {
        return GetRefreshUserResponse.builder()
                .users(List.of(user))
                .lastRecordInPage(lastRecord)
                .moreAvailable(moreAvailable)
                .build();
    }

}
