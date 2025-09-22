package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.FEE_PAID;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.SALARIED;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.SPTW;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.VOLUNTARY;

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

}
