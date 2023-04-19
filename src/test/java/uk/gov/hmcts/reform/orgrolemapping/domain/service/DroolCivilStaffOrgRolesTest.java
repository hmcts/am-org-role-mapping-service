package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

@RunWith(MockitoJUnitRunner.class)
class DroolCivilStaffOrgRolesTest extends DroolBase {

    @Test
    void shouldReturnCivilCaseworkerMappings() {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode("AAA6");
        cap.setSuspended(false);
        cap.setRegionId("region1");

        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("civil_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("tribunal-caseworker", "hmcts-legal-operations"));
        roleAssignments.forEach(r -> {
            assertEquals("LEGAL_OPERATIONS", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());
        });

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).collect(Collectors.toList())
                .forEach(r -> {
                    assertEquals("CIVIL", r.getAttributes().get("jurisdiction").asText());
                    assertEquals("decision_making_work", r.getAttributes().get("workTypes").asText());
                    assertEquals("region1", r.getAttributes().get("region").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                });
    }

    @Test
    void shouldReturnCivilCaseworkerMappingsForSeniorTribunalCaseworker() {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId3();
        List<String> skillCodes = List.of("civil", "test", "ctsc");
        cap.setServiceCode("AAA6");
        cap.setSuspended(false);
        cap.setRegionId("region1");
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
            assertEquals("ORGANISATION", r.getRoleType().toString());
            if (!r.getRoleName().contains("hmcts")) {
                assertEquals(skillCodes, r.getAuthorisations());
            }
        });

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).collect(Collectors.toList())
                .forEach(r -> {
                    assertEquals("CIVIL", r.getAttributes().get("jurisdiction").asText());
                    if ("senior-tribunal-caseworker".equals(r.getRoleName())
                        || "task-supervisor".equals(r.getRoleName())) {
                        assertEquals("decision_making_work,access_requests",
                                r.getAttributes().get("workTypes").asText());
                    }
                    assertEquals("region1", r.getAttributes().get("region").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                });
    }


    static Stream<Arguments> generateData() {
        return Stream.of(
                Arguments.of("3", Arrays.asList("hmcts-admin", "hearing-centre-team-leader"),
                        2, Collections.singletonList("hearing_work,access_requests")),
                Arguments.of("10", Arrays.asList("hmcts-ctsc", "ctsc"),
                        2, Arrays.asList("routine_work")),
                Arguments.of("9", Arrays.asList("hmcts-ctsc", "ctsc-team-leader"),
                        2, Arrays.asList("routine_work,access_requests")),
                Arguments.of("4", Arrays.asList("hmcts-admin", "hearing-centre-admin"),
                        2, Collections.singletonList("hearing_work")),
                Arguments.of("1", Arrays.asList("senior-tribunal-caseworker","hmcts-legal-operations"),
                        2, List.of("decision_making_work,access_requests")),
                Arguments.of("2", Arrays.asList("tribunal-caseworker","hmcts-legal-operations"),
                        2, List.of("decision_making_work")),
                Arguments.of("6", Arrays.asList("hmcts-admin", "nbc-team-leader"),
                        2, Arrays.asList("routine_work,access_requests")),
                Arguments.of("11", Arrays.asList("hmcts-admin","national-business-centre"),
                        2, Arrays.asList("routine_work"))
        );
    }

    static Stream<Arguments> generateDatav11() {
        return Stream.of(
                Arguments.of("1", Arrays.asList("tribunal-caseworker"),
                        1, Arrays.asList("decision_making_work"), RoleCategory.LEGAL_OPERATIONS),
                Arguments.of("3", Arrays.asList("hearing-centre-admin"),
                        1, Collections.singletonList("hearing_work"), RoleCategory.ADMIN),
                Arguments.of("6", Arrays.asList("national-business-centre"),
                        1, Arrays.asList("routine_work"), RoleCategory.ADMIN),
                Arguments.of("9", Arrays.asList("ctsc"),
                        1, Arrays.asList("routine_work"), RoleCategory.CTSC)
        );
    }

    @ParameterizedTest
    @MethodSource("generateData")
    void shouldReturnCivilAdminMappings(String roleId, List<String> roleNames, int roleCount, List<String> workTypes) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        List<String> skillCodes = List.of("civil", "test", "ctsc");
        cap.setRoleId(roleId);
        cap.setServiceCode("AAA6");
        cap.setSuspended(false);
        cap.setRegionId("region1");
        cap.setSkillCodes(skillCodes);

        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("civil_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(roleCount, roleAssignments.size());

        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNames.toArray()));

        roleAssignments.forEach(r -> {
            assertEquals("ORGANISATION", r.getRoleType().toString());
            if (!r.getRoleName().contains("hmcts")) {
                assertEquals(skillCodes, r.getAuthorisations());
            }
        });

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals("CIVIL", r.getAttributes().get("jurisdiction").asText());
                    if (!(roleId.equals("10") || roleId.equals("9"))) {
                        assertEquals("region1", r.getAttributes().get("region").asText());
                    }
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                });

        List<Map<String, JsonNode>> list = roleAssignments.stream()
                .map(RoleAssignment::getAttributes).toList();
        List<String> workTypesCombined = new ArrayList<>();

        for (Map<String, JsonNode> e : list) {
            if (e.containsKey("workTypes")) {
                workTypesCombined.add(e.entrySet().stream()
                    .filter(a -> a.getKey().equals("workTypes"))
                    .map(Map.Entry::getValue).findFirst().get().textValue());
            }
        }
        assertThat(workTypesCombined,containsInAnyOrder(workTypes.toArray()));
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
        cap.setServiceCode("AAA6");
        cap.setSuspended(false);
        cap.setRegionId("region1");
        cap.setSkillCodes(skillCodes);

        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("civil_wa_1_1", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(roleCount, roleAssignments.size());

        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNames.toArray()));

        roleAssignments.forEach(r -> {
            assertEquals("ORGANISATION", r.getRoleType().toString());
            assertEquals(expectedRoleCategory, r.getRoleCategory());
            if (!r.getRoleName().contains("hmcts")) {
                assertEquals(skillCodes, r.getAuthorisations());
            }
        });

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals("CIVIL", r.getAttributes().get("jurisdiction").asText());
                    if (!(roleId.equals("10") || roleId.equals("9"))) {
                        assertEquals("region1", r.getAttributes().get("region").asText());
                    } else {
                        assertFalse(r.getAttributes().containsKey("region"));
                    }
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                });

        List<Map<String, JsonNode>> list = roleAssignments.stream()
                .map(RoleAssignment::getAttributes).toList();
        List<String> workTypesCombined = new ArrayList<>();

        for (Map<String, JsonNode> e : list) {
            if (e.containsKey("workTypes")) {
                workTypesCombined.add(e.entrySet().stream()
                        .filter(a -> a.getKey().equals("workTypes"))
                        .map(Map.Entry::getValue).findFirst().get().textValue());
            }
        }
        assertThat(workTypesCombined, containsInAnyOrder(workTypes.toArray()));
    }

}
