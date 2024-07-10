package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
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
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.VarargsAggregator;

@RunWith(MockitoJUnitRunner.class)
class DroolCivilJudicialRoleMappingTest extends DroolBase {

    // NB: multi-regions are: all English and Welsh regions
    static List<String> multiRegionJudicialList = List.of("1", "2", "3", "4", "5", "6", "7");

    @ParameterizedTest
    @CsvSource({
        "CIVIL District Judge-Salaried,'judge,hmcts-judiciary',1,false",
        "CIVIL Presiding Judge-Salaried,'judge,hmcts-judiciary',1,false",
        "CIVIL Resident Judge-Salaried,'judge,hmcts-judiciary',1,false",
        "CIVIL Tribunal Judge-Salaried,'judge,hmcts-judiciary',1,false",

        "CIVIL Employment Judge-Salaried,'judge,hmcts-judiciary',1,true",
        "CIVIL Employment Judge-Salaried,'judge,hmcts-judiciary',2,true",
        "CIVIL Employment Judge-Salaried,'judge,hmcts-judiciary',3,true",
        "CIVIL Employment Judge-Salaried,'judge,hmcts-judiciary',4,true",
        "CIVIL Employment Judge-Salaried,'judge,hmcts-judiciary',5,true",
        "CIVIL Employment Judge-Salaried,'judge,hmcts-judiciary',6,true",
        "CIVIL Employment Judge-Salaried,'judge,hmcts-judiciary',7,true",
        "CIVIL Employment Judge-Salaried,'judge,hmcts-judiciary',11,false", // Scotland

        "CIVIL Designated Civil Judge-Salaried,"
                + "'judge,leadership-judge,task-supervisor,hmcts-judiciary,case-allocator',1,false",
        "CIVIL Circuit Judge-Salaried,'judge,circuit-judge,hmcts-judiciary',1,false",
        "CIVIL Specialist Circuit Judge-Salaried,'judge,circuit-judge,hmcts-judiciary',1,false",
        "CIVIL Senior Circuit Judge-Salaried,'judge,circuit-judge,hmcts-judiciary',1,false",
        "CIVIL High Court Judge-Salaried,'judge,circuit-judge,hmcts-judiciary',1,false"
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
                "judge", "leadership-judge", "task-supervisor", "case-allocator", "circuit-judge"
        );

        //assertion
        List<String> expectedRoleList = Arrays.stream(expectedRoles.split(",")).toList();
        MultiRegion.assertRoleAssignmentCount(
                roleAssignments,
                expectedRoleList,
                expectMultiRegion,
                rolesThatRequireRegions,
                multiRegionJudicialList
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
                multiRegionJudicialList,
                region, // fallback if not multi-region scenario
                null // i.e. no bookings
        );
    }

    @ParameterizedTest
    @CsvSource({
        "CIVIL Deputy Circuit Judge-Fee-Paid,'judge,circuit-judge,fee-paid-judge,hmcts-judiciary',1,false",
        "CIVIL Deputy District Judge-Fee-Paid,'judge,fee-paid-judge,hmcts-judiciary',1,false",
        "CIVIL Deputy District Judge - Sitting in Retirement-Fee-Paid,'judge,fee-paid-judge,hmcts-judiciary',1,false",
        "CIVIL Recorder-Fee-Paid,'judge,fee-paid-judge,hmcts-judiciary',1,false",
        "CIVIL District Judge (sitting in retirement)-Fee-Paid,'judge,fee-paid-judge,hmcts-judiciary',1,false",
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
    void shouldReturnFeePaidRoles(String setOffice, String expectedRoles, String region,
                                  boolean expectMultiRegion) throws IOException {

        judicialOfficeHolders.forEach(joh -> {
            joh.setOffice(setOffice);
            joh.setRegionId(region);
        });

        JudicialBooking judicialBooking = TestDataBuilder.buildJudicialBooking();
        judicialBooking.setUserId(judicialOfficeHolders.stream().findFirst()
                .orElse(JudicialOfficeHolder.builder().build()).getUserId());
        judicialBooking.setLocationId("location1");
        judicialBooking.setRegionId("1");
        judicialBookings = Set.of(judicialBooking);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("CIVIL", true));

        List<String> rolesThatRequireRegions = new ArrayList<>(List.of(
                "judge", "circuit-judge"
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
                multiRegionJudicialList
        );

        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(2).getActorId());

        Map<String, List<String>> roleNameToRegionsMap = MultiRegion.buildRoleNameToRegionsMap(rolesThatRequireRegions);

        roleAssignments.forEach(r -> {
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());

            // check region status and add to map
            MultiRegion.assertRegionStatusAndUpdateRoleToRegionMap(r, roleNameToRegionsMap);
        });
        RoleAssignment role = roleAssignments.stream().filter(r -> "judge".equals(r.getRoleName())).findFirst().get();
        assertEquals(judicialBooking.getLocationId(), role.getAttributes().get("baseLocation").asText());
        assertEquals(judicialBooking.getRegionId(), role.getAttributes().get("region").asText());

        // verify regions add to map
        MultiRegion.assertRoleNameToRegionsMapIsAsExpected(
                roleNameToRegionsMap,
                expectedRoleList,
                expectMultiRegion,
                multiRegionJudicialList,
                region, // fallback if not multi-region scenario
                judicialBooking.getRegionId()
        );
    }

