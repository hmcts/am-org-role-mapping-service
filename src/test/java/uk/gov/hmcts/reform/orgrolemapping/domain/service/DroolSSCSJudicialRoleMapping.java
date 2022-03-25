package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.ROLE_ASSIGNMENTS_RESULTS_KEY;

@RunWith(MockitoJUnitRunner.class)
public class DroolSSCSJudicialRoleMapping extends DroolBase{

    @Test
    void shouldReturnPresidentRoles() {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("SSCS Tribunal Judge-Salaried"));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertEquals("caseworker-sscs-judge",roleAssignments.get(0).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(1).getRoleName());
        assertEquals("task-supervisor",roleAssignments.get(2).getRoleName());
        assertEquals("hmcts-judiciary",roleAssignments.get(3).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(2).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(3).getActorId());
        assertEquals("Salaried", roleAssignments.get(0).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(1).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(2).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(3).getAttributes().get("contractType").asText());

    }

    @Test
    void shouldReturnRegionalTribunalRoles() {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("SSCS Regional Tribunal Judge-Salaried"));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertEquals("caseworker-sscs-judge",roleAssignments.get(0).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(1).getRoleName());
        assertEquals("task-supervisor",roleAssignments.get(2).getRoleName());
        assertEquals("hmcts-judiciary",roleAssignments.get(3).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(2).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(3).getActorId());
        assertEquals("Salaried", roleAssignments.get(0).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(1).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(2).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(3).getAttributes().get("contractType").asText());

    }

    @Test
    void shouldReturnDistrictTribunalRoles() {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("SSCS District Tribunal Judge-Salaried"));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertEquals("caseworker-sscs-judge",roleAssignments.get(0).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(1).getRoleName());
        assertEquals("task-supervisor",roleAssignments.get(2).getRoleName());
        assertEquals("hmcts-judiciary",roleAssignments.get(3).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(2).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(3).getActorId());
        assertEquals("Salaried", roleAssignments.get(0).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(1).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(2).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(3).getAttributes().get("contractType").asText());


    }

    @Test
    void shouldReturnTribunalRoles() {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("SSCS Tribunal Judge-Salaried"));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertEquals("caseworker-sscs-judge",roleAssignments.get(0).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(1).getRoleName());
        assertEquals("task-supervisor",roleAssignments.get(2).getRoleName());
        assertEquals("hmcts-judiciary",roleAssignments.get(3).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(2).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(3).getActorId());
        assertEquals("Salaried", roleAssignments.get(0).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(1).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(2).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(3).getAttributes().get("contractType").asText());

    }

    @Test
    void shouldReturnTribunalMedicalSalariedRoles() {

        judicialOfficeHolders.forEach(joh -> {
            joh.setOffice("SSCS Tribunal member medical-Salaried");
        });


        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertEquals("case-allocator",roleAssignments.get(0).getRoleName());
        assertEquals("task-supervisor",roleAssignments.get(1).getRoleName());
        assertEquals("hmcts-judiciary",roleAssignments.get(2).getRoleName());
        assertEquals("caseworker-sscs-medical",roleAssignments.get(3).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(2).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(3).getActorId());
        assertEquals("Salaried", roleAssignments.get(0).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(1).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(2).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(3).getAttributes().get("contractType").asText());

    }

    @Test
    void shouldReturnMedicalMemberSalariedRoles() {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("SSCS Regional Medical Member-Salaried"));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        System.out.println(roleAssignments.get(0).getRoleName());
        System.out.println(roleAssignments.get(1).getRoleName());
        System.out.println(roleAssignments.get(2).getRoleName());
        System.out.println(roleAssignments.get(3).getRoleName());

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertEquals("case-allocator",roleAssignments.get(0).getRoleName());
        assertEquals("task-supervisor",roleAssignments.get(1).getRoleName());
        assertEquals("hmcts-judiciary",roleAssignments.get(2).getRoleName());
        assertEquals("caseworker-sscs-medical",roleAssignments.get(3).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(2).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(3).getActorId());
        assertEquals("Salaried", roleAssignments.get(0).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(1).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(2).getAttributes().get("contractType").asText());
        assertEquals("Salaried", roleAssignments.get(3).getAttributes().get("contractType").asText());

    }

    @Test
    void shouldReturnTribunalJudgeFeePaid() {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("SSCS Tribunal Judge-Fee Paid"));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());
        assertEquals("caseworker-sscs-judge-feepaid",roleAssignments.get(0).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals("Fee-Paid", roleAssignments.get(0).getAttributes().get("contractType").asText());

    }

    @Test
    void shouldReturnTribunalMedicalFeePaid() {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("SSCS Tribunal member medical-Fee Paid"));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());
        assertEquals("caseworker-sscs-medical-feepaid",roleAssignments.get(0).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals("Fee-Paid", roleAssignments.get(0).getAttributes().get("contractType").asText());

    }

    @Test
    void shouldReturnTribunalOptometristFeePaid() {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("SSCS Tribunal Member Optometrist-Fee Paid"));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());
        assertEquals("caseworker-sscs-medical-feepaid",roleAssignments.get(0).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals("Fee-Paid", roleAssignments.get(0).getAttributes().get("contractType").asText());

    }

    @Test
    void shouldReturnTribunalDisabilityFeePaid() {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("SSCS Tribunal member disability-Fee Paid"));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());
        assertEquals("caseworker-sscs-disability-feepaid",roleAssignments.get(0).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals("Fee-Paid", roleAssignments.get(0).getAttributes().get("contractType").asText());

    }

    @Test
    void shouldReturnTribunalMemberFeePaid() {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("SSCS Tribunal Member-Fee Paid"));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());
        assertEquals("caseworker-sscs-disability-feepaid",roleAssignments.get(0).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals("Fee-Paid", roleAssignments.get(0).getAttributes().get("contractType").asText());

    }

    @Test
    void shouldReturnTribunalLayFeePaid() {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("SSCS Tribunal Member Lay-Fee Paid"));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());
        assertEquals("caseworker-sscs-disability-feepaid",roleAssignments.get(0).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals("Fee-Paid", roleAssignments.get(0).getAttributes().get("contractType").asText());

    }

    @Test
    void shouldReturnTribunalServiceFeePaid() {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("SSCS Tribunal Member Service-Fee Paid"));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());
        assertEquals("caseworker-sscs-disability-feepaid",roleAssignments.get(0).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals("Fee-Paid", roleAssignments.get(0).getAttributes().get("contractType").asText());

    }

    @Test
    void shouldReturnTribunalFinanciallyQualifiedRole() {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("SSCS Tribunal member financially qualified"));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());
        assertEquals("caseworker-sscs-financial-feepaid",roleAssignments.get(0).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals("Fee-Paid", roleAssignments.get(0).getAttributes().get("contractType").asText());

    }


}
