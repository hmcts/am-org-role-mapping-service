package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.ROLE_ASSIGNMENTS_RESULTS_KEY;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.VarargsAggregator;

@RunWith(MockitoJUnitRunner.class)
class DroolJudicialRoleMappingCivilTest extends DroolBase {
    @ParameterizedTest
    @CsvSource({
            "CIVIL District Judge-Salaried,judge",
            "CIVIL Presiding Judge-Salaried,judge",
            "CIVIL Resident Judge-Salaried,judge",
            "CIVIL Circuit Judge-Salaried,circuit-judge",
            "CIVIL Specialist Circuit Judge-Salaried,circuit-judge",
            "CIVIL Senior Circuit Judge-Salaried,circuit-judge",
            "CIVIL High Court Judge-Salaried,circuit-judge"
    })
    void shouldReturnSalariedRoles(String setOffice, String roleNameOutput) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("civil_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2, roleAssignments.size());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput, "hmcts-judiciary"));
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(r -> {
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            if (!r.getRoleName().contains("hmcts")) {
                assertEquals(regionId, r.getAttributes().get("region").asText());
            }
        });

    }

    @ParameterizedTest
    @CsvSource({
            "CIVIL Deputy District Judge-Fee-Paid,fee-paid-judge",
            "CIVIL Deputy District Judge - Sitting in Retirement-Fee-Paid,fee-paid-judge",
            "CIVIL Recorder-Fee-Paid,fee-paid-judge"
    })
    void shouldReturnFeePaidRoles(String setOffice, String roleNameOutput) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("civil_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(3, roleAssignments.size());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(2).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput, "judge","hmcts-judiciary"));
        roleAssignments.forEach(r -> assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText()));
    }

    @ParameterizedTest
    @CsvSource({
            "CIVIL Deputy Circuit Judge-Fee-Paid,fee-paid-judge"
    })
    void shouldReturnCircuitJudgeRoles(String setOffice, String roleNameOutput) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("civil_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(3, roleAssignments.size());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(2).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput, "circuit-judge","hmcts-judiciary"));
        roleAssignments.forEach(r -> assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText()));
    }

    @ParameterizedTest
    @CsvSource({
            "CIVIL Designated Civil Judge-Salaried,hmcts-judiciary,leadership-judge,task-supervisor,case-allocator"
    })
    void shouldReturnHmctsJudiciaryRoles(String setOffice,
                                         @AggregateWith(VarargsAggregator.class) String[] roleNameOutput) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("civil_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput));
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(r -> {
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            if (!r.getRoleName().contains("hmcts")) {
                assertEquals(regionId, r.getAttributes().get("region").asText());
            }
        });
    }
}
