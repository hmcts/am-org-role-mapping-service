package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArgumentOverrides;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArguments;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.FeatureFlagEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.orgrolemapping.drool.BaseDroolTestIntegration.EMPTY_ROLE_ASSIGNMENT_TEMPLATE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.*;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.overrideTestArguments;

public class StcicJudicialIT {


    private static final String LEADERSHIP_JUDGE_OUTPUT_TEMPLATE
        = "01_Salaried_Leadership_Judge";
    private static final String FEE_PAID_JUDGE_OUTPUT_TEMPLATE
        = "03_FeePaid_Judge";
    private static final String FEE_PAID_TRIBUNAL_MEMBER_OUTPUT_TEMPLATE
        = "04_FeePaid_Tribunal_Member";
    private static final String FEE_PAID_TRIBUNAL_MEMBER_DISABILITY_OUTPUT_TEMPLATE
        = "08_FeePaid_Tribunal_Member_Disability";
    private static final String FEE_PAID_TRIBUNAL_MEMBER_FINANCIAL_OUTPUT_TEMPLATE
        = "09_FeePaid_Tribunal_Member_Financial";
    private static final String FEE_PAID_TRIBUNAL_MEMBER_MEDICAL_OUTPUT_TEMPLATE
        = "07_FeePaid_Tribunal_Member_Medical";
    private static final String SALARIED_JUDGE_OUTPUT_TEMPLATE
        = "02_Salaried_Judge";
    private static final String SALARIED_TRIBUNAL_MEMBER_MEDICAL_OUTPUT_TEMPLATE
        = "06_Salaried_Tribunal_Member_Medical";
    private static final String MAGISTRATE_OUTPUT_TEMPLATE
        = "05_Magistrate";


