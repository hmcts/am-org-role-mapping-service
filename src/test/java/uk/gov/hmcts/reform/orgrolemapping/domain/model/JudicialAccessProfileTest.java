package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Jurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.jrd.AdditionalRole;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.jrd.AdditionalRoleEnum;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.jrd.Appointment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.jrd.AppointmentEnum;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.jrd.AppointmentGroup;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.FEE_PAID;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.SALARIED;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.SPTW;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.VOLUNTARY;

@Slf4j
@SuppressWarnings({"squid:S1192"})
class JudicialAccessProfileTest {


    @Nested
    @DisplayName("AppointmentType Checkers")
    class AppointmentTypeCheckers {

        @Test
        void testAppointmentTypeCheckers() {
            assertAppointmentTypeCheckers(FEE_PAID, true, false, false);
            assertAppointmentTypeCheckers(SALARIED, false, true, false);
            assertAppointmentTypeCheckers(SPTW, false, true, false);
            assertAppointmentTypeCheckers(VOLUNTARY, false, false, true);

            // test when an unexpected value is set
            assertAppointmentTypeCheckers("unexpected", false, false, false);

            // test when null/empty value is set
            assertAppointmentTypeCheckers(null, false, false, false);
            assertAppointmentTypeCheckers("", false, false, false);
        }

        private void assertAppointmentTypeCheckers(String appointmentType,
                                                   boolean expectedIsFeePaid,
                                                   boolean expectedIsSalaried,
                                                   boolean expectedIsVoluntary) {

            // GIVEN
            JudicialAccessProfile judicialAccessProfile = JudicialAccessProfile.builder()
                .appointmentType(appointmentType)
                .build();

            // WHEN / THEN
            log.info("Running isFeePaid() on '" + appointmentType + "', expecting: " + expectedIsFeePaid);
            assertEquals(expectedIsFeePaid, judicialAccessProfile.isFeePaid(), "isFeePaid() check failed");

            log.info("Running isSalaried() on '" + appointmentType + "', expecting: " + expectedIsSalaried);
            assertEquals(expectedIsSalaried, judicialAccessProfile.isSalaried(), "isSalaried() check failed");

            log.info("Running isVoluntary() on '" + appointmentType + "', expecting: " + expectedIsVoluntary);
            assertEquals(expectedIsVoluntary, judicialAccessProfile.isVoluntary(), "isVoluntary() check failed");

        }

    }


    @Nested
    @DisplayName("EndDate Checker")
    class EndDateChecker {

        @Test
        void testEndDateChecker() {
            assertEndDateChecker(null, true, "null end date");

            assertEndDateChecker(ZonedDateTime.now().plusDays(1), true, "future end date");

            assertEndDateChecker(ZonedDateTime.now().minusDays(1), false, "expired end date");
        }


        private void assertEndDateChecker(ZonedDateTime appointmentEndDateTime,
                                          boolean expectedHasValidEndDate,
                                          String testDescription) {

            // GIVEN
            JudicialAccessProfile judicialAccessProfile = JudicialAccessProfile.builder()
                .endTime(appointmentEndDateTime)
                .build();

            // WHEN / THEN
            log.info("Running hasValidEndDate() on " + testDescription + ", expecting: " + expectedHasValidEndDate);
            assertEquals(
                expectedHasValidEndDate,
                judicialAccessProfile.hasValidEndDate(),
                "hasValidEndDate() check failed for: " + testDescription
            );

        }

    }


    @Nested
    @DisplayName("HasAppointmentCode Tests")
    class HasAppointmentCode {

        // test enum for future edge cases when validating Appointments
        public enum ExtraTestAppointments implements AppointmentEnum {

            APPOINTMENT_WITH_MULTIPLE_CODES("Appointment With Multiple Codes", List.of("code-1", "code-2"));

            private final String name;
            private final List<String> codes;

