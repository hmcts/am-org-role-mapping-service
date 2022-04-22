package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.ROLE_ASSIGNMENTS_RESULTS_KEY;

@RunWith(MockitoJUnitRunner.class)
class DroolAdminOrgRoleMappingTest extends DroolBase {

    private final String workTypes = "hearing_work, upper_tribunal, routine_work";

    @Test
    void shouldReturnHearingCentreAdminAndAdminOrgRolesForRoleId_3_to_5() {
        allProfiles.clear();
        IntStream.range(3, 6).forEach(roleId ->
                allProfiles.add(TestDataBuilder.buildUserAccessProfile(roleId + "", false)));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(6,roleAssignments.size());
        IntStream.range(0, 3).forEach(id -> {
            assertEquals("hmcts-admin",roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN,roleAssignments.get(id).getRoleCategory());
        });
        IntStream.range(3, 6).forEach(id -> {
            assertEquals("hearing-centre-admin",roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN,roleAssignments.get(id).getRoleCategory());
        });
        assertEquals(usersAccessProfiles.keySet().stream().iterator().next(),
                roleAssignments.get(0).getActorId());
        assertEquals(workTypes,
                roleAssignments.get(3).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(4).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(5).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnNationalBusinessCentreAndAdminOrgRolesForRoleId_6_to_8() {
        allProfiles.clear();
        IntStream.range(6, 9).forEach(roleId ->
                allProfiles.add(TestDataBuilder.buildUserAccessProfile(roleId + "", false)));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(6,roleAssignments.size());
        IntStream.range(0, 3).forEach(id -> {
            assertEquals("hmcts-admin",roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN,roleAssignments.get(id).getRoleCategory());
        });
        IntStream.range(3, 6).forEach(id -> {
            assertEquals("national-business-centre",roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN,roleAssignments.get(id).getRoleCategory());
        });
        assertEquals(usersAccessProfiles.keySet().stream().iterator().next(),
                roleAssignments.get(0).getActorId());
        assertEquals(workTypes,
                roleAssignments.get(3).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(4).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(5).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnCtscAndAdminOrgRolesForRoleId_9_to_10() {
        allProfiles.clear();
        IntStream.range(9, 11).forEach(roleId ->
                allProfiles.add(TestDataBuilder.buildUserAccessProfile(roleId + "", false)));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4,roleAssignments.size());
        IntStream.range(0, 2).forEach(id -> {
            assertEquals("hmcts-admin",roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN,roleAssignments.get(id).getRoleCategory());
        });
        IntStream.range(2, 4).forEach(id -> {
            assertEquals("ctsc",roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN,roleAssignments.get(id).getRoleCategory());
        });
        assertEquals(usersAccessProfiles.keySet().stream().iterator().next(),
                roleAssignments.get(0).getActorId());
        assertEquals(workTypes,
                roleAssignments.get(2).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(3).getAttributes().get("workTypes").asText());
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
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(32,roleAssignments.size());
        IntStream.range(0, 8).forEach(id -> {
            assertEquals("hmcts-admin",roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN,roleAssignments.get(id).getRoleCategory());
        });
        IntStream.range(8, 11).forEach(id -> {
            assertEquals("hearing-centre-admin",roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN,roleAssignments.get(id).getRoleCategory());
        });
        IntStream.range(11, 13).forEach(id -> {
            assertEquals("national-business-centre", roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN, roleAssignments.get(id).getRoleCategory());
        });
        IntStream.range(14, 15).forEach(id -> {
            assertEquals("ctsc", roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN, roleAssignments.get(id).getRoleCategory());
        });
        IntStream.range(16, 23).forEach(id -> {
            assertEquals("case-allocator", roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN, roleAssignments.get(id).getRoleCategory());
        });
        IntStream.range(24, 31).forEach(id -> {
            assertEquals("task-supervisor", roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN, roleAssignments.get(id).getRoleCategory());
        });

        assertEquals(usersAccessProfiles.keySet().stream().iterator().next(),
                roleAssignments.get(0).getActorId());

        assertEquals(workTypes,
                roleAssignments.get(8).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(9).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(10).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(11).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(12).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(13).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(14).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(15).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldNotReturnAdminOrgRolesForRoleId_3_to_10_with_V1_0() {
        allProfiles.clear();
        IntStream.range(3, 11).forEach(roleId ->
                allProfiles.add(TestDataBuilder.buildUserAccessProfile(roleId + "", false)));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("iac_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

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
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

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
        buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

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
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("tribunal-caseworker", "hmcts-legal-operations", "registrar",
                        "hmcts-legal-operations"));
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(roleAssignment -> {
            assertEquals(RoleCategory.LEGAL_OPERATIONS, roleAssignment.getRoleCategory());
            assertEquals(regionId, roleAssignment.getAttributes().get("region").asText());
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
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(8, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("tribunal-caseworker", "hmcts-legal-operations", "registrar",
                        "hmcts-legal-operations", "task-supervisor", "case-allocator","task-supervisor",
                        "case-allocator"));
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(roleAssignment -> {
            assertEquals(RoleCategory.LEGAL_OPERATIONS, roleAssignment.getRoleCategory());
            assertEquals(regionId, roleAssignment.getAttributes().get("region").asText());
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
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(12, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("hmcts-admin", "hmcts-admin","hmcts-admin","hmcts-admin","hmcts-admin",
                        "hmcts-admin","superuser", "superuser", "clerk","clerk","clerk","clerk"));
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(roleAssignment -> {
            assertEquals(RoleCategory.ADMIN, roleAssignment.getRoleCategory());
            assertEquals(regionId, roleAssignment.getAttributes().get("region").asText());
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
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(24, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("hmcts-admin", "hmcts-admin","hmcts-admin","hmcts-admin","hmcts-admin",
                        "hmcts-admin","superuser", "superuser", "clerk","clerk","clerk","clerk", "task-supervisor",
                        "task-supervisor", "task-supervisor","task-supervisor", "task-supervisor","task-supervisor",
                        "case-allocator", "case-allocator", "case-allocator", "case-allocator", "case-allocator",
                        "case-allocator"));
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(roleAssignment -> {
            assertEquals(RoleCategory.ADMIN, roleAssignment.getRoleCategory());
            assertEquals(regionId, roleAssignment.getAttributes().get("region").asText());
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
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("dwp", "hmrc"));
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(roleAssignment -> {
            assertEquals(RoleCategory.OTHER_GOV_DEPT, roleAssignment.getRoleCategory());
            assertEquals(regionId, roleAssignment.getAttributes().get("region").asText());
            assertEquals("SSCS", roleAssignment.getAttributes().get("jurisdiction").asText());
            assertEquals("applications,hearing_work,routine_work,priority",
                        roleAssignment.getAttributes().get("workTypes").asText());
        });
    }

}