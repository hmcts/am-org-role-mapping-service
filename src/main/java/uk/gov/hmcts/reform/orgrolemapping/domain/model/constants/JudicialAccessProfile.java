package uk.gov.hmcts.reform.orgrolemapping.domain.model.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class JudicialAccessProfile {

    @UtilityClass
    public static final class AppointmentType {

        public static final String FEE_PAID = "Fee Paid";
        public static final String SALARIED = "Salaried";
        public static final String SPTW = "SPTW";
        public static final String VOLUNTARY = "Voluntary";

        public static boolean isFeePaid(String appointmentType) {
            return FEE_PAID.equalsIgnoreCase(appointmentType);
        }

        public static boolean isSalaried(String appointmentType) {
            return SALARIED.equalsIgnoreCase(appointmentType)
                || SPTW.equalsIgnoreCase(appointmentType);
        }

        public static boolean isVoluntary(String appointmentType) {
            return VOLUNTARY.equalsIgnoreCase(appointmentType);
        }

    }

}
