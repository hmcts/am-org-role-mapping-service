package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import java.util.Arrays;
import java.util.Collections;
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
class DroolCivilStaffOrgRolesTest extends DroolBase {

    static final String SERVICE_CODE = "AAA6";
    static final String REGION_ID = "region1";
    static final String JURISDICTION = "CIVIL";
    static final String ROLE_TYPE = "ORGANISATION";

    static Map<String, String> expectedRoleNameWorkTypesMap = new HashMap<>();

    {
        expectedRoleNameWorkTypesMap.put("hmcts-admin", null);
        expectedRoleNameWorkTypesMap.put("hearing-centre-team-leader", "hearing_work,access_requests");
        expectedRoleNameWorkTypesMap.put("hmcts-ctsc", null);
        expectedRoleNameWorkTypesMap.put("ctsc", "routine_work");
        expectedRoleNameWorkTypesMap.put("ctsc-team-leader", "routine_work,access_requests");
        expectedRoleNameWorkTypesMap.put("hearing-centre-admin", "hearing_work");
        expectedRoleNameWorkTypesMap.put("senior-tribunal-caseworker", "decision_making_work,access_requests");
        expectedRoleNameWorkTypesMap.put("tribunal-caseworker", "decision_making_work");
        expectedRoleNameWorkTypesMap.put("hmcts-legal-operations", null);
        expectedRoleNameWorkTypesMap.put("nbc-team-leader", "routine_work,access_requests");
        expectedRoleNameWorkTypesMap.put("national-business-centre", "routine_work");
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
            assertTrue(r.isReadOnly());
            assertNull(primaryLocation);
            assertNull(r.getAttributes().get("region"));
        } else {
            assertEquals(Classification.PUBLIC, r.getClassification());
            assertEquals(GrantType.STANDARD, r.getGrantType());
            assertEquals(JURISDICTION, r.getAttributes().get("jurisdiction").asText());
            assertFalse(r.isReadOnly());
            assertEquals(UserAccessProfileBuilder.PRIMARY_LOCATION_ID, primaryLocation);
            if (List.of("9", "10").contains(roleId)) {
                assertNull(r.getAttributes().get("region"));
            } else {
                assertNull(r.getAttributes().get(REGION_ID));
            }
        }

        String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());
        String actualWorkTypes = null;
        if (r.getAttributes().get("workTypes") != null) {
            actualWorkTypes = r.getAttributes().get("workTypes").asText();
        }
        assertEquals(expectedWorkTypes, actualWorkTypes);
    }

    @Test
    void shouldReturnCivilCaseworkerMappings() {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(SERVICE_CODE);
        cap.setSuspended(false);
        cap.setRegionId(REGION_ID);

        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(List.of(FeatureFlag.builder().flagName("civil_wa_1_0").status(true).build(),
                        FeatureFlag.builder().flagName("civil_wa_1_4").status(false).build()));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("tribunal-caseworker", "hmcts-legal-operations"));
        roleAssignments.forEach(r -> {
            assertEquals("LEGAL_OPERATIONS", r.getRoleCategory().toString());
            assertEquals(ROLE_TYPE, r.getRoleType().toString());
        });

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).collect(Collectors.toList())
                .forEach(r -> {
                    assertEquals(JURISDICTION, r.getAttributes().get("jurisdiction").asText());
                    assertEquals("decision_making_work", r.getAttributes().get("workTypes").asText());
                    assertEquals(REGION_ID, r.getAttributes().get("region").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                });
    }

    @Test
    void shouldReturnCivilCaseworkerMappingsForSeniorTribunalCaseworker() {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId3();
        List<String> skillCodes = List.of("civil", "test", "ctsc");
        cap.setServiceCode(SERVICE_CODE);
        cap.setSuspended(false);
        cap.setRegionId(REGION_ID);
        cap.setTaskSupervisorFlag("Y");
        cap.setCaseAllocatorFlag("Y");
        cap.setSkillCodes(skillCodes);
        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("civil_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("senior-tribunal-caseworker", "hmcts-legal-operations", "task-supervisor",
                        "case-allocator"));
        roleAssignments.forEach(r -> {
            assertEquals("LEGAL_OPERATIONS", r.getRoleCategory().toString());
            assertEquals(ROLE_TYPE, r.getRoleType().toString());
            if (!r.getRoleName().contains("hmcts")) {
                assertEquals(skillCodes, r.getAuthorisations());
            }
        });

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).collect(Collectors.toList())
                .forEach(r -> {
                    assertEquals(JURISDICTION, r.getAttributes().get("jurisdiction").asText());
                    if ("senior-tribunal-caseworker".equals(r.getRoleName())
                        || "task-supervisor".equals(r.getRoleName())) {
                        assertEquals("decision_making_work,access_requests",
                                r.getAttributes().get("workTypes").asText());
                    }
                    assertEquals(REGION_ID, r.getAttributes().get("region").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                });
    }

    static Stream<Arguments> generateDatav11() {
        return Stream.of(
            Arguments.of("10", Arrays.asList("hmcts-ctsc", "ctsc"),
                2, Arrays.asList("routine_work"), RoleCategory.CTSC),
            Arguments.of("4", Arrays.asList("hmcts-admin", "hearing-centre-admin"),
                2, Collections.singletonList("hearing_work"), RoleCategory.ADMIN),
            Arguments.of("2", Arrays.asList("tribunal-caseworker", "hmcts-legal-operations"),
                2, List.of("decision_making_work"), RoleCategory.LEGAL_OPERATIONS),
            Arguments.of("11", Arrays.asList("hmcts-admin","national-business-centre"),
                2, Arrays.asList("routine_work"), RoleCategory.ADMIN),
            Arguments.of("1", Arrays.asList("tribunal-caseworker", "senior-tribunal-caseworker",
                "hmcts-legal-operations"), 3, Arrays.asList("decision_making_work"), RoleCategory.LEGAL_OPERATIONS),
            Arguments.of("3", Arrays.asList("hearing-centre-admin", "hmcts-admin",
                "hearing-centre-team-leader"), 3, Collections.singletonList("hearing_work"), RoleCategory.ADMIN),
            Arguments.of("6", Arrays.asList("national-business-centre", "hmcts-admin", "nbc-team-leader"),
                3, Arrays.asList("routine_work"), RoleCategory.ADMIN),
            Arguments.of("9", Arrays.asList("ctsc", "hmcts-ctsc", "ctsc-team-leader"),
                3, Arrays.asList("routine_work"), RoleCategory.CTSC)
        );
    }

    @ParameterizedTest
    @MethodSource("generateDatav11")
    void shouldReturnCivilAdminMappings_v11(String roleId,
                                            List<String> roleNames,
                                            int roleCount,
                                            List<String> workTypes,
                                            RoleCategory expectedRoleCategory) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId3();
        List<String> skillCodes = List.of("civil", "test", "ctsc");
        cap.setRoleId(roleId);
        cap.setServiceCode(SERVICE_CODE);
        cap.setSuspended(false);
        cap.setRegionId(REGION_ID);
        cap.setSkillCodes(skillCodes);

        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(List.of(FeatureFlag.builder().flagName("civil_wa_1_0").status(true).build(),
                        FeatureFlag.builder().flagName("civil_wa_1_1").status(true).build(),
                        FeatureFlag.builder().flagName("civil_wa_1_2").status(true).build(),
                        FeatureFlag.builder().flagName("civil_wa_1_3").status(true).build(),
                        FeatureFlag.builder().flagName("civil_wa_1_4").status(false).build()));

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
