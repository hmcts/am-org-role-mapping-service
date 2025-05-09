package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
class DroolPrivateLawJudicialRoleMappingTest extends DroolBase {

    static final String REGION_ID = "LDN";
    static final String JURISDICTION = "PRIVATELAW";
    static final RoleCategory ROLE_CATEGORY = RoleCategory.JUDICIAL;

    static final String USER_ID = "3168da13-00b3-41e3-81fa-cbc71ac28a69";
    static final String PRIMARY_LOCATION_ID = "London";
    static final String BOOKING_LOCATION_ID = "Scotland";
    static final String BOOKING_REGION_ID = "1";
    static final String SERVICE_CODES = "ABA5";
    static Map<String, String> expectedRoleNameWorkTypesMap = new HashMap<>();

    static {
        expectedRoleNameWorkTypesMap.put("judge", "hearing_work,decision_making_work,applications,routine_work");
        expectedRoleNameWorkTypesMap.put("hmcts-judiciary", null);
        expectedRoleNameWorkTypesMap.put("leadership-judge", null);
        expectedRoleNameWorkTypesMap.put("task-supervisor", "routine_work,hearing_work,applications");
        expectedRoleNameWorkTypesMap.put("case-allocator", null);
        expectedRoleNameWorkTypesMap.put("specific-access-approver-judiciary", "access_requests");
        expectedRoleNameWorkTypesMap.put("circuit-judge", "hearing_work,decision_making_work,applications,"
                + "routine_work");
        expectedRoleNameWorkTypesMap.put("fee-paid-judge", "hearing_work,decision_making_work,applications,"
                + "routine_work");
        expectedRoleNameWorkTypesMap.put("magistrate", "hearing_work,applications,routine_work");
    }

