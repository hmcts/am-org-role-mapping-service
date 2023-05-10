package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
class DroolSscsJudicialRoleMappingTest extends DroolBase {

    @ParameterizedTest
    @CsvSource({
            "SSCS President of Tribunal-Salaried,'leadership-judge,judge,case-allocator,task-supervisor,specific-access-approver-judiciary,hmcts-judiciary'",
            "SSCS Regional Tribunal Judge-Salaried,'leadership-judge,judge,case-allocator,task-supervisor,specific-access-approver-judiciary,hmcts-judiciary'",
            "SSCS Tribunal Judge-Salaried,'judge,hmcts-judiciary'",
            "SSCS Tribunal Member Medical-Salaried,'medical,hmcts-judiciary'",

    })
    void shouldReturnSalariedRoles(String setOffice, String expectedRoles) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        boolean allAssignmentsHaveFeePaidContractType = roleAssignments.stream()
                .allMatch(ra -> ra.getAttributes().get("contractType").asText().equals("Salaried"));
        assertTrue(allAssignmentsHaveFeePaidContractType);

        roleAssignments.forEach(r -> {
            if (!r.getRoleName().equalsIgnoreCase("hmcts-judiciary")) {
                assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());
            }
            //assert work types
            if (("leadership-judge").equals(r.getRoleName())) {
                assertNull(r.getAttributes().get("workTypes"));
            } else if (("judge").equals(r.getRoleName())) {
                assertThat(r.getAttributes().get("workTypes").asText().split(","),
                        arrayContainingInAnyOrder("pre_hearing_work", "hearing_work",
                                "post_hearing_work", "decision_making_work",
                                "routine_work", "priority"));
            } else if (("case-allocator").equals(r.getRoleName())) {
                assertNull(r.getAttributes().get("workTypes"));
            } else if (("task-supervisor").equals(r.getRoleName())) {
                assertNull(r.getAttributes().get("workTypes"));
            } else if (("specific-access-approver-judiciary").equals(r.getRoleName())) {
                assertThat(r.getAttributes().get("workTypes").asText().split(","),
                        arrayContainingInAnyOrder("access_requests"));
            } else if (("hmcts-judiciary").equals(r.getRoleName())) {
                assertNull(r.getAttributes().get("workTypes"));
            } else if (("medical").equals(r.getRoleName())) {
                assertThat(r.getAttributes().get("workTypes").asText().split(","),
                        arrayContainingInAnyOrder("hearing_work", "priority"));
            }
        });
    }

    @ParameterizedTest
    @CsvSource({
            "SSCS Tribunal Judge-Fee Paid,'fee-paid-judge,judge,hmcts-judiciary'",
            "SSCS Tribunal Member Medical-Fee Paid,'fee-paid-medical,hmcts-judiciary'",
            "SSCS Tribunal Member Optometrist-Fee Paid,'fee-paid-medical,hmcts-judiciary'",
            "SSCS Tribunal Member Disability-Fee Paid,'fee-paid-disability,hmcts-judiciary'",
            "SSCS Tribunal Member-Fee Paid,'fee-paid-tribunal-member,hmcts-judiciary'",
            "SSCS Tribunal Member Lay-Fee Paid,'fee-paid-tribunal-member,hmcts-judiciary'",
            "SSCS Tribunal Member Service-Fee Paid,'fee-paid-tribunal-member,hmcts-judiciary'",
            "SSCS Tribunal Member Financially Qualified,'fee-paid-financial,hmcts-judiciary'"

    })
    void shouldReturnFeePaidRoles(String setOffice, String expectedRoles) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        boolean allAssignmentsHaveFeePaidContractType = roleAssignments.stream()
                .allMatch(ra -> ra.getAttributes().get("contractType").asText().equals("Fee-Paid"));
        assertTrue(allAssignmentsHaveFeePaidContractType);

        roleAssignments.forEach(r -> {
                    if (!r.getRoleName().equalsIgnoreCase("hmcts-judiciary")) {
                        assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());
                    }
                    //assert work types
                    if (("fee-paid-judge").equals(r.getRoleName())) {
                        assertThat(r.getAttributes().get("workTypes").asText().split(","),
                                arrayContainingInAnyOrder("pre_hearing_work", "hearing_work",
                                        "post_hearing_work", "decision_making_work",
                                        "routine_work", "priority"));
                    } else if (("judge").equals(r.getRoleName())) {
                        assertThat(r.getAttributes().get("workTypes").asText().split(","),
                                arrayContainingInAnyOrder("pre_hearing_work", "hearing_work",
                                        "post_hearing_work", "decision_making_work",
                                        "routine_work", "priority"));
                    } else if (("hmcts-judiciary").equals(r.getRoleName())) {
                        assertNull(r.getAttributes().get("workTypes"));
                    } else if (("fee-paid-medical").equals(r.getRoleName())) {
                        assertThat(r.getAttributes().get("workTypes").asText().split(","),
                                arrayContainingInAnyOrder("hearing_work", "priority"));
                    } else if (("fee-paid-disability").equals(r.getRoleName())) {
                        assertThat(r.getAttributes().get("workTypes").asText().split(","),
                                arrayContainingInAnyOrder("hearing_work", "priority"));
                    } else if (("regional-centre-team-leader").equals(r.getRoleName())) {
                        assertNull(r.getAttributes().get("workTypes"));
                    } else if (("fee-paid-tribunal-member").equals(r.getRoleName())) {
                        assertThat(r.getAttributes().get("workTypes").asText().split(","),
                                arrayContainingInAnyOrder("hearing_work", "priority"));
                    } else if (("fee-paid-financial").equals(r.getRoleName())) {
                        assertThat(r.getAttributes().get("workTypes").asText().split(","),
                                arrayContainingInAnyOrder("hearing_work", "priority"));
                    }
                });
    }


}
