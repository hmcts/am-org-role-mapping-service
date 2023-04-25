package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Appointment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Authorisation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialOfficeHolder;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    @ParameterizedTest
    @CsvSource({
            "CIVIL District Judge-Salaried,judge",
            "CIVIL Presiding Judge-Salaried,judge",
            "CIVIL Resident Judge-Salaried,judge",
            "CIVIL Circuit Judge-Salaried,circuit-judge",
            "CIVIL Specialist Circuit Judge-Salaried,circuit-judge",
            "CIVIL Senior Circuit Judge-Salaried,circuit-judge",
            "CIVIL High Court Judge-Salaried,circuit-judge"
    })
    void shouldReturnSalariedRoles(String setOffice, String roleNameOutput) {

        allProfiles.clear();

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("civil_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(3, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput, "hmcts-judiciary", "hearing-viewer"));

        roleAssignments.forEach(r -> {
            if (!"hearing-viewer".equals(r.getRoleName())) {
                assertEquals("Salaried", r.getAttributes().get("contractType").asText());
                assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(), r.getActorId());

                if (!r.getRoleName().contains("hmcts")) {
                    assertEquals("3", r.getAttributes().get("region").asText());
                }
            }
        });
    }

    @ParameterizedTest
    @CsvSource({
            "CIVIL Deputy District Judge-Fee-Paid,fee-paid-judge",
            "CIVIL Deputy District Judge - Sitting in Retirement-Fee-Paid,fee-paid-judge",
            "CIVIL Recorder-Fee-Paid,fee-paid-judge"
    })
    void shouldReturnFeePaidRoles(String setOffice, String roleNameOutput) throws IOException {

        allProfiles.clear();

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));
        JudicialBooking judicialBooking = TestDataBuilder.buildJudicialBooking();
        judicialBooking.setUserId(judicialOfficeHolders.stream().findFirst()
                .orElse(JudicialOfficeHolder.builder().build()).getUserId());
        judicialBooking.setLocationId("location1");
        judicialBooking.setRegionId("1");
        judicialBookings = Set.of(judicialBooking);
        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("civil_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput, "judge", "hmcts-judiciary", "hearing-viewer"));

        roleAssignments.forEach(r -> {
            if (!"hearing-viewer".equals(r.getRoleName())) {
                assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
                assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(), r.getActorId());
            }

            if ("judge".equals(r.getRoleName())) {
                assertEquals(judicialBooking.getLocationId(), r.getAttributes().get("baseLocation").asText());
                assertEquals(judicialBooking.getRegionId(), r.getAttributes().get("region").asText());
            }
        });
    }

    @ParameterizedTest
    @CsvSource({
            "CIVIL Deputy Circuit Judge-Fee-Paid"
    })
    void shouldReturnCircuitJudgeRoles(String setOffice) throws IOException {

        allProfiles.clear();

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        JudicialBooking judicialBooking = TestDataBuilder.buildJudicialBooking();
        judicialBooking.setUserId(judicialOfficeHolders.stream().findFirst()
                .orElse(JudicialOfficeHolder.builder().build()).getUserId());
        judicialBooking.setLocationId("location1");
        judicialBooking.setRegionId("1");
        judicialBookings = Set.of(judicialBooking);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("civil_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("fee-paid-judge", "circuit-judge", "hmcts-judiciary",
                        "hearing-viewer"));

        roleAssignments.forEach(r -> {
            if (!"hearing-viewer".equals(r.getRoleName())) {
                assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
                assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(), r.getActorId());
            }

            if ("circuit-judge".equals(r.getRoleName()) || "senior-judge".equals(r.getRoleName())) {
                assertEquals(judicialBooking.getLocationId(), r.getAttributes().get("baseLocation").asText());
                assertEquals(judicialBooking.getRegionId(), r.getAttributes().get("region").asText());
            }
        });
    }

    @ParameterizedTest
    @CsvSource({
            "CIVIL Designated Civil Judge-Salaried"
    })
    void shouldReturnHmctsJudiciaryRoles(String setOffice) {

        allProfiles.clear();
        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("civil_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5, roleAssignments.size());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("hmcts-judiciary", "leadership-judge", "task-supervisor",
                        "case-allocator", "hearing-viewer"));

        roleAssignments.forEach(r -> {
            if (!"hearing-viewer".equals(r.getRoleName())) {
                assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            }

            if (r.getRoleName().contains("leadership-judge")
                    || r.getRoleName().contains("task-supervisor")
                    || r.getRoleName().contains("case-allocator")
            ) {
                assertEquals("3", r.getAttributes().get("region").asText());
            }
        });
    }

    @Test
    void civilJudicialScenario_2() {

        allProfiles.clear();
        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<Authorisation> authorisationList = TestDataBuilder.buildListOfAuthorisations(1);

        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(TestDataBuilder.buildAppointmentWithParams(
                "487294","TRUE","Circuit Judge", "SPTW",
                LocalDate.now().minusYears(1L),null,
                Collections.singletonList("Pool of Judges"),null));
        appointmentList.add(TestDataBuilder.buildAppointmentWithParams(
                null,"FALSE","Tribunal Judge","Fee Paid",
                LocalDate.now(),LocalDate.now().minusYears(1L),null,null));

        Set<UserAccessProfile> userAccessProfiles = AssignmentRequestBuilder.convertProfileToJudicialAccessProfile(
                TestDataBuilder.buildJudicialProfileWithParams(appointmentList, authorisationList));

        judicialAccessProfiles = userAccessProfiles.stream()
                .map(obj -> (JudicialAccessProfile) obj).collect(Collectors.toSet());
        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("civil_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("circuit-judge","hmcts-judiciary", "hearing-viewer", "hearing-viewer",
                        "hearing-viewer"));

        roleAssignments.forEach(r -> {
            if (!"hearing-viewer".equals(r.getRoleName())) {
                assertEquals("Salaried", r.getAttributes().get("contractType").asText());

                if (r.getGrantType().equals(GrantType.STANDARD)) {
                    assertEquals(authorisationList.size(), r.getAuthorisations().size());
                }
            }

            if ("circuit-judge".equals(r.getRoleName())) {
                assertEquals(appointmentList.get(0).getEpimmsId(),
                        r.getAttributes().get("primaryLocation").asText());
                assertEquals(appointmentList.get(0).getStartDate().getDayOfYear(),
                        r.getBeginTime().getDayOfYear());
                assertNull(r.getEndTime());
            }
        });
    }

    @Test
    void civilJudicialScenario_4() {

        allProfiles.clear();
        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<Authorisation> authorisationList = TestDataBuilder.buildListOfAuthorisations(2);

        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(TestDataBuilder.buildAppointmentWithParams(
                "487294","TRUE","Recorder", "Fee Paid",
                LocalDate.now().minusYears(1L),null,null,null));
        appointmentList.add(TestDataBuilder.buildAppointmentWithParams(
                "487294","FALSE","Deputy District Judge- Fee-Paid","Fee Paid",
                LocalDate.now().minusYears(1L),null,null,null));
        appointmentList.add(TestDataBuilder.buildAppointmentWithParams(
                null,"FALSE","Tribunal Judge","Fee Paid",
                LocalDate.now().minusYears(1L),null,null,null));
        appointmentList.add(TestDataBuilder.buildAppointmentWithParams(
                null,"FALSE","Employment Judge","Fee Paid",
                LocalDate.now().minusYears(1L),null,null,null));

        Set<UserAccessProfile> userAccessProfiles = AssignmentRequestBuilder.convertProfileToJudicialAccessProfile(
                TestDataBuilder.buildJudicialProfileWithParams(appointmentList, authorisationList));

        judicialAccessProfiles = userAccessProfiles.stream()
                .map(obj -> (JudicialAccessProfile) obj).collect(Collectors.toSet());

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("civil_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(7, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("fee-paid-judge", "fee-paid-judge", "hmcts-judiciary",
                        "hearing-viewer", "hearing-viewer","hearing-viewer", "hearing-viewer"));

        roleAssignments.forEach(r -> {
            if (!"hearing-viewer".equals(r.getRoleName())) {
                assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
                assertEquals(appointmentList.get(0).getStartDate().getDayOfYear(), r.getBeginTime().getDayOfYear());
                assertNull(r.getEndTime());

                if (r.getGrantType().equals(GrantType.STANDARD)) {
                    assertEquals(authorisationList.size(), r.getAuthorisations().size());
                }
            }

            if ("fee-paid-judge".equals(r.getRoleName())) {
                assertEquals(appointmentList.get(0).getStartDate().getDayOfYear(),
                        r.getBeginTime().getDayOfYear());
                assertNull(r.getEndTime());
            }
        });
    }

    @Test
    void civilJudicialScenario_9() {

        allProfiles.clear();
        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<Authorisation> authorisationList = TestDataBuilder.buildListOfAuthorisations(3);

        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(TestDataBuilder.buildAppointmentWithParams(
                "487294","FALSE","Recorder", "Fee Paid",
                LocalDate.now().minusDays(20L),null,null,null));
        appointmentList.add(TestDataBuilder.buildAppointmentWithParams(
                "487294","FALSE","Deputy Upper Tribunal Judge","Fee Paid",
                LocalDate.now().minusYears(1L),null,null,null));
        appointmentList.add(TestDataBuilder.buildAppointmentWithParams(
                null,"TRUE","Tribunal Judge","Fee Paid",
                LocalDate.now().minusYears(1L),null,
                Arrays.asList("Diversity Role Models", "Pool of Judges", "Resident Immigration Judge"), "BFA1"));

        Set<UserAccessProfile> userAccessProfiles = AssignmentRequestBuilder.convertProfileToJudicialAccessProfile(
                TestDataBuilder.buildJudicialProfileWithParams(appointmentList, authorisationList));

        judicialAccessProfiles = userAccessProfiles.stream()
                .map(obj -> (JudicialAccessProfile) obj).collect(Collectors.toSet());

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("civil_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(10, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("fee-paid-judge", "fee-paid-judge", "hmcts-judiciary", "hmcts-judiciary",
                        "judge", "leadership-judge", "hearing-viewer", "senior-judge", "case-allocator",
                        "task-supervisor"));

        //TODO: resolve below test block