    static void assertCommonRoleAssignmentAttributes(RoleAssignment r, String appointment) {
        assertEquals(ActorIdType.IDAM, r.getActorIdType());
        assertEquals(USER_ID, r.getActorId());
        assertEquals(RoleType.ORGANISATION, r.getRoleType());
        assertEquals(ROLE_CATEGORY, r.getRoleCategory());

        String primaryLocation = null;
        if (r.getAttributes().get("primaryLocation") != null) {
            primaryLocation = r.getAttributes().get("primaryLocation").asText();
        }

        if (List.of("hmcts-judiciary").contains(
                r.getRoleName())) {
            assertEquals(Classification.PRIVATE, r.getClassification());
            assertEquals(GrantType.BASIC, r.getGrantType());
            assertEquals(null, r.getAttributes().get("jurisdiction"));
            assertTrue(r.isReadOnly());
            assertNull(primaryLocation);
            assertNull(r.getAttributes().get("region"));
        } else {
            assertEquals(Classification.PUBLIC, r.getClassification());
            assertEquals(GrantType.STANDARD, r.getGrantType());
            assertEquals(JURISDICTION, r.getAttributes().get("jurisdiction").asText());
            assertFalse(r.isReadOnly());
            assertEquals(SERVICE_CODES, r.getAuthorisations().get(0));

            if (bookingLocationAppointments.contains(appointment)
                    && List.of("circuit-judge", "judge").contains(r.getRoleName())) {
                assertEquals(BOOKING_LOCATION_ID, primaryLocation);
                assertEquals(BOOKING_REGION_ID, r.getAttributes().get("region").asText());
                assertEquals(BOOKING_LOCATION_ID, r.getAttributes().get("baseLocation").asText());
            } else {
                assertEquals(PRIMARY_LOCATION_ID, primaryLocation);
                assertEquals(REGION_ID, r.getAttributes().get("region").asText());
                assertNull(r.getAttributes().get("baseLocation"));
            }

        }

        String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());
        String actualWorkTypes = null;
        if (r.getAttributes().get("workTypes") != null) {
            actualWorkTypes = r.getAttributes().get("workTypes").asText();
        }
        assertEquals(expectedWorkTypes, actualWorkTypes);
    }


    List<String> judgeRoleNamesWithWorkTypes = List.of("judge", "circuit-judge", "fee-paid-judge");
    static List<String> bookingLocationAppointments = List.of(
            "Deputy District Judge- Fee-Paid",
            "Deputy District Judge",
            "Deputy District Judge- Sitting in Retirement",
            "Deputy District Judge (sitting in retirement)",
            "Recorder",
            "Deputy District Judge - PRFD",
            "Deputy District Judge (MC)- Fee paid",
            "Deputy District Judge (MC)- Sitting in Retirement",
            "Deputy High Court Judge",
            "High Court Judge- Sitting in Retirement",
            "Deputy Circuit Judge",
            "District Judge (MC) (sitting in retirement)",
            "District Judge (sitting in retirement)");

    static Stream<Arguments> endToEndData() {
        // Parameters String appointment, String appointmentType, List<String> assignedRoles,
        // List<String> expectedRoleNames boolean privateLawV13IsEnabled
        return Stream.of(
                Arguments.of("Circuit Judge",
                        "Salaried",
                        List.of(""),
                        List.of("judge", "circuit-judge", "hmcts-judiciary")),
                Arguments.of("Circuit Judge",
                        "SPTW",
                        List.of(""),
                        List.of("judge", "circuit-judge", "hmcts-judiciary")),
                Arguments.of("Deputy Circuit Judge",
                        "Fee Paid",
                        List.of(""),
                        List.of("judge","circuit-judge", "fee-paid-judge", "hmcts-judiciary")),
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
                Arguments.of("Deputy District Judge",
                        "Fee Paid",
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary")),
                Arguments.of("Deputy District Judge- Sitting in Retirement",
                        "Fee Paid",
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary")),
                Arguments.of("Deputy District Judge (sitting in retirement)",
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
                        List.of("judge", "circuit-judge", "hmcts-judiciary")),
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
                        List.of("judge", "hmcts-judiciary")),
                Arguments.of("Magistrate",
                        "Voluntary",
                        List.of("Magistrates-Voluntary"),
                        List.of("magistrate")),
                Arguments.of("District Judge (MC) (sitting in retirement)",
                        "Fee Paid",
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary")),
                Arguments.of("District Judge (sitting in retirement)",
                        "Fee Paid",
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary"))
        );
    }

    @ParameterizedTest
    @MethodSource("endToEndData")
    void shouldTakeJudicialAccessProfileConvertToJudicialOfficeHolderThenReturnRoleAssignments(
            String appointment, String appointmentType, List<String> assignedRoles, List<String> expectedRoleNames) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();
        JudicialBooking booking = JudicialBooking.builder().userId(USER_ID).locationId(BOOKING_LOCATION_ID)
                .regionId(BOOKING_REGION_ID).build();
        judicialBookings.add(booking);

        judicialAccessProfiles.add(
                JudicialAccessProfile.builder()
                        .appointment(appointment)
                        .appointmentType(appointmentType)
                        .userId(USER_ID)
                        .roles(assignedRoles)
                        .regionId(REGION_ID)
                        .primaryLocationId(PRIMARY_LOCATION_ID)
                        .ticketCodes(List.of(SERVICE_CODES))
                        .authorisations(List.of(
                                Authorisation.builder()
                                        .serviceCodes(List.of(SERVICE_CODES))
                                        .endDate(LocalDateTime.now().plusYears(1L))
                                        .build()
                        ))
                        .build()
        );

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags(true));

        //assertions
        assertFalse(roleAssignments.isEmpty());

        List<String> roleNameResults =
                roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList());
        assertThat(roleNameResults, containsInAnyOrder(expectedRoleNames.toArray()));

        for (RoleAssignment r : roleAssignments) {
            assertCommonRoleAssignmentAttributes(r,appointment);
        }
    }


    @Test
    void falsePrivateLawFlagTest() {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        judicialAccessProfiles.add(
                JudicialAccessProfile.builder()
                        .appointment("District Judge (MC)")
                        .appointmentType("SPTW")
                        .userId(USER_ID)
                        .roles(List.of("District Judge"))
                        .regionId(REGION_ID)
                        .primaryLocationId(PRIMARY_LOCATION_ID)
                        .ticketCodes(List.of(SERVICE_CODES))
                        .authorisations(List.of(
                                Authorisation.builder()
                                        .serviceCodes(List.of(SERVICE_CODES))
                                        .endDate(LocalDateTime.now().plusYears(1L))
                                        .build()
                        ))
                        .build()
        );

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags(false));

        //assertions
        assertTrue(roleAssignments.isEmpty());
    }

    List<FeatureFlag> getFeatureFlags(Boolean status) {
        return getAllFeatureFlagsToggleByJurisdiction("PRIVATELAW", status);
    }

}
