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

public class PossessionsJudicialIT {

    private static final String LEADERSHIP_JUDGE_OUTPUT_TEMPLATE = "Salaried_Leadership_Judge";
    private static final String SALARIED_CIRCUIT_JUDGE_OUTPUT_TEMPLATE = "Salaried_Circuit_Judge";
    private static final String SALARIED_JUDGE_OUTPUT_TEMPLATE = "Salaried_Judge";
    private static final String FEE_PAID_CIRCUIT_JUDGE_OUTPUT_TEMPLATE = "FeePaid_Circuit_Judge";
    private static final String FEE_PAID_JUDGE_OUTPUT_TEMPLATE = "FeePaid_Judge";

    public static List<DroolJudicialTestArguments> getTestArguments() {

        // Possessions special tests:
        // * multi region tests for 1 & 5 for all salaried judges
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
            generateSalariedTestArguments(
                "002_Generic_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );


        // 003 Generic Circuit Judge - Salaried
        // 003.1 Circuit Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "003.1_Circuit_Judge__Salaried",
                SALARIED_CIRCUIT_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );
        // 003.2 Circuit Judge Central Criminal Court - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "003.2_Circuit_Judge_Central_Criminal_Court__Salaried",
                SALARIED_CIRCUIT_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );
        // 003.3 Court of Appeal Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "003.3_Court_of_Appeal_Judge__Salaried",
                SALARIED_CIRCUIT_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );
        // 003.4 High Court Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "003.4_High_Court_Judge__Salaried",
                SALARIED_CIRCUIT_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );
        // 003.5 Senior Circuit Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "003.5_Senior_Circuit_Judge__Salaried",
                SALARIED_CIRCUIT_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );
        // 003.6 Specialist Circuit Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "003.6_Specialist_Circuit_Judge__Salaried",
                SALARIED_CIRCUIT_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );
        // 003.7 Additional Role: Presiding Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "003.7_AR__Presiding_Judge__Salaried",
                SALARIED_CIRCUIT_JUDGE_OUTPUT_TEMPLATE,
                true
            )
        );


        // 004 Generic Circuit Judge - Fee Paid
        // 004.1 Circuit Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "004.1_Circuit_Judge__FeePaid",
                FEE_PAID_CIRCUIT_JUDGE_OUTPUT_TEMPLATE
            )
        );
        // 004.2 Circuit Judge (sitting in retirement) - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "004.2_Circuit_Judge_(sitting_in_retirement)__FeePaid",
                FEE_PAID_CIRCUIT_JUDGE_OUTPUT_TEMPLATE
            )
        );
        // 004.3 Deputy Circuit Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "004.3_Deputy_Circuit_Judge__FeePaid",
                FEE_PAID_CIRCUIT_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 005 Generic Circuit Judge - Salaried
        // 005.1 Chief Insolvency and Companies Court Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "005.1_Chief_Insolvency_and_Companies_Court_Judge__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );
        // 005.2 Chief Master - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "005.2_Chief_Master__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );
        // 005.3 Deputy Chamber President - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "005.3_Deputy_Chamber_President__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );
        // 005.4 Master of the Rolls - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "005.4_Master_of_the_Rolls__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );
        // 005.5 Regional Employment Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "005.5_Regional_Employment_Judge__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );
        // 005.6 Senior Master - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "005.6_Senior_Master__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );
        // 005.7 Additional Role: Designated Civil Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "005.7_AR__Designated_Civil_Judge__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE,
                true
            )
        );
        // 005.8 Additional Role: Acting Designated Civil Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "005.8_AR__Acting_Designated_Civil_Judge__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE,
                true
            )
        );

        // FlagOff Tests
        arguments.addAll(flagOffTestsPossessionsWa10(arguments));

        // adjust test arguments ready for use
        return adjustTestArguments(arguments, "Possessions");
    }


    private static List<DroolJudicialTestArguments> generateSalariedTestArguments(String jrdResponseFileName,
                                                                                  String rasRequestFileName,
                                                                                  boolean additionalRoleTest) {

        List<DroolJudicialTestArguments> arguments = generateStandardSalariedTestArguments(
            jrdResponseFileName,
            rasRequestFileName,
            additionalRoleTest // if additional role test...
                ? SALARIED_JUDGE_OUTPUT_TEMPLATE // then allow AR-Expired fallback to standard salaried template
                : null // else no fallback required
        );

        arguments = cloneListOfTestArgumentsForMultiRegion(
            arguments,
            List.of(REGION_02_MIDLANDS), // any single region
            List.of(REGION_01_LONDON, REGION_05_SOUTH_EAST) // multi-regions
        );

        // expand to include additional tests for SPTW
        arguments.addAll(
            DroolJudicialTestArgumentsHelper.cloneListOfSalariedTestArgumentsForSptw(arguments)
        );

        return arguments;
    }


    private static List<DroolJudicialTestArguments> flagOffTestsPossessionsWa10(
        List<DroolJudicialTestArguments> inputArguments
    ) {
        List<DroolJudicialTestArgumentOverrides> testOverrides = new ArrayList<>();
        FeatureFlagEnum flag = FeatureFlagEnum.POSSESSIONS_WA_1_0;

        // override everything with Empty template as Possessions not supported with this flag off
        testOverrides.add(DroolJudicialTestArgumentOverrides.builder()
            .overrideDescription("Possessions_not_supported_without_flag")
            .overrideRasRequestFileNameWithoutBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
            .overrideRasRequestFileNameWithBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
            .overrideAdditionalRoleExpiredFallbackFileName(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
            .overrideTurnOffFlags(List.of(flag))
            .build()
        );

        return overrideTestArguments(inputArguments, testOverrides);
    }

}
