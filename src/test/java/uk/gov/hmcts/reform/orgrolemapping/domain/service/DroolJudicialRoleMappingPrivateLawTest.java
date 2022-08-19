package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Authorisation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(MockitoJUnitRunner.class)
class DroolJudicialRoleMappingPrivateLawTest extends DroolBase {

    static Stream<Arguments> generateData() {
        return Stream.of(
                Arguments.of("Circuit Judge",
                        "Salaried",
                        List.of(""),
                        List.of("circuit-judge", "hmcts-judiciary")),
                Arguments.of("Deputy Circuit Judge",
                        "Fee Paid",
                        List.of(""),
                        List.of("circuit-judge", "fee-paid-judge", "hmcts-judiciary")),
                Arguments.of("Deputy District Judge – PRFD",
                        "Fee Paid",
                        List.of("Deputy District Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary")),
                Arguments.of("Deputy District Judge (MC) - Fee Paid",
                        "Fee Paid",
                        List.of("Deputy District Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary")),
                Arguments.of("Deputy District Judge (MC) - Sitting in Retirement",
                        "Fee Paid",
                        List.of("Deputy District Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary")),
                Arguments.of("Deputy District Judge - Fee Paid",
                        "Fee Paid",
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary")),
                Arguments.of("Deputy District Judge - Sitting in Retirement",
                        "Fee Paid",
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary")),
                Arguments.of("Deputy High Court Judge",
                        "Fee Paid",
                        List.of("Deputy High Court Judge"),
                        List.of("judge","fee-paid-judge","hmcts-judiciary")),
                Arguments.of("District Judge",
                        "Salaried",
                        List.of(""),
                        List.of("judge", "hmcts-judiciary")),
                //Arguments.of("District Judge (MC)", "SPTW",
                //        List.of(""),
                //        List.of("judge","fee-paid-judge","hmcts-judiciary")),
                // doesn't exist in table 2.6 but does in 2.2
                Arguments.of("High Court Judge",
                        "Salaried",
                        List.of(""),
                        List.of("circuit-judge", "hmcts-judiciary")),
                Arguments.of("High Court Judge - Sitting in Retirement",
                        "Fee Paid",
                        List.of("High Court Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary")),
                Arguments.of("Recorder",
                        "Fee Paid",
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary")),
                Arguments.of("Designated Family Judge",
                        "Salaried",
                        List.of("Designated Family Judge"),
                        List.of("leadership-judge","judge","task-supervisor","hmcts-judiciary","case-allocator")),
                Arguments.of("Family Division Liaison Judge",
                        "Salaried",
                        List.of("Presiding Judge"),
                        List.of("judge", "hmcts-judiciary")),
                Arguments.of("Senior Family Liaison Judge",
                        "Salaried",
                        List.of("Resident Judge"),
                        List.of("judge", "hmcts-judiciary"))
        );
    }

    @ParameterizedTest
    @MethodSource("generateData")
    void shouldTakeJudicialAccessProfileConvertToJudicialOfficeHolderThenReturnRoleAssignments(
            String appointment, String appointmentType, List<String> assignedRoles, List<String> expectedRoleNames) {

        String userId = "3168da13-00b3-41e3-81fa-cbc71ac28a69";

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        judicialBookings.add(JudicialBooking.builder().userId(userId).build());

        judicialAccessProfiles.add(
                JudicialAccessProfile.builder()
                        .appointment(appointment)
                        .appointmentType(appointmentType)
                        .userId(userId)
                        .roles(assignedRoles)
                        .authorisations(List.of(
                                Authorisation.builder()
                                        .serviceCodes(List.of("ABA5"))
                                        .endDate(LocalDateTime.now().plusYears(1L))
                                        .build()
                        ))
                        .build()
        );

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("privatelaw_wa_1_0", true));

        //assertions
        assertFalse(roleAssignments.isEmpty());

        List<String> roleNameResults =
                roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList());
        assertThat(roleNameResults, containsInAnyOrder(expectedRoleNames.toArray()));

    }
}
