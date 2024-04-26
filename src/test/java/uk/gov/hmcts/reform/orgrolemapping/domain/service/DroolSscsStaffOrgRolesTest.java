package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.helper.RoleAssignmentAssertHelper.MultiRegion;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
class DroolSscsStaffOrgRolesTest extends DroolBase {

    // NB: multi-regions are: South-West and Wales
    static  List<String> multiRegionList = List.of("6", "7");
    // NB: multi-regions are: all English and Welsh regions
    static  List<String> multiRegionCtscList = List.of("1", "2", "3", "4", "5", "6", "7");

    @ParameterizedTest
    @CsvSource({
        "3,BBA3,'hmcts-admin,hearing-centre-team-leader,hearing-centre-admin,"
                + "specific-access-approver-admin',N,N,1,false",
        "3,BBA3,'hmcts-admin,hearing-centre-team-leader,hearing-centre-admin,"
                + "task-supervisor,specific-access-approver-admin',Y,N,1,false",
        "3,BBA3,'hmcts-admin,hearing-centre-team-leader,hearing-centre-admin,"
                + "case-allocator,specific-access-approver-admin',N,Y,1,false",
        "3,BBA3,'hmcts-admin,hearing-centre-team-leader,hearing-centre-admin,"
            + "task-supervisor,case-allocator,specific-access-approver-admin',Y,Y,1,false",
        "3,BBA3,'hmcts-admin,hearing-centre-team-leader,hearing-centre-admin,"
            + "task-supervisor,case-allocator,specific-access-approver-admin',Y,Y,6,true",
        "3,BBA3,'hmcts-admin,hearing-centre-team-leader,hearing-centre-admin,"
            + "task-supervisor,case-allocator,specific-access-approver-admin',Y,Y,7,true",

        "4,BBA3,'hmcts-admin,hearing-centre-admin',N,N,1,false",
        "4,BBA3,'hmcts-admin,task-supervisor,hearing-centre-admin',Y,N,1,false",
        "4,BBA3,'hmcts-admin,case-allocator,hearing-centre-admin',N,Y,1,false",
        "4,BBA3,'hmcts-admin,task-supervisor,case-allocator,hearing-centre-admin',Y,Y,1,false",
        "4,BBA3,'hmcts-admin,task-supervisor,case-allocator,hearing-centre-admin',Y,Y,6,true",
        "4,BBA3,'hmcts-admin,task-supervisor,case-allocator,hearing-centre-admin',Y,Y,7,true",

        "5,BBA3,'clerk,hmcts-admin',N,N,1,false",
        "5,BBA3,'clerk,task-supervisor,hmcts-admin',Y,N,1,false",
        "5,BBA3,'clerk,case-allocator,hmcts-admin',N,Y,1,false",
        "5,BBA3,'clerk,task-supervisor,case-allocator,hmcts-admin',Y,Y,1,false",
        "5,BBA3,'clerk,task-supervisor,case-allocator,hmcts-admin',Y,Y,6,true",
        "5,BBA3,'clerk,task-supervisor,case-allocator,hmcts-admin',Y,Y,7,true",

        "12,BBA3,'hmcts-admin,regional-centre-team-leader,regional-centre-admin,"
                + "specific-access-approver-admin',N,N,1,false",
        "12,BBA3,'hmcts-admin,regional-centre-team-leader,regional-centre-admin,"
                + "task-supervisor,specific-access-approver-admin',Y,N,1,false",
        "12,BBA3,'hmcts-admin,regional-centre-team-leader,regional-centre-admin,"
                + "case-allocator,specific-access-approver-admin',N,Y,1,false",
        "12,BBA3,'hmcts-admin,regional-centre-team-leader,regional-centre-admin,"
                + "task-supervisor,case-allocator,specific-access-approver-admin',Y,Y,1,false",
        "12,BBA3,'hmcts-admin,regional-centre-team-leader,regional-centre-admin,"
                + "task-supervisor,case-allocator,specific-access-approver-admin',Y,Y,6,true",
        "12,BBA3,'hmcts-admin,regional-centre-team-leader,regional-centre-admin,"
                + "task-supervisor,case-allocator,specific-access-approver-admin',Y,Y,7,true",

        "13,BBA3,'hmcts-admin,regional-centre-admin',N,N,1,false",
        "13,BBA3,'hmcts-admin,task-supervisor,regional-centre-admin',Y,N,1,false",
        "13,BBA3,'hmcts-admin,case-allocator,regional-centre-admin',N,Y,1,false",
        "13,BBA3,'hmcts-admin,task-supervisor,case-allocator,regional-centre-admin',Y,Y,1,false",
        "13,BBA3,'hmcts-admin,task-supervisor,case-allocator,regional-centre-admin',Y,Y,6,true",
        "13,BBA3,'hmcts-admin,task-supervisor,case-allocator,regional-centre-admin',Y,Y,7,true",
    })
    void shouldReturnSscsAdminMappings(String roleId, String serviceCode, String expectedRoles,
                                       String taskSupervisorFlag, String caseAllocatorFlag,
                                       String region, boolean expectMultiRegion) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<String> skillCodes = List.of("sscs", "test", "ctsc");
        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(serviceCode);
        cap.setSuspended(false);
        cap.setRoleId(roleId);
        cap.setTaskSupervisorFlag(taskSupervisorFlag);
        cap.setCaseAllocatorFlag(caseAllocatorFlag);
        cap.setRegionId(region);
        cap.setSkillCodes(skillCodes);
        allProfiles.add(cap);

