package uk.gov.hmcts.reform.orgrolemapping.domain.service;


import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

@RunWith(MockitoJUnitRunner.class)
class DroolPrivateLawStaffOrgRolesTest extends DroolBase {

    static final String REGION_ID = "LDN";
    static final String JURISDICTION = "PRIVATELAW";
    static List<String> SKILL_CODES = List.of("privatelaw", "test", "ctsc");

    static Map<String, String> expectedRoleNameWorkTypesMap = new HashMap<>();

    {
        expectedRoleNameWorkTypesMap.put("hmcts-admin", null);
        expectedRoleNameWorkTypesMap.put("hearing-centre-team-leader", "routine_work,hearing_work,applications");
        expectedRoleNameWorkTypesMap.put("hmcts-ctsc", null);
        expectedRoleNameWorkTypesMap.put("ctsc", "routine_work,hearing_work,applications");
        expectedRoleNameWorkTypesMap.put("ctsc-team-leader", "routine_work,hearing_work,applications");
        expectedRoleNameWorkTypesMap.put("hearing-centre-admin", "routine_work,hearing_work,applications");
        expectedRoleNameWorkTypesMap.put("senior-tribunal-caseworker", "decision_making_work");
        expectedRoleNameWorkTypesMap.put("tribunal-caseworker", "routine_work,hearing_work,applications,"
                + "decision_making_work");
        expectedRoleNameWorkTypesMap.put("hmcts-legal-operations", null);
        expectedRoleNameWorkTypesMap.put("task-supervisor", "routine_work,hearing_work,applications");
        expectedRoleNameWorkTypesMap.put("case-allocator", null);
        expectedRoleNameWorkTypesMap.put("specific-access-approver-ctsc", "access_requests");
        expectedRoleNameWorkTypesMap.put("specific-access-approver-admin", "access_requests");
        expectedRoleNameWorkTypesMap.put("specific-access-approver-legal-ops", "access_requests");
        expectedRoleNameWorkTypesMap.put("caseworker-privatelaw-externaluser-viewonly", null);
    }

    static void assertCommonRoleAssignmentAttributes(RoleAssignment r, String roleId,
                                                     RoleCategory expectedRoleCategory) {
        assertEquals(ActorIdType.IDAM, r.getActorIdType());
        assertEquals(UserAccessProfileBuilder.ID1, r.getActorId());
        assertEquals(RoleType.ORGANISATION, r.getRoleType());
        assertEquals(expectedRoleCategory, r.getRoleCategory());

        String primaryLocation = null;
        if (r.getAttributes().get("primaryLocation") != null) {
            primaryLocation = r.getAttributes().get("primaryLocation").asText();
        }

        if (List.of("hmcts-judiciary", "hmcts-ctsc", "hmcts-admin","hmcts-legal-operations").contains(
                r.getRoleName())) {
            assertEquals(Classification.PRIVATE, r.getClassification());
            assertEquals(GrantType.BASIC, r.getGrantType());
            assertEquals(null, r.getAttributes().get("jurisdiction"));
            assertTrue(r.isReadOnly());
            assertNull(primaryLocation);
            assertNull(r.getAttributes().get("region"));
        } else {
            assertEquals(Classification.PUBLIC, r.getClassification());
            assertEquals(GrantType.STANDARD, r.getGrantType());
            assertEquals(JURISDICTION, r.getAttributes().get("jurisdiction").asText());
            assertFalse(r.isReadOnly());
            assertEquals(UserAccessProfileBuilder.PRIMARY_LOCATION_ID, primaryLocation);
            assertEquals(SKILL_CODES,r.getAuthorisations());
            if (List.of("9", "10").contains(roleId)) {
                assertNull(r.getAttributes().get("region"));
            } else {
                assertEquals(REGION_ID, r.getAttributes().get("region").asText());
            }
        }

        String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());

