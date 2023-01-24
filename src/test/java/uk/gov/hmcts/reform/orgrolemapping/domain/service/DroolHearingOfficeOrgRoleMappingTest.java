package uk.gov.hmcts.reform.orgrolemapping.domain.service;


import org.hamcrest.MatcherAssert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.buildUserAccessProfile3;


@RunWith(MockitoJUnitRunner.class)
class DroolHearingOfficeOrgRoleMappingTest extends DroolBase {

    static final String LD_FLAG = "sscs_hearing_1_0";

    @ParameterizedTest
    @CsvSource({
            "BBA3,SSCS",
            "ABA5,PRIVATELAW"

    })
    void shouldReturnHearingJudicialRoles(String serviceCode, String jurisdiction) {

        judicialAccessProfiles.forEach(judicialAccessProfile -> judicialAccessProfile.getAuthorisations().forEach(a ->
                a.setServiceCodes(List.of(serviceCode))));
        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags(LD_FLAG, true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());

        roleAssignments.forEach(r -> {
            assertEquals("hearing-viewer", r.getRoleName());
            assertEquals(judicialAccessProfiles.stream().iterator().next().getEndTime().plusDays(1), r.getEndTime());
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals(jurisdiction, r.getAttributes().get("jurisdiction").asText());
            MatcherAssert.assertThat(judicialAccessProfiles.stream().iterator().next().getTicketCodes(),
                    containsInAnyOrder(r.getAuthorisations().toArray()));
            assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
        });
    }

    @ParameterizedTest
    @CsvSource({
            "BBA3",
            "ABA5"
    })
    void shouldReturnEmptyRoles_expiredAuthorisation(String serviceCode) {

        judicialAccessProfiles.forEach(jap -> jap.getAuthorisations().forEach(a -> {
            a.setServiceCodes(List.of(serviceCode));
            a.setEndDate(LocalDateTime.now().minusDays(1));
        }));
        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags(LD_FLAG, true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }


    @ParameterizedTest
    @CsvSource({
            "false,BBA3",
            "true,DUMMY"
    })
    void shouldReturnEmptyRoles(boolean ldFlag, String serviceCode) {

        judicialAccessProfiles.forEach(jap -> jap.getAuthorisations().forEach(a ->
                a.setServiceCodes(List.of(serviceCode))));
        List.of("2","4","5","9","10","12","13","14","15").forEach(a ->
                allProfiles.add(buildUserAccessProfile3(serviceCode, a, "")));
        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags(LD_FLAG, ldFlag));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }


    @ParameterizedTest
    @CsvSource({
            "'4,5,9,10,12,13',BBA3,SSCS",
            "'3,4',ABA3,PUBLICLAW",
            "'3,4',ABA5,PRIVATELAW"
            
    })
    void shouldReturnHearingManagerAndViewerCaseWorker_Admin(String roleId, String serviceCode,
                                                             String jurisdiction) {
        List<String> roleIds = List.of(roleId.split(","));
        roleIds.forEach(a -> allProfiles.add(buildUserAccessProfile3(serviceCode, a, "")));

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags(LD_FLAG, true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2, roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals(RoleCategory.ADMIN, r.getRoleCategory());
            assertEquals(usersAccessProfiles.keySet().stream().iterator().next(), r.getActorId());
            assertEquals(jurisdiction, r.getAttributes().get("jurisdiction").asText());
            assertThat(r.getRoleName()).matches(s -> Stream.of("hearing-manager", "hearing-viewer")
                    .anyMatch(s::contains));
        });
    }

    @ParameterizedTest
    @CsvSource({
            "'9,10',ABA5,PRIVATELAW",
            "'9,10',ABA3,PUBLICLAW"
    })
    void shouldReturnHearingManagerAndViewerCaseWorker_Ctsc(String roleId, String serviceCode,
                                                             String jurisdiction) {
        List<String> roleIds = List.of(roleId.split(","));
        roleIds.forEach(a -> allProfiles.add(buildUserAccessProfile3(serviceCode, a, "")));

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags(LD_FLAG, true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2, roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals(RoleCategory.CTSC, r.getRoleCategory());
            assertEquals(usersAccessProfiles.keySet().stream().iterator().next(), r.getActorId());
            assertEquals(jurisdiction, r.getAttributes().get("jurisdiction").asText());
            assertThat(r.getRoleName()).matches(s -> Stream.of("hearing-manager", "hearing-viewer")
                    .anyMatch(s::contains));
        });
    }

    @ParameterizedTest
    @CsvSource({
            "2,BBA3,SSCS",
            "2,ABA5,PRIVATELAW",
            "1,ABA3,PUBLICLAW",
            "2,ABA3,PUBLICLAW",     
            "1,ABA5,PRIVATELAW"
            
    })
    void shouldReturnHearingManagerAndViewerCaseWorker_LegalOps(String roleId, String serviceCode,
                                                                String jurisdiction) {
        allProfiles.add(buildUserAccessProfile3(serviceCode, roleId, ""));

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags(LD_FLAG, true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2, roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals(RoleCategory.LEGAL_OPERATIONS, r.getRoleCategory());
            assertEquals(usersAccessProfiles.keySet().stream().iterator().next(), r.getActorId());
            assertEquals(jurisdiction, r.getAttributes().get("jurisdiction").asText());
            assertThat(r.getRoleName()).matches(s -> Stream.of("hearing-manager", "hearing-viewer")
                    .anyMatch(s::contains));
        });
    }

    @ParameterizedTest
    @CsvSource({
            "14,BBA3,SSCS",
            "15,BBA3,SSCS",
            "14,ABA5,PRIVATELAW",
            "15,ABA5,PRIVATELAW"

    })
    void shouldReturnListedHearingViewerCaseWorker_otherGovDept(String roleId, String serviceCode,
                                                                String jurisdiction) {
        allProfiles.add(buildUserAccessProfile3(serviceCode, roleId, ""));

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags(LD_FLAG, true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals(RoleCategory.OTHER_GOV_DEPT, r.getRoleCategory());
            assertEquals(usersAccessProfiles.keySet().stream().iterator().next(), r.getActorId());
            assertEquals(jurisdiction, r.getAttributes().get("jurisdiction").asText());
            assertThat(r.getRoleName()).matches(s -> Stream.of("listed-hearing-viewer").anyMatch(s::contains));
        });
    }
}