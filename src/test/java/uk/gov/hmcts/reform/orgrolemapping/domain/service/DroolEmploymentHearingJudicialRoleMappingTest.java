package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.ExtraTestAdditionalRoles;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.ExtraTestAppointments;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(MockitoExtension.class)
class DroolEmploymentHearingJudicialRoleMappingTest extends DroolBase {

    private static final String JURISDICTION = Jurisdiction.EMPLOYMENT.getName();
    private static final String SERVICE_CODE = Jurisdiction.EMPLOYMENT.getServiceCodes().get(0);

    static String userId = "3168da13-00b3-41e3-81fa-cbc71ac28a69";

    static Map<String, String> employmentExpectedRoleNameWorkTypesMap = new HashMap<>();

    static {
        employmentExpectedRoleNameWorkTypesMap.put(RoleName.LEADERSHIP_JUDGE, null);
        employmentExpectedRoleNameWorkTypesMap.put(RoleName.JUDGE,
            "hearing_work,decision_making_work,routine_work,applications,amendments");
        employmentExpectedRoleNameWorkTypesMap.put(RoleName.TASK_SUPERVISOR, null);
        employmentExpectedRoleNameWorkTypesMap.put(RoleName.CASE_ALLOCATOR, null);
        employmentExpectedRoleNameWorkTypesMap.put(RoleName.HMCTS_JUDICIARY, null);
        employmentExpectedRoleNameWorkTypesMap.put(RoleName.SPECIFIC_ACCESS_APPROVER_JUDICIARY,
            "access_requests");
        employmentExpectedRoleNameWorkTypesMap.put(RoleName.FEE_PAID_JUDGE,
            "hearing_work,decision_making_work,routine_work,applications,amendments");
        employmentExpectedRoleNameWorkTypesMap.put(RoleName.TRIBUNAL_MEMBER,
            "hearing_work");
    }

    // NB: to be retired after DTSAM-970
    private enum LegacyAppointment implements AppointmentEnum {

        ANY_OTHER_APPOINTMENT("Any Other Appointment", List.of("any-code")),
        EMPLOYMENT_JUDGE("Employment Judge", List.of("48")),
        EMPLOYMENT_JUDGE_SITTING_IN_RETIREMENT("Employment Judge (sitting in retirement)", List.of("128", "215")),
        RECORDER("Recorder", List.of("67")),
        REGIONAL_TRIBUNAL_JUDGE("Regional Tribunal Judge", List.of("74")),
        TRIBUNAL_JUDGE("Tribunal Judge", List.of("84"));

        private final String name;
        private final List<String> codes;

        LegacyAppointment(String name, List<String> codes) {
            this.name = name;
            this.codes = codes;
        }

        public String getName() {
            return name;
        }

        public List<String> getCodes() {
            return codes;
        }
    }

