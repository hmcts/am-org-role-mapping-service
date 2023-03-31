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
import static org.junit.jupiter.api.Assertions.assertNull;

@RunWith(MockitoJUnitRunner.class)
class DroolEmploymentStaffOrgRolesTest extends DroolBase {

    @ParameterizedTest
    @CsvSource({
            "3,BHA1,'hearing-centre-team-leader,hearing-centre-admin,hmcts-admin,specific-access-approver-admin',N,N",
            "12,BHA1,'regional-centre-team-leader,regional-centre-admin,hmcts-admin,specific-access-approver-admin'"
                    + ",N,N",
            "12,BHA1,'regional-centre-team-leader,regional-centre-admin,hmcts-admin,specific-access-approver-admin,"
                    + "task-supervisor,case-allocator',Y,Y",
            "12,BHA1,'regional-centre-team-leader,regional-centre-admin,hmcts-admin,specific-access-approver-admin,"
                    + "task-supervisor',Y,N",
            "12,BHA1,'regional-centre-team-leader,regional-centre-admin,hmcts-admin,specific-access-approver-admin,"
                    + "case-allocator',N,Y",

    })
    void shouldReturnEmploymentAdminMappings(String roleId, String serviceCode, String expectedRoles,
                                             String taskSupervisorFlag, String caseAllocatorFlag) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<String> skillCodes = List.of("employment", "test", "ctsc");
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
                buildExecuteKieSession(getFeatureFlags("employment_wa_1_0", true));

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
                 "specific-access-approver-admin");

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals("EMPLOYMENT", r.getAttributes().get("jurisdiction").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                    //assert region
                    if (roleNamesWithRegionAttribute.contains(r.getRoleName())) {
                        assertEquals("LDN", r.getAttributes().get("region").asText());
                    }
                    //assert work types
                    if (("hearing-centre-team-leader").equals(r.getRoleName())) {
                        assertEquals("hearing_work,routine_work,applications,amendments",
                                r.getAttributes().get("workTypes").asText());
                    } else if (("hearing-centre-admin").equals(r.getRoleName())) {
                        assertEquals("hearing_work,routine_work,applications,amendments",
                                r.getAttributes().get("workTypes").asText());
                    }


                });
    }

    @ParameterizedTest
    @CsvSource({
            "10,BHA1,'ctsc,hmcts-ctsc',N,N",
            "9,BHA1,'ctsc-team-leader,ctsc,hmcts-ctsc,specific-access-approver-ctsc',N,N",
            "9,BHA1,'ctsc-team-leader,ctsc,hmcts-ctsc,task-supervisor,case-allocator,"
                    + "specific-access-approver-ctsc',Y,Y",
            "9,BHA1,'ctsc-team-leader,ctsc,hmcts-ctsc,case-allocator,specific-access-approver-ctsc',N,Y",
            "9,BHA1,'ctsc-team-leader,ctsc,hmcts-ctsc,task-supervisor,specific-access-approver-ctsc',Y,N"
    })
    void shouldReturnEmploymentCtscMappings(String roleId, String serviceCode, String expectedRoles,
                                            String taskSupervisorFlag, String caseAllocatorFlag) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<String> skillCodes = List.of("employment", "test", "ctsc");
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
                buildExecuteKieSession(getFeatureFlags("employment_wa_1_0", true));


        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        roleAssignments.forEach(r -> {
            assertEquals("CTSC", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());
            if (("ctsc").equals(r.getRoleName())) {
                assertEquals(skillCodes,r.getAuthorisations());
            }

        });

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals("EMPLOYMENT", r.getAttributes().get("jurisdiction").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                    //assert work types
                    if (("ctsc").equals(r.getRoleName())) {
                        assertEquals("hearing_work,routine_work,applications,amendments",
                                r.getAttributes().get("workTypes").asText());
                    } else if (("ctsc-team-leader").equals(r.getRoleName())) {
                        assertEquals("hearing_work,routine_work,applications,amendments",
                                r.getAttributes().get("workTypes").asText());
                    }
                });
    }


    @ParameterizedTest
    @CsvSource({
            "2,BHA1,'tribunal-caseworker,hmcts-legal-operations',N,N",
            "1,BHA1,'tribunal-caseworker,senior-tribunal-caseworker,hmcts-legal-operations,"
                    + "specific-access-approver-legal-ops',N,N",
            "1,BHA1,'tribunal-caseworker,senior-tribunal-caseworker,hmcts-legal-operations,task-supervisor,"
                    + "specific-access-approver-legal-ops',Y,N",
            "1,BHA1,'tribunal-caseworker,senior-tribunal-caseworker,hmcts-legal-operations,case-allocator,"
                    + "specific-access-approver-legal-ops',N,Y",
            "1,BHA1,'tribunal-caseworker,senior-tribunal-caseworker,hmcts-legal-operations,"
                    + "task-supervisor,case-allocator,specific-access-approver-legal-ops',Y,Y"
    })
    void shouldReturnEmploymentCaseWorkerMappings(String roleId, String serviceCode, String expectedRoles,
                                                  String taskSupervisorFlag, String caseAllocatorFlag) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();
        List<String> skillCodes = List.of("employment", "test", "ctsc");
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
                buildExecuteKieSession(getFeatureFlags("employment_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        roleAssignments.forEach(r -> {
            assertEquals("LEGAL_OPERATIONS", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());
        });

        List<String> roleNamesWithRegionAttribute = List.of("tribunal-caseworker", "senior-tribunal-caseworker",
                "specific-access-approver-legal-ops");

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals("EMPLOYMENT", r.getAttributes().get("jurisdiction").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                    //assert region
                    if (roleNamesWithRegionAttribute.contains(r.getRoleName())) {
                        assertEquals("LDN", r.getAttributes().get("region").asText());
                    }
                    //assert work types
                    if (("senior-tribunal-caseworker").equals(r.getRoleName())) {
                        assertNull(r.getAttributes().get("workTypes"));
                    } else if (("tribunal-caseworker").equals(r.getRoleName())) {
                        assertEquals("hearing_work,routine_work,applications,amendments",
                                r.getAttributes().get("workTypes").asText());
                    } else if (Objects.equals("specific-access-approver-legal-ops", r.getRoleName())) {
                        assertEquals("access_requests",
                                r.getAttributes().get("workTypes").asText());
                    }
                });
    }

    @Test
    void shouldNotReturnCtsRoles_disabledFlag() {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();
        List<String> skillCodes = List.of("employment", "test", "ctsc");
        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode("BHA1");
        cap.setSuspended(false);
        cap.setRoleId("10");
        cap.setSkillCodes(skillCodes);

        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("employment_wa_1_0", false));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }
}
