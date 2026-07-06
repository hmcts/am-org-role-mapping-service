package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArgumentOverrides;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArguments;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.orgrolemapping.drool.BaseDroolTestIntegration.EMPTY_ROLE_ASSIGNMENT_TEMPLATE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.adjustTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.cloneListOfTestArgumentsForMultiRegion;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardFeePaidTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardSalariedTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.overrideTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_01_LONDON;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_02_MIDLANDS;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_05_SOUTH_EAST;

public class FrJudicialIT {

    private static final String LEADERSHIP_JUDGE_OUTPUT_TEMPLATE = "Salaried_Leadership_Judge";
    private static final String SALARIED_JUDGE_OUTPUT_TEMPLATE = "Salaried_Judge";
    private static final String FEE_PAID_JUDGE_OUTPUT_TEMPLATE = "FeePaid_Judge";

    public static List<DroolJudicialTestArguments> getTestArguments() {

        // Financial Remedy special tests:
        // * generic role mappings present: so Additional Role tests will use a fallback when AR is expired.

        List<DroolJudicialTestArguments> arguments = new ArrayList<>();


        // 001 Generic - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "001_Generic_Judge__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 002 Generic - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "002_Generic_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 003 Generic Leadership Judge - Salaried
        // 003.01 Additional Role: Lead Financial Remedy Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "003.01_AR__Lead_Financial_Remedy_Judge__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE,
                SALARIED_JUDGE_OUTPUT_TEMPLATE // allow AR-Expired fallback to standard salaried template
            )
        );
        // 003.02 Circuit Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "003.02_Circuit_Judge__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE
            )
        );
        // 003.03 High Court Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "003.03_High_Court_Judge__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE
            )
        );
        // 003.04 District Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "003.04_District_Judge__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // FlagOff Tests
        arguments.addAll(flagOffTestsFrWa10(arguments));


        // adjust test arguments ready for use
        return adjustTestArguments(arguments, "FR");

    }


    private static List<DroolJudicialTestArguments> flagOffTestsFrWa10(
        List<DroolJudicialTestArguments> inputArguments
    ) {
        List<DroolJudicialTestArgumentOverrides> testOverrides = new ArrayList<>();
        FeatureFlagEnum flag = FeatureFlagEnum.FR_WA_1_0;

        // override everything with Empty template as FR not supported with this flag off
        testOverrides.add(DroolJudicialTestArgumentOverrides.builder()
            .overrideDescription("FR_not_supported_without_flag")
            .overrideRasRequestFileNameWithoutBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
            .overrideRasRequestFileNameWithBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
            .overrideAdditionalRoleExpiredFallbackFileName(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
            .overrideTurnOffFlags(List.of(flag))
            .build()
        );

        return overrideTestArguments(inputArguments, testOverrides);
    }

}
