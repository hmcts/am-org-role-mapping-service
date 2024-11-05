package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

@RunWith(MockitoJUnitRunner.class)
class DroolCivilStaffOrgRolesTest extends DroolBase {

    static final String SERVICE_CODE_1 = "AAA6";
    static final String SERVICE_CODE_2 = "AAA7";
    static final String REGION_ID = "region1";
    static final String JURISDICTION = "CIVIL";
    static final String ROLE_TYPE = "ORGANISATION";

    static Map<String, String> expectedRoleNameWorkTypesMap = new HashMap<>();

    static {
        expectedRoleNameWorkTypesMap.put("hmcts-admin", null);
        expectedRoleNameWorkTypesMap.put("hearing-centre-team-leader", "hearing_work,access_requests");
        expectedRoleNameWorkTypesMap.put("hmcts-ctsc", null);
        expectedRoleNameWorkTypesMap.put("ctsc", "routine_work");
        expectedRoleNameWorkTypesMap.put("ctsc-team-leader", "routine_work,access_requests");
        expectedRoleNameWorkTypesMap.put("hearing-centre-admin",
                "hearing_work,multi_track_hearing_work,intermediate_track_hearing_work,routine_work");
        expectedRoleNameWorkTypesMap.put("senior-tribunal-caseworker", "decision_making_work,access_requests");
        expectedRoleNameWorkTypesMap.put("tribunal-caseworker", "decision_making_work");
        expectedRoleNameWorkTypesMap.put("hmcts-legal-operations", null);
        expectedRoleNameWorkTypesMap.put("nbc-team-leader", "routine_work,access_requests");
        expectedRoleNameWorkTypesMap.put("national-business-centre", "routine_work");
        expectedRoleNameWorkTypesMap.put("task-supervisor", "routine_work,hearing_work,access_requests");
        expectedRoleNameWorkTypesMap.put("case-allocator", null);
    }

    static void assertCommonRoleAssignmentAttributes(RoleAssignment r, String roleId,
                                                     RoleCategory expectedRoleCategory) {
        assertEquals(ActorIdType.IDAM, r.getActorIdType());
        assertEquals(UserAccessProfileBuilder.ID2, r.getActorId());
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
            if (List.of("9", "10", "1", "2").contains(roleId)) {
                assertNull(r.getAttributes().get("region"));
            } else {
                assertEquals(REGION_ID, r.getAttributes().get("region").asText());
            }
        }


        String expectedWorkTypes;
        if (roleId.equals("1") && r.getRoleName().equals("task-supervisor")) {
            expectedWorkTypes = "decision_making_work,access_requests";
        } else {
            expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());
        }

        String actualWorkTypes = null;
        if (r.getAttributes().get("workTypes") != null) {
            actualWorkTypes = r.getAttributes().get("workTypes").asText();
        }
        assertEquals(expectedWorkTypes, actualWorkTypes);
    }

    /* test parameters
     * roleId,expectedRoleNames,expectedRoleCount,expectedRoleCategory,taskSupervisorFlag,taskAllocatorFlag
     */
    static Stream<Arguments> generateDatav14() {
        return Stream.of(
            Arguments.of("10", Arrays.asList("hmcts-ctsc", "ctsc"), 2, RoleCategory.CTSC, "N", "N"),
            Arguments.of("9", Arrays.asList("ctsc", "hmcts-ctsc", "ctsc-team-leader"), 3, RoleCategory.CTSC,
                "N", "N"),
            Arguments.of("9", Arrays.asList("ctsc", "hmcts-ctsc", "ctsc-team-leader", "task-supervisor",
                "case-allocator"), 5, RoleCategory.CTSC, "Y", "Y"),
            Arguments.of("11", Arrays.asList("hmcts-admin","national-business-centre"), 2,
                RoleCategory.ADMIN, "N", "N"),
            Arguments.of("6", Arrays.asList("national-business-centre", "hmcts-admin", "nbc-team-leader"),
                3, RoleCategory.ADMIN, "N", "N"),
            Arguments.of("6", Arrays.asList("national-business-centre", "hmcts-admin", "nbc-team-leader",
                "task-supervisor", "case-allocator"), 5, RoleCategory.ADMIN, "Y", "Y"),
            Arguments.of("3", Arrays.asList("hearing-centre-admin", "hmcts-admin",
                "hearing-centre-team-leader"), 3, RoleCategory.ADMIN, "N", "N"),
            Arguments.of("3", Arrays.asList("hearing-centre-admin", "hmcts-admin",
                "hearing-centre-team-leader", "task-supervisor"), 4, RoleCategory.ADMIN, "Y", "N"),
            Arguments.of("4", Arrays.asList("hmcts-admin", "hearing-centre-admin"), 2, RoleCategory.ADMIN,
                "N", "N"),
            Arguments.of("2", Arrays.asList("tribunal-caseworker", "hmcts-legal-operations"), 2,
                RoleCategory.LEGAL_OPERATIONS, "N", "N"),
            Arguments.of("1", Arrays.asList("tribunal-caseworker", "senior-tribunal-caseworker",
                "hmcts-legal-operations"), 3, RoleCategory.LEGAL_OPERATIONS, "N", "N"),
            Arguments.of("1", Arrays.asList("tribunal-caseworker", "senior-tribunal-caseworker",
                "hmcts-legal-operations", "task-supervisor", "case-allocator"), 5, RoleCategory.LEGAL_OPERATIONS, "Y",
                "Y")
        );
    }

    @ParameterizedTest
    @MethodSource("generateDatav14")
    void shouldReturnCivilAdminMappings_v14(String roleId,
                                            List<String> roleNames,
                                            int roleCount,
                                            RoleCategory expectedRoleCategory,
                                            String taskSupervisorFlag,
                                            String taskAllocatorFlag) {
        // As CIVIL has 2 service codes AAA6 and AAA7 and the CaseWorkerAccessProfile has one service code we run
        // the test method twice, once with each service code
        shouldReturnCivilAdminMappings_v14(roleId, roleNames, roleCount, expectedRoleCategory, taskSupervisorFlag,
            taskAllocatorFlag, SERVICE_CODE_1);
        shouldReturnCivilAdminMappings_v14(roleId, roleNames, roleCount, expectedRoleCategory, taskSupervisorFlag,
                taskAllocatorFlag, SERVICE_CODE_2);
    }

    void shouldReturnCivilAdminMappings_v14(String roleId,
                                            List<String> roleNames,
                                            int roleCount,
                                            RoleCategory expectedRoleCategory,
                                            String taskSupervisorFlag,
                                            String taskAllocatorFlag,
                                            String serviceCode) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId3();
        List<String> skillCodes = List.of("civil", "test", "ctsc");
        cap.setRoleId(roleId);
        cap.setServiceCode(serviceCode);
        cap.setSuspended(false);
        cap.setRegionId(REGION_ID);
        cap.setSkillCodes(skillCodes);
        cap.setTaskSupervisorFlag(taskSupervisorFlag);
        cap.setCaseAllocatorFlag(taskAllocatorFlag);

        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("CIVIL", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(roleCount, roleAssignments.size());

        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNames.toArray()));

        roleAssignments.forEach(r -> {
            if (!r.getRoleName().contains("hmcts")) {
                assertEquals(skillCodes, r.getAuthorisations());
            }
        });

        for (RoleAssignment r : roleAssignments) {
            assertCommonRoleAssignmentAttributes(r, roleId, expectedRoleCategory);
        }
    }

}
