package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Jurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.jrd.Appointment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.jrd.AppointmentEnum;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.jrd.AppointmentGroup;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.FEE_PAID;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.SALARIED;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.SPTW;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.VOLUNTARY;

@Slf4j
class JudicialAccessProfileTest {

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

    @Test
    void testEndDateChecker() {
        assertEndDateChecker(null, true, "null end date");

        assertEndDateChecker(ZonedDateTime.now().plusDays(1), true, "future end date");

        assertEndDateChecker(ZonedDateTime.now().minusDays(1), false, "expired end date");
    }

    @ParameterizedTest
    @EnumSource(value = Appointment.class)
    void testHasAppointmentCode_appointments(Appointment appointment) {

        assertFalse(CollectionUtils.isEmpty(appointment.getCodes()), "Appointment has no codes defined");

        // happy path tests - matching code
        appointment.getCodes().forEach(code ->
            assertAppointmentCodeChecker(
                code,
                appointment,
                true,
                "should match on valid code: " + code
            )
        );

        // negative tests - non-matching code

        assertAppointmentCodeChecker(
            null,
            appointment,
            false,
            "should fail to match on null code"
        );
        assertAppointmentCodeChecker(
            "",
            appointment,
            false,
            "should fail to match on empty code"
        );
        assertAppointmentCodeChecker(
            "wrong",
            appointment,
            false,
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
                    true,
                    "should match on valid code: " + code + " (from: '" + appointment.getName() + "')"
                )
            )
        );

        // negative tests - non-matching code
        assertAppointmentCodeChecker(
            null,
            appointmentGroup,
            false,
            "should fail to match on null code"
        );
        assertAppointmentCodeChecker(
            "",
            appointmentGroup,
            false,
            "should fail to match on empty code"
        );
        assertAppointmentCodeChecker(
            "wrong",
            appointmentGroup,
            false,
            "should fail to match on wrong code"
        );

    }

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
    @CsvSource(textBlock = """
        CIVIL, AAA6, true
        CIVIL, AAA7, true
        EMPLOYMENT, BHA1, true
        CIVIL, XYZ1, false
        EMPLOYMENT, XYZ2, false
        """)
    void testHasValidAuthorisation(Jurisdiction jurisdiction, String serviceCode, boolean expectedHasValidAuth) {

        // NO END DATE
        assertHasValidAuthorisationChecker(
            List.of(serviceCode),
            null, // no end date
            jurisdiction,
            expectedHasValidAuth,
            "no auth end date, single auth service code"
        );

        assertHasValidAuthorisationChecker(
            List.of("XYZ1", serviceCode, "XYZ2"),
            null, // no end date
            jurisdiction,
            expectedHasValidAuth,
            "no auth end date, multiple service codes"
        );

        // VALID END DATE
        assertHasValidAuthorisationChecker(
            List.of(serviceCode),
            LocalDateTime.now().plusDays(1), // valid end date
            jurisdiction,
            expectedHasValidAuth,
            "valid auth end date, single auth service code"
        );

        assertHasValidAuthorisationChecker(
            List.of("XYZ1", "XYZ2", serviceCode),
            LocalDateTime.now().plusDays(10), // valid end date
            jurisdiction,
            expectedHasValidAuth,
            "valid auth end date, multiple auth service codes"
        );

        // EXPIRED END DATE
        assertHasValidAuthorisationChecker(
            List.of(serviceCode),
            LocalDateTime.now().minusDays(1), // expired end date
            jurisdiction,
            false, // as expired
            "expired auth end date, single auth service code"
        );

        assertHasValidAuthorisationChecker(
            List.of("XYZ1", "XYZ2", serviceCode),
            LocalDateTime.now().minusDays(10), // expired end date
            jurisdiction,
            false, // as expired
            "expired auth end date, multiple auth service codes"
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

    private void assertAppointmentTypeCheckers(String appointmentType,
                                               boolean expectedIsFeePaid,
                                               boolean expectedIsSalaried,
                                               boolean expectedIsVoluntary) {

        // GIVEN
        JudicialAccessProfile judicialAccessProfile = JudicialAccessProfile.builder()
            .appointmentType(appointmentType)
            .build();

        // WHEN / THEN
        assertEquals(expectedIsFeePaid, judicialAccessProfile.isFeePaid(), "isFeePaid() check failed");
        assertEquals(expectedIsSalaried, judicialAccessProfile.isSalaried(), "isSalaried() check failed");
        assertEquals(expectedIsVoluntary, judicialAccessProfile.isVoluntary(), "isVoluntary() check failed");

    }

    private void assertEndDateChecker(ZonedDateTime dateTime, boolean expectedHasValidEndDate, String testDescription) {

        // GIVEN
        JudicialAccessProfile judicialAccessProfile = JudicialAccessProfile.builder()
            .endTime(dateTime)
            .build();

        // WHEN / THEN
        assertEquals(
            expectedHasValidEndDate,
            judicialAccessProfile.hasValidEndDate(),
            "hasValidEndDate() check failed for: " + testDescription
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
        log.info("Running HasValidAuthorisation test: " + testDescription);
        assertEquals(
            expectedHasValidAuth,
            judicialAccessProfile.hasValidAuthorisation(jurisdiction),
            "hasValidAuthorisation() check failed for: " + testDescription
        );

    }

    private void assertHasValidAuthorisationChecker(List<String> serviceCodes,
                                                    LocalDateTime dateTime,
                                                    Jurisdiction jurisdiction,
                                                    boolean expectedHasValidAuth,
                                                    String testDescription) {

        // GIVEN
        Authorisation testAuthorisation = Authorisation.builder()
            .serviceCodes(serviceCodes)
            .endDate(dateTime)
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
