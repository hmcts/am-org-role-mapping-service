package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArgumentOverrides;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArguments;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.FeatureFlagEnum;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.adjustTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateOverrideFlagOffCatchAll;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardSalariedTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.overrideTestArguments;

public class IacJudicialIT {

    private static final String LEADERSHIP_JUDGE_OUTPUT_TEMPLATE = "Leadership_Salaried_Judge";
    private static final String SALARIED_JUDGE_OUTPUT_TEMPLATE = "Salaried_Judge";

    @SuppressWarnings({"LineLength"})
    public static List<DroolJudicialTestArguments> getTestArguments() {

        // IAC special tests:
        // * Additional role scenario will use fallback to standard salaried output when additional role is expired

        List<DroolJudicialTestArguments> arguments = new ArrayList<>();

        // 002 Resident Tribunal Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "002_Resident_Tribunal_Judge__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 004 Acting Resident Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "004_Acting_Resident_Judge__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE,
                SALARIED_JUDGE_OUTPUT_TEMPLATE // allow AR-Expired fallback to standard salaried template
            )
        );


        // generate extra flag off tests for IAC_WA_1_6
        arguments.addAll(flagOffTestsIacWa16(arguments));


        // adjust test arguments ready for use
        return adjustTestArguments(arguments, "IAC");
    }

    private static List<DroolJudicialTestArguments> flagOffTestsIacWa16(
        List<DroolJudicialTestArguments> inputArguments
    ) {
        List<DroolJudicialTestArgumentOverrides> testOverrides = new ArrayList<>();
        FeatureFlagEnum flag = FeatureFlagEnum.IAC_WA_1_6;

        // the following scenarios will default back to Salaried_Judge when flag is off
        testOverrides.add(DroolJudicialTestArgumentOverrides.builder()
            .overrideDescription("Default_back_to_Salaried_Judge")
            .findJrdResponseFileName("002_Resident_Tribunal_Judge__Salaried")
            .overrideRasRequestFileNameWithoutBooking(SALARIED_JUDGE_OUTPUT_TEMPLATE)
            .overrideRasRequestFileNameWithBooking(SALARIED_JUDGE_OUTPUT_TEMPLATE)
            .overrideTurnOffFlags(List.of(flag))
            .build()
        );
        testOverrides.add(DroolJudicialTestArgumentOverrides.builder()
            .overrideDescription("Default_back_to_Salaried_Judge")
            .findJrdResponseFileName("004_Acting_Resident_Judge__Salaried")
            .overrideRasRequestFileNameWithoutBooking(SALARIED_JUDGE_OUTPUT_TEMPLATE)
            .overrideRasRequestFileNameWithBooking(SALARIED_JUDGE_OUTPUT_TEMPLATE)
            .overrideTurnOffFlags(List.of(flag))
            .build()
        );

        // must use a catch-all override to run all unaffected tests with the flag off
        testOverrides.add(generateOverrideFlagOffCatchAll(flag));

        return overrideTestArguments(inputArguments, testOverrides);
    }

}
