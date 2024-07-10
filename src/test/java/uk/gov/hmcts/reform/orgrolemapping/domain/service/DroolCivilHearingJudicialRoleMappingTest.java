package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Authorisation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;

@RunWith(MockitoJUnitRunner.class)
class DroolCivilHearingJudicialRoleMappingTest extends DroolBase {

    String userId = "3168da13-00b3-41e3-81fa-cbc71ac28a69";
    List<String> judgeRoleNamesWithWorkTypes = List.of("judge", "task-supervisor", "circuit-judge",
            "specific-access-approver-judiciary", "fee-paid-judge");

    static Stream<Arguments> endToEndData() {
        return Stream.of(
                Arguments.of("District Judge",
                        "Salaried",
                        true,
                        true,
                        List.of(""),
                        List.of("judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("",
                        "Salaried",
                        true,
                        true,
                        List.of("Presiding Judge"),
                        List.of("judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("",
                        "Salaried",
                        true,
                        true,
                        List.of("Resident Judge"),
                        List.of("judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Tribunal Judge",
                        "Salaried",
                        false,
                        true,
                        List.of(""),
                        List.of("judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Employment Judge",
                        "Salaried",
                        false,
                        true,
                        List.of(""),
                        List.of("judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("",
                        "Salaried",
                        false,
                        true,
                        List.of("Designated Civil Judge"),
                        List.of("judge", "leadership-judge", "task-supervisor", "hmcts-judiciary",
                                "case-allocator", "hearing-viewer")),
                Arguments.of("Circuit Judge",
                        "Salaried",
                        false,
                        true,
                        List.of(""),
                        List.of("judge", "circuit-judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Specialist Circuit Judge",
                        "Salaried",
                        true,
                        true,
                        List.of(""),
                        List.of("judge", "circuit-judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Senior Circuit Judge",
                        "Salaried",
                        true,
                        true,
                        List.of(""),
                        List.of("judge", "circuit-judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("High Court Judge",
                        "Salaried",
                        true,
                        true,
                        List.of(""),
                        List.of("judge", "circuit-judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Deputy Circuit Judge",
                        "Fee Paid",
                        true,
                        true,
                        List.of("Deputy District Judge"),
                        List.of("judge", "circuit-judge", "fee-paid-judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Deputy District Judge- Fee-Paid",
                        "Fee Paid",
                        true,
                        true,
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Deputy District Judge- Sitting in Retirement",
                        "Fee Paid",
                        true,
                        true,
                        List.of("Deputy District Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Deputy District Judge (sitting in retirement)",
                        "Fee Paid",
                        true,
                        true,
                        List.of("Deputy District Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Recorder",
                        "Fee Paid",
                        true,
                        true,
                        List.of("Recorder - Fee Paid"),
                        List.of("judge", "fee-paid-judge","hmcts-judiciary", "hearing-viewer")),
                Arguments.of("District Judge (sitting in retirement)",
                        "Fee Paid",
                        true,
                        true,
                        List.of("District Judge (sitting in retirement)"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Tribunal Judge",
                        "Fee Paid",
                        true,
                        true,
                        List.of("Tribunal Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Employment Judge",
                        "Fee Paid",
                        true,
                        true,
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary", "hearing-viewer"))
        );
    }

    @ParameterizedTest
    @MethodSource("endToEndData")
    void shouldTakeJudicialAccessProfileConvertToJudicialOfficeHolderThenReturnRoleAssignments(
            String appointment, String appointmentType, boolean addBooking, boolean hearingFlag,
            List<String> assignedRoles, List<String> expectedRoleNames) {

        allProfiles.clear();
        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();
        judicialBookings.clear();
        if (addBooking) {
            JudicialBooking booking = JudicialBooking.builder()
                    .userId(userId).locationId("Scotland").regionId("1")
                    .build();
            judicialBookings.add(booking);
        }

        judicialAccessProfiles.add(
                JudicialAccessProfile.builder()
                        .appointment(appointment)
                        .appointmentType(appointmentType)
                        .userId(userId)
                        .roles(assignedRoles)
                        .regionId("LDN")
                        .primaryLocationId("London")
                        .ticketCodes(List.of("AAA6"))
                        .authorisations(List.of(
                                Authorisation.builder()
                                        .serviceCodes(List.of("AAA6"))
                                        .jurisdiction("CIVIL")
                                        .endDate(LocalDateTime.now().plusYears(1L))
                                        .build()
                        ))
                        .build()
        );

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(setFeatureFlags(hearingFlag));

        //assertions
        assertFalse(roleAssignments.isEmpty());

        List<String> roleNameResults =
                roleAssignments.stream().map(RoleAssignment::getRoleName).toList();
        assertThat(roleNameResults, containsInAnyOrder(expectedRoleNames.toArray()));

        roleAssignments.forEach(r -> {
            assertEquals(userId, r.getActorId());
            if (!r.getRoleName().contains("hmcts-judiciary")) {
                assertEquals(Classification.PUBLIC, r.getClassification());
                assertEquals(GrantType.STANDARD, r.getGrantType());
                assertEquals("AAA6", r.getAuthorisations().get(0));
                if (!addBooking) {
                    assertEquals("London", r.getAttributes().get("primaryLocation").asText());
                }
                if (judgeRoleNamesWithWorkTypes.contains(r.getRoleName())) {
                    assertEquals("hearing_work,decision_making_work,applications",
                            r.getAttributes().get("workTypes").asText());
                } else if (r.getRoleName().contains("leadership-judge")) {
                    assertEquals("LDN", r.getAttributes().get("region").asText());
                    assertEquals("access_requests",
                            r.getAttributes().get("workTypes").asText());
                } else {
                    assertNull(r.getAttributes().get("workTypes"));
                }
            } else {
                assertEquals(Classification.PRIVATE, r.getClassification());
                assertEquals(GrantType.BASIC, r.getGrantType());
            }
            if (r.getRoleName().contains("magistrate")) {
                assertEquals(Classification.PUBLIC, r.getClassification());
                assertEquals(GrantType.STANDARD, r.getGrantType());
                assertEquals("AAA6", r.getAuthorisations().get(0));
                assertEquals("LDN", r.getAttributes().get("region").asText());
                assertEquals("London", r.getAttributes().get("primaryLocation").asText());
            }
        });

    }

    private List<FeatureFlag> setFeatureFlags(boolean hearingFlag) {
        List<String> flags = List.of("civil_wa_1_0", "civil_wa_1_1", "civil_wa_1_2",
                "civil_wa_1_3", "civil_wa_1_4", "civil_wa_1_5", "civil_wa_1_7", "sscs_hearing_1_0");

        return flags.stream()
                .map(flag -> FeatureFlag.builder()
                        .flagName(flag)
                        .status(!flag.equals("sscs_hearing_1_0") || hearingFlag)
                        .build())
                .toList();
    }

}
