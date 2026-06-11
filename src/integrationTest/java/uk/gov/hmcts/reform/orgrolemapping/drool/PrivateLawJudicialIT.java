package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArgumentOverrides;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArguments;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.adjustTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.cloneListOfTestArgumentsForMultiRegion;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.formatRasRequestFileNameWithSuffix;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateOverrideFlagOffCatchAll;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardFeePaidTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardSalariedTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardVoluntaryTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.overrideTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_01_LONDON;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_02_MIDLANDS;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_05_SOUTH_EAST;

public class PrivateLawJudicialIT {


    private static final String LEADERSHIP_JUDGE_OUTPUT_TEMPLATE = "Salaried_Leadership_Judge";
    private static final String SALARIED_JUDGE_OUTPUT_TEMPLATE = "Salaried_Judge";
    private static final String SALARIED_CIRCUIT_JUDGE_OUTPUT_TEMPLATE = "Salaried_Circuit_Judge";
    private static final String FEE_PAID_JUDGE_OUTPUT_TEMPLATE = "FeePaid_Judge";
    private static final String FEE_PAID_CIRCUIT_JUDGE_OUTPUT_TEMPLATE = "FeePaid_Circuit_Judge";
    private static final String MAGISTRATE_OUTPUT_TEMPLATE = "Magistrate";

    private static final String HEARING_ROLES_ONLY_OUTPUT_TEMPLATE = "HearingRolesOnly";

    public static List<DroolJudicialTestArguments> getTestArguments() {

        // PrivateLaw special tests:
        // * Additional Role tests will use a fallback when the Additional Role has expired as the hearing role
        //   mappings are based on any active Appointment + Authorisation being present
        // * multi region tests for 1 & 5 for all salaried judges

        List<DroolJudicialTestArguments> arguments = new ArrayList<>();


        // 001 Circuit Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "001_Circuit_Judge__Salaried",
                SALARIED_CIRCUIT_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );


        // 002 Deputy Circuit Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "002_Deputy_Circuit_Judge__FeePaid",
                FEE_PAID_CIRCUIT_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 003 Deputy District Judge - PRFD - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "003_Deputy_District_Judge-PRFD__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 004 Deputy District Judge (MC) - Fee paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "004_Deputy_District_Judge-(MC)__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 005 Deputy District Judge (MC) - Sitting in Retirement - Fee paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "005_Deputy_District_Judge-(MC)_Sitting_in_Retirement__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 006 Deputy District Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "006.1_Deputy_District_Judge-Fee-Paid__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "006.2_Deputy_District_Judge__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 007 Deputy District Judge - Sitting in Retirement - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "007.1_Deputy_District_Judge_Sitting_in_Retirement__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "007.2_Deputy_District_Judge_(sitting_in_retirement)__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 008 Deputy High Court Judge - Fee paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "008_Deputy_High_Court_Judge__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 009 District Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "009_District_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );


        // 010 District Judge (MC) - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "010_District_Judge-(MC)__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );


