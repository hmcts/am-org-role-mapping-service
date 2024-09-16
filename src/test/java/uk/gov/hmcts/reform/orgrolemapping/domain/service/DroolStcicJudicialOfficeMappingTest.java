package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Authorisation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@RunWith(MockitoJUnitRunner.class)
class DroolStcicJudicialOfficeMappingTest extends DroolBase {

    //=================================SALARIED ROLES==================================
    @ParameterizedTest
    @CsvSource({
        "President of Tribunal,BBA2,'leadership-judge,senior-judge,judge,case-allocator,task-supervisor,"
                + "hmcts-judiciary,specific-access-approver-judiciary'",
        "Principal Judge,BBA2,'leadership-judge,senior-judge,judge,case-allocator,task-supervisor,"
                + "hmcts-judiciary,specific-access-approver-judiciary'",
        "Tribunal Judge,BBA2,'judge,hmcts-judiciary,case-allocator,task-supervisor,"
                + "specific-access-approver-judiciary'",
        "Judge of the First-tier Tribunal,BBA2,'judge,hmcts-judiciary,case-allocator,task-supervisor,"
                + "specific-access-approver-judiciary'",
        "Circuit Judge,BBA2,'judge,hmcts-judiciary,case-allocator,task-supervisor,"
                + "specific-access-approver-judiciary'",
        "Regional Tribunal Judge,BBA2,'judge,hmcts-judiciary,case-allocator,task-supervisor,"
                + "specific-access-approver-judiciary'",
        "Tribunal Member Medical,BBA2,'medical,hmcts-judiciary'"
    })
    void verifySalariedAndSptwRoles(String appointment, String serviceCode, String expectedRoles) {
        shouldReturnSalariedRoles(appointment, "Salaried", serviceCode, "N", expectedRoles);
        shouldReturnSalariedRoles(appointment, "Salaried", serviceCode, "Y", expectedRoles);
        shouldReturnSalariedRoles(appointment, "SPTW", serviceCode, "N", expectedRoles);
        shouldReturnSalariedRoles(appointment, "SPTW", serviceCode, "Y", expectedRoles);
    }

