package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Authorisation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.ROLE_ASSIGNMENTS_RESULTS_KEY;

@RunWith(MockitoJUnitRunner.class)
class DroolJrdOfficeOrgRoleMappingTest extends DroolBase {

    String workTypes = "hearing-work,upper-tribunal,decision-making-work,applications";
    String workTypesFP = "hearing-work,decision-making-work,applications";

    @Test
    void shouldReturnPresidentRoles_withIAC() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("President of Tribunal");
            judicialAccessProfile.setServiceCode("BFA1");
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCode(null));
        });
        validatePresidentRoles();
    }

    @Test
    void shouldReturnPresidentRoles_withAuthorisation() {

        judicialAccessProfiles.forEach(profile -> profile.setAppointment("President of Tribunal"));
        validatePresidentRoles();
    }

    private void validatePresidentRoles() {
        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertEquals("senior-judge", roleAssignments.get(0).getRoleName());
        assertEquals("hmcts-judiciary", roleAssignments.get(1).getRoleName());
        assertEquals("case-allocator", roleAssignments.get(2).getRoleName());
        assertEquals("judge", roleAssignments.get(3).getRoleName());
        assertEquals(workTypes, roleAssignments.get(0).getAttributes().get("workTypes").asText());
        assertEquals(workTypes, roleAssignments.get(3).getAttributes().get("workTypes").asText());
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
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
    void shouldReturnEmptyRoles_withoutAuthorisationAndIAC_allAppointments() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("President of Tribunal");
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCode(null));
        });

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Judge");
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCode(null));
        });
        JudicialAccessProfile tribunalProfile = TestDataBuilder.buildJudicialAccessProfile();
        tribunalProfile.setAppointment("Tribunal Judge");
        tribunalProfile.setAppointmentType("SPTW");
        tribunalProfile.getAuthorisations().forEach(a -> a.setServiceCode(null));
        judicialAccessProfiles.add(tribunalProfile);

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment("Tribunal Judge");
        profile.setAppointmentType("fee paid");
        profile.getAuthorisations().forEach(a -> a.setServiceCode(null));
        judicialAccessProfiles.add(profile);

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    @Test
    @DisplayName("Scenario 3b: Tribunal Judge Salaried")
    void shouldReturnTribunalJudgeSalariedRoleswithIAC() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Judge");
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.setServiceCode("BFA1");
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCode(null));
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(3, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("hmcts-judiciary", "case-allocator", "judge"));
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
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
    @DisplayName("Scenario 4: IAC Judge with Non IAC Auth")
    void iacJudgeWithoutIacAuth() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Judge");
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.setServiceCode("BFA1");
            judicialAccessProfile.setTicketCodes(List.of("373"));
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCode("BAA03"));
        });
        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(3, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("hmcts-judiciary", "case-allocator", "judge"));
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
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
    void shouldReturnTribunalJudgeSalariedRoles_withAuthorisation() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Judge");
            judicialAccessProfile.setAppointmentType("Salaried");
        });
        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment("Tribunal Judge");
        profile.setAppointmentType("SPTW");
        judicialAccessProfiles.add(profile);

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(6, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("hmcts-judiciary", "case-allocator", "judge", "hmcts-judiciary",
                        "case-allocator", "judge"));
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
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
    @DisplayName("Scenario 1, 3a : should return Tribunal FeePaid Judge roles with IAC")
    void shouldReturnTribunalFeePaidJudgeRoles_withIAC() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Judge");
            judicialAccessProfile.setAppointmentType("fee paid");
            judicialAccessProfile.setServiceCode("BFA1");

            judicialAccessProfile.getAuthorisations().forEach(a -> {
                a.setServiceCode("BFA1");
                a.setStartDate(LocalDateTime.now().minusMonths(20));
                a.setEndDate(LocalDateTime.now().minusMonths(10));
            });
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2, roleAssignments.size());
        assertEquals("hmcts-judiciary", roleAssignments.get(0).getRoleName());
        assertEquals("fee-paid-judge", roleAssignments.get(1).getRoleName());
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
            }
        });
        assertEquals(workTypesFP, roleAssignments.get(1).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnTribunalFeePaidJudgeRoles_withAuthorisation() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Judge");
            judicialAccessProfile.setAppointmentType("fee paid");
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2, roleAssignments.size());
        assertEquals("hmcts-judiciary", roleAssignments.get(0).getRoleName());
        assertEquals("fee-paid-judge", roleAssignments.get(1).getRoleName());
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
            }
        });
        assertEquals(workTypesFP, roleAssignments.get(1).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnTribunalFeePaidJudgeRolesAndUnmatchedAppointment_withAuthorisation() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Judge");
            judicialAccessProfile.setAppointmentType("fee paid");
        });

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment("Employment Judge");
        profile.setAppointmentType("fee paid");
        judicialAccessProfiles.add(profile);

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertEquals("hmcts-judiciary", roleAssignments.get(0).getRoleName());
        assertEquals("fee-paid-judge", roleAssignments.get(1).getRoleName());
        assertEquals("hmcts-judiciary", roleAssignments.get(2).getRoleName());
        assertEquals("fee-paid-judge", roleAssignments.get(3).getRoleName());
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
            }
        });
        assertEquals(workTypesFP, roleAssignments.get(1).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnImmigrationJudicialRoles_withIAC() {
        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setRoles(List.of("Assistant Resident Judge",
                    "Resident Immigration Judge",
                    "Designated Immigration Judge"));
            judicialAccessProfile.setAppointmentType("fee paid");
            judicialAccessProfile.setServiceCode("BFA1");
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCode(null));
        });


        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(16, roleAssignments.size());
        assertEquals("senior-judge", roleAssignments.get(0).getRoleName());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("hmcts-judiciary", "case-allocator", "judge", "leadership-judge",
                        "task-supervisor", "hmcts-judiciary", "case-allocator", "judge", "leadership-judge",
                        "task-supervisor", "hmcts-judiciary", "case-allocator", "judge", "leadership-judge",
                        "task-supervisor", "senior-judge"));
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());

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
    void shouldReturnImmigrationJudicialRoles_withAuthorisation() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setRoles(List.of("Assistant Resident Judge",
                    "Resident Immigration Judge",
                    "Designated Immigration Judge"));
            judicialAccessProfile.setAppointmentType("fee paid");
        });


        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(16, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("hmcts-judiciary", "case-allocator", "judge", "leadership-judge",
                        "task-supervisor", "hmcts-judiciary", "case-allocator", "judge", "leadership-judge",
                        "task-supervisor", "hmcts-judiciary", "case-allocator", "judge", "leadership-judge",
                        "task-supervisor", "senior-judge"));
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
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
    void shouldReturnDefaultTribunalJudgeSalariedRoles_authorisationBased() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Employment Judge");
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.setServiceCode("BFA2");
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(3, roleAssignments.size());
        assertEquals("hmcts-judiciary", roleAssignments.get(0).getRoleName());
        assertEquals("case-allocator", roleAssignments.get(1).getRoleName());
        assertEquals("judge", roleAssignments.get(2).getRoleName());
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
            }
        });
        assertEquals(workTypes, roleAssignments.get(2).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnDefaultTribunalJudgeFeePaidRoles_authorisationBased() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Employment Judge");
            judicialAccessProfile.setAppointmentType("fee paid");
            judicialAccessProfile.setServiceCode("BFA2");
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2, roleAssignments.size());
        assertEquals("hmcts-judiciary", roleAssignments.get(0).getRoleName());
        assertEquals("fee-paid-judge", roleAssignments.get(1).getRoleName());
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
            }
        });
        assertEquals(workTypesFP, roleAssignments.get(1).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnDefaultTribunalJudgeSalariedRoles_IacBased() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Employment Judge");
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.setServiceCode("BFA1");
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCode(null));
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(3, roleAssignments.size());
        assertEquals("hmcts-judiciary", roleAssignments.get(0).getRoleName());
        assertEquals("case-allocator", roleAssignments.get(1).getRoleName());
        assertEquals("judge", roleAssignments.get(2).getRoleName());
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
            }
        });
        assertEquals(workTypes, roleAssignments.get(2).getAttributes().get("workTypes").asText());
    }

    @Test
    @DisplayName("Scenario 2: Non IAC Base Location with IAC Authorisation")
    void shouldReturnDefaultTribunalJudgeFeePaidRoles_IacBased() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Judge");
            judicialAccessProfile.setAppointmentType("fee paid");
            judicialAccessProfile.setServiceCode("");
            judicialAccessProfile.setTicketCodes(List.of("374", "375", "376"));
            judicialAccessProfile.getAuthorisations().forEach(a -> {
                a.setServiceCode("BFA1");
                a.setStartDate(LocalDateTime.now().minusMonths(20));
            });
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2, roleAssignments.size());
        assertEquals("hmcts-judiciary", roleAssignments.get(0).getRoleName());
        assertEquals("fee-paid-judge", roleAssignments.get(1).getRoleName());
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[374, 375, 376, 373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
            }
        });
        assertEquals(workTypesFP, roleAssignments.get(1).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldNotReturnPresidentRoles_disabledFlag() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("President of Tribunal");
            judicialAccessProfile.setServiceCode("BFA1");
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", false));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    @Test
    void shouldReturnRoles_withPresidentAppointment_withResidentRole_withIAC() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("President of Tribunal");
            judicialAccessProfile.setServiceCode("BFA1");
            judicialAccessProfile.setRoles(List.of("Resident Immigration Judge"));
            judicialAccessProfile.setTicketCodes(List.of("375"));
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCode(null));

        });


        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(10, roleAssignments.size());
        assertEquals("senior-judge", roleAssignments.get(0).getRoleName());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("senior-judge", "senior-judge", "hmcts-judiciary", "hmcts-judiciary",
                        "leadership-judge", "case-allocator", "case-allocator", "task-supervisor", "judge", "judge"));
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());

            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[375, 373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
            }
        });
    }

    @Test
    @DisplayName("Scenario 2: Missing Service code in Appointment")
    void shouldCreateAssignmentsWhenAppointmentServiceCodeIsMissing() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Employment Judge");
            judicialAccessProfile.setAppointmentType("fee paid");
            judicialAccessProfile.setServiceCode(null);
            judicialAccessProfile.setAuthorisations(
                    List.of(Authorisation.builder().serviceCode("BFA1").build(),
                            Authorisation.builder().serviceCode("SSCS").build(),
                            Authorisation.builder().serviceCode("Dummy").build()
                    ));
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2, roleAssignments.size());
        assertEquals("hmcts-judiciary", roleAssignments.get(0).getRoleName());
        assertEquals("fee-paid-judge", roleAssignments.get(1).getRoleName());
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
            }
        });
        assertEquals(workTypesFP, roleAssignments.get(1).getAttributes().get("workTypes").asText());
    }

    @Test
    @DisplayName("Scenario 3b: Tribunal Judge Salaried")
    void shouldReturnTribunalJudgeSalariedRoles_withIAC() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Judge");
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.setServiceCode("BFA1");
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCode(null));
        });
        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment("Tribunal Judge");
        profile.setAppointmentType("SPTW");
        judicialAccessProfiles.add(profile);

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(6, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("hmcts-judiciary", "case-allocator", "judge", "hmcts-judiciary",
                        "case-allocator", "judge"));
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[375]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
            }
        });
    }

    @Test
    @DisplayName("Scenario 5,9: Employment Judge Fees Paid Without Service Code.")
    void shouldReturnEmploymentFeePaidJudgeRoles_withAuthorisation() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Judge");
            judicialAccessProfile.setAppointmentType("fee paid");
            judicialAccessProfile.setServiceCode(null);
        });

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment("Employment Judge");
        profile.setAppointmentType("fee paid");
        profile.setServiceCode(null);
        judicialAccessProfiles.add(profile);

        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().userId(UUID.randomUUID().toString()).serviceCode("BFA1").build(),
                        Authorisation.builder().userId(UUID.randomUUID().toString()).serviceCode("SSCS").build(),
                        Authorisation.builder().userId(UUID.randomUUID().toString()).serviceCode("Dummy").build()
                )));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertEquals("hmcts-judiciary", roleAssignments.get(0).getRoleName());
        assertEquals("fee-paid-judge", roleAssignments.get(1).getRoleName());
        assertEquals("hmcts-judiciary", roleAssignments.get(2).getRoleName());
        assertEquals("fee-paid-judge", roleAssignments.get(3).getRoleName());
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
            }
        });
        assertEquals(workTypesFP, roleAssignments.get(1).getAttributes().get("workTypes").asText());
    }

    @Test
    @DisplayName("Scenario 6: Tribunal Judge SPTW Without Service Code.")
    void shouldReturnTribunalJudgeSptw_withAuthorisation() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Judge");
            judicialAccessProfile.setAppointmentType("fee paid");
            judicialAccessProfile.setServiceCode(null);
            judicialAccessProfile.setTicketCodes(List.of("101", "102", "103", "104"));
        });

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment("Tribunal Judge");
        profile.setAppointmentType("SPTW");
        profile.setServiceCode(null);
        profile.setTicketCodes(List.of("101", "102", "103", "104"));
        judicialAccessProfiles.add(profile);

        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCode("BFA1").startDate(LocalDateTime.now().minusMonths(10))
                                .endDate(LocalDateTime.now().minusMonths(5)).build(),
                        Authorisation.builder().serviceCode("BFA1").startDate(LocalDateTime.now().minusMonths(10))
                                .build(),
                        Authorisation.builder().userId(UUID.randomUUID().toString()).ticketCode("A").build(),
                        Authorisation.builder().userId(UUID.randomUUID().toString()).serviceCode("Dummy").build()
                )));


        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("hmcts-judiciary", "fee-paid-judge", "judge", "hmcts-judiciary",
                        "case-allocator"));
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[101, 102, 103, 104, 373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
            }
        });
    }

    @Test
    @DisplayName("Scenario 7: Missing Service Code in one appointment.")
    void missingServiceCodeInOneAppointment() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Judge");
            judicialAccessProfile.setAppointmentType("fee paid");
            judicialAccessProfile.setServiceCode("BFA1");
        });

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment("Employment Judge");
        profile.setAppointmentType("fee paid");
        profile.setServiceCode(null);
        judicialAccessProfiles.add(profile);

        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCode("BFA1").startDate(LocalDateTime.now().minusMonths(10))
                        .build())));
        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("hmcts-judiciary", "fee-paid-judge", "hmcts-judiciary", "fee-paid-judge"));

        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
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
    @DisplayName("Scenario 8: Judge has 2 IAC appointments")
    void judgeHavingTwoValidIacAppointments() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Deputy Upper Tribunal Judge");
            judicialAccessProfile.setAppointmentType("fee paid");
            judicialAccessProfile.setServiceCode(null);
            judicialAccessProfile.setTicketCodes(List.of("101", "102", "103"));
        });

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment("Tribunal Judge");
        profile.setAppointmentType("SPTW");
        profile.setServiceCode(null);
        profile.setTicketCodes(List.of("101", "102", "103"));
        judicialAccessProfiles.add(profile);

        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCode("BFA1").startDate(LocalDateTime.now().minusMonths(10))
                        .build())));
        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("hmcts-judiciary", "hmcts-judiciary", "fee-paid-judge", "judge", "case-allocator"));

        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[101, 102, 103, 373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
            }
        });
    }

    @Test
    @DisplayName("Scenario 10: Tribunal Judge with Single role")
    void tribunalJudgeWithSingleRole() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Judge");
            judicialAccessProfile.setAppointmentType("fee paid");
            judicialAccessProfile.setServiceCode(null);
            judicialAccessProfile.setTicketCodes(List.of("101", "102", "103", "104"));
        });

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment("Tribunal Judge");
        profile.setAppointmentType("SPTW");
        profile.setRoles(Collections.singletonList("Pool of Judges"));
        profile.setServiceCode(null);
        profile.setTicketCodes(List.of("101", "102", "103", "104"));
        judicialAccessProfiles.add(profile);

        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCode("BFA1").startDate(LocalDateTime.now().minusMonths(10))
                        .build())));
        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("hmcts-judiciary", "fee-paid-judge", "hmcts-judiciary", "case-allocator", "judge"));

        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[101, 102, 103, 104, 373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
            }
        });
    }

    @Test
    @DisplayName("Scenario 11: Tribunal Judge with Multiple role")
    void tribunalJudgeWithMultipleRole() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Judge");
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.setServiceCode("BFA1");
            judicialAccessProfile.setTicketCodes(List.of("101"));
            judicialAccessProfile.setRoles(List.of("Resident Immigration Judge",
                    "IT Liaison Judge",
                    "Pool of Judges",
                    "Tribunals Committee Member (JC)"));
        });

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment("Deputy Upper Tribunal Judge");
        profile.setAppointmentType("fee paid");
        profile.setServiceCode(null);
        profile.setTicketCodes(List.of("101"));
        judicialAccessProfiles.add(profile);

        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCode("BFA1").startDate(LocalDateTime.now().minusMonths(10))
                        .build())));
        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(11, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("hmcts-judiciary", "senior-judge", "hmcts-judiciary",
                        "case-allocator", "judge", "leadership-judge",
                        "case-allocator", "task-supervisor",
                        "judge", "fee-paid-judge", "hmcts-judiciary"));

        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[101, 373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
            }
        });
    }

    @Test
    @DisplayName("Scenario 12: Employment Judge with Multiple role")
    void employmentJudgeWithMultipleRole() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Employment Judge");
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.setServiceCode("BFA1");
            judicialAccessProfile.setRoles(List.of("Diversity Role Models",
                    "Diversity and Community Relations Judges",
                    "Pool of Judges"));
        });

        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCode("BFA1").ticketCode("373")
                        .startDate(LocalDateTime.now().minusMonths(10)).build())));
        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(3, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("case-allocator", "judge", "hmcts-judiciary"));

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
    @DisplayName("Scenario 13: Appointment is Regional Tribunal Judge")
    void appointmentIsRegionalTribunalJudge() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Regional Tribunal Judge");
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.setServiceCode("BFA1");
            judicialAccessProfile.setRoles(List.of("Resident Immigration Judge"));
        });

        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCode("BFA1").ticketCode("373")
                        .startDate(LocalDateTime.now().minusMonths(10)).build())));
        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(6, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("case-allocator", "judge", "hmcts-judiciary",
                        "leadership-judge", "senior-judge", "task-supervisor"));

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
    @DisplayName("Scenario 14: Judge has two Appointments and same appointment type but no IAC mapping available")
    void noIacMappingForJudge() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Deputy Upper Tribunal Judge");
            judicialAccessProfile.setAppointmentType("fee paid");
            judicialAccessProfile.setServiceCode("BFA1");
        });

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointment("Employment Judge");
        profile.setAppointmentType("fee paid");
        profile.setServiceCode("");
        judicialAccessProfiles.add(profile);

        judicialAccessProfiles.forEach(profiles -> profiles.setAuthorisations(
                List.of(Authorisation.builder().serviceCode("BFA1").ticketCode("373")
                        .startDate(LocalDateTime.now().minusMonths(10)).build())));
        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_jrd_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("fee-paid-judge", "hmcts-judiciary", "fee-paid-judge", "hmcts-judiciary"));

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
}