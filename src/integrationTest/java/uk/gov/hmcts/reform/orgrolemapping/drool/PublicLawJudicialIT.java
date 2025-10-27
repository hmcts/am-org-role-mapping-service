package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArguments;
import uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.SALARIED;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.adjustTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateCommonJudicialOverrideMapValues;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardFeePaidTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_01_LONDON;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_02_MIDLANDS;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_05_SOUTH_EAST;

public class PublicLawJudicialIT {

    @SuppressWarnings({"LineLength"})
    public static List<DroolJudicialTestArguments> getTestArguments() {

        List<DroolJudicialTestArguments> arguments = new ArrayList<>();

        // 001 Circuit Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "001_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ",
                false
            )
        );

        // 002 Deputy Circuit Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "002_Deputy_Circuit_Judge__FeePaid",
                "002_DCJ__003_R__020_CJ-SIR__021_R-SIR__022_DUTJ",
                false
            )
        );

        // 003 Recorder - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "003_Recorder__FeePaid",
                "002_DCJ__003_R__020_CJ-SIR__021_R-SIR__022_DUTJ",
                false
            )
        );

        // 004 Deputy District Judge - PRFD - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "004_Deputy_District_Judge-PRFD__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR",
                false
            )
        );

        // 005 Deputy District Judge (MC) - Fee paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "005_Deputy_District_Judge-(MC)__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR",
                false
            )
        );

        // 006 Deputy District Judge (MC) - Sitting in Retirement - Fee paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "006_Deputy_District_Judge-(MC)_Sitting_in_Retirement__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR",
                false
            )
        );

        // 007 Deputy District Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "007.1_Deputy_District_Judge-Fee-Paid__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR",
                false
            )
        );
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "007.2_Deputy_District_Judge__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR",
                false
            )
        );

        // 008 Deputy District Judge - Sitting in Retirement - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "008.1_Deputy_District_Judge_Sitting_in_Retirement__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR",
                false
            )
        );
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "008.2_Deputy_District_Judge_(sitting_in_retirement)__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR",
                false
            )
        );

        // 009 Deputy High Court Judge - Fee paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "009_Deputy_High_Court_Judge__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR",
                false
            )
        );

        // 010 District Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "010_District_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ",
                false
            )
        );

        // 011 District Judge (MC) - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "011_District_Judge-(MC)__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ",
                false
            )
        );

        // 012 High Court Judge / President of the Family Division - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "012.1_High_Court_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ",
                false
            )
        );
        arguments.addAll(
            generateSalariedTestArguments(
                "012.2_President_of_the_Family_Division__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ",
                false
            )
        );

        // 013 High Court Judge - Sitting in Retirement - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "013.1_High_Court_Judge_Sitting-in-Retirement__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR",
                false
            )
        );
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "013.2_High_Court_Judge_(sitting-in-retirement)__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR",
                false
            )
        );

        // 014 Designated Family Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "014_Designated_Family_Judge__Salaried",
                "014_DFJ__014b_ADFJ",
                false
            )
        );

        // 014b Acting Designated Family Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "014b_Acting_Designated_Family_Judge__Salaried",
                "014_DFJ__014b_ADFJ",
                true
            )
        );

        // 015 Magistrate - Voluntary
        arguments.add(
            DroolJudicialTestArguments.builder()
                .jrdResponseFileName("015_Magistrate__Voluntary")
                .rasRequestFileNameWithBooking("015_M__withoutBooking")
                .rasRequestFileNameWithoutBooking("015_M__withoutBooking") // bookings have no effect on this scenario
                .additionalRoleTest(false)
                .overrideMapValues(null)
                .build()
        );

        // 016 Tribunal Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "016_Tribunal_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ",
                false
            )
        );

        // 017 Employment Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "017_Employment_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ",
                false
            )
        );

        // 018 Specialist Circuit Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "018_Specialist_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ",
                false
            )
        );

        // 019 Senior Circuit Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "019_Senior_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ",
                false
            )
        );

        // 020 Circuit Judge (sitting in retirement) - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "020_Circuit_Judge_(sitting_in_retirement)__FeePaid",
                "002_DCJ__003_R__020_CJ-SIR__021_R-SIR__022_DUTJ",
                false
            )
        );

        // 021 Recorder (sitting in retirement) - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "021_Recorder_(sitting_in_retirement)__FeePaid",
                "002_DCJ__003_R__020_CJ-SIR__021_R-SIR__022_DUTJ",
                false
            )
        );


        // 022 Deputy Upper Tribunal Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "022_Deputy_Upper_Tribunal_Judge__FeePaid",
                "002_DCJ__003_R__020_CJ-SIR__021_R-SIR__022_DUTJ",
                false
            )
        );


        // 023 District Judge (MC) (sitting_in_retirement) - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "023_District_Judge_(MC)_(sitting_in_retirement)__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR",
                false
            )
        );


        // 024 District Judge (sitting in retirement) - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "024_District_Judge_(sitting_in_retirement)__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR",
                false
            )
        );


        // adjust test arguments ready for use
        return adjustTestArguments(arguments, "PublicLaw");
    }


    private static List<DroolJudicialTestArguments> generateSalariedTestArguments(String jrdResponseFileName,
                                                                                  String rasRequestFileName,
                                                                                  boolean additionalRoleTest) {

        List<DroolJudicialTestArguments> arguments = new ArrayList<>();

        arguments.addAll(List.of(
            // SALARIED + single region
            DroolJudicialTestArguments.builder()
                .description("SALARIED__singleRegion")
                .jrdResponseFileName(jrdResponseFileName)
                .rasRequestFileNameWithBooking(rasRequestFileName + "__singleRegion")
                // NB: With and Without Booking test output will match for these scenarios
                .rasRequestFileNameWithoutBooking(rasRequestFileName + "__singleRegion")
                .additionalRoleTest(additionalRoleTest)
                .overrideMapValues(generateCommonJudicialOverrideMapValues(SALARIED, REGION_02_MIDLANDS))
                .build(),

            // SALARIED + multi region (1 + 5)
            DroolJudicialTestArguments.builder()
                .description("SALARIED__multiRegion_1")
                .jrdResponseFileName(jrdResponseFileName)
                .rasRequestFileNameWithBooking(rasRequestFileName + "__multiRegion_1_5")
                // NB: With and Without Booking test output will match for these scenarios
                .rasRequestFileNameWithoutBooking(rasRequestFileName + "__multiRegion_1_5")
                .additionalRoleTest(additionalRoleTest)
                .overrideMapValues(generateCommonJudicialOverrideMapValues(SALARIED, REGION_01_LONDON))
                .build(),
            DroolJudicialTestArguments.builder()
                .description("SALARIED__multiRegion_5")
                .jrdResponseFileName(jrdResponseFileName)
                .rasRequestFileNameWithBooking(rasRequestFileName + "__multiRegion_1_5")
                // NB: With and Without Booking test output will match for these scenarios
                .rasRequestFileNameWithoutBooking(rasRequestFileName + "__multiRegion_1_5")
                .additionalRoleTest(additionalRoleTest)
                .overrideMapValues(generateCommonJudicialOverrideMapValues(SALARIED, REGION_05_SOUTH_EAST))
                .build()
        ));

        // expand to include additional tests for SPTW
        arguments.addAll(
            DroolJudicialTestArgumentsHelper.cloneListOfSalariedTestArgumentsForSptw(arguments)
        );

        return arguments;
    }

}
