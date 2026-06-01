package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.crd.JobTitle;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
@ExtendWith(MockitoExtension.class)
class DroolDivorceAdminOrgRoleMappingTest extends DroolBase {

    private static final String PRIMARY_LOCATION_ID = UserAccessProfileBuilder.PRIMARY_LOCATION_ID;
    private static final String REGION_ID = "LDN";
    private static final String JURISDICTION = Jurisdiction.DIVORCE.getName();

    private static final String WORK_TYPES_HEARING = "routine_work, review_case";
    private static final String WORK_TYPES_ACCESS_REQUESTS = "access_requests";

    private record ExpectedRole(String roleName,
                                RoleCategory roleCategory,
                                Classification classification,
                                GrantType grantType,
                                boolean readOnly,
                                String jurisdiction,
                                String primaryLocation,
                                String region,
                                String workTypes) {
    }

    private static ExpectedRole basicAdminRole(String roleName) {
        return new ExpectedRole(roleName, RoleCategory.ADMIN,
                Classification.PRIVATE, GrantType.BASIC, true,
                null, null, null, null);
    }

    private static ExpectedRole standardAdminRole(String roleName, String workTypes) {
        return new ExpectedRole(roleName, RoleCategory.ADMIN,
                Classification.PUBLIC, GrantType.STANDARD, false,
                JURISDICTION, PRIMARY_LOCATION_ID, null, workTypes);
    }

    private static ExpectedRole standardAdminRoleWithRegion(String roleName, String workTypes) {
        return new ExpectedRole(roleName, RoleCategory.ADMIN,
                Classification.PUBLIC, GrantType.STANDARD, false,
                JURISDICTION, PRIMARY_LOCATION_ID, REGION_ID, workTypes);
    }

    private static ExpectedRole taskSupervisor() {
        return new ExpectedRole(RoleName.TASK_SUPERVISOR, RoleCategory.ADMIN,
                Classification.PUBLIC, GrantType.STANDARD, false,
                JURISDICTION, PRIMARY_LOCATION_ID, null, null);
    }

    private static ExpectedRole caseAllocator() {
        return new ExpectedRole(RoleName.CASE_ALLOCATOR, RoleCategory.ADMIN,
                Classification.PUBLIC, GrantType.STANDARD, false,
                JURISDICTION, PRIMARY_LOCATION_ID, null, null);
    }

    private static final List<ExpectedRole> EXPECTED_ROLES_HEARING_CENTRE_TEAM_LEADER = List.of(
            basicAdminRole(RoleName.HMCTS_ADMIN),
            standardAdminRoleWithRegion(RoleName.HEARING_CENTRE_ADMIN, WORK_TYPES_HEARING),
            standardAdminRoleWithRegion(RoleName.HEARING_CENTRE_TEAM_LEADER, WORK_TYPES_HEARING),
            standardAdminRole(RoleName.SPECIFIC_ACCESS_APPROVER_ADMIN, WORK_TYPES_ACCESS_REQUESTS),
            standardAdminRole(RoleName.SPECIFIC_ACCESS_APPROVER_LEGAL_OPS, WORK_TYPES_ACCESS_REQUESTS),
            taskSupervisor(),
            caseAllocator()
    );

    private static final List<ExpectedRole> EXPECTED_ROLES_HEARING_CENTRE_ADMIN = List.of(
            basicAdminRole(RoleName.HMCTS_ADMIN),
            standardAdminRoleWithRegion(RoleName.HEARING_CENTRE_ADMIN, WORK_TYPES_HEARING),
            taskSupervisor(),
            caseAllocator()
    );

    private static final List<ExpectedRole> EXPECTED_ROLES_NBC_TEAM_LEADER = List.of(
            basicAdminRole(RoleName.HMCTS_ADMIN),
            standardAdminRole(RoleName.NBC, null),
            standardAdminRole(RoleName.NBC_TEAM_LEADER, null),
            standardAdminRole(RoleName.SPECIFIC_ACCESS_APPROVER_ADMIN, WORK_TYPES_ACCESS_REQUESTS),
            taskSupervisor(),
            caseAllocator()
    );

    private static final List<ExpectedRole> EXPECTED_ROLES_NBC_ADMIN = List.of(
            basicAdminRole(RoleName.HMCTS_ADMIN),
            standardAdminRole(RoleName.NBC, null),
            taskSupervisor(),
            caseAllocator()
    );

    static Stream<Arguments> divorceAdminScenarios() {
        return Stream.of(
            Arguments.of(JobTitle.HEARING_CENTRE_TEAM_LEADER, "Y", "Y",
                    EXPECTED_ROLES_HEARING_CENTRE_TEAM_LEADER),
            Arguments.of(JobTitle.HEARING_CENTRE_TEAM_LEADER, "N", "N",
                    EXPECTED_ROLES_HEARING_CENTRE_TEAM_LEADER),
            Arguments.of(JobTitle.HEARING_CENTRE_ADMIN, "Y", "Y",
                    EXPECTED_ROLES_HEARING_CENTRE_ADMIN),
            Arguments.of(JobTitle.HEARING_CENTRE_ADMIN, "N", "N",
                    EXPECTED_ROLES_HEARING_CENTRE_ADMIN),
            Arguments.of(JobTitle.NBC_TEAM_LEADER, "Y", "Y",
                    EXPECTED_ROLES_NBC_TEAM_LEADER),
            Arguments.of(JobTitle.NBC_TEAM_LEADER, "N", "N",
                    EXPECTED_ROLES_NBC_TEAM_LEADER),
            Arguments.of(JobTitle.NBC_ADMIN, "Y", "Y",
                    EXPECTED_ROLES_NBC_ADMIN),
            Arguments.of(JobTitle.NBC_ADMIN, "N", "N",
                    EXPECTED_ROLES_NBC_ADMIN)
        );
    }

