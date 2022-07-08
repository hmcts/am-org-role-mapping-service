package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.ROLE_ASSIGNMENTS_RESULTS_KEY;

@RunWith(MockitoJUnitRunner.class)
class  DroolCaseMappingTest extends DroolBase {

    private final String workTypes = "hearing_work, routine_work, decision_making_work, applications";

    @Test
    void shouldReturnOneSeniorCaseWorker() {

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals(usersAccessProfiles.keySet().iterator().next(),roleAssignments.get(0).getActorId());
    }

    @Test
    void shouldReturnOneCaseWorker() {

        allProfiles.forEach(userAccessProfile -> {
            if (userAccessProfile.getRoleId().equals("2")) {
                userAccessProfile.setServiceCode("BFA1");
            } else {
                userAccessProfile.setServiceCode("BFA2");
            }

        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1,roleAssignments.size());
        assertEquals("tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals(usersAccessProfiles.keySet().stream().skip(1).iterator().next(),
                roleAssignments.get(0).getActorId());
    }

    @Test
    void shouldReturnBothCaseWorker() {

        allProfiles.forEach(userAccessProfile -> {
            if (userAccessProfile.getRoleId().equals("2")) {
                userAccessProfile.setServiceCode("BFA1");
            }

        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2,roleAssignments.size());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(1).getRoleName());
        assertEquals(usersAccessProfiles.keySet().stream().iterator().next(),
                roleAssignments.get(0).getActorId());
        assertEquals(usersAccessProfiles.keySet().stream().skip(1).iterator().next(),
                roleAssignments.get(1).getActorId());
    }

    @Test
    void shouldReturnZeroCaseWorkerWrongServiceCode() {

        allProfiles.forEach(userAccessProfile -> {
            if (userAccessProfile.getRoleId().equals("1")) {
                userAccessProfile.setServiceCode("BFA2");
            }
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_0", true));

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
    void shouldReturnZeroCaseWorkerWrongRoleId() {

        allProfiles.forEach(userAccessProfile -> {
            userAccessProfile.setRoleId("5");
            if (userAccessProfile.getServiceCode().equals("BFA2")) {
                userAccessProfile.setServiceCode("BFA1");
            }
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_0", true));

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
    void shouldReturnZeroCaseWorkerWrongFlag() {

        allProfiles.forEach(userAccessProfile -> {
            userAccessProfile.setSuspended(true);
            if (userAccessProfile.getServiceCode().equals("BFA2")) {
                userAccessProfile.setServiceCode("BFA1");
            }
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_0", true));

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
    void shouldReturnOneSeniorCaseWorkerForNewRule() {

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals(usersAccessProfiles.keySet().iterator().next(),roleAssignments.get(0).getActorId());
    }

    @Test
    void shouldReturnOneCaseWorkerForNewRule() {

        allProfiles.forEach(userAccessProfile -> {
            if (userAccessProfile.getRoleId().equals("2")) {
                userAccessProfile.setServiceCode("BFA1");
            } else {
                userAccessProfile.setServiceCode("BFA2");
            }

        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2,roleAssignments.size());
        assertEquals("tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals("hmcts-legal-operations",roleAssignments.get(1).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(1).getRoleCategory());
        assertEquals(usersAccessProfiles.keySet().stream().skip(1).iterator().next(),
                roleAssignments.get(0).getActorId());
        assertEquals(workTypes,
                roleAssignments.get(0).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnCaseAllocatorForNewRule() {
        Iterator<CaseWorkerAccessProfile> profiles = allProfiles.iterator();
        while (profiles.hasNext()) {
            CaseWorkerAccessProfile userAccessProfile = profiles.next();
            if (userAccessProfile.getRoleId().equals("2")) {
                profiles.remove();
            }
            if (userAccessProfile.getRoleId().equals("1")) {
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("Y");
                userAccessProfile.setTaskSupervisorFlag("X");
                userAccessProfile.setSuspended(false);
            }
        }

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4,roleAssignments.size());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(1).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(2).getRoleName());
        assertEquals("hmcts-legal-operations",roleAssignments.get(3).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(3).getRoleCategory());
        assertEquals(usersAccessProfiles.keySet().stream().iterator().next(),
                roleAssignments.get(0).getActorId());

        assertEquals(workTypes,
                roleAssignments.get(1).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnDoubleCaseAllocatorRolesForNewRule() {

        allProfiles.forEach(userAccessProfile -> {
            if (userAccessProfile.getRoleId().equals("2")) {
                userAccessProfile.setRoleId("1");
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("Y");
                userAccessProfile.setTaskSupervisorFlag("X");
                userAccessProfile.setSuspended(false);
            }
            if (userAccessProfile.getRoleId().equals("1")) {
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("Y");
                userAccessProfile.setTaskSupervisorFlag("X");
                userAccessProfile.setSuspended(false);
            }
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(8,roleAssignments.size());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(1).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(2).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(3).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(4).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(4).getRoleCategory());
        assertEquals("case-allocator",roleAssignments.get(5).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(5).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(6).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(6).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(7).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(7).getRoleCategory());
        Assertions.assertThat(usersAccessProfiles.keySet()).contains(
                roleAssignments.get(0).getActorId());

        assertEquals(workTypes,
                roleAssignments.get(2).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(3).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnTaskSupervisorForNewRule() {

        Iterator<CaseWorkerAccessProfile> profiles = allProfiles.iterator();
        while (profiles.hasNext()) {
            CaseWorkerAccessProfile userAccessProfile = profiles.next();
            if (userAccessProfile.getRoleId().equals("2")) {
                profiles.remove();
            }
            if (userAccessProfile.getRoleId().equals("1")) {
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("X");
                userAccessProfile.setTaskSupervisorFlag("Y");
                userAccessProfile.setSuspended(false);
            }
        }

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4,roleAssignments.size());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(1).getRoleName());
        assertEquals("task-supervisor",roleAssignments.get(2).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(2).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(3).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(3).getRoleCategory());
        assertEquals(usersAccessProfiles.keySet().stream().iterator().next(),
                roleAssignments.get(0).getActorId());

        assertEquals(workTypes,
                roleAssignments.get(1).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnDoubleTaskSupervisorForNewRule() {

        allProfiles.forEach(userAccessProfile -> {
            if (userAccessProfile.getRoleId().equals("2")) {
                userAccessProfile.setRoleId("1");
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("X");
                userAccessProfile.setTaskSupervisorFlag("Y");
                userAccessProfile.setSuspended(false);
            }
            if (userAccessProfile.getRoleId().equals("1")) {
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("X");
                userAccessProfile.setTaskSupervisorFlag("Y");
                userAccessProfile.setSuspended(false);
            }
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(8,roleAssignments.size());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(1).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(2).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(3).getRoleName());
        assertEquals("task-supervisor",roleAssignments.get(4).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(4).getRoleCategory());
        assertEquals("task-supervisor",roleAssignments.get(5).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(5).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(6).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(6).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(7).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(7).getRoleCategory());
        Assertions.assertThat(usersAccessProfiles.keySet()).contains(
                roleAssignments.get(0).getActorId());

        assertEquals(workTypes,
                roleAssignments.get(2).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(3).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnTribunalWorkerAndCaseAllocatorRolesForNewRule() {

        allProfiles.forEach(userAccessProfile -> {
            if (userAccessProfile.getRoleId().equals("2")) {
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("X");
                userAccessProfile.setTaskSupervisorFlag("Y");
                userAccessProfile.setSuspended(false);
            }
            if (userAccessProfile.getRoleId().equals("1")) {
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("Y");
                userAccessProfile.setTaskSupervisorFlag("X");
                userAccessProfile.setSuspended(false);
            }
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(7,roleAssignments.size());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(1).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(2).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(3).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(3).getRoleCategory());
        assertEquals("task-supervisor",roleAssignments.get(4).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(4).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(5).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(5).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(6).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(6).getRoleCategory());
        assertEquals(usersAccessProfiles.keySet().stream().iterator().next(),
                roleAssignments.get(0).getActorId());
        Assertions.assertThat(usersAccessProfiles.keySet()).contains(
                roleAssignments.get(2).getActorId());

        assertEquals(workTypes,
                roleAssignments.get(1).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(2).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnTaskSupervisorCaseAllocatorForNewRule() {
        Iterator<CaseWorkerAccessProfile> profiles = allProfiles.iterator();
        while (profiles.hasNext()) {
            CaseWorkerAccessProfile userAccessProfile = profiles.next();
            if (userAccessProfile.getRoleId().equals("2")) {
                profiles.remove();
            }
            if (userAccessProfile.getRoleId().equals("1")) {
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("Y");
                userAccessProfile.setTaskSupervisorFlag("Y");
                userAccessProfile.setSuspended(false);
            }
        }

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5,roleAssignments.size());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(1).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(2).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(2).getRoleCategory());
        assertEquals("task-supervisor",roleAssignments.get(3).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(3).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(4).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(4).getRoleCategory());
        assertEquals(usersAccessProfiles.keySet().stream().iterator().next(),
                roleAssignments.get(0).getActorId());

        assertEquals(workTypes,
                roleAssignments.get(1).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnDoubleAllocatorAndSupervisorRolesForNewRule() {

        allProfiles.forEach(userAccessProfile -> {
            if (userAccessProfile.getRoleId().equals("2")) {
                userAccessProfile.setRoleId("1");
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("Y");
                userAccessProfile.setTaskSupervisorFlag("Y");
                userAccessProfile.setSuspended(false);
            }
            if (userAccessProfile.getRoleId().equals("1")) {
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("Y");
                userAccessProfile.setTaskSupervisorFlag("Y");
                userAccessProfile.setSuspended(false);
            }
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(10,roleAssignments.size());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(1).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(2).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(3).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(4).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(4).getRoleCategory());
        assertEquals("case-allocator",roleAssignments.get(5).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(5).getRoleCategory());
        assertEquals("task-supervisor",roleAssignments.get(6).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(6).getRoleCategory());
        assertEquals("task-supervisor",roleAssignments.get(7).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(7).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(8).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(8).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(9).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(9).getRoleCategory());
        Assertions.assertThat(usersAccessProfiles.keySet()).contains(
                roleAssignments.get(0).getActorId());

        assertEquals(workTypes,
                roleAssignments.get(2).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(3).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnBothCaseWorkerForNewRule() {

        allProfiles.forEach(userAccessProfile -> {
            if (userAccessProfile.getRoleId().equals("2")) {
                userAccessProfile.setServiceCode("BFA1");
            }
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5,roleAssignments.size());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(1).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(2).getRoleName());
        assertEquals("hmcts-legal-operations",roleAssignments.get(3).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(3).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(4).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(4).getRoleCategory());
        Assertions.assertThat(usersAccessProfiles.keySet()).contains(
                roleAssignments.get(1).getActorId());
        Assertions.assertThat(usersAccessProfiles.keySet().stream()).contains(
                roleAssignments.get(2).getActorId());

        assertEquals(workTypes,
                roleAssignments.get(1).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(2).getAttributes().get("workTypes").asText());

    }

    @Test
    void shouldReturnZeroCaseWorkerWrongServiceCodeForNewRule() {

        allProfiles.forEach(userAccessProfile -> {
            if (userAccessProfile.getRoleId().equals("1")) {
                userAccessProfile.setServiceCode("BFA2");
            }
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

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
    void shouldReturnZeroCaseWorkerWrongRoleIdForNewRule() {

        allProfiles.forEach(userAccessProfile -> {
            userAccessProfile.setRoleId("11");
            if (userAccessProfile.getServiceCode().equals("BFA2")) {
                userAccessProfile.setServiceCode("BFA1");
            }
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

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
    void shouldReturnZeroCaseWorkerWrongFlagForNewRule() {

        allProfiles.forEach(userAccessProfile -> {
            userAccessProfile.setSuspended(true);
            if (userAccessProfile.getServiceCode().equals("BFA2")) {
                userAccessProfile.setServiceCode("BFA1");
            }
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertTrue(roleAssignments.isEmpty());

    }

}