package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.ROLE_ASSIGNMENTS_RESULTS_KEY;

@RunWith(MockitoJUnitRunner.class)
class DroolAdminOrgRoleMappingTest extends DroolBase {


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
            assertEquals("CTSC",roleAssignments.get(id).getRoleName());
            assertEquals(RoleCategory.ADMIN,roleAssignments.get(id).getRoleCategory());
        });
        assertEquals(usersAccessProfiles.keySet().stream().iterator().next(),
                roleAssignments.get(0).getActorId());
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
            assertEquals("CTSC", roleAssignments.get(id).getRoleName());
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
            UserAccessProfile profile = TestDataBuilder.buildUserAccessProfile(roleId + "", false);
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
}