    @ParameterizedTest
    @MethodSource("divorceAdminScenarios")
    void shouldReturnDivorceAdminMappings(JobTitle jobTitle,
                                          String taskSupervisorFlag,
                                          String caseAllocatorFlag,
                                          List<ExpectedRole> expectedBaseRoles) {
        allProfiles.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId5();
        cap.setServiceCode(Jurisdiction.DIVORCE.getServiceCodes().getFirst());
        cap.setSuspended(false);
        cap.setRoleId(jobTitle.getRoleId());
        cap.setRoleName(jobTitle.getRoleName());
        cap.setTaskSupervisorFlag(taskSupervisorFlag);
        cap.setCaseAllocatorFlag(caseAllocatorFlag);
        cap.setRegionId(REGION_ID);
        allProfiles.add(cap);

        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("DIVORCE", true));

        log.info("Returned divorce admin roles for {}: {}",
                jobTitle, roleAssignments.stream().map(RoleAssignment::getRoleName).toList());

        assertFalse(roleAssignments.isEmpty());

        Map<String, RoleAssignment> roleAssignmentByName = roleAssignments.stream()
                .collect(Collectors.toMap(RoleAssignment::getRoleName, Function.identity()));

        List<ExpectedRole> expectedRoles = new ArrayList<>(expectedBaseRoles);
        if ("N".equals(taskSupervisorFlag)) {
            expectedRoles.removeIf(r -> RoleName.TASK_SUPERVISOR.equals(r.roleName()));
        }
        if ("N".equals(caseAllocatorFlag)) {
            expectedRoles.removeIf(r -> RoleName.CASE_ALLOCATOR.equals(r.roleName()));
        }

        assertEquals(expectedRoles.size(), roleAssignments.size(),
                "Expected " + expectedRoles.size() + " roles but got " + roleAssignments.size()
                        + ": " + roleAssignments.stream().map(RoleAssignment::getRoleName).toList());

        expectedRoles.forEach(expected -> {
            RoleAssignment actual = roleAssignmentByName.get(expected.roleName());
            assertNotNull(actual, "Missing role assignment for: " + expected.roleName());

            assertEquals(expected.roleCategory(), actual.getRoleCategory());
            assertEquals(RoleType.ORGANISATION, actual.getRoleType());
            assertEquals(expected.classification(), actual.getClassification());
            assertEquals(expected.grantType(), actual.getGrantType());
            assertEquals(expected.readOnly(), actual.isReadOnly());

            if (expected.jurisdiction() == null) {
                assertNull(actual.getAttributes().get(Attributes.Name.JURISDICTION),
                        "Expected no jurisdiction on " + expected.roleName());
            } else {
                assertNotNull(actual.getAttributes().get(Attributes.Name.JURISDICTION));
                assertEquals(JURISDICTION,
                        actual.getAttributes().get(Attributes.Name.JURISDICTION).asText());
            }

            if (expected.primaryLocation() == null) {
                assertNull(actual.getAttributes().get(Attributes.Name.PRIMARY_LOCATION),
                        "Expected no primaryLocation on " + expected.roleName());
            } else {
                assertNotNull(actual.getAttributes().get(Attributes.Name.PRIMARY_LOCATION));
                assertEquals(expected.primaryLocation(),
                        actual.getAttributes().get(Attributes.Name.PRIMARY_LOCATION).asText());
            }

            if (expected.region() == null) {
                assertNull(actual.getAttributes().get(Attributes.Name.REGION),
                        "Expected no region on " + expected.roleName());
            } else {
                assertNotNull(actual.getAttributes().get(Attributes.Name.REGION));
                assertEquals(expected.region(),
                        actual.getAttributes().get(Attributes.Name.REGION).asText());
            }

            if (expected.workTypes() == null) {
                assertNull(actual.getAttributes().get(Attributes.Name.WORK_TYPES),
                        "Expected no workTypes on " + expected.roleName());
            } else {
                assertNotNull(actual.getAttributes().get(Attributes.Name.WORK_TYPES));
                assertEquals(expected.workTypes(),
                        actual.getAttributes().get(Attributes.Name.WORK_TYPES).asText());
            }
        });
    }

    @Test
    void shouldNotReturnDivorceAdminRolesWhenFeatureFlagIsOff() {
        allProfiles.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId5();
        cap.setServiceCode(Jurisdiction.DIVORCE.getServiceCodes().getFirst());
        cap.setSuspended(false);
        cap.setRoleId(JobTitle.HEARING_CENTRE_TEAM_LEADER.getRoleId());
        cap.setRoleName(JobTitle.HEARING_CENTRE_TEAM_LEADER.getRoleName());
        cap.setTaskSupervisorFlag("Y");
        cap.setCaseAllocatorFlag("Y");
        cap.setRegionId(REGION_ID);
        allProfiles.add(cap);

        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("DIVORCE", false));

        assertEquals(0, roleAssignments.size());
    }
}
