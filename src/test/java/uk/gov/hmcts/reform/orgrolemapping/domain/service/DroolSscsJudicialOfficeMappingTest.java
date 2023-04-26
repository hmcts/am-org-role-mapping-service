package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
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
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
class DroolSscsJudicialOfficeMappingTest extends DroolBase {

    //=================================SALARIED ROLES==================================
    @ParameterizedTest
    @CsvSource({
            "President of Tribunal,Salaried,BBA3,judge,"
                    + "'hearing_work,decision_making_work,routine_work,access_requests,priority'",
            "Regional Tribunal Judge,Salaried,BBA3,judge,"
                    + "'hearing_work,decision_making_work,routine_work,access_requests,priority'",
            "Tribunal Judge,Salaried,BBA3,judge,"
                    + "'hearing_work,decision_making_work,routine_work,access_requests,priority'"
    })
    void shouldReturnSalariedRoles(String appointment, String appointmentType,
                                  String serviceCode, String roleNameOutput, String workTypes) {

        allProfiles.clear();

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment(appointment);
            judicialAccessProfile.setAppointmentType(appointmentType);
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCodes(List.of(serviceCode)));
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput, "case-allocator", "task-supervisor",
                        "hmcts-judiciary", "hearing-viewer"));

        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            if (!"hearing-viewer".equals(r.getRoleName())) {
                assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            }

            if ("judge".equals(r.getRoleName())) {
                assertEquals(workTypes, r.getAttributes().get("workTypes").asText());
            }

            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));

            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
                assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());

            }
        });
    }

    //Special Medical Salaried
    //sscs_tribunal_member_medical_salaried_joh
    @Test
    void shouldReturnTribunalMemberMedicalRolesNoAccessRequests() {

        allProfiles.clear();

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Member Medical");
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.setBaseLocationId("1032");
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCodes(List.of("BBA3")));
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("case-allocator", "task-supervisor", "hmcts-judiciary",
                        "medical", "hearing-viewer"));

        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            if (!"hearing-viewer".equals(r.getRoleName())) {
                assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            }

            if ("medical".equals(r.getRoleName())) {
                assertEquals("hearing_work,decision_making_work,routine_work,priority",
                        r.getAttributes().get("workTypes").asText());
            }

            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("1032"));

            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
                assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());

            }
        });
    }

    //=================================FEE-PAID ROLES==================================
    //sscs_tribunal_member_medical_fee_paid_joh
    //sscs_tribunal_member_disability_fee_paid_joh
    //sscs_tribunal_member_financially_qualified_joh fee_paid
    //sscs_tribunal_member_fee_paid_joh
    //sscs_tribunal_member_lay_fee_paid_joh
    //sscs_tribunal_member_optometrist_fee_paid_joh
    //sscs_tribunal_member_service_fee_paid_joh
    //sscs_tribunal_judge_fee_paid_joh
    @ParameterizedTest
    @CsvSource({
            "Tribunal Member Medical,Fee Paid,BBA3,'fee-paid-medical,hearing-viewer','hearing_work,priority'",
            "Tribunal Member Disability,Fee Paid,BBA3,'fee-paid-disability,hearing-viewer','hearing_work,priority'",
            "Tribunal Member Financially Qualified,Fee Paid,BBA3,'fee-paid-financial,hearing-viewer',"
                    + "'hearing_work,priority'",
            "Tribunal Member Lay,Fee Paid,BBA3,'fee-paid-disability,hearing-viewer','hearing_work,priority'",
            "Tribunal Member Optometrist,Fee Paid,BBA3,'fee-paid-medical,hearing-viewer','hearing_work,priority'",
            "Tribunal Member Service,Fee Paid,BBA3,'fee-paid-disability,hearing-viewer','hearing_work,priority'",
            "Tribunal Member,Fee Paid,BBA3,'fee-paid-disability,hearing-viewer','hearing_work,priority'",
            "Tribunal Judge,Fee Paid,BBA3,'fee-paid-judge,hearing-viewer',"
                    + "'hearing_work,decision_making_work,routine_work,priority'"
    })
    void shouldReturnTribunalMemberMedicalFeePaidRoles(String appointment, String appointmentType,
                                                       String serviceCode, String roleNameOutput, String workTypes) {

        allProfiles.clear();

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment(appointment);
            judicialAccessProfile.setAppointmentType(appointmentType);
            judicialAccessProfile.setBaseLocationId("1032");
            judicialAccessProfile.getAuthorisations().forEach(a -> {
                a.setServiceCodes(List.of(serviceCode));
                a.setTicketCode("362");
            });
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(roleNameOutput.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput.split(",")));

        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles
                    .stream().iterator().next().getUserId(), r.getActorId());

            if (!"hearing-viewer".equals(r.getRoleName())) {
                assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
                assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
                assertEquals(workTypes, r.getAttributes().get("workTypes").asText());
            }
        });
    }

    //sscs_district_tribunal_judge_salaried_joh
    @Test
    void shouldReturnTribunalDistrictJudgeSalariedSetUpRole() {

        allProfiles.clear();

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("judge");
            judicialAccessProfile.setRoles(List.of("District Tribunal Judge"));
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCodes(List.of("BBA3")));
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("hearing-viewer"));
    }

    //sscs_regional_medical_member_salaried_joh
    @Test
    void shouldReturnEmptyForRoleRegionalMedicalSalariedSetUpRole() {

        allProfiles.clear();

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setRoles(List.of("Regional Medical Member"));
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCodes(List.of("BBA3")));
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("hearing-viewer"));
    }

    //    Invalid authorisation(expired enddate) and valid appointment
    //    Invalid authorisation(wrong servicecode) and valid appointment
    //    Invalid authorisation(expired enddate) and Invalid appointment(wrong servicecode)
    //    Valid appointmentRoles
    //    Invalid appointmentRoles

    // Invalid authorisation(expired enddate) and valid appointment(Salaried)
    @ParameterizedTest
    @CsvSource({
            "President of Tribunal",
            "Regional Tribunal Judge",
            "Tribunal Member Medical",
            "Tribunal Judge"
    })
    void shouldNotReturnSalariedRolesExpiredEndDate(String appointment) {

        allProfiles.clear();

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment(appointment);
        profile.setAppointmentType("Salaried");
        judicialAccessProfiles.add(profile);
        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCodes(List.of("BBA3")).endDate(LocalDateTime.now()
                                .minusMonths(5)).build())));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    //Invalid authorisation(wrong servicecode) and valid appointment
    @ParameterizedTest
    @CsvSource({
            "President of Tribunal,AAA",
            "Regional Tribunal Judge,AAA",
            "Tribunal Member Medical,AAA",
            "Tribunal Judge,AAA"
    })
    void shouldNotReturnSalariedRolesExpiredEndDate(String appointment, String serviceCode) {

        allProfiles.clear();

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment(appointment);
        profile.setAppointmentType("Salaried");
        judicialAccessProfiles.add(profile);
        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCodes(List.of(serviceCode))
                        .build())));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    // Invalid authorisation(expired enddate) and valid appointment(Fee-Paid)
    @ParameterizedTest
    @CsvSource({
            "Tribunal Member Medical",
            "Tribunal Member Disability",
            "Tribunal Member Financially Qualified",
            "Tribunal Member Lay",
            "Tribunal Member Optometrist",
            "Tribunal Member Service",
            "Tribunal Member",
            "Tribunal Judge"
    })
    void shouldNotReturnTribunalFeePaidRolesExpiredEndDate(String appointment) {

        allProfiles.clear();

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment(appointment);
        profile.setAppointmentType("Fee Paid");
        judicialAccessProfiles.add(profile);
        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCodes(List.of("BBA3")).endDate(LocalDateTime.now()
                                .minusMonths(5)).build())));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    //Invalid authorisation(wrong servicecode) and valid appointment
    @ParameterizedTest
    @CsvSource({
            "Tribunal Member Medical,AAA",
            "Tribunal Member Disability,AAA",
            "Tribunal Member Financially Qualified,AAA",
            "Tribunal Member Lay,AAA",
            "Tribunal Member Optometrist,AAA",
            "Tribunal Member Service,AAA",
            "Tribunal Member,AAA",
            "Tribunal Judge,AAA"
    })
    void shouldNotReturnTribunalFeePaidRolesExpiredEndDate(String appointment,
                                                           String serviceCode) {
        allProfiles.clear();

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment(appointment);
        profile.setAppointmentType("Fee Paid");
        judicialAccessProfiles.add(profile);
        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCodes(List.of(serviceCode))
                        .build())));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    //Invalid authorisation(expired enddate) and Invalid appointment(wrong servicecode)
    @ParameterizedTest
    @CsvSource({
            "President of Tribunal,AAA",
            "Regional Tribunal Judge,AAA",
            "Tribunal Member Medical,AAA",
            "Tribunal Judge,AAA"
    })
    void shouldNotReturnSalariedExpiredDateandWServiceode(String appointment, String serviceCode) {

        allProfiles.clear();

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment(appointment);
        profile.setAppointmentType("Salaried");
        judicialAccessProfiles.add(profile);
        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCodes(List.of(serviceCode)).endDate(LocalDateTime.now()
                                .minusMonths(5)).build())));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());

    }

    //Invalid authorisation(expired enddate) and Invalid appointment(wrong servicecode)
    @ParameterizedTest
    @CsvSource({
            "Tribunal Member Medical,AAA",
            "Tribunal Member Disability,AAA",
            "Tribunal Member Financially Qualified,AAA",
            "Tribunal Member Lay,AAA",
            "Tribunal Member Optometrist,AAA",
            "Tribunal Member Service,AAA",
            "Tribunal Member,AAA",
            "Tribunal Judge,AAA"
    })
    void shouldNotReturnFeePaidRolesExpiredDateandWServiceode(String appointment,
                                                              String serviceCode) {
        allProfiles.clear();

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment(appointment);
        profile.setAppointmentType("Fee Paid");
        judicialAccessProfiles.add(profile);
        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCodes(List.of(serviceCode)).endDate(LocalDateTime.now()
                                .minusMonths(5)).build())));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    @Test
    void shouldReturnAllValidAppointmentRoles() {

        allProfiles.clear();

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setRoles(List.of("District Tribunal Judge", "Regional Medical Member"));
            judicialAccessProfile.setAppointment("Tribunal Member Medical");
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.setBaseLocationId("1032");
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCodes(List.of("BBA3")));

        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("task-supervisor", "case-allocator", "hmcts-judiciary", "medical",
                        "hearing-viewer"));
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
            }
        });
    }

    @Test
    void shouldReturnAllValidInAppointmentRoles() {

        allProfiles.clear();

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setRoles(List.of("District Judge", "Medical Member"));
            judicialAccessProfile.setAppointment("Tribunal Member Medical");
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.setBaseLocationId("1032");
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCodes(List.of("BBA3")));

        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("task-supervisor", "case-allocator", "hmcts-judiciary", "medical",
                        "hearing-viewer"));
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
            }
        });
    }

    @Test
    void shouldReturnAllValidAppointmentRoles_unmappedAppoitment() {

        allProfiles.clear();

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("President of Tribunal");
            judicialAccessProfile.setRoles(List.of("District Tribunal Judge", "Regional Medical Member"));
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCodes(List.of("BBA3")));

        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("task-supervisor", "case-allocator", "hmcts-judiciary", "judge",
                        "hearing-viewer"));
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
            }
        });

    }

    @Test
    void shouldReturnAllInValidDefaultRoles() {

        allProfiles.clear();

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setRoles(List.of("Wrong Role"));
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCodes(List.of("BBA3")));
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("hearing-viewer"));
    }
}
