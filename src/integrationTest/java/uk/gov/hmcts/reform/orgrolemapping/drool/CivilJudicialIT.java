package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArguments;
import uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.SALARIED;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.adjustTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_01_LONDON;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_02_MIDLANDS;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_05_SOUTH_EAST;

public class CivilJudicialIT {

    public static final String SERVICE_CODES = "[[SERVICE_CODES]]";

    public static List<DroolJudicialTestArguments> getTestArguments(String serviceCode) {

        List<DroolJudicialTestArguments> arguments = new ArrayList<>();

        // 001 Circuit Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                serviceCode,
                "001_Circuit_Judge__Salaried",
                "001_CJ__003_SpCJ__009_SeCJ__010_HCJ",
                false
            )
        );

        // 005 Deputy District Judge - Sitting in Retirement - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                serviceCode,
                "005.1_Deputy_District_Judge_Sitting_in_Retirement__FeePaid",
                "004_DDJ__005_DDJ-SIR__008_R__015_DJ-SIR__020_CJ-SIR__017_TJ",
                false
            )
        );
        arguments.addAll(
            generateFeePaidTestArguments(
                serviceCode,
                "005.2_Deputy_District_Judge_(sitting_in_retirement)__FeePaid",
                "004_DDJ__005_DDJ-SIR__008_R__015_DJ-SIR__020_CJ-SIR__017_TJ",
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
                                                                                 boolean additionalRoleTest) {

        return List.of(
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

    }

    @SuppressWarnings({"SameParameterValue"})
    private static List<DroolJudicialTestArguments> generateSalariedTestArguments(String serviceCode,
                                                                                  String jrdResponseFileName,
                                                                                  String rasRequestFileName,
                                                                                  boolean additionalRoleTest) {

        List<DroolJudicialTestArguments> arguments = new ArrayList<>();
        arguments.addAll(List.of(
            // SALARIED + single region
            DroolJudicialTestArguments.builder()
                .description(serviceCode + "__SALARIED__singleRegion")
                .jrdResponseFileName(jrdResponseFileName)
                .rasRequestFileNameWithBooking(rasRequestFileName + "__singleRegion")
                // NB: With and Without Booking test output will match for these scenarios
                .rasRequestFileNameWithoutBooking(rasRequestFileName + "__singleRegion")
                .additionalRoleTest(additionalRoleTest)
                .overrideMapValues(
                    generateJudicialOverrideMapValues(serviceCode, SALARIED, REGION_02_MIDLANDS)
                )
                .build(),

            // SALARIED + multi region (1 + 5)
            DroolJudicialTestArguments.builder()
                .description(serviceCode + "__SALARIED__multiRegion_1")
                .jrdResponseFileName(jrdResponseFileName)
                .rasRequestFileNameWithBooking(rasRequestFileName + "__multiRegion_1_5")
                // NB: With and Without Booking test output will match for these scenarios
                .rasRequestFileNameWithoutBooking(rasRequestFileName + "__multiRegion_1_5")
                .additionalRoleTest(additionalRoleTest)
                .overrideMapValues(
                    generateJudicialOverrideMapValues(serviceCode, SALARIED, REGION_01_LONDON)
                )
                .build(),
            DroolJudicialTestArguments.builder()
                .description(serviceCode + "__SALARIED__multiRegion_5")
                .jrdResponseFileName(jrdResponseFileName)
                .rasRequestFileNameWithBooking(rasRequestFileName + "__multiRegion_1_5")
                // NB: With and Without Booking test output will match for these scenarios
                .rasRequestFileNameWithoutBooking(rasRequestFileName + "__multiRegion_1_5")
                .additionalRoleTest(additionalRoleTest)
                .overrideMapValues(
                    generateJudicialOverrideMapValues(serviceCode, SALARIED, REGION_05_SOUTH_EAST)
                )
                .build()
        ));

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