    static Stream<Arguments> endToEndDataAppointments() {
        return Stream.of(
                Arguments.of(Appointment.PRESIDENT_OF_TRIBUNAL,
                        AppointmentType.SALARIED,
                        false,
                        ExtraTestAdditionalRoles.ANY_OTHER_ROLE,
                        List.of("leadership-judge", "judge", "task-supervisor", "case-allocator", "hmcts-judiciary",
                                "specific-access-approver-judiciary", "hearing-viewer"),
                        null),
                /* Test to be enabled after DTSAM-995
                Arguments.of(Appointment.PRESIDENT_OF_TRIBUNAL,
                        AppointmentType.SPTW,
                        false,
                        ExtraTestAdditionalRoles.ANY_OTHER_ROLE,
                        List.of("leadership-judge", "judge", "task-supervisor", "case-allocator", "hmcts-judiciary",
                            "specific-access-approver-judiciary", "hearing-viewer"),
                        null),
                */
                Arguments.of(Appointment.PRESIDENT_ET_SCOTLAND,
                        AppointmentType.SALARIED,
                        false,
                        ExtraTestAdditionalRoles.ANY_OTHER_ROLE,
                        List.of("leadership-judge", "judge", "task-supervisor", "case-allocator", "hmcts-judiciary",
                            "specific-access-approver-judiciary", "hearing-viewer"),
                        null),
                /* Test to be enabled after DTSAM-995
                Arguments.of(Appointment.PRESIDENT_ET_SCOTLAND,
                        AppointmentType.SPTW,
                        false,
                        ExtraTestAdditionalRoles.ANY_OTHER_ROLE,
                        List.of("leadership-judge", "judge", "task-supervisor", "case-allocator", "hmcts-judiciary",
                            "specific-access-approver-judiciary", "hearing-viewer"),
                        null),
                */
                Arguments.of(Appointment.VICE_PRESIDENT,
                        AppointmentType.SALARIED,
                        false,
                        ExtraTestAdditionalRoles.ANY_OTHER_ROLE,
                        List.of("leadership-judge", "judge", "task-supervisor", "case-allocator", "hmcts-judiciary",
                                "specific-access-approver-judiciary", "hearing-viewer"),
                        null),
                /* Test to be enabled after DTSAM-995
                Arguments.of(Appointment.VICE_PRESIDENT,
                        AppointmentType.SPTW,
                        false,
                        ExtraTestAdditionalRoles.ANY_OTHER_ROLE,
                        List.of("leadership-judge", "judge", "task-supervisor", "case-allocator", "hmcts-judiciary",
                            "specific-access-approver-judiciary", "hearing-viewer"),
                        null),
                 */
                Arguments.of(Appointment.VICE_PRESIDENT_ET_SCOTLAND,
                        AppointmentType.SALARIED,
                        false,
                        ExtraTestAdditionalRoles.ANY_OTHER_ROLE,
                        List.of("leadership-judge", "judge", "task-supervisor", "case-allocator", "hmcts-judiciary",
                            "specific-access-approver-judiciary", "hearing-viewer"),
                        null),
                /* Test to be enabled after DTSAM-995
                Arguments.of(Appointment.VICE_PRESIDENT_ET_SCOTLAND,
                        AppointmentType.SPTW,
                        false,
                        ExtraTestAdditionalRoles.ANY_OTHER_ROLE,
                        List.of("leadership-judge", "judge", "task-supervisor", "case-allocator", "hmcts-judiciary",
                                "specific-access-approver-judiciary", "hearing-viewer"),
                        null),
                 */
                Arguments.of(Appointment.REGIONAL_EMPLOYMENT_JUDGE,
                        AppointmentType.SALARIED,
                        false,
                        ExtraTestAdditionalRoles.ANY_OTHER_ROLE,
                        List.of("leadership-judge", "judge", "task-supervisor", "case-allocator", "hmcts-judiciary",
                                "specific-access-approver-judiciary", "hearing-viewer"),
                        null),
                /* Test to be enabled after DTSAM-995
                Arguments.of(Appointment.REGIONAL_EMPLOYMENT_JUDGE,
                        AppointmentType.SPTW,
                        false,
                        ExtraTestAdditionalRoles.ANY_OTHER_ROLE,
                        List.of("leadership-judge", "judge", "task-supervisor", "case-allocator", "hmcts-judiciary",
                            "specific-access-approver-judiciary", "hearing-viewer"),
                        null),
                 */
                Arguments.of(LegacyAppointment.EMPLOYMENT_JUDGE,
                        AppointmentType.SALARIED,
                        false,
                        ExtraTestAdditionalRoles.ANY_OTHER_ROLE,
                        List.of("judge", "hmcts-judiciary", "hearing-viewer", "case-allocator"),
                        null),
                Arguments.of(LegacyAppointment.EMPLOYMENT_JUDGE,
                        AppointmentType.SPTW,
                        false,
                        ExtraTestAdditionalRoles.ANY_OTHER_ROLE,
                        List.of("judge", "hmcts-judiciary", "hearing-viewer", "case-allocator"),
                        null),
                Arguments.of(LegacyAppointment.EMPLOYMENT_JUDGE,
                        AppointmentType.FEE_PAID,
                        false,
                        ExtraTestAdditionalRoles.ANY_OTHER_ROLE,
                        List.of("fee-paid-judge", "hmcts-judiciary", "hearing-viewer"),
                        null),
                Arguments.of(LegacyAppointment.EMPLOYMENT_JUDGE_SITTING_IN_RETIREMENT,
                        AppointmentType.FEE_PAID,
                        false,
                        ExtraTestAdditionalRoles.ANY_OTHER_ROLE,
                        List.of("fee-paid-judge", "hmcts-judiciary", "hearing-viewer"),
                        null),
                Arguments.of(LegacyAppointment.RECORDER,
                        AppointmentType.FEE_PAID,
                        false,
                        ExtraTestAdditionalRoles.ANY_OTHER_ROLE,
                        List.of("fee-paid-judge", "hmcts-judiciary", "hearing-viewer"),
                        null),
                Arguments.of(LegacyAppointment.REGIONAL_TRIBUNAL_JUDGE,
                        AppointmentType.FEE_PAID,
                        false,
                        ExtraTestAdditionalRoles.ANY_OTHER_ROLE,
                        List.of("fee-paid-judge", "hmcts-judiciary", "hearing-viewer"),
                        null),
                Arguments.of(LegacyAppointment.TRIBUNAL_JUDGE,
                        AppointmentType.FEE_PAID,
                        false,
                        ExtraTestAdditionalRoles.ANY_OTHER_ROLE,
                        List.of("fee-paid-judge", "hmcts-judiciary", "hearing-viewer"),
                        null),
                Arguments.of(Appointment.TRIBUNAL_MEMBER,
                        AppointmentType.FEE_PAID,
                        false,
                        ExtraTestAdditionalRoles.ANY_OTHER_ROLE,
                        List.of("tribunal-member", "hmcts-judiciary", "hearing-viewer"),
                        "1036"),
                Arguments.of(Appointment.TRIBUNAL_MEMBER_LAY,
                        AppointmentType.FEE_PAID,
                        false,
                        ExtraTestAdditionalRoles.ANY_OTHER_ROLE,
                        List.of("tribunal-member", "hmcts-judiciary", "hearing-viewer"),
                        "1036")
        );
    }