            ExtraTestAppointments(String name, List<String> codes) {
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

        @ParameterizedTest
        @EnumSource(value = Appointment.class)
        @EnumSource(value = ExtraTestAppointments.class, names = {"APPOINTMENT_WITH_MULTIPLE_CODES"})
        void testHasAppointmentCode_appointments(AppointmentEnum appointment) {

            assertFalse(CollectionUtils.isEmpty(appointment.getCodes()), "Appointment has no codes defined");

            // happy path tests - matching code

            appointment.getCodes().forEach(code ->
                assertAppointmentCodeChecker(
                    code,
                    appointment,
                    true, // happy path test
                    "should match on valid code: " + code
                )
            );


            // negative tests - non-matching code

            assertAppointmentCodeChecker(
                null,
                appointment,
                false, // as no match
                "should fail to match on null code"
            );
            assertAppointmentCodeChecker(
                "",
                appointment,
                false, // as no match
                "should fail to match on empty code"
            );
            assertAppointmentCodeChecker(
                "wrong",
                appointment,
                false, // as no match
                "should fail to match on wrong code"
            );

        }

        @ParameterizedTest
        @EnumSource(value = AppointmentGroup.class)
        void testHasAppointmentCode_appointmentGroups(AppointmentGroup appointmentGroup) {

            assertFalse(CollectionUtils.isEmpty(appointmentGroup.getCodes()), "Appointment Group has no codes defined");

            // happy path tests - matching code

            appointmentGroup.getMembers().forEach(appointment ->
                appointment.getCodes().forEach(code ->
                    assertAppointmentCodeChecker(
                        code,
                        appointmentGroup,
                        true, // happy path test
                        "should match on valid code: " + code + " (from: '" + appointment.getName() + "')"
                    )
                )
            );


            // negative tests - non-matching code

            assertAppointmentCodeChecker(
                null,
                appointmentGroup,
                false, // as no match
                "should fail to match on null code"
            );
            assertAppointmentCodeChecker(
                "",
                appointmentGroup,
                false, // as no match
                "should fail to match on empty code"
            );
            assertAppointmentCodeChecker(
                "wrong",
                appointmentGroup,
                false, // as no match
                "should fail to match on wrong code"
            );

        }

        private void assertAppointmentCodeChecker(String japRoleId,
                                                  AppointmentEnum testAppointment,
                                                  boolean expectedHasAppointmentCode,
                                                  String testDescription) {

            // GIVEN
            JudicialAccessProfile judicialAccessProfile = JudicialAccessProfile.builder()
                .roleId(japRoleId)
                .build();

            // WHEN / THEN
            log.info("Running hasAppointmentCode(" + testAppointment.getName() + ") test: " + testDescription);
            assertEquals(
                expectedHasAppointmentCode,
                judicialAccessProfile.hasAppointmentCode(testAppointment),
                "hasAppointmentCode() check failed for: " + testDescription
            );

        }

    }


    @Nested
    @DisplayName("HasValidAdditionalRole Tests")
    class HasValidAdditionalRole {

        // test enum for future edge cases when validating Additional Roles
        public enum ExtraTestAdditionalRoles implements AdditionalRoleEnum {

            ANY_OTHER_ROLE("Any Other Role", List.of("any-code")),
            ROLE_WITH_MULTIPLE_CODES("Role With Multiple Codes", List.of("code-1", "code-2"));

            private final String name;
            private final List<String> codes;