        // 011 High Court Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "011_High_Court_Judge__Salaried",
                SALARIED_CIRCUIT_JUDGE_OUTPUT_TEMPLATE,
                false
            )
        );


        // 012 High Court Judge - Sitting in Retirement - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "012.1_High_Court_Judge_Sitting-in-Retirement__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 013 Recorder - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "013_Recorder__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 014 Designated Family Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "014_Designated_Family_Judge__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE,
                true
            )
        );


        // 015 Family Division Liaison Judge (Presiding Judge - Salaried)
        arguments.addAll(
            generateSalariedTestArguments(
                "015_Family_Division_Liaison_Judge",
                SALARIED_JUDGE_OUTPUT_TEMPLATE,
                true
            )
        );


        // 016 Senior Family Liaison Judge (Resident Judge - Salaried)
        arguments.addAll(
            generateSalariedTestArguments(
                "016_Senior_Family_Liaison_Judge",
                SALARIED_JUDGE_OUTPUT_TEMPLATE,
                true
            )
        );


        // 017 Magistrate - Voluntary
        arguments.addAll(
            generateStandardVoluntaryTestArguments(
                "017_Magistrate__Voluntary",
                MAGISTRATE_OUTPUT_TEMPLATE,
                false // NB: bookings have no effect on this scenario
            )
        );


        // 018 District Judge (MC) (sitting_in_retirement) - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "018_District_Judge_(MC)_(sitting_in_retirement)__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 019 District Judge (sitting in retirement) - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "019_District_Judge_(sitting_in_retirement)__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 020 FL401-Judge – Fee Paid :: see Civil tests
        // 021 FL401-Judge – Salaried :: see Civil tests

        // 022 Circuit Judge (sitting in retirement) - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "022_Circuit_Judge_(sitting_in_retirement)__FeePaid",
                FEE_PAID_CIRCUIT_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // FlagOff Tests
        arguments.addAll(flagOffTestsPrivateLawWa19(arguments));


        // adjust test arguments ready for use
        return adjustTestArguments(arguments, "PrivateLaw");
    }


    private static List<DroolJudicialTestArguments> generateSalariedTestArguments(String jrdResponseFileName,
                                                                                  String rasRequestFileName,
                                                                                  boolean additionalRoleTest) {

        List<DroolJudicialTestArguments> arguments = generateStandardSalariedTestArguments(
            jrdResponseFileName,
            rasRequestFileName,
            additionalRoleTest // if additional role test...
                ? HEARING_ROLES_ONLY_OUTPUT_TEMPLATE // hearing fallback needed on this additional role test
                : null // else no fallback required
        );

        arguments = cloneListOfTestArgumentsForMultiRegion(
            arguments,
            List.of(REGION_02_MIDLANDS), // any single region
            List.of(REGION_01_LONDON, REGION_05_SOUTH_EAST) // multi-regions
        );

        return arguments;
    }


    private static List<DroolJudicialTestArguments> flagOffTestsPrivateLawWa19(
        List<DroolJudicialTestArguments> inputArguments
    ) {
        String singleRegionFileNameSuffix = "singleRegion";
        String multiRegionFileNameSuffix =
            "multiRegion_" + String.join("_", List.of(REGION_01_LONDON, REGION_05_SOUTH_EAST));

        List<DroolJudicialTestArgumentOverrides> testOverrides = new ArrayList<>();
        FeatureFlagEnum flag = FeatureFlagEnum.PRIVATELAW_WA_1_9;

        // no multi region when flag is off
        testOverrides.add(DroolJudicialTestArgumentOverrides.builder()
            .overrideDescription("no_multi_region")
            .findRasRequestFileNameWithoutBooking(
                formatRasRequestFileNameWithSuffix(
                    LEADERSHIP_JUDGE_OUTPUT_TEMPLATE,
                    multiRegionFileNameSuffix
                )
            )
            .overrideRasRequestFileNameWithoutBooking(
                formatRasRequestFileNameWithSuffix(
                    LEADERSHIP_JUDGE_OUTPUT_TEMPLATE,
                    singleRegionFileNameSuffix
                )
            )
            .overrideRasRequestFileNameWithBooking(
                formatRasRequestFileNameWithSuffix(
                    LEADERSHIP_JUDGE_OUTPUT_TEMPLATE,
                    singleRegionFileNameSuffix
                )
            )
            .overrideTurnOffFlags(List.of(flag))
            .build()
        );
        testOverrides.add(DroolJudicialTestArgumentOverrides.builder()
            .overrideDescription("no_multi_region")
            .findRasRequestFileNameWithoutBooking(
                formatRasRequestFileNameWithSuffix(
                    SALARIED_JUDGE_OUTPUT_TEMPLATE,
                    multiRegionFileNameSuffix
                )
            )
            .overrideRasRequestFileNameWithoutBooking(
                formatRasRequestFileNameWithSuffix(
                    SALARIED_JUDGE_OUTPUT_TEMPLATE,
                    singleRegionFileNameSuffix
                )
            )
            .overrideRasRequestFileNameWithBooking(
                formatRasRequestFileNameWithSuffix(
                    SALARIED_JUDGE_OUTPUT_TEMPLATE,
                    singleRegionFileNameSuffix
                )
            )
            .overrideTurnOffFlags(List.of(flag))
            .build()
        );
        testOverrides.add(DroolJudicialTestArgumentOverrides.builder()
            .overrideDescription("no_multi_region")
            .findRasRequestFileNameWithoutBooking(
                formatRasRequestFileNameWithSuffix(
                    SALARIED_CIRCUIT_JUDGE_OUTPUT_TEMPLATE,
                    multiRegionFileNameSuffix
                )
            )
            .overrideRasRequestFileNameWithoutBooking(
                formatRasRequestFileNameWithSuffix(
                    SALARIED_CIRCUIT_JUDGE_OUTPUT_TEMPLATE,
                    singleRegionFileNameSuffix
                )
            )
            .overrideRasRequestFileNameWithBooking(
                formatRasRequestFileNameWithSuffix(
                    SALARIED_CIRCUIT_JUDGE_OUTPUT_TEMPLATE,
                    singleRegionFileNameSuffix
                )
            )
            .overrideTurnOffFlags(List.of(flag))
            .build()
        );

        // must use a catch-all override to run all unaffected tests with the flag off
        testOverrides.add(generateOverrideFlagOffCatchAll(flag));

        return overrideTestArguments(inputArguments, testOverrides);
    }

}
