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
        "President of Tribunal,Salaried,BBA3,'leadership-judge,judge,post-hearing-salaried-judge,case-allocator,"
                + "task-supervisor,specific-access-approver-judiciary,hmcts-judiciary'",
        "Regional Tribunal Judge,Salaried,BBA3,'leadership-judge,judge,post-hearing-salaried-judge,case-allocator,"
                + "task-supervisor,specific-access-approver-judiciary,hmcts-judiciary'",
        "Principal Judge,Salaried,BBA3,'leadership-judge,judge,post-hearing-salaried-judge,case-allocator,"
                    + "task-supervisor,specific-access-approver-judiciary,hmcts-judiciary'",
        "Tribunal Judge,Salaried,BBA3,'hmcts-judiciary,judge,post-hearing-salaried-judge'"
    })
    void shouldReturSalariedRoles(String appointment, String appointmentType,
                                  String serviceCode, String expectedRoles) {

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
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
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
                assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());
            }
        });
    }

    //=================================FEE-PAID ROLES==================================
    @ParameterizedTest
    @CsvSource({
        "Tribunal Judge,Fee Paid,BBA3,'fee-paid-judge,hmcts-judiciary','371'",
        "Judge of the First-tier Tribunal (sitting in retirement),"
            + "Fee Paid,BBA3,'fee-paid-judge,hmcts-judiciary','371'",
        "Tribunal Member Medical,Fee Paid,BBA3,'fee-paid-medical,hmcts-judiciary','371'",
        "Tribunal Member Optometrist,Fee Paid,BBA3,'fee-paid-medical,hmcts-judiciary','371'",
        "Tribunal Member Disability,Fee Paid,BBA3,'fee-paid-disability,hmcts-judiciary','371'",
        "Member of the First-tier Tribunal Lay,Fee Paid,BBA3,'fee-paid-disability,hmcts-judiciary','371'",
        "Tribunal Member,Fee Paid,BBA3,'fee-paid-tribunal-member,hmcts-judiciary','371'",
        "Tribunal Member Lay,Fee Paid,BBA3,'fee-paid-tribunal-member,hmcts-judiciary','371'",
        "Tribunal Member Service,Fee Paid,BBA3,'fee-paid-tribunal-member,hmcts-judiciary','371'",
        "Tribunal Member Financially Qualified,Fee Paid,BBA3,'fee-paid-financial,hmcts-judiciary','372'",
        "Tribunal Member Financially Qualified,Fee Paid,BBA3,'fee-paid-financial,hmcts-judiciary','362'",
        "Member of the First-tier Tribunal,Fee Paid,BBA3,'fee-paid-financial,hmcts-judiciary','372'",
        "Member of the First-tier Tribunal,Fee Paid,BBA3,'fee-paid-financial,hmcts-judiciary','362'"
    })
    void shouldReturnTribunalMemberMedicalFeePaidRoles2(String appointment, String appointmentType,
                                                       String serviceCode, String expectedRoles, String ticketCode) {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment(appointment);
            judicialAccessProfile.setAppointmentType(appointmentType);
            judicialAccessProfile.setBaseLocationId("1032");
            judicialAccessProfile.setTicketCodes(List.of(ticketCode));
            judicialAccessProfile.getAuthorisations().forEach(a -> {
                a.setServiceCodes(List.of(serviceCode));
                a.setTicketCode(ticketCode);
            });
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

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
                assertEquals("[" + ticketCode + "]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
                assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());
            }
        });
    }

    //Special Medical Salaried
    //sscs_tribunal_member_medical_salaried_joh
    @ParameterizedTest
    @CsvSource({

        "Tribunal Member Medical,'hmcts-judiciary,medical'",
        "Chief Medical Member First-tier Tribunal,'hmcts-judiciary,medical'",
        "Regional Medical Member,'hmcts-judiciary,medical'"

    })
    void shouldReturnTribunalMemberMedicalRolesNoAccessRequests(String appointment, String expectedRoles) {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment(appointment);
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.setBaseLocationId("1032");
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCodes(List.of("BBA3")));
        });


        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));

        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("1032"));

            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
                assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());

            }
        });

        assertEquals("hearing_work,priority",
                roleAssignments.get(1).getAttributes().get("workTypes").asText());
    }

    //sscs_district_tribunal_judge_salaried_joh
    @Test
    void shouldReturnTribunalDistrictJudgeSalariedSetUpRole() {

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
        assertTrue(roleAssignments.isEmpty());
    }

    //sscs_regional_medical_member_salaried_joh
    @Test
    void shouldReturnEmptyForRoleRegionalMedicalSalariedSetUpRole() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setRoles(List.of("Regional Medical Member"));
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCodes(List.of("BBA3")));
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());

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
        "Principal Judge",
        "Tribunal Member Medical",
        "Chief Medical Member First-tier Tribunal",
        "Regional Medical Member",
        "Tribunal Judge"
    })
    void shouldNotReturnSalariedRolesExpiredEndDate(String appointment) {

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
        "Principal Judge,AAA",
        "Tribunal Member Medical,AAA",
        "Chief Medical Member First-tier Tribunal,AAA",
        "Regional Medical Member,AAA",
        "Tribunal Judge,AAA"
    })
    void shouldNotReturnSalariedRolesExpiredEndDate(String appointment, String serviceCode) {

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
        "Judge of the First-tier Tribunal (sitting in retirement)",
        "Tribunal Member Medical",
        "Tribunal Member Disability",
        "Member of the First-tier Tribunal Lay",
        "Tribunal Member Financially Qualified",
        "Member of the First-tier Tribunal",
        "Tribunal Member Lay",
        "Tribunal Member Optometrist",
        "Tribunal Member Service",
        "Tribunal Member",
        "Tribunal Judge"
    })
    void shouldNotReturnTribunalFeePaidRolesExpiredEndDate(String appointment) {

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
        "Member of the First-tier Tribunal Lay,AAA",
        "Tribunal Member Financially Qualified,AAA",
        "Member of the First-tier Tribunal,AAA",
        "Tribunal Member Lay,AAA",
        "Tribunal Member Optometrist,AAA",
        "Tribunal Member Service,AAA",
        "Tribunal Member,AAA",
        "Tribunal Judge,AAA",
        "Judge of the First-tier Tribunal (sitting in retirement),AAA"
    })
    void shouldNotReturnTribunalFeePaidRolesExpiredEndDate(String appointment,
                                                           String serviceCode) {

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
        "Principal Judge,AAA",
        "Tribunal Member Medical,AAA",
        "Chief Medical Member First-tier Tribunal,AAA",
        "Regional Medical Member,AAA",
        "Tribunal Judge,AAA"
    })
    void shouldNotReturnSalariedExpiredDateandWServiceode(String appointment, String serviceCode) {

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
        "Member of the First-tier Tribunal Lay,AAA",
        "Tribunal Member Financially Qualified,AAA",
        "Member of the First-tier Tribunal,AAA",
        "Tribunal Member Lay,AAA",
        "Tribunal Member Optometrist,AAA",
        "Tribunal Member Service,AAA",
        "Tribunal Member,AAA",
        "Tribunal Judge,AAA",
        "Judge of the First-tier Tribunal (sitting in retirement),AAA"
    })
    void shouldNotReturnFeePaidRolesExpiredDateandWServiceode(String appointment,
                                                              String serviceCode) {

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


    @ParameterizedTest
    @CsvSource({

        "Tribunal Member Medical,'hmcts-judiciary,medical'",
        "Chief Medical Member First-tier Tribunal,'hmcts-judiciary,medical'",
        "Regional Medical Member,'hmcts-judiciary,medical'"
    })

   void shouldReturnAllValidAppointmentRoles(String appointment, String expectedRoles) {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setRoles(List.of("District Tribunal Judge", "Regional Medical Member"));
            judicialAccessProfile.setAppointment(appointment);
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.setBaseLocationId("1032");
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCodes(List.of("BBA3")));

        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        assertFalse(roleAssignments.isEmpty());

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

    @ParameterizedTest
    @CsvSource({

        "Tribunal Member Medical,'hmcts-judiciary,medical'",
        "Chief Medical Member First-tier Tribunal,'hmcts-judiciary,medical'",
        "Regional Medical Member,'hmcts-judiciary,medical'"
    })
    void shouldReturnAllValidInAppointmentRoles(String appointment, String expectedRoles) {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setRoles(List.of("District Judge", "Medical Member"));
            judicialAccessProfile.setAppointment(appointment);
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.setBaseLocationId("1032");
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCodes(List.of("BBA3")));

        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        assertFalse(roleAssignments.isEmpty());
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
                containsInAnyOrder("leadership-judge", "task-supervisor", "case-allocator",
                        "specific-access-approver-judiciary", "hmcts-judiciary", "judge",
                        "post-hearing-salaried-judge"));
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

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setRoles(List.of("Wrong Role"));
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCodes(List.of("BBA3")));
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @CsvSource({
        "President of Tribunal,Salaried",
        "Regional Tribunal Judge,Salaried",
        "Principal Judge,Salaried",
        "Tribunal Judge,Salaried",
        "Tribunal Judge,Fee Paid",
        "Judge of the First-tier Tribunal (sitting in retirement),Salaried",
        "Tribunal Member Medical,Salaried",
        "Chief Medical Member First-tier Tribunal,Salaried",
        "Regional Medical Member,Salaried",
        "Tribunal Member Medical,Fee Paid",
        "Tribunal Member Optometrist,Fee Paid",
        "Tribunal Member Disability,Fee Paid",
        "Member of the First-tier Tribunal Lay,Fee Paid",
        "Tribunal Member,Fee Paid",
        "Tribunal Member Lay,Fee Paid",
        "Tribunal Member Service,Fee Paid",
        "Tribunal Member Financially Qualified,Fee Paid",
        "Member of the First-tier Tribunal,Fee Paid"
    })
    void shouldReturnCftRegionIdV1FromJapAsRegion(String appointment, String appointmentType) {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment(appointment);
            judicialAccessProfile.setAppointmentType(appointmentType);
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCodes(List.of("BBA3")));
            judicialAccessProfile.setCftRegionIdV1("cft_region_id_v1");
            judicialAccessProfile.setRegionId("location_id");
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        roleAssignments.forEach(r -> {
            if (r.getAttributes().get("region") != null) {
                assertEquals("cft_region_id_v1", r.getAttributes().get("region").asText());
            }
        });
    }

}
