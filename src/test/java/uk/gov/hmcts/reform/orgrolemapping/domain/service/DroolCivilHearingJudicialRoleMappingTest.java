package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
    List<String> judgeRoleNamesWithWorkTypes = List.of("judge", "task-supervisor", "case-allocator",
            "specific-access-approver-judiciary", "fee-paid-judge");

    static Stream<Arguments> endToEndData() {
        return Stream.of(
                Arguments.of("Circuit Judge",
                        "Salaried",
                        false,
                        List.of(""),
                        List.of("circuit-judge", "hmcts-judiciary", "hearing-viewer")),
                Arguments.of("Deputy Circuit Judge",
                        "Fee Paid",
                        true,
                        List.of("Deputy District Judge"),
                        List.of("circuit-judge", "fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer")),
                Arguments.of("Deputy District Judge- Sitting in Retirement",
                        "Fee Paid",
                        true,
                        List.of("Deputy District Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer")),
                Arguments.of("Deputy District Judge- Fee-Paid",
                        "Fee Paid",
                        true,
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer")),
                Arguments.of("District Judge",
                        "Salaried",
                        true,
                        List.of(""),
                        List.of("judge", "hmcts-judiciary",
                                "hearing-viewer")),
                Arguments.of("High Court Judge",
                        "Salaried",
                        true,
                        List.of(""),
                        List.of("circuit-judge", "hmcts-judiciary",
                                "hearing-viewer")),
            Arguments.of("Senior Circuit Judge",
                        "Salaried",
                        true,
                        List.of(""),
                        List.of("circuit-judge", "hmcts-judiciary",
                            "hearing-viewer")),
            Arguments.of("Specialist Circuit Judge",
                        "Salaried",
                        true,
                        List.of(""),
                        List.of("circuit-judge", "hmcts-judiciary",
                            "hearing-viewer")),
                Arguments.of("Recorder", "Fee Paid",
                        false,
                        List.of("Recorder - Fee Paid"),
                        List.of("fee-paid-judge","hmcts-judiciary",
                                "hearing-viewer"))
        );
    }

    @ParameterizedTest
    @MethodSource("endToEndData")
    void shouldTakeJudicialAccessProfileConvertToJudicialOfficeHolderThenReturnRoleAssignments(
            String appointment, String appointmentType, boolean addBooking,
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
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(List.of(FeatureFlag.builder().flagName("civil_wa_1_0").status(true).build()));

        //assertions
        assertFalse(roleAssignments.isEmpty());

        List<String> roleNameResults =
                roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList());
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
}
