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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@Slf4j
class DroolIacJudicialRoleMappingTest extends DroolBase {

    static String workTypes = "hearing_work,upper_tribunal,decision_making_work,applications";
    static String workTypesFP = "hearing_work,decision_making_work,applications";
    static String workTypesAccess = "hearing_work,upper_tribunal,decision_making_work,applications,access_requests";

    static Map<String, String> expectedRoleNameWorkTypesMap = new HashMap<>();

    static {
        expectedRoleNameWorkTypesMap.put("senior-judge", workTypes);
        expectedRoleNameWorkTypesMap.put("hmcts-judiciary", null);
        expectedRoleNameWorkTypesMap.put("leadership-judge", workTypesAccess);
        expectedRoleNameWorkTypesMap.put("case-allocator", null);
        expectedRoleNameWorkTypesMap.put("task-supervisor", null);
        expectedRoleNameWorkTypesMap.put("judge", workTypes);
        expectedRoleNameWorkTypesMap.put("fee-paid-judge", workTypesFP);
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        allProfiles.clear();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("iacRoleScenarios")
    void shouldReturnCorrectIacRoles(
            String office,
            List<String> expectedRoles,
            String expectedContractType) {

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

            assertTrue(expectedRoles.contains(ra.getRoleName()));
            assertEquals(expectedActorId, ra.getActorId());
            assertEquals(
                    expectedContractType,
                    ra.getAttributes().get("contractType").asText()
            );

            if (expectedRoleNameWorkTypesMap.containsKey(ra.getRoleName())) {
                String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(ra.getRoleName());
                String actualWorkTypes = null;
                if (ra.getAttributes().get("workTypes") != null) {
                    actualWorkTypes = ra.getAttributes().get("workTypes").asText();
                }
                assertEquals(expectedWorkTypes, actualWorkTypes);
            } else {
                assertFalse(ra.getAttributes().containsKey("workTypes"));
            }
        }
    }

    static Stream<Arguments> iacRoleScenarios() {
        return Stream.of(

            // IAC President
            Arguments.of(
                "IAC President of Tribunals",
                List.of(
                    "senior-judge",
                    "hmcts-judiciary",
                    "case-allocator",
                    "judge"
                ),
                "Salaried"
            ),

            // IAC Resident Immigration Judge
            Arguments.of(
                "IAC Resident Immigration Judge",
                List.of(
                    "senior-judge",
                    "hmcts-judiciary",
                    "leadership-judge",
                    "case-allocator",
                    "task-supervisor",
                    "judge"
                ),
                "Salaried"
            ),

            // IAC Designated Immigration Judge
            Arguments.of(
                "IAC Designated Immigration Judge",
                List.of(
                    "hmcts-judiciary",
                    "leadership-judge",
                    "case-allocator",
                    "task-supervisor",
                    "judge"
                ),
                "Salaried"
            ),

            // IAC Assistant Resident Judge
            Arguments.of(
                "IAC Assistant Resident Judge",
                List.of(
                    "hmcts-judiciary",
                    "leadership-judge",
                    "case-allocator",
                    "task-supervisor",
                    "judge"
                ),
                "Salaried"
            ),

            // IAC Tribunal Judge (Salaried)
            Arguments.of(
                "IAC Tribunal Judge (Salaried)",
                List.of(
                    "hmcts-judiciary",
                    "case-allocator",
                    "judge"
                ),
                "Salaried"
            ),

            // IAC Tribunal Judge (Fee-Paid)
            Arguments.of(
                "IAC Tribunal Judge (Fee-Paid)",
                List.of(
                    "hmcts-judiciary",
                    "fee-paid-judge"
                ),
                "Fee-Paid"
            ),

            // IAC Resident Tribunal Judge
            Arguments.of(
                JudicialOfficeHolder.Office.IAC.RESIDENT_OF_TRIBUNAL_JUDGE,
                List.of(
                    "hmcts-judiciary",
                    "leadership-judge",
                    "case-allocator",
                    "task-supervisor",
                    "judge"
                ),
                "Salaried"
            ),

            // IAC Acting Resident Judge
            Arguments.of(
                JudicialOfficeHolder.Office.IAC.ACTING_RESIDENT_JUDGE,
                List.of(
                    "hmcts-judiciary",
                    "leadership-judge",
                    "case-allocator",
                    "task-supervisor",
                    "judge"
                ),
                "Salaried"
            )
        );
    }

    private List<FeatureFlag> getFeatureFlags() {
        return getAllFeatureFlagsToggleByJurisdiction("IAC", true);
    }

}