            ExtraTestAdditionalRoles(String name, List<String> codes) {
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

        @ParameterizedTest
        @NullAndEmptySource
        void testHasValidAdditionalRole_falseWhenNullOrEmptyRoleList(List<RoleV2> roles) {

            assertHasValidAdditionalRoleChecker(
                roles,
                ExtraTestAdditionalRoles.ANY_OTHER_ROLE, // NB: any AdditionalRole will do
                false,
                "null/empty authorisations"
            );

        }

        @ParameterizedTest
        @EnumSource(value = AdditionalRole.class)
        @EnumSource(value = ExtraTestAdditionalRoles.class, names = {"ROLE_WITH_MULTIPLE_CODES"})
        void testHasValidAdditionalRole(AdditionalRoleEnum additionalRole) {

            assertFalse(CollectionUtils.isEmpty(additionalRole.getCodes()), "AdditionalRole has no codes defined");

            // happy path tests - matching code

            additionalRole.getCodes().forEach(additionalRoleCode ->
                testHasValidAdditionalRole(additionalRole, additionalRoleCode)
            );


            // negative tests - non-matching code

            assertHasValidAdditionalRoleChecker(
                null,
                null, // no end date
                additionalRole,
                false, // as no match
                "should fail to match on null code"
            );
            assertHasValidAdditionalRoleChecker(
                "",
                null, // no end date
                additionalRole,
                false, // as no match
                "should fail to match on empty code"
            );
            assertHasValidAdditionalRoleChecker(
                "wrong",
                null, // no end date
                additionalRole,
                false, // as no match
                "should fail to match on wrong code"
            );

        }

        void testHasValidAdditionalRole(AdditionalRoleEnum additionalRole, String additionalRoleCode) {

            // happy path tests - matching code

            // NO END DATE
            assertHasValidAdditionalRoleChecker(
                additionalRoleCode,
                null, // no end date
                additionalRole,
                true, // happy path test
                "should match on valid code: " + additionalRoleCode + ", no auth end date"
            );

            // VALID END DATE
            assertHasValidAdditionalRoleChecker(
                additionalRoleCode,
                LocalDate.now().plusDays(1), // valid end date
                additionalRole,
                true, // happy path test
                "should match on valid code: " + additionalRoleCode + ", valid auth end date"
            );

            // negative tests

            // EXPIRED END DATE
            assertHasValidAdditionalRoleChecker(
                additionalRoleCode,
                LocalDate.now().minusDays(10), // expired end date
                additionalRole,
                false, // as expired
                "should fail to match on code: " + additionalRoleCode + ", expired auth end date"
            );

        }

        private void assertHasValidAdditionalRoleChecker(List<RoleV2> assignedRoles,
                                                         AdditionalRoleEnum additionalRole,
                                                         boolean expectedHasValidRole,
                                                         String testDescription) {

            // GIVEN
            JudicialAccessProfile judicialAccessProfile = JudicialAccessProfile.builder()
                .additionalRoles(assignedRoles)
                .build();

            // WHEN / THEN
            log.info("Running hasValidAdditionalRole(" + additionalRole.getName() + ") test: " + testDescription);
            assertEquals(
                expectedHasValidRole,
                judicialAccessProfile.hasValidAdditionalRole(additionalRole),
                "hasValidAdditionalRole() check failed for: " + testDescription
            );

        }

        private void assertHasValidAdditionalRoleChecker(String additionalRoleCode,
                                                         LocalDate roleEndDate,
                                                         AdditionalRoleEnum additionalRole,
                                                         boolean expectedHasValidRole,
                                                         String testDescription) {

            // GIVEN
            RoleV2 testRole = RoleV2.builder()
                .jurisdictionRoleId(additionalRoleCode)
                .endDate(roleEndDate)
                .build();

            // WHEN / THEN
            assertHasValidAdditionalRoleChecker(
                List.of(testRole),
                additionalRole,
                expectedHasValidRole,
                testDescription + ", single role"
            );

            // repeat for multiple authorisations
            RoleV2 otherRole1 = RoleV2.builder().build();
            RoleV2 otherRole2 = RoleV2.builder().jurisdictionRoleId("anotherCode").build();

            // WHEN / THEN
            assertHasValidAdditionalRoleChecker(
                List.of(otherRole1, testRole, otherRole2),
                additionalRole,
                expectedHasValidRole,
                testDescription + ", multiple roles"
            );

        }

    }


    @Nested
    @DisplayName("HasValidAuthorisation Tests")
    class HasValidAuthorisation {

        @ParameterizedTest
        @NullAndEmptySource
        void testHasValidAuthorisation_falseWhenNullOrEmptyAuthorisations(List<Authorisation> authorisations) {

            assertHasValidAuthorisationChecker(
                authorisations,
                Jurisdiction.CIVIL, // NB: any jurisdiction will do
                false,
                "null/empty authorisations"
            );

        }

        @ParameterizedTest
        @EnumSource(value = Jurisdiction.class)
        void testHasValidAuthorisation(Jurisdiction jurisdiction) {

            assertFalse(CollectionUtils.isEmpty(jurisdiction.getServiceCodes()), "Jurisdiction has no codes defined");

            // happy path tests - matching code

            jurisdiction.getServiceCodes().forEach(serviceCode ->
                testHasValidAuthorisation(jurisdiction, serviceCode)
            );


            // negative tests - non-matching code

            ArrayList<String> stringListWithNull = new ArrayList<>();
            stringListWithNull.add(null);
            assertHasValidAuthorisationChecker(
                stringListWithNull,
                null, // no end date
                jurisdiction,
                false, // as no match
                "should fail to match on null code"
            );
            assertHasValidAuthorisationChecker(
                List.of(""),
                null, // no end date
                jurisdiction,
                false, // as no match
                "should fail to match on empty code"
            );
            assertHasValidAuthorisationChecker(
                List.of("wrong"),
                null, // no end date
                jurisdiction,
                false, // as no match
                "should fail to match on wrong code"
            );

        }