//
//        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
//                .forEach(r -> {
//                    //removes the auth in the past successfully so minus 1
//                    assertEquals(authorisationList.size() - 1, r.getAuthorisations().size());
//                    assertEquals(JacksonUtils.convertObjectIntoJsonNode(""), r.getAttributes().get("primaryLocation"));
//                });
//
//        assertEquals(appointmentList.get(0).getStartDate().getDayOfYear(),
//                roleAssignments.get(0).getBeginTime().getDayOfYear());
//        assertNull(roleAssignments.get(0).getEndTime());
//
//        roleAssignments.forEach(r -> {
//            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
//            assertEquals(appointmentList.get(0).getStartDate().getDayOfYear(), r.getBeginTime().getDayOfYear());
//            assertNull(r.getEndTime());
//        });
    }

    @Test
    void civilJudicialScenario_16() {

        allProfiles.clear();
        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<Authorisation> authorisationList = TestDataBuilder.buildListOfAuthorisations(4);

        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(TestDataBuilder.buildAppointmentWithParams(
                "886493","TRUE","Senior Circuit Judge", "Salaried",
                LocalDate.now().minusYears(1L),null, Collections.singletonList("Designated Civil Judge"),null));

        Set<UserAccessProfile> userAccessProfiles = AssignmentRequestBuilder.convertProfileToJudicialAccessProfile(
                TestDataBuilder.buildJudicialProfileWithParams(appointmentList, authorisationList));

        judicialAccessProfiles = userAccessProfiles.stream()
                .map(obj -> (JudicialAccessProfile) obj).collect(Collectors.toSet());

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("civil_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(7, roleAssignments.size());

        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("leadership-judge", "circuit-judge", "task-supervisor",
                        "case-allocator", "hmcts-judiciary", "hearing-viewer", "hearing-viewer"));

        roleAssignments.forEach(r -> {
            if (!"hearing-viewer".equals(r.getRoleName())) {
                assertEquals("Salaried", r.getAttributes().get("contractType").asText());
                assertEquals(appointmentList.get(0).getStartDate().getDayOfYear(), r.getBeginTime().getDayOfYear());
                assertNull(r.getEndTime());

                if (r.getGrantType().equals(GrantType.STANDARD)) {
                    assertEquals(authorisationList.size(), r.getAuthorisations().size());
                    assertEquals(appointmentList.get(0).getEpimmsId(),
                            r.getAttributes().get("primaryLocation").asText());
                }
            }
        });
    }

    @Test
    void civilJudicialScenario_DistrictJudgeInRetirement() {

        allProfiles.clear();
        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<Authorisation> authorisationList = TestDataBuilder.buildListOfAuthorisations(2);

        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(TestDataBuilder.buildAppointmentWithParams(
                "487294","FALSE","Deputy District Judge- Sitting in Retirement","Fee Paid",
                LocalDate.now().minusYears(1L),null,null,null));

        Set<UserAccessProfile> userAccessProfiles = AssignmentRequestBuilder.convertProfileToJudicialAccessProfile(
                TestDataBuilder.buildJudicialProfileWithParams(appointmentList, authorisationList));

        judicialAccessProfiles = userAccessProfiles.stream()
                .map(obj -> (JudicialAccessProfile) obj).collect(Collectors.toSet());


        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("civil_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("fee-paid-judge", "hmcts-judiciary", "hearing-viewer", "hearing-viewer"));

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD))
                .filter(r -> CollectionUtils.isNotEmpty(r.getAuthorisations()))
                .toList()
                .forEach(r -> {
                    assertEquals(authorisationList.size(), r.getAuthorisations().size());

                    if ("fee-paid-judge".equals(r.getRoleName()) || "hmcts-judiciary".equals(r.getRoleName())) {
                        assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
                        assertEquals(appointmentList.get(0).getStartDate().getDayOfYear(),
                                r.getBeginTime().getDayOfYear());
                        assertNull(r.getEndTime());
                    }
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

        allProfiles.clear();

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

        assertEquals(2, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(
                        "judge", "hearing-viewer"));

        RoleAssignment role = roleAssignments.stream().filter(r -> "judge".equals(r.getRoleName())).findFirst()
                .get();

        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(), role.getActorId());
        assertEquals(contractType, role.getAttributes().get("contractType").asText());

        if (setOffice.equals("CIVIL Deputy Circuit Judge-Fee-Paid")) {
            assertNotNull(role.getAttributes().get("baseLocation"));
        } else {
            assertNull(role.getAttributes().get("baseLocation"));
        }

        assertNotNull(role.getAttributes().get("region"));
    }
}