    static Stream<Arguments> endToEndDataAdditionalRoles() {
        return Stream.of(
                Arguments.of(ExtraTestAppointments.ANY_OTHER_APPOINTMENT,
                        AppointmentType.SALARIED,
                        false,
                        AdditionalRole.ACTING_REGIONAL_EMPLOYMENT_JUDGE,
                        List.of("leadership-judge", "judge", "task-supervisor", "case-allocator", "hmcts-judiciary",
                                "specific-access-approver-judiciary", "hearing-viewer"),
                        null),
                Arguments.of(ExtraTestAppointments.ANY_OTHER_APPOINTMENT,
                        AppointmentType.SPTW,
                        false,
                        AdditionalRole.ACTING_REGIONAL_EMPLOYMENT_JUDGE,
                        List.of("leadership-judge", "judge", "task-supervisor", "case-allocator", "hmcts-judiciary",
                            "specific-access-approver-judiciary", "hearing-viewer"),
                        null)
        );
    }

    @SuppressWarnings({"ParameterCanBeLocal"})
    @ParameterizedTest
    @MethodSource("endToEndDataAppointments")
    @MethodSource("endToEndDataAdditionalRoles") // NB: AdditionalRole mappings still require a valid appointment
    void shouldGenerateNoRoleAssignments_forAppointmentMapping_expiredAuthEndDate(
        AppointmentEnum appointment, String appointmentType, boolean addBooking,
        AdditionalRoleEnum additionalRole, List<String> expectedRoleNames, String baseLocationId) {

        // override parameter as this test will allways generate no role assignments as authorisation is expired
        expectedRoleNames = List.of();

        shouldGenerateRoleAssignments_forAppointmentMapping(
            appointment,
            appointmentType,
            addBooking,
            additionalRole,
            expectedRoleNames,
            baseLocationId,
            LocalDateTime.now().minusDays(1L) // i.e. expired end date
        );
    }

    @ParameterizedTest
    @MethodSource("endToEndDataAppointments")
    @MethodSource("endToEndDataAdditionalRoles") // NB: AdditionalRole mappings still require a valid appointment
    void shouldGenerateRoleAssignments_forAppointmentMapping_noAuthEndDate(
        AppointmentEnum appointment, String appointmentType, boolean addBooking,
        AdditionalRoleEnum additionalRole, List<String> expectedRoleNames, String baseLocationId) {

        shouldGenerateRoleAssignments_forAppointmentMapping(
            appointment,
            appointmentType,
            addBooking,
            additionalRole,
            expectedRoleNames,
            baseLocationId,
            null // i.e. no end date
        );
    }

    @ParameterizedTest
    @MethodSource("endToEndDataAppointments")
    @MethodSource("endToEndDataAdditionalRoles") // NB: AdditionalRole mappings still require a valid appointment
    void shouldGenerateRoleAssignments_forAppointmentMapping_validAuthEndDate(
        AppointmentEnum appointment, String appointmentType, boolean addBooking,
        AdditionalRoleEnum additionalRole, List<String> expectedRoleNames, String baseLocationId) {

        shouldGenerateRoleAssignments_forAppointmentMapping(
            appointment,
            appointmentType,
            addBooking,
            additionalRole,
            expectedRoleNames,
            baseLocationId,
            LocalDateTime.now().plusYears(1L) // i.e. valid end date
        );
    }

