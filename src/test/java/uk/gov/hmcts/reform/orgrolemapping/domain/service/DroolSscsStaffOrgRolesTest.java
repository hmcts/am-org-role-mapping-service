package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class DroolSscsStaffOrgRolesTest extends DroolBase {

    @ParameterizedTest
    @CsvSource({
            "3,BBA3,'hmcts-admin,hearing-centre-team-leader,hearing-centre-admin,specific-access-approver-admin',N,N",
            "3,BBA3,'hmcts-admin,hearing-centre-team-leader,hearing-centre-admin,task-supervisor,specific-access-approver-admin',Y,N",
            "3,BBA3,'hmcts-admin,hearing-centre-team-leader,hearing-centre-admin,case-allocator,specific-access-approver-admin',N,Y",
            "3,BBA3,'hmcts-admin,hearing-centre-team-leader,hearing-centre-admin,task-supervisor,case-allocator,specific-access-approver-admin',Y,Y",
            "4,BBA3,'hmcts-admin,hearing-centre-admin',N,N",
            "4,BBA3,'hmcts-admin,task-supervisor,hearing-centre-admin',Y,N",
            "4,BBA3,'hmcts-admin,case-allocator,hearing-centre-admin',N,Y",
            "4,BBA3,'hmcts-admin,task-supervisor,case-allocator,hearing-centre-admin',Y,Y",
            "5,BBA3,'clerk,hmcts-admin',N,N",
            "5,BBA3,'clerk,task-supervisor,hmcts-admin',Y,N",
            "5,BBA3,'clerk,case-allocator,hmcts-admin',N,Y",
            "5,BBA3,'clerk,task-supervisor,case-allocator,hmcts-admin',Y,Y",
            "12,BBA3,'hmcts-admin,regional-centre-team-leader,regional-centre-admin,specific-access-approver-admin',N,N",
            "12,BBA3,'hmcts-admin,regional-centre-team-leader,regional-centre-admin,task-supervisor,specific-access-approver-admin',Y,N",
            "12,BBA3,'hmcts-admin,regional-centre-team-leader,regional-centre-admin,case-allocator,specific-access-approver-admin',N,Y",
            "12,BBA3,'hmcts-admin,regional-centre-team-leader,regional-centre-admin,task-supervisor,case-allocator,specific-access-approver-admin',Y,Y",
            "13,BBA3,'hmcts-admin,regional-centre-admin',N,N",
            "13,BBA3,'hmcts-admin,task-supervisor,regional-centre-admin',Y,N",
            "13,BBA3,'hmcts-admin,case-allocator,regional-centre-admin',N,Y",
            "13,BBA3,'hmcts-admin,task-supervisor,case-allocator,regional-centre-admin',Y,Y",
    })
    void shouldReturnSscsAdminMappings(String roleId, String serviceCode, String expectedRoles,
                                             String taskSupervisorFlag, String caseAllocatorFlag) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<String> skillCodes = List.of("sscs", "test", "ctsc");
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
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        roleAssignments.forEach(r -> {
            assertEquals("ADMIN", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());
        });
        List<String> roleNamesWithRegionAttribute = List.of("hearing-centre-team-leader","hearing-centre-admin",
                "specific-access-approver-admin", "clerk", "specific-access-approver-admin", "regional-centre-team-leader",
                "regional-centre-admin");

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                    //assert region
                    if (roleNamesWithRegionAttribute.contains(r.getRoleName())) {
                        assertEquals("LDN", r.getAttributes().get("region").asText());
                    }
                    //assert work types
                    if (("hearing-centre-team-leader").equals(r.getRoleName())) {
                        assertNull(r.getAttributes().get("workTypes"));
                    } else if (("hearing-centre-admin").equals(r.getRoleName())) {
                        assertThat(r.getAttributes().get("workTypes").asText().split(","),
                                arrayContainingInAnyOrder("pre_hearing_work", "hearing_work",
                                        "post_hearing_work", "decision_making_work",
                                        "routine_work", "priority", "applications"));
                    } else if (("task-supervisor").equals(r.getRoleName())) {
                        assertNull(r.getAttributes().get("workTypes"));
                    } else if (("case-allocator").equals(r.getRoleName())) {
                        assertNull(r.getAttributes().get("workTypes"));
                    } else if (("specific-access-approver-admin").equals(r.getRoleName())) {
                        assertEquals("access_requests", r.getAttributes().get("workTypes").asText());
                    } else if (("regional-centre-team-leader").equals(r.getRoleName())) {
                        assertNull(r.getAttributes().get("workTypes"));
                    } else if (("regional-centre-admin").equals(r.getRoleName())) {
                        assertThat(r.getAttributes().get("workTypes").asText().split(","),
                                arrayContainingInAnyOrder("pre_hearing_work", "hearing_work",
                                        "post_hearing_work", "decision_making_work",
                                        "routine_work", "priority", "applications"));
                    }
                });
    }

    @ParameterizedTest
    @CsvSource({
            "9,BBA3,'hmcts-ctsc,ctsc,ctsc-team-leader,specific-access-approver-ctsc',N,N",
            "9,BBA3,'hmcts-ctsc,ctsc,ctsc-team-leader,task-supervisor,specific-access-approver-ctsc',Y,N",
            "9,BBA3,'hmcts-ctsc,ctsc,ctsc-team-leader,case-allocator,specific-access-approver-ctsc',N,Y",
            "9,BBA3,'hmcts-ctsc,ctsc,ctsc-team-leader,task-supervisor,case-allocator,specific-access-approver-ctsc',Y,Y",
            "10,BBA3,'hmcts-ctsc,ctsc',N,N",
            "10,BBA3,'hmcts-ctsc,task-supervisor,ctsc',Y,N",
            "10,BBA3,'hmcts-ctsc,case-allocator,ctsc',N,Y",
            "10,BBA3,'hmcts-ctsc,task-supervisor,case-allocator,ctsc',Y,Y",
    })
    void shouldReturnSscsCtscMappings(String roleId, String serviceCode, String expectedRoles,
                                            String taskSupervisorFlag, String caseAllocatorFlag) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<String> skillCodes = List.of("sscs", "test", "ctsc");
        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(serviceCode);
        cap.setSuspended(false);
        cap.setRoleId(roleId);
        cap.setTaskSupervisorFlag(taskSupervisorFlag);
        cap.setCaseAllocatorFlag(caseAllocatorFlag);
        cap.setSkillCodes(skillCodes);

        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        roleAssignments.forEach(r -> {
            assertEquals("CTSC", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());
            if (("ctsc").equals(r.getRoleName()) || ("ctsc-team-leader").equals(r.getRoleName())) {
                assertEquals(skillCodes,r.getAuthorisations());
            }
        });

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                    //assert work types
                    if (("ctsc").equals(r.getRoleName())) {
                        assertThat(r.getAttributes().get("workTypes").asText().split(","),
                                arrayContainingInAnyOrder("pre_hearing_work", "hearing_work",
                                        "post_hearing_work", "decision_making_work",
                                        "routine_work", "priority", "applications"));
                    } else if (("ctsc-team-leader").equals(r.getRoleName())) {
                        assertNull(r.getAttributes().get("workTypes"));
                    } else if (("task-supervisor").equals(r.getRoleName())) {
                        assertNull(r.getAttributes().get("workTypes"));
                    } else if (("case-allocator").equals(r.getRoleName())) {
                        assertNull(r.getAttributes().get("workTypes"));
                    } else if (("specific-access-approver-ctsc").equals(r.getRoleName())) {
                        assertEquals("access_requests", r.getAttributes().get("workTypes").asText());
                    }
                });
    }

    @ParameterizedTest
    @CsvSource({
            "1,BBA3,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,specific-access-approver-legal-ops',N,N",
            "1,BBA3,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,task-supervisor,specific-access-approver-legal-ops',Y,N",
            "1,BBA3,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,case-allocator,specific-access-approver-legal-ops',N,Y",
            "1,BBA3,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,task-supervisor,case-allocator,specific-access-approver-legal-ops',Y,Y",
            "2,BBA3,'tribunal-caseworker,hmcts-legal-operations',N,N",
            "2,BBA3,'tribunal-caseworker,task-supervisor,hmcts-legal-operations',Y,N",
            "2,BBA3,'tribunal-caseworker,case-allocator,hmcts-legal-operations',N,Y",
            "2,BBA3,'tribunal-caseworker,task-supervisor,case-allocator,hmcts-legal-operations',Y,Y",
            "16,BBA3,'registrar,hmcts-legal-operations',N,N",
            "16,BBA3,'registrar,task-supervisor,hmcts-legal-operations',Y,N",
            "16,BBA3,'registrar,case-allocator,hmcts-legal-operations',N,Y",
            "16,BBA3,'registrar,task-supervisor,case-allocator,hmcts-legal-operations',Y,Y",
    })
    void shouldReturnSscsCaseWorkerMappings(String roleId, String serviceCode, String expectedRoles,
                                                  String taskSupervisorFlag, String caseAllocatorFlag) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();
        List<String> skillCodes = List.of("sscs", "test", "ctsc");
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
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        roleAssignments.forEach(r -> {
            assertEquals("LEGAL_OPERATIONS", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());
        });

        List<String> roleNamesWithRegionAttribute = List.of("tribunal-caseworker");

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                    //assert region
                    if (roleNamesWithRegionAttribute.contains(r.getRoleName())) {
                        assertEquals("LDN", r.getAttributes().get("region").asText());
                    }
                    //assert work types
                    if (("senior-tribunal-caseworker").equals(r.getRoleName())) {
                        assertNull(r.getAttributes().get("workTypes"));
                    } else if (("tribunal-caseworker").equals(r.getRoleName())) {
                        assertThat(r.getAttributes().get("workTypes").asText().split(","),
                                arrayContainingInAnyOrder("pre_hearing_work", "hearing_work",
                                        "post_hearing_work", "decision_making_work",
                                        "routine_work", "priority", "applications"));
                    } else if (("task-supervisor").equals(r.getRoleName())) {
                        assertNull(r.getAttributes().get("workTypes"));
                    } else if (("case-allocator").equals(r.getRoleName())) {
                        assertNull(r.getAttributes().get("workTypes"));
                    } else if (("specific-access-approver-legal-ops").equals(r.getRoleName())) {
                        assertEquals("access_requests", r.getAttributes().get("workTypes").asText());
                    } else if (("registrar").equals(r.getRoleName())) {
                        assertThat(r.getAttributes().get("workTypes").asText().split(","),
                                arrayContainingInAnyOrder("pre_hearing_work", "hearing_work",
                                        "post_hearing_work", "decision_making_work",
                                        "routine_work", "priority", "applications"));
                    }
                });
    }

    @Test
    void shouldNotReturnCtsRoles_disabledFlag() {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();
        List<String> skillCodes = List.of("sscs", "test", "ctsc");
        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode("BBA3");
        cap.setSuspended(false);
        cap.setRoleId("10");
        cap.setSkillCodes(skillCodes);

        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", false));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }
}
