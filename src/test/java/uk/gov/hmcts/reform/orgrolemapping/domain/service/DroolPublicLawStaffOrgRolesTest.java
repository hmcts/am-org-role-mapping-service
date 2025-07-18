package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DroolPublicLawStaffOrgRolesTest extends DroolBase {

    @ParameterizedTest
    @CsvSource({
        "10,ABA3,'ctsc,hmcts-ctsc',N,N",
        "10,ABA3,'ctsc,hmcts-ctsc,task-supervisor',Y,N",
        "9,ABA3,'ctsc-team-leader,ctsc,hmcts-ctsc,specific-access-approver-ctsc',N,N",
        "9,ABA3,'ctsc-team-leader,ctsc,hmcts-ctsc,specific-access-approver-ctsc,task-supervisor,"
                + "case-allocator',Y,Y",
        "9,ABA3,'ctsc-team-leader,ctsc,hmcts-ctsc,specific-access-approver-ctsc,case-allocator',N,Y",
        "9,ABA3,'ctsc-team-leader,ctsc,hmcts-ctsc,specific-access-approver-ctsc,task-supervisor',Y,N"
    })
    void shouldReturnPublicLawCtscMappings(String roleId, String serviceCode, String expectedRoles,
                                           String taskSupervisorFlag, String caseAllocatorFlag) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<String> skillCodes = List.of("publiclaw", "test", "ctsc");
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
                buildExecuteKieSession(getFeatureFlags(true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        roleAssignments.forEach(r -> {
            assertEquals("CTSC", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());

        });

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals("PUBLICLAW", r.getAttributes().get("jurisdiction").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                    assertEquals(skillCodes,r.getAuthorisations());
                    //assert work types
                    if (("ctsc").equals(r.getRoleName())) {
                        assertEquals("routine_work,applications,decision_making_work,error_management",
                                r.getAttributes().get("workTypes").asText());

                    } else if (("ctsc-team-leader").equals(r.getRoleName())) {
                        assertEquals("routine_work,applications,decision_making_work,access_requests,error_management",
                                r.getAttributes().get("workTypes").asText());

                    } else if (Objects.equals("task-supervisor", r.getRoleName())) {
                        assertEquals("access_requests",
                                r.getAttributes().get("workTypes").asText());

                    } else if (Objects.equals("case-allocator", r.getRoleName())) {
                        assertEquals("routine_work",
                                r.getAttributes().get("workTypes").asText());
                    }
                });
    }

    @ParameterizedTest
    @CsvSource({
        "4,ABA3,'hearing-centre-admin,hmcts-admin',N,N",
        "4,ABA3,'hearing-centre-admin,hmcts-admin,task-supervisor',Y,N",
        "3,ABA3,'hearing-centre-team-leader,hearing-centre-admin,hmcts-admin,specific-access-approver-admin',N,N",
        "3,ABA3,'hearing-centre-team-leader,hearing-centre-admin,hmcts-admin,specific-access-approver-admin,"
                + "task-supervisor,case-allocator',Y,Y",
        "3,ABA3,'hearing-centre-team-leader,hearing-centre-admin,hmcts-admin,specific-access-approver-admin,"
                + "task-supervisor',Y,N",
        "3,ABA3,'hearing-centre-team-leader,hearing-centre-admin,hmcts-admin,specific-access-approver-admin,"
                + "case-allocator',N,Y",
    })
    void shouldReturnPublicLawAdminMappings(String roleId, String serviceCode, String expectedRoles,
                                             String taskSupervisorFlag, String caseAllocatorFlag) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<String> skillCodes = List.of("publiclaw", "test", "ctsc");
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
                buildExecuteKieSession(getFeatureFlags(true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        roleAssignments.forEach(r -> {
            assertEquals("ADMIN", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());
        });
        List<String> roleNamesWithRegionAttribute = List.of("hearing-centre-team-leader","case-allocator",
                 "task-supervisor", "specific-access-approver-admin", "hearing-centre-admin");

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals("PUBLICLAW", r.getAttributes().get("jurisdiction").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                    assertEquals(skillCodes,r.getAuthorisations());
                    //assert region
                    if (roleNamesWithRegionAttribute.contains(r.getRoleName())) {
                        assertEquals("LDN", r.getAttributes().get("region").asText());
                    }
                    //assert work types
                    if (("hearing-centre-team-leader").equals(r.getRoleName())) {
                        assertEquals("routine_work,access_requests",
                                r.getAttributes().get("workTypes").asText());
                    } else if (("hearing-centre-admin").equals(r.getRoleName())) {
                        assertEquals("routine_work",
                                r.getAttributes().get("workTypes").asText());
                    } else if (Objects.equals("task-supervisor", r.getRoleName())) {
                        assertEquals("access_requests",
                                r.getAttributes().get("workTypes").asText());
                    } else if (Objects.equals("case-allocator", r.getRoleName())) {
                        assertEquals("routine_work",
                                r.getAttributes().get("workTypes").asText());
                    }


                });
    }

    @ParameterizedTest
    @CsvSource({
        "2,ABA3,'tribunal-caseworker,hmcts-legal-operations',N,N",
        "2,ABA3,'tribunal-caseworker,hmcts-legal-operations,task-supervisor',Y,N",
        "1,ABA3,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,"
                + "specific-access-approver-legal-ops',N,N",
        "1,ABA3,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,"
                + "specific-access-approver-legal-ops,task-supervisor',Y,N",
        "1,ABA3,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,"
                + "specific-access-approver-legal-ops,case-allocator',N,Y",
        "1,ABA3,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,"
                + "specific-access-approver-legal-ops,task-supervisor,case-allocator',Y,Y",
    })
    void shouldReturnPublicLawCaseWorkerMappings(String roleId, String serviceCode, String expectedRoles,
                                                  String taskSupervisorFlag, String caseAllocatorFlag) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<String> skillCodes = List.of("publiclaw", "test", "ctsc");
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
                buildExecuteKieSession(getFeatureFlags(true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        roleAssignments.forEach(r -> {
            assertEquals("LEGAL_OPERATIONS", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());
        });

        List<String> roleNamesWithRegionAttribute = List.of("senior-tribunal-caseworker",
                "task-supervisor", "case-allocator", "specific-access-approver-legal-ops", "tribunal-caseworker");

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals("PUBLICLAW", r.getAttributes().get("jurisdiction").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                    assertEquals(skillCodes,r.getAuthorisations());
                    //assert region
                    if (roleNamesWithRegionAttribute.contains(r.getRoleName())) {
                        assertEquals("LDN", r.getAttributes().get("region").asText());
                    }
                    //assert work types
                    if (("senior-tribunal-caseworker").equals(r.getRoleName())) {
                        assertEquals("hearing_work,routine_work,decision_making_work,applications,access_requests",
                                r.getAttributes().get("workTypes").asText());
                    } else if (("tribunal-caseworker").equals(r.getRoleName())) {
                        assertEquals("hearing_work,decision_making_work,applications,routine_work",
                                r.getAttributes().get("workTypes").asText());
                    } else if (Objects.equals("task-supervisor", r.getRoleName())) {
                        assertEquals("access_requests",
                                r.getAttributes().get("workTypes").asText());
                    } else if (Objects.equals("case-allocator", r.getRoleName())) {
                        assertEquals("routine_work",
                                r.getAttributes().get("workTypes").asText());
                    }

                });
    }

    @Test
    void shouldNotReturnCtsRoles_disabledFlag() {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<String> skillCodes = List.of("publiclaw", "test", "ctsc");
        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode("ABA3");
        cap.setSuspended(false);
        cap.setRoleId("10");
        cap.setSkillCodes(skillCodes);
        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags(false));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

    List<FeatureFlag> getFeatureFlags(Boolean status) {
        return getAllFeatureFlagsToggleByJurisdiction("PUBLICLAW", status);
    }

}
