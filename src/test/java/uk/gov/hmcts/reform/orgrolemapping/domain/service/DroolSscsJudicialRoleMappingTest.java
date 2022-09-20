package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.hamcrest.collection.ArrayMatching;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.VarargsAggregator;

@RunWith(MockitoJUnitRunner.class)
class DroolSscsJudicialRoleMappingTest extends DroolBase {

    @ParameterizedTest
    @CsvSource({
        "SSCS Tribunal Judge-Salaried,judge,case-allocator,task-supervisor,hmcts-judiciary,judge,case-allocator,"
                 + "task-supervisor",
        "SSCS Regional Tribunal Judge-Salaried,judge,case-allocator,task-supervisor,hmcts-judiciary,judge,"
                + "case-allocator,task-supervisor",
        "SSCS District Tribunal Judge-Salaried,judge,case-allocator,task-supervisor,hmcts-judiciary,judge,"
                + "case-allocator,task-supervisor",
        "SSCS Tribunal Judge-Salaried,judge,case-allocator,task-supervisor,hmcts-judiciary,judge,case-allocator,"
                + "task-supervisor",
        "SSCS Tribunal Member Medical-Salaried,medical,case-allocator,task-supervisor,hmcts-judiciary,medical,"
                + "case-allocator,task-supervisor",
        "SSCS Regional Medical Member-Salaried,medical,case-allocator,task-supervisor,hmcts-judiciary,medical,"
                + "case-allocator,task-supervisor"
    })
    void shouldReturnSalariedRoles(String setOffice, @AggregateWith(VarargsAggregator.class) String[] roleNameOutput) {

        judicialOfficeHolders.forEach(joh -> {
            joh.setOffice(setOffice);
            joh.setRegionId("7");
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(7, roleAssignments.size());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(1).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(2).getActorId());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(3).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput));
        roleAssignments.forEach(r -> {
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            if (!"hmcts-judiciary".equals(r.getRoleName())) {
                assertThat(new String[]{"7", "6"},
                        ArrayMatching.hasItemInArray(r.getAttributes().get("region").asText()));
            }
        });

    }

    @ParameterizedTest
    @CsvSource({
            "SSCS Tribunal Judge-Fee Paid,fee-paid-judge",
            "SSCS Tribunal Member Medical-Fee Paid,fee-paid-medical",
            "SSCS Tribunal Member Optometrist-Fee Paid,fee-paid-medical",
            "SSCS Tribunal Member Disability-Fee Paid,fee-paid-disability",
            "SSCS Tribunal Member-Fee Paid,fee-paid-disability",
            "SSCS Tribunal Member Lay-Fee Paid,fee-paid-disability",
            "SSCS Tribunal Member Service-Fee Paid,fee-paid-disability",
            "SSCS Tribunal Member Financially Qualified,fee-paid-financial"

    })
    void shouldReturnFeePaidRoles(String setOffice, String roleNameOutput) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());
        assertEquals(roleNameOutput,roleAssignments.get(0).getRoleName());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertEquals("Fee-Paid", roleAssignments.get(0).getAttributes().get("contractType").asText());

    }


}
