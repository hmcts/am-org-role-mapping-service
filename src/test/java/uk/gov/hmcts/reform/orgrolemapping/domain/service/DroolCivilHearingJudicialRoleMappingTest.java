package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    List<String> judgeRoleNamesWithWorkTypes = List.of("judge", "specific-access-approver-judiciary", "fee-paid-judge",
            "task-supervisor");

    List<String> judgeRoleNamesWithExtendedWorkTypes = List.of("circuit-judge", "district-judge",
            "deputy-district-judge", "recorder");

    static Stream<Arguments> endToEndData() {
        return Stream.of(
                Arguments.of("District Judge",
                        "Salaried",
                        true,
                        List.of(""),
                        List.of("judge", "hmcts-judiciary", "hearing-viewer", "district-judge")),
                Arguments.of("",
                        "Salaried",
                        true,
                        List.of("Presiding Judge"),
                        List.of("judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("",
                        "Salaried",
                        true,
                        List.of("Resident Judge"),
                        List.of("judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Tribunal Judge",
                        "Salaried",
                        false,
                        List.of(""),
                        List.of("judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Employment Judge",
                        "Salaried",
                        false,
                        List.of(""),
                        List.of("judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("",
                        "Salaried",
                        false,
                        List.of("Designated Civil Judge"),
                        List.of("judge", "leadership-judge", "task-supervisor", "hmcts-judiciary",
                                "case-allocator", "hearing-viewer")),
                Arguments.of("Circuit Judge",
                        "Salaried",
                        false,
                        List.of(""),
                        List.of("judge", "circuit-judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Specialist Circuit Judge",
                        "Salaried",
                        true,
                        List.of(""),
                        List.of("judge", "circuit-judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Senior Circuit Judge",
                        "Salaried",
                        true,
                        List.of(""),
                        List.of("judge", "circuit-judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("High Court Judge",
                        "Salaried",
                        true,
                        List.of(""),
                        List.of("judge", "circuit-judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Deputy Circuit Judge",
                        "Fee Paid",
                        true,
                        List.of("Deputy District Judge"),
                        List.of("judge", "circuit-judge", "fee-paid-judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Deputy District Judge- Fee-Paid",
                        "Fee Paid",
                        true,
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary", "hearing-viewer",
                                "deputy-district-judge")),
                Arguments.of("Deputy District Judge",
                        "Fee Paid",
                        true,
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary", "hearing-viewer",
                                "deputy-district-judge")),
                Arguments.of("Deputy District Judge",
                        "Fee Paid",
                        false,
                        List.of(""),
                        List.of("fee-paid-judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Deputy District Judge- Sitting in Retirement",
                        "Fee Paid",
                        true,
                        List.of("Deputy District Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary", "hearing-viewer",
                                "deputy-district-judge")),
                Arguments.of("Deputy District Judge (sitting in retirement)",
                        "Fee Paid",
                        true,
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary", "hearing-viewer",
                                "deputy-district-judge")),
                Arguments.of("Recorder",
                        "Fee Paid",
                        true,
                        List.of("Recorder - Fee Paid"),
                        List.of("judge", "fee-paid-judge","hmcts-judiciary", "hearing-viewer", "recorder")),
                Arguments.of("District Judge (sitting in retirement)",
                        "Fee Paid",
                        true,
                        List.of("District Judge (sitting in retirement)"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary", "hearing-viewer",
                                "deputy-district-judge")),
                Arguments.of("Tribunal Judge",
                        "Fee Paid",
                        true,
                        List.of("Tribunal Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Employment Judge",
                        "Fee Paid",
                        true,
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Circuit Judge (sitting in retirement)",
                        "Fee Paid",
                        true,
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary", "hearing-viewer", "circuit-judge"))
        );
    }

    @ParameterizedTest
    @MethodSource("endToEndData")
    void shouldTakeJudicialAccessProfileConvertToJudicialOfficeHolderThenReturnRoleAssignments(
            String appointment, String appointmentType, boolean addBooking,
            List<String> assignedRoles, List<String> expectedRoleNames) {
        // As CIVIL has 2 service codes AAA6 and AAA7 and the JudicialAccessProfile has only one service code we run
        // the test method twice, once with each service code
        shouldTakeJudicialAccessProfileConvertToJudicialOfficeHolderThenReturnRoleAssignments(
                appointment, appointmentType, addBooking, assignedRoles, expectedRoleNames, "AAA6");
        shouldTakeJudicialAccessProfileConvertToJudicialOfficeHolderThenReturnRoleAssignments(
                appointment, appointmentType, addBooking, assignedRoles, expectedRoleNames, "AAA7");
    }

    void shouldTakeJudicialAccessProfileConvertToJudicialOfficeHolderThenReturnRoleAssignments(
            String appointment, String appointmentType, boolean addBooking,
            List<String> assignedRoles, List<String> expectedRoleNames, String serviceCode) {

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
                        .ticketCodes(List.of(serviceCode))
                        .authorisations(List.of(
                                Authorisation.builder()
                                        .serviceCodes(List.of(serviceCode))
                                        .jurisdiction("CIVIL")
                                        .endDate(LocalDateTime.now().plusYears(1L))
                                        .build()
                        ))
                        .build()
        );

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(setFeatureFlags());

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
                assertEquals(serviceCode, r.getAuthorisations().get(0));
                if (!addBooking) {
                    assertEquals("London", r.getAttributes().get("primaryLocation").asText());
                }
                if (judgeRoleNamesWithWorkTypes.contains(r.getRoleName())) {
                    assertEquals("decision_making_work,applications",
                            r.getAttributes().get("workTypes").asText());
                } else if (judgeRoleNamesWithExtendedWorkTypes.contains(r.getRoleName())) {
                    assertEquals("decision_making_work,applications,Multi_Track_decision_making_work,"
                                   + "Intermediate_Track_decision_making_work",
                            r.getAttributes().get("workTypes").asText());
                } else if (r.getRoleName().contains("leadership-judge")) {
                    assertEquals("LDN", r.getAttributes().get("region").asText());
                    assertEquals("decision_making_work,applications,access_requests,"
                            + "Multi_Track_decision_making_work,Intermediate_Track_decision_making_work",
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
                assertEquals(serviceCode, r.getAuthorisations().get(0));
                assertEquals("LDN", r.getAttributes().get("region").asText());
                assertEquals("London", r.getAttributes().get("primaryLocation").asText());
            }
        });

    }

    private List<FeatureFlag> setFeatureFlags() {
        List<FeatureFlag> featureFlags = new ArrayList<>(getAllFeatureFlagsToggleByJurisdiction("CIVIL", true));

        for (FeatureFlag flag : featureFlags) {
            if (flag.getFlagName().contains("hearing")) {
                flag.setStatus(true);
            }
        }

        return featureFlags;
    }

}
