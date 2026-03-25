package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.Authorisation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Jurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.jrd.AdditionalRole;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.jrd.AdditionalRoleEnum;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.jrd.Appointment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.jrd.AppointmentEnum;
import uk.gov.hmcts.reform.orgrolemapping.helper.RoleAssignmentAssertHelper.MultiRegion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.RoleName.CASE_ALLOCATOR;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.RoleName.CIRCUIT_JUDGE;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.RoleName.JUDGE;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.RoleName.LEADERSHIP_JUDGE;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.RoleName.SPECIFIC_ACCESS_APPROVER_JUDICIARY;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.RoleName.SPECIFIC_ACCESS_APPROVER_LEGAL_OPS;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.RoleName.TASK_SUPERVISOR;

@ExtendWith(MockitoExtension.class)
class DroolPossessionsJudicialOfficeHolderMappingTest extends DroolBase {

    private static final String USER_ID = "3168da13-00b3-41e3-81fa-cbc71ac28a69";

    private static final ZonedDateTime BOOKING_BEGIN_TIME = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1);
    private static final ZonedDateTime BOOKING_END_TIME = ZonedDateTime.now(ZoneOffset.UTC).plusDays(1);
    private static final String ACCESS_PROFILE_PRIMARY_LOCATION_ID = "London";
    private static final ZonedDateTime ACCESS_PROFILE_BEGIN_TIME = ZonedDateTime.now(ZoneOffset.UTC).minusMonths(1);
    private static final ZonedDateTime ACCESS_PROFILE_END_TIME = ZonedDateTime.now(ZoneOffset.UTC).plusMonths(1);
    private static final String BOOKING_REGION_ID = "1";
    private static final String BOOKING_LOCATION_ID = "Scotland";
    private static final String ACCESS_PROFILE_REGION_ID = "LDN";

    // NB: multi-regions are: London and South-East
    static List<String> multiRegionList = List.of("1", "5");

    static Map<String, String> expectedRoleNameWorkTypesMap = new HashMap<>();

    static {
        expectedRoleNameWorkTypesMap.put("judge", "hearing_work,routine_work,decision_making_work,"
                + "applications");
        expectedRoleNameWorkTypesMap.put("hmcts-Judiciary", null);
        expectedRoleNameWorkTypesMap.put("Fee Paid-judge", null);
        expectedRoleNameWorkTypesMap.put("Judge", "hearing_work,routine_work,decision_making_work,"
                + "applications");
        expectedRoleNameWorkTypesMap.put("leadership-judge", null);
        expectedRoleNameWorkTypesMap.put("task-supervisor", null);
        expectedRoleNameWorkTypesMap.put("case-allocator", null);
        expectedRoleNameWorkTypesMap.put("specific-access-approver-judiciary", "access_requests");
        expectedRoleNameWorkTypesMap.put("specific-access-approver-legal-ops", "access_requests");
        expectedRoleNameWorkTypesMap.put("circuit-judge", "appeals");
    }

    static Stream<Arguments> circuitJudgeSalaried() {
        // Parameters AppointmentEnum appointment, AdditionalRoleEnum assignedRoles
        return Stream.of(
                // Appointments
                Arguments.of(Appointment.CIRCUIT_JUDGE,
                        LegacyAdditionalRole.ANY_OTHER_ROLE),
                Arguments.of(Appointment.CIRCUIT_JUDGE_CENTRAL_CRIMINAL_COURT,
                        LegacyAdditionalRole.ANY_OTHER_ROLE),
                Arguments.of(Appointment.COURT_OF_APPEAL_JUDGE,
                        LegacyAdditionalRole.ANY_OTHER_ROLE),
                Arguments.of(Appointment.HIGH_COURT_JUDGE,
                        LegacyAdditionalRole.ANY_OTHER_ROLE),
                Arguments.of(Appointment.SENIOR_CIRCUIT_JUDGE,
                        LegacyAdditionalRole.ANY_OTHER_ROLE),
                Arguments.of(Appointment.SPECIALIST_CIRCUIT_JUDGE,
                        LegacyAdditionalRole.ANY_OTHER_ROLE),
                // Additional Roles
                Arguments.of(LegacyAppointment.ANY_OTHER_APPOINTMENT,
                        AdditionalRole.PRESIDING_JUDGE)
        );
    }

    static Stream<Arguments> leadershipJudgeSalaried() {
        // Parameters AppointmentEnum appointment, AdditionalRoleEnum assignedRoles
        return Stream.of(
                // Appointments
                Arguments.of(Appointment.CHIEF_INSOLVENCY_AND_COMPANIES_COURT_JUDGE,
                        LegacyAdditionalRole.ANY_OTHER_ROLE),
                Arguments.of(Appointment.CHIEF_MASTER,
                        LegacyAdditionalRole.ANY_OTHER_ROLE),
                Arguments.of(Appointment.DEPUTY_CHAMBER_PRESIDENT,
                        LegacyAdditionalRole.ANY_OTHER_ROLE),
                Arguments.of(Appointment.MASTER_OF_THE_ROLLS,
                        LegacyAdditionalRole.ANY_OTHER_ROLE),
                Arguments.of(Appointment.REGIONAL_EMPLOYMENT_JUDGE,
                        LegacyAdditionalRole.ANY_OTHER_ROLE),
                Arguments.of(Appointment.SENIOR_MASTER,
                        LegacyAdditionalRole.ANY_OTHER_ROLE),
                // Additional Roles
                Arguments.of(LegacyAppointment.ANY_OTHER_APPOINTMENT,
                        AdditionalRole.DESIGNATED_CIVIL_JUDGE),
                Arguments.of(LegacyAppointment.ANY_OTHER_APPOINTMENT,
                        AdditionalRole.ACTING_DESIGNATED_CIVIL_JUDGE)
        );
    }

    static Stream<Arguments> salaried() {
        // Parameters AppointmentEnum appointment, AdditionalRoleEnum assignedRoles
        return Stream.of(
                // Appointments
                Arguments.of(Appointment.CHIEF_INSOLVENCY_AND_COMPANIES_COURT_JUDGE,
                        LegacyAdditionalRole.ANY_OTHER_ROLE),
                Arguments.of(Appointment.CHIEF_MASTER,
                        LegacyAdditionalRole.ANY_OTHER_ROLE),
                Arguments.of(Appointment.DEPUTY_CHAMBER_PRESIDENT,
                        LegacyAdditionalRole.ANY_OTHER_ROLE),
                Arguments.of(Appointment.MASTER_OF_THE_ROLLS,
                        LegacyAdditionalRole.ANY_OTHER_ROLE),
                Arguments.of(Appointment.REGIONAL_EMPLOYMENT_JUDGE,
                        LegacyAdditionalRole.ANY_OTHER_ROLE),
                Arguments.of(Appointment.SENIOR_MASTER,
                        LegacyAdditionalRole.ANY_OTHER_ROLE),
                // Additional Roles
                Arguments.of(LegacyAppointment.ANY_OTHER_APPOINTMENT,
                        AdditionalRole.DESIGNATED_CIVIL_JUDGE),
                Arguments.of(LegacyAppointment.ANY_OTHER_APPOINTMENT,
                        AdditionalRole.ACTING_DESIGNATED_CIVIL_JUDGE)
        );
    }

    @ParameterizedTest
    @MethodSource("circuitJudgeSalaried")
    void verifyCircuitJudgeSalariedAndSptwRoles(AppointmentEnum appointment, AdditionalRoleEnum assignedRoles) {
        String expectedRoleNames = "hmcts-judiciary,judge,circuit-judge";
        runSalariedTestsForSalariedAndSptw(appointment, assignedRoles, expectedRoleNames, "1", true);
        runSalariedTestsForSalariedAndSptw(appointment, assignedRoles, expectedRoleNames, "5", true);
        runSalariedTestsForSalariedAndSptw(appointment, assignedRoles, expectedRoleNames, "2", false);
    }

    @ParameterizedTest
    @EnumSource(value = Appointment.class, names = {"CIRCUIT_JUDGE", "CIRCUIT_JUDGE_SITTING_IN_RETIREMENT",
        "DEPUTY_CIRCUIT_JUDGE" })
    void verifyCircuitJudgeFeePaidRoles(AppointmentEnum appointment) {
        String expectedRoleNamesWithBooking = "hmcts-judiciary,fee-paid-judge,judge,circuit-judge";
        String expectedRoleNamesWithOutBooking = "hmcts-judiciary,fee-paid-judge";
        shouldReturnFeePaidRolesFromJudicialAccessProfile(appointment, true, expectedRoleNamesWithBooking);
        shouldReturnFeePaidRolesFromJudicialAccessProfile(appointment, false, expectedRoleNamesWithOutBooking);
    }

    @ParameterizedTest
    @MethodSource("leadershipJudgeSalaried")
    void verifyLeadershipJudgeSalariedAndSptwRoles(AppointmentEnum appointment, AdditionalRoleEnum assignedRoles) {
        String expectedRoleNames = "leadership-judge,judge,task-supervisor,hmcts-judiciary,case-allocator,"
                + "specific-access-approver-judiciary,specific-access-approver-legal-ops";
        runSalariedTestsForSalariedAndSptw(appointment, assignedRoles, expectedRoleNames, "1", true);
        runSalariedTestsForSalariedAndSptw(appointment, assignedRoles, expectedRoleNames, "5", true);
        runSalariedTestsForSalariedAndSptw(appointment, assignedRoles, expectedRoleNames, "2", false);
    }

    @ParameterizedTest
    @EnumSource(value = Appointment.class, names = {"VICE_PRESIDENT"})
    @EnumSource(value = LegacyAppointment.class, names = {
        "ADJUDICATOR",
        "DEPUTY_COSTS_JUDGE",
        "DEPUTY_DISTRICT_JUDGE",
        "DEPUTY_DISTRICT_JUDGE_PRFD",
        "DEPUTY_DISTRICT_JUDGE_SITTING_IN_RETIREMENT",
        "DEPUTY_MASTER",
        "DISTRICT_JUDGE",
        "DEPUTY_INSOLVENCY_AND_COMPANIES_COURT_JUDGE",
        "DISTRICT_JUDGE_SITTING_IN_RETIREMENT",
        "EMPLOYMENT_JUDGE",
        "HIGH_COURT_JUDGE_SITTING_IN_RETIREMENT",
        "INSOLVENCY_AND_COMPANIES_COURT_JUDGE_SITTING_IN_RETIREMENT",
        "RECORDER",
        "TRIBUNAL_JUDGE"
    })
    void verifyGenericFeePaidRoles(AppointmentEnum appointment) {
        String expectedRoleNamesWithBooking = "hmcts-judiciary,fee-paid-judge,judge";
        String expectedRoleNamesWithOutBooking = "hmcts-judiciary,fee-paid-judge";
        shouldReturnFeePaidRolesFromJudicialAccessProfile(appointment, true, expectedRoleNamesWithBooking);
        shouldReturnFeePaidRolesFromJudicialAccessProfile(appointment, false, expectedRoleNamesWithOutBooking);
    }

    @ParameterizedTest
    @EnumSource(value = Appointment.class, names = {"VICE_PRESIDENT"})
    @EnumSource(value = LegacyAppointment.class, names = {
        "COSTS_JUDGE",
        "DISTRICT_JUDGE_MC",
        "EMPLOYMENT_JUDGE",
        "INSOLVENCY_AND_COMPANIES_COURT_JUDGE",
        "JUDGE_OF_THE_FIRST_TIER_TRIBUNAL",
        "REGISTRAR_OF_CRIMINAL_APPEALS",
        "SENIOR_COSTS_JUDGE",
        "TRIBUNAL_JUDGE"
    })
    void verifyGenericSalariedAndSptwRoles(AppointmentEnum appointment) {
        String expectedRoleNames = "hmcts-judiciary,judge";
        runSalariedTestsForSalariedAndSptw(
                appointment, LegacyAdditionalRole.ANY_OTHER_ROLE, expectedRoleNames, "1", true);
        runSalariedTestsForSalariedAndSptw(
                appointment, LegacyAdditionalRole.ANY_OTHER_ROLE, expectedRoleNames, "5", true);
        runSalariedTestsForSalariedAndSptw(
                appointment, LegacyAdditionalRole.ANY_OTHER_ROLE, expectedRoleNames, "2", false);
    }

    void shouldReturnSalariedRolesFromJudicialAccessProfile(
            AppointmentEnum appointment, String appointmentType, AdditionalRoleEnum assignedRole,
            String expectedRoleNames, String region, boolean expectMultiRegion) {

        clearAndPrepareProfilesForDroolSession(
                appointment,
                appointmentType,
                assignedRole,
                region,
                false
        );

        // create map for all salaried roleNames that need regions
        List<String> rolesThatRequireRegions = List.of(
                JUDGE,
                LEADERSHIP_JUDGE,
                TASK_SUPERVISOR,
                CASE_ALLOCATOR,
                SPECIFIC_ACCESS_APPROVER_JUDICIARY,
                CIRCUIT_JUDGE,
                SPECIFIC_ACCESS_APPROVER_LEGAL_OPS
        );

        Map<String, List<String>> roleNameToRegionsMap = MultiRegion.buildRoleNameToRegionsMap(rolesThatRequireRegions);

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(setFeatureFlags());

        //assertions
        List<String> expectedRoleList = Arrays.stream(expectedRoleNames.split(",")).toList();
        MultiRegion.assertRoleAssignmentCount(
                roleAssignments,
                expectedRoleList,
                expectMultiRegion,
                rolesThatRequireRegions,
                multiRegionList
        );

        roleAssignments.forEach(r -> {
            if (r.getAttributes().get("contractType") != null) {
                assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            }

            assertRoleSpecificAttributes(r, "Salaried", roleNameToRegionsMap);
        });

        // verify regions add to map
        MultiRegion.assertRoleNameToRegionsMapIsAsExpected(
                roleNameToRegionsMap,
                expectedRoleList,
                expectMultiRegion,
                multiRegionList,
                region, // fallback if not multi-region scenario
                null // i.e. no bookings
        );
    }

    void shouldReturnFeePaidRolesFromJudicialAccessProfile(
            AppointmentEnum appointment, boolean addBooking,  String expectedRoleNames) {

        clearAndPrepareProfilesForDroolSession(
                appointment,
                "Fee Paid",
                LegacyAdditionalRole.ANY_OTHER_ROLE,
                ACCESS_PROFILE_REGION_ID,
                addBooking
        );

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(setFeatureFlags());

        //assertions
        assertFalse(roleAssignments.isEmpty());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).toList(),
                containsInAnyOrder(expectedRoleNames.split(",")));
        assertEquals(expectedRoleNames.split(",").length, roleAssignments.size());

        roleAssignments.forEach(r -> {
            if (r.getAttributes().get("contractType") != null) {
                assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            }

            assertRoleSpecificAttributes(r, "Fee Paid", null);
        });
    }

    private void clearAndPrepareProfilesForDroolSession(AppointmentEnum appointment, String appointmentType,
                                                        AdditionalRoleEnum role, String region, boolean addBooking) {
        allProfiles.clear();
        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();
        judicialBookings.clear();

        judicialAccessProfiles.add(
                JudicialAccessProfile.builder()
                        .appointment(appointment.getName())
                        .appointmentCode(appointment.getCodes().get(0))
                        .appointmentType(appointmentType)
                        .userId(USER_ID)
                        .roles(List.of(role.getName()))
                        .regionId(region)
                        .primaryLocationId(ACCESS_PROFILE_PRIMARY_LOCATION_ID)
                        .ticketCodes(Jurisdiction.POSSESSIONS.getServiceCodes())
                        .beginTime(ACCESS_PROFILE_BEGIN_TIME)
                        .endTime(ACCESS_PROFILE_END_TIME)
                        .additionalRoles(List.of(RoleV2.builder()
                                .jurisdictionRoleName(role.getName())
                                .jurisdictionRoleId(role.getCodes().get(0)) // any role code will do in this test
                                .startDate(LocalDate.now().minusDays(20L))
                                .endDate(LocalDate.now().plusDays(20L)) // i.e. valid end date
                                .build()))
                        .authorisations(List.of(
                                Authorisation.builder()
                                        .serviceCodes(Jurisdiction.POSSESSIONS.getServiceCodes())
                                        .jurisdiction("PCS")
                                        .endDate(LocalDateTime.now().plusYears(1L))
                                        .build()
                        ))
                        .build()
        );

        if (addBooking) {
            judicialBookings.add(
                    JudicialBooking.builder()
                            .userId(USER_ID).locationId(BOOKING_LOCATION_ID)
                            .regionId(BOOKING_REGION_ID)
                            .beginTime(BOOKING_BEGIN_TIME)
                            .endTime(BOOKING_END_TIME)
                            .build()
            );
        }
    }

    private void assertRoleSpecificAttributes(RoleAssignment r, String appointmentType,
                                              Map<String, List<String>> roleNameToRegionsMap) {
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

        String primaryLocation = null;
        if (r.getAttributes().get("primaryLocation") != null) {
            primaryLocation = r.getAttributes().get("primaryLocation").asText();
        }

        if (roleNameToRegionsMap != null) {
            // check region status and add to map
            MultiRegion.assertRegionStatusAndUpdateRoleToRegionMap(r, roleNameToRegionsMap);
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
            assertEquals("AAA3", r.getAuthorisations().get(0));
            assertEquals("PCS", r.getAttributes().get("jurisdiction").asText());
            assertFalse(r.isReadOnly());

            if ((r.getRoleName().equals("judge") || r.getRoleName().equals("circuit-judge"))
                    && appointmentType.equals("Fee Paid")) {
                assertEquals(BOOKING_BEGIN_TIME, r.getBeginTime());
                assertEquals(BOOKING_END_TIME, r.getEndTime());
                assertEquals(BOOKING_REGION_ID, r.getAttributes().get("region").asText());
                assertEquals(BOOKING_LOCATION_ID, primaryLocation);
            } else if (r.getRoleName().equals("fee-paid-judge")) {
                assertEquals(ACCESS_PROFILE_BEGIN_TIME, r.getBeginTime());
                assertEquals(ACCESS_PROFILE_END_TIME.plusDays(1), r.getEndTime());
                assertEquals(ACCESS_PROFILE_PRIMARY_LOCATION_ID, primaryLocation);
                assertNull(r.getAttributes().get("region"));
            } else {
                assertEquals(ACCESS_PROFILE_BEGIN_TIME, r.getBeginTime());
                assertEquals(ACCESS_PROFILE_END_TIME.plusDays(1), r.getEndTime());
                assertEquals(ACCESS_PROFILE_PRIMARY_LOCATION_ID, primaryLocation);
            }

        }
    }

    private List<FeatureFlag> setFeatureFlags() {
        List<FeatureFlag> featureFlags = new ArrayList<>(getAllFeatureFlagsToggleByJurisdiction("POSSESSIONS", true));

        for (FeatureFlag flag : featureFlags) {
            if (flag.getFlagName().contains("hearing")) {
                flag.setStatus(true);
            }
        }

        return featureFlags;
    }

    private void runSalariedTestsForSalariedAndSptw(AppointmentEnum appointment, AdditionalRoleEnum assignedRoles,
                                                    String expectedRoleNames, String region,
                                                    boolean expectMultiRegion) {
        shouldReturnSalariedRolesFromJudicialAccessProfile(
                appointment, "Salaried", assignedRoles, expectedRoleNames, region, expectMultiRegion);
        shouldReturnSalariedRolesFromJudicialAccessProfile(
                appointment, "SPTW", assignedRoles, expectedRoleNames, region, expectMultiRegion);
    }
}