    void shouldReturnSalariedRoles(String appointment, String appointmentType,
                                   String serviceCode, String endDateNull, String expectedRoles) {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment(appointment);
            judicialAccessProfile.setAppointmentType(appointmentType);
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCodes(List.of(serviceCode)));
            if (endDateNull.equals("Y")) {
                judicialAccessProfile.getAuthorisations().forEach(a -> a.setEndDate(null));
            }

        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("st_cic_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).toList(),
                containsInAnyOrder(expectedRoles.split(",")));

        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
                assertEquals("ST_CIC", r.getAttributes().get("jurisdiction").asText());
            }
        });
    }

    //=================================FEE-PAID ROLES==================================
    @ParameterizedTest
    @CsvSource({
        "Tribunal Judge,Fee Paid,BBA2,'fee-paid-judge,hmcts-judiciary'",
        "Judge of the First-tier Tribunal (sitting in retirement),Fee Paid,BBA2,'fee-paid-judge,hmcts-judiciary'",
        "Chairman,Fee Paid,BBA2,'fee-paid-judge,hmcts-judiciary'",
        "Recorder,Fee Paid,BBA2,'fee-paid-judge,hmcts-judiciary'",
        "Deputy Upper Tribunal Judge,Fee Paid,BBA2,'fee-paid-judge,hmcts-judiciary'",
        "Tribunal Member,Fee Paid,BBA2,'fee-paid-tribunal-member,hmcts-judiciary'",
        "Tribunal Member Lay,Fee Paid,BBA2,'fee-paid-tribunal-member,hmcts-judiciary'",
        "Advisory Committee Member - Magistrate,Voluntary,BBA2,'fee-paid-tribunal-member,hmcts-judiciary'",
        "Magistrate,Voluntary,BBA2,'fee-paid-tribunal-member,hmcts-judiciary'",
        "Tribunal Member Medical,Fee Paid,BBA2,'fee-paid-medical,hmcts-judiciary'",
        "Tribunal Member Optometrist,Fee Paid,BBA2,'fee-paid-medical,hmcts-judiciary'",
        "Tribunal Member Disability,Fee Paid,BBA2,'fee-paid-disability,fee-paid-tribunal-member,hmcts-judiciary'",
        "Member of the First-tier Tribunal (sitting in retirement),Fee Paid,BBA2,'fee-paid-disability,"
                + "fee-paid-tribunal-member,hmcts-judiciary'",
        "Tribunal Member Financially Qualified,Fee Paid,BBA2,'fee-paid-financial,hmcts-judiciary'"
    })
    void verifyFeePaidRoles(String appointment, String appointmentType, String serviceCode, String expectedRoles) {
        shouldReturnFeePaidRoles(appointment, appointmentType, serviceCode, "N", expectedRoles);
        shouldReturnFeePaidRoles(appointment, appointmentType, serviceCode, "Y", expectedRoles);
    }

    void shouldReturnFeePaidRoles(String appointment, String appointmentType, String serviceCode,
                                  String endDateNull, String expectedRoles) {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment(appointment);
            judicialAccessProfile.setAppointmentType(appointmentType);
            judicialAccessProfile.setTicketCodes(List.of("376"));
            judicialAccessProfile.getAuthorisations().forEach(a -> {
                a.setServiceCodes(List.of(serviceCode));
                a.setTicketCode("376");

            });
            if (endDateNull.equals("Y")) {
                judicialAccessProfile.getAuthorisations().forEach(a -> a.setEndDate(null));
            }
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("st_cic_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[376]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
                assertEquals("ST_CIC", r.getAttributes().get("jurisdiction").asText());
            }
        });
    }

    // Invalid authorisation(expired enddate) and valid appointment(Salaried) and base location(1032)
    @ParameterizedTest
    @CsvSource({
        "President of Tribunal",
        "Principal Judge",
        "Tribunal Judge",
        "Judge of the First-tier Tribunal",
        "Circuit Judge",
        "Regional Tribunal Judge",
        "Tribunal Member Medical"
    })
    void shouldNotReturnSalariedRolesExpiredEndDate(String appointment) {

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment(appointment);
        profile.setAppointmentType("Salaried");
        profile.setBaseLocationId("1032");
        judicialAccessProfiles.add(profile);
        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCodes(List.of("BBA2")).endDate(LocalDateTime.now()
                                .minusMonths(5)).build())));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("st_cic_wa_1_0", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    // Invalid authorisation(expired enddate) and valid appointment(Fee Paid) and base location(1032)
    @ParameterizedTest
    @CsvSource({
        "Tribunal Judge",
        "Judge of the First-tier Tribunal (sitting in retirement)",
        "Chairman",
        "Recorder",
        "Deputy Upper Tribunal Judge",
        "Tribunal Member",
        "Tribunal Member Lay",
        "Advisory Committee Member - Magistrate",
        "Magistrate",
        "Tribunal Member Medical",
        "Tribunal Member Optometrist",
        "Tribunal Member Disability",
        "Member of the First-tier Tribunal (sitting in retirement)",
        "Tribunal Member Financially Qualified"
    })
    void shouldNotReturnFeePaidRolesExpiredEndDate(String appointment) {

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment(appointment);
        if (appointment.contains("Magistrate")) {
            profile.setAppointmentType("Voluntary");
        } else {
            profile.setAppointmentType("Fee Paid");
        }
        profile.setBaseLocationId("1032");
        judicialAccessProfiles.add(profile);
        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCodes(List.of("BBA2")).endDate(LocalDateTime.now()
                        .minusMonths(5)).build())));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("st_cic_wa_1_0", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    //Invalid authorisation(wrong servicecode) and valid appointment and base location(1032)
    @ParameterizedTest
    @CsvSource({
        "President of Tribunal",
        "Principal Judge",
        "Tribunal Judge",
        "Judge of the First-tier Tribunal",
        "Circuit Judge",
        "Regional Tribunal Judge",
        "Tribunal Member Medical"
    })
    void shouldNotReturnSalariedRolesWrongServiceCode(String appointment) {

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment(appointment);
        profile.setAppointmentType("Salaried");
        profile.setBaseLocationId("1032");
        judicialAccessProfiles.add(profile);
        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCodes(List.of("wrong service code"))
                        .build())));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("st_cic_wa_1_0", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    //Invalid authorisation(wrong servicecode) and valid appointment and base location(1032)
    @ParameterizedTest
    @CsvSource({
        "Tribunal Judge",
        "Judge of the First-tier Tribunal (sitting in retirement)",
        "Chairman",
        "Recorder",
        "Deputy Upper Tribunal Judge",
        "Tribunal Member",
        "Tribunal Member Lay",
        "Advisory Committee Member - Magistrate",
        "Magistrate",
        "Tribunal Member Medical",
        "Tribunal Member Optometrist",
        "Tribunal Member Disability",
        "Member of the First-tier Tribunal (sitting in retirement)",
        "Tribunal Member Financially Qualified"
    })
    void shouldNotReturnFeePaidRolesWrongServiceCode(String appointment) {

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment(appointment);
        if (appointment.contains("Magistrate")) {
            profile.setAppointmentType("Voluntary");
        } else {
            profile.setAppointmentType("Fee Paid");
        }
        profile.setBaseLocationId("1032");
        judicialAccessProfiles.add(profile);
        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCodes(List.of("wrong service code"))
                        .build())));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("st_cic_wa_1_0", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

}
