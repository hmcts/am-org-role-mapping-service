package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.AppointmentV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AuthorisationV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialOfficeHolder;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.RoleAssignmentAssertHelper.MultiRegion;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class DroolCivilJudicialRoleMappingTest extends DroolBase {

    // Salaried & Fee Paid Employment Judge's should have their 'judge' & 'fee-paid-judge' RAs expanded from 1-7
    static List<String> employmentJudgeMultiRegionList = List.of("1", "2", "3", "4", "5", "6", "7");
    // All salaried RAs with region 1 should also have RA with region 5 & vice versa
    static List<String> salariedMultiRegionList = List.of("1", "5");

    @ParameterizedTest
    @CsvSource({
        "CIVIL District Judge-Salaried,'judge,district-judge,hmcts-judiciary',1,true",
        "CIVIL District Judge-Salaried,'judge,district-judge,hmcts-judiciary',5,true",
        "CIVIL District Judge-Salaried,'judge,district-judge,hmcts-judiciary',2,false",

        "CIVIL Presiding Judge-Salaried,'judge,hmcts-judiciary',1,true",
        "CIVIL Presiding Judge-Salaried,'judge,hmcts-judiciary',5,true",
        "CIVIL Presiding Judge-Salaried,'judge,hmcts-judiciary',3,false",

        "CIVIL Resident Judge-Salaried,'judge,hmcts-judiciary',1,true",
        "CIVIL Resident Judge-Salaried,'judge,hmcts-judiciary',5,true",
        "CIVIL Resident Judge-Salaried,'judge,hmcts-judiciary',4,false",

        "CIVIL Tribunal Judge-Salaried,'judge,hmcts-judiciary',1,true",
        "CIVIL Tribunal Judge-Salaried,'judge,hmcts-judiciary',5,true",
        "CIVIL Tribunal Judge-Salaried,'judge,hmcts-judiciary',6,false",

        "CIVIL Employment Judge-Salaried,'judge,hmcts-judiciary',1,true",
        "CIVIL Employment Judge-Salaried,'judge,hmcts-judiciary',2,true",
        "CIVIL Employment Judge-Salaried,'judge,hmcts-judiciary',3,true",
        "CIVIL Employment Judge-Salaried,'judge,hmcts-judiciary',4,true",
        "CIVIL Employment Judge-Salaried,'judge,hmcts-judiciary',5,true",
        "CIVIL Employment Judge-Salaried,'judge,hmcts-judiciary',6,true",
        "CIVIL Employment Judge-Salaried,'judge,hmcts-judiciary',7,true",
        "CIVIL Employment Judge-Salaried,'judge,hmcts-judiciary',11,false", // Scotland

        "CIVIL Designated Civil Judge-Salaried,"
                + "'judge,leadership-judge,task-supervisor,hmcts-judiciary,case-allocator',1,true",
        "CIVIL Designated Civil Judge-Salaried,"
                + "'judge,leadership-judge,task-supervisor,hmcts-judiciary,case-allocator',5,true",
        "CIVIL Designated Civil Judge-Salaried,"
                + "'judge,leadership-judge,task-supervisor,hmcts-judiciary,case-allocator',7,false",

        "CIVIL Circuit Judge-Salaried,'judge,circuit-judge,hmcts-judiciary',1,true",
        "CIVIL Circuit Judge-Salaried,'judge,circuit-judge,hmcts-judiciary',5,true",
        "CIVIL Circuit Judge-Salaried,'judge,circuit-judge,hmcts-judiciary',11,false",

        "CIVIL Specialist Circuit Judge-Salaried,'judge,circuit-judge,hmcts-judiciary',1,true",
        "CIVIL Specialist Circuit Judge-Salaried,'judge,circuit-judge,hmcts-judiciary',5,true",
        "CIVIL Specialist Circuit Judge-Salaried,'judge,circuit-judge,hmcts-judiciary',11,false",

        "CIVIL Senior Circuit Judge-Salaried,'judge,circuit-judge,hmcts-judiciary',1,true",
        "CIVIL Senior Circuit Judge-Salaried,'judge,circuit-judge,hmcts-judiciary',5,true",
        "CIVIL Senior Circuit Judge-Salaried,'judge,circuit-judge,hmcts-judiciary',11,false",

        "CIVIL High Court Judge-Salaried,'judge,circuit-judge,hmcts-judiciary',1,true",
        "CIVIL High Court Judge-Salaried,'judge,circuit-judge,hmcts-judiciary',5,true",
        "CIVIL High Court Judge-Salaried,'judge,circuit-judge,hmcts-judiciary',11,false"
    })
    void shouldReturnSalariedRoles(String setOffice, String expectedRoles, String region, boolean expectMultiRegion) {

        judicialOfficeHolders.forEach(joh -> {
            joh.setOffice(setOffice);
            joh.setRegionId(region);
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("CIVIL", true));

        List<String> rolesThatRequireRegions = List.of(
                "judge", "leadership-judge", "task-supervisor", "case-allocator", "circuit-judge", "district-judge"
        );

        //assertion
        List<String> expectedRoleList = Arrays.stream(expectedRoles.split(",")).toList();
        MultiRegion.assertRoleAssignmentCount(
                roleAssignments,
                expectedRoleList,
                expectMultiRegion,
                rolesThatRequireRegions,
                setOffice.equals("CIVIL Employment Judge-Salaried")
                        ? employmentJudgeMultiRegionList : salariedMultiRegionList
        );

        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());

        Map<String, List<String>> roleNameToRegionsMap = MultiRegion.buildRoleNameToRegionsMap(rolesThatRequireRegions);

        roleAssignments.forEach(r -> {
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            assertEquals("JUDICIAL", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());

            // check region status and add to map
            MultiRegion.assertRegionStatusAndUpdateRoleToRegionMap(r, roleNameToRegionsMap);
        });

        // verify regions add to map
        MultiRegion.assertRoleNameToRegionsMapIsAsExpected(
                roleNameToRegionsMap,
                expectedRoleList,
                expectMultiRegion,
                setOffice.equals("CIVIL Employment Judge-Salaried")
                        ? employmentJudgeMultiRegionList : salariedMultiRegionList,
                region, // fallback if not multi-region scenario
                null // i.e. no bookings
        );
    }

    @ParameterizedTest
    @CsvSource({
        "CIVIL Deputy Circuit Judge-Fee-Paid,'judge,circuit-judge,fee-paid-judge,hmcts-judiciary',1,false",
        "CIVIL Deputy District Judge-Fee-Paid,'judge,deputy-district-judge,fee-paid-judge,hmcts-judiciary',1,false",
        "CIVIL Deputy District Judge - Sitting in Retirement-Fee-Paid,'judge,deputy-district-judge,fee-paid-judge,"
             + "hmcts-judiciary',1,false",
        "CIVIL Recorder-Fee-Paid,'judge,recorder,fee-paid-judge,hmcts-judiciary',1,false",
        "CIVIL District Judge (sitting in retirement)-Fee-Paid,'judge,deputy-district-judge,fee-paid-judge,"
             + "hmcts-judiciary',1,false",
        "CIVIL Circuit Judge (sitting in retirement)-Fee-Paid,'judge,fee-paid-judge,hmcts-judiciary,circuit-judge'"
            + ",1,false",
        "CIVIL Tribunal Judge-Fee-Paid,'judge,fee-paid-judge,hmcts-judiciary',1,false",

        "CIVIL Employment Judge-Fee-Paid,'judge,fee-paid-judge,hmcts-judiciary',1,true",
        "CIVIL Employment Judge-Fee-Paid,'judge,fee-paid-judge,hmcts-judiciary',2,true",
        "CIVIL Employment Judge-Fee-Paid,'judge,fee-paid-judge,hmcts-judiciary',3,true",
        "CIVIL Employment Judge-Fee-Paid,'judge,fee-paid-judge,hmcts-judiciary',4,true",
        "CIVIL Employment Judge-Fee-Paid,'judge,fee-paid-judge,hmcts-judiciary',5,true",
        "CIVIL Employment Judge-Fee-Paid,'judge,fee-paid-judge,hmcts-judiciary',6,true",
        "CIVIL Employment Judge-Fee-Paid,'judge,fee-paid-judge,hmcts-judiciary',7,true",
        "CIVIL Employment Judge-Fee-Paid,'judge,fee-paid-judge,hmcts-judiciary',11,false" // Scotland
    })
    void verifyFeePaidRolesWithBooking(String setOffice, String expectedRoles, String region,
                                       boolean expectMultiRegion) throws IOException {
        shouldReturnFeePaidRoles(setOffice, expectedRoles, region, expectMultiRegion, true);
    }

    @ParameterizedTest
    @CsvSource({
        "CIVIL Deputy Circuit Judge-Fee-Paid,'fee-paid-judge,hmcts-judiciary',1,false",
        "CIVIL Deputy District Judge-Fee-Paid,'fee-paid-judge,hmcts-judiciary',1,false",
        "CIVIL Deputy District Judge - Sitting in Retirement-Fee-Paid,'fee-paid-judge,hmcts-judiciary',1,false",
        "CIVIL Recorder-Fee-Paid,'fee-paid-judge,hmcts-judiciary',1,false",
        "CIVIL District Judge (sitting in retirement)-Fee-Paid,'fee-paid-judge,hmcts-judiciary',1,false",
        "CIVIL Circuit Judge (sitting in retirement)-Fee-Paid,'fee-paid-judge,hmcts-judiciary',1,false",
        "CIVIL Tribunal Judge-Fee-Paid,'fee-paid-judge,hmcts-judiciary',1,false",

        "CIVIL Employment Judge-Fee-Paid,'fee-paid-judge,hmcts-judiciary',1,true",
        "CIVIL Employment Judge-Fee-Paid,'fee-paid-judge,hmcts-judiciary',2,true",
        "CIVIL Employment Judge-Fee-Paid,'fee-paid-judge,hmcts-judiciary',3,true",
        "CIVIL Employment Judge-Fee-Paid,'fee-paid-judge,hmcts-judiciary',4,true",
        "CIVIL Employment Judge-Fee-Paid,'fee-paid-judge,hmcts-judiciary',5,true",
        "CIVIL Employment Judge-Fee-Paid,'fee-paid-judge,hmcts-judiciary',6,true",
        "CIVIL Employment Judge-Fee-Paid,'fee-paid-judge,hmcts-judiciary',7,true",
        "CIVIL Employment Judge-Fee-Paid,'fee-paid-judge,hmcts-judiciary',11,false" // Scotland
    })
    void verifyFeePaidRolesWithoutBooking(String setOffice, String expectedRoles, String region,
                                          boolean expectMultiRegion) throws IOException {
        shouldReturnFeePaidRoles(setOffice, expectedRoles, region, expectMultiRegion, false);
    }

    void shouldReturnFeePaidRoles(String setOffice, String expectedRoles, String region,
                                  boolean expectMultiRegion, boolean addBooking) throws IOException {

        judicialOfficeHolders.forEach(joh -> {
            joh.setOffice(setOffice);
            joh.setRegionId(region);
        });

        JudicialBooking judicialBooking = null;
        if (addBooking) {
            judicialBooking = TestDataBuilder.buildJudicialBooking();
            judicialBooking.setUserId(judicialOfficeHolders.stream().findFirst()
                    .orElse(JudicialOfficeHolder.builder().build()).getUserId());
            judicialBooking.setLocationId("location1");
            judicialBooking.setRegionId("1");
            judicialBookings = Set.of(judicialBooking);
        }

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("CIVIL", true));

        List<String> rolesThatRequireRegions = new ArrayList<>(List.of(
                "judge", "circuit-judge", "deputy-district-judge", "recorder"
        ));
        if (setOffice.equals("CIVIL Employment Judge-Fee-Paid")) {
            rolesThatRequireRegions.add("fee-paid-judge");
        }

        //assertion
        List<String> expectedRoleList = Arrays.stream(expectedRoles.split(",")).toList();
        MultiRegion.assertRoleAssignmentCount(
                roleAssignments,
                expectedRoleList,
                expectMultiRegion,
                rolesThatRequireRegions,
                employmentJudgeMultiRegionList
        );

        for (RoleAssignment roleAssignment : roleAssignments) {
            assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(), roleAssignment.getActorId());
        }

        Map<String, List<String>> roleNameToRegionsMap = MultiRegion.buildRoleNameToRegionsMap(rolesThatRequireRegions);

        roleAssignments.forEach(r -> {
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());

            // check region status and add to map
            MultiRegion.assertRegionStatusAndUpdateRoleToRegionMap(r, roleNameToRegionsMap);
        });

        if (addBooking) {
            RoleAssignment role = roleAssignments.stream()
                    .filter(r -> "judge".equals(r.getRoleName())).findFirst().get();
            assertEquals(judicialBooking.getLocationId(), role.getAttributes().get("baseLocation").asText());
            assertEquals(judicialBooking.getRegionId(), role.getAttributes().get("region").asText());
        }

        // verify regions add to map
        MultiRegion.assertRoleNameToRegionsMapIsAsExpected(
                roleNameToRegionsMap,
                expectedRoleList,
                expectMultiRegion,
                employmentJudgeMultiRegionList,
                region, // fallback if not multi-region scenario
                judicialBooking != null ? judicialBooking.getRegionId() : null
        );
    }

    @Test
    void civilJudicialScenario_2V2() {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<AuthorisationV2> authorisationList = TestDataBuilder.buildListOfAuthorisationsV2(1);

        List<AppointmentV2> appointmentList = new ArrayList<>();
        appointmentList.add(TestDataBuilder.buildAppointmentWithParamsV2(
                "487294","TRUE","Circuit Judge", "SPTW",
                LocalDate.now().minusYears(1L), null, null));
        appointmentList.add(TestDataBuilder.buildAppointmentWithParamsV2(
                null,"FALSE","Tribunal Judge","Fee Paid",
                LocalDate.now(), LocalDate.now().minusYears(1L), null));
        var roles = TestDataBuilder.buildListOfRolesV2(
                List.of("Pool of Judges"),
                LocalDate.now().minusYears(1L),
                null
        );

        Set<UserAccessProfile> userAccessProfiles = AssignmentRequestBuilder.convertProfileToJudicialAccessProfileV2(
                TestDataBuilder.buildJudicialProfileWithParamsV2(appointmentList, authorisationList, roles));

        judicialAccessProfiles = userAccessProfiles.stream()
                .map(obj -> (JudicialAccessProfile) obj).collect(Collectors.toSet());
        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("CIVIL", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5, roleAssignments.size());
        // 2x judge => region 1 + 5
        // 2x circuit-judge => region 1 + 5
        // 1x hmcts-judiciary
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName)
                        .distinct()
                        .collect(Collectors.toList()),
                containsInAnyOrder("judge","circuit-judge","hmcts-judiciary"));
        assertEquals(appointmentList.get(0).getEpimmsId(),
                roleAssignments.get(0).getAttributes().get("primaryLocation").asText());
        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> assertEquals(authorisationList.size(), r.getAuthorisations().size()));
        assertEquals(appointmentList.get(0).getStartDate().getDayOfYear(),
                roleAssignments.get(0).getBeginTime().getDayOfYear());
        assertNull(roleAssignments.get(0).getEndTime());
        roleAssignments.forEach(r -> assertEquals("Salaried", r.getAttributes().get("contractType").asText()));
    }

    @Test
    void civilJudicialScenario_4V2() {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<AuthorisationV2> authorisationList = TestDataBuilder.buildListOfAuthorisationsV2(2);

        List<AppointmentV2> appointmentList = new ArrayList<>();
        appointmentList.add(TestDataBuilder.buildAppointmentWithParamsV2(
                "487294","TRUE","Recorder", "Fee Paid",
                LocalDate.now().minusYears(1L),null,null));
        appointmentList.add(TestDataBuilder.buildAppointmentWithParamsV2(
                "487294","FALSE","Deputy District Judge- Fee-Paid","Fee Paid",
                LocalDate.now().minusYears(1L),null,null));
        appointmentList.add(TestDataBuilder.buildAppointmentWithParamsV2(
                null,"FALSE","Tribunal Judge","Fee Paid",
                LocalDate.now().minusYears(1L),null,null));
        appointmentList.add(TestDataBuilder.buildAppointmentWithParamsV2(
                null,"FALSE","Employment Judge","Fee Paid",
                LocalDate.now().minusYears(1L),null,null));

        Set<UserAccessProfile> userAccessProfiles = AssignmentRequestBuilder.convertProfileToJudicialAccessProfileV2(
                TestDataBuilder.buildJudicialProfileWithParamsV2(appointmentList, authorisationList));

        judicialAccessProfiles = userAccessProfiles.stream()
                .map(obj -> (JudicialAccessProfile) obj).collect(Collectors.toSet());

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("CIVIL", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(10, roleAssignments.size());
        // 1x hmcts-judiciary
        // 1x fee-paid-judge => Recorder Fee Paid (primaryLocation attr == "487294")
        // 1x fee-paid-judge => Tribunal Judge Fee Paid & Deputy District Judge- Fee-Paid (primaryLocation attr == "")
        // 7x fee-paid-judge => multi region Employment Judge Fee Paid
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName)
                        .distinct()
                        .collect(Collectors.toList()),
                containsInAnyOrder("fee-paid-judge","hmcts-judiciary"));

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> assertEquals(authorisationList.size(), r.getAuthorisations().size()));

        assertEquals(appointmentList.get(0).getStartDate().getDayOfYear(),
                roleAssignments.get(0).getBeginTime().getDayOfYear());
        assertNull(roleAssignments.get(0).getEndTime());

        roleAssignments.forEach(r -> {
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            assertEquals(appointmentList.get(0).getStartDate().getDayOfYear(), r.getBeginTime().getDayOfYear());
            assertNull(r.getEndTime());
        });
    }

    @Test
    void civilJudicialScenario_9V2() {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<AuthorisationV2> authorisationList = TestDataBuilder.buildListOfAuthorisationsV2(3);

        List<AppointmentV2> appointmentList = new ArrayList<>();
        appointmentList.add(TestDataBuilder.buildAppointmentWithParamsV2(
                "487294","FALSE","Recorder", "Fee Paid",
                LocalDate.now().minusYears(1L),null,null));
        appointmentList.add(TestDataBuilder.buildAppointmentWithParamsV2(
                "487294","FALSE","Deputy Upper Tribunal Judge","Fee Paid",
                LocalDate.now().minusYears(1L),null,null));
        appointmentList.add(TestDataBuilder.buildAppointmentWithParamsV2(
                null,"TRUE","Tribunal Judge","Fee Paid",
                LocalDate.now().minusYears(1L),null, "BFA1"));
        var roles = TestDataBuilder.buildListOfRolesV2(
                Arrays.asList("Diversity Role Models", "Pool of Judges", "Resident Immigration Judge"),
                LocalDate.now().minusYears(1L),
                null
        );

        Set<UserAccessProfile> userAccessProfiles = AssignmentRequestBuilder.convertProfileToJudicialAccessProfileV2(
                TestDataBuilder.buildJudicialProfileWithParamsV2(appointmentList, authorisationList, roles));

        judicialAccessProfiles = userAccessProfiles.stream()
                .map(obj -> (JudicialAccessProfile) obj).collect(Collectors.toSet());

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("CIVIL", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(3, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("fee-paid-judge","fee-paid-judge","hmcts-judiciary"));

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    //removes the auth in the past successfully so minus 1
                    assertEquals(authorisationList.size() - 1, r.getAuthorisations().size());
                    assertNotNull(r.getAttributes().get("primaryLocation"));
                });

        assertEquals(appointmentList.get(0).getStartDate().getDayOfYear(),
                roleAssignments.get(0).getBeginTime().getDayOfYear());
        assertNull(roleAssignments.get(0).getEndTime());

        roleAssignments.forEach(r -> {
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            assertEquals(appointmentList.get(0).getStartDate().getDayOfYear(), r.getBeginTime().getDayOfYear());
            assertNull(r.getEndTime());
        });
    }

    @Test
    void civilJudicialScenario_16V2() {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<AuthorisationV2> authorisationList = TestDataBuilder.buildListOfAuthorisationsV2(4);

        List<AppointmentV2> appointmentList = new ArrayList<>();
        appointmentList.add(TestDataBuilder.buildAppointmentWithParamsV2(
                "886493","TRUE","Senior Circuit Judge", "Salaried",
                LocalDate.now().minusYears(1L),null, null));

        List<RoleV2> roles = Collections.singletonList(RoleV2.builder().jurisdictionRoleName("Designated Civil Judge")
                .build());

        Set<UserAccessProfile> userAccessProfiles = AssignmentRequestBuilder.convertProfileToJudicialAccessProfileV2(
                TestDataBuilder.buildJudicialProfileWithParamsV2(appointmentList, authorisationList, roles));

        judicialAccessProfiles = userAccessProfiles.stream()
                .map(obj -> (JudicialAccessProfile) obj).collect(Collectors.toSet());

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("CIVIL", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(11, roleAssignments.size());
        // 2x judge => region 1 + 5
        // 2x leadership-judge => region 1 + 5
        // 2x circuit-judge => region 1 + 5
        // 2x task-supervisor => region 1 + 5
        // 2x case-allocator => region 1 + 5
        // 1x hmcts-judiciary
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName)
                        .distinct()
                        .collect(Collectors.toList()),
                containsInAnyOrder("judge", "leadership-judge", "circuit-judge", "task-supervisor",
                        "case-allocator", "hmcts-judiciary"));
        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals(authorisationList.size(), r.getAuthorisations().size());
                    assertEquals(appointmentList.get(0).getEpimmsId(),
                            r.getAttributes().get("primaryLocation").asText());
                });

        roleAssignments.forEach(r -> {
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            assertEquals(appointmentList.get(0).getStartDate().getDayOfYear(), r.getBeginTime().getDayOfYear());
            assertNull(r.getEndTime());
        });
    }

    @Test
    void civilJudicialScenario_DistrictJudgeInRetirementV2() {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<AuthorisationV2> authorisationList = TestDataBuilder.buildListOfAuthorisationsV2(2);

        List<AppointmentV2> appointmentList = new ArrayList<>();
        appointmentList.add(TestDataBuilder.buildAppointmentWithParamsV2(
                "487294","FALSE","Deputy District Judge- Sitting in Retirement","Fee Paid",
                LocalDate.now().minusYears(1L),null,null));

        Set<UserAccessProfile> userAccessProfiles = AssignmentRequestBuilder.convertProfileToJudicialAccessProfileV2(
                TestDataBuilder.buildJudicialProfileWithParamsV2(appointmentList, authorisationList));

        judicialAccessProfiles = userAccessProfiles.stream()
                .map(obj -> (JudicialAccessProfile) obj).collect(Collectors.toSet());


        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("CIVIL", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(
                        "fee-paid-judge","hmcts-judiciary"));

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> assertEquals(authorisationList.size(), r.getAuthorisations().size()));

        assertEquals(appointmentList.get(0).getStartDate().getDayOfYear(),
                roleAssignments.get(0).getBeginTime().getDayOfYear());
        assertNull(roleAssignments.get(0).getEndTime());

        roleAssignments.forEach(r -> {
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            assertEquals(appointmentList.get(0).getStartDate().getDayOfYear(), r.getBeginTime().getDayOfYear());
            assertNull(r.getEndTime());
        });
    }
}