        void testHasValidAuthorisation(Jurisdiction jurisdiction, String serviceCode) {

            // happy path tests - matching code

            // NO END DATE
            assertHasValidAuthorisationChecker(
                List.of(serviceCode),
                null, // no end date
                jurisdiction,
                true, // happy path test
                "should match on valid code: " + serviceCode + ", no auth end date, single auth service code"
            );

            assertHasValidAuthorisationChecker(
                List.of("no-match1", serviceCode, "no-match2"),
                null, // no end date
                jurisdiction,
                true, // happy path test
                "should match on valid code: " + serviceCode + ", no auth end date, multiple service codes"
            );

            // VALID END DATE
            assertHasValidAuthorisationChecker(
                List.of(serviceCode),
                LocalDateTime.now().plusDays(1), // valid end date
                jurisdiction,
                true, // happy path test
                "should match on valid code: " + serviceCode + ", valid auth end date, single auth service code"
            );

            assertHasValidAuthorisationChecker(
                List.of("no-match1", "no-match2", serviceCode),
                LocalDateTime.now().plusDays(10), // valid end date
                jurisdiction,
                true, // happy path test
                "should match on valid code: " + serviceCode + ", valid auth end date, multiple auth service codes"
            );

            // negative tests

            // EXPIRED END DATE
            assertHasValidAuthorisationChecker(
                List.of(serviceCode),
                LocalDateTime.now().minusDays(1), // expired end date
                jurisdiction,
                false, // as expired
                "should fail to match on code: " + serviceCode + ", expired auth end date, single auth service code"
            );

            assertHasValidAuthorisationChecker(
                List.of("XYZ1", "XYZ2", serviceCode),
                LocalDateTime.now().minusDays(10), // expired end date
                jurisdiction,
                false, // as expired
                "should fail to match on code: " + serviceCode + ", expired auth end date, multiple auth service code"
            );

        }

        private void assertHasValidAuthorisationChecker(List<Authorisation> authorisations,
                                                        Jurisdiction jurisdiction,
                                                        boolean expectedHasValidAuth,
                                                        String testDescription) {

            // GIVEN
            JudicialAccessProfile judicialAccessProfile = JudicialAccessProfile.builder()
                .authorisations(authorisations)
                .build();

            // WHEN / THEN
            log.info("Running HasValidAuthorisation(" + jurisdiction.getName() + ") test: " + testDescription);
            assertEquals(
                expectedHasValidAuth,
                judicialAccessProfile.hasValidAuthorisation(jurisdiction),
                "hasValidAuthorisation() check failed for: " + testDescription
            );

        }

        private void assertHasValidAuthorisationChecker(List<String> serviceCodes,
                                                        LocalDateTime authorisationEndDateTime,
                                                        Jurisdiction jurisdiction,
                                                        boolean expectedHasValidAuth,
                                                        String testDescription) {

            // GIVEN
            Authorisation testAuthorisation = Authorisation.builder()
                .serviceCodes(serviceCodes)
                .endDate(authorisationEndDateTime)
                .build();

            // WHEN / THEN
            assertHasValidAuthorisationChecker(
                List.of(testAuthorisation),
                jurisdiction,
                expectedHasValidAuth,
                testDescription + ", single authorisation"
            );

            // repeat for multiple authorisations
            Authorisation otherAuthorisation1 = Authorisation.builder().build();
            Authorisation otherAuthorisation2 = Authorisation.builder().serviceCodes(List.of("anotherCode")).build();

            // WHEN / THEN
            assertHasValidAuthorisationChecker(
                List.of(otherAuthorisation1, testAuthorisation, otherAuthorisation2),
                jurisdiction,
                expectedHasValidAuth,
                testDescription + ", multiple authorisations"
            );

        }

    }


}
