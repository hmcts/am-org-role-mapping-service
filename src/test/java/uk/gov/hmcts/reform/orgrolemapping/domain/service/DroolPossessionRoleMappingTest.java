package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.Attributes;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.RoleName;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Jurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.crd.JobTitle;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
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

    private static final String PRIMARY_LOCATION_ID = "219164";
    private static final String NO_PRIMARY_LOCATION_ID = null;
    private static final String REGION_LDN = "LDN";
    private static final String NO_REGION = null;
    private static final String WORK_TYPES_HEARING_CENTRE =
        "routine_work,applications,decision_making_work,error_management,appeals";
    private static final String WORK_TYPES_WLU =
        "routine_work,applications,decision_making_work,error_management";
    private static final String WORK_TYPES_BAILIFF = "enforcement_support,error_management";
    private static final String WORK_TYPES_ACCESS_REQUESTS = "access_requests";
    private static final String WORK_TYPES_CTSC = "routine_work,applications,decision_making_work,error_management";
    private static final String WORK_TYPES_CTSC_TEAM_LEADER = "routine_work,applications,decision_making_work,"
            + "access_requests,error_management";
    private static final String NO_WORK_TYPES = null;

    // Captures role-specific output expectations so each scenario stays readable.
    private record ExpectedRole(String roleName,
                                RoleCategory roleCategory, Classification classification,
                                GrantType grantType,
                                boolean readOnly,
                                String region,
                                String primaryLocation,
                                String workTypes) {
    }

    private static ExpectedRole expectedRole(String roleName,
                                             RoleCategory roleCategory,
                                             Classification classification,
                                             GrantType grantType,
                                             boolean readOnly,
                                             String region,
                                             String primaryLocation,
                                             String workTypes) {
        return new ExpectedRole(roleName, roleCategory, classification, grantType, readOnly, region,
                primaryLocation, workTypes);
    }

    // define the expected attributes for roles which have variations depending on jobrole

    private static ExpectedRole hmctsAdmin(String region, String primaryLocation) {
        return expectedRole(
                RoleName.HMCTS_ADMIN,
                RoleCategory.ADMIN,
                Classification.PRIVATE,
                GrantType.BASIC,
                true,
                region,
                primaryLocation,
                NO_WORK_TYPES
        );
    }

    private static ExpectedRole taskSupervisor(RoleCategory roleCategory, String region) {
        return expectedRole(
                RoleName.TASK_SUPERVISOR,
                roleCategory,
                Classification.PUBLIC,
                GrantType.STANDARD,
                false,
                region,
                PRIMARY_LOCATION_ID,
                NO_WORK_TYPES
        );
    }

    private static ExpectedRole caseAllocator(RoleCategory roleCategory, String region) {
        return expectedRole(
                RoleName.CASE_ALLOCATOR,
                roleCategory,
                Classification.PUBLIC,
                GrantType.STANDARD,
                false,
                region,
                PRIMARY_LOCATION_ID,
                NO_WORK_TYPES
        );
    }

    private static ExpectedRole specificAccessApproverAdmin(String region) {
        return expectedRole(
                RoleName.SPECIFIC_ACCESS_APPROVER_ADMIN,
                RoleCategory.ADMIN,
                Classification.PUBLIC,
                GrantType.STANDARD,
                false,
                region,
                PRIMARY_LOCATION_ID,
                WORK_TYPES_ACCESS_REQUESTS
        );
    }

    // define the expected attributes for roles which do not alter

    private static final ExpectedRole ROLE_HEARING_CENTRE_TEAM_LEADER = expectedRole(
            RoleName.HEARING_CENTRE_TEAM_LEADER,
            RoleCategory.ADMIN,
            Classification.PUBLIC,
            GrantType.STANDARD,
            false,
            REGION_LDN,
            PRIMARY_LOCATION_ID,
            WORK_TYPES_HEARING_CENTRE
    );

    private static final ExpectedRole ROLE_HEARING_CENTRE_ADMIN = expectedRole(
            RoleName.HEARING_CENTRE_ADMIN,
            RoleCategory.ADMIN,
            Classification.PUBLIC,
            GrantType.STANDARD,
            false,
            REGION_LDN,
            PRIMARY_LOCATION_ID,
            WORK_TYPES_HEARING_CENTRE
    );

    private static final ExpectedRole ROLE_WLU_ADMIN = expectedRole(
        RoleName.WLU_ADMIN,
        RoleCategory.ADMIN,
        Classification.PUBLIC,
        GrantType.STANDARD,
        false,
        NO_REGION,
        PRIMARY_LOCATION_ID,
        WORK_TYPES_WLU
    );

    private static final ExpectedRole ROLE_WLU_TEAM_LEADER = expectedRole(
        RoleName.WLU_TEAM_LEADER,
        RoleCategory.ADMIN,
        Classification.PUBLIC,
        GrantType.STANDARD,
        false,
        NO_REGION,
        PRIMARY_LOCATION_ID,
        WORK_TYPES_WLU
    );

    private static final ExpectedRole ROLE_BAILIFF_ADMIN = expectedRole(
        RoleName.BAILIFF_ADMIN,
        RoleCategory.ADMIN,
        Classification.PUBLIC,
        GrantType.STANDARD,
        false,
        NO_REGION,
        PRIMARY_LOCATION_ID,
        WORK_TYPES_BAILIFF
    );

    private static final ExpectedRole ROLE_CTSC = expectedRole(
        RoleName.CTSC,
        RoleCategory.CTSC,
        Classification.PUBLIC,
        GrantType.STANDARD,
        false,
        NO_REGION,
        PRIMARY_LOCATION_ID,
        WORK_TYPES_CTSC
    );

    private static final ExpectedRole ROLE_HMCTS_CTSC = expectedRole(
        RoleName.HMCTS_CTSC,
        RoleCategory.CTSC,
        Classification.PRIVATE,
        GrantType.BASIC,
        true,
        NO_REGION,
        NO_PRIMARY_LOCATION_ID,
        NO_WORK_TYPES
    );

    private static final ExpectedRole ROLE_CTSC_TEAM_LEADER = expectedRole(
        RoleName.CTSC_TEAM_LEADER,
        RoleCategory.CTSC,
        Classification.PUBLIC,
        GrantType.STANDARD,
        false,
        NO_REGION,
        PRIMARY_LOCATION_ID,
        WORK_TYPES_CTSC_TEAM_LEADER
    );

    private static final ExpectedRole ROLE_SPECIFIC_ACCESS_APPROVER_CTSC = expectedRole(
        RoleName.SPECIFIC_ACCESS_APPROVER_CTSC,
        RoleCategory.CTSC,
        Classification.PUBLIC,
        GrantType.STANDARD,
        false,
        NO_REGION,
        PRIMARY_LOCATION_ID,
        WORK_TYPES_ACCESS_REQUESTS
    );



    // Define expected roles for each job id.

    private static final List<ExpectedRole> EXPECTED_ROLES_HEARING_CENTRE_TEAM_LEADER = List.of(
        ROLE_HEARING_CENTRE_TEAM_LEADER,
        ROLE_HEARING_CENTRE_ADMIN,
        hmctsAdmin(NO_REGION, NO_PRIMARY_LOCATION_ID),
        specificAccessApproverAdmin(REGION_LDN),
        taskSupervisor(RoleCategory.ADMIN, REGION_LDN),
        caseAllocator(RoleCategory.ADMIN, REGION_LDN)
    );

    private static final List<ExpectedRole> EXPECTED_ROLES_HEARING_CENTRE_ADMIN = List.of(
        ROLE_HEARING_CENTRE_ADMIN,
        hmctsAdmin(NO_REGION, NO_PRIMARY_LOCATION_ID),
        taskSupervisor(RoleCategory.ADMIN,  REGION_LDN),
        caseAllocator(RoleCategory.ADMIN, REGION_LDN)
    );

    private static final List<ExpectedRole> EXPECTED_ROLES_WLU_ADMIN = List.of(
        hmctsAdmin(NO_REGION, PRIMARY_LOCATION_ID),
        ROLE_WLU_ADMIN,
        taskSupervisor(RoleCategory.ADMIN, NO_REGION),
        caseAllocator(RoleCategory.ADMIN, NO_REGION)
    );

    private static final List<ExpectedRole> EXPECTED_ROLES_WLU_TEAM_LEADER = List.of(
        hmctsAdmin(NO_REGION, NO_PRIMARY_LOCATION_ID),
        ROLE_WLU_ADMIN,
        ROLE_WLU_TEAM_LEADER,
        specificAccessApproverAdmin(NO_REGION),
        taskSupervisor(RoleCategory.ADMIN,  NO_REGION),
        caseAllocator(RoleCategory.ADMIN, NO_REGION)
    );

    private static final List<ExpectedRole> EXPECTED_ROLES_BAILIFF_ADMIN = List.of(
        hmctsAdmin(REGION_LDN, PRIMARY_LOCATION_ID),
        ROLE_BAILIFF_ADMIN,
        taskSupervisor(RoleCategory.ADMIN, REGION_LDN),
        caseAllocator(RoleCategory.ADMIN, REGION_LDN)
    );

    private static final List<ExpectedRole> EXPECTED_ROLES_CTSC_ADMIN = List.of(
        ROLE_CTSC,
        ROLE_HMCTS_CTSC,
        taskSupervisor(RoleCategory.CTSC, NO_REGION),
        caseAllocator(RoleCategory.CTSC, NO_REGION)
    );

    private static final List<ExpectedRole> EXPECTED_ROLES_CTSC_TEAM_LEADER = List.of(
        ROLE_CTSC_TEAM_LEADER,
        ROLE_CTSC,
        ROLE_HMCTS_CTSC,
        ROLE_SPECIFIC_ACCESS_APPROVER_CTSC,
        taskSupervisor(RoleCategory.CTSC, NO_REGION),
        caseAllocator(RoleCategory.CTSC, NO_REGION)
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
            ),
            Arguments.of(
                JobTitle.CTSC_ADMIN,
                TaskSupervisorFlag.NO,
                CaseAllocatorFlag.NO,
                EXPECTED_ROLES_CTSC_ADMIN
            ),
            Arguments.of(
                JobTitle.CTSC_ADMIN,
                TaskSupervisorFlag.YES,
                CaseAllocatorFlag.YES,
                EXPECTED_ROLES_CTSC_ADMIN
            ),
            Arguments.of(
                JobTitle.CTSC_TEAM_LEADER,
                TaskSupervisorFlag.NO,
                CaseAllocatorFlag.NO,
                EXPECTED_ROLES_CTSC_TEAM_LEADER
            ),
            Arguments.of(
                JobTitle.CTSC_TEAM_LEADER,
                TaskSupervisorFlag.YES,
                CaseAllocatorFlag.YES,
                EXPECTED_ROLES_CTSC_TEAM_LEADER
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
        cap.setServiceCode(Jurisdiction.POSSESSIONS.getServiceCodes().getFirst());
        cap.setSuspended(false);
        cap.setRoleId(jobTitle.getRoleId());
        cap.setRoleName(jobTitle.getRoleName());
        cap.setTaskSupervisorFlag(taskSupervisorFlag.value());
        cap.setCaseAllocatorFlag(caseAllocatorFlag.value());
        cap.setRegionId(REGION_LDN);

        allProfiles.add(cap);

        List<RoleAssignment> roleAssignments =
            buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction(Jurisdiction.POSSESSIONS.getName(), true));

        log.info("Returned possession roles: {}",
                roleAssignments.stream().map(RoleAssignment::getRoleName).toList());

        assertFalse(roleAssignments.isEmpty());

        Map<String, RoleAssignment> roleAssignmentByName = roleAssignments.stream()
            .collect(Collectors.toMap(RoleAssignment::getRoleName, Function.identity()));

        assertRolePresence(roleAssignmentByName, RoleName.TASK_SUPERVISOR,
                taskSupervisorFlag == TaskSupervisorFlag.YES);
        assertRolePresence(roleAssignmentByName, RoleName.CASE_ALLOCATOR,
                caseAllocatorFlag == CaseAllocatorFlag.YES);

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

            assertEquals(expected.roleCategory, actual.getRoleCategory());
            assertEquals(RoleType.ORGANISATION, actual.getRoleType());
            assertEquals(expected.classification(), actual.getClassification());
            assertEquals(expected.grantType(), actual.getGrantType());
            assertEquals(expected.readOnly(), actual.isReadOnly());

            assertNotNull(actual.getAttributes().get(Attributes.Name.JURISDICTION));
            assertEquals(Jurisdiction.POSSESSIONS.getName(),
                actual.getAttributes().get(Attributes.Name.JURISDICTION).asText());

            if (expected.primaryLocation() == null) {
                assertNull(actual.getAttributes().get(Attributes.Name.PRIMARY_LOCATION));
            } else {
                assertNotNull(actual.getAttributes().get(Attributes.Name.PRIMARY_LOCATION));
                assertEquals(expected.primaryLocation(),
                        actual.getAttributes().get(Attributes.Name.PRIMARY_LOCATION).asText());
            }

            if (expected.region() == null) {
                assertNull(actual.getAttributes().get(Attributes.Name.REGION));
            } else {
                assertNotNull(actual.getAttributes().get(Attributes.Name.REGION));
                assertEquals(expected.region(), actual.getAttributes().get(Attributes.Name.REGION).asText());
            }

            if (expected.workTypes() == null) {
                assertNull(actual.getAttributes().get(Attributes.Name.WORK_TYPES));
            } else {
                assertNotNull(actual.getAttributes().get(Attributes.Name.WORK_TYPES));
                assertEquals(expected.workTypes(),
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
