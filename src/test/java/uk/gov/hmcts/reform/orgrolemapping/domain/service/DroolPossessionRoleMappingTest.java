package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.Attributes;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.RoleName;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.*;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.crd.JobTitle;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DroolPossessionRoleMappingTest extends DroolBase {

    private enum TaskSupervisorFlag {
        YES("Y"),
        NO("N");

        private final String value;

        TaskSupervisorFlag(String value) {
            this.value = value;
        }

        String value() {
            return value;
        }
    }

    private enum CaseAllocatorFlag {
        YES("Y"),
        NO("N");

        private final String value;

        CaseAllocatorFlag(String value) {
            this.value = value;
        }

        String value() {
            return value;
        }
    }

    private static final String REGION_LDN = "LDN";
    private static final String WORK_TYPES_HEARING_CENTRE =
        "routine_work,applications,decision_making_work,error_management,appeals";
    private static final String WORK_TYPES_WLU =
        "routine_work,applications,decision_making_work,error_management";
    private static final String WORK_TYPES_BAILIFF = "enforcement_support,error_management";
    private static final String WORK_TYPES_ACCESS_REQUESTS = "access_requests";

    // Captures role-specific output expectations so each scenario stays readable.
    private record ExpectedRole(String roleName,
                                Classification classification,
                                GrantType grantType,
                                boolean readOnly,
                                String expectedRegion,
                                String expectedWorkTypes) {
    }

    private static ExpectedRole expectedRole(String roleName,
                                             Classification classification,
                                             GrantType grantType,
                                             boolean readOnly,
                                             String expectedRegion,
                                             String expectedWorkTypes) {
        return new ExpectedRole(roleName, classification, grantType, readOnly, expectedRegion, expectedWorkTypes);
    }

    // define the expected attributes for each role
    private static final ExpectedRole ROLE_HEARING_CENTRE_TEAM_LEADER = expectedRole(
        RoleName.HEARING_CENTRE_TEAM_LEADER,
        Classification.PUBLIC,
        GrantType.STANDARD,
        false,
        REGION_LDN,
        WORK_TYPES_HEARING_CENTRE
    );

    private static final ExpectedRole ROLE_HEARING_CENTRE_ADMIN = expectedRole(
        RoleName.HEARING_CENTRE_ADMIN,
        Classification.PUBLIC,
        GrantType.STANDARD,
        false,
        REGION_LDN,
        WORK_TYPES_HEARING_CENTRE
    );

    private static final ExpectedRole ROLE_TASK_SUPERVISOR = expectedRole(
        RoleName.TASK_SUPERVISOR,
        Classification.PUBLIC,
        GrantType.STANDARD,
        false,
        REGION_LDN,
        null
    );

    private static final ExpectedRole ROLE_HMCTS_ADMIN = expectedRole(
        RoleName.HMCTS_ADMIN,
        Classification.PRIVATE,
        GrantType.BASIC,
        true,
        null,
        null
    );

    private static final ExpectedRole ROLE_CASE_ALLOCATOR = expectedRole(
        RoleName.CASE_ALLOCATOR,
        Classification.PUBLIC,
        GrantType.STANDARD,
        false,
        REGION_LDN,
        null
    );

    private static final ExpectedRole ROLE_SPECIFIC_ACCESS_APPROVER_ADMIN = expectedRole(
        RoleName.SPECIFIC_ACCESS_APPROVER_ADMIN,
        Classification.PUBLIC,
        GrantType.STANDARD,
        false,
        REGION_LDN,
        WORK_TYPES_ACCESS_REQUESTS
    );

    private static final ExpectedRole ROLE_WLU_ADMIN = expectedRole(
        RoleName.WLU_ADMIN,
        Classification.PUBLIC,
        GrantType.STANDARD,
        false,
        null,
        WORK_TYPES_WLU
    );

    private static final ExpectedRole ROLE_WLU_TEAM_LEADER = expectedRole(
        RoleName.WLU_TEAM_LEADER,
        Classification.PUBLIC,
        GrantType.STANDARD,
        false,
        null,
        WORK_TYPES_WLU
    );

    private static final ExpectedRole ROLE_BAILIFF_ADMIN = expectedRole(
        RoleName.BAILIFF_ADMIN,
        Classification.PUBLIC,
        GrantType.STANDARD,
        false,
        null,
        WORK_TYPES_BAILIFF
    );

    // Define expected roles for each job id.

    private static final List<ExpectedRole> EXPECTED_ROLES_HEARING_CENTRE_TEAM_LEADER = List.of(
        ROLE_HEARING_CENTRE_TEAM_LEADER,
        ROLE_HEARING_CENTRE_ADMIN,
        ROLE_HMCTS_ADMIN,
        ROLE_SPECIFIC_ACCESS_APPROVER_ADMIN,
        ROLE_TASK_SUPERVISOR,
        ROLE_CASE_ALLOCATOR
    );

    private static final List<ExpectedRole> EXPECTED_ROLES_HEARING_CENTRE_ADMIN = List.of(
        ROLE_HEARING_CENTRE_ADMIN,
        ROLE_HMCTS_ADMIN,
        ROLE_TASK_SUPERVISOR,
        ROLE_CASE_ALLOCATOR
    );

    private static final List<ExpectedRole> EXPECTED_ROLES_WLU_ADMIN = List.of(
        ROLE_HMCTS_ADMIN,
        ROLE_WLU_ADMIN,
        ROLE_TASK_SUPERVISOR,
        ROLE_CASE_ALLOCATOR
    );

    private static final List<ExpectedRole> EXPECTED_ROLES_WLU_TEAM_LEADER = List.of(
        ROLE_HMCTS_ADMIN,
        ROLE_WLU_ADMIN,
        ROLE_WLU_TEAM_LEADER,
        ROLE_SPECIFIC_ACCESS_APPROVER_ADMIN,
        ROLE_TASK_SUPERVISOR,
        ROLE_CASE_ALLOCATOR
    );

    private static final List<ExpectedRole> EXPECTED_ROLES_BAILIFF_ADMIN = List.of(
        ROLE_HMCTS_ADMIN,
        ROLE_BAILIFF_ADMIN,
        ROLE_TASK_SUPERVISOR,
        ROLE_CASE_ALLOCATOR
    );

    static Stream<Arguments> possessionAdminScenarios() {
        return Stream.of(
            Arguments.of(
                JobTitle.HEARING_CENTRE_TEAM_LEADER,
                TaskSupervisorFlag.YES,
                CaseAllocatorFlag.YES,
                EXPECTED_ROLES_HEARING_CENTRE_TEAM_LEADER
            ),
            Arguments.of(
                JobTitle.HEARING_CENTRE_TEAM_LEADER,
                TaskSupervisorFlag.NO,
                CaseAllocatorFlag.NO,
                EXPECTED_ROLES_HEARING_CENTRE_TEAM_LEADER
            ),
            Arguments.of(
                JobTitle.HEARING_CENTRE_ADMIN,
                TaskSupervisorFlag.YES,
                CaseAllocatorFlag.YES,
                EXPECTED_ROLES_HEARING_CENTRE_ADMIN
            ),
            Arguments.of(
                JobTitle.HEARING_CENTRE_ADMIN,
                TaskSupervisorFlag.NO,
                CaseAllocatorFlag.NO,
                EXPECTED_ROLES_HEARING_CENTRE_ADMIN
            ),
            Arguments.of(
                JobTitle.WLU_ADMIN,
                TaskSupervisorFlag.YES,
                CaseAllocatorFlag.YES,
                EXPECTED_ROLES_WLU_ADMIN
            ),
            Arguments.of(
                JobTitle.WLU_ADMIN,
                TaskSupervisorFlag.NO,
                CaseAllocatorFlag.NO,
                EXPECTED_ROLES_WLU_ADMIN
            ),
            Arguments.of(
                JobTitle.WLU_TEAM_LEADER,
                TaskSupervisorFlag.YES,
                CaseAllocatorFlag.YES,
                EXPECTED_ROLES_WLU_TEAM_LEADER
            ),
            Arguments.of(
                JobTitle.WLU_TEAM_LEADER,
                TaskSupervisorFlag.NO,
                CaseAllocatorFlag.NO,
                EXPECTED_ROLES_WLU_TEAM_LEADER
            ),
            Arguments.of(
                JobTitle.BAILIFF_ADMIN,
                TaskSupervisorFlag.YES,
                CaseAllocatorFlag.YES,
                EXPECTED_ROLES_BAILIFF_ADMIN
            ),
            Arguments.of(
                JobTitle.BAILIFF_ADMIN,
                TaskSupervisorFlag.NO,
                CaseAllocatorFlag.NO,
                EXPECTED_ROLES_BAILIFF_ADMIN
            )
        );
    }

    @ParameterizedTest
    @MethodSource("possessionAdminScenarios")
    void shouldReturnPossessionsAdminMappings(JobTitle jobTitle,
                                              TaskSupervisorFlag taskSupervisorFlag,
                                              CaseAllocatorFlag caseAllocatorFlag,
                                              List<ExpectedRole> expectedBaseRoles) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId5();
        cap.setServiceCode(Jurisdiction.POSSESSION.getServiceCodes().get(0));
        cap.setSuspended(false);
        cap.setRoleId(jobTitle.getRoleId());
        cap.setRoleName(jobTitle.getRoleName());
        cap.setTaskSupervisorFlag(taskSupervisorFlag.value());
        cap.setCaseAllocatorFlag(caseAllocatorFlag.value());
        cap.setRegionId(REGION_LDN);

        allProfiles.add(cap);

        List<RoleAssignment> roleAssignments =
            buildExecuteKieSession(getFeatureFlags(FeatureFlagEnum.POSSESSIONS_WA_1_0.getValue(), true));

        System.out.println("Returned possession roles: "
            + roleAssignments.stream().map(RoleAssignment::getRoleName).toList());

        assertFalse(roleAssignments.isEmpty());

        Map<String, RoleAssignment> roleAssignmentByName = roleAssignments.stream()
            .collect(Collectors.toMap(RoleAssignment::getRoleName, Function.identity()));

        assertRolePresence(roleAssignmentByName, RoleName.TASK_SUPERVISOR, taskSupervisorFlag == TaskSupervisorFlag.YES);
        assertRolePresence(roleAssignmentByName, RoleName.CASE_ALLOCATOR, caseAllocatorFlag == CaseAllocatorFlag.YES);

        List<ExpectedRole> expectedRoles = new ArrayList<>(expectedBaseRoles);
        if (taskSupervisorFlag == TaskSupervisorFlag.NO) {
            expectedRoles.removeIf(expectedRole -> RoleName.TASK_SUPERVISOR.equals(expectedRole.roleName()));
        }
        if (caseAllocatorFlag == CaseAllocatorFlag.NO) {
            expectedRoles.removeIf(expectedRole -> RoleName.CASE_ALLOCATOR.equals(expectedRole.roleName()));
        }

        assertEquals(expectedRoles.size(), roleAssignments.size());

        expectedRoles.forEach(expected -> {
            RoleAssignment actual = roleAssignmentByName.get(expected.roleName());
            assertNotNull(actual, "Missing role assignment for role: " + expected.roleName());

            assertEquals(RoleCategory.ADMIN, actual.getRoleCategory());
            assertEquals(RoleType.ORGANISATION, actual.getRoleType());
            assertEquals(expected.classification(), actual.getClassification());
            assertEquals(expected.grantType(), actual.getGrantType());
            assertEquals(expected.readOnly(), actual.isReadOnly());

            assertNotNull(actual.getAttributes().get(Attributes.Name.JURISDICTION));
            assertNotNull(actual.getAttributes().get(Attributes.Name.PRIMARY_LOCATION));
            assertEquals(Jurisdiction.POSSESSION.getName(),
                actual.getAttributes().get(Attributes.Name.JURISDICTION).asText());

            if (expected.expectedRegion() == null) {
                assertNull(actual.getAttributes().get(Attributes.Name.REGION));
            } else {
                assertNotNull(actual.getAttributes().get(Attributes.Name.REGION));
                assertEquals(expected.expectedRegion(), actual.getAttributes().get(Attributes.Name.REGION).asText());
            }

            if (expected.expectedWorkTypes() == null) {
                assertNull(actual.getAttributes().get(Attributes.Name.WORK_TYPES));
            } else {
                assertNotNull(actual.getAttributes().get(Attributes.Name.WORK_TYPES));
                assertEquals(expected.expectedWorkTypes(),
                    actual.getAttributes().get(Attributes.Name.WORK_TYPES).asText());
            }
        });
    }

    private static void assertRolePresence(Map<String, RoleAssignment> roleAssignmentByName,
                                           String roleName,
                                           boolean expectedPresent) {
        if (expectedPresent) {
            assertTrue(roleAssignmentByName.containsKey(roleName),
                "Expected role missing when flag is enabled: " + roleName);
        } else {
            assertFalse(roleAssignmentByName.containsKey(roleName),
                "Role should not be present when flag is disabled: " + roleName);
        }
    }
}
