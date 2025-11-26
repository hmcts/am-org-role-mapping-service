package uk.gov.hmcts.reform.orgrolemapping.drool;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.adjustTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.cloneListOfTestArgumentsForMultiRegion;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardFeePaidTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardSalariedTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_01_LONDON;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_02_MIDLANDS;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_03_NORTH_EAST;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_04_NORTH_WEST;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_05_SOUTH_EAST;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_06_SOUTH_WEST;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_07_WALES;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_11_SCOTLAND;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.cloneAndOverrideMap;

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
                "001_Circuit_Judge__Salaried",
                "001_CJ__003_SpCJ__009_SeCJ__010_HCJ",
                false,
                false
            )
        );

        // 002 Deputy Circuit Judge - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                "002_Deputy_Circuit_Judge__FeePaid",
                "002_DCJ__020_CJ-SIR",
                false
            )
        );

        // 003 Specialist Circuit Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "003_Specialist_Circuit_Judge__Salaried",
                "001_CJ__003_SpCJ__009_SeCJ__010_HCJ",
                false,
                false
            )
        );

        // 004 Deputy District Judge - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                "004.1_Deputy_District_Judge-Fee-Paid__FeePaid",
                "004_DDJ__005_DDJ-SIR",
                false
            )
        );
        arguments.addAll(
            generateFeePaidTestArguments(
                "004.2_Deputy_District_Judge__FeePaid",
                "004_DDJ__005_DDJ-SIR",
                false
            )
        );

        // 005 Deputy District Judge - Sitting in Retirement - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                "005.1_Deputy_District_Judge_Sitting_in_Retirement__FeePaid",
                "004_DDJ__005_DDJ-SIR",
                false
            )
        );
        arguments.addAll(
            generateFeePaidTestArguments(
                "005.2_Deputy_District_Judge_(sitting_in_retirement)__FeePaid",
                "004_DDJ__005_DDJ-SIR",
                false
            )
        );

        // 006 District Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
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
                "008_Recorder__FeePaid",
                "008_R",
                false
            )
        );

        // 009 Senior Circuit Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "009_Senior_Circuit_Judge__Salaried",
                "001_CJ__003_SpCJ__009_SeCJ__010_HCJ",
                false,
                false
            )
        );

        // 010 High Court Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
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
                "012_Designated_Civil_Judge__Salaried",
                "012_DCJ",
                true, // additional role test
                false
            )
        );

        // 013 Presiding Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "013_Presiding_Judge__Salaried",
                "013_PJ__014_RJ__016_TJ",
                true, // additional role test
                false
            )
        );

        // 014 Resident Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "014_Resident_Judge__Salaried",
                "013_PJ__014_RJ__016_TJ",
                true, // additional role test
                false
            )
        );

        // 015 District Judge (sitting in retirement) - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                "015_District_Judge_(sitting_in_retirement)__FeePaid",
                "015_DJ-SIR",
                false
            )
        );

        // 016 Tribunal Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "016_Tribunal_Judge__Salaried",
                "013_PJ__014_RJ__016_TJ",
                false,
                false
            )
        );

        // 017 Tribunal Judge - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                "017_Tribunal_Judge__FeePaid",
                "017_TJ",
                false
            )
        );

        // 018 Employment Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "018_Employment_Judge__Salaried",
                "018_EJ",
                false,
                true // employment judge appointment
            )
        );

        // 019 Employment Judge - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                "019_Employment_Judge__FeePaid",
                "019_EJ",
                true // employment judge appointment
            )
        );

        // 020 Circuit Judge (sitting in retirement) - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                "020_Circuit_Judge_(sitting_in_retirement)__FeePaid",
                "002_DCJ__020_CJ-SIR",
                false
            )
        );

        // 021 Lead and Deputy Online Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "021_Lead_and_Deputy_Online_Judge__Salaried",
                "021_LDOJ",
                true, // additional role test
                false
            )
        );

        arguments = adjustTestArgumentsForServiceCode(arguments, serviceCode);

        // adjust test arguments ready for use
        return adjustTestArguments(arguments, "Civil");
    }

    private static List<DroolJudicialTestArguments> adjustTestArgumentsForServiceCode(
        List<DroolJudicialTestArguments> arguments,
        String serviceCode
    ) {
        // adjust all test arguments to include ServiceCode in Description and Override Map
        return arguments.stream()
            .map(testArguments -> testArguments.cloneBuilder()
                .description(
                    StringUtils.isEmpty(testArguments.getDescription())
                        ? serviceCode
                        : serviceCode + "__" + testArguments.getDescription()
                )
                .overrideMapValues(
                    cloneAndOverrideMap(
                        testArguments.getOverrideMapValues(),
                        Map.of(SERVICE_CODES, serviceCode)
                    )
                )
                .build()
            )
            .toList();
    }

    private static List<DroolJudicialTestArguments> generateFeePaidTestArguments(String jrdResponseFileName,
                                                                                 String rasRequestFileName,
                                                                                 boolean employmentAppointmentTest) {

        List<DroolJudicialTestArguments> arguments = generateStandardFeePaidTestArguments(
            jrdResponseFileName,
            rasRequestFileName
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

    private static List<DroolJudicialTestArguments> generateSalariedTestArguments(String jrdResponseFileName,
                                                                                  String rasRequestFileName,
                                                                                  boolean additionalRoleTest,
                                                                                  boolean employmentAppointmentTest) {


        List<DroolJudicialTestArguments> arguments = generateStandardSalariedTestArguments(
            jrdResponseFileName,
            rasRequestFileName,
            additionalRoleTest
        );

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

        arguments = cloneListOfTestArgumentsForMultiRegion(
            arguments,
            singleRegions,
            multiRegions
        );

        return arguments;
    }

}
