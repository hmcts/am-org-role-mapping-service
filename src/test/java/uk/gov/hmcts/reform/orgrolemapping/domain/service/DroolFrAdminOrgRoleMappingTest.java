package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
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
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.crd.JobTitle;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(MockitoExtension.class)
class DroolFrAdminOrgRoleMappingTest extends DroolBase {

    private static final String PRIMARY_LOCATION_ID = UserAccessProfileBuilder.PRIMARY_LOCATION_ID;
    private static final String REGION_ID = "LDN";
    private static final String JURISDICTION = Jurisdiction.FR.getName();
    private static final String FEATURE_FLAG_PREFIX = "FR";

    private static final String WORK_TYPES_HEARING = "routine_work, review_case";
    private static final String WORK_TYPES_ACCESS_REQUESTS = "access_requests";
    private static final String WORK_TYPES_CTSC_TEAM_LEADER =
            "hearing_work, routine_work, decision_making_work, applications, review_case, evidence";
    private static final String WORK_TYPES_CTSC =
            "hearing_work, routine_work, applications, review_case, evidence";

    private static final Map<String, String> STAFF_ROLE_WORK_TYPES = new HashMap<>();

    static {
        STAFF_ROLE_WORK_TYPES.put(RoleName.CTSC_TEAM_LEADER, WORK_TYPES_CTSC_TEAM_LEADER);
        STAFF_ROLE_WORK_TYPES.put(RoleName.CTSC, WORK_TYPES_CTSC);
        STAFF_ROLE_WORK_TYPES.put(RoleName.SPECIFIC_ACCESS_APPROVER_CTSC, WORK_TYPES_ACCESS_REQUESTS);
    }

    private record ExpectedRole(String roleName,
                                RoleCategory roleCategory,
                                Classification classification,
                                GrantType grantType,
                                boolean readOnly,
                                String jurisdiction,
                                String primaryLocation,
                                String region,
                                String caseType,
                                String workTypes) {
    }

    private static ExpectedRole basicAdminRole(String roleName) {
        return new ExpectedRole(roleName, RoleCategory.ADMIN,
                Classification.PRIVATE, GrantType.BASIC, true,
                null, null, null, null, null);
    }

    private static ExpectedRole standardAdminRole(String roleName, String workTypes) {
        return new ExpectedRole(roleName, RoleCategory.ADMIN,
                Classification.PUBLIC, GrantType.STANDARD, false,
                JURISDICTION, PRIMARY_LOCATION_ID, REGION_ID, null, workTypes);
    }

    private static ExpectedRole standardAdminRoleWithRegion(String roleName, String workTypes) {
        return new ExpectedRole(roleName, RoleCategory.ADMIN,
                Classification.PUBLIC, GrantType.STANDARD, false,
                JURISDICTION, PRIMARY_LOCATION_ID, REGION_ID, null, workTypes);
    }

    private static ExpectedRole taskSupervisor() {
        return new ExpectedRole(RoleName.TASK_SUPERVISOR, RoleCategory.ADMIN,
                Classification.PUBLIC, GrantType.STANDARD, false,
                JURISDICTION, PRIMARY_LOCATION_ID, REGION_ID, null, null);
    }

