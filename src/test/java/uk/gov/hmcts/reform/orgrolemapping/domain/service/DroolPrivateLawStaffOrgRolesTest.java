package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
class DroolPrivateLawStaffOrgRolesTest extends DroolBase {

    @ParameterizedTest
    @CsvSource({
            "10,ABA5,'ctsc,hmcts-ctsc','routine_work,hearing_work,applications'",
            "9,ABA5,'ctsc-team-leader,hmcts-ctsc','routine_work,access_requests,hearing_work,applications'",
    })
    void shouldReturnPrivateLawStaffMappings(String roleId, String serviceCode, String expectedRoles,
                                             String worktypes) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(serviceCode);
        cap.setSuspended(false);
        cap.setRoleId(roleId);

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
        });

        roleAssignments.stream().filter(c -> c.getGrantType().equals(GrantType.STANDARD)).collect(Collectors.toList())
                .forEach(r -> {
                    assertEquals("PRIVATELAW", r.getAttributes().get("jurisdiction").asText());
                    assertEquals(worktypes, r.getAttributes().get("workTypes").asText());
                    assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
                });
    }

    static Stream<Arguments> generateData() {
        return Stream.of(
                Arguments.of("10", Arrays.asList("ctsc", "hmcts-ctsc"),
                        Collections.singletonList("routine_work,hearing_work,applications"),"CTSC"),
                Arguments.of("4", Arrays.asList("hearing-centre-admin", "hmcts-admin"),
                        Collections.singletonList("routine_work,hearing_work,applications"),"ADMINISTRATOR"),
                Arguments.of("2", Arrays.asList("tribunal-caseworker", "hmcts-legal-operations"),
                        Collections.singletonList("routine_work,hearing_work,applications"),"LEGAL_OPERATIONS"),
                Arguments.of("1", Arrays.asList("senior-tribunal-caseworker", "hmcts-legal-operations"),
                        Collections.singletonList("decision_making_work,access_requests"),"LEGAL_OPERATIONS"));
    }

    @ParameterizedTest
    @MethodSource("generateData")
    void shouldReturnPrivateLawAdminMappings(String roleId, List<String> roleNames, List<String> workTypes,
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
        assertEquals(roleNames.size(), roleAssignments.size());

        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNames.toArray()));

        roleAssignments.forEach(r -> {
            assertTrue(roleCategory.contains(r.getRoleCategory().toString()));
            if (GrantType.STANDARD == r.getGrantType()) {
                assertEquals("PRIVATELAW", r.getAttributes().get("jurisdiction").asText());
                assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
            }
        });

        List<Map<String, JsonNode>> list = roleAssignments.stream().map(RoleAssignment::getAttributes).toList();
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
    @CsvSource({
            "1,ABA5,'senior-tribunal-caseworker,task-supervisor,hmcts-legal-operations,case-allocator'," +
                    "'routine_work,hearing_work,applications'",
            "3,ABA5,'hearing-centre-team-leader,task-supervisor,hmcts-admin,case-allocator'," +
                    "'routine_work,hearing_work,applications'",
            "9,ABA5,'ctsc-team-leader,hmcts-ctsc,task-supervisor,case-allocator'," +
                    "'routine_work,access_requests,hearing_work,applications'",
    })
    void shouldReturnPrivateLawCtsOrgRolesForRoleId_caseAndTaskSupervisor(String roleId, String serviceCode,
                                                                          String expectedRoles, String worktypes) {
        allProfiles.clear();

            CaseWorkerAccessProfile profile = TestDataBuilder.buildUserAccessProfile(roleId, serviceCode,
                    false);
            profile.setCaseAllocatorFlag("Y");
            profile.setTaskSupervisorFlag("Y");
            allProfiles.add(profile);

        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("privatelaw_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        roleAssignments.forEach(roleAssignment -> {
            if ("ctsc-team-leader".equals(roleAssignment.getRoleName())
                    || "hearing-centre-team-leader".equals(roleAssignment.getRoleName())) {
                assertEquals("PRIVATELAW",roleAssignment.getAttributes().get("jurisdiction").asText());
                assertEquals("routine_work,access_requests,hearing_work,applications",
                        roleAssignment.getAttributes().get("workTypes").asText());
            }
            if (RoleCategory.CTSC != roleAssignment.getRoleCategory() && !List.of("hmcts-admin", "hmcts-legal-operations",
                    "ctsc-team-leader", "ctsc", "hmcts-ctsc").contains(roleAssignment.getRoleName())) {
                assertNotNull(roleAssignment.getAttributes().get("region").asText());
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