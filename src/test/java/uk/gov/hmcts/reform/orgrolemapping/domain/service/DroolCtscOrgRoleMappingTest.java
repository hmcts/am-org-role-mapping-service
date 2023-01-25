package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;


@RunWith(MockitoJUnitRunner.class)
public class DroolCtscOrgRoleMappingTest extends DroolBase {

    @ParameterizedTest
    @CsvSource({
            "10,BFA1,'ctsc,hmcts-ctsc',N,N",
            "9,BFA1,'ctsc,hmcts-ctsc',N,N"
    })
    void shouldReturnIACCtscMappings(String roleId, String serviceCode, String expectedRoles,
                                     String taskSupervisorFlag, String caseAllocatorFlag) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();
        allProfiles.clear();
        List<String> skillCodes = List.of("civil", "test", "ctsc");
        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(serviceCode);
        cap.setSuspended(false);
        cap.setRoleId(roleId);
        cap.setTaskSupervisorFlag(taskSupervisorFlag);
        cap.setCaseAllocatorFlag(caseAllocatorFlag);
        cap.setSkillCodes(skillCodes);
        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        roleAssignments.forEach(r -> {
            assertEquals("CTSC", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());
            if (!r.getRoleName().contains("hmcts")) {
                assertEquals(skillCodes, r.getAuthorisations());
            }
        });

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals("IA", r.getAttributes().get("jurisdiction").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                    //assert work types
                    if (("ctsc").equals(r.getRoleName())) {
                        assertEquals("hearing_work, upper_tribunal, routine_work",
                                r.getAttributes().get("workTypes").asText());
                    }
                });
    }

}

