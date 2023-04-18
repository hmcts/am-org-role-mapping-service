package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

@RunWith(MockitoJUnitRunner.class)
class DroolPrivateLawStaffOrgRolesTest extends DroolBase {

    @ParameterizedTest
    @CsvSource({
            "10,ABA5,'ctsc,hmcts-ctsc',N,N",
            "9,ABA5,'ctsc-team-leader,ctsc,hmcts-ctsc,specific-access-approver-ctsc',N,N",
            "9,ABA5,'ctsc-team-leader,ctsc,hmcts-ctsc,task-supervisor,case-allocator,"
                    + "specific-access-approver-ctsc',Y,Y",
            "9,ABA5,'ctsc-team-leader,ctsc,hmcts-ctsc,case-allocator,specific-access-approver-ctsc',N,Y",
            "9,ABA5,'ctsc-team-leader,ctsc,hmcts-ctsc,task-supervisor,specific-access-approver-ctsc',Y,N"
    })
    void shouldReturnPrivateLawCtscMappings(String roleId, String serviceCode, String expectedRoles,
                                            String taskSupervisorFlag, String caseAllocatorFlag) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<String> skillCodes = List.of("privatelaw", "test", "ctsc");
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
                buildExecuteKieSession(getFeatureFlags("privatelaw_wa_1_0", true));


        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        roleAssignments.forEach(r -> {
            assertEquals("CTSC", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());
            if (!r.getRoleName().contains("hmcts")) {
                assertEquals(skillCodes,r.getAuthorisations());
            }

        });

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals("PRIVATELAW", r.getAttributes().get("jurisdiction").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                    //assert work types
                    if (("ctsc").equals(r.getRoleName())) {
                        assertEquals("routine_work,hearing_work,applications",
                                r.getAttributes().get("workTypes").asText());
                    } else if (("ctsc-team-leader").equals(r.getRoleName())) {
                        assertEquals("routine_work,hearing_work,applications",
                                r.getAttributes().get("workTypes").asText());
                    }
                });
    }

    @ParameterizedTest
    @CsvSource({
            "4,ABA5,'hearing-centre-admin,hmcts-admin',N,N",
            "3,ABA5,'hearing-centre-team-leader,hearing-centre-admin,hmcts-admin,"
                    + "specific-access-approver-admin',N,N",
            "3,ABA5,'hearing-centre-team-leader,hearing-centre-admin,hmcts-admin,task-supervisor,"
                    + "specific-access-approver-admin',Y,N",
            "3,ABA5,'hearing-centre-team-leader,hearing-centre-admin,hmcts-admin,case-allocator,"
                    + "specific-access-approver-admin',N,Y",
            "3,ABA5,'hearing-centre-team-leader,hearing-centre-admin,hmcts-admin,task-supervisor,case-allocator,"
                     + "specific-access-approver-admin',Y,Y",
    })
    void shouldReturnPrivateLawAdminMappings(String roleId, String serviceCode, String expectedRoles,
                                             String taskSupervisorFlag, String caseAllocatorFlag) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();
        List<String> skillCodes = List.of("privatelaw", "test", "ctsc");
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
                buildExecuteKieSession(getFeatureFlags("privatelaw_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        roleAssignments.forEach(r -> {
            assertEquals("ADMIN", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());
            if (!r.getRoleName().contains("hmcts")) {
                assertEquals(skillCodes, r.getAuthorisations());
            }
        });

        List<String> roleNamesWithRegionAttribute = List.of("hearing-centre-team-leader", "task-supervisor",
                "case-allocator", "hearing-centre-admin");

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals("PRIVATELAW", r.getAttributes().get("jurisdiction").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                    //assert region
                    if (roleNamesWithRegionAttribute.contains(r.getRoleName())) {
                        assertEquals("LDN", r.getAttributes().get("region").asText());
                    }
                    //assert work types
                    if (("hearing-centre-team-leader").equals(r.getRoleName())) {
                        assertEquals("routine_work,hearing_work,applications",
                                r.getAttributes().get("workTypes").asText());
                    } else if (("hearing-centre-admin").equals(r.getRoleName())) {
                        assertEquals("routine_work,hearing_work,applications",
                                r.getAttributes().get("workTypes").asText());
                    }


                });
    }

    @ParameterizedTest
    @CsvSource({
            "2,ABA5,'tribunal-caseworker,hmcts-legal-operations',N,N",
            "1,ABA5,'senior-tribunal-caseworker,hmcts-legal-operations,specific-access-approver-legal-ops',N,N",
            "1,ABA5,'senior-tribunal-caseworker,hmcts-legal-operations,task-supervisor,"
                    + "specific-access-approver-legal-ops',Y,N",
            "1,ABA5,'senior-tribunal-caseworker,hmcts-legal-operations,case-allocator,"
                    + "specific-access-approver-legal-ops',N,Y",
            "1,ABA5,'senior-tribunal-caseworker,hmcts-legal-operations,task-supervisor,case-allocator,"
                    + "specific-access-approver-legal-ops',Y,Y",
    })
    void shouldReturnPrivateLawCaseWorkerMappings(String roleId, String serviceCode, String expectedRoles,
                                             String taskSupervisorFlag, String caseAllocatorFlag) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();
        List<String> skillCodes = List.of("privatelaw", "test", "ctsc");
        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(serviceCode);
        cap.setSuspended(false);
        cap.setRoleId(roleId);
        cap.setTaskSupervisorFlag(taskSupervisorFlag);
        cap.setCaseAllocatorFlag(caseAllocatorFlag);
        cap.setRegionId("LDN");
        cap.setSkillCodes(skillCodes);

        allProfiles.add(cap);

        List<FeatureFlag> featureFlags = new ArrayList<>();
        featureFlags.add(FeatureFlag.builder().flagName("privatelaw_wa_1_0").status(true).build());
        featureFlags.add(FeatureFlag.builder().flagName("privatelaw_wa_1_1").status(false).build());

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(featureFlags);

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        roleAssignments.forEach(r -> {
            assertEquals("LEGAL_OPERATIONS", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());
            if (!r.getRoleName().contains("hmcts")) {
                assertEquals(skillCodes, r.getAuthorisations());
            }
        });

        List<String> roleNamesWithRegionAttribute = List.of("tribunal-caseworker", "senior-tribunal-caseworker",
                "task-supervisor", "case-allocator");

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals("PRIVATELAW", r.getAttributes().get("jurisdiction").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                    //assert region
                    if (roleNamesWithRegionAttribute.contains(r.getRoleName())) {
                        assertEquals("LDN", r.getAttributes().get("region").asText());
                    }
                    //assert work types
                    if (("senior-tribunal-caseworker").equals(r.getRoleName())) {
                        assertEquals("decision_making_work",
                                r.getAttributes().get("workTypes").asText());
                    } else if (("tribunal-caseworker").equals(r.getRoleName())) {
                        assertEquals("routine_work,hearing_work,applications",
                                r.getAttributes().get("workTypes").asText());
                    } else if (Objects.equals("task-supervisor", r.getRoleName())) {
                        assertEquals("routine_work,hearing_work,applications",
                                r.getAttributes().get("workTypes").asText());
                    }
                });
    }

    @ParameterizedTest
    @CsvSource({
            "2,ABA5,'tribunal-caseworker',N,N",
    })
    void shouldReturnPrivateLawCaseWorkerMappingsUpdatedWorkTypes(String roleId,
                                                                  String serviceCode,
                                                                  String expectedRoles,
                                                                  String taskSupervisorFlag,
                                                                  String caseAllocatorFlag) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();
        List<String> skillCodes = List.of("privatelaw", "test", "ctsc");
        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(serviceCode);
        cap.setSuspended(false);
        cap.setRoleId(roleId);
        cap.setTaskSupervisorFlag(taskSupervisorFlag);
        cap.setCaseAllocatorFlag(caseAllocatorFlag);
        cap.setRegionId("LDN");
        cap.setSkillCodes(skillCodes);

        allProfiles.add(cap);

        List<FeatureFlag> featureFlags = new ArrayList<>();
        featureFlags.add(FeatureFlag.builder().flagName("privatelaw_wa_1_0").status(false).build());
        featureFlags.add(FeatureFlag.builder().flagName("privatelaw_wa_1_1").status(true).build());

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(featureFlags);

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        roleAssignments.forEach(r -> {
            assertEquals("LEGAL_OPERATIONS", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());
            if (!r.getRoleName().contains("hmcts")) {
                assertEquals(skillCodes, r.getAuthorisations());
            }
        });

        List<String> roleNamesWithRegionAttribute = List.of("tribunal-caseworker", "senior-tribunal-caseworker",
                "task-supervisor", "case-allocator");

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals("PRIVATELAW", r.getAttributes().get("jurisdiction").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                    //assert region
                    if (roleNamesWithRegionAttribute.contains(r.getRoleName())) {
                        assertEquals("LDN", r.getAttributes().get("region").asText());
                    }
                    //assert work types
                    if (("senior-tribunal-caseworker").equals(r.getRoleName())) {
                        assertEquals("decision_making_work",
                                r.getAttributes().get("workTypes").asText());
                    } else if (("tribunal-caseworker").equals(r.getRoleName())) {
                        assertEquals("routine_work,hearing_work,applications,decision_making_work",
                                r.getAttributes().get("workTypes").asText());
                    } else if (Objects.equals("task-supervisor", r.getRoleName())) {
                        assertEquals("routine_work,hearing_work,applications",
                                r.getAttributes().get("workTypes").asText());
                    }
                });
    }

    @Test
    void shouldNotReturnCtsRoles_disabledFlag() {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();
        List<String> skillCodes = List.of("privatelaw", "test", "ctsc");
        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode("ABA5");
        cap.setSuspended(false);
        cap.setRoleId("10");
        cap.setSkillCodes(skillCodes);

        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("privatelaw_wa_1_0", false));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }
}