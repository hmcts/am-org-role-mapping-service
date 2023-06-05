package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class DroolStcicStaffOrgRoleTest extends DroolBase {

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
            "12,BBA2,'regional-centre-team-leader,regional-centre-admin,hmcts-admin,task-supervisor,"
                    + "case-allocator,specific-access-approver-admin',Y,Y",
            "13,BBA2,'regional-centre-admin,hmcts-admin',N,N"
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
            if (("hearing-centre-team-leader").equals(r.getRoleName())) {
                assertNull(r.getAttributes().get("workTypes"));
            } else if (("hearing-centre-admin").equals(r.getRoleName())) {
                assertThat(r.getAttributes().get("workTypes").asText().split(","),
                        arrayContainingInAnyOrder("applications", "hearing_work",
                                "routine_work", "priority"));
            } else if (("hmcts-admin").equals(r.getRoleName())) {
                assertNull(r.getAttributes().get("workTypes"));
            } else if (("task-supervisor").equals(r.getRoleName())) {
                assertNull(r.getAttributes().get("workTypes"));
            } else if (("case-allocator").equals(r.getRoleName())) {
                assertNull(r.getAttributes().get("workTypes"));
            } else if (("specific-access-approver-admin").equals(r.getRoleName())) {
                assertEquals("access_requests", r.getAttributes().get("workTypes").asText());
            }
            //assert classification
            List<String> rolesWithPublicClassification = List.of("hearing-centre-team-leader", "hearing-centre-admin",
                                                                "task-supervisor", "case-allocator",
                                                                "regional-centre-team-leader", "regional-centre-admin");
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
            "9,BBA2,'ctsc-team-leader,ctsc,hmcts-ctsc,task-supervisor,case-allocator,specific-access-approver-ctsc'"
                    + ",Y,Y",
            "10,BBA2,'ctsc,hmcts-ctsc,task-supervisor',Y,N",
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
            if (("ctsc-team-leader").equals(r.getRoleName())) {
                assertThat(r.getAttributes().get("workTypes").asText().split(","),
                        arrayContainingInAnyOrder("applications", "hearing_work",
                                "routine_work"));
            } else if (("ctsc").equals(r.getRoleName())) {
                assertThat(r.getAttributes().get("workTypes").asText().split(","),
                        arrayContainingInAnyOrder("applications", "hearing_work",
                                "routine_work"));
            } else if (("hmcts-ctsc").equals(r.getRoleName())) {
                assertNull(r.getAttributes().get("workTypes"));
            } else if (("task-supervisor").equals(r.getRoleName())) {
                assertNull(r.getAttributes().get("workTypes"));
            } else if (("case-allocator").equals(r.getRoleName())) {
                assertNull(r.getAttributes().get("workTypes"));
            } else if (("specific-access-approver-ctsc").equals(r.getRoleName())) {
                assertEquals("access_requests", r.getAttributes().get("workTypes").asText());
            }
            //assert classification
            List<String> rolesWithPublicClassification = List.of("ctsc-team-leader", "ctsc",
                    "task-supervisor", "case-allocator");
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
            "1,BBA2,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,task-supervisor,"
                    + "case-allocator,specific-access-approver-legal-ops',Y,Y",
            "2,BBA2,'tribunal-caseworker,hmcts-legal-operations,task-supervisor,case-allocator',Y,Y"
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
            if (("senior-tribunal-caseworker").equals(r.getRoleName())) {
                assertNull(r.getAttributes().get("workTypes"));
            } else if (("tribunal-caseworker").equals(r.getRoleName())) {
                assertThat(r.getAttributes().get("workTypes").asText().split(","),
                        arrayContainingInAnyOrder("applications", "hearing_work",
                                "routine_work", "priority", "decision_making_work"));
            } else if (("hmcts-legal-operations").equals(r.getRoleName())) {
                assertNull(r.getAttributes().get("workTypes"));
            } else if (("task-supervisor").equals(r.getRoleName())) {
                assertNull(r.getAttributes().get("workTypes"));
            } else if (("case-allocator").equals(r.getRoleName())) {
                assertNull(r.getAttributes().get("workTypes"));
            } else if (("specific-access-approver-legal-ops").equals(r.getRoleName())) {
                assertEquals("access_requests", r.getAttributes().get("workTypes").asText());
            }
            //assert classification
            List<String> rolesWithPublicClassification = List.of("senior-tribunal-caseworker", "tribunal-caseworker",
                    "task-supervisor", "case-allocator");
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
            if (("cica").equals(r.getRoleName())) {
                assertNull(r.getAttributes().get("workTypes"));
            }
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
