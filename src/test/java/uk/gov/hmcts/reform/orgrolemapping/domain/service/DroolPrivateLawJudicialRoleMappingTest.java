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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.Attributes;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.RoleName;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DroolPrivateLawJudicialRoleMappingTest extends DroolBase {

    static final String JURISDICTION = "PRIVATELAW";
    static final String SERVICE_CODE_PRL = "ABA5";

    static final String USER_ID = "3168da13-00b3-41e3-81fa-cbc71ac28a69";
    static final String ACCESS_PROFILE_PRIMARY_LOCATION_ID = "London";
    static final ZonedDateTime ACCESS_PROFILE_BEGIN_TIME = ZonedDateTime.now(ZoneOffset.UTC).minusMonths(1);
    static final ZonedDateTime ACCESS_PROFILE_END_TIME = ZonedDateTime.now(ZoneOffset.UTC).plusMonths(1);

    static final String BOOKING_LOCATION_ID = "Scotland";
    static final String BOOKING_REGION_ID = "1";
    static final ZonedDateTime BOOKING_BEGIN_TIME = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1);
    static final ZonedDateTime BOOKING_END_TIME = ZonedDateTime.now(ZoneOffset.UTC).plusDays(1);

    // NB: multi-regions are: London and South-East
    static List<String> multiRegionList = List.of("1", "5");

    static Map<String, String> expectedRoleNameWorkTypesMap = new HashMap<>();

    static {
        expectedRoleNameWorkTypesMap.put(RoleName.JUDGE, "hearing_work,decision_making_work,applications,routine_work");
        expectedRoleNameWorkTypesMap.put(RoleName.HMCTS_JUDICIARY, null);
        expectedRoleNameWorkTypesMap.put(RoleName.LEADERSHIP_JUDGE, null);
        expectedRoleNameWorkTypesMap.put(RoleName.TASK_SUPERVISOR, "routine_work,hearing_work,applications");
        expectedRoleNameWorkTypesMap.put(RoleName.CASE_ALLOCATOR, null);
        expectedRoleNameWorkTypesMap.put(RoleName.SPECIFIC_ACCESS_APPROVER_JUDICIARY, "access_requests");
        expectedRoleNameWorkTypesMap.put(RoleName.CIRCUIT_JUDGE,
            "hearing_work,decision_making_work,applications,routine_work");
        expectedRoleNameWorkTypesMap.put(RoleName.FEE_PAID_JUDGE,
            "hearing_work,decision_making_work,applications,routine_work");
        expectedRoleNameWorkTypesMap.put(RoleName.MAGISTRATE, "hearing_work,applications,routine_work");
        expectedRoleNameWorkTypesMap.put(RoleName.FL401_JUDGE,
            "hearing_work,decision_making_work,applications,routine_work");
    }


    static void assertCommonRoleAssignmentAttributes(RoleAssignment r,
                                                     String appointmentType,
                                                     String serviceCode,
                                                     Map<String, List<String>> roleNameToRegionsMap) {
        assertEquals(ActorIdType.IDAM, r.getActorIdType());
        assertEquals(USER_ID, r.getActorId());
        assertEquals(RoleType.ORGANISATION, r.getRoleType());
        assertEquals(RoleCategory.JUDICIAL, r.getRoleCategory());

        String primaryLocation = null;
        if (r.getAttributes().get(Attributes.Name.PRIMARY_LOCATION) != null) {
            primaryLocation = r.getAttributes().get(Attributes.Name.PRIMARY_LOCATION).asText();
        }

        if (roleNameToRegionsMap != null) {
            // check region status and add to map
            MultiRegion.assertRegionStatusAndUpdateRoleToRegionMap(r, roleNameToRegionsMap);
        }

        if (r.getRoleName().equals(RoleName.HMCTS_JUDICIARY)) {
            assertEquals(Classification.PRIVATE, r.getClassification());
            assertEquals(GrantType.BASIC, r.getGrantType());
            assertNull(r.getAttributes().get(Attributes.Name.JURISDICTION));
            assertTrue(r.isReadOnly());
            assertNull(primaryLocation);
            assertEquals(ACCESS_PROFILE_BEGIN_TIME, r.getBeginTime());
            assertEquals(ACCESS_PROFILE_END_TIME.plusDays(1), r.getEndTime());
            assertNull(r.getAttributes().get(Attributes.Name.REGION));
            assertNull(r.getAttributes().get(Attributes.Name.BASE_LOCATION));
        } else {
            assertEquals(Classification.PUBLIC, r.getClassification());
            assertEquals(GrantType.STANDARD, r.getGrantType());
            assertEquals(JURISDICTION, r.getAttributes().get(Attributes.Name.JURISDICTION).asText());
            assertFalse(r.isReadOnly());
            assertEquals(serviceCode, r.getAuthorisations().get(0));

            if ((r.getRoleName().equals(RoleName.JUDGE)
                || r.getRoleName().equals(RoleName.CIRCUIT_JUDGE)
                || r.getRoleName().equals(RoleName.FL401_JUDGE))
                && appointmentType.equals(AppointmentType.FEE_PAID)) {
                assertEquals(BOOKING_BEGIN_TIME, r.getBeginTime());
                assertEquals(BOOKING_END_TIME, r.getEndTime());
                assertEquals(BOOKING_LOCATION_ID, primaryLocation);
                assertEquals(BOOKING_REGION_ID, r.getAttributes().get(Attributes.Name.REGION).asText());
                assertEquals(BOOKING_LOCATION_ID, r.getAttributes().get(Attributes.Name.BASE_LOCATION).asText());
            } else if (r.getRoleName().equals(RoleName.FEE_PAID_JUDGE)) {
                assertEquals(ACCESS_PROFILE_BEGIN_TIME, r.getBeginTime());
                assertEquals(ACCESS_PROFILE_END_TIME.plusDays(1), r.getEndTime());
                assertEquals(ACCESS_PROFILE_PRIMARY_LOCATION_ID, primaryLocation);
                assertNull(r.getAttributes().get(Attributes.Name.BASE_LOCATION));
                assertNull(r.getAttributes().get(Attributes.Name.REGION));
            } else {
                assertEquals(ACCESS_PROFILE_BEGIN_TIME, r.getBeginTime());
                assertEquals(ACCESS_PROFILE_END_TIME.plusDays(1), r.getEndTime());
                assertEquals(ACCESS_PROFILE_PRIMARY_LOCATION_ID, primaryLocation);
                assertNull(r.getAttributes().get(Attributes.Name.BASE_LOCATION));
            }
        }

        String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());
        String actualWorkTypes = null;
        if (r.getAttributes().get(Attributes.Name.WORK_TYPES) != null) {
            actualWorkTypes = r.getAttributes().get(Attributes.Name.WORK_TYPES).asText();
        }
        assertEquals(expectedWorkTypes, actualWorkTypes);
    }


    static Stream<Arguments> genericJudgeSalaried() {
        // Parameters AppointmentEnum appointment, AdditionalRoleEnum assignedRoles
        return Stream.of(
            // Appointments
            Arguments.of(LegacyAppointment.DISTRICT_JUDGE,
                LegacyAdditionalRole.ANY_OTHER_ROLE),
            Arguments.of(LegacyAppointment.DISTRICT_JUDGE_MC,
                LegacyAdditionalRole.ANY_OTHER_ROLE),
            // Additional Roles
            Arguments.of(LegacyAppointment.ANY_OTHER_APPOINTMENT,
                LegacyAdditionalRole.FAMILY_DIVISION_LIAISON_JUDGE),
            Arguments.of(LegacyAppointment.ANY_OTHER_APPOINTMENT,
                LegacyAdditionalRole.SENIOR_FAMILY_LIAISON_JUDGE)
        );
    }

    static Stream<Arguments> leadershipJudgeSalaried() {
        // Parameters AppointmentEnum appointment, AdditionalRoleEnum assignedRoles
        return Stream.of(
            // Appointments
            // :: NONE
            // Additional Roles
            Arguments.of(LegacyAppointment.ANY_OTHER_APPOINTMENT,
                AdditionalRole.DESIGNATED_FAMILY_JUDGE)
        );
    }


    @ParameterizedTest
    @EnumSource(value = Appointment.class, names = {
        "CIRCUIT_JUDGE",
        "HIGH_COURT_JUDGE",
    })
    void verifyCircuitJudgeSalariedAndSptwRoles(AppointmentEnum appointment) {
        List<String> expectedRoleNames = List.of(RoleName.JUDGE, RoleName.CIRCUIT_JUDGE, RoleName.HMCTS_JUDICIARY);
        AdditionalRoleEnum assignedRoles = LegacyAdditionalRole.ANY_OTHER_ROLE;
        runSalariedTestsForSalariedAndSptw(appointment, assignedRoles, SERVICE_CODE_PRL, expectedRoleNames, "1", false);
        runSalariedTestsForSalariedAndSptw(appointment, assignedRoles, SERVICE_CODE_PRL, expectedRoleNames, "5", false);
        runSalariedTestsForSalariedAndSptw(appointment, assignedRoles, SERVICE_CODE_PRL, expectedRoleNames, "2", false);
    }


    @ParameterizedTest
    @EnumSource(value = Appointment.class, names = {
        "DEPUTY_CIRCUIT_JUDGE",
        "CIRCUIT_JUDGE_SITTING_IN_RETIREMENT",
    })
    void verifyCircuitJudgeFeePaidRoles(AppointmentEnum appointment) {
        List<String> expectedRoleNamesWithBooking = List.of(
            RoleName.JUDGE, RoleName.CIRCUIT_JUDGE, RoleName.FEE_PAID_JUDGE, RoleName.HMCTS_JUDICIARY
        );
        List<String> expectedRoleNamesWithOutBooking = List.of(
            RoleName.FEE_PAID_JUDGE, RoleName.HMCTS_JUDICIARY
        );
        shouldReturnFeePaidRolesFromJudicialAccessProfile(
            appointment, SERVICE_CODE_PRL, expectedRoleNamesWithBooking, true
        );
        shouldReturnFeePaidRolesFromJudicialAccessProfile(
            appointment, SERVICE_CODE_PRL, expectedRoleNamesWithOutBooking, false
        );
    }


    @ParameterizedTest
    @MethodSource("leadershipJudgeSalaried")
    void verifyLeadershipJudgeSalariedAndSptwRoles(AppointmentEnum appointment, AdditionalRoleEnum assignedRoles) {
        List<String> expectedRoleNames = List.of(
            RoleName.LEADERSHIP_JUDGE,
            RoleName.JUDGE,
            RoleName.TASK_SUPERVISOR,
            RoleName.HMCTS_JUDICIARY,
            RoleName.SPECIFIC_ACCESS_APPROVER_JUDICIARY,
            RoleName.CASE_ALLOCATOR
        );
        runSalariedTestsForSalariedAndSptw(appointment, assignedRoles, SERVICE_CODE_PRL, expectedRoleNames, "1", false);
        runSalariedTestsForSalariedAndSptw(appointment, assignedRoles, SERVICE_CODE_PRL, expectedRoleNames, "5", false);
        runSalariedTestsForSalariedAndSptw(appointment, assignedRoles, SERVICE_CODE_PRL, expectedRoleNames, "2", false);
    }


    @ParameterizedTest
    @EnumSource(value = LegacyAppointment.class, names = {
        "DEPUTY_DISTRICT_JUDGE_PRFD",
        "DEPUTY_DISTRICT_JUDGE_MC",
        "DEPUTY_DISTRICT_JUDGE_MC_SITTING_IN_RETIREMENT",
        "DEPUTY_DISTRICT_JUDGE_FEE_PAID",
        "DEPUTY_DISTRICT_JUDGE",
        "DEPUTY_DISTRICT_JUDGE_SITTING_IN_RETIREMENT",
        "DEPUTY_DISTRICT_JUDGE_SITTING_IN_RETIREMENT2",
        "DEPUTY_HIGH_COURT_JUDGE",
        "DISTRICT_JUDGE_MC_SITTING_IN_RETIREMENT",
        "DISTRICT_JUDGE_SITTING_IN_RETIREMENT",
        "HIGH_COURT_JUDGE_SITTING_IN_RETIREMENT2",
        "RECORDER",
    })
    void verifyGenericFeePaidRoles(AppointmentEnum appointment) {
        List<String> expectedRoleNamesWithBooking = List.of(
            RoleName.JUDGE, RoleName.FEE_PAID_JUDGE, RoleName.HMCTS_JUDICIARY
        );
        List<String> expectedRoleNamesWithOutBooking = List.of(
            RoleName.FEE_PAID_JUDGE, RoleName.HMCTS_JUDICIARY
        );
        shouldReturnFeePaidRolesFromJudicialAccessProfile(
            appointment, SERVICE_CODE_PRL, expectedRoleNamesWithBooking, true
        );
        shouldReturnFeePaidRolesFromJudicialAccessProfile(
            appointment, SERVICE_CODE_PRL, expectedRoleNamesWithOutBooking, false
        );
    }


    @ParameterizedTest
    @MethodSource("genericJudgeSalaried")
    void verifyGenericSalariedAndSptwRoles(AppointmentEnum appointment, AdditionalRoleEnum assignedRoles) {
        List<String> expectedRoleNames = List.of(RoleName.JUDGE, RoleName.HMCTS_JUDICIARY);
        runSalariedTestsForSalariedAndSptw(appointment, assignedRoles, SERVICE_CODE_PRL, expectedRoleNames, "1", false);
        runSalariedTestsForSalariedAndSptw(appointment, assignedRoles, SERVICE_CODE_PRL, expectedRoleNames, "5", false);
        runSalariedTestsForSalariedAndSptw(appointment, assignedRoles, SERVICE_CODE_PRL, expectedRoleNames, "2", false);
    }


    @ParameterizedTest
    @EnumSource(value = Appointment.class, names = {
        "MAGISTRATE",
    })
    void verifyMagistrateRoles(AppointmentEnum appointment) {
        List<String> expectedRoleNamesWithBooking = List.of(RoleName.MAGISTRATE);
        shouldReturnMagistrateRolesFromJudicialAccessProfile(appointment, expectedRoleNamesWithBooking);
    }


    @ParameterizedTest
    @EnumSource(value = LegacyAppointment.class, names = {
        "DEPUTY_DISTRICT_JUDGE_FEE_PAID",
        "DEPUTY_DISTRICT_JUDGE",
        "DEPUTY_DISTRICT_JUDGE_SITTING_IN_RETIREMENT",
        "DEPUTY_DISTRICT_JUDGE_SITTING_IN_RETIREMENT2",
        "RECORDER",
    })
    void verifyCivilJudgeFeePaidRoles(AppointmentEnum appointment) {
        List<String> expectedRoleNamesWithBooking = List.of(RoleName.FL401_JUDGE);
        List<String> expectedRoleNamesWithOutBooking = List.of();
        Jurisdiction.CIVIL.getServiceCodes().forEach(serviceCode -> {
            shouldReturnFeePaidRolesFromJudicialAccessProfile(
                appointment, serviceCode, expectedRoleNamesWithBooking, true
            );
            shouldReturnFeePaidRolesFromJudicialAccessProfile(
                appointment, serviceCode, expectedRoleNamesWithOutBooking, false
            );
        });
    }

    @ParameterizedTest
    @EnumSource(value = LegacyAppointment.class, names = {
        "DISTRICT_JUDGE",
    })
    void verifyCivilJudgeSalariedAndSptwRoles(AppointmentEnum appointment) {
        List<String> expectedRoleNames = List.of(RoleName.FL401_JUDGE);
        AdditionalRoleEnum assignedRoles = LegacyAdditionalRole.ANY_OTHER_ROLE;
        Jurisdiction.CIVIL.getServiceCodes().forEach(serviceCode -> {
            runSalariedTestsForSalariedAndSptw(appointment, assignedRoles, serviceCode, expectedRoleNames, "1", false);
            runSalariedTestsForSalariedAndSptw(appointment, assignedRoles, serviceCode, expectedRoleNames, "5", false);
            runSalariedTestsForSalariedAndSptw(appointment, assignedRoles, serviceCode, expectedRoleNames, "2", false);
        });
    }


    private void runSalariedTestsForSalariedAndSptw(AppointmentEnum appointment,
                                                    AdditionalRoleEnum assignedRoles,
                                                    String serviceCode,
                                                    List<String>  expectedRoleNames,
                                                    String region,
                                                    boolean expectMultiRegion) {
        shouldReturnSalariedRolesFromJudicialAccessProfile(
            appointment, AppointmentType.SALARIED, assignedRoles, serviceCode,
            expectedRoleNames, region, expectMultiRegion
        );
        shouldReturnSalariedRolesFromJudicialAccessProfile(
            appointment, AppointmentType.SPTW, assignedRoles, serviceCode,
            expectedRoleNames, region, expectMultiRegion
        );
    }


    void shouldReturnSalariedRolesFromJudicialAccessProfile(AppointmentEnum appointment,
                                                            String appointmentType,
                                                            AdditionalRoleEnum assignedRole,
                                                            String serviceCode,
                                                            List<String>  expectedRoleNames,
                                                            String region,
                                                            boolean expectMultiRegion) {

        clearAndPrepareProfilesForDroolSession(
            appointment,
            appointmentType,
            assignedRole,
            serviceCode,
            region,
            false
        );

        // create map for all salaried roleNames that need regions
        List<String> rolesThatRequireRegions = List.of(
            RoleName.JUDGE,
            RoleName.LEADERSHIP_JUDGE,
            RoleName.TASK_SUPERVISOR,
            RoleName.CASE_ALLOCATOR,
            RoleName.SPECIFIC_ACCESS_APPROVER_JUDICIARY,
            RoleName.CIRCUIT_JUDGE,
            RoleName.SPECIFIC_ACCESS_APPROVER_LEGAL_OPS,
            RoleName.FL401_JUDGE
        );

        Map<String, List<String>> roleNameToRegionsMap = MultiRegion.buildRoleNameToRegionsMap(rolesThatRequireRegions);

        // Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags());

        // assertions
        MultiRegion.assertRoleAssignmentCount(
            roleAssignments,
            expectedRoleNames,
            expectMultiRegion,
            rolesThatRequireRegions,
            multiRegionList
        );

        roleAssignments.forEach(r -> {
            if (r.getAttributes().get(Attributes.Name.CONTRACT_TYPE) != null) {
                assertEquals(Attributes.ContractType.SALARIED,
                    r.getAttributes().get(Attributes.Name.CONTRACT_TYPE).asText());
            }

            assertCommonRoleAssignmentAttributes(r, appointmentType, serviceCode, roleNameToRegionsMap);
        });

        // verify regions add to map
        MultiRegion.assertRoleNameToRegionsMapIsAsExpected(
            roleNameToRegionsMap,
            expectedRoleNames,
            expectMultiRegion,
            multiRegionList,
            region, // fallback if not multi-region scenario
            null // i.e. no bookings
        );
    }


    void shouldReturnFeePaidRolesFromJudicialAccessProfile(AppointmentEnum appointment,
                                                           String serviceCode,
                                                           List<String> expectedRoleNames,
                                                           boolean addBooking) {
        String appointmentType = AppointmentType.FEE_PAID;
        String region = "any-region";

        clearAndPrepareProfilesForDroolSession(
            appointment,
            appointmentType,
            LegacyAdditionalRole.ANY_OTHER_ROLE,
            serviceCode,
            region,
            addBooking
        );

        // Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags());

        // assertions
        List<String> actualRoleNames = roleAssignments.stream().map(RoleAssignment::getRoleName).toList();
        assertTrue(actualRoleNames.containsAll(expectedRoleNames));
        assertEquals(expectedRoleNames.size(), roleAssignments.size());

        roleAssignments.forEach(r -> {
            if (r.getAttributes().get(Attributes.Name.CONTRACT_TYPE) != null) {
                assertEquals(Attributes.ContractType.FEE_PAID,
                    r.getAttributes().get(Attributes.Name.CONTRACT_TYPE).asText());
            }

            assertCommonRoleAssignmentAttributes(r, appointmentType, serviceCode, null);
        });
    }

    void shouldReturnMagistrateRolesFromJudicialAccessProfile(AppointmentEnum appointment,
                                                           List<String> expectedRoleNames) {
        String appointmentType = AppointmentType.VOLUNTARY;
        String region = "any-region";
        String serviceCode = SERVICE_CODE_PRL;

        clearAndPrepareProfilesForDroolSession(
            appointment,
            appointmentType,
            LegacyAdditionalRole.ANY_OTHER_ROLE,
            serviceCode,
            region,
            false
        );

        // Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags());

        // assertions
        List<String> actualRoleNames = roleAssignments.stream().map(RoleAssignment::getRoleName).toList();
        assertTrue(actualRoleNames.containsAll(expectedRoleNames));
        assertEquals(expectedRoleNames.size(), roleAssignments.size());

        roleAssignments.forEach(r -> {
            if (r.getAttributes().get(Attributes.Name.CONTRACT_TYPE) != null) {
                assertEquals(Attributes.ContractType.VOLUNTARY,
                    r.getAttributes().get(Attributes.Name.CONTRACT_TYPE).asText());
            }
            assertEquals(region, r.getAttributes().get(Attributes.Name.REGION).asText());

            assertCommonRoleAssignmentAttributes(r, appointmentType, serviceCode, null);
        });
    }

    private void clearAndPrepareProfilesForDroolSession(AppointmentEnum appointment,
                                                        String appointmentType,
                                                        AdditionalRoleEnum role,
                                                        String serviceCode,
                                                        String region,
                                                        boolean addBooking) {
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
                .ticketCodes(List.of(serviceCode))
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
                        .serviceCodes(List.of(serviceCode))
                        .jurisdiction(JURISDICTION)
                        .endDate(LocalDateTime.now().plusYears(1L))
                        .build()
                ))
                .build()
        );

        if (addBooking) {
            judicialBookings.add(
                JudicialBooking.builder()
                    .userId(USER_ID)
                    .locationId(BOOKING_LOCATION_ID)
                    .regionId(BOOKING_REGION_ID)
                    .beginTime(BOOKING_BEGIN_TIME)
                    .endTime(BOOKING_END_TIME)
                    .build()
            );
        }
    }

    List<FeatureFlag> getFeatureFlags() {
        return getAllFeatureFlagsToggleByJurisdiction("PRIVATELAW", true);
    }

}
