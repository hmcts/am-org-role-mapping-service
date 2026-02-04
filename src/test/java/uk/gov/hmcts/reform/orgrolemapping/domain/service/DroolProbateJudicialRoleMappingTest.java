package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
class DroolProbateJudicialRoleMappingTest extends DroolBase {

    String workTypes = "hearing_work,upper_tribunal,decision_making_work,applications";
    String workTypesFP = "hearing_work,decision_making_work,applications";
    String workTypesAccess = "hearing_work,upper_tribunal,decision_making_work,applications,access_requests";

    @Test
    void shouldReturnFeePaidTribunalJudgeRoles() {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("IAC Tribunal Judge (Fee-Paid)"));

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags());

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
