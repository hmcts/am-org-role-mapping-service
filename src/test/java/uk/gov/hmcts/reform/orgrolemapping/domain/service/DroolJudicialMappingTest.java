package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.ROLE_ASSIGNMENTS_RESULTS_KEY;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
class DroolJudicialMappingTest extends DroolJudicialBase {

    @Test
    void shouldReturnJudge() {

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true), true);

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1,roleAssignments.size());
        assertEquals("judge",roleAssignments.get(0).getRoleName());
    }

    @Test
    void shouldReturnPresidentRoles() {

        judicialOfficeHolders.forEach(judicialOfficeHolder -> {
            judicialOfficeHolder.setOffice("IAC President of Tribunals");
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true), false);

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertEquals("senior-judge",roleAssignments.get(0).getRoleName());
        assertEquals("hmcts-judiciary",roleAssignments.get(1).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(2).getRoleName());
        assertEquals("judge",roleAssignments.get(3).getRoleName());
    }

    @Test
    void shouldReturnResidentJudgeRoles() {

        judicialOfficeHolders.forEach(judicialOfficeHolder -> {
            judicialOfficeHolder.setOffice("IAC Resident Judge");
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true), false);

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(6, roleAssignments.size());
        assertEquals("senior-judge",roleAssignments.get(0).getRoleName());
        assertEquals("hmcts-judiciary",roleAssignments.get(1).getRoleName());
        assertEquals("leadership-judge",roleAssignments.get(2).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(3).getRoleName());
        assertEquals("task-supervisor",roleAssignments.get(4).getRoleName());
        assertEquals("judge",roleAssignments.get(5).getRoleName());
    }

    @Test
    void shouldReturnImmigrationJudgeRoles() {

        judicialOfficeHolders.forEach(judicialOfficeHolder -> {
            judicialOfficeHolder.setOffice("IAC Designated Immigration Judge");
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true), false);

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5, roleAssignments.size());
        assertEquals("hmcts-judiciary",roleAssignments.get(0).getRoleName());
        assertEquals("leadership-judge",roleAssignments.get(1).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(2).getRoleName());
        assertEquals("task-supervisor",roleAssignments.get(3).getRoleName());
        assertEquals("judge",roleAssignments.get(4).getRoleName());
    }

    @Test
    void shouldReturnAssistantResidentJudgeRoles() {

        judicialOfficeHolders.forEach(judicialOfficeHolder -> {
            judicialOfficeHolder.setOffice("IAC Assistant Resident Judge");
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true), false);

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5, roleAssignments.size());
        assertEquals("hmcts-judiciary",roleAssignments.get(0).getRoleName());
        assertEquals("leadership-judge",roleAssignments.get(1).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(2).getRoleName());
        assertEquals("task-supervisor",roleAssignments.get(3).getRoleName());
        assertEquals("judge",roleAssignments.get(4).getRoleName());
    }

    @Test
    void shouldReturnSalariedTribunalJudgeRoles() {

        judicialOfficeHolders.forEach(judicialOfficeHolder -> {
            judicialOfficeHolder.setOffice("IAC Tribunal Judge (Salaried)");
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true), false);

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(3, roleAssignments.size());
        assertEquals("hmcts-judiciary",roleAssignments.get(0).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(1).getRoleName());
        assertEquals("judge",roleAssignments.get(2).getRoleName());
    }

    @Test
    void shouldReturnFeePaidTribunalJudgeRoles() {

        judicialOfficeHolders.forEach(judicialOfficeHolder -> {
            judicialOfficeHolder.setOffice("IAC Tribunal Judge (Fee-Paid)");
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true), false);

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(3, roleAssignments.size());
        assertEquals("hmcts-judiciary",roleAssignments.get(0).getRoleName());
        assertEquals("judge",roleAssignments.get(1).getRoleName());
        assertEquals("fee-paid-judge",roleAssignments.get(2).getRoleName());
    }

}
