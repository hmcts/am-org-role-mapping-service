package uk.gov.hmcts.reform.orgrolemapping.drool;

import org.apache.commons.lang3.Strings;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.BASE_LOCATION_ID;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.JBS_REGION_ID;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.adjustTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.cloneListOfTestArgumentsForMultiRegion;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.cloneTestArgumentsAndExpandOverrides;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardFeePaidTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardSalariedTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_02_MIDLANDS;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_06_SOUTH_WEST;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_07_WALES;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_12_NATIONAL;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_ID;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_NAME;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.cloneAndOverrideMap;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.expandDescription;

public class SscsJudicialIT {

    private static final String EXTRA_AUTH_CODE = "[[EXTRA_AUTH_CODE]]";

    private static final List<String> SINGLE_BOOKING_REGION_TEST_VALUES = List.of(
        REGION_02_MIDLANDS // any single region
    );
    private static final List<String> SINGLE_REGION_TEST_VALUES = List.of(
        REGION_02_MIDLANDS, // any single region
        REGION_12_NATIONAL // NB: additional test case - where no region attribute is in generated role-assignments
    );
    private static final List<String> MULTI_REGION_TEST_VALUES = List.of(
        REGION_06_SOUTH_WEST,
        REGION_07_WALES
    );

    private static final String FEE_PAID_JUDGE_OUTPUT_TEMPLATE = "003_FeePaid";

    // Tribunal Member templates
    private static final String TRIBUNAL_DISABILITY_FEE_PAID_OUTPUT_TEMPLATE = "Tribunal_Disability_FeePaid";
    private static final String TRIBUNAL_FINANCIAL_FEE_PAID_OUTPUT_TEMPLATE = "Tribunal_Financial_FeePaid";
    private static final String TRIBUNAL_MEDICAL_FEE_PAID_OUTPUT_TEMPLATE = "Tribunal_Medical_FeePaid";
    private static final String TRIBUNAL_MEDICAL_SALARIED_OUTPUT_TEMPLATE = "Tribunal_Medical_Salaried";
    private static final String TRIBUNAL_MEMBER_FEE_PAID_OUTPUT_TEMPLATE = "Tribunal_Member_FeePaid";

    private static final String NO_REGION_TEMPLATE_SUFFIX = "__noRegion";

    private static final String HEARING_ROLES_ONLY_OUTPUT_TEMPLATE = "HearingRolesOnly";

    // flags for use with tribunal member test argument generation
    private static final boolean TRIBUNAL_BASE_LOCATION_TEST_OFF = false;
    private static final boolean TRIBUNAL_BASE_LOCATION_TEST_ON = true;
    private static final boolean TRIBUNAL_FEE_PAID_TEST = false;
    private static final boolean TRIBUNAL_SALARIED_TEST = true;


