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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(MockitoJUnitRunner.class)
class DroolPublicLawJudicialRoleMappingTest extends DroolBase {

    private static final String USER_ID = "3168da13-00b3-41e3-81fa-cbc71ac28a69";
    private static final ZonedDateTime BOOKING_BEGIN_TIME = ZonedDateTime.of(
            2024,1,1,1,1,1,0, ZoneId.of("Europe/London"));
    private static final ZonedDateTime BOOKING_END_TIME = ZonedDateTime.of(
            2024,1,2,1,1,1,0, ZoneId.of("Europe/London"));
    private static final String BOOKING_REGION_ID = "1";
    private static final String BOOKING_LOCATION_ID = "Scotland";
    private static final ZonedDateTime ACCESS_PROFILE_BEGIN_TIME = ZonedDateTime.of(
            2024,2,1,1,1,1,0, ZoneId.of("Europe/London"));
    private static final ZonedDateTime ACCESS_PROFILE_END_TIME = ZonedDateTime.of(
            2024,2,2,1,1,1,0, ZoneId.of("Europe/London"));
    private static final String ACCESS_PROFILE_REGION_ID = "LDN";
    private static final String ACCESS_PROFILE_PRIMARY_LOCATION_ID = "London";

    static Map<String, String> expectedRoleNameWorkTypesMap = new HashMap<>();

    {
        expectedRoleNameWorkTypesMap.put("judge", "hearing_work,routine_work,decision_making_work,"
                + "applications");
        expectedRoleNameWorkTypesMap.put("hmcts-judiciary", null);
        expectedRoleNameWorkTypesMap.put("leadership-judge", "hearing_work,decision_making_work,applications,"
                + "access_requests");
        expectedRoleNameWorkTypesMap.put("task-supervisor", "hearing_work,routine_work,decision_making_work,"
                + "applications,access_requests");
        expectedRoleNameWorkTypesMap.put("case-allocator", null);
        expectedRoleNameWorkTypesMap.put("specific-access-approver-judiciary", "access_requests");
        expectedRoleNameWorkTypesMap.put("fee-paid-judge", "hearing_work,routine_work,"
                + "decision_making_work,applications");
        expectedRoleNameWorkTypesMap.put("magistrate", null);
    }

