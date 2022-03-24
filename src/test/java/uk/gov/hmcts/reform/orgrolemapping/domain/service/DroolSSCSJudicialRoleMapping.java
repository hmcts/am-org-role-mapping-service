package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.ROLE_ASSIGNMENTS_RESULTS_KEY;

@RunWith(MockitoJUnitRunner.class)
public class DroolSSCSJudicialRoleMapping extends DroolBase{

    //========================= SSCS "caseworker-sscs-judge" Org role mapping ========================//
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
        System.out.println(roleAssignments);
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

    //========================= SSCS "case-allocator" Org role mapping ========================//

    @Test
    void shouldReturnTribunalMedicalRoles() {


        judicialOfficeHolders.forEach(joh -> {
            joh.setOffice("SSCS Tribunal member medical-Salaried");
            joh.setPrimaryLocation("Tribunal Location");
        });


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
        System.out.println(roleAssignments);


    }

    @Test
    void shouldReturnRegionalMedicalRoles() {

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

    //========================= SSCS "task-supervisor" Org role mapping ========================//






}
