package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArguments;
import uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.SALARIED;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.adjustTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.cloneListOfTestArgumentsForMultiRegion;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_01_LONDON;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_02_MIDLANDS;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_03_NORTH_EAST;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_04_NORTH_WEST;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_05_SOUTH_EAST;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_06_SOUTH_WEST;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_07_WALES;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_11_SCOTLAND;

public class CivilJudicialIT {

    public static final String SERVICE_CODES = "[[SERVICE_CODES]]";

    public static List<DroolJudicialTestArguments> getTestArguments(String serviceCode) {

        // Civil special tests:
        // * multi region tests for 1 & 5 for all salaried judges
        // * multi region tests for 1 - 7 for all employment judge appointments (Fee-Paid and Salaried)

        List<DroolJudicialTestArguments> arguments = new ArrayList<>();

        // 001 Circuit Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                serviceCode,
                "001_Circuit_Judge__Salaried",
                "001_CJ__003_SpCJ__009_SeCJ__010_HCJ",
                false,
                false
            )
        );

        // 002 Deputy Circuit Judge - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                serviceCode,
                "002_Deputy_Circuit_Judge__FeePaid",
                "002_DCJ__020_CJ-SIR",
                false,
                false
            )
        );

        // 003 Specialist Circuit Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                serviceCode,
                "003_Specialist_Circuit_Judge__Salaried",
                "001_CJ__003_SpCJ__009_SeCJ__010_HCJ",
                false,
                false
            )
        );

        // 004 Deputy District Judge - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                serviceCode,
                "004.1_Deputy_District_Judge-Fee-Paid__FeePaid",
                "004_DDJ__005_DDJ-SIR",
                false,
                false
            )
        );
        arguments.addAll(
            generateFeePaidTestArguments(
                serviceCode,
                "004.2_Deputy_District_Judge__FeePaid",
                "004_DDJ__005_DDJ-SIR",
                false,
                false
            )
        );

        // 005 Deputy District Judge - Sitting in Retirement - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                serviceCode,
                "005.1_Deputy_District_Judge_Sitting_in_Retirement__FeePaid",
                "004_DDJ__005_DDJ-SIR",
                false,
                false
            )
        );
        arguments.addAll(
            generateFeePaidTestArguments(
                serviceCode,
                "005.2_Deputy_District_Judge_(sitting_in_retirement)__FeePaid",
                "004_DDJ__005_DDJ-SIR",
                false,
                false
            )
        );

        // 006 District Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                serviceCode,
                "006_District_Judge__Salaried",
                "006_DJ",
                false,
                false
            )
        );

        // 007 none

        // 008 Recorder - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                serviceCode,
                "008_Recorder__FeePaid",
                "008_R",
                false,
                false
            )
        );

        // 009 Senior Circuit Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                serviceCode,
                "009_Senior_Circuit_Judge__Salaried",
                "001_CJ__003_SpCJ__009_SeCJ__010_HCJ",
                false,
                false
            )
        );

        // 010 High Court Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                serviceCode,
                "010_High_Court_Judge__Salaried",
                "001_CJ__003_SpCJ__009_SeCJ__010_HCJ",
                false,
                false
            )
        );

        // 011 none

        // 012 Designated Civil Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                serviceCode,
                "012_Designated_Civil_Judge__Salaried",
                "012_DCJ",
                true, // additional role test
                false
            )
        );

        // 013 Presiding Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                serviceCode,
                "013_Presiding_Judge__Salaried",
                "013_PJ__014_RJ__016_TJ",
                true, // additional role test
                false
            )
        );

        // 014 Resident Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                serviceCode,
                "014_Resident_Judge__Salaried",
                "013_PJ__014_RJ__016_TJ",
                true, // additional role test
                false
            )
        );

        // 015 District Judge (sitting in retirement) - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                serviceCode,
                "015_District_Judge_(sitting_in_retirement)__FeePaid",
                "015_DJ-SIR",
                false,
                false
            )
        );

        // 016 Tribunal Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                serviceCode,
                "016_Tribunal_Judge__Salaried",
                "013_PJ__014_RJ__016_TJ",
                false,
                false
            )
        );

        // 017 Tribunal Judge - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                serviceCode,
                "017_Tribunal_Judge__FeePaid",
                "017_TJ",
                false,
                false
            )
        );

        // 018 Employment Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                serviceCode,
                "018_Employment_Judge__Salaried",
                "018_EJ",
                false,
                true // employment judge appointment
            )
        );

        // 019 Employment Judge - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                serviceCode,
                "019_Employment_Judge__FeePaid",
                "019_EJ",
                false,
                true // employment judge appointment
            )
        );

        // 020 Circuit Judge (sitting in retirement) - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                serviceCode,
                "020_Circuit_Judge_(sitting_in_retirement)__FeePaid",
                "002_DCJ__020_CJ-SIR",
                false,
                false
            )
        );

        // 021 Lead and Deputy Online Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                serviceCode,
                "021_Lead_and_Deputy_Online_Judge__Salaried",
                "021_LDOJ",
                true, // additional role test
                false
            )
        );

        // adjust test arguments ready for use
        return adjustTestArguments(arguments, "Civil");
    }

    @SuppressWarnings({"SameParameterValue"})
    private static List<DroolJudicialTestArguments> generateFeePaidTestArguments(String serviceCode,
                                                                                 String jrdResponseFileName,
                                                                                 String rasRequestFileName,
                                                                                 boolean additionalRoleTest,
                                                                                 boolean employmentAppointmentTest) {

        List<DroolJudicialTestArguments> arguments = List.of(
            DroolJudicialTestArguments.builder()
                .description(serviceCode)
                .jrdResponseFileName(jrdResponseFileName)
                .rasRequestFileNameWithBooking(rasRequestFileName + "__withBooking")
                .rasRequestFileNameWithoutBooking(rasRequestFileName + "__withoutBooking")
                .additionalRoleTest(additionalRoleTest)
                .overrideMapValues(
                    Map.of(SERVICE_CODES, serviceCode)
                )
                .build()
        );

        // use multi region tests for employment judge appointments
        if (employmentAppointmentTest) {
            List<String> singleRegions = List.of(REGION_11_SCOTLAND);
            List<String> multiRegions = List.of(
                REGION_01_LONDON,
                REGION_02_MIDLANDS,
                REGION_03_NORTH_EAST,
                REGION_04_NORTH_WEST,
                REGION_05_SOUTH_EAST,
                REGION_06_SOUTH_WEST,
                REGION_07_WALES
            );

            arguments = cloneListOfTestArgumentsForMultiRegion(
                arguments,
                singleRegions,
                multiRegions
            );
        }

        return arguments;
    }

    private static List<DroolJudicialTestArguments> generateSalariedTestArguments(String serviceCode,
                                                                                  String jrdResponseFileName,
                                                                                  String rasRequestFileName,
                                                                                  boolean additionalRoleTest,
                                                                                  boolean employmentAppointmentTest) {

        // default multi region test values for all salaried tests
        List<String> singleRegions = List.of(REGION_02_MIDLANDS);
        List<String> multiRegions = List.of(REGION_01_LONDON, REGION_05_SOUTH_EAST);

        // override multi region test values for employment judge appointments
        if (employmentAppointmentTest) {
            singleRegions = List.of(REGION_11_SCOTLAND);
            multiRegions = List.of(
                REGION_01_LONDON,
                REGION_02_MIDLANDS,
                REGION_03_NORTH_EAST,
                REGION_04_NORTH_WEST,
                REGION_05_SOUTH_EAST,
                REGION_06_SOUTH_WEST,
                REGION_07_WALES
            );
        }

        List<DroolJudicialTestArguments> arguments = List.of(
            DroolJudicialTestArguments.builder()
                .description(serviceCode + "__SALARIED")
                .jrdResponseFileName(jrdResponseFileName)
                .rasRequestFileNameWithBooking(rasRequestFileName)
                // NB: With and Without Booking test output will match for these scenarios
                .rasRequestFileNameWithoutBooking(rasRequestFileName)
                .additionalRoleTest(additionalRoleTest)
                .overrideMapValues(
                    generateJudicialOverrideMapValues(serviceCode, SALARIED, REGION_02_MIDLANDS)
                )
                .build()
        );

        arguments = cloneListOfTestArgumentsForMultiRegion(
            arguments,
            singleRegions,
            multiRegions
        );

        // expand to include additional tests for SPTW
        arguments.addAll(
            DroolJudicialTestArgumentsHelper.cloneListOfSalariedTestArgumentsForSptw(arguments)
        );

        return arguments;
    }

    @SuppressWarnings({"SameParameterValue"})
    private static Map<String, String> generateJudicialOverrideMapValues(String serviceCode,
                                                                         String appointmentType,
                                                                         String region) {
        Map<String, String> overrides = DroolJudicialTestArgumentsHelper.generateCommonJudicialOverrideMapValues(
            appointmentType,
            region
        );
        overrides.put(SERVICE_CODES, serviceCode);
        return overrides;
    }

}
