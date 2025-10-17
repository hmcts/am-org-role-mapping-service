package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

@ExtendWith(MockitoExtension.class)
class DroolAdminOrgRoleMappingTest extends DroolBase {

    static Map<String, String> expectedRoleNameWorkTypesMap = new HashMap<>();

    static {
        expectedRoleNameWorkTypesMap.put("hmcts-admin", null);
        expectedRoleNameWorkTypesMap.put("hearing-centre-admin", "hearing_work, upper_tribunal, routine_work, "
                + "review_case");
        expectedRoleNameWorkTypesMap.put("national-business-centre", "hearing_work, upper_tribunal, routine_work");
        expectedRoleNameWorkTypesMap.put("case-allocator", null);
        expectedRoleNameWorkTypesMap.put("task-supervisor", null);
    }

    static Stream<Arguments> generateData() {
        return Stream.of(
                Arguments.of("3", Arrays.asList("hmcts-admin", "hearing-centre-admin")),
                Arguments.of("4", Arrays.asList("hmcts-admin", "hearing-centre-admin")),
                Arguments.of("5", Arrays.asList("hmcts-admin", "hearing-centre-admin")),
                Arguments.of("6", Arrays.asList("hmcts-admin", "national-business-centre")),
                Arguments.of("7", Arrays.asList("hmcts-admin", "national-business-centre")),
                Arguments.of("8", Arrays.asList("hmcts-admin", "national-business-centre"))
        );
    }

    @ParameterizedTest
    @MethodSource("generateData")
    void verifyIacAdminProfileRoleCreation(String roleId, List<String> roleNames) {
        allProfiles.clear();
        allProfiles.add(TestDataBuilder.buildUserAccessProfile(roleId, false));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("IAC", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(roleNames.size(), roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNames.toArray()));
        assertEquals(RoleCategory.ADMIN,roleAssignments.get(0).getRoleCategory());
        assertEquals(RoleCategory.ADMIN,roleAssignments.get(1).getRoleCategory());
        assertEquals(usersAccessProfiles.keySet().stream().iterator().next(),
                roleAssignments.get(0).getActorId());

        for (RoleAssignment r : roleAssignments) {
            String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());
            String actualWorkTypes = null;
            if (r.getAttributes().get("workTypes") != null) {
                actualWorkTypes = r.getAttributes().get("workTypes").asText();
            }
            assertEquals(expectedWorkTypes, actualWorkTypes);
        }
    }

    @Test
    void shouldReturnAllAdminOrgRolesForRoleId_3_to_10() {
        allProfiles.clear();
        IntStream.range(3, 9).forEach(roleId ->
                allProfiles.add(TestDataBuilder.buildUserAccessProfile(roleId + "", false)));
        List<String> skillCodes = List.of("IA", "test", "ctsc");
        allProfiles.forEach(userAccessProfile -> {
            userAccessProfile.setCaseAllocatorFlag("Y");
            userAccessProfile.setTaskSupervisorFlag("Y");
            userAccessProfile.setSkillCodes(skillCodes);
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("IAC", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5,roleAssignments.size());
        IntStream.of(0).forEach(id -> {
            assertEquals("hmcts-admin",roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN,roleAssignments.get(id).getRoleCategory());
        });
        IntStream.of(1).forEach(id -> {
            assertEquals("hearing-centre-admin",roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN,roleAssignments.get(id).getRoleCategory());
            assertEquals(skillCodes,roleAssignments.get(id).getAuthorisations());
        });
        IntStream.of(2).forEach(id -> {
            assertEquals("national-business-centre", roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN, roleAssignments.get(id).getRoleCategory());
            assertEquals(skillCodes,roleAssignments.get(id).getAuthorisations());
        });
        IntStream.of(3).forEach(id -> {
            assertEquals("case-allocator", roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN, roleAssignments.get(id).getRoleCategory());
            assertEquals(skillCodes,roleAssignments.get(id).getAuthorisations());
        });
        IntStream.of(4).forEach(id -> {
            assertEquals("task-supervisor", roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN, roleAssignments.get(id).getRoleCategory());
            assertEquals(skillCodes,roleAssignments.get(id).getAuthorisations());
        });
        assertEquals(usersAccessProfiles.keySet().stream().iterator().next(),
                roleAssignments.get(0).getActorId());

        for (RoleAssignment r : roleAssignments) {
            String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());
            String actualWorkTypes = null;
            if (r.getAttributes().get("workTypes") != null) {
                actualWorkTypes = r.getAttributes().get("workTypes").asText();
            }
            assertEquals(expectedWorkTypes, actualWorkTypes);
        }

    }

    @Test
    void shouldNotReturnAdminOrgRolesForRoleId_3_to_10_with_BFA2() {
        allProfiles.clear();
        IntStream.range(3, 8).forEach(roleId -> {
            CaseWorkerAccessProfile profile = TestDataBuilder.buildUserAccessProfile(roleId + "", false);
            profile.setServiceCode("BFA2");
            allProfiles.add(profile);
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("IAC", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    @Test
    void shouldNotReturnAdminOrgRolesForRoleId_3_to_10_with_suspendedProfile() {
        allProfiles.clear();
        IntStream.range(3, 8).forEach(roleId ->
                allProfiles.add(TestDataBuilder.buildUserAccessProfile(roleId + "", true)));

        allProfiles.forEach(userAccessProfile -> {
            userAccessProfile.setCaseAllocatorFlag("Y");
            userAccessProfile.setTaskSupervisorFlag("Y");
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("IAC", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

}