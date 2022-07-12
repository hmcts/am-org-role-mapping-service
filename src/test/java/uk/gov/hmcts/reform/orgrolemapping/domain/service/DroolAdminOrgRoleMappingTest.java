package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.hamcrest.collection.ArrayMatching;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
class DroolAdminOrgRoleMappingTest extends DroolBase {

    private static final String workTypes = "hearing_work, upper_tribunal, routine_work";

    static Stream<Arguments> generateData() {
        return Stream.of(
                Arguments.of("3", Arrays.asList("hmcts-admin", "hearing-centre-admin"), workTypes),
                Arguments.of("4", Arrays.asList("hmcts-admin", "hearing-centre-admin"), workTypes),
                Arguments.of("5", Arrays.asList("hmcts-admin", "hearing-centre-admin"), workTypes),
                Arguments.of("6", Arrays.asList("hmcts-admin", "national-business-centre"), workTypes),
                Arguments.of("7", Arrays.asList("hmcts-admin", "national-business-centre"), workTypes),
                Arguments.of("8", Arrays.asList("hmcts-admin", "national-business-centre"), workTypes),
                Arguments.of("9", Arrays.asList("hmcts-admin", "ctsc"), workTypes),
                Arguments.of("10", Arrays.asList("hmcts-admin", "ctsc"), workTypes)
        );
    }

