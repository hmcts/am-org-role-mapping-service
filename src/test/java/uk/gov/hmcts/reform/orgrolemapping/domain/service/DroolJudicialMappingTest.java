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
class DroolJudicialMappingTest extends DroolBase {

    String workTypes = "hearing_work,upper_tribunal,decision_making_work,applications";
    String workTypesFP = "hearing_work,decision_making_work,applications";
    String workTypesAccess = "hearing_work,upper_tribunal,decision_making_work,applications,access_requests";

    @Test
    void shouldReturnPresidentRoles() {

        judicialOfficeHolders.forEach(judicialOfficeHolder -> {
            judicialOfficeHolder.setOffice("IAC President of Tribunals");
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
        assertEquals(4, roleAssignments.size());
        assertEquals("senior-judge",roleAssignments.get(0).getRoleName());
        assertEquals("hmcts-judiciary",roleAssignments.get(1).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(2).getRoleName());
        assertEquals("judge",roleAssignments.get(3).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(2).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(3).getActorId());
        assertEquals("Salaried", roleAssignments.get(0).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(1).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(2).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(3).getAttributes().get("contractType").asText());
        assertEquals(workTypes, roleAssignments.get(0).getAttributes().get("workTypes").asText());
        assertEquals(workTypes, roleAssignments.get(3).getAttributes().get("workTypes").asText());


    }

    @Test
    void shouldReturnResidentJudgeRoles() {

        judicialOfficeHolders.forEach(judicialOfficeHolder -> {
            judicialOfficeHolder.setOffice("IAC Resident Immigration Judge");
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
        assertEquals(6, roleAssignments.size());
        assertEquals("senior-judge",roleAssignments.get(0).getRoleName());
        assertEquals("hmcts-judiciary",roleAssignments.get(1).getRoleName());
        assertEquals("leadership-judge",roleAssignments.get(2).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(3).getRoleName());
        assertEquals("task-supervisor",roleAssignments.get(4).getRoleName());
        assertEquals("judge",roleAssignments.get(5).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(2).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(3).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(4).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(5).getActorId());
        assertEquals("Salaried", roleAssignments.get(0).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(1).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(2).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(3).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(4).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(5).getAttributes().get("contractType").asText());
        assertEquals(workTypes, roleAssignments.get(0).getAttributes().get("workTypes").asText());
        assertEquals(workTypesAccess, roleAssignments.get(2).getAttributes().get("workTypes").asText());
        assertEquals(workTypes, roleAssignments.get(5).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnImmigrationJudgeRoles() {

        judicialOfficeHolders.forEach(judicialOfficeHolder -> {
            judicialOfficeHolder.setOffice("IAC Designated Immigration Judge");
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
        assertEquals(5, roleAssignments.size());
        assertEquals("hmcts-judiciary",roleAssignments.get(0).getRoleName());
        assertEquals("leadership-judge",roleAssignments.get(1).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(2).getRoleName());
        assertEquals("task-supervisor",roleAssignments.get(3).getRoleName());
        assertEquals("judge",roleAssignments.get(4).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(2).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(3).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(4).getActorId());
        assertEquals("Salaried", roleAssignments.get(0).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(1).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(2).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(3).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(4).getAttributes().get("contractType").asText());
        assertEquals(workTypesAccess, roleAssignments.get(1).getAttributes().get("workTypes").asText());
        assertEquals(workTypes, roleAssignments.get(4).getAttributes().get("workTypes").asText());

    }

    @Test
    void shouldReturnAssistantResidentJudgeRoles() {

        judicialOfficeHolders.forEach(judicialOfficeHolder -> {
            judicialOfficeHolder.setOffice("IAC Assistant Resident Judge");
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
        assertEquals(5, roleAssignments.size());
        assertEquals("hmcts-judiciary",roleAssignments.get(0).getRoleName());
        assertEquals("leadership-judge",roleAssignments.get(1).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(2).getRoleName());
        assertEquals("task-supervisor",roleAssignments.get(3).getRoleName());
        assertEquals("judge",roleAssignments.get(4).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(2).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(3).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(4).getActorId());
        assertEquals("Salaried", roleAssignments.get(0).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(1).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(2).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(3).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(4).getAttributes().get("contractType").asText());
        assertEquals(workTypesAccess, roleAssignments.get(1).getAttributes().get("workTypes").asText());
        assertEquals(workTypes, roleAssignments.get(4).getAttributes().get("workTypes").asText());

    }

    @Test
    void shouldReturnSalariedTribunalJudgeRoles() {

        judicialOfficeHolders.forEach(judicialOfficeHolder -> {
            judicialOfficeHolder.setOffice("IAC Tribunal Judge (Salaried)");
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
        assertEquals("hmcts-judiciary",roleAssignments.get(0).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(1).getRoleName());
        assertEquals("judge",roleAssignments.get(2).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(2).getActorId());
        assertEquals("Salaried", roleAssignments.get(0).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(1).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(2).getAttributes().get("contractType").asText());
        assertEquals(workTypes, roleAssignments.get(2).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnFeePaidTribunalJudgeRoles() {

        judicialOfficeHolders.forEach(judicialOfficeHolder -> {
            judicialOfficeHolder.setOffice("IAC Tribunal Judge (Fee-Paid)");
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
        assertEquals("hmcts-judiciary",roleAssignments.get(0).getRoleName());
        assertEquals("fee-paid-judge",roleAssignments.get(1).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertEquals("Fee-Paid", roleAssignments.get(0).getAttributes().get("contractType").asText());
        assertEquals("Fee-Paid", roleAssignments.get(1).getAttributes().get("contractType").asText());
        assertEquals(workTypesFP, roleAssignments.get(1).getAttributes().get("workTypes").asText());
    }

}