    public static List<DroolJudicialTestArguments> getTestArguments() {

        // SSCS special tests:
        // * Fee-Paid is only bookable if Auth Code 368 is present
        // * multi region tests for 6 & 7 for all judges
        // * no-region if Appointment Region = 12 (i.e. National)
        // * most (but not ALL) tribunal member tests have a base location filter

        List<DroolJudicialTestArguments> arguments = new ArrayList<>();

        // 001 President of Tribunal - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "001_President_of_Tribunal__Salaried",
                "001_PT" + NO_REGION_TEMPLATE_SUFFIX
            )
        );

        // 002 Regional Tribunal Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "002_Regional_Tribunal_Judge__Salaried",
                "002_RTJ__002a_PJ"
            )
        );

        // 002a Principal Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "002a_Principal_Judge__Salaried",
                "002_RTJ__002a_PJ"
            )
        );

        // 002b Judge of the First-tier Tribunal - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "002b_Judge_of_the_First-tier_Tribunal__Salaried",
                "002b_JoFTT__004_TJ"
            )
        );

        // 003 Tribunal Judge - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                "003_Tribunal_Judge__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 003a Employment Judge - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                "003a_Employment_Judge__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 003b Judge of the First-tier Tribunal (sitting in retirement) - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                "003b_Judge_of_the_First-tier_Tribunal_(sitting_in_retirement)__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 003c Chairman - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                "003c_Chairman__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 003d Deputy District Judge (MC) - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                "003d_Deputy_District_Judge-(MC)__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 003e Recorder - Fee Paid
        arguments.addAll(
            generateFeePaidTestArguments(
                "003e_Recorder__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 004 Tribunal Judge - Salaried
        arguments.addAll(
            generateSalariedTestArguments(
                "004_Tribunal_Judge__Salaried",
                "002b_JoFTT__004_TJ"
            )
        );

        // 005 Tribunal Member Medical - Fee Paid
        arguments.addAll(
            generateTribunalTestArguments(
                "005_Tribunal_Member_Medical__FeePaid",
                TRIBUNAL_MEDICAL_FEE_PAID_OUTPUT_TEMPLATE,
                TRIBUNAL_BASE_LOCATION_TEST_ON,
                TRIBUNAL_FEE_PAID_TEST
            )
        );

        // 006 Tribunal Member Medical - Salaried
        arguments.addAll(
            generateTribunalTestArguments(
                "006_Tribunal_Member_Medical__Salaried",
                TRIBUNAL_MEDICAL_SALARIED_OUTPUT_TEMPLATE,
                TRIBUNAL_BASE_LOCATION_TEST_ON,
                TRIBUNAL_SALARIED_TEST
            )
        );

        // 006a Chief Medical Member First-tier Tribunal - Salaried
        arguments.addAll(
            generateTribunalTestArguments(
                "006a_Chief_Medical_Member_First-tier_Tribunal__Salaried",
                TRIBUNAL_MEDICAL_SALARIED_OUTPUT_TEMPLATE,
                TRIBUNAL_BASE_LOCATION_TEST_ON,
                TRIBUNAL_SALARIED_TEST
            )
        );

        // 006b Regional Medical Member - Salaried
        arguments.addAll(
            generateTribunalTestArguments(
                "006b_Regional_Medical_Member__Salaried",
                TRIBUNAL_MEDICAL_SALARIED_OUTPUT_TEMPLATE,
                TRIBUNAL_BASE_LOCATION_TEST_ON,
                TRIBUNAL_SALARIED_TEST
            )
        );

        // 007 Tribunal Member Disability - Fee Paid
        arguments.addAll(
            generateTribunalTestArguments(
                "007_Tribunal_Member_Disability__FeePaid",
                TRIBUNAL_DISABILITY_FEE_PAID_OUTPUT_TEMPLATE,
                TRIBUNAL_BASE_LOCATION_TEST_ON,
                TRIBUNAL_FEE_PAID_TEST
            )
        );

        // 007a Member of the First-tier Tribunal Lay - Fee Paid
        arguments.addAll(
            generateTribunalTestArguments(
                "007a_Member_of_the_First-tier_Tribunal_Lay__FeePaid",
                TRIBUNAL_DISABILITY_FEE_PAID_OUTPUT_TEMPLATE,
                TRIBUNAL_BASE_LOCATION_TEST_ON,
                TRIBUNAL_FEE_PAID_TEST
            )
        );

        // 007b Member of the First-tier Tribunal (sitting in retirement) - Fee Paid
        arguments.addAll(
            generateTribunalTestArguments(
                "007b_Member_of_the_First-tier_Tribunal_(sitting_in_retirement)__FeePaid",
                TRIBUNAL_DISABILITY_FEE_PAID_OUTPUT_TEMPLATE,
                TRIBUNAL_BASE_LOCATION_TEST_ON,
                TRIBUNAL_FEE_PAID_TEST
            )
        );

        // 008 Tribunal Member Financially Qualified - Fee Paid
        arguments.addAll(
            generateTribunalTestArguments(
                "008_Tribunal_Member_Financially_Qualified__FeePaid",
                TRIBUNAL_FINANCIAL_FEE_PAID_OUTPUT_TEMPLATE,
                TRIBUNAL_BASE_LOCATION_TEST_ON,
                TRIBUNAL_FEE_PAID_TEST
            )
        );

        // 008a Member of the First-tier Tribunal - Fee Paid
        arguments.addAll(
            generateTribunalTestArguments(
                "008a_Member_of_the_First-tier_Tribunal__FeePaid",
                TRIBUNAL_FINANCIAL_FEE_PAID_OUTPUT_TEMPLATE,
                TRIBUNAL_BASE_LOCATION_TEST_ON,
                TRIBUNAL_FEE_PAID_TEST
            )
        );

        // 009 Tribunal Member - Fee Paid
        arguments.addAll(
            generateTribunalTestArguments(
                "009_Tribunal_Member__FeePaid",
                TRIBUNAL_MEMBER_FEE_PAID_OUTPUT_TEMPLATE,
                TRIBUNAL_BASE_LOCATION_TEST_ON,
                TRIBUNAL_FEE_PAID_TEST
            )
        );

        // 010 Tribunal Member Lay - Fee Paid
        arguments.addAll(
            generateTribunalTestArguments(
                "010_Tribunal_Member_Lay__FeePaid",
                TRIBUNAL_MEMBER_FEE_PAID_OUTPUT_TEMPLATE,
                TRIBUNAL_BASE_LOCATION_TEST_ON,
                TRIBUNAL_FEE_PAID_TEST
            )
        );

        // 011 Tribunal Member Optometrist - Fee Paid
        arguments.addAll(
            generateTribunalTestArguments(
                "011_Tribunal_Member_Optometrist__FeePaid",
                TRIBUNAL_MEDICAL_FEE_PAID_OUTPUT_TEMPLATE,
                TRIBUNAL_BASE_LOCATION_TEST_OFF, // no base location filter for this appointment
                TRIBUNAL_FEE_PAID_TEST
            )
        );

        // 012 Tribunal Member Service - Fee Paid
        arguments.addAll(
            generateTribunalTestArguments(
                "012_Tribunal_Member_Service__FeePaid",
                TRIBUNAL_MEMBER_FEE_PAID_OUTPUT_TEMPLATE,
                TRIBUNAL_BASE_LOCATION_TEST_OFF, // no base location filter for this appointment
                TRIBUNAL_FEE_PAID_TEST
            )
        );

        arguments = adjustTestArgumentsForRegion12Tests(arguments);

        // adjust test arguments ready for use
        return adjustTestArguments(arguments, "SSCS");
    }


    private static List<DroolJudicialTestArguments> adjustTestArgumentsForRegion12Tests(
        List<DroolJudicialTestArguments> arguments
    ) {
        return arguments.stream()
            .map(testArguments -> {
                // if it is a `singleRegion-12` test then repoint it to the equivalent `noRegion` templates
                if (Strings.CI.endsWith(testArguments.getDescription(), "singleRegion_12")) {
                    return testArguments.cloneBuilder()
                        .description(testArguments.getDescription() + NO_REGION_TEMPLATE_SUFFIX)
                        .rasRequestFileNameWithoutBooking(
                            renameTemplateWithNoRegionSuffix(testArguments.getRasRequestFileNameWithoutBooking())
                        )
                        .rasRequestFileNameWithBooking(
                            renameTemplateWithNoRegionSuffix(testArguments.getRasRequestFileNameWithBooking())
                        )
                        .build();
                } else {
                    // no adjustment required
                    return testArguments;
                }
            })
            .toList();
    }


    @SuppressWarnings({"SameParameterValue"})
    private static List<DroolJudicialTestArguments> generateFeePaidTestArguments(String jrdResponseFileName,
                                                                                 String rasRequestFileName) {

        List<DroolJudicialTestArguments> arguments = new ArrayList<>();

        // Part 1: Auth368: Appointment Region Single/MultiRegion tests
        List<DroolJudicialTestArguments> p1Arguments = generateStandardFeePaidTestArguments(
            jrdResponseFileName,
            rasRequestFileName + "__Auth368__AppointmentRegion"
        );

        // expand for single & multi-region appointment regions
        p1Arguments = cloneListOfTestArgumentsForMultiRegion(
            p1Arguments,
            SINGLE_REGION_TEST_VALUES,
            MULTI_REGION_TEST_VALUES
        );

        // rename description + add extra auth code >> transfer to final arguments list
        for (DroolJudicialTestArguments testArguments : p1Arguments) {
            arguments.add(testArguments.cloneBuilder()
                .description("Auth368__AppointmentRegion__" + testArguments.getDescription())
                .overrideMapValues(
                    cloneAndOverrideMap(
                        testArguments.getOverrideMapValues(),
                        Map.of(EXTRA_AUTH_CODE, "368")
                    )
                )
                .build()
            );
        }

        // Part 2: Booking Region Single/MultiRegion tests
        List<DroolJudicialTestArguments> p2Arguments = generateStandardFeePaidTestArguments(
            jrdResponseFileName,
            rasRequestFileName + "__Auth368__BookingRegion"
        );

        // expand for single & multi-region appointment regions
        p2Arguments = cloneListOfTestArgumentsForMultiRegion(
            p2Arguments,
            // NB: use different single region test values which excludes Region 12 as not applicable to bookings
            SINGLE_BOOKING_REGION_TEST_VALUES,
            MULTI_REGION_TEST_VALUES
        );

        // rename description + add extra auth code >> transfer to final arguments list
        // + adjust REGION values (i.e. move multi region values to BOOKING region)
        for (DroolJudicialTestArguments testArguments : p2Arguments) {
            arguments.add(testArguments.cloneBuilder()
                .description("Auth368__BookingRegion__" + testArguments.getDescription())
                .overrideMapValues(
                    cloneAndOverrideMap(
                        testArguments.getOverrideMapValues(),
                        Map.of(
                            EXTRA_AUTH_CODE, "368",
                            // move multi region values to BOOKING region
                            JBS_REGION_ID, testArguments.getOverrideMapValues().get(REGION_ID),
                            // reset appointment region values to something else
                            REGION_ID, "appointment_region_id",
                            REGION_NAME, "appointment_region_name"
                        )
                    )
                )
                .build()
            );
        }

        // Part 3: When bookings are not permitted: i.e. Auth code != 368
        List<DroolJudicialTestArguments> p3Arguments = generateStandardFeePaidTestArguments(
            jrdResponseFileName,
            rasRequestFileName + "__notAuth368__NoBookingAllowed",
            false, // NB: no additional role tests in SSCS
            false // NB: bookings not permitted for this scenario (i.e. Auth code != 368)
        );

        // expand for single & multi-region appointment regions
        p3Arguments = cloneListOfTestArgumentsForMultiRegion(
            p3Arguments,
            SINGLE_REGION_TEST_VALUES,
            MULTI_REGION_TEST_VALUES
        );

        // rename description + add extra auth code >> transfer to final arguments list
        for (DroolJudicialTestArguments testArguments : p3Arguments) {
            arguments.add(testArguments.cloneBuilder()
                .description("notAuth368__NoBookingAllowed__" + testArguments.getDescription())
                .overrideMapValues(
                    cloneAndOverrideMap(
                        testArguments.getOverrideMapValues(),
                        Map.of(EXTRA_AUTH_CODE, "not368")
                    )
                )
                .build()
            );
        }

        return arguments;
    }

    private static List<DroolJudicialTestArguments> generateSalariedTestArguments(String jrdResponseFileName,
                                                                                  String rasRequestFileName) {

        List<DroolJudicialTestArguments> arguments = generateStandardSalariedTestArguments(
            jrdResponseFileName,
            rasRequestFileName
        );

        // if using regions then expand for multi region tests: (i.e. if not using a no_region template)
        if (!rasRequestFileName.endsWith(NO_REGION_TEMPLATE_SUFFIX)) {
            arguments = cloneListOfTestArgumentsForMultiRegion(
                arguments,
                SINGLE_REGION_TEST_VALUES,
                MULTI_REGION_TEST_VALUES
            );
        }

        return arguments;
    }

    private static List<DroolJudicialTestArguments> generateTribunalTestArguments(
        String jrdResponseFileName,
        String rasRequestFileName,
        boolean includeBaseLocationTests,
        boolean salariedTests
    ) {

        List<DroolJudicialTestArguments> standardArguments;

        if (salariedTests) {
            standardArguments = generateStandardSalariedTestArguments(
                jrdResponseFileName,
                rasRequestFileName
            );
        } else {
            standardArguments = generateStandardFeePaidTestArguments(
                jrdResponseFileName,
                rasRequestFileName,
                false, // NB: no additional role tests in SSCS
                false // NB: bookings not used for tribunal member fee-paid
            );
        }

        List<DroolJudicialTestArguments> outputArguments = new ArrayList<>();
        List<DroolJudicialTestArguments> argumentsBeforeMultiRegionExpansion = new ArrayList<>();

        // add base location overrides for tribunal member tests
        standardArguments.forEach(argument -> {

            if (includeBaseLocationTests) {
                argumentsBeforeMultiRegionExpansion.add(
                    cloneTestArgumentsAndExpandOverrides(
                        argument,
                        "base-location-1032",
                        Map.of(
                            BASE_LOCATION_ID, "1032"
                        )
                    )
                );

                // NB: add negative test when base location is not supported direct to output list of test arguments
                outputArguments.add(
                    argument.cloneBuilder()
                        .description(
                            expandDescription(argument.getDescription(), "base-location-not1032__notSupported")
                        )
                        // NB: Hearing Roles will still apply even when base location not supported
                        .rasRequestFileNameWithoutBooking(HEARING_ROLES_ONLY_OUTPUT_TEMPLATE)
                        .rasRequestFileNameWithBooking(HEARING_ROLES_ONLY_OUTPUT_TEMPLATE)
                        .overrideMapValues(
                            cloneAndOverrideMap(
                                argument.getOverrideMapValues(),
                                Map.of(
                                    BASE_LOCATION_ID, "not1032"
                                )
                            )
                        )
                        .build()
                );

            } else {
                // no special test to generate: just set base location to anything
                argumentsBeforeMultiRegionExpansion.add(
                    argument.cloneBuilder()
                        .overrideMapValues(
                            cloneAndOverrideMap(
                                argument.getOverrideMapValues(),
                                Map.of(
                                    BASE_LOCATION_ID, "any-base-location"
                                )
                            )
                        )
                        .build()
                );
            }

        });

        // now apply multiRegion test expansion to those tests that need it
        outputArguments.addAll(
            cloneListOfTestArgumentsForMultiRegion(
                argumentsBeforeMultiRegionExpansion,
                SINGLE_REGION_TEST_VALUES,
                MULTI_REGION_TEST_VALUES
            )
        );

        return outputArguments;
    }

    private static String renameTemplateWithNoRegionSuffix(String templateFileName) {
        return templateFileName.replace("__singleRegion", NO_REGION_TEMPLATE_SUFFIX);
    }

}
