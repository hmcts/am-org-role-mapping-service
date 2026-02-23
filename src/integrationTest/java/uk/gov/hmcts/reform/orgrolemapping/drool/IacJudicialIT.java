package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArgumentOverrides;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArguments;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.FeatureFlagEnum;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.orgrolemapping.drool.BaseDroolTestIntegration.NO_BOOKABLE_ROLES_FLAG;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.adjustTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateOverrideFlagOffCatchAll;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardFeePaidTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardSalariedTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.overrideTestArguments;

public class IacJudicialIT {

    private static final String FEE_PAID_JUDGE_OUTPUT_TEMPLATE = "FeePaid_Judge";
    private static final String LEADERSHIP_JUDGE_OUTPUT_TEMPLATE = "Leadership_Salaried_Judge";
    private static final String SALARIED_JUDGE_OUTPUT_TEMPLATE = "Salaried_Judge";
    private static final String SENIOR_JUDGE_OUTPUT_TEMPLATE = "Senior_Salaried_Judge";
    private static final String SENIOR_AND_LEADERSHIP_JUDGE_OUTPUT_TEMPLATE = "Senior_and_Leadership_Salaried_Judge";

    @SuppressWarnings({"LineLength"})
    public static List<DroolJudicialTestArguments> getTestArguments() {

        // IAC special tests:
        // * Additional role scenario will use fallback to standard salaried output when additional role is expired

        List<DroolJudicialTestArguments> arguments = new ArrayList<>();

        // 001 President of Tribunal - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "001_President_of_Tribunal__Salaried",
                SENIOR_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 002 Resident Tribunal Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "002_Resident_Tribunal_Judge__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 003 Resident Immigration Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "003_Resident_Immigration_Judge__Salaried",
                SENIOR_AND_LEADERSHIP_JUDGE_OUTPUT_TEMPLATE,
                SALARIED_JUDGE_OUTPUT_TEMPLATE // allow AR-Expired fallback to standard salaried template
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

        // 005 Assistant Resident Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "005_Assistant_Resident_Judge__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE,
                SALARIED_JUDGE_OUTPUT_TEMPLATE // allow AR-Expired fallback to standard salaried template
            )
        );

        // 006 Designated Immigration Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "006_Designated_Immigration_Judge__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE,
                SALARIED_JUDGE_OUTPUT_TEMPLATE // allow AR-Expired fallback to standard salaried template
            )
        );

        // 007 Tribunal Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "007_Tribunal_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 008 Tribunal Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "008_Tribunal_Judge__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE,
                false,
                NO_BOOKABLE_ROLES_FLAG
            )
        );

        // 009 Any Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "009_Any_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 010 Any Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "010_Any_Judge__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE,
                false,
                NO_BOOKABLE_ROLES_FLAG
            )
        );


        // adjust all tests to use temporary work around for Appointment Expired fallback (see DTSAM-1204)
        arguments = adjustAllTestsToUseAppointmentExpiredFallback(arguments);


        // generate extra flag off tests for IAC_WA_1_6
        arguments.addAll(flagOffTestsIacWa16(arguments));


        // adjust test arguments ready for use
        return adjustTestArguments(arguments, "IAC");
    }

    private static List<DroolJudicialTestArguments> adjustAllTestsToUseAppointmentExpiredFallback(
        List<DroolJudicialTestArguments> inputArguments
    ) {
        // temporary work around as existing IAC drool rules do not handle Authorisation End Date expiry correctly
        // see DTSAM-1204
        return new ArrayList<>(inputArguments.stream()
            .map(argument -> {
                // fallback to normal template when running Authorisation Expired scenario
                String authorisationExpiredFallbackFileName = argument.getRasRequestFileNameWithoutBooking();

                // fallback to generic role for some appointments/additional-roles
                if ("002_Resident_Tribunal_Judge__Salaried".equals(argument.getJrdResponseFileName())
                    || "004_Acting_Resident_Judge__Salaried".equals(argument.getJrdResponseFileName())) {
                    authorisationExpiredFallbackFileName = SALARIED_JUDGE_OUTPUT_TEMPLATE;
                }

                return argument.cloneBuilder()
                    .authorisationExpiredFallbackFileName(authorisationExpiredFallbackFileName)
                    .build();
            })
            .toList());
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