    void shouldGenerateRoleAssignments_forAppointmentMapping(
        AppointmentEnum appointment, String appointmentType, boolean addBooking,
        AdditionalRoleEnum additionalRole, List<String> expectedRoleNames, String baseLocationId,
        LocalDateTime authEndDate) {

        assertFalse(CollectionUtils.isEmpty(appointment.getCodes()), "Appointment has no codes defined");

        // repeat for all valid appointment codes
        appointment.getCodes().forEach(appointmentCode -> {

            RoleV2 additionalRoles = RoleV2.builder()
                .jurisdictionRoleName(additionalRole.getName())
                .jurisdictionRoleId(additionalRole.getCodes().get(0)) // any role code will do in this test
                .startDate(LocalDate.now().minusDays(20L))
                .endDate(LocalDate.now().plusDays(20L)) // i.e. valid end date
                .build();

            shouldTakeJudicialAccessProfileConvertToJudicialOfficeHolderThenReturnRoleAssignments(
                appointment.getName(),
                appointmentCode,
                appointmentType,
                addBooking,
                additionalRoles,
                expectedRoleNames,
                baseLocationId,
                authEndDate
            );

        });

    }

    @Disabled("To be enabled as part of DTSAM-995")
    @SuppressWarnings({"ParameterCanBeLocal"})
    @ParameterizedTest
    @MethodSource("endToEndDataAdditionalRoles")
    void shouldGenerateNoRoleAssignments_forAdditionalRoleMapping_expiredRoleEndDate(
        AppointmentEnum appointment, String appointmentType, boolean addBooking,
        AdditionalRoleEnum additionalRole, List<String> expectedRoleNames, String baseLocationId) {

        // override parameter as this test will allways generate no role assignments as additional role is expired
        expectedRoleNames = List.of();

        shouldGenerateRoleAssignments_forAdditionalRole(
            appointment,
            appointmentType,
            addBooking,
            additionalRole,
            expectedRoleNames,
            baseLocationId,
            LocalDate.now().minusDays(1L) // i.e. expired end date
        );
    }

    @ParameterizedTest
    @MethodSource("endToEndDataAdditionalRoles")
    void shouldGenerateRoleAssignments_forAdditionalRoleMapping_noRoleEndDate(
        AppointmentEnum appointment, String appointmentType, boolean addBooking,
        AdditionalRoleEnum additionalRole, List<String> expectedRoleNames, String baseLocationId) {

        shouldGenerateRoleAssignments_forAdditionalRole(
            appointment,
            appointmentType,
            addBooking,
            additionalRole,
            expectedRoleNames,
            baseLocationId,
            null // i.e. no end date
        );

    }

    @ParameterizedTest
    @MethodSource("endToEndDataAdditionalRoles")
    void shouldGenerateRoleAssignments_forAdditionalRoleMapping_validRoleEndDate(
        AppointmentEnum appointment, String appointmentType, boolean addBooking,
        AdditionalRoleEnum additionalRole, List<String> expectedRoleNames, String baseLocationId) {

        shouldGenerateRoleAssignments_forAdditionalRole(
            appointment,
            appointmentType,
            addBooking,
            additionalRole,
            expectedRoleNames,
            baseLocationId,
            LocalDate.now().plusYears(1L) // i.e. valid end date
        );

    }

    void shouldGenerateRoleAssignments_forAdditionalRole(
        AppointmentEnum appointment, String appointmentType, boolean addBooking,
        AdditionalRoleEnum additionalRole, List<String> expectedRoleNames, String baseLocationId,
        LocalDate roleEndDate) {

        assertFalse(CollectionUtils.isEmpty(additionalRole.getCodes()), "AdditionalRole has no codes defined");

        // repeat for all valid additional-role codes
        additionalRole.getCodes().forEach(additionalRoleCode -> {

            RoleV2 additionalRoles = RoleV2.builder()
                .jurisdictionRoleName(additionalRole.getName())
                .jurisdictionRoleId(additionalRoleCode)
                .startDate(LocalDate.now().minusDays(20L))
                .endDate(roleEndDate)
                .build();

            shouldTakeJudicialAccessProfileConvertToJudicialOfficeHolderThenReturnRoleAssignments(
                appointment.getName(),
                appointment.getCodes().get(0), // any appointment code will do in this test
                appointmentType,
                addBooking,
                additionalRoles,
                expectedRoleNames,
                baseLocationId,
                LocalDateTime.now().plusYears(1L) // i.e. valid end date
            );

        });

    }

