package uk.gov.hmcts.reform.orgrolemapping.domain.model.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class JudicialOfficeHolder {

    @UtilityClass
    public static final class Office {

        @UtilityClass
        public static final class Employment {

            // specific office values
            public static final String PRESIDENT_OF_TRIBUNAL_SALARIED
                = "EMPLOYMENT President of Tribunal-Salaried";
            public static final String PRESIDENT_ET_SCOTLAND_SALARIED
                = "EMPLOYMENT President Employment Tribunals (Scotland)-Salaried";
            public static final String VICE_PRESIDENT_SALARIED
                = "EMPLOYMENT Vice President-Salaried";
            public static final String REGIONAL_EMPLOYMENT_JUDGE_SALARIED
                = "EMPLOYMENT Regional Employment Judge-Salaried";

            // generic values
            public static final String EMPLOYMENT_JUDGE_SALARIED
                = "EMPLOYMENT Employment Judge-Salaried";
            public static final String EMPLOYMENT_JUDGE_FEE_PAID
                = "EMPLOYMENT Employment Judge-Fee-Paid";

            // tribunal member (group)
            public static final String TRIBUNAL_MEMBER_FEE_PAID
                = "EMPLOYMENT Tribunal Member-Fee-Paid";

            // additional roles
            public static final String ACTING_REGIONAL_EMPLOYMENT_JUDGE_SALARIED
                = "EMPLOYMENT Acting Regional Employment Judge-Salaried";

        }

        @UtilityClass
        public static final class IAC {
            // tribunal member (group)
            public static final String RESIDENT_OF_TRIBUNAL_JUDGE
                    = "IAC Resident Tribunal Judge";
            public static final String ACTING_RESIDENT_JUDGE
                    = "IAC Acting Resident Judge";


        }

    }

}
