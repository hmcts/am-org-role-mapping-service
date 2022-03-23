package uk.gov.hmcts.reform.orgrolemapping.domain.service;


import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.ROLE_ASSIGNMENTS_RESULTS_KEY;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.buildUserAccessProfile3;


@RunWith(MockitoJUnitRunner.class)
public class DroolHearingOfficeOrgRoleMappingTest extends DroolBase {
    String workTypes = "";
    static final String SERVICE_CODE = "BBA3";
    static final String LD_FLAG = "sscs_hearing_1_0";

    @Test
    void shouldReturnJudgeRoles_withSscs() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> judicialAccessProfile.getAuthorisations().forEach(a ->
                a.setServiceCode(SERVICE_CODE)));
        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags(LD_FLAG, true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());

        roleAssignments.forEach(r -> {
            assertEquals("hearing-viewer", r.getRoleName());
            assertEquals(workTypes,r.getAttributes().get("workTypes").asText());
            assertEquals(judicialAccessProfiles.stream().iterator().next().getEndTime().plusDays(1), r.getEndTime());
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());
            MatcherAssert.assertThat(judicialAccessProfiles.stream().iterator().next().getTicketCodes(),
                    containsInAnyOrder(r.getAuthorisations().toArray()));
            assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
        });
    }

    @Test
    void shouldReturnEmptyRoles_withSscs_expiredAuthorisation() {

        judicialAccessProfiles.forEach(jap -> jap.getAuthorisations().forEach(a -> {
            a.setServiceCode(SERVICE_CODE);
            a.setEndDate(LocalDateTime.now().minusDays(1));
        }));
        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags(LD_FLAG, true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }


    @ParameterizedTest
    @CsvSource({
            "false,BBA3",
            "true,DUMMY"
    })
    void shouldReturnEmptyRoles(boolean ldFlag, String serviceCode) {

        judicialAccessProfiles.forEach(jap -> jap.getAuthorisations().forEach(a -> a.setServiceCode(serviceCode)));
        List.of("2","4","5","9","10","12","13","14","15").forEach(a ->
                allProfiles.add(buildUserAccessProfile3(serviceCode, a, "")));
        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags(LD_FLAG, ldFlag));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }


    @Test
    void shouldReturnHearingManagerAndViewerCaseWorker_Admin() {
        List<String> roleIds = List.of("4","5","9","10","12","13");
        roleIds.forEach(a -> allProfiles.add(buildUserAccessProfile3(SERVICE_CODE, a, "")));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags(LD_FLAG, true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(roleIds.size() * 2, roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals(RoleCategory.ADMIN, r.getRoleCategory());
            assertEquals(usersAccessProfiles.keySet().stream().iterator().next(), r.getActorId());
            assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());
            assertThat(r.getRoleName()).matches(s -> Stream.of("hearing-manager", "hearing-viewer")
                    .anyMatch(s::contains));
            assertEquals(workTypes, r.getAttributes().get("workTypes").asText());
        });
    }

    @Test
    void shouldReturnHearingManagerAndViewerCaseWorker_LegalOps() {
        allProfiles.add(buildUserAccessProfile3(SERVICE_CODE, "2", ""));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags(LD_FLAG, true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2, roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals(RoleCategory.LEGAL_OPERATIONS, r.getRoleCategory());
            assertEquals(usersAccessProfiles.keySet().stream().iterator().next(), r.getActorId());
            assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());
            assertThat(r.getRoleName()).matches(s -> Stream.of("hearing-manager", "hearing-viewer")
                    .anyMatch(s::contains));
            assertEquals(workTypes, r.getAttributes().get("workTypes").asText());
        });
    }

    @Test
    void shouldReturnListedHearingViewerCaseWorker_otherGovDept() {
        List<String> roleIds = List.of("14","15");
        roleIds.forEach(a -> allProfiles.add(buildUserAccessProfile3(SERVICE_CODE, a, "")));

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags(LD_FLAG, true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(roleIds.size(), roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals(RoleCategory.OTHER_GOV_DEPT, r.getRoleCategory());
            assertEquals(usersAccessProfiles.keySet().stream().iterator().next(), r.getActorId());
            assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());
            assertThat(r.getRoleName()).matches(s -> Stream.of("listed-hearing-viewer").anyMatch(s::contains));
            assertEquals(workTypes, r.getAttributes().get("workTypes").asText());
        });
    }
}
