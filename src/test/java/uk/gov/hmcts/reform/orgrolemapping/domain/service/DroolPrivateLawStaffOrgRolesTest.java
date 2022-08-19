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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.collection.ArrayMatching;
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
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

@RunWith(MockitoJUnitRunner.class)
class DroolPrivateLawStaffOrgRolesTest extends DroolBase {

    @Test
    void shouldReturnPrivateLawStaffMappings() {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode("ABA5");
        cap.setSuspended(false);
        cap.setRoleId("10");

        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("privatelaw_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("ctsc", "hmcts-ctsc"));
        roleAssignments.forEach(r -> {
            assertEquals("CTSC", r.getRoleCategory().toString());
            assertEquals("ORGANISATION", r.getRoleType().toString());
        });

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).collect(Collectors.toList())
                .forEach(r -> {
                    assertEquals("PRIVATELAW", r.getAttributes().get("jurisdiction").asText());
                    assertTrue(r.getAttributes().get("workTypes").asText().contains("routine_work"));
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                });
    }

    static Stream<Arguments> generateData() {
        return Stream.of(
            Arguments.of("10", Arrays.asList("ctsc", "hmcts-ctsc"),
                2, Collections.singletonList("routine_work,hearing_work,applications"),"CTSC"),
            Arguments.of("4", Arrays.asList("hearing-centre-admin", "hmcts-admin"),
                2, Collections.singletonList("routine_work,hearing_work,applications"),"ADMINISTRATOR"),
            Arguments.of("2", Arrays.asList("legal-caseworker", "hmcts-legal-operations"),
                2, Collections.singletonList("routine_work,hearing_work,applications"),"LEGAL_OPERATIONS"));
    }

    @ParameterizedTest
    @MethodSource("generateData")
    void shouldReturnCivilAdminMappings(String roleId, List<String> roleNames, int roleCount, List<String> workTypes,
                                        String roleCategory) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setRoleId(roleId);
        cap.setServiceCode("ABA5");
        cap.setSuspended(false);
        cap.setRegionId("region1");

        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
            buildExecuteKieSession(getFeatureFlags("privatelaw_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(roleCount, roleAssignments.size());

        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
            containsInAnyOrder(roleNames.toArray()));

        roleAssignments.forEach(r -> {
            assertTrue(roleCategory.contains(r.getRoleCategory().toString()));
        });

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).toList()
            .forEach(r -> {
                assertEquals("PRIVATELAW", r.getAttributes().get("jurisdiction").asText());
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

    @Test
    void shouldReturnPrivateLawCtsOrgRolesForRoleId_caseAndTaskSupervisor() {
        allProfiles.clear();
        Stream.of(9,3).forEach(roleId -> {
            CaseWorkerAccessProfile profile = TestDataBuilder.buildUserAccessProfile(roleId + "",
                "ABA5", false);
            profile.setCaseAllocatorFlag("Y");
            profile.setTaskSupervisorFlag("Y");
            allProfiles.add(profile);
        });
        List<RoleAssignment> roleAssignments =
            buildExecuteKieSession(getFeatureFlags("privatelaw_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
            containsInAnyOrder("ctsc-team-leader","task-supervisor", "hmcts-ctsc","case-allocator",
                "hearing-centre-team-leader", "task-supervisor", "hmcts-admin","case-allocator"));
        roleAssignments.forEach(roleAssignment -> {
            if ("ctsc-team-leader".equals(roleAssignment.getRoleName())
                || "hearing-centre-team-leader".equals(roleAssignment.getRoleName())) {
                assertEquals("PRIVATELAW",roleAssignment.getAttributes().get("jurisdiction").asText());
                assertEquals("routine_work,access_requests,hearing_work,applications",
                    roleAssignment.getAttributes().get("workTypes").asText());
            }
            if (RoleCategory.ADMIN.equals(roleAssignment.getRoleCategory())
                && !roleAssignment.getRoleName().contains("hmcts")) {
                assertThat(new String[]{"7"},
                    ArrayMatching.hasItemInArray(roleAssignment.getAttributes().get("region").asText()));
            } else {
                assertNull(roleAssignment.getAttributes().get("region"));
            }
        });
    }

    @Test
    void shouldNotReturnCtsRoles_disabledFlag() {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode("ABA5");
        cap.setSuspended(false);
        cap.setRoleId("10");

        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
            buildExecuteKieSession(getFeatureFlags("privatelaw_wa_1_0", false));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

}
