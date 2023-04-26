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
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
class DroolPublicLawStaffOrgRolesTest extends DroolBase {

    @ParameterizedTest
    @CsvSource({
            "10,ABA3,'ctsc,hmcts-ctsc,hearing-viewer,hearing-manager',N,N",
            "9,ABA3,'ctsc-team-leader,hmcts-ctsc,specific-access-approver-ctsc,hearing-viewer,hearing-manager',N,N",
            "9,ABA3,'ctsc-team-leader,hmcts-ctsc,specific-access-approver-ctsc,task-supervisor,case-allocator"
                    + ",hearing-viewer,hearing-manager',Y,Y",
            "9,ABA3,'ctsc-team-leader,hmcts-ctsc,specific-access-approver-ctsc,case-allocator,hearing-viewer,"
                    + "hearing-manager',N,Y",
            "9,ABA3,'ctsc-team-leader,hmcts-ctsc,specific-access-approver-ctsc,task-supervisor,hearing-viewer,"
                    + "hearing-manager',Y,N"
    })
    void shouldReturnPublicLawCtscMappings(String roleId, String serviceCode, String expectedRoles,
                                           String taskSupervisorFlag, String caseAllocatorFlag) {

        allProfiles.clear();
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
                buildExecuteKieSession(getFeatureFlags("publiclaw_wa_1_0", true));

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
                    if (!r.getRoleName().contains("hearing")) {
                        assertEquals(skillCodes, r.getAuthorisations());
                    }
                    //assert work types
                    if (("ctsc").equals(r.getRoleName())) {
                        assertEquals("routine_work",
                                r.getAttributes().get("workTypes").asText());

                    } else if (("ctsc-team-leader").equals(r.getRoleName())) {
                        assertEquals("routine_work,access_requests",
                                r.getAttributes().get("workTypes").asText());
                    }
                });
    }

    @ParameterizedTest
    @CsvSource({
            "4,ABA3,'hearing-centre-admin,hmcts-admin,hearing-viewer,hearing-manager',N,N",
            "3,ABA3,'hearing-centre-team-leader,hmcts-admin,specific-access-approver-admin,"
                    + "hearing-viewer,hearing-manager',N,N",
            "3,ABA3,'hearing-centre-team-leader,hmcts-admin,specific-access-approver-admin,"
                    + "task-supervisor,case-allocator,hearing-viewer,hearing-manager',Y,Y",
            "3,ABA3,'hearing-centre-team-leader,hmcts-admin,specific-access-approver-admin,"
                    + "task-supervisor,hearing-viewer,hearing-manager',Y,N",
            "3,ABA3,'hearing-centre-team-leader,hmcts-admin,specific-access-approver-admin,case-allocator,"
                    + "hearing-viewer,hearing-manager',N,Y",

    })
    void shouldReturnPublicLawAdminMappings(String roleId, String serviceCode, String expectedRoles,
                                             String taskSupervisorFlag, String caseAllocatorFlag) {

        allProfiles.clear();
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
                buildExecuteKieSession(getFeatureFlags("publiclaw_wa_1_0", true));

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
                    if (!r.getRoleName().contains("hearing")) {
                        assertEquals(skillCodes, r.getAuthorisations());
                    }
                    //assert region
                    if (roleNamesWithRegionAttribute.contains(r.getRoleName())) {
                        assertEquals("LDN", r.getAttributes().get("region").asText());
                    }
                    //assert work types
                    if (("hearing-centre-team-leader").equals(r.getRoleName())) {
                        assertEquals("routine_work,access_requests",
                                r.getAttributes().get("workTypes").asText());
                    } else if (("hearing-centre-admin").equals(r.getRoleName())) {
                        assertEquals("routine_work,decision_making_work",
                                r.getAttributes().get("workTypes").asText());
                    }


                });
    }

    @ParameterizedTest
    @CsvSource({
            "2,ABA3,'tribunal-caseworker,hmcts-legal-operations,hearing-viewer,hearing-manager',N,N",
            "1,ABA3,'senior-tribunal-caseworker,hmcts-legal-operations,specific-access-approver-legal-ops,"
                    + "hearing-viewer,hearing-manager',N,N",
            "1,ABA3,'senior-tribunal-caseworker,hmcts-legal-operations,specific-access-approver-legal-ops,"
                    + "task-supervisor,hearing-viewer,hearing-manager',Y,N",
            "1,ABA3,'senior-tribunal-caseworker,hmcts-legal-operations,specific-access-approver-legal-ops,"
                    + "case-allocator,hearing-viewer,hearing-manager',N,Y",
            "1,ABA3,'senior-tribunal-caseworker,hmcts-legal-operations,specific-access-approver-legal-ops,"
                    + "task-supervisor,case-allocator,hearing-viewer,hearing-manager',Y,Y",
    })
    void shouldReturnPublicLawCaseWorkerMappings(String roleId, String serviceCode, String expectedRoles,
                                                  String taskSupervisorFlag, String caseAllocatorFlag) {

        allProfiles.clear();
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
                buildExecuteKieSession(getFeatureFlags("publiclaw_wa_1_0", true));

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
                    if (!r.getRoleName().contains("hearing")) {
                        assertEquals(skillCodes, r.getAuthorisations());
                    }
                    //assert region
                    if (roleNamesWithRegionAttribute.contains(r.getRoleName())) {
                        assertEquals("LDN", r.getAttributes().get("region").asText());
                    }
                    //assert work types
                    if (("senior-tribunal-caseworker").equals(r.getRoleName())) {
                        assertEquals("hearing_work,decision_making_work,applications,access_requests",
                                r.getAttributes().get("workTypes").asText());
                    } else if (("tribunal-caseworker").equals(r.getRoleName())) {
                        assertEquals("hearing_work,decision_making_work,applications",
                                r.getAttributes().get("workTypes").asText());
                    } else if (Objects.equals("task-supervisor", r.getRoleName())) {
                        assertEquals("routine_work,access_requests",
                                r.getAttributes().get("workTypes").asText());
                    } else if (Objects.equals("case-allocator", r.getRoleName())) {
                        assertEquals("routine_work",
                                r.getAttributes().get("workTypes").asText());
                    }

                });
    }

    @Test
    void shouldNotReturnCtsRoles_disabledFlag() {

        allProfiles.clear();
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
                buildExecuteKieSession(getFeatureFlags("publiclaw_wa_1_0", false));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("hearing-manager", "hearing-viewer"));
    }
}
