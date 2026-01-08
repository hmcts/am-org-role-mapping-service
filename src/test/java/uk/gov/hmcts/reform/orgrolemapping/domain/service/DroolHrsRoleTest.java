package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

@ExtendWith(MockitoExtension.class)
class DroolHrsRoleTest extends DroolBase {
    private final List<String> expectedSkillCodes = List.of("SKILL:HRS:AA", "SKILL:HRS:AB", "SKILL:HRS:BA");

    @ParameterizedTest
    @CsvSource({
        "22,HRS,'hrs-team-leader,hrs-listener,hrs-sharer'",
        "23,HRS,'hrs-listener",
        "24,HRS,'hrs-sharer"
    })
    void shouldReturnHrsAdminMappings(String roleId, String serviceCode, String expectedRoles) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(serviceCode);
        cap.setSuspended(false);
        cap.setRoleId(roleId);
        cap.setTaskSupervisorFlag("N");
        cap.setCaseAllocatorFlag("N");
        cap.setRegionId("LDN");
        cap.setSkillCodes(expectedSkillCodes);
        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("HRS", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        roleAssignments.forEach(r -> {
            assertEquals("ADMIN", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());
        });

        roleAssignments.forEach(r -> {
            assertNotNull(r.getAttributes().get("jurisdiction"));
            assertNotNull(r.getAttributes().get("primaryLocation"));

            //assert jurisdiction
            assertEquals(r.getAttributes().get("jurisdiction").toString(), "HRS");
            //assert region
            assertNull(r.getAttributes().get("region"));
            //assert work types
            assertNull(r.getAttributes().get("workTypes"));
            //assert classification
            assertEquals(r.getClassification().toString(), "PUBLIC");
            //assert grant type
            assertEquals(r.getGrantType().toString(), "STANDARD");
            //assert ReadOnly
            assertEquals(r.isReadOnly(), false);
            //assert Skill codes
            assertEquals(r.getAuthorisations().size(),3);
            assertIterableEquals(r.getAuthorisations(), expectedSkillCodes);
        });
    }



}