    void shouldTakeJudicialAccessProfileConvertToJudicialOfficeHolderThenReturnRoleAssignments(
        String appointment, String appointmentCode, String appointmentType, boolean addBooking,
        RoleV2 additionalRole, List<String> expectedRoleNames, String baseLocationId,
        LocalDateTime authEndDate) {

        log.info("""
                    Running JudicialAccessProfile -> RoleAssignments test for:
                      * Appointment: '{}', with code: '{}', authEndDate: {}
                      * AdditionalRole: '{}', with code: '{}', roleEndDate: {}
                      * AppointmentType: '{}'
                      * Expected RoleNames: {}
                    """,
            appointment,
            appointmentCode,
            authEndDate,
            additionalRole.getJurisdictionRoleName(),
            additionalRole.getJurisdictionRoleId(),
            additionalRole.getEndDate(),
            appointmentType,
            expectedRoleNames
        );

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
                        .appointmentCode(appointmentCode)
                        .appointmentType(appointmentType)
                        .userId(userId)
                        .roles(List.of(additionalRole.getJurisdictionRoleName()))
                        .additionalRoles(List.of(additionalRole))
                        .regionId("LDN")
                        .baseLocationId(baseLocationId)
                        .primaryLocationId("London")
                        .ticketCodes(List.of(SERVICE_CODE))
                        .authorisations(List.of(
                                Authorisation.builder()
                                        .serviceCodes(List.of(SERVICE_CODE))
                                        .jurisdiction(JURISDICTION)
                                        .endDate(authEndDate)
                                        .build()
                        ))
                        .build()
        );

        log.info("""
                    Test JudicialAccessProfiles used:
                    {}
                    """,
            writeValueAsPrettyJson(judicialAccessProfiles)
        );

        // Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(setFeatureFlags());


        List<String> roleNameResults = roleAssignments.stream().map(RoleAssignment::getRoleName).toList();

        log.info("""
                    Results:
                      * RoleNames: {}

                    RoleAssignments:
                    {}
                    """,
            roleNameResults,
            writeValueAsPrettyJson(roleAssignments)
        );

        // assert expected number of role assignments match ...
        assertEquals(expectedRoleNames.size(), roleAssignments.size());
        // ... then check all are present
        assertThat(roleNameResults, containsInAnyOrder(expectedRoleNames.toArray()));

        // assertions
        roleAssignments.forEach(r -> {
            assertEquals(userId, r.getActorId());
            assertCommonRoleAssignmentAttributes(r, "LDN", appointment);
        });

    }

    static void assertCommonRoleAssignmentAttributes(RoleAssignment r, String regionId, String office) {


        //filter

        assertEquals(ActorIdType.IDAM, r.getActorIdType());
        assertEquals(userId, r.getActorId());
        assertEquals(RoleType.ORGANISATION, r.getRoleType());
        assertEquals(RoleCategory.JUDICIAL, r.getRoleCategory());
        assertNull(r.getAttributes().get(Attributes.Name.BOOKABLE));

        if (r.getRoleName().equals(RoleName.HMCTS_JUDICIARY)) {
            assertNull(r.getAttributes().get(Attributes.Name.REGION));
            assertEquals(Classification.PRIVATE, r.getClassification());
            assertEquals(GrantType.BASIC, r.getGrantType());
            assertTrue(r.isReadOnly());
        } else if (r.getRoleName().equals(RoleName.HEARING_VIEWER)) {
            assertNull(r.getAttributes().get(Attributes.Name.REGION));
            assertEquals(Classification.PUBLIC, r.getClassification());
            assertEquals(GrantType.STANDARD, r.getGrantType());
            assertEquals(JURISDICTION, r.getAttributes().get(Attributes.Name.JURISDICTION).asText());
            assertFalse(r.isReadOnly());
            assertNull(r.getAttributes().get(Attributes.Name.CONTRACT_TYPE));
        } else {
            assertEquals(Classification.PUBLIC, r.getClassification());
            assertEquals(GrantType.STANDARD, r.getGrantType());
            assertEquals(JURISDICTION, r.getAttributes().get(Attributes.Name.JURISDICTION).asText());
            assertFalse(r.isReadOnly());
        }

        String expectedWorkTypes = employmentExpectedRoleNameWorkTypesMap.get(r.getRoleName());
        String actualWorkTypes = null;
        if (r.getAttributes().get(Attributes.Name.WORK_TYPES) != null) {
            actualWorkTypes = r.getAttributes().get(Attributes.Name.WORK_TYPES).asText();
        }
        assertEquals(expectedWorkTypes, actualWorkTypes);
    }

    private List<FeatureFlag> setFeatureFlags() {
        List<FeatureFlag> featureFlags = new ArrayList<>(getAllFeatureFlagsToggleByJurisdiction("EMPLOYMENT", true));

        featureFlags.add(
                FeatureFlag.builder()
                        .flagName("sscs_hearing_1_0")
                        .status(true)
                        .build()
        );

        return featureFlags;
    }

}
