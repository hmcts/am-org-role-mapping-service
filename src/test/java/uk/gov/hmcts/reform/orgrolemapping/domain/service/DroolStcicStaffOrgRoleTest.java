package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

@RunWith(MockitoJUnitRunner.class)
class DroolStcicStaffOrgRoleTest extends DroolBase {

    static Map<String, String> expectedRoleNameWorkTypesMap = new HashMap<>();

    static {
        expectedRoleNameWorkTypesMap.put("senior-tribunal-caseworker", "decision_making_work");
        expectedRoleNameWorkTypesMap.put("tribunal-caseworker", "decision_making_work");
        expectedRoleNameWorkTypesMap.put("hmcts-legal-operations", null);
        expectedRoleNameWorkTypesMap.put("task-supervisor", null);
        expectedRoleNameWorkTypesMap.put("case-allocator", null);
        expectedRoleNameWorkTypesMap.put("specific-access-approver-legal-ops", "access_requests");
        expectedRoleNameWorkTypesMap.put("hearing-centre-team-leader", "applications,hearing_work,routine_work,"
                + "priority");
        expectedRoleNameWorkTypesMap.put("hearing-centre-admin", "applications,hearing_work,routine_work,priority");
        expectedRoleNameWorkTypesMap.put("hmcts-admin", null);
        expectedRoleNameWorkTypesMap.put("specific-access-approver-admin", "access_requests");
        expectedRoleNameWorkTypesMap.put("regional-centre-team-leader", "applications,hearing_work,routine_work,"
                + "priority");
        expectedRoleNameWorkTypesMap.put("regional-centre-admin", "applications,hearing_work,routine_work,priority");
        expectedRoleNameWorkTypesMap.put("cica", null);
        expectedRoleNameWorkTypesMap.put("ctsc-team-leader", "applications,hearing_work,routine_work,priority");
        expectedRoleNameWorkTypesMap.put("ctsc", "applications,hearing_work,routine_work,priority");
        expectedRoleNameWorkTypesMap.put("hmcts-ctsc", null);
        expectedRoleNameWorkTypesMap.put("specific-access-approver-ctsc", "access_requests");
    }