    @ParameterizedTest
    @MethodSource("generateData")
    void verifyIacAdminProfileRoleCreation(String roleId, List<String> roleNames, String workTypes) {
        allProfiles.clear();
        allProfiles.add(TestDataBuilder.buildUserAccessProfile(roleId, false));

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(roleNames.size(), roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNames.toArray()));
        assertEquals(RoleCategory.ADMIN,roleAssignments.get(0).getRoleCategory());
        assertEquals(RoleCategory.ADMIN,roleAssignments.get(1).getRoleCategory());
        assertEquals(usersAccessProfiles.keySet().stream().iterator().next(),
                roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().filter(w -> w.getAttributes().get("workTypes") != null)
                        .map(w -> w.getAttributes().get("workTypes").asText()).toList(),
                containsInAnyOrder(workTypes));
    }

    @Test
    void shouldReturnAllAdminOrgRolesForRoleId_3_to_10() {
        allProfiles.clear();
        IntStream.range(3, 11).forEach(roleId ->
                allProfiles.add(TestDataBuilder.buildUserAccessProfile(roleId + "", false)));

        allProfiles.forEach(userAccessProfile -> {
            userAccessProfile.setCaseAllocatorFlag("Y");
            userAccessProfile.setTaskSupervisorFlag("Y");
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(6,roleAssignments.size());
        IntStream.of(0).forEach(id -> {
            assertEquals("hmcts-admin",roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN,roleAssignments.get(id).getRoleCategory());
        });
        IntStream.of(1).forEach(id -> {
            assertEquals("hearing-centre-admin",roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN,roleAssignments.get(id).getRoleCategory());
        });
        IntStream.of(2).forEach(id -> {
            assertEquals("national-business-centre", roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN, roleAssignments.get(id).getRoleCategory());
        });
        IntStream.of(3).forEach(id -> {
            assertEquals("ctsc", roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN, roleAssignments.get(id).getRoleCategory());
        });
        IntStream.of(4).forEach(id -> {
            assertEquals("case-allocator", roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN, roleAssignments.get(id).getRoleCategory());
        });
        IntStream.of(5).forEach(id -> {
            assertEquals("task-supervisor", roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN, roleAssignments.get(id).getRoleCategory());
        });

        assertEquals(usersAccessProfiles.keySet().stream().iterator().next(),
                roleAssignments.get(0).getActorId());
        assertEquals(workTypes,
                roleAssignments.get(1).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(2).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(3).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldNotReturnAdminOrgRolesForRoleId_3_to_10_with_V1_0() {
        allProfiles.clear();
        IntStream.range(3, 11).forEach(roleId ->
                allProfiles.add(TestDataBuilder.buildUserAccessProfile(roleId + "", false)));

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags("iac_1_0", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    @Test
    void shouldNotReturnAdminOrgRolesForRoleId_3_to_10_with_BFA2() {
        allProfiles.clear();
        IntStream.range(3, 11).forEach(roleId -> {
            CaseWorkerAccessProfile profile = TestDataBuilder.buildUserAccessProfile(roleId + "", false);
            profile.setServiceCode("BFA2");
            allProfiles.add(profile);
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    @Test
    void shouldNotReturnAdminOrgRolesForRoleId_3_to_10_with_suspendedProfile() {
        allProfiles.clear();
        IntStream.range(3, 11).forEach(roleId ->
                allProfiles.add(TestDataBuilder.buildUserAccessProfile(roleId + "", true)));

        allProfiles.forEach(userAccessProfile -> {
            userAccessProfile.setCaseAllocatorFlag("Y");
            userAccessProfile.setTaskSupervisorFlag("Y");
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    @Test
    void shouldReturnSscsStaffOrgRolesForRoleId_2_and_16() {
        allProfiles.clear();
        Stream.of(2, 16).forEach(roleId ->
                allProfiles.add(TestDataBuilder.buildUserAccessProfile(roleId + "", "BBA3",
                        false)));

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("tribunal-caseworker", "tribunal-caseworker",
                        "registrar", "registrar", "hmcts-legal-operations"));

        roleAssignments.forEach(roleAssignment -> {
            assertEquals(RoleCategory.LEGAL_OPERATIONS, roleAssignment.getRoleCategory());
            if (!roleAssignment.getRoleName().contains("hmcts")) {
                assertThat(new String[]{"7", "6"},
                        ArrayMatching.hasItemInArray(roleAssignment.getAttributes().get("region").asText()));
            } else {
                assertNull(roleAssignment.getAttributes().get("region"));
            }
            if (List.of("tribunal-caseworker", "registrar").contains(roleAssignment.getRoleName())) {
                assertEquals("SSCS", roleAssignment.getAttributes().get("jurisdiction").asText());
                assertEquals("applications,hearing_work,routine_work,priority,decision_making_work",
                        roleAssignment.getAttributes().get("workTypes").asText());
            }
        });
    }

    @Test
    void shouldReturnSscsStaffOrgRolesForRoleId_2_and_16_caseAndTaskSupervisor() {
        allProfiles.clear();
        Stream.of(2, 16).forEach(roleId -> {
            CaseWorkerAccessProfile profile = TestDataBuilder.buildUserAccessProfile(roleId + "",
                    "BBA3", false);
            profile.setCaseAllocatorFlag("Y");
            profile.setTaskSupervisorFlag("Y");
            allProfiles.add(profile);
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(9, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("tribunal-caseworker", "hmcts-legal-operations", "registrar",
                        "case-allocator","task-supervisor","task-supervisor",
                        "tribunal-caseworker", "registrar", "case-allocator"));

        roleAssignments.forEach(roleAssignment -> {
            assertEquals(RoleCategory.LEGAL_OPERATIONS, roleAssignment.getRoleCategory());
            if (!roleAssignment.getRoleName().contains("hmcts")) {
                assertThat(new String[]{"7", "6"},
                        ArrayMatching.hasItemInArray(roleAssignment.getAttributes().get("region").asText()));
            } else {
                assertNull(roleAssignment.getAttributes().get("region"));
            }
            if (List.of("tribunal-caseworker", "registrar").contains(roleAssignment.getRoleName())) {
                assertEquals("SSCS", roleAssignment.getAttributes().get("jurisdiction").asText());
                assertEquals("applications,hearing_work,routine_work,priority,decision_making_work",
                        roleAssignment.getAttributes().get("workTypes").asText());
            }
        });
    }

    @Test
    void shouldReturnSscsAdminOrgRolesForRoleId() {
        allProfiles.clear();
        Stream.of(4, 5, 9, 10, 12, 13).forEach(roleId ->
                allProfiles.add(TestDataBuilder.buildUserAccessProfile(roleId + "", "BBA3",
                        false)));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("hmcts-admin","superuser", "superuser", "clerk", "clerk"));

        roleAssignments.forEach(roleAssignment -> {
            assertEquals(RoleCategory.ADMIN, roleAssignment.getRoleCategory());
            if (!roleAssignment.getRoleName().contains("hmcts")) {
                assertThat(new String[]{"7", "6"},
                        ArrayMatching.hasItemInArray(roleAssignment.getAttributes().get("region").asText()));
            } else {
                assertNull(roleAssignment.getAttributes().get("region"));
            }
            if ("superuser".equals(roleAssignment.getRoleName())) {
                assertEquals("SSCS", roleAssignment.getAttributes().get("jurisdiction").asText());
                assertEquals("applications,hearing_work,routine_work,priority,access_requests",
                        roleAssignment.getAttributes().get("workTypes").asText());
            } else if ("clerk".equals(roleAssignment.getRoleName())) {
                assertEquals("SSCS", roleAssignment.getAttributes().get("jurisdiction").asText());
                assertEquals("applications,hearing_work,routine_work,priority",
                        roleAssignment.getAttributes().get("workTypes").asText());
            }
        });
    }

    @Test
    void shouldReturnSscsAdminOrgRolesForRoleId_caseAndTaskSupervisor() {
        allProfiles.clear();
        Stream.of(4, 5, 9, 10, 12, 13).forEach(roleId -> {
            CaseWorkerAccessProfile profile = TestDataBuilder.buildUserAccessProfile(roleId + "",
                    "BBA3", false);
            profile.setCaseAllocatorFlag("Y");
            profile.setTaskSupervisorFlag("Y");
            allProfiles.add(profile);
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(9, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("hmcts-admin","superuser", "superuser","clerk","clerk",
                        "task-supervisor", "task-supervisor","case-allocator", "case-allocator"));

        roleAssignments.forEach(roleAssignment -> {
            assertEquals(RoleCategory.ADMIN, roleAssignment.getRoleCategory());
            if (!roleAssignment.getRoleName().contains("hmcts")) {
                assertThat(new String[]{"7", "6"},
                        ArrayMatching.hasItemInArray(roleAssignment.getAttributes().get("region").asText()));
            } else {
                assertNull(roleAssignment.getAttributes().get("region"));
            }
            if ("superuser".equals(roleAssignment.getRoleName())) {
                assertEquals("SSCS", roleAssignment.getAttributes().get("jurisdiction").asText());
                assertEquals("applications,hearing_work,routine_work,priority,access_requests",
                        roleAssignment.getAttributes().get("workTypes").asText());
            } else if ("clerk".equals(roleAssignment.getRoleName())) {
                assertEquals("SSCS", roleAssignment.getAttributes().get("jurisdiction").asText());
                assertEquals("applications,hearing_work,routine_work,priority",
                        roleAssignment.getAttributes().get("workTypes").asText());
            }
        });
    }

    @Test
    void shouldReturnSscsOgdOrgRolesForRoleId() {
        allProfiles.clear();
        Stream.of(14, 15).forEach(roleId -> {
            CaseWorkerAccessProfile profile = TestDataBuilder.buildUserAccessProfile(roleId + "",
                    "BBA3", false);
            profile.setCaseAllocatorFlag("Y");
            profile.setTaskSupervisorFlag("Y");
            allProfiles.add(profile);
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("dwp", "hmrc","dwp", "hmrc"));
        roleAssignments.forEach(roleAssignment -> {
            assertEquals(RoleCategory.OTHER_GOV_DEPT, roleAssignment.getRoleCategory());
            assertThat(new String[]{"7", "6"},
                    ArrayMatching.hasItemInArray(roleAssignment.getAttributes().get("region").asText()));
            assertEquals("SSCS", roleAssignment.getAttributes().get("jurisdiction").asText());
            assertEquals("applications,hearing_work,routine_work,priority",
                        roleAssignment.getAttributes().get("workTypes").asText());
        });
    }

}