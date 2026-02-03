package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialOfficeHolder;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
@Slf4j
class DroolIacJudicialRoleMappingTest extends DroolBase {

    static String workTypes = "hearing_work,upper_tribunal,decision_making_work,applications";
    static String workTypesFP = "hearing_work,decision_making_work,applications";
    static String workTypesAccess = "hearing_work,upper_tribunal,decision_making_work,applications,access_requests";

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        allProfiles.clear();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("iacRoleScenarios")
    void shouldReturnCorrectIacRoles(
            String scenario,
            String office,
            List<String> expectedRoles,
            String expectedContractType,
            Map<Integer, String> expectedWorkTypesByIndex) {

        // given
        judicialOfficeHolders.forEach(joh -> joh.setOffice(office));

        // when
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags());

        // then
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.size(), roleAssignments.size());

        String expectedActorId =
                judicialOfficeHolders.iterator().next().getUserId();

        for (int i = 0; i < expectedRoles.size(); i++) {
            RoleAssignment ra = roleAssignments.get(i);

            assertEquals(expectedRoles.get(i), ra.getRoleName());
            assertEquals(expectedActorId, ra.getActorId());
            assertEquals(
                    expectedContractType,
                    ra.getAttributes().get("contractType").asText()
            );
        }

        expectedWorkTypesByIndex.forEach((index, expectedWorkType) ->
                assertEquals(
                        expectedWorkType,
                        roleAssignments.get(index)
                                .getAttributes()
                                .get("workTypes")
                                .asText()
                )
        );
    }

    static Stream<Arguments> iacRoleScenarios() {
        return Stream.of(

                // IAC President
                Arguments.of(
                        "IAC President of Tribunals",
                        "IAC President of Tribunals",
                        List.of(
                                "senior-judge",
                                "hmcts-judiciary",
                                "case-allocator",
                                "judge"
                        ),
                        "Salaried",
                        Map.of(
                                0, workTypes,
                                3, workTypes
                        )
                ),

                // IAC Resident Immigration Judge
                Arguments.of(
                        "IAC Resident Immigration Judge",
                        "IAC Resident Immigration Judge",
                        List.of(
                                "senior-judge",
                                "hmcts-judiciary",
                                "leadership-judge",
                                "case-allocator",
                                "task-supervisor",
                                "judge"
                        ),
                        "Salaried",
                        Map.of(
                                0, workTypes,
                                2, workTypesAccess,
                                5, workTypes
                        )
                ),

                // IAC Designated Immigration Judge
                Arguments.of(
                        "IAC Designated Immigration Judge",
                        "IAC Designated Immigration Judge",
                        List.of(
                                "hmcts-judiciary",
                                "leadership-judge",
                                "case-allocator",
                                "task-supervisor",
                                "judge"
                        ),
                        "Salaried",
                        Map.of(
                                1, workTypesAccess,
                                4, workTypes
                        )
                ),

                // IAC Assistant Resident Judge
                Arguments.of(
                        "IAC Assistant Resident Judge",
                        "IAC Assistant Resident Judge",
                        List.of(
                                "hmcts-judiciary",
                                "leadership-judge",
                                "case-allocator",
                                "task-supervisor",
                                "judge"
                        ),
                        "Salaried",
                        Map.of(
                                1, workTypesAccess,
                                4, workTypes
                        )
                ),

                // IAC Tribunal Judge (Salaried)
                Arguments.of(
                        "IAC Tribunal Judge (Salaried)",
                        "IAC Tribunal Judge (Salaried)",
                        List.of(
                                "hmcts-judiciary",
                                "case-allocator",
                                "judge"
                        ),
                        "Salaried",
                        Map.of(
                                2, workTypes
                        )
                ),

                // IAC Tribunal Judge (Fee-Paid)
                Arguments.of(
                        "IAC Tribunal Judge (Fee-Paid)",
                        "IAC Tribunal Judge (Fee-Paid)",
                        List.of(
                                "hmcts-judiciary",
                                "fee-paid-judge"
                        ),
                        "Fee-Paid",
                        Map.of(
                                1, workTypesFP
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("residentAndActingResidentIacOffices")
    void shouldReturnResidentAndActingResidentJudgeRoles(
            String office) {

        // given
        judicialOfficeHolders.forEach(joh ->
                joh.setOffice(office));

        // when
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags());

        // then
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5, roleAssignments.size());

        assertEquals("hmcts-judiciary", roleAssignments.get(0).getRoleName());
        assertEquals("leadership-judge", roleAssignments.get(1).getRoleName());
        assertEquals("case-allocator", roleAssignments.get(2).getRoleName());
        assertEquals("task-supervisor", roleAssignments.get(3).getRoleName());
        assertEquals("judge", roleAssignments.get(4).getRoleName());

        String expectedActorId =
                judicialOfficeHolders.iterator().next().getUserId();

        roleAssignments.forEach(ra ->
                assertEquals(expectedActorId, ra.getActorId()));
    }

    static Stream<String> residentAndActingResidentIacOffices() {
        return Stream.of(
                JudicialOfficeHolder.Office.IAC.RESIDENT_OF_TRIBUNAL_JUDGE,
                JudicialOfficeHolder.Office.IAC.ACTING_RESIDENT_JUDGE
        );
    }

    private List<FeatureFlag> getFeatureFlags() {
        return getAllFeatureFlagsToggleByJurisdiction("IAC", true);
    }

}
