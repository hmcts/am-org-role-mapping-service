package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArguments;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.*;

public class StcicJudicialIT {
    public static List<DroolJudicialTestArguments> getTestArguments() {


        List<DroolJudicialTestArguments> arguments = new ArrayList<>();

        // 001 President of Tribunals - Salaried
        arguments.addAll(
                generateStandardSalariedTestArguments(
                        "001_President_of_Tribunal__Salaried",
                        "001_PT__002_PJ"
                )
        );
        // 002 Principal Judge - Salaried
        arguments.addAll(
                generateStandardSalariedTestArguments(
                        "001_Principal_Judge__Salaried",
                        "001_PT__002_PJ"
                )
        );

        // 003 Tribunal_Judge - Fee Paid
        arguments.addAll(
                generateStandardFeePaidTestArguments(
                        "003_Tribunal_Judge__FeePaid",
                        "003_TJ__015_JFT__014_C__017_R__013_DUTJ"
                        //
                )
        );
        // 004 Tribunal_Judge - Salaried
        arguments.addAll(
                generateStandardSalariedTestArguments(
                        "004_Tribunal_Judge__Salaried",
                        "004_TJ__019_JFT__018_CJ__020_RTJ"
                )
        );
        // 005 Tribunal_Member - Fee Paid
        arguments.addAll(
                generateStandardFeePaidTestArguments(
                        "005_Tribunal_Member__FeePaid",
                        "005_TM__006_TML"
                )
        );
        // 006 Tribunal Member Lay - Fee Paid
        arguments.addAll(
                generateStandardFeePaidTestArguments(
                        "006_Tribunal_Member_Lay__FeePaid",
                        "005_TM__006_TML"
                )
        );
        // 007 Tribunal Member Medical - Salaried
        arguments.addAll(
                generateStandardSalariedTestArguments(
                        "007_Tribunal_Member_Medical__Salaried",
                        "008_TMM__011_TMO"
                )
        );
        // 008 Tribunal Member Medical - Fee Paid
        arguments.addAll(
                generateStandardFeePaidTestArguments(
                        "008_Tribunal_Member_Medical__FeePaid",
                        "008_TMM__011_TMO"
                )
        );
        // 009 Tribunal Member Disability - Fee Paid
        arguments.addAll(
                generateStandardFeePaidTestArguments(
                               "008_Tribunal_Member_Medical__FeePaid",
                                "008_TMM__011_TMO"
                )
        );
        // 010 Tribunal Member Financially Qualified - Fee Paid
        arguments.addAll(
                generateStandardFeePaidTestArguments(
                        "008_Tribunal_Member_Medical__FeePaid",
                        "008_TMM__011_TMO"
                )
        );
        // 011 Tribunal Member Optometrist - Fee Paid
        arguments.addAll(
                generateStandardFeePaidTestArguments(
                        "008_Tribunal_Member_Medical__FeePaid",
                        "008_TMM__011_TMO"
                )
        );
        // 012 Advisory Committee Member - Magistrate - Voluntary
        arguments.addAll(
                generateStandardFeePaidTestArguments(
                        "012_Tribunal_Member_Medical__FeePaid",
                        "008_TMM__011_TMO"
                )
        );
        // 013 Deputy Upper Tribunal Judge - Fee Paid
        arguments.addAll(
                generateStandardFeePaidTestArguments(
                        "013_Tribunal_Member_Medical__FeePaid",
                        "008_TMM__011_TMO"
                )
        );
        // 014 Chairman - Fee Paid
        arguments.addAll(
                generateStandardFeePaidTestArguments(
                        "014_Tribunal_Member_Medical__FeePaid",
                        "008_TMM__011_TMO"
                )
        );
        // 015 Judge of the First-tier Tribunal (sitting in retirement)     - Fee Paid
        arguments.addAll(
                generateStandardFeePaidTestArguments(
                        "015_Tribunal_Member_Medical__FeePaid",
                        "008_TMM__011_TMO"
                )
        );
        // 016 Member of the First-tier Tribunal (sitting in retirement) - Fee Paid
        arguments.addAll(
                generateStandardFeePaidTestArguments(
                        "016_Tribunal_Member_Medical__FeePaid",
                        "008_TMM__011_TMO"
                )
        );
        // 017 Recorder - Fee Paid
        arguments.addAll(
                generateStandardFeePaidTestArguments(
                        "017_Tribunal_Member_Medical__FeePaid",
                        "008_TMM__011_TMO"
                )
        );
        // 018 Circuit Judge - Salaried
        arguments.addAll(
                generateStandardFeePaidTestArguments(
                        "018_Tribunal_Member_Medical__FeePaid",
                        "008_TMM__011_TMO"
                )
        );
        // 019 Judge of the First-tier Tribunal - Salaried
        arguments.addAll(
                generateStandardFeePaidTestArguments(
                        "019_Tribunal_Member_Medical__FeePaid",
                        "008_TMM__011_TMO"
                )
        );
        // 020 Regional Tribunal Judge - Salaried
        arguments.addAll(
                generateStandardFeePaidTestArguments(
                        "020_Tribunal_Member_Medical__FeePaid",
                        "008_TMM__011_TMO"
                )
        );
        // 021 Magistrate - Voluntary
        arguments.addAll(
                generateStandardFeePaidTestArguments(
                        "021_Tribunal_Member_Medical__FeePaid",
                        "008_TMM__011_TMO"
                )
        );
        // adjust test arguments ready for use
        return adjustTestArguments(arguments, "StCIC");
    }


}