        // create map for all ADMIN roleNames that need regions
        List<String> rolesThatRequireRegions = List.of(
                "hearing-centre-team-leader",
                "hearing-centre-admin",
                "specific-access-approver-admin",
                "clerk",
                "regional-centre-team-leader",
                "regional-centre-admin"
        );
        Map<String, List<String>> roleNameToRegionsMap = MultiRegion.buildRoleNameToRegionsMap(rolesThatRequireRegions);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        List<String> expectedRoleList = Arrays.stream(expectedRoles.split(",")).toList();
        MultiRegion.assertRoleAssignmentCount(
                roleAssignments,
                expectedRoleList,
                expectMultiRegion,
                rolesThatRequireRegions,
                multiRegionList
        );

        roleAssignments.forEach(r -> {
            assertEquals("ADMIN", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());

            // check region status and add to map
            MultiRegion.assertRegionStatusAndUpdateRoleToRegionMap(r, roleNameToRegionsMap);
        });

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                    //assert work types
                    if (("hearing-centre-team-leader").equals(r.getRoleName())) {
                        assertNull(r.getAttributes().get("workTypes"));
                    } else if (("hearing-centre-admin").equals(r.getRoleName())) {
                        assertThat(r.getAttributes().get("workTypes").asText().split(","),
                                arrayContainingInAnyOrder("pre_hearing", "hearing_work",
                                        "post_hearing", "decision_making_work",
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
                                arrayContainingInAnyOrder("pre_hearing", "hearing_work",
                                        "post_hearing", "decision_making_work",
                                        "routine_work", "priority", "applications"));
                    } else if (("clerk").equals(r.getRoleName())) {
                        assertThat(r.getAttributes().get("workTypes").asText().split(","),
                                arrayContainingInAnyOrder("pre_hearing", "hearing_work",
                                        "post_hearing", "decision_making_work",
                                        "routine_work", "priority", "applications"));
                    } else {
                        assertNull(r.getAttributes().get("workTypes"));
                    }
                });

        // verify regions add to map
        MultiRegion.assertRoleNameToRegionsMapIsAsExpected(
                roleNameToRegionsMap,
                expectedRoleList,
                expectMultiRegion,
                multiRegionList,
                region, // fallback if not multi-region scenario
                null // i.e. no bookings
        );
    }

    @ParameterizedTest
    @CsvSource({
        "9,BBA3,'hmcts-ctsc,ctsc,ctsc-team-leader,specific-access-approver-ctsc',N,N,1,true",
        "9,BBA3,'hmcts-ctsc,ctsc,ctsc-team-leader,task-supervisor,specific-access-approver-ctsc',Y,N,1,true",
        "9,BBA3,'hmcts-ctsc,ctsc,ctsc-team-leader,case-allocator,specific-access-approver-ctsc',N,Y,1,true",
        "9,BBA3,'hmcts-ctsc,ctsc,ctsc-team-leader,task-supervisor,case-allocator,specific-access-approver-ctsc',"
                + "Y,Y,1,true",
        "9,BBA3,'hmcts-ctsc,ctsc,ctsc-team-leader,task-supervisor,case-allocator,specific-access-approver-ctsc',"
                + "Y,Y,2,true",
        "9,BBA3,'hmcts-ctsc,ctsc,ctsc-team-leader,task-supervisor,case-allocator,specific-access-approver-ctsc',"
                + "Y,Y,3,true",
        "9,BBA3,'hmcts-ctsc,ctsc,ctsc-team-leader,task-supervisor,case-allocator,specific-access-approver-ctsc',"
                + "Y,Y,4,true",
        "9,BBA3,'hmcts-ctsc,ctsc,ctsc-team-leader,task-supervisor,case-allocator,specific-access-approver-ctsc',"
                + "Y,Y,5,true",
        "9,BBA3,'hmcts-ctsc,ctsc,ctsc-team-leader,task-supervisor,case-allocator,specific-access-approver-ctsc',"
                + "Y,Y,6,true",
        "9,BBA3,'hmcts-ctsc,ctsc,ctsc-team-leader,task-supervisor,case-allocator,specific-access-approver-ctsc',"
                + "Y,Y,7,true",
        "9,BBA3,'hmcts-ctsc,ctsc,ctsc-team-leader,task-supervisor,case-allocator,specific-access-approver-ctsc',"
                + "Y,Y,12,false", // i.e. scotland
        "10,BBA3,'hmcts-ctsc,ctsc',N,N,1,true",
        "10,BBA3,'hmcts-ctsc,task-supervisor,ctsc',Y,N,1,true",
        "10,BBA3,'hmcts-ctsc,case-allocator,ctsc',N,Y,1,true",
        "10,BBA3,'hmcts-ctsc,task-supervisor,case-allocator,ctsc',Y,Y,1,true",
        "10,BBA3,'hmcts-ctsc,task-supervisor,case-allocator,ctsc',Y,Y,2,true",
        "10,BBA3,'hmcts-ctsc,task-supervisor,case-allocator,ctsc',Y,Y,3,true",
        "10,BBA3,'hmcts-ctsc,task-supervisor,case-allocator,ctsc',Y,Y,4,true",
        "10,BBA3,'hmcts-ctsc,task-supervisor,case-allocator,ctsc',Y,Y,5,true",
        "10,BBA3,'hmcts-ctsc,task-supervisor,case-allocator,ctsc',Y,Y,6,true",
        "10,BBA3,'hmcts-ctsc,task-supervisor,case-allocator,ctsc',Y,Y,7,true",
        "10,BBA3,'hmcts-ctsc,task-supervisor,case-allocator,ctsc',Y,Y,12,false", // i.e. scotland
    })
    void shouldReturnSscsCtscMappings(String roleId, String serviceCode, String expectedRoles,
                                      String taskSupervisorFlag, String caseAllocatorFlag,
                                      String region, boolean expectMultiRegion) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        List<String> skillCodes = List.of("sscs", "test", "ctsc");
        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(serviceCode);
        cap.setSuspended(false);
        cap.setRoleId(roleId);
        cap.setTaskSupervisorFlag(taskSupervisorFlag);
        cap.setCaseAllocatorFlag(caseAllocatorFlag);
        cap.setRegionId(region);
        cap.setSkillCodes(skillCodes);

        allProfiles.add(cap);

        // create map for all CTSC roleNames that need regions
        List<String> rolesThatRequireRegions = List.of(
                "ctsc",
                "ctsc-team-leader",
                "task-supervisor",
                "case-allocator",
                "specific-access-approver-ctsc"
        );
        Map<String, List<String>> roleNameToRegionsMap = MultiRegion.buildRoleNameToRegionsMap(rolesThatRequireRegions);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        List<String> expectedRoleList = Arrays.stream(expectedRoles.split(",")).toList();
        MultiRegion.assertRoleAssignmentCount(
                roleAssignments,
                expectedRoleList,
                expectMultiRegion,
                rolesThatRequireRegions,
                multiRegionCtscList
        );

        roleAssignments.forEach(r -> {
            assertEquals("CTSC", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());
            if (("ctsc").equals(r.getRoleName()) || ("ctsc-team-leader").equals(r.getRoleName())) {
                assertEquals(skillCodes,r.getAuthorisations());
            }

            // check region status and add to map
            MultiRegion.assertRegionStatusAndUpdateRoleToRegionMap(r, roleNameToRegionsMap);
        });

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                    //assert work types
                    if (("ctsc").equals(r.getRoleName())) {
                        assertThat(r.getAttributes().get("workTypes").asText().split(","),
                                arrayContainingInAnyOrder("pre_hearing", "hearing_work",
                                        "post_hearing", "decision_making_work",
                                        "routine_work", "priority", "applications"));
                    } else if (("ctsc-team-leader").equals(r.getRoleName())) {
                        assertNull(r.getAttributes().get("workTypes"));
                    } else if (("task-supervisor").equals(r.getRoleName())) {
                        assertNull(r.getAttributes().get("workTypes"));
                    } else if (("case-allocator").equals(r.getRoleName())) {
                        assertNull(r.getAttributes().get("workTypes"));
                    } else if (("specific-access-approver-ctsc").equals(r.getRoleName())) {
                        assertEquals("access_requests", r.getAttributes().get("workTypes").asText());
                    } else {
                        assertNull(r.getAttributes().get("workTypes"));
                    }
                });

        // verify regions add to map
        MultiRegion.assertRoleNameToRegionsMapIsAsExpected(
                roleNameToRegionsMap,
                expectedRoleList,
                expectMultiRegion,
                multiRegionCtscList,
                region, // fallback if not multi-region scenario
                null // i.e. no bookings
        );
    }

    @ParameterizedTest
    @CsvSource({
        "1,BBA3,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,"
                + "specific-access-approver-legal-ops',N,N,1,false",
        "1,BBA3,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,task-supervisor,"
                + "specific-access-approver-legal-ops',Y,N,1,false",
        "1,BBA3,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,case-allocator,"
                + "specific-access-approver-legal-ops',N,Y,1,false",
        "1,BBA3,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,task-supervisor,"
                + "case-allocator,specific-access-approver-legal-ops',Y,Y,1,false",
        "1,BBA3,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,task-supervisor,"
                + "case-allocator,specific-access-approver-legal-ops',Y,Y,6,true",
        "1,BBA3,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,task-supervisor,"
                + "case-allocator,specific-access-approver-legal-ops',Y,Y,7,true",

        "2,BBA3,'tribunal-caseworker,hmcts-legal-operations',N,N,1,false",
        "2,BBA3,'tribunal-caseworker,task-supervisor,hmcts-legal-operations',Y,N,1,false",
        "2,BBA3,'tribunal-caseworker,case-allocator,hmcts-legal-operations',N,Y,1,false",
        "2,BBA3,'tribunal-caseworker,task-supervisor,case-allocator,hmcts-legal-operations',Y,Y,1,false",
        "2,BBA3,'tribunal-caseworker,task-supervisor,case-allocator,hmcts-legal-operations',Y,Y,6,true",
        "2,BBA3,'tribunal-caseworker,task-supervisor,case-allocator,hmcts-legal-operations',Y,Y,7,true",

        "16,BBA3,'registrar,hmcts-legal-operations',N,N,1,false",
        "16,BBA3,'registrar,task-supervisor,hmcts-legal-operations',Y,N,1,false",
        "16,BBA3,'registrar,case-allocator,hmcts-legal-operations',N,Y,1,false",
        "16,BBA3,'registrar,task-supervisor,case-allocator,hmcts-legal-operations',Y,Y,1,false",
        "16,BBA3,'registrar,task-supervisor,case-allocator,hmcts-legal-operations',Y,Y,6,true",
        "16,BBA3,'registrar,task-supervisor,case-allocator,hmcts-legal-operations',Y,Y,7,true",
    })
    void shouldReturnSscsCaseWorkerMappings(String roleId, String serviceCode, String expectedRoles,
                                            String taskSupervisorFlag, String caseAllocatorFlag,
                                            String region, boolean expectMultiRegion) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();
        List<String> skillCodes = List.of("sscs", "test", "ctsc");
        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(serviceCode);
        cap.setSuspended(false);
        cap.setRoleId(roleId);
        cap.setTaskSupervisorFlag(taskSupervisorFlag);
        cap.setCaseAllocatorFlag(caseAllocatorFlag);
        cap.setRegionId(region);
        cap.setSkillCodes(skillCodes);

        allProfiles.add(cap);

        // create map for all CTSC roleNames that need regions
        List<String> rolesThatRequireRegions = new java.util.ArrayList<>(List.of(
                "senior-tribunal-caseworker",
                "tribunal-caseworker",
                "specific-access-approver-legal-ops",
                "registrar"
        ));
        if (!roleId.equals("1")) {
            rolesThatRequireRegions.add("task-supervisor");
            rolesThatRequireRegions.add("case-allocator");
        }
        Map<String, List<String>> roleNameToRegionsMap = MultiRegion.buildRoleNameToRegionsMap(rolesThatRequireRegions);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        List<String> expectedRoleList = Arrays.stream(expectedRoles.split(",")).toList();
        MultiRegion.assertRoleAssignmentCount(
                roleAssignments,
                expectedRoleList,
                expectMultiRegion,
                rolesThatRequireRegions,
                multiRegionList
        );

        roleAssignments.forEach(r -> {
            assertEquals("LEGAL_OPERATIONS", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());

            // check region status and add to map
            MultiRegion.assertRegionStatusAndUpdateRoleToRegionMap(r, roleNameToRegionsMap);
        });

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                    //assert work types
                    if (("senior-tribunal-caseworker").equals(r.getRoleName())) {
                        assertNull(r.getAttributes().get("workTypes"));
                    } else if (("tribunal-caseworker").equals(r.getRoleName())) {
                        assertThat(r.getAttributes().get("workTypes").asText().split(","),
                                arrayContainingInAnyOrder("pre_hearing", "hearing_work",
                                        "post_hearing", "decision_making_work",
                                        "routine_work", "priority", "applications"));
                    } else if (("task-supervisor").equals(r.getRoleName())) {
                        assertNull(r.getAttributes().get("workTypes"));
                    } else if (("case-allocator").equals(r.getRoleName())) {
                        assertNull(r.getAttributes().get("workTypes"));
                    } else if (("specific-access-approver-legal-ops").equals(r.getRoleName())) {
                        assertEquals("access_requests", r.getAttributes().get("workTypes").asText());
                    } else if (("registrar").equals(r.getRoleName())) {
                        assertThat(r.getAttributes().get("workTypes").asText().split(","),
                                arrayContainingInAnyOrder("pre_hearing", "hearing_work",
                                        "post_hearing", "decision_making_work",
                                        "routine_work", "priority", "applications"));
                    } else {
                        assertNull(r.getAttributes().get("workTypes"));
                    }
                });

        // verify regions add to map
        MultiRegion.assertRoleNameToRegionsMapIsAsExpected(
                roleNameToRegionsMap,
                expectedRoleList,
                expectMultiRegion,
                multiRegionList,
                region, // fallback if not multi-region scenario
                null // i.e. no bookings
        );
    }

    @ParameterizedTest
    @CsvSource({
        "14,BBA3,'dwp',N,N",
        "15,BBA3,'hmrc',N,N"
    })
    void shouldReturnSscsOtherGovDepMappings(String roleId, String serviceCode, String expectedRoles,
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
                buildExecuteKieSession(setFeatureFlags());

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
            assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());
            assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
            assertEquals("PUBLIC", r.getClassification().toString());
            assertEquals("STANDARD", r.getGrantType().toString());
            assertNull(r.getAttributes().get("region"));

            //assert work types
            if (("dwp").equals(r.getRoleName())) {
                assertThat(r.getAttributes().get("workTypes").asText().split(","),
                        arrayContainingInAnyOrder("applications", "hearing_work",
                                "routine_work", "priority", "pre_hearing"));
            } else if (("hmrc").equals(r.getRoleName())) {
                assertThat(r.getAttributes().get("workTypes").asText().split(","),
                        arrayContainingInAnyOrder("applications", "hearing_work",
                                "routine_work", "priority", "pre_hearing"));
            } else {
                assertNull(r.getAttributes().get("workTypes"));
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

    private static List<FeatureFlag> setFeatureFlags() {
        return List.of(FeatureFlag.builder().flagName("sscs_wa_1_0").status(true).build(),
                FeatureFlag.builder().flagName("sscs_wa_1_2").status(true).build());
    }
}