    public static List<DroolJudicialTestArguments> getTestArguments() {

        List<DroolJudicialTestArguments> arguments = new ArrayList<>();


        // 001 President of Tribunal - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "001_President_of_Tribunal__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 002 Principal Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "002_Principal_Judge__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 003 Tribunal Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "003_Tribunal_Judge__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 004 Tribunal Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "004_Tribunal_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 005 Tribunal Member - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "005_Tribunal_Member__FeePaid",
                FEE_PAID_TRIBUNAL_MEMBER_OUTPUT_TEMPLATE,
                false,
                false // NB: bookings have no effect on this scenario
            )
        );

        // 006 Tribunal Member Lay - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "006_Tribunal_Member_Lay__FeePaid",
                FEE_PAID_TRIBUNAL_MEMBER_OUTPUT_TEMPLATE,
                false,
                false // NB: bookings have no effect on this scenario
            )
        );

        // 007 Tribunal Member Medical - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "007_Tribunal_Member_Medical__Salaried",
                SALARIED_TRIBUNAL_MEMBER_MEDICAL_OUTPUT_TEMPLATE
            )
        );

        // 008 Tribunal Member Medical - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "008_Tribunal_Member_Medical__FeePaid",
                FEE_PAID_TRIBUNAL_MEMBER_MEDICAL_OUTPUT_TEMPLATE,
                false,
                false // NB: bookings have no effect on this scenario
            )
        );

        // 009 Tribunal Member Disability - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "009_Tribunal_Member_Disability__FeePaid",
                FEE_PAID_TRIBUNAL_MEMBER_DISABILITY_OUTPUT_TEMPLATE,
                false,
                false // NB: bookings have no effect on this scenario
            )
        );

        // 010 Tribunal Member Financially Qualified - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "010_Tribunal_Member_Financially_Qualified__FeePaid",
                FEE_PAID_TRIBUNAL_MEMBER_FINANCIAL_OUTPUT_TEMPLATE,
                false,
                false // NB: bookings have no effect on this scenario
            )
        );

        // 011 Tribunal Member Optometrist - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "011_Tribunal_Member_Optometrist__FeePaid",
                FEE_PAID_TRIBUNAL_MEMBER_MEDICAL_OUTPUT_TEMPLATE,
                false,
                false // NB: bookings have no effect on this scenario
            )
        );

        // 012 Advisory Committee Member - Magistrate - Voluntary
        arguments.addAll(
            generateStandardVoluntaryTestArguments(
                "012_Advisory_Committee_Member-Magistrate__Voluntary",
                MAGISTRATE_OUTPUT_TEMPLATE,
                false // NB: bookings have no effect on this scenario
            )
        );

        // 013 Deputy Upper Tribunal Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "013_Deputy_Upper_Tribunal_Judge__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 014 Chairman - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "014_Chairman__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 015 Judge of the First-tier Tribunal (sitting in retirement) - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "015_Judge_of_the_First-tier_Tribunal_(sitting_in_retirement)__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 016 Member of the First-tier Tribunal (sitting in retirement) - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "016_Member_of_the_First-tier_Tribunal_(sitting_in_retirement)__FeePaid",
                FEE_PAID_TRIBUNAL_MEMBER_DISABILITY_OUTPUT_TEMPLATE,
                false,
                false // NB: bookings have no effect on this scenario
            )
        );

        // 017 Recorder - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "017_Recorder__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 018 Circuit Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "018_Circuit_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 019 Judge of the First-tier Tribunal - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "019_Judge_of_the_First-tier_Tribunal__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 020 Regional Tribunal Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "020_Regional_Tribunal_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 021 Magistrate - Voluntary
        arguments.addAll(
            generateStandardVoluntaryTestArguments(
                "021_Magistrate__Voluntary",
                MAGISTRATE_OUTPUT_TEMPLATE,
                false // NB: bookings have no effect on this scenario
            )
        );

        // generate extra flag off tests for ST_CIC_WA_1_3
        arguments.addAll(flagOffTestsStcicWa13(arguments));


        // adjust test arguments ready for use
        return adjustTestArguments(arguments, "StCIC");
    }

    private static List<DroolJudicialTestArguments> flagOffTestsStcicWa13(
            List<DroolJudicialTestArguments> inputArguments
    ) {
        List<DroolJudicialTestArgumentOverrides> testOverrides = new ArrayList<>();
        FeatureFlagEnum flag = FeatureFlagEnum.ST_CIC_WA_1_3;

        // TribunalMembers worktypes changes  prior to DTSAM-1146
        testOverrides.add(DroolJudicialTestArgumentOverrides.builder()
                .overrideDescription("NoWorkTypes")
                .findJrdResponseFileName("005_Tribunal_Member__FeePaid")
                .overrideRasRequestFileNameWithoutBooking("FlagOff_STCIC_WA_1_3/" +
                        FEE_PAID_TRIBUNAL_MEMBER_OUTPUT_TEMPLATE)
                .overrideRasRequestFileNameWithBooking("FlagOff_STCIC_WA_1_3/" +
                        FEE_PAID_TRIBUNAL_MEMBER_OUTPUT_TEMPLATE)
                .overrideTurnOffFlags(List.of(flag))
                .build()
        );

        // TribunalMembers Lay worktypes changes  prior to DTSAM-1146
        testOverrides.add(DroolJudicialTestArgumentOverrides.builder()
                .overrideDescription("NoWorkTypes")
                .findJrdResponseFileName("006_Tribunal_Member_Lay__FeePaid")
                .overrideRasRequestFileNameWithoutBooking("FlagOff_STCIC_WA_1_3/" +
                        FEE_PAID_TRIBUNAL_MEMBER_OUTPUT_TEMPLATE)
                .overrideRasRequestFileNameWithBooking("FlagOff_STCIC_WA_1_3/" +
                        FEE_PAID_TRIBUNAL_MEMBER_OUTPUT_TEMPLATE)
                .overrideTurnOffFlags(List.of(flag))
                .build()
        );

        // TribunalMembers Medical worktypes changes  prior to DTSAM-1146
        testOverrides.add(DroolJudicialTestArgumentOverrides.builder()
                .overrideDescription("NoWorkTypes")
                .findJrdResponseFileName("007_Tribunal_Member_Medical__Salaried")
                .overrideRasRequestFileNameWithoutBooking("FlagOff_STCIC_WA_1_3/" +
                        SALARIED_TRIBUNAL_MEMBER_MEDICAL_OUTPUT_TEMPLATE)
                .overrideRasRequestFileNameWithBooking("FlagOff_STCIC_WA_1_3/" +
                        SALARIED_TRIBUNAL_MEMBER_MEDICAL_OUTPUT_TEMPLATE)
                .overrideTurnOffFlags(List.of(flag))
                .build()
        );

        // TribunalMembers Medical worktypes changes  prior to DTSAM-1146
        testOverrides.add(DroolJudicialTestArgumentOverrides.builder()
                .overrideDescription("NoWorkTypes")
                .findJrdResponseFileName("008_Tribunal_Member_Medical__FeePaid")
                .overrideRasRequestFileNameWithoutBooking("FlagOff_STCIC_WA_1_3/" +
                        FEE_PAID_TRIBUNAL_MEMBER_MEDICAL_OUTPUT_TEMPLATE)
                .overrideRasRequestFileNameWithBooking("FlagOff_STCIC_WA_1_3/" +
                        FEE_PAID_TRIBUNAL_MEMBER_MEDICAL_OUTPUT_TEMPLATE)
                .overrideTurnOffFlags(List.of(flag))
                .build()
        );

        // TribunalMembers Disability worktypes changes  prior to DTSAM-1146
        testOverrides.add(DroolJudicialTestArgumentOverrides.builder()
                .overrideDescription("NoWorkTypes")
                .findJrdResponseFileName("009_Tribunal_Member_Disability__FeePaid")
                .overrideRasRequestFileNameWithoutBooking("FlagOff_STCIC_WA_1_3/" +
                        FEE_PAID_TRIBUNAL_MEMBER_DISABILITY_OUTPUT_TEMPLATE)
                .overrideRasRequestFileNameWithBooking("FlagOff_STCIC_WA_1_3/" +
                        FEE_PAID_TRIBUNAL_MEMBER_DISABILITY_OUTPUT_TEMPLATE)
                .overrideTurnOffFlags(List.of(flag))
                .build()
        );

        // TribunalMembers Financially Qualified worktypes changes  prior to DTSAM-1146
        testOverrides.add(DroolJudicialTestArgumentOverrides.builder()
                .overrideDescription("NoWorkTypes")
                .findJrdResponseFileName("010_Tribunal_Member_Financially_Qualified__FeePaid")
                .overrideRasRequestFileNameWithoutBooking("FlagOff_STCIC_WA_1_3/" +
                        FEE_PAID_TRIBUNAL_MEMBER_FINANCIAL_OUTPUT_TEMPLATE)
                .overrideRasRequestFileNameWithBooking("FlagOff_STCIC_WA_1_3/" +
                        FEE_PAID_TRIBUNAL_MEMBER_FINANCIAL_OUTPUT_TEMPLATE)
                .overrideTurnOffFlags(List.of(flag))
                .build()
        );

        // TribunalMembers Optometrist worktypes changes  prior to DTSAM-1146
        testOverrides.add(DroolJudicialTestArgumentOverrides.builder()
                .overrideDescription("NoWorkTypes")
                .findJrdResponseFileName("011_Tribunal_Member_Optometrist__FeePaid")
                .overrideRasRequestFileNameWithoutBooking("FlagOff_STCIC_WA_1_3/" +
                        FEE_PAID_TRIBUNAL_MEMBER_MEDICAL_OUTPUT_TEMPLATE)
                .overrideRasRequestFileNameWithBooking("FlagOff_STCIC_WA_1_3/" +
                        FEE_PAID_TRIBUNAL_MEMBER_MEDICAL_OUTPUT_TEMPLATE)
                .overrideTurnOffFlags(List.of(flag))
                .build()
        );

        // Member of the First-tier Tribunal (sitting in retirement) worktypes changes  prior to DTSAM-1146
        testOverrides.add(DroolJudicialTestArgumentOverrides.builder()
                .overrideDescription("NoWorkTypes")
                .findJrdResponseFileName("016_Member_of_the_First-tier_Tribunal_(sitting_in_retirement)__FeePaid")
                .overrideRasRequestFileNameWithoutBooking("FlagOff_STCIC_WA_1_3/" +
                        FEE_PAID_TRIBUNAL_MEMBER_DISABILITY_OUTPUT_TEMPLATE)
                .overrideRasRequestFileNameWithBooking("FlagOff_STCIC_WA_1_3/" +
                        FEE_PAID_TRIBUNAL_MEMBER_DISABILITY_OUTPUT_TEMPLATE)
                .overrideTurnOffFlags(List.of(flag))
                .build()
        );

        // must use a catch-all override to run all unaffected tests with the flag off
        testOverrides.add(generateOverrideFlagOffCatchAll(flag));

        return overrideTestArguments(inputArguments, testOverrides);
    }

}
