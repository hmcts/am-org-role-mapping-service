package uk.gov.hmcts.reform.orgrolemapping.domain.service;

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
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.VarargsAggregator;

@RunWith(MockitoJUnitRunner.class)
class DroolJudicialRoleMappingCivilTest extends DroolBase {
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

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("civil_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2, roleAssignments.size());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput, "hmcts-judiciary"));
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(r -> {
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            if (!r.getRoleName().contains("hmcts")) {
                assertEquals(regionId, r.getAttributes().get("region").asText());
            }
        });

    }

    //@ParameterizedTest
    @CsvSource({
            "CIVIL Deputy District Judge-Fee-Paid,fee-paid-judge",
            "CIVIL Deputy District Judge - Sitting in Retirement-Fee-Paid,fee-paid-judge",
            "CIVIL Recorder-Fee-Paid,fee-paid-judge"
    })
    void shouldReturnFeePaidRoles(String setOffice, String roleNameOutput) throws IOException {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));
        JudicialBooking judicialBooking = TestDataBuilder.buildJudicialBooking();
        judicialBooking.setUserId(judicialOfficeHolders.stream().findFirst()
                .orElse(JudicialOfficeHolder.builder().build()).getUserId());
        judicialBooking.setLocationId("location1");

        judicialBookings = Set.of(judicialBooking);
        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("civil_temp_judge_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(3, roleAssignments.size());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(2).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput, "judge","hmcts-judiciary"));
        roleAssignments.forEach(r -> assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText()));
    }

    @ParameterizedTest
    @CsvSource({
            "CIVIL Deputy Circuit Judge-Fee-Paid,fee-paid-judge"
    })
    void shouldReturnCircuitJudgeRoles(String setOffice, String roleNameOutput) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("civil_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(3, roleAssignments.size());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(2).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput, "circuit-judge","hmcts-judiciary"));
        roleAssignments.forEach(r -> assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText()));
    }

    @ParameterizedTest
    @CsvSource({
            "CIVIL Designated Civil Judge-Salaried,hmcts-judiciary,leadership-judge,task-supervisor,case-allocator"
    })
    void shouldReturnHmctsJudiciaryRoles(String setOffice,
                                         @AggregateWith(VarargsAggregator.class) String[] roleNameOutput) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("civil_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
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
    void civilJudicialScenario_2() {

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
        assertEquals(2, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("circuit-judge","hmcts-judiciary"));
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
    void civilJudicialScenario_4() {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<Authorisation> authorisationList = TestDataBuilder.buildListOfAuthorisations(2);

        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(TestDataBuilder.buildAppointmentWithParams(
                "487294","TRUE","Recorder", "Fee Paid",
                LocalDate.now().minusYears(1L),null,null,null));
        appointmentList.add(TestDataBuilder.buildAppointmentWithParams(
                "487294","FALSE","Deputy District Judge - Fee Paid","Fee Paid",
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
        assertEquals(3, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("fee-paid-judge",
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

    @Test
    void civilJudicialScenario_9() {

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
        assertEquals(2, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("fee-paid-judge","hmcts-judiciary"));

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    //removes the auth in the past successfully so minus 1
                    assertEquals(authorisationList.size() - 1, r.getAuthorisations().size());
                    assertEquals(JacksonUtils.convertObjectIntoJsonNode(""), r.getAttributes().get("primaryLocation"));
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
    void civilJudicialScenario_16() {

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
        assertEquals(5, roleAssignments.size());

        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("leadership-judge", "circuit-judge", "task-supervisor",
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
}

