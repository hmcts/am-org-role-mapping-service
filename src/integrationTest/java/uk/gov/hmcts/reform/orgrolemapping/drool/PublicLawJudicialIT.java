package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArguments;
import uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.adjustTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.cloneListOfTestArgumentsForMultiRegion;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardFeePaidTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardSalariedTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardVoluntaryTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_01_LONDON;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_02_MIDLANDS;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_05_SOUTH_EAST;

public class PublicLawJudicialIT {

    private static final String SENIOR_SALARIED_JUDGE_OUTPUT_TEMPLATE = "Senior_Salaried_Judge";
    private static final String SALARIED_JUDGE_OUTPUT_TEMPLATE = "Salaried_Judge";
    private static final String FEE_PAID_JUDGE_OUTPUT_TEMPLATE = "FeePaid_Judge";
    private static final String MAGISTRATE_OUTPUT_TEMPLATE = "Magistrate";

    @SuppressWarnings({"LineLength"})
    public static List<DroolJudicialTestArguments> getTestArguments() {

        // PublicLaw special tests:
        // * multi region tests for 1 & 5 for all salaried judges

        List<DroolJudicialTestArguments> arguments = new ArrayList<>();

        // 001 Circuit Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "001_Circuit_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );

        // 002 Deputy Circuit Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "002_Deputy_Circuit_Judge__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 003 Recorder - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "003_Recorder__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 004 Deputy District Judge - PRFD - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "004_Deputy_District_Judge-PRFD__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 005 Deputy District Judge (MC) - Fee paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "005_Deputy_District_Judge-(MC)__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 006 Deputy District Judge (MC) - Sitting in Retirement - Fee paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "006_Deputy_District_Judge-(MC)_Sitting_in_Retirement__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 007 Deputy District Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "007.1_Deputy_District_Judge-Fee-Paid__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "007.2_Deputy_District_Judge__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 008 Deputy District Judge - Sitting in Retirement - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "008.1_Deputy_District_Judge_Sitting_in_Retirement__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "008.2_Deputy_District_Judge_(sitting_in_retirement)__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 009 Deputy High Court Judge - Fee paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "009_Deputy_High_Court_Judge__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 010 District Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "010_District_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );

        // 011 District Judge (MC) - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "011_District_Judge-(MC)__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );

        // 012 High Court Judge / President of the Family Division - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "012.1_High_Court_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );
        arguments.addAll(
            generateSalariedTestArguments(
                "012.2_President_of_the_Family_Division__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );

        // 013 High Court Judge - Sitting in Retirement - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "013.1_High_Court_Judge_Sitting-in-Retirement__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "013.2_High_Court_Judge_(sitting-in-retirement)__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 014 Designated Family Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "014_Designated_Family_Judge__Salaried",
                SENIOR_SALARIED_JUDGE_OUTPUT_TEMPLATE,
                true // additional role test
            )
        );

        // 014b Acting Designated Family Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "014b_Acting_Designated_Family_Judge__Salaried",
                SENIOR_SALARIED_JUDGE_OUTPUT_TEMPLATE,
                true // additional role test
            )
        );

        // 015 Magistrate - Voluntary
        arguments.addAll(
            generateStandardVoluntaryTestArguments(
                "015_Magistrate__Voluntary",
                MAGISTRATE_OUTPUT_TEMPLATE,
                false // NB: bookings have no effect on this scenario
            )
        );

        // 016 Tribunal Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "016_Tribunal_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );

        // 017 Employment Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "017_Employment_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );

        // 018 Specialist Circuit Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "018_Specialist_Circuit_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );

        // 019 Senior Circuit Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "019_Senior_Circuit_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );

        // 020 Circuit Judge (sitting in retirement) - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "020_Circuit_Judge_(sitting_in_retirement)__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 021 Recorder (sitting in retirement) - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "021_Recorder_(sitting_in_retirement)__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 022 Deputy Upper Tribunal Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "022_Deputy_Upper_Tribunal_Judge__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 023 District Judge (MC) (sitting_in_retirement) - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "023_District_Judge_(MC)_(sitting_in_retirement)__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 024 District Judge (sitting in retirement) - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "024_District_Judge_(sitting_in_retirement)__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // adjust test arguments ready for use
        return adjustTestArguments(arguments, "PublicLaw");
    }


    private static List<DroolJudicialTestArguments> generateSalariedTestArguments(String jrdResponseFileName,
                                                                                  String rasRequestFileName,
                                                                                  boolean additionalRoleTest) {

        List<DroolJudicialTestArguments> arguments = generateStandardSalariedTestArguments(
            jrdResponseFileName,
            rasRequestFileName,
            additionalRoleTest,
            additionalRoleTest
                    ? SALARIED_JUDGE_OUTPUT_TEMPLATE // allow AR-Expired fallback to standard salaried template
                    : null 
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

}
