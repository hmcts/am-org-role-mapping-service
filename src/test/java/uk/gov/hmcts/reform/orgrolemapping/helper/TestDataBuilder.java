package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Setter;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Appointment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Authorisation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JRDUserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialOfficeHolder;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Request;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Status;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
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

    public static UserInfo buildUserInfo(String uuid) {
        List<String> list = new ArrayList<>();
        return UserInfo.builder().sub("sub").uid(uuid)
                .name("James").givenName("007").familyName("Bond").roles(list).build();
    }

    public static UserRequest buildUserRequest() {
        List<String> users = List.of(id_1,id_2);
        return UserRequest.builder().userIds(users).build();
    }

    public static UserRequest buildBadUserRequest() {
        List<String> users = List.of(id_1,id_2);
        return UserRequest.builder().userIds(users).build();
    }

    public static UserRequest buildUserRequestIndividual() {
        List<String> users = List.of(id_1);
        return UserRequest.builder().userIds(users).build();
    }

    public static UserRequest buildInvalidRequest() {
        List<String> users = List.of(id_1,id_2);
        return UserRequest.builder().userIds(users).build();
    }

    public static CaseWorkerAccessProfile buildUserAccessProfile(String roleId, String serviceCode, boolean suspended) {
        return CaseWorkerAccessProfile.builder().id(id_1).suspended(suspended).areaOfWorkId("London")
                .primaryLocationId("123456").primaryLocationName("south-east").roleId(roleId).regionId("7")
                .serviceCode(serviceCode).roleName(ROLE_NAME_STCW).build();
    }

    public static CaseWorkerAccessProfile buildUserAccessProfile(String roleId, boolean suspended) {
        return buildUserAccessProfile(roleId, "BFA1", suspended);
    }

    public static CaseWorkerAccessProfile buildUserAccessProfile1(boolean suspended) {
        return CaseWorkerAccessProfile.builder().id(id_1).suspended(suspended).areaOfWorkId("London")
                .primaryLocationId("123456").primaryLocationName("south-east").roleId("1").regionId("3")
                .serviceCode("BFA1").roleName(ROLE_NAME_STCW).build();
    }

    public static CaseWorkerAccessProfile buildUserAccessProfile3(String serviceCode, String roleId, String roleName) {
        return CaseWorkerAccessProfile.builder().id(id_1).suspended(false).areaOfWorkId("London")
                .primaryLocationId("123456").primaryLocationName("south-east").roleId(roleId)
                .serviceCode(serviceCode).roleName(roleName).build();
    }

    public static CaseWorkerAccessProfile buildUserAccessProfile2(boolean suspended) {
        return CaseWorkerAccessProfile.builder().id(id_2).suspended(suspended).areaOfWorkId("London")
                .primaryLocationId("123457").primaryLocationName("south-east").roleId("2").regionId("3")
                .serviceCode("BFA2").roleName(ROLE_NAME_TCW).build();
    }

    public static Set<CaseWorkerAccessProfile> buildUserAccessProfileSet(boolean suspended1, boolean suspended2) {
        Set<CaseWorkerAccessProfile> caseWorkerAccessProfileSet = new HashSet<>();
        caseWorkerAccessProfileSet.add(buildUserAccessProfile1(suspended1));
        caseWorkerAccessProfileSet.add(buildUserAccessProfile2(suspended2));
        return caseWorkerAccessProfileSet;
    }

    public static Map<String, Set<CaseWorkerAccessProfile>> buildUserAccessProfileMap(boolean suspended1,
                                                                                      boolean suspended2) {

        HashMap<String, Set<CaseWorkerAccessProfile>> userAccessProfiles = new HashMap<>();
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
        List<CaseWorkerProfile.BaseLocation> baseLocationList = Collections.emptyList();
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
        List<CaseWorkerProfile.WorkArea> workAreaList = Collections.emptyList();
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
        List<CaseWorkerProfile> caseWorkerProfiles = Collections.emptyList();
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

    public static CaseWorkerProfilesResponse buildUserProfilesResponse() {
        return  CaseWorkerProfilesResponse.builder()
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
    }


    public static AssignmentRequest buildAssignmentRequest(Status requestStatus, Status roleStatus,
                                                           Boolean replaceExisting) {
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

    public static Collection<RoleAssignment> buildRequestedRoleCollection(Status status) {
        Collection<RoleAssignment> requestedRoles = Collections.emptyList();
        requestedRoles.add(buildRoleAssignment(status));
        requestedRoles.add(buildRoleAssignment(status));
        return requestedRoles;
    }

    public static RoleAssignment buildRoleAssignment(Status status) {
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

    public static RoleAssignmentRequestResource buildRoleAssignmentRequestResource() {
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
        JudicialAccessProfile.JudicialAccessProfileBuilder builder = JudicialAccessProfile.builder();
        builder.userId(id_1);
        builder.roleId("84");
        builder.contractTypeId("5");
        builder.beginTime(ZonedDateTime.now(ZoneOffset.UTC).plusDays(1));
        builder.endTime(ZonedDateTime.now(ZoneOffset.UTC).plusMonths(1));
        builder.baseLocationId("1");
        builder.primaryLocationId("primary location");
        builder.appointment("2");
        builder.regionId("3");
        builder.ticketCodes(List.of("373"));
        builder.authorisations(Collections.singletonList(
                Authorisation.builder().serviceCodes(List.of("BFA1")).build()));
        return builder
                .build();
    }

    public static Set<JudicialAccessProfile> buildJudicialAccessProfileSet() {
        Set<JudicialAccessProfile> judicialAccessProfileSet = new HashSet<>();
        judicialAccessProfileSet.add(buildJudicialAccessProfile());

        return judicialAccessProfileSet;
    }

    public static JudicialOfficeHolder buildJudicialOfficeHolder() {
        return JudicialOfficeHolder.builder()
                .userId(id_2)
                .beginTime(ZonedDateTime.now(ZoneOffset.UTC).plusDays(1))
                .endTime(ZonedDateTime.now(ZoneOffset.UTC).plusMonths(1))
                .baseLocationId("1")
                .primaryLocation("2")
                .regionId("3")
                .build();
    }

    public static Set<JudicialOfficeHolder> buildJudicialOfficeHolderSet() {
        Set<JudicialOfficeHolder> judicialOfficeHolders = new HashSet<>();
        judicialOfficeHolders.add(buildJudicialOfficeHolder());

        return judicialOfficeHolders;
    }

    public static JRDUserRequest buildRefreshRoleRequest() {
        return JRDUserRequest.builder().sidamIds(Set.of(id_1, id_2)).build();
    }

    public static JudicialProfile buildJudicialProfile() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        return objectMapper.readValue(
                new File("src/main/resources/judicialProfileSample.json"),
                JudicialProfile.class);
    }

    public static JudicialBooking buildJudicialBooking() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.readValue(
                new File("src/main/resources/judicialBookingSample.json"),
                JudicialBooking.class);
    }

    public static JudicialAccessProfile buildJudicialAccessProfileWithParams(List<String> ticketCodes,
                                                                             String appointment,
                                                                             String appointmentType,
                                                                             String locationId,
                                                                             List<Authorisation> authorisations,
                                                                             ZonedDateTime beginTime,
                                                                             ZonedDateTime endTime,
                                                                             List<String> roles,
                                                                             String serviceCode) {
        return JudicialAccessProfile.builder()
                .userId(id_1)
                .roleId("1")
                .regionId("2")
                .contractTypeId("3")
                .appointment(appointment)
                .appointmentType(appointmentType)
                .baseLocationId(locationId)
                .primaryLocationId(locationId)
                .ticketCodes(ticketCodes)
                .authorisations(authorisations)
                .beginTime(beginTime)
                .endTime(endTime)
                .roles(roles)
                .serviceCode(serviceCode)
                .build();
    }

    public static JudicialProfile buildJudicialProfileWithParams(
            List<Appointment> appointments, List<Authorisation> authorisations) {
        return JudicialProfile.builder()
                .sidamId("111")
                .objectId("fa88df1d-4204-4039-8e2a-fa11d4c643ec")
                .knownAs("Penney")
                .surname("Azcarate")
                .fullName("Penney Azcarate")
                .postNominals("The Honourable")
                .emailId("EMP42867@ejudiciary.net")
                .appointments(appointments)
                .authorisations(authorisations)
                .build();
    }

    public static Appointment buildAppointmentWithParams(String epimms, String isPrinciple, String appointment,
                                                         String appointmentType, LocalDate startDate, LocalDate endDate,
                                                         List<String> roles, String serviceCode) {
        return Appointment.builder()
                .baseLocationId("827")
                .epimmsId(epimms)
                .courtName("Fairfax County Courthouse")
                .cftRegionID("NULL")
                .cftRegion("South East")
                .locationId("1")
                .location("South East")
                .isPrincipalAppointment(isPrinciple)
                .appointment(appointment)
                .appointmentType(appointmentType)
                .startDate(startDate)
                .endDate(endDate)
                .roles(roles)
                .serviceCode(serviceCode)
                .build();
    }



    public static Authorisation buildAuthorisationWithParams(String jurisdiction,
                                                             String ticketCode,
                                                             String ticketDescription,
                                                             List<String> serviceCodes,
                                                             LocalDateTime startDate,
                                                             LocalDateTime endDate) {
        return Authorisation.builder()
                .jurisdiction(jurisdiction)
                .ticketCode(ticketCode)
                .ticketDescription(ticketDescription)
                .startDate(startDate)
                .endDate(endDate)
                .serviceCodes(serviceCodes)
                .build();
    }

    public static List<Authorisation> buildListOfAuthorisations(int setNumber) {
        Authorisation auth = TestDataBuilder.buildAuthorisationWithParams("Authorisation Civil", "294",
                "Civil Authorisation", Collections.singletonList("AAA6"), null, null);

        Authorisation auth2 = TestDataBuilder.buildAuthorisationWithParams("Authorisation Family", "313",
                "Court of Protection", Collections.singletonList("ABA7"), null, null);

        Authorisation auth3 = TestDataBuilder.buildAuthorisationWithParams("Authorisation Tribunals", "374",
                "First Tier - Health, Education and Social Care", null, LocalDateTime.now().minusYears(20L), null);

        Authorisation auth4 = TestDataBuilder.buildAuthorisationWithParams("Authorisation Tribunals", "342",
                "Mental Health", Collections.singletonList("BCA2"),
                LocalDateTime.now().minusYears(15L), LocalDateTime.now().plusYears(1L));

        Authorisation auth5 = TestDataBuilder.buildAuthorisationWithParams("Authorisation Family", "315",
                "Private Law", Collections.singletonList("ABA5"),
                LocalDateTime.now().minusYears(9L), LocalDateTime.now().plusYears(14L));

        Authorisation auth6 = TestDataBuilder.buildAuthorisationWithParams("Authorisation Family", "316",
                "Public Law", Collections.singletonList("ABA3"),
                LocalDateTime.now().minusYears(9L), LocalDateTime.now().plusYears(14L));

        Authorisation auth7 = TestDataBuilder.buildAuthorisationWithParams("Authorisation Tribunals", "356",
                "Restricted Patients Panel", null,
                LocalDateTime.now().minusYears(7L), LocalDateTime.now().plusYears(1L));

        Authorisation auth8 = TestDataBuilder.buildAuthorisationWithParams("Authorisation Family", "317",
                "Section 9-1 Family", null,
                LocalDateTime.now().minusYears(3L), LocalDateTime.now().plusYears(3L));

        Authorisation auth9 = TestDataBuilder.buildAuthorisationWithParams("Authorisation Civil", "290",
                "Administrative Court", null,
                LocalDateTime.now().minusYears(3L), LocalDateTime.now().plusYears(3L));

        Authorisation auth10 = TestDataBuilder.buildAuthorisationWithParams("Authorisation Civil", "300",
                "Section 9(1) Chancery", null, null, null);

        Authorisation auth11 = TestDataBuilder.buildAuthorisationWithParams("Authorisation Family", "301",
                "Section 9(1) Queens Bench", null, null, null);

        Authorisation auth12 = TestDataBuilder.buildAuthorisationWithParams("Authorisation Tribunals", "372",
                "Upper - Immigration and Asylum", null, LocalDateTime.now().minusYears(10L),
                LocalDateTime.now().minusYears(2L));

        List<Authorisation> authorisationList;

        switch (setNumber) {
            case 1:
                authorisationList = List.of(auth, auth2, auth3, auth4, auth5, auth6, auth7, auth8);
                break;
            case 2:
                authorisationList = List.of(auth, auth3, auth4, auth5, auth6);
                break;
            case 3:
                authorisationList = List.of(auth, auth3, auth4, auth9,  auth12);
                break;
            case 4:
                authorisationList = List.of(auth, auth5, auth10, auth11);
                break;
            default:
                authorisationList = Collections.singletonList(auth);
        }

        return authorisationList;

    }

    public static class VarargsAggregator implements ArgumentsAggregator {
        @Override
        public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
                throws ArgumentsAggregationException {
            return accessor.toList().stream()
                    .skip(context.getIndex())
                    .map(String::valueOf)
                    .toArray(String[]::new);
        }
    }
}