package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Authorisation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
class DroolPrivateLawJudicialRoleMappingTest extends DroolBase {

    String userId = "3168da13-00b3-41e3-81fa-cbc71ac28a69";
    List<String> judgeRoleNamesWithWorkTypes = List.of("judge", "circuit-judge", "fee-paid-judge");
    List<String> bookingLocationAppointments = List.of(
            "Deputy District Judge- Fee-Paid",
            "Deputy District Judge- Sitting in Retirement", "Recorder",
            "Deputy District Judge - PRFD",
            "Deputy District Judge (MC)- Fee paid",
            "Deputy District Judge (MC)- Sitting in Retirement",
            "Deputy High Court Judge",
            "High Court Judge- Sitting in Retirement",
            "Deputy Circuit Judge");

    static Stream<Arguments> endToEndData() {
        return Stream.of(
                Arguments.of("Circuit Judge",
                        "Salaried",
                        List.of(""),
                        List.of("circuit-judge", "hmcts-judiciary")),
                Arguments.of("Deputy Circuit Judge",
                        "Fee Paid",
                        List.of(""),
                        List.of("circuit-judge", "fee-paid-judge", "hmcts-judiciary")),
                Arguments.of("Deputy District Judge - PRFD",
                        "Fee Paid",
                        List.of("Deputy District Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary")),
                Arguments.of("Deputy District Judge (MC)- Fee paid",
                        "Fee Paid",
                        List.of("Deputy District Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary")),
                Arguments.of("Deputy District Judge (MC)- Sitting in Retirement",
                        "Fee Paid",
                        List.of("Deputy District Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary")),
                Arguments.of("Deputy District Judge- Fee-Paid",
                        "Fee Paid",
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary")),
                Arguments.of("Deputy District Judge- Sitting in Retirement",
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
                Arguments.of("District Judge (MC)",
                        "SPTW",
                        List.of("District Judge"),
                        List.of("judge","hmcts-judiciary")),
                Arguments.of("High Court Judge",
                        "Salaried",
                        List.of(""),
                        List.of("circuit-judge", "hmcts-judiciary")),
                Arguments.of("High Court Judge- Sitting in Retirement",
                        "Fee Paid",
                        List.of("High Court Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary")),
                Arguments.of("Recorder",
                        "Fee Paid",
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary")),
                Arguments.of("",
                        "",
                        List.of("Designated Family Judge"),
                        List.of("leadership-judge","judge","task-supervisor","hmcts-judiciary","case-allocator",
                                "specific-access-approver-judiciary")),
                Arguments.of("",
                        "",
                        List.of("Family Division Liaison Judge"),
                        List.of("judge", "hmcts-judiciary")),
                Arguments.of("",
                        "",
                        List.of("Senior Family Liaison Judge"),
                        List.of("judge", "hmcts-judiciary"))
        );
    }

    @ParameterizedTest
    @MethodSource("endToEndData")
    void shouldTakeJudicialAccessProfileConvertToJudicialOfficeHolderThenReturnRoleAssignments(
            String appointment, String appointmentType, List<String> assignedRoles, List<String> expectedRoleNames) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();
        JudicialBooking booking = JudicialBooking.builder().userId(userId).locationId("Scotland").regionId("1").build();
        judicialBookings.add(booking);

        judicialAccessProfiles.add(
                JudicialAccessProfile.builder()
                        .appointment(appointment)
                        .appointmentType(appointmentType)
                        .userId(userId)
                        .roles(assignedRoles)
                        .regionId("LDN")
                        .primaryLocationId("London")
                        .ticketCodes(List.of("ABA5"))
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

        roleAssignments.forEach(r -> {
            assertEquals(userId, r.getActorId());
            if (!r.getRoleName().contains("hmcts-judiciary")) {
                assertEquals("ABA5", r.getAuthorisations().get(0));
                if (judgeRoleNamesWithWorkTypes.contains(r.getRoleName())) {
                    assertEquals("hearing_work,decision_making_work,applications",
                            r.getAttributes().get("workTypes").asText());
                } else if (r.getRoleName().contains("leadership-judge")) {
                    assertEquals("LDN", r.getAttributes().get("region").asText());
                }
                if (bookingLocationAppointments.contains(appointment)
                        && List.of("circuit-judge", "judge").contains(r.getRoleName())) {
                    assertEquals(booking.getLocationId(), r.getAttributes().get("primaryLocation").asText());
                    assertEquals(booking.getLocationId(), r.getAttributes().get("baseLocation").asText());
                    assertEquals(booking.getRegionId(), r.getAttributes().get("region").asText());
                } else {
                    assertEquals("London", r.getAttributes().get("primaryLocation").asText());
                    assertEquals("LDN", r.getAttributes().get("region").asText());
                }
            } else {
                assertEquals(1, r.getAttributes().size());
            }
        });
    }

    @Test
    void falsePrivateLawFlagTest() {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        judicialAccessProfiles.add(
                JudicialAccessProfile.builder()
                        .appointment("District Judge (MC)")
                        .appointmentType("SPTW")
                        .userId(userId)
                        .roles(List.of("District Judge"))
                        .regionId("LDN")
                        .primaryLocationId("London")
                        .ticketCodes(List.of("ABA5"))
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
                buildExecuteKieSession(getFeatureFlags("privatelaw_wa_1_0", false));

        //assertions
        assertTrue(roleAssignments.isEmpty());
    }
}
