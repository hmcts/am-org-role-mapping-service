package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.FEE_PAID;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.SALARIED;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.SPTW;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.VOLUNTARY;

class JudicialOfficeHolderTest {

    @Test
    void testContractTypeCheckers() {

        // NB: JOH.ContractType value populated from `JudicialAccessProfile.appointmentType`
        assertContractTypeCheckers(FEE_PAID, true, false, false);
        assertContractTypeCheckers(SALARIED, false, true, false);
        assertContractTypeCheckers(SPTW, false, true, false);
        assertContractTypeCheckers(VOLUNTARY, false, false, true);

        // test when an unexpected value is set
        assertContractTypeCheckers("unexpected", false, false, false);

        // test when null/empty value is set
        assertContractTypeCheckers(null, false, false, false);
        assertContractTypeCheckers("", false, false, false);
    }

    private void assertContractTypeCheckers(String contractType,
                                            boolean expectedIsFeePaid,
                                            boolean expectedIsSalaried,
                                            boolean expectedIsVoluntary) {

        // GIVEN
        JudicialOfficeHolder judicialOfficeHolder = JudicialOfficeHolder.builder()
            .contractType(contractType)
            .build();

        // WHEN / THEN
        assertEquals(expectedIsFeePaid, judicialOfficeHolder.isFeePaid(), "isFeePaid() check failed");
        assertEquals(expectedIsSalaried, judicialOfficeHolder.isSalaried(), "isSalaried() check failed");
        assertEquals(expectedIsVoluntary, judicialOfficeHolder.isVoluntary(), "isVoluntary() check failed");

    }

}