    @ParameterizedTest
    @CsvSource({
        "3,BBA2,'hearing-centre-team-leader,hearing-centre-admin,hmcts-admin,specific-access-approver-admin',N,N",
        "3,BBA2,'hearing-centre-team-leader,hearing-centre-admin,hmcts-admin,task-supervisor,"
                + "specific-access-approver-admin',Y,N",
        "3,BBA2,'hearing-centre-team-leader,hearing-centre-admin,hmcts-admin,case-allocator,"
                + "specific-access-approver-admin',N,Y",
        "3,BBA2,'hearing-centre-team-leader,hearing-centre-admin,hmcts-admin,task-supervisor,"
                + "case-allocator,specific-access-approver-admin',Y,Y",
        "4,BBA2,'hearing-centre-admin,hmcts-admin',N,N",
        "4,BBA2,'hearing-centre-admin,hmcts-admin,task-supervisor',Y,N",
        "4,BBA2,'hearing-centre-admin,hmcts-admin,case-allocator',N,Y",
        "4,BBA2,'hearing-centre-admin,hmcts-admin,task-supervisor,case-allocator',Y,Y",
        "12,BBA2,'regional-centre-team-leader,regional-centre-admin,hmcts-admin,"
                + "specific-access-approver-admin',N,N",
        "12,BBA2,'regional-centre-team-leader,regional-centre-admin,hmcts-admin,task-supervisor,"
                + "specific-access-approver-admin',Y,N",
        "12,BBA2,'regional-centre-team-leader,regional-centre-admin,hmcts-admin,"
                + "case-allocator,specific-access-approver-admin',N,Y",
        "12,BBA2,'regional-centre-team-leader,regional-centre-admin,hmcts-admin,task-supervisor,"
                + "case-allocator,specific-access-approver-admin',Y,Y",
        "13,BBA2,'regional-centre-admin,hmcts-admin',N,N",
        "13,BBA2,'regional-centre-admin,hmcts-admin,task-supervisor',Y,N",
        "13,BBA2,'regional-centre-admin,hmcts-admin,case-allocator',N,Y",
        "13,BBA2,'regional-centre-admin,hmcts-admin,task-supervisor,case-allocator',Y,Y"
    })
    void shouldReturnStcicAdminMappings(String roleId, String serviceCode, String expectedRoles,
                                       String taskSupervisorFlag, String caseAllocatorFlag) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<String> skillCodes = List.of("st_cic", "test", "ctsc");
        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(serviceCode);
        cap.setSuspended(false);
        cap.setRoleId(roleId);
        cap.setTaskSupervisorFlag(taskSupervisorFlag);
        cap.setCaseAllocatorFlag(caseAllocatorFlag);
        cap.setRegionId("LDN");
        cap.setSkillCodes(skillCodes);
        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("st_cic_wa_1_0", true));

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
            if (r.getRoleName().equals("hmcts-admin")) {
                assertNull(r.getAttributes().get("jurisdiction"));
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("ST_CIC", r.getAttributes().get("jurisdiction").asText());
                assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
            }
            //assert region
            assertNull(r.getAttributes().get("region"));
            //assert work types
            String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());
            String actualWorkTypes = null;
            if (r.getAttributes().get("workTypes") != null) {
                actualWorkTypes = r.getAttributes().get("workTypes").asText();
            }
            assertEquals(expectedWorkTypes, actualWorkTypes);
            //assert classification
            List<String> rolesWithPublicClassification = List.of("hearing-centre-team-leader", "hearing-centre-admin",
                                                                "task-supervisor", "case-allocator",
                                                                "regional-centre-team-leader", "regional-centre-admin",
                                                                "specific-access-approver-admin");
            if (rolesWithPublicClassification.contains(r.getRoleName())) {
                assertEquals(r.getClassification().toString(), "PUBLIC");
            } else {
                assertEquals(r.getClassification().toString(), "PRIVATE");
            }
            //assert grant type
            List<String> rolesWithStandardGrantType = List.of("hearing-centre-team-leader", "hearing-centre-admin",
                    "task-supervisor", "case-allocator", "specific-access-approver-admin",
                    "regional-centre-team-leader", "regional-centre-admin");
            if (rolesWithStandardGrantType.contains(r.getRoleName())) {
                assertEquals(r.getGrantType().toString(), "STANDARD");
            } else {
                assertEquals(r.getGrantType().toString(), "BASIC");
            }
        });
    }

    @ParameterizedTest
    @CsvSource({
        "9,BBA2,'ctsc-team-leader,ctsc,hmcts-ctsc,specific-access-approver-ctsc'"
                + ",N,N",
        "9,BBA2,'ctsc-team-leader,ctsc,hmcts-ctsc,task-supervisor,specific-access-approver-ctsc'"
                + ",Y,N",
        "9,BBA2,'ctsc-team-leader,ctsc,hmcts-ctsc,case-allocator,specific-access-approver-ctsc'"
                + ",N,Y",
        "9,BBA2,'ctsc-team-leader,ctsc,hmcts-ctsc,task-supervisor,case-allocator,specific-access-approver-ctsc'"
                + ",Y,Y",
        "10,BBA2,'ctsc,hmcts-ctsc',N,N",
        "10,BBA2,'ctsc,hmcts-ctsc,task-supervisor',Y,N",
        "10,BBA2,'ctsc,hmcts-ctsc,case-allocator',N,Y",
        "10,BBA2,'ctsc,hmcts-ctsc,task-supervisor,case-allocator',Y,Y"
    })
    void shouldReturnStcicCtscMappings(String roleId, String serviceCode, String expectedRoles,
                                                   String taskSupervisorFlag, String caseAllocatorFlag) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<String> skillCodes = List.of("st_cic", "test", "ctsc");
        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(serviceCode);
        cap.setSuspended(false);
        cap.setRoleId(roleId);
        cap.setTaskSupervisorFlag(taskSupervisorFlag);
        cap.setCaseAllocatorFlag(caseAllocatorFlag);
        cap.setRegionId("LDN");
        cap.setSkillCodes(skillCodes);
        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("st_cic_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        roleAssignments.forEach(r -> {
            assertEquals("CTSC", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());
        });

        roleAssignments.forEach(r -> {
            if (r.getRoleName().equals("hmcts-ctsc")) {
                assertNull(r.getAttributes().get("jurisdiction"));
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("ST_CIC", r.getAttributes().get("jurisdiction").asText());
                assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
            }
            //assert region
            assertNull(r.getAttributes().get("region"));
            //assert work types
            String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());
            String actualWorkTypes = null;
            if (r.getAttributes().get("workTypes") != null) {
                actualWorkTypes = r.getAttributes().get("workTypes").asText();
            }
            assertEquals(expectedWorkTypes, actualWorkTypes);
            //assert classification
            List<String> rolesWithPublicClassification = List.of("ctsc-team-leader", "ctsc",
                    "task-supervisor", "case-allocator", "specific-access-approver-ctsc");
            if (rolesWithPublicClassification.contains(r.getRoleName())) {
                assertEquals(r.getClassification().toString(), "PUBLIC");
            } else {
                assertEquals(r.getClassification().toString(), "PRIVATE");
            }
            //assert grant type
            List<String> rolesWithStandardGrantType = List.of("ctsc-team-leader", "ctsc",
                    "task-supervisor", "case-allocator", "specific-access-approver-ctsc");
            if (rolesWithStandardGrantType.contains(r.getRoleName())) {
                assertEquals(r.getGrantType().toString(), "STANDARD");
            } else {
                assertEquals(r.getGrantType().toString(), "BASIC");
            }
        });
    }

    @ParameterizedTest
    @CsvSource({
        "1,BBA2,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,"
                + "specific-access-approver-legal-ops',N,N",
        "1,BBA2,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,task-supervisor,"
                + "specific-access-approver-legal-ops',Y,N",
        "1,BBA2,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,"
                + "case-allocator,specific-access-approver-legal-ops',N,Y",
        "1,BBA2,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,task-supervisor,"
                + "case-allocator,specific-access-approver-legal-ops',Y,Y",
        "2,BBA2,'tribunal-caseworker,hmcts-legal-operations',N,N",
        "2,BBA2,'tribunal-caseworker,hmcts-legal-operations,task-supervisor',Y,N",
        "2,BBA2,'tribunal-caseworker,hmcts-legal-operations,case-allocator',N,Y",
        "2,BBA2,'tribunal-caseworker,hmcts-legal-operations,task-supervisor,case-allocator',Y,Y",
    })
    void shouldReturnStcicCaseWorkerMappings(String roleId, String serviceCode, String expectedRoles,
                                                  String taskSupervisorFlag, String caseAllocatorFlag) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<String> skillCodes = List.of("st_cic", "test", "ctsc");
        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(serviceCode);
        cap.setSuspended(false);
        cap.setRoleId(roleId);
        cap.setTaskSupervisorFlag(taskSupervisorFlag);
        cap.setCaseAllocatorFlag(caseAllocatorFlag);
        cap.setRegionId("LDN");
        cap.setSkillCodes(skillCodes);
        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("st_cic_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        roleAssignments.forEach(r -> {
            assertEquals("LEGAL_OPERATIONS", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());
        });

        roleAssignments.forEach(r -> {
            if (r.getRoleName().equals("hmcts-legal-operations")) {
                assertNull(r.getAttributes().get("jurisdiction"));
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("ST_CIC", r.getAttributes().get("jurisdiction").asText());
                assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
            }
            //assert region
            assertNull(r.getAttributes().get("region"));
            //assert work types
            String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());
            String actualWorkTypes = null;
            if (r.getAttributes().get("workTypes") != null) {
                actualWorkTypes = r.getAttributes().get("workTypes").asText();
            }
            assertEquals(expectedWorkTypes, actualWorkTypes);
            //assert classification
            List<String> rolesWithPublicClassification = List.of("senior-tribunal-caseworker", "tribunal-caseworker",
                    "task-supervisor", "case-allocator", "specific-access-approver-legal-ops");
            if (rolesWithPublicClassification.contains(r.getRoleName())) {
                assertEquals(r.getClassification().toString(), "PUBLIC");
            } else {
                assertEquals(r.getClassification().toString(), "PRIVATE");
            }
            //assert grant type
            List<String> rolesWithStandardGrantType = List.of("senior-tribunal-caseworker", "tribunal-caseworker",
                    "task-supervisor", "case-allocator", "specific-access-approver-legal-ops");
            if (rolesWithStandardGrantType.contains(r.getRoleName())) {
                assertEquals(r.getGrantType().toString(), "STANDARD");
            } else {
                assertEquals(r.getGrantType().toString(), "BASIC");
            }
        });
    }

    @ParameterizedTest
    @CsvSource({
        "17,BBA2,'cica',N,N"
    })
    void shouldReturnStcicOtherGovDepMappings(String roleId, String serviceCode, String expectedRoles,
                                                        String taskSupervisorFlag, String caseAllocatorFlag) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<String> skillCodes = List.of("st_cic", "test", "ctsc");
        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(serviceCode);
        cap.setSuspended(false);
        cap.setRoleId(roleId);
        cap.setTaskSupervisorFlag(taskSupervisorFlag);
        cap.setCaseAllocatorFlag(caseAllocatorFlag);
        cap.setRegionId("LDN");
        cap.setSkillCodes(skillCodes);
        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("st_cic_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        roleAssignments.forEach(r -> {
            assertEquals("OTHER_GOV_DEPT", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());
        });

        roleAssignments.forEach(r -> {
            //assert region
            assertNull(r.getAttributes().get("region"));
            //assert work types
            String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());
            String actualWorkTypes = null;
            if (r.getAttributes().get("workTypes") != null) {
                actualWorkTypes = r.getAttributes().get("workTypes").asText();
            }
            assertEquals(expectedWorkTypes, actualWorkTypes);
            //assert classification
            List<String> rolesWithPublicClassification = List.of("cica");
            if (rolesWithPublicClassification.contains(r.getRoleName())) {
                assertEquals(r.getClassification().toString(), "PUBLIC");
            } else {
                assertEquals(r.getClassification().toString(), "PRIVATE");
            }
            //assert grant type
            List<String> rolesWithStandardGrantType = List.of("cica");
            if (rolesWithStandardGrantType.contains(r.getRoleName())) {
                assertEquals(r.getGrantType().toString(), "STANDARD");
            } else {
                assertEquals(r.getGrantType().toString(), "BASIC");
            }
        });
    }

}
