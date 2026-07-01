package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DroolDivorceStaffOrgRoleMappingTest extends DroolBase {

    private static final String DIVORCE = "DIVORCE";
    private static final String SERVICE_CODE = "ABA2";

    static Map<String, String> expectedRoleNameWorkTypesMap = new HashMap<>();

    static {
        expectedRoleNameWorkTypesMap.put("ctsc-team-leader",
                "hearing_work, routine_work, decision_making_work, applications, review_case, evidence");
        expectedRoleNameWorkTypesMap.put("ctsc",
                "hearing_work, routine_work, applications, review_case, evidence");
        expectedRoleNameWorkTypesMap.put("specific-access-approver-ctsc", "access_requests");
    }

    @ParameterizedTest
    @CsvSource({
        "9,'ctsc-team-leader,ctsc,hmcts-ctsc,specific-access-approver-ctsc',N,N",
        "9,'ctsc-team-leader,ctsc,hmcts-ctsc,specific-access-approver-ctsc,task-supervisor',Y,N",
        "9,'ctsc-team-leader,ctsc,hmcts-ctsc,specific-access-approver-ctsc,case-allocator',N,Y",
        "9,'ctsc-team-leader,ctsc,hmcts-ctsc,specific-access-approver-ctsc,task-supervisor,case-allocator',Y,Y",
        "10,'ctsc,hmcts-ctsc',N,N",
        "10,'ctsc,hmcts-ctsc,task-supervisor',Y,N",
        "10,'ctsc,hmcts-ctsc,case-allocator',N,Y",
        "10,'ctsc,hmcts-ctsc,task-supervisor,case-allocator',Y,Y"
    })
    void shouldReturnDivorceCtscMappings(String roleId,
                                         String expectedRoles,
                                         String taskSupervisorFlag,
                                         String caseAllocatorFlag) {
        allProfiles.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(SERVICE_CODE);
        cap.setSuspended(false);
        cap.setRoleId(roleId);
        cap.setTaskSupervisorFlag(taskSupervisorFlag);
        cap.setCaseAllocatorFlag(caseAllocatorFlag);
        allProfiles.add(cap);

        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction(DIVORCE, true));

        String[] roleNames = StringUtils.isEmpty(expectedRoles) ? new String[0] : expectedRoles.split(",");
        assertEquals(roleNames.length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNames));
        roleAssignments.forEach(r -> {
            assertEquals(RoleCategory.CTSC, r.getRoleCategory());
            assertEquals(RoleType.ORGANISATION, r.getRoleType());
            assertEquals(cap.getId(), r.getActorId());
        });

        roleAssignments.forEach(r -> {
            if (r.getRoleName().equals("hmcts-ctsc")) {
                assertNull(r.getAttributes().get("jurisdiction"));
                assertEquals(Classification.PRIVATE, r.getClassification());
                assertEquals(GrantType.BASIC, r.getGrantType());
            } else {
                assertEquals(DIVORCE, r.getAttributes().get("jurisdiction").asText());
                assertEquals(Classification.PUBLIC, r.getClassification());
                assertEquals(GrantType.STANDARD, r.getGrantType());
                assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
            }
            if (expectedRoleNameWorkTypesMap.containsKey(r.getRoleName())) {
                String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());
                String actualWorkTypes = null;
                if (r.getAttributes().get("workTypes") != null) {
                    actualWorkTypes = r.getAttributes().get("workTypes").asText();
                }
                assertEquals(expectedWorkTypes, actualWorkTypes);
            } else {
                assertFalse(r.getAttributes().containsKey("workTypes"));
            }
        });
    }

    @Test
    void shouldNotReturnDivorceRolesWhenFeatureFlagIsOff() {
        allProfiles.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(SERVICE_CODE);
        cap.setSuspended(false);
        cap.setRoleId("9");
        cap.setTaskSupervisorFlag("Y");
        cap.setCaseAllocatorFlag("Y");
        allProfiles.add(cap);

        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction(DIVORCE, false));

        assertTrue(roleAssignments.isEmpty());
    }

    @Test
    void shouldNotReturnOrgRolesForCaseWorker_with_suspendedProfile() {
        allProfiles.clear();
        IntStream.range(1, 11).forEach(roleId ->
                allProfiles.add(TestDataBuilder.buildUserAccessProfile(roleId + "", true)));

        allProfiles.forEach(userAccessProfile -> {
            userAccessProfile.setServiceCode(SERVICE_CODE);
            userAccessProfile.setCaseAllocatorFlag("Y");
            userAccessProfile.setTaskSupervisorFlag("Y");
        });

        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction(DIVORCE, true));

        assertTrue(roleAssignments.isEmpty());
    }

}