    private static ExpectedRole caseAllocator() {
        return new ExpectedRole(RoleName.CASE_ALLOCATOR, RoleCategory.ADMIN,
                Classification.PUBLIC, GrantType.STANDARD, false,
                JURISDICTION, PRIMARY_LOCATION_ID, REGION_ID, null, null);
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

    static Stream<Arguments> frAdminScenarios() {
        return Stream.of(
            scenariosFor(JobTitle.HEARING_CENTRE_TEAM_LEADER, EXPECTED_ROLES_HEARING_CENTRE_TEAM_LEADER),
            scenariosFor(JobTitle.HEARING_CENTRE_ADMIN, EXPECTED_ROLES_HEARING_CENTRE_ADMIN),
            scenariosFor(JobTitle.NBC_TEAM_LEADER, EXPECTED_ROLES_NBC_TEAM_LEADER),
            scenariosFor(JobTitle.NBC_ADMIN, EXPECTED_ROLES_NBC_ADMIN)
        ).flatMap(Function.identity());
    }

    private static Stream<Arguments> scenariosFor(JobTitle jobTitle, List<ExpectedRole> expectedRoles) {
        return Stream.of(
            Arguments.of(jobTitle, "Y", "Y", expectedRoles),
            Arguments.of(jobTitle, "Y", "N", expectedRoles),
            Arguments.of(jobTitle, "N", "Y", expectedRoles),
            Arguments.of(jobTitle, "N", "N", expectedRoles)
        );
    }

    @ParameterizedTest
    @MethodSource("frAdminScenarios")
    void shouldReturnFrAdminMappings(JobTitle jobTitle,
                                     String taskSupervisorFlag,
                                     String caseAllocatorFlag,
                                     List<ExpectedRole> expectedBaseRoles) {
        allProfiles.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId5();
        cap.setServiceCode(Jurisdiction.FR.getServiceCodes().getFirst());
        cap.setSuspended(false);
        cap.setRoleId(jobTitle.getRoleId());
        cap.setRoleName(jobTitle.getRoleName());
        cap.setTaskSupervisorFlag(taskSupervisorFlag);
        cap.setCaseAllocatorFlag(caseAllocatorFlag);
        cap.setRegionId(REGION_ID);
        allProfiles.add(cap);

        List<RoleAssignment> roleAssignments =
            buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction(FEATURE_FLAG_PREFIX, true));

        log.info("Returned FR admin roles for {}: {}",
                jobTitle, roleAssignments.stream().map(RoleAssignment::getRoleName).toList());

        assertFalse(roleAssignments.isEmpty());

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

        Map<String, RoleAssignment> roleAssignmentByName = roleAssignments.stream()
                .collect(Collectors.toMap(RoleAssignment::getRoleName, Function.identity()));

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

            if (expected.caseType() == null) {
                assertNull(actual.getAttributes().get(Attributes.Name.CASE_TYPE),
                        "Expected no caseType on " + expected.roleName());
            } else {
                assertNotNull(actual.getAttributes().get(Attributes.Name.CASE_TYPE));
                assertEquals(expected.caseType(),
                        actual.getAttributes().get(Attributes.Name.CASE_TYPE).asText());
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
    void shouldNotReturnFrAdminRolesWhenFeatureFlagIsOff() {
        allProfiles.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId5();
        cap.setServiceCode(Jurisdiction.FR.getServiceCodes().getFirst());
        cap.setSuspended(false);
        cap.setRoleId(JobTitle.HEARING_CENTRE_TEAM_LEADER.getRoleId());
        cap.setRoleName(JobTitle.HEARING_CENTRE_TEAM_LEADER.getRoleName());
        cap.setTaskSupervisorFlag("Y");
        cap.setCaseAllocatorFlag("Y");
        cap.setRegionId(REGION_ID);
        allProfiles.add(cap);

        List<RoleAssignment> roleAssignments =
            buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction(FEATURE_FLAG_PREFIX, false));

        assertEquals(0, roleAssignments.size());
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
    void shouldReturnFrCtscMappings(String roleId,
                                    String expectedRoles,
                                    String taskSupervisorFlag,
                                    String caseAllocatorFlag) {
        allProfiles.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(Jurisdiction.FR.getServiceCodes().getFirst());
        cap.setSuspended(false);
        cap.setRoleId(roleId);
        cap.setTaskSupervisorFlag(taskSupervisorFlag);
        cap.setCaseAllocatorFlag(caseAllocatorFlag);
        cap.setRegionId(REGION_ID);
        allProfiles.add(cap);

        List<RoleAssignment> roleAssignments =
            buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction(FEATURE_FLAG_PREFIX, true));

        String[] roleNames = expectedRoles.isBlank() ? new String[0] : expectedRoles.split(",");
        assertEquals(roleNames.length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNames));

        roleAssignments.forEach(r -> {
            assertEquals(RoleCategory.CTSC, r.getRoleCategory());
            assertEquals(RoleType.ORGANISATION, r.getRoleType());
            assertEquals(cap.getId(), r.getActorId());
        });

        roleAssignments.forEach(r -> {
            if (RoleName.HMCTS_CTSC.equals(r.getRoleName())) {
                assertNull(r.getAttributes().get(Attributes.Name.JURISDICTION));
                assertEquals(Classification.PRIVATE, r.getClassification());
                assertEquals(GrantType.BASIC, r.getGrantType());
            } else {
                assertEquals(JURISDICTION, r.getAttributes().get(Attributes.Name.JURISDICTION).asText());
                assertEquals(Classification.PUBLIC, r.getClassification());
                assertEquals(GrantType.STANDARD, r.getGrantType());
                assertEquals(cap.getPrimaryLocationId(),
                        r.getAttributes().get(Attributes.Name.PRIMARY_LOCATION).asText());
            }
            if (STAFF_ROLE_WORK_TYPES.containsKey(r.getRoleName())) {
                assertEquals(STAFF_ROLE_WORK_TYPES.get(r.getRoleName()),
                        r.getAttributes().get(Attributes.Name.WORK_TYPES).asText());
            } else {
                assertFalse(r.getAttributes().containsKey(Attributes.Name.WORK_TYPES));
            }
        });
    }

    @Test
    void shouldNotReturnFrRolesWhenFeatureFlagIsOff() {
        allProfiles.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(Jurisdiction.FR.getServiceCodes().getFirst());
        cap.setSuspended(false);
        cap.setRoleId(JobTitle.CTSC_TEAM_LEADER.getRoleId());
        cap.setTaskSupervisorFlag("Y");
        cap.setCaseAllocatorFlag("Y");
        cap.setRegionId(REGION_ID);
        allProfiles.add(cap);

        List<RoleAssignment> roleAssignments =
            buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction(FEATURE_FLAG_PREFIX, false));

        assertTrue(roleAssignments.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
        "Y,Y",
        "Y,N",
        "N,Y",
        "N,N"
    })
    void shouldNotReturnOrgRolesForCaseWorkerWithSuspendedProfile(String taskSupervisorFlag,
                                                                   String caseAllocatorFlag) {
        allProfiles.clear();
        IntStream.range(1, 11).forEach(roleId ->
                allProfiles.add(TestDataBuilder.buildUserAccessProfile(String.valueOf(roleId), true)));

        allProfiles.forEach(userAccessProfile -> {
            userAccessProfile.setServiceCode(Jurisdiction.FR.getServiceCodes().getFirst());
            userAccessProfile.setCaseAllocatorFlag(caseAllocatorFlag);
            userAccessProfile.setTaskSupervisorFlag(taskSupervisorFlag);
            userAccessProfile.setRegionId(REGION_ID);
        });

        List<RoleAssignment> roleAssignments =
            buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction(FEATURE_FLAG_PREFIX, true));

        assertTrue(roleAssignments.isEmpty());
    }
}
