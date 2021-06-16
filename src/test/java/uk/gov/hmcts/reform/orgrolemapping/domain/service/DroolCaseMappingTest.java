package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.ROLE_ASSIGNMENTS_RESULTS_KEY;

@RunWith(MockitoJUnitRunner.class)
class DroolCaseMappingTest extends DroolBase {


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
        assertEquals(1,roleAssignments.size());
        assertEquals("tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals(usersAccessProfiles.keySet().stream().skip(1).iterator().next(),
                roleAssignments.get(0).getActorId());


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
        assertEquals(2,roleAssignments.size());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(1).getRoleName());
        assertEquals(usersAccessProfiles.keySet().stream().iterator().next(),
                roleAssignments.get(0).getActorId());
        assertEquals(usersAccessProfiles.keySet().stream().skip(1).iterator().next(),
                roleAssignments.get(1).getActorId());

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
            userAccessProfile.setRoleId("5");
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

    @NotNull
    private List<FeatureFlag> getFeatureFlags(String flagName, Boolean status) {
        return Arrays.asList(FeatureFlag.builder().flagName(flagName).status(status).build());
    }
}