    @ParameterizedTest
    @CsvSource({
        "CIVIL Deputy Circuit Judge-Fee-Paid,fee-paid-judge"
    })
    void shouldReturnCircuitJudgeRoles(String setOffice, String roleNameOutput) throws IOException {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        JudicialBooking judicialBooking = TestDataBuilder.buildJudicialBooking();
        judicialBooking.setUserId(judicialOfficeHolders.stream().findFirst()
                .orElse(JudicialOfficeHolder.builder().build()).getUserId());
        judicialBooking.setLocationId("location1");
        judicialBooking.setRegionId("1");
        judicialBookings = Set.of(judicialBooking);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("CIVIL", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(2).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput, "judge","circuit-judge","hmcts-judiciary"));
        roleAssignments.forEach(r -> assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText()));
        RoleAssignment role = roleAssignments.stream().filter(r -> "circuit-judge".equals(r.getRoleName())).findFirst()
                .get();
        assertEquals(judicialBooking.getLocationId(), role.getAttributes().get("baseLocation").asText());
        assertEquals(judicialBooking.getRegionId(), role.getAttributes().get("region").asText());
    }

    @ParameterizedTest
    @CsvSource({
        "CIVIL Designated Civil Judge-Salaried,judge,hmcts-judiciary,leadership-judge,task-supervisor,case-allocator"
    })
    void shouldReturnHmctsJudiciaryRoles(String setOffice,
                                         @AggregateWith(VarargsAggregator.class) String[] roleNameOutput) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("CIVIL", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5, roleAssignments.size());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput));
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(r -> {
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            if (!r.getRoleName().contains("hmcts")) {
                assertEquals(regionId, r.getAttributes().get("region").asText());
            }
        });
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
        assertEquals(3, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
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
                LocalDate.now().minusDays(20L),null,null));
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
            assertNotNull(r.getBeginTime().getDayOfYear());
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
        assertEquals(6, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
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

    @ParameterizedTest
    @CsvSource({
        "CIVIL Designated Civil Judge-Salaried,Salaried",
        "CIVIL Circuit Judge-Salaried,Salaried",
        "CIVIL Specialist Circuit Judge-Salaried,Salaried",
        "CIVIL Senior Circuit Judge-Salaried,Salaried",
        "CIVIL High Court Judge-Salaried,Salaried",
        "CIVIL Deputy Circuit Judge-Fee-Paid,Fee-Paid"
    })
    void shouldReturnJudgeRolesV11(String setOffice,String contractType) throws IOException {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        JudicialBooking judicialBooking = TestDataBuilder.buildJudicialBooking();
        judicialBooking.setUserId(judicialOfficeHolders.stream().findFirst()
                .orElse(JudicialOfficeHolder.builder().build()).getUserId());
        judicialBooking.setLocationId("location1");
        judicialBooking.setRegionId("1");
        judicialBookings = Set.of(judicialBooking);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("civil_wa_1_1", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());

        assertEquals(1, roleAssignments.size());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals("judge", roleAssignments.get(0).getRoleName());

        roleAssignments.forEach(r -> assertEquals(contractType, r.getAttributes().get("contractType").asText()));

        RoleAssignment role = roleAssignments.stream().filter(r -> "judge".equals(r.getRoleName())).findFirst()
                .get();

        if (setOffice.equals("CIVIL Deputy Circuit Judge-Fee-Paid")) {
            assertNotNull(role.getAttributes().get("baseLocation"));
        } else {
            assertNull(role.getAttributes().get("baseLocation"));
        }

        assertNotNull(role.getAttributes().get("region"));
    }
}