    static Stream<Arguments> endToEndData() {
        return Stream.of(
                Arguments.of("Circuit Judge",
                        "Salaried",
                        false,
                        true,
                        List.of(""),
                        List.of("judge", "hmcts-judiciary", "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy Circuit Judge",
                        "Fee Paid",
                        true,
                        true,
                        List.of("Deputy District Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy Circuit Judge",
                        "Fee Paid",
                        false,
                        true,
                        List.of("Deputy District Judge"),
                        List.of("fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy Circuit Judge",
                        "Fee Paid",
                        false,
                        false,
                        List.of("Deputy District Judge"),
                        List.of("fee-paid-judge", "hmcts-judiciary")),
                Arguments.of("Deputy District Judge - PRFD",
                        "Fee Paid",
                        true,
                        true,
                        List.of("Deputy District Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy District Judge - PRFD",
                        "Fee Paid",
                        false,
                        true,
                        List.of("Deputy District Judge"),
                        List.of("fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy District Judge (MC)- Fee paid",
                        "Fee Paid",
                        true,
                        true,
                        List.of("Deputy District Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy District Judge (MC)- Fee paid",
                        "Fee Paid",
                        false,
                        true,
                        List.of("Deputy District Judge"),
                        List.of("fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy District Judge (MC)- Sitting in Retirement",
                        "Fee Paid",
                        true,
                        true,
                        List.of("Deputy District Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy District Judge (MC)- Sitting in Retirement",
                        "Fee Paid",
                        false,
                        true,
                        List.of("Deputy District Judge"),
                        List.of("fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy District Judge- Fee-Paid",
                        "Fee Paid",
                        true,
                        true,
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy District Judge- Fee-Paid",
                        "Fee Paid",
                        false,
                        true,
                        List.of(""),
                        List.of("fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy District Judge- Sitting in Retirement",
                        "Fee Paid",
                        true,
                        true,
                        List.of(""),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy District Judge- Sitting in Retirement",
                        "Fee Paid",
                        false,
                        true,
                        List.of(""),
                        List.of("fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy High Court Judge",
                        "Fee Paid",
                        true,
                        true,
                        List.of("Deputy High Court Judge"),
                        List.of("judge","fee-paid-judge","hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Deputy High Court Judge",
                        "Fee Paid",
                        false,
                        true,
                        List.of("Deputy High Court Judge"),
                        List.of("fee-paid-judge","hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("District Judge",
                        "Salaried",
                        true,
                        true,
                        List.of(""),
                        List.of("judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("District Judge (MC)",
                        "SPTW",
                        true,
                        true,
                        List.of("District Judge"),
                        List.of("judge","hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("High Court Judge",
                        "Salaried",
                        true,
                        true,
                        List.of(""),
                        List.of("judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("High Court Judge- Sitting in Retirement",
                        "Fee Paid",
                        true,
                        true,
                        List.of("High Court Judge"),
                        List.of("judge", "fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("High Court Judge- Sitting in Retirement",
                        "Fee Paid",
                        false,
                        true,
                        List.of("High Court Judge"),
                        List.of("fee-paid-judge", "hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                // hearing-manager excluded from Magistrate
                Arguments.of("Magistrate", "Voluntary",
                        false,
                        true,
                        List.of("Magistrate - Voluntary"),
                        List.of("magistrate", "hearing-viewer")),
                Arguments.of("Recorder", "Fee Paid",
                        false,
                        true,
                        List.of("Recorder - Fee Paid"),
                        List.of("fee-paid-judge","hmcts-judiciary",
                                "hearing-viewer", "hearing-manager")),
                Arguments.of("Tribunal Judge", "Salaried",
                        false,
                        true,
                        List.of("Tribunal Judge - Salaried"),
                        List.of("judge","hmcts-judiciary","hearing-viewer","hearing-manager")),
                Arguments.of("Employment Judge", "Salaried",
                        false,
                        true,
                        List.of("Employment Judge - Salaried"),
                        List.of("judge","hmcts-judiciary","hearing-viewer","hearing-manager")),
                Arguments.of("Specialist Circuit Judge", "Salaried",
                        false,
                        true,
                        List.of("Specialist Circuit Judge - Salaried"),
                        List.of("judge","hmcts-judiciary","hearing-viewer","hearing-manager")),
                Arguments.of("Senior Circuit Judge", "Salaried",
                        false,
                        true,
                        List.of("Senior Circuit Judge - Salaried"),
                        List.of("judge","hmcts-judiciary","hearing-viewer","hearing-manager")),
                Arguments.of("Circuit Judge (sitting in retirement)", "Fee Paid",
                        true,
                        true,
                        List.of("Circuit Judge - Sitting in Retirement - Fee Paid"),
                        List.of("judge","fee-paid-judge","hmcts-judiciary","hearing-viewer","hearing-manager")),
                //no Judicial Booking, no judge role
                Arguments.of("Circuit Judge (sitting in retirement)", "Fee Paid",
                        false,
                        true,
                        List.of("Circuit Judge - Sitting in Retirement - Fee Paid"),
                        List.of("fee-paid-judge","hmcts-judiciary","hearing-viewer","hearing-manager")),
                Arguments.of("Recorder (sitting in retirement)", "Fee Paid",
                        true,
                        true,
                        List.of("Recorder - Sitting in Retirement - Fee Paid"),
                        List.of("judge","fee-paid-judge","hmcts-judiciary","hearing-viewer","hearing-manager")),
                //no Judicial Booking, no judge role
                Arguments.of("Recorder (sitting in retirement)", "Fee Paid",
                        false,
                        true,
                        List.of("Recorder - Sitting in Retirement - Fee Paid"),
                        List.of("fee-paid-judge","hmcts-judiciary","hearing-viewer","hearing-manager")),
                Arguments.of("Deputy Upper Tribunal Judge", "Fee Paid",
                        true,
                        true,
                        List.of("Deputy Upper Tribunal Judge - Fee Paid"),
                        List.of("judge","fee-paid-judge","hmcts-judiciary","hearing-viewer","hearing-manager")),
                //no Judicial Booking, no judge role
                Arguments.of("Deputy Upper Tribunal Judge", "Fee Paid",
                        false,
                        true,
                        List.of("Deputy Upper Tribunal Judge - Fee Paid"),
                        List.of("fee-paid-judge","hmcts-judiciary","hearing-viewer","hearing-manager")),
                Arguments.of("District Judge (MC)- Sitting in Retirement", "Fee Paid",
                        true,
                        true,
                        List.of("District Judge (MC) - Sitting in Retirement - Fee Paid"),
                        List.of("judge","fee-paid-judge","hmcts-judiciary","hearing-viewer","hearing-manager")),
                //no Judicial Booking, no judge role
                Arguments.of("District Judge (MC)- Sitting in Retirement", "Fee Paid",
                        false,
                        true,
                        List.of("District Judge (MC) - Sitting in Retirement - Fee Paid"),
                        List.of("fee-paid-judge","hmcts-judiciary","hearing-viewer","hearing-manager")),
                Arguments.of("District Judge (sitting in retirement)", "Fee Paid",
                        true,
                        true,
                        List.of("District Judge - Sitting in Retirement - Fee Paid"),
                        List.of("judge","fee-paid-judge","hmcts-judiciary","hearing-viewer","hearing-manager")),
                //no Judicial Booking, no judge role
                Arguments.of("District Judge (sitting in retirement)", "Fee Paid",
                        false,
                        true,
                        List.of("District Judge - Sitting in Retirement - Fee Paid"),
                        List.of("fee-paid-judge","hmcts-judiciary","hearing-viewer","hearing-manager"))
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
                    .userId(USER_ID).locationId(BOOKING_LOCATION_ID).regionId(BOOKING_REGION_ID)
                    .beginTime(BOOKING_BEGIN_TIME)
                    .endTime(BOOKING_END_TIME)
                    .build();
            judicialBookings.add(booking);
        }

        judicialAccessProfiles.add(
                JudicialAccessProfile.builder()
                        .appointment(appointment)
                        .appointmentType(appointmentType)
                        .userId(USER_ID)
                        .roles(assignedRoles)
                        .regionId(ACCESS_PROFILE_REGION_ID)
                        .primaryLocationId(ACCESS_PROFILE_PRIMARY_LOCATION_ID)
                        .ticketCodes(List.of("ABA3"))
                        .beginTime(ACCESS_PROFILE_BEGIN_TIME)
                        .endTime(ACCESS_PROFILE_END_TIME)
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
                buildExecuteKieSession(
                        List.of(FeatureFlag.builder().flagName("publiclaw_wa_1_0").status(true).build(),
                                FeatureFlag.builder().flagName("sscs_hearing_1_0").status(hearingFlag).build(),
                                FeatureFlag.builder().flagName("publiclaw_wa_1_1").status(true).build(),
                                FeatureFlag.builder().flagName("publiclaw_wa_1_2").status(true).build())
                );

        //assertions
        assertFalse(roleAssignments.isEmpty());

        List<String> roleNameResults =
                roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList());
        assertThat(roleNameResults, containsInAnyOrder(expectedRoleNames.toArray()));

        roleAssignments.forEach(r -> {
            assertEquals(ActorIdType.IDAM, r.getActorIdType());
            assertEquals(USER_ID, r.getActorId());
            assertEquals(RoleType.ORGANISATION, r.getRoleType());
            assertEquals(RoleCategory.JUDICIAL, r.getRoleCategory());

            String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());
            String actualWorkTypes = null;
            if (r.getAttributes().get("workTypes") != null) {
                actualWorkTypes = r.getAttributes().get("workTypes").asText();
            }
            assertEquals(expectedWorkTypes, actualWorkTypes);

            assertRoleSpecificAtrributes(r, appointmentType);
        });
    }

    private void assertRoleSpecificAtrributes(RoleAssignment r, String appointmentType) {
        String primaryLocation = null;
        if (r.getAttributes().get("primaryLocation") != null) {
            primaryLocation = r.getAttributes().get("primaryLocation").asText();
        }

        if (r.getRoleName().equals("hmcts-judiciary")) {
            assertEquals(Classification.PRIVATE, r.getClassification());
            assertEquals(GrantType.BASIC, r.getGrantType());
            assertTrue(r.isReadOnly());
            assertNull(r.getAttributes().get("jurisdiction"));
            assertNull(primaryLocation);
            assertEquals(ACCESS_PROFILE_BEGIN_TIME, r.getBeginTime());
            assertEquals(ACCESS_PROFILE_END_TIME.plusDays(1), r.getEndTime());
        } else {
            assertEquals(Classification.PUBLIC, r.getClassification());
            assertEquals(GrantType.STANDARD, r.getGrantType());
            assertEquals("ABA3", r.getAuthorisations().get(0));
            assertEquals("PUBLICLAW", r.getAttributes().get("jurisdiction").asText());
            assertFalse(r.isReadOnly());

            if (r.getRoleName().equals("judge") && appointmentType.equals("Fee Paid")) {
                assertEquals(BOOKING_BEGIN_TIME, r.getBeginTime());
                assertEquals(BOOKING_END_TIME, r.getEndTime());
                assertEquals(BOOKING_REGION_ID, r.getAttributes().get("region").asText());
                assertEquals(BOOKING_LOCATION_ID, primaryLocation);
            } else {
                assertEquals(ACCESS_PROFILE_BEGIN_TIME, r.getBeginTime());
                assertEquals(ACCESS_PROFILE_END_TIME.plusDays(1), r.getEndTime());
                assertEquals(ACCESS_PROFILE_PRIMARY_LOCATION_ID, primaryLocation);
                if (!r.getRoleName().equals("hearing-viewer")
                        && !r.getRoleName().equals("hearing-manager")) {
                    assertEquals(ACCESS_PROFILE_REGION_ID, r.getAttributes().get("region").asText());
                }
            }

        }
    }


    @Test
    void falsePublicLawFlagTest() {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        judicialAccessProfiles.add(
                JudicialAccessProfile.builder()
                        .appointment("District Judge (MC)")
                        .appointmentType("SPTW")
                        .userId(USER_ID)
                        .roles(List.of("District Judge"))
                        .regionId(ACCESS_PROFILE_REGION_ID)
                        .primaryLocationId(ACCESS_PROFILE_PRIMARY_LOCATION_ID)
                        .ticketCodes(List.of("ABA3"))
                        .authorisations(List.of(
                                Authorisation.builder()
                                        .serviceCodes(List.of("ABA3"))
                                        .endDate(LocalDateTime.now().plusYears(1L))
                                        .build()
                        ))
                        .build()
        );

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("publiclaw_wa_1_0", false));

        //assertions
        assertTrue(roleAssignments.isEmpty());
    }
}
