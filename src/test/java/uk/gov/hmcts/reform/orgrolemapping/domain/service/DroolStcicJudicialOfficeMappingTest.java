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
import java.util.stream.Collectors;

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
            "President of Tribunal,Salaried,BBA2,N,'senior-judge,judge,case-allocator,task-supervisor,"
                    + "hmcts-judiciary,specific-access-approver-judiciary'",
            "President of Tribunal,Salaried,BBA2,Y,'senior-judge,judge,case-allocator,task-supervisor,"
                    + "hmcts-judiciary,specific-access-approver-judiciary'",
            "Principal Judge,Salaried,BBA2,N,'leadership-judge,senior-judge,judge,case-allocator,task-supervisor,"
                    + "hmcts-judiciary,specific-access-approver-judiciary'",
            "Principal Judge,Salaried,BBA2,Y,'leadership-judge,senior-judge,judge,case-allocator,task-supervisor,"
                    + "hmcts-judiciary,specific-access-approver-judiciary'",
            "Tribunal Judge,Salaried,BBA2,N,'judge,hmcts-judiciary,case-allocator,task-supervisor,"
                    + "specific-access-approver-judiciary'",
            "Tribunal Judge,Salaried,BBA2,Y,'judge,hmcts-judiciary,case-allocator,task-supervisor,"
                    + "specific-access-approver-judiciary'",
            "Tribunal Member Medical,Salaried,BBA2,N,'medical,hmcts-judiciary'",
            "Tribunal Member Medical,Salaried,BBA2,Y,'medical,hmcts-judiciary'",

    })
    void shouldReturnSalariedRoles(String appointment, String appointmentType,
                                  String serviceCode, String endDateNull, String expectedRoles) {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment(appointment);
            judicialAccessProfile.setAppointmentType(appointmentType);
            judicialAccessProfile.setBaseLocationId("1032");
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
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));

        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            assertNull(r.getAttributes().get("primaryLocation"));
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("ST_CIC", r.getAttributes().get("jurisdiction").asText());
            }
        });
    }

    //=================================FEE-PAID ROLES==================================
    @ParameterizedTest
    @CsvSource({
            "Tribunal Judge,Fee Paid,BBA2,N,'fee-paid-judge,hmcts-judiciary'",
            "Tribunal Judge,Fee Paid,BBA2,Y,'fee-paid-judge,hmcts-judiciary'",
            "Tribunal Member,Fee Paid,BBA2,N,'fee-paid-tribunal-member'",
            "Tribunal Member,Fee Paid,BBA2,Y,'fee-paid-tribunal-member'",
            "Tribunal Member Medical,Fee Paid,BBA2,N,'fee-paid-medical'",
            "Tribunal Member Medical,Fee Paid,BBA2,Y,'fee-paid-medical'",
            "Tribunal Member Lay,Fee Paid,BBA2,N,'fee-paid-tribunal-member'",
            "Tribunal Member Lay,Fee Paid,BBA2,Y,'fee-paid-tribunal-member'",
            "Tribunal Member Disability,Fee Paid,BBA2,N,'fee-paid-disability'",
            "Tribunal Member Disability,Fee Paid,BBA2,Y,'fee-paid-disability'",
            "Tribunal Member Financial,Fee Paid,BBA2,N,'fee-paid-financial'",
            "Tribunal Member Financial,Fee Paid,BBA2,Y,'fee-paid-financial'",
    })
    void shouldReturnFeePaidRoles(String appointment, String appointmentType,
                                  String serviceCode,String endDateNull, String expectedRoles) {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment(appointment);
            judicialAccessProfile.setAppointmentType(appointmentType);
            judicialAccessProfile.setBaseLocationId("1032");
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
            assertNull(r.getAttributes().get("primaryLocation"));
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
            } else {
                assertEquals("[376]", r.getAuthorisations().toString());
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
                buildExecuteKieSession(getFeatureFlags("st_cic_1_0", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    // Invalid authorisation(expired enddate) and valid appointment(Fee Paid) and base location(1032)
    @ParameterizedTest
    @CsvSource({
            "Tribunal Judge",
            "Tribunal Member",
            "Tribunal Member Medical",
            "Tribunal Member Lay",
            "Tribunal Member Disability",
            "Tribunal Member Financial"
    })
    void shouldNotReturnFeePaidRolesExpiredEndDate(String appointment) {

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment(appointment);
        profile.setAppointmentType("Fee Paid");
        profile.setBaseLocationId("1032");
        judicialAccessProfiles.add(profile);
        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCodes(List.of("BBA2")).endDate(LocalDateTime.now()
                        .minusMonths(5)).build())));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("st_cic_1_0", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    //Invalid authorisation(wrong servicecode) and valid appointment and base location(1032)
    @ParameterizedTest
    @CsvSource({
            "President of Tribunal",
            "Principal Judge",
            "Tribunal Judge",
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
                buildExecuteKieSession(getFeatureFlags("st_cic_1_0", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    //Invalid authorisation(wrong servicecode) and valid appointment and base location(1032)
    @ParameterizedTest
    @CsvSource({
            "Tribunal Judge",
            "Tribunal Member",
            "Tribunal Member Medical",
            "Tribunal Member Lay",
            "Tribunal Member Disability",
            "Tribunal Member Financial"
    })
    void shouldNotReturnFeePaidRolesWrongServiceCode(String appointment) {

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment(appointment);
        profile.setAppointmentType("Fee Paid");
        profile.setBaseLocationId("1032");
        judicialAccessProfiles.add(profile);
        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCodes(List.of("wrong service code"))
                        .build())));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("st_cic_1_0", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    //wrong base location(1032) and valid authorisation(servicecode) and valid appointment
    @ParameterizedTest
    @CsvSource({
            "Tribunal Member Medical",
    })
    void shouldNotReturnSalariedRolesWrongBaseLocation(String appointment) {

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment(appointment);
        profile.setAppointmentType("Salaried");
        profile.setBaseLocationId("999999");
        judicialAccessProfiles.add(profile);
        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCodes(List.of("BBA2"))
                        .build())));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("st_cic_1_0", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    //wrong base location(1032) and valid authorisation(servicecode) and valid appointment
    @ParameterizedTest
    @CsvSource({
            "Tribunal Member Lay",
            "Tribunal Member Medical",
            "Tribunal Member Disability",
            "Tribunal Member Financial",
    })
    void shouldNotReturnFeePaidRolesWrongBaseLocation(String appointment) {

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment(appointment);
        profile.setAppointmentType("Fee Paid");
        profile.setBaseLocationId("999999");
        judicialAccessProfiles.add(profile);
        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCodes(List.of("BBA2"))
                        .build())));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("st_cic_1_0", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

}