        String actualWorkTypes = null;
        if (r.getAttributes().get("workTypes") != null) {
            actualWorkTypes = r.getAttributes().get("workTypes").asText();
        }
        assertEquals(expectedWorkTypes, actualWorkTypes);
    }

    @ParameterizedTest
    @CsvSource({
        "10,ABA5,'ctsc,hmcts-ctsc',N,N,CTSC",
        "9,ABA5,'ctsc-team-leader,ctsc,hmcts-ctsc,specific-access-approver-ctsc',N,N,CTSC",
        "9,ABA5,'ctsc-team-leader,ctsc,hmcts-ctsc,task-supervisor,case-allocator,"
                + "specific-access-approver-ctsc',Y,Y,CTSC",
        "9,ABA5,'ctsc-team-leader,ctsc,hmcts-ctsc,case-allocator,specific-access-approver-ctsc',N,Y,CTSC",
        "9,ABA5,'ctsc-team-leader,ctsc,hmcts-ctsc,task-supervisor,specific-access-approver-ctsc',Y,N,CTSC",
        "4,ABA5,'hearing-centre-admin,hmcts-admin',N,N,ADMIN",
        "3,ABA5,'hearing-centre-team-leader,hearing-centre-admin,hmcts-admin,"
                + "specific-access-approver-admin',N,N,ADMIN",
        "3,ABA5,'hearing-centre-team-leader,hearing-centre-admin,hmcts-admin,task-supervisor,"
                + "specific-access-approver-admin',Y,N,ADMIN",
        "3,ABA5,'hearing-centre-team-leader,hearing-centre-admin,hmcts-admin,case-allocator,"
                + "specific-access-approver-admin',N,Y,ADMIN",
        "3,ABA5,'hearing-centre-team-leader,hearing-centre-admin,hmcts-admin,task-supervisor,case-allocator,"
                + "specific-access-approver-admin',Y,Y,ADMIN",
        "2,ABA5,'tribunal-caseworker,hmcts-legal-operations',N,N,LEGAL_OPERATIONS",
        "1,ABA5,'senior-tribunal-caseworker,hmcts-legal-operations,specific-access-approver-legal-ops',N,N,"
                + "LEGAL_OPERATIONS",
        "1,ABA5,'senior-tribunal-caseworker,hmcts-legal-operations,task-supervisor,"
                + "specific-access-approver-legal-ops',Y,N,LEGAL_OPERATIONS",
        "1,ABA5,'senior-tribunal-caseworker,hmcts-legal-operations,case-allocator,"
                + "specific-access-approver-legal-ops',N,Y,LEGAL_OPERATIONS",
        "1,ABA5,'senior-tribunal-caseworker,hmcts-legal-operations,task-supervisor,case-allocator,"
                + "specific-access-approver-legal-ops',Y,Y,LEGAL_OPERATIONS",
    })
    void shouldReturnPrivateLawMappings(String roleId, String serviceCode, String expectedRoles,
                                            String taskSupervisorFlag, String caseAllocatorFlag,
                                            String expectedRoleCategory) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(serviceCode);
        cap.setSuspended(false);
        cap.setRoleId(roleId);
        cap.setTaskSupervisorFlag(taskSupervisorFlag);
        cap.setCaseAllocatorFlag(caseAllocatorFlag);
        cap.setSkillCodes(SKILL_CODES);
        cap.setRegionId(REGION_ID);

        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags());


        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));

        for (RoleAssignment r : roleAssignments) {
            assertCommonRoleAssignmentAttributes(r, roleId, RoleCategory.valueOf(expectedRoleCategory));
        }
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
                buildExecuteKieSession(
                        List.of(FeatureFlag.builder().flagName("privatelaw_wa_1_0").status(false).build(),
                                FeatureFlag.builder().flagName("privatelaw_wa_1_1").status(false).build())
                );

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }


    List<FeatureFlag> getFeatureFlags() {
        return List.of(FeatureFlag.builder().flagName("privatelaw_wa_1_0").status(true).build(),
                FeatureFlag.builder().flagName("privatelaw_wa_1_1").status(true).build());
    }
}