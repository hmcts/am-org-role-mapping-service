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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(MockitoJUnitRunner.class)
class DroolPublicLawJudicialRoleMappingTest extends DroolBase {

    String userId = "3168da13-00b3-41e3-81fa-cbc71ac28a69";
    List<String> judgeRoleNamesWithWorkTypes = List.of("judge", "task-supervisor", "case-allocator",
            "specific-access-approver-judiciary", "fee-paid-judge");

    static Stream<Arguments> endToEndData() {
        return Stream.of(
                Arguments.of("Circuit Judge",
                        "Salaried",
                        false,
                        List.of(""),
                        List.of("judge", "hmcts-judiciary", "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy Circuit Judge",
                        "Fee Paid",
                        true,
                        List.of("Deputy District Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy Circuit Judge",
                        "Fee Paid",
                        false,
                        List.of("Deputy District Judge"),
                        List.of("fee-paid-judge", "hmcts-judiciary", "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy District Judge - PRFD",
                        "Fee Paid",
                        true,
                        List.of("Deputy District Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy District Judge - PRFD",
                        "Fee Paid",
                        false,
                        List.of("Deputy District Judge"),
                        List.of("fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy District Judge (MC)- Fee paid",
                        "Fee Paid",
                        true,
                        List.of("Deputy District Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy District Judge (MC)- Fee paid",
                        "Fee Paid",
                        false,
                        List.of("Deputy District Judge"),
                        List.of("fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy District Judge (MC)- Sitting in Retirement",
                        "Fee Paid",
                        true,
                        List.of("Deputy District Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy District Judge (MC)- Sitting in Retirement",
                        "Fee Paid",
                        false,
                        List.of("Deputy District Judge"),
                        List.of("fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy District Judge- Fee-Paid",
                        "Fee Paid",
                        true,
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy District Judge- Fee-Paid",
                        "Fee Paid",
                        false,
                        List.of(""),
                        List.of("fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy District Judge- Sitting in Retirement",
                        "Fee Paid",
                        true,
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy District Judge- Sitting in Retirement",
                        "Fee Paid",
                        false,
                        List.of(""),
                        List.of("fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy High Court Judge",
                        "Fee Paid",
                        true,
                        List.of("Deputy High Court Judge"),
                        List.of("judge","fee-paid-judge","hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy High Court Judge",
                        "Fee Paid",
                        false,
                        List.of("Deputy High Court Judge"),
                        List.of("fee-paid-judge","hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("District Judge",
                        "Salaried",
                        true,
                        List.of(""),
                        List.of("judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("District Judge (MC)",
                        "SPTW",
                        true,
                        List.of("District Judge"),
                        List.of("judge","hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("High Court Judge",
                        "Salaried",
                        true,
                        List.of(""),
                        List.of("judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("High Court Judge- Sitting in Retirement",
                        "Fee Paid",
                        true,
                        List.of("High Court Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("High Court Judge- Sitting in Retirement",
                        "Fee Paid",
                        false,
                        List.of("High Court Judge"),
                        List.of("fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Magistrate", "Voluntary",
                        false,
                        List.of("Magistrate - Voluntary"),
                        List.of("magistrate", "hearing-viewer")),
                Arguments.of("Recorder", "Fee Paid",
                        false,
                        List.of("Recorder - Fee Paid"),
                        List.of("fee-paid-judge","hmcts-judiciary",
                                "hearing-viewer", "hearing-manager"))
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
                        .ticketCodes(List.of("ABA3"))
                        .authorisations(List.of(
                                Authorisation.builder()
                                        .serviceCodes(List.of("ABA3"))
                                        .jurisdiction("PUBLICLAW")
                                        .endDate(LocalDateTime.now().plusYears(1L))
                                        .build()
                        ))
                        .build()
        );

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("publiclaw_wa_1_0", true));

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
                assertEquals("ABA3", r.getAuthorisations().get(0));
                assertEquals("London", r.getAttributes().get("primaryLocation").asText());
                if (judgeRoleNamesWithWorkTypes.contains(r.getRoleName())) {
                    assertEquals("hearing_work,decision_making_work,applications",
                            r.getAttributes().get("workTypes").asText());
                    assertEquals("LDN", r.getAttributes().get("region").asText());
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
                assertEquals("ABA3", r.getAuthorisations().get(0));
                assertEquals("LDN", r.getAttributes().get("region").asText());
                assertEquals("London", r.getAttributes().get("primaryLocation").asText());
            }
        });
    }
}
