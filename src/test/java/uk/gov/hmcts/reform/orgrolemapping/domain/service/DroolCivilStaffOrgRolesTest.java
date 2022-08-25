package uk.gov.hmcts.reform.orgrolemapping.domain.service;

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
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

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
        cap.setServiceCode("AAA6");
        cap.setSuspended(false);
        cap.setRegionId("region1");
        cap.setTaskSupervisorFlag("Y");
        cap.setCaseAllocatorFlag("Y");
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
        });

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).collect(Collectors.toList())
                .forEach(r -> {
                    assertEquals("CIVIL", r.getAttributes().get("jurisdiction").asText());
                    if ("senior-tribunal-caseworker".equals(r.getRoleName())) {
                        assertEquals("decision_making_work,access_requests",
                                r.getAttributes().get("workTypes").asText());
                    } else {
                        assertEquals("routine_work", r.getAttributes().get("workTypes").asText());
                    }
                    assertEquals("region1", r.getAttributes().get("region").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                });
    }


    static Stream<Arguments> generateData() {
        return Stream.of(
                Arguments.of("3", Arrays.asList("hmcts-admin", "hearing-centre-team-leader"),
                        2, Collections.singletonList("hearing_work,access_requests")),
                Arguments.of("10", Arrays.asList("hmcts-admin", "ctsc", "national-business-centre"),
                        3, Arrays.asList("routine_work", "routine_work")),
                Arguments.of("9", Arrays.asList("hmcts-admin", "ctsc-team-leader", "nbc-team-leader"),
                        3, Arrays.asList("routine_work,access_requests", "routine_work,access_requests")),
                Arguments.of("4", Arrays.asList("hmcts-admin", "hearing-centre-admin"),
                        2, Collections.singletonList("hearing_work")),
                Arguments.of("1", Arrays.asList("senior-tribunal-caseworker","hmcts-legal-operations"),
                        2, List.of("decision_making_work,access_requests")),
                Arguments.of("2", Arrays.asList("tribunal-caseworker","hmcts-legal-operations"),
                        2, List.of("decision_making_work")),
                Arguments.of("6", Arrays.asList("hmcts-admin", "ctsc-team-leader", "nbc-team-leader"),
                        3, Arrays.asList("routine_work,access_requests", "routine_work,access_requests")),
                Arguments.of("11", Arrays.asList("hmcts-admin","ctsc","national-business-centre"),
                        3, Arrays.asList("routine_work","routine_work"))
        );
    }

    @ParameterizedTest
    @MethodSource("generateData")
    void shouldReturnCivilAdminMappings(String roleId, List<String> roleNames, int roleCount, List<String> workTypes) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setRoleId(roleId);
        cap.setServiceCode("AAA6");
        cap.setSuspended(false);
        cap.setRegionId("region1");

        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("civil_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(roleCount, roleAssignments.size());

        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNames.toArray()));

        roleAssignments.forEach(r -> assertEquals("ORGANISATION", r.getRoleType().toString()));

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
                .forEach(r -> {
                    assertEquals("CIVIL", r.getAttributes().get("jurisdiction").asText());
                    assertEquals("region1", r.getAttributes().get("region").asText());
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
}
