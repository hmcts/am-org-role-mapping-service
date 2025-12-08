package uk.gov.hmcts.reform.orgrolemapping.helper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArgumentOverrides;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArguments;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.TestScenario;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.TestScenario.TestScenarioBuilder;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.FeatureFlagEnum;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.FEE_PAID;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType.SALARIED;
import static uk.gov.hmcts.reform.orgrolemapping.drool.BaseDroolTestIntegration.EMPTY_ROLE_ASSIGNMENT_TEMPLATE;
import static uk.gov.hmcts.reform.orgrolemapping.drool.RunJudicialDroolIntegrationTests.DROOL_JUDICIAL_TEST_OUTPUT_PATH;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.DF;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.IDAM_ID;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_02_MIDLANDS;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.addRegionOverrideMapValues;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.cloneAndOverrideMap;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.expandDescription;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.expireDateInReplaceMap;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.setBooleanInReplaceMap;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.useNullDateInReplaceMap;

@SuppressWarnings({
    "java:S115" // Constant names should comply with a naming convention
})
public class DroolJudicialTestArgumentsHelper {

    private static final String HAPPY_PATH = "HappyPath";
    private static final String ALL_DATES_SUPPLIED = "all dates supplied";
    private static final String NO_APPOINTMENT_END_DATE = "no appointment end date";
    private static final String NO_AUTHORISATION_END_DATE = "no authorisation end date";
    private static final String NO_ADDITIONAL_ROLE_END_DATE = "no additional role end date";
    private static final String NO_BOOKING_END_DATE = "no booking end date";
    private static final String HAPPY_PATH_DESCRIPTION = HAPPY_PATH + " - %s - %s";

    private static final String NEGATIVE_TEST = "NegativeTest";
    private static final String APPOINTMENT_END_DATE_EXPIRED
        = "appointment end date expired";
    private static final String AUTHORISATION_END_DATE_EXPIRED
        = "authorisation end date expired";
    private static final String ADDITIONAL_ROLE_END_DATE_EXPIRED
        = "additional role end date expired";
    private static final String SOFT_DELETE_FLAG_SET = "soft delete flag set";
    private static final String NEGATIVE_TEST_DESCRIPTION = NEGATIVE_TEST + " - %s";

    public static final String SPTW_TEST_APPOINTMENT_TYPE = "SPTW-50%";
    public static final String SPTW_TEST_CONTRACT_TYPE_ID = "5";


    // JUDICIAL replace values
    public static final String APPOINTMENT_BEGIN_TIME = "[[APPOINTMENT_BEGIN_TIME]]";
    public static final String APPOINTMENT_END_TIME = "[[APPOINTMENT_END_TIME]]";
    public static final String AUTHORISATION_BEGIN_TIME = "[[AUTHORISATION_BEGIN_TIME]]";
    public static final String AUTHORISATION_END_TIME = "[[AUTHORISATION_END_TIME]]";
    public static final String APPOINTMENT_TYPE = "[[APPOINTMENT_TYPE]]";
    public static final String CONTRACT_TYPE_ID = "[[CONTRACT_TYPE_ID]]";
    public static final String DELETED_FLAG = "[[DELETED_FLAG]]";
    public static final String ROLE_BEGIN_TIME = "[[ROLE_BEGIN_TIME]]";
    public static final String ROLE_END_TIME = "[[ROLE_END_TIME]]";
    public static final String BASE_LOCATION_ID = "[[BASE_LOCATION_ID]]";
    // JUDICIAL BOOKING replace values
    public static final String JBS_BEGIN_TIME = "[[JBS_BEGIN_TIME]]";
    public static final String JBS_END_TIME = "[[JBS_END_TIME]]";
    public static final String JBS_LOCATION_ID = "[[JBS_LOCATION_ID]]";
    public static final String JBS_REGION_ID = "[[JBS_REGION_ID]]";

    public static List<DroolJudicialTestArguments> adjustTestArguments(List<DroolJudicialTestArguments> arguments,
                                                                       String jurisdiction) {
        // adjust test arguments ready for use
        return arguments.stream()
            .map(testArguments ->
                testArguments.cloneBuilder()
                    .jurisdiction(jurisdiction)
                    .testGroup(testArguments.getJrdResponseFileName())
                    .testName(formatTestName(testArguments))
                    .description(formatDisplayName(testArguments, jurisdiction))
                    .outputLocation(formatOutputLocation(testArguments, jurisdiction))
                    .jrdResponseFileName(
                        formatJrdResponseFileName(testArguments.getJrdResponseFileName(), jurisdiction)
                    )
                    .rasRequestFileNameWithBooking(
                        formatRasRequestFileName(testArguments.getRasRequestFileNameWithBooking(), jurisdiction)
                    )
                    .rasRequestFileNameWithoutBooking(
                        formatRasRequestFileName(testArguments.getRasRequestFileNameWithoutBooking(), jurisdiction)
                    )
                    .additionalRoleExpiredFallbackFileName(
                        formatRasRequestFileName(testArguments.getAdditionalRoleExpiredFallbackFileName(), jurisdiction)
                    )
                    .build()
            )
            .toList();
    }

    public static List<DroolJudicialTestArguments> cloneListOfSalariedTestArgumentsForSptw(
        List<DroolJudicialTestArguments> originalList
    ) {
        // override Appointment Type values with SPTW values
        Map<String, String> overrideMapValues = new HashMap<>();
        overrideMapValues.put(APPOINTMENT_TYPE, SPTW_TEST_APPOINTMENT_TYPE);
        overrideMapValues.put(CONTRACT_TYPE_ID, SPTW_TEST_CONTRACT_TYPE_ID);

        return originalList.stream()
            .map(originalArgs -> originalArgs.cloneBuilder()
                .description(originalArgs.getDescription().replace("SALARIED", "SPTW"))
                .overrideMapValues(
                    cloneAndOverrideMap(
                        originalArgs.getOverrideMapValues(),
                        overrideMapValues
                    )
                )
                .build()
            )
            .toList();
    }

    public static List<DroolJudicialTestArguments> cloneListOfTestArgumentsForMultiRegion(
        List<DroolJudicialTestArguments> inputArguments,
        List<String> singleRegions,
        List<String> multiRegions
    ) {
        String singleRegionFileNameSuffix = "singleRegion";
        String multiRegionFileNameSuffix = "multiRegion_" + String.join("_", multiRegions);


        List<DroolJudicialTestArguments> outputArguments = new ArrayList<>();

        inputArguments.forEach(originalArgs -> {
            // single region
            outputArguments.addAll(
                singleRegions.stream().map(region -> {
                    Map<String, String> regionOverrideMapValues = new HashMap<>();
                    addRegionOverrideMapValues(regionOverrideMapValues, region);

                    return originalArgs.cloneBuilder()
                        .description(
                            expandDescription(originalArgs.getDescription(), "singleRegion_" + region)
                        )
                        .rasRequestFileNameWithBooking(
                            formatRasRequestFileNameWithSuffix(
                                originalArgs.getRasRequestFileNameWithBooking(),
                                singleRegionFileNameSuffix
                            )
                        )
                        .rasRequestFileNameWithoutBooking(
                            formatRasRequestFileNameWithSuffix(
                                originalArgs.getRasRequestFileNameWithoutBooking(),
                                singleRegionFileNameSuffix
                            )
                        )
                        .overrideMapValues(
                            cloneAndOverrideMap(
                                originalArgs.getOverrideMapValues(),
                                regionOverrideMapValues
                            )
                        )
                        .build();
                }).toList()
            );

            // multi region
            outputArguments.addAll(
                multiRegions.stream().map(region -> {
                    Map<String, String> regionOverrideMapValues = new HashMap<>();
                    addRegionOverrideMapValues(regionOverrideMapValues, region);

                    return originalArgs.cloneBuilder()
                        .description(
                            expandDescription(originalArgs.getDescription(), "multiRegion_" + region)
                        )
                        .rasRequestFileNameWithBooking(
                            formatRasRequestFileNameWithSuffix(
                                originalArgs.getRasRequestFileNameWithBooking(),
                                multiRegionFileNameSuffix
                            )
                        )
                        .rasRequestFileNameWithoutBooking(
                            formatRasRequestFileNameWithSuffix(
                                originalArgs.getRasRequestFileNameWithoutBooking(),
                                multiRegionFileNameSuffix
                            )
                        )
                        .overrideMapValues(
                            cloneAndOverrideMap(
                                originalArgs.getOverrideMapValues(),
                                regionOverrideMapValues
                            )
                        )
                        .build();
                }).toList()
            );
        });

        return outputArguments;
    }

    public static DroolJudicialTestArguments cloneTestArgumentsAndExpandOverrides(
        DroolJudicialTestArguments testArguments,
        String description,
        Map<String, String> overrideMapValues
    ) {
        return testArguments.cloneBuilder()
            .description(
                StringUtils.isNotEmpty(description)
                    ? expandDescription(testArguments.getDescription(), description)
                    : testArguments.getDescription()
            )
            .overrideMapValues(
                cloneAndOverrideMap(
                    testArguments.getOverrideMapValues(),
                    overrideMapValues
                )
            )
            .build();
    }

    public static Map<String, String> createDefaultJudicialReplaceMap(Map<String, String> overrideMapValues) {

        // build map of happy path values: even for those that may not be in the test
        Map<String, String> outputMap = new HashMap<>(Map.of(
            IDAM_ID, UUID.randomUUID().toString(),
            APPOINTMENT_BEGIN_TIME, LocalDate.now().minusDays(5).format(DF),
            APPOINTMENT_END_TIME, LocalDate.now().plusDays(5).format(DF),
            AUTHORISATION_BEGIN_TIME, LocalDate.now().minusDays(3).format(DF),
            AUTHORISATION_END_TIME, LocalDate.now().plusDays(3).format(DF),
            DELETED_FLAG, "false",
            ROLE_BEGIN_TIME, LocalDate.now().minusDays(2).format(DF),
            ROLE_END_TIME, LocalDate.now().plusDays(2).format(DF),
            JBS_BEGIN_TIME, LocalDate.now().minusDays(1).format(DF) + "T00:00:00Z",
            JBS_END_TIME, LocalDate.now().plusDays(1).format(DF) + "T00:00:00Z"
        ));
        // add extras as Map.of constructor above is full
        outputMap.put(JBS_REGION_ID, "region_jbs");
        outputMap.put(JBS_LOCATION_ID, "location_jbs");

        // add or override with extra values if needed
        if (MapUtils.isNotEmpty(overrideMapValues)) {
            outputMap.putAll(overrideMapValues);
        }

        return outputMap;
    }

    public static String formatJudicialTestOutputLocation(DroolJudicialTestArguments testArguments,
                                                          String scenarioOutputPath) {
        if (StringUtils.isEmpty(testArguments.getOutputLocation())) {
            return null;
        }
        return DROOL_JUDICIAL_TEST_OUTPUT_PATH + testArguments.getOutputLocation() + scenarioOutputPath;
    }

    public static Map<String, String> generateCommonJudicialOverrideMapValues(String appointmentType, String region) {
        Map<String, String> overrideMapValues = new HashMap<>();

        // NB: these should match codes in UtilityFunctions.getAppointmentTypeFromAppointment(...)
        switch (appointmentType) {
            case JudicialAccessProfile.AppointmentType.FEE_PAID -> {
                overrideMapValues.put(APPOINTMENT_TYPE, "Fee-Paid");
                overrideMapValues.put(CONTRACT_TYPE_ID, "1");
            }
            case JudicialAccessProfile.AppointmentType.VOLUNTARY -> {
                overrideMapValues.put(APPOINTMENT_TYPE, "Voluntary");
                overrideMapValues.put(CONTRACT_TYPE_ID, "2");
            }
            case JudicialAccessProfile.AppointmentType.SPTW -> {
                overrideMapValues.put(APPOINTMENT_TYPE, "SPTW-50%");
                overrideMapValues.put(CONTRACT_TYPE_ID, "5");
            }
            default -> {
                overrideMapValues.put(APPOINTMENT_TYPE, "Salaried");
                overrideMapValues.put(CONTRACT_TYPE_ID, "0");
            }
        }

        if (StringUtils.isNotEmpty(region)) {
            addRegionOverrideMapValues(overrideMapValues, region);
        }

        return overrideMapValues;
    }

    public static List<TestScenario> generateJudicialHappyPathScenarios(DroolJudicialTestArguments testArguments,
                                                                        boolean includeBookingScenario) {
        List<TestScenario> testScenarios = new ArrayList<>();

        Map<String, String> overrideMapValues = testArguments.getOverrideMapValues();

        String bookingScenario = includeBookingScenario ? "WithBooking" : "WithoutBooking";
        String scenarioOutputPath = HAPPY_PATH + "/" + bookingScenario;

        testScenarios.add(createTestScenarioBuilderWithDefaults(testArguments)
            .description(String.format(HAPPY_PATH_DESCRIPTION, ALL_DATES_SUPPLIED, bookingScenario))
            .outputLocation(
                formatJudicialTestOutputLocation(testArguments, scenarioOutputPath + "AllDatesSupplied/")
            )
            .replaceMap(createDefaultJudicialReplaceMap(overrideMapValues))
            .build());

        testScenarios.add(createTestScenarioBuilderWithDefaults(testArguments)
            .description(String.format(HAPPY_PATH_DESCRIPTION, NO_APPOINTMENT_END_DATE, bookingScenario))
            .outputLocation(
                formatJudicialTestOutputLocation(testArguments, scenarioOutputPath + "NoAppointmentEndDate/")
            )
            .replaceMap(
                useNullDateInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), APPOINTMENT_END_TIME)
            )
            .build());

        testScenarios.add(createTestScenarioBuilderWithDefaults(testArguments)
            .description(String.format(HAPPY_PATH_DESCRIPTION, NO_AUTHORISATION_END_DATE, bookingScenario))
            .outputLocation(
                formatJudicialTestOutputLocation(testArguments, scenarioOutputPath + "NoAuthorisationEndDate/")
            )
            .replaceMap(
                useNullDateInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), AUTHORISATION_END_TIME)
            )
            .build());

        if (testArguments.isAdditionalRoleTest()) {
            testScenarios.add(createTestScenarioBuilderWithDefaults(testArguments)
                .description(String.format(HAPPY_PATH_DESCRIPTION, NO_ADDITIONAL_ROLE_END_DATE, bookingScenario))
                .outputLocation(
                    formatJudicialTestOutputLocation(testArguments, scenarioOutputPath + "NoAdditionalRoleEndDate/")
                )
                .replaceMap(
                    useNullDateInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), ROLE_END_TIME)
                )
                .build());
        }

        if (includeBookingScenario) {
            testScenarios.add(createTestScenarioBuilderWithDefaults(testArguments)
                .description(String.format(HAPPY_PATH_DESCRIPTION, NO_BOOKING_END_DATE, bookingScenario))
                .outputLocation(
                    formatJudicialTestOutputLocation(testArguments, scenarioOutputPath + "NoBookingEndDate/")
                )
                .replaceMap(
                    useNullDateInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), JBS_END_TIME)
                )
                .build());
        }

        return testScenarios;
    }


    public static List<TestScenario> generateJudicialNegativePathScenarios(DroolJudicialTestArguments testArguments) {
        List<TestScenario> testScenarios = new ArrayList<>();

        Map<String, String> overrideMapValues = testArguments.getOverrideMapValues();

        // NB: JBS only returns valid bookings so no need to test with expired booking end date

        testScenarios.add(createTestScenarioBuilderWithDefaults(testArguments)
            .description(String.format(NEGATIVE_TEST_DESCRIPTION, APPOINTMENT_END_DATE_EXPIRED))
            .outputLocation(
                formatJudicialTestOutputLocation(testArguments, "NegativeTest/AppointmentEndDateExpired/")
            )
            .replaceMap(
                expireDateInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), APPOINTMENT_END_TIME)
            )
            .build());

        testScenarios.add(createTestScenarioBuilderWithDefaults(testArguments)
            .description(String.format(NEGATIVE_TEST_DESCRIPTION, AUTHORISATION_END_DATE_EXPIRED))
            .outputLocation(
                formatJudicialTestOutputLocation(testArguments, "NegativeTest/AuthorisationEndDateExpired/")
            )
            .replaceMap(
                expireDateInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), AUTHORISATION_END_TIME)
            )
            .build());

        if (testArguments.isAdditionalRoleTest()) {
            String description = String.format(NEGATIVE_TEST_DESCRIPTION, ADDITIONAL_ROLE_END_DATE_EXPIRED);
            String scenarioOutputPath = "NegativeTest/AdditionalRoleEndDateExpired/";

            if (StringUtils.isNotEmpty(testArguments.getAdditionalRoleExpiredFallbackFileName())) {
                description += " - using RAS fallback file";
                scenarioOutputPath += "WithFallback/";
            }

            testScenarios.add(createTestScenarioBuilderWithDefaults(testArguments)
                .description(description)
                .outputLocation(
                    formatJudicialTestOutputLocation(testArguments, scenarioOutputPath)
                )
                .replaceMap(
                    expireDateInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), ROLE_END_TIME)
                )
                // NB: may need to override RAS request file to use fallback template when additional role is expired
                .overrideRasRequestFileName(testArguments.getAdditionalRoleExpiredFallbackFileName())
                .build());
        }

        testScenarios.add(createTestScenarioBuilderWithDefaults(testArguments)
            .description(String.format(NEGATIVE_TEST_DESCRIPTION, SOFT_DELETE_FLAG_SET))
            .outputLocation(
                formatJudicialTestOutputLocation(testArguments, "NegativeTest/SoftDeleteFlagSet/")
            )
            .replaceMap(
                setBooleanInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), DELETED_FLAG, true)
            )
            .build());

        return testScenarios;
    }

    public static DroolJudicialTestArgumentOverrides generateOverrideFlagOffCatchAll(
        FeatureFlagEnum overrideTurnOffFlag
    ) {
        // This is a catch-all override tha will match on all test arguments.
        // It should be the final override used in Flag Off Tests, so we repeat all test arguments with the flag off,
        // even those without any change in behaviour.
        return DroolJudicialTestArgumentOverrides.builder()
            .overrideTurnOffFlags(List.of(overrideTurnOffFlag))
            .build();
    }

    public static DroolJudicialTestArgumentOverrides generateOverrideWhenNotSupported(
        String findJrdResponseFileName,
        FeatureFlagEnum overrideTurnOffFlag
    ) {
        return generateOverrideWhenNotSupported(
            "NotSupported",
            findJrdResponseFileName,
            null,
            overrideTurnOffFlag != null ? List.of(overrideTurnOffFlag) : null
        );
    }

    public static DroolJudicialTestArgumentOverrides generateOverrideWhenNotSupported(
        String overrideDescription,
        String findJrdResponseFileName,
        Map<String, String> findOverrideMapValues,
        List<FeatureFlagEnum> overrideTurnOffFlags
    ) {
        return DroolJudicialTestArgumentOverrides.builder()
            .overrideDescription(overrideDescription)
            .findJrdResponseFileName(findJrdResponseFileName)
            .findOverrideMapValues(findOverrideMapValues)
            .overrideRasRequestFileNameWithoutBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
            .overrideRasRequestFileNameWithBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
            .overrideTurnOffFlags(overrideTurnOffFlags)
            .build();
    }

    public static DroolJudicialTestArgumentOverrides generateOverrideWhenSptwNotSupported(
        String findJrdResponseFileName,
        FeatureFlagEnum overrideTurnOffFlag
    ) {
        return generateOverrideWhenNotSupported(
            "SptwNotSupported",
            findJrdResponseFileName,
            Map.of(
                APPOINTMENT_TYPE, SPTW_TEST_APPOINTMENT_TYPE,
                CONTRACT_TYPE_ID, SPTW_TEST_CONTRACT_TYPE_ID
            ),
            overrideTurnOffFlag != null ? List.of(overrideTurnOffFlag) : null
        );
    }

    @SuppressWarnings({"SameParameterValue"})
    public static List<DroolJudicialTestArguments> generateStandardFeePaidTestArguments(String jrdResponseFileName,
                                                                                        String rasRequestFileName,
                                                                                        boolean additionalRoleTest) {
        return generateStandardFeePaidTestArguments(jrdResponseFileName,
            rasRequestFileName,
            additionalRoleTest,
            true // use separate withBookings and withoutBookings output file
        );

    }

    public static List<DroolJudicialTestArguments> generateStandardFeePaidTestArguments(String jrdResponseFileName,
                                                                                        String rasRequestFileName) {
        return generateStandardFeePaidTestArguments(
            jrdResponseFileName,
            rasRequestFileName,
            false, // default to false, i.e. NOT an Additional Role Test
            true // default to use separate withBookings and withoutBookings output file
        );
    }

    @SuppressWarnings({"SameParameterValue"})
    public static List<DroolJudicialTestArguments> generateStandardFeePaidTestArguments(String jrdResponseFileName,
                                                                                        String rasRequestFileName,
                                                                                        boolean additionalRoleTest,
                                                                                        boolean useWithBookings) {
        return List.of(
            DroolJudicialTestArguments.builder()
                .jrdResponseFileName(jrdResponseFileName)
                .rasRequestFileNameWithBooking(rasRequestFileName + (useWithBookings ? "__withBooking" : ""))
                .rasRequestFileNameWithoutBooking(rasRequestFileName + (useWithBookings ? "__withoutBooking" : ""))
                .additionalRoleTest(additionalRoleTest)
                .overrideMapValues(
                    generateCommonJudicialOverrideMapValues(FEE_PAID, REGION_02_MIDLANDS)
                )
                .build()
        );

    }

    public static List<DroolJudicialTestArguments> generateStandardSalariedTestArguments(String jrdResponseFileName,
                                                                                         String rasRequestFileName) {
        return generateStandardSalariedTestArguments(
            jrdResponseFileName,
            rasRequestFileName,
            false // default to false, i.e. NOT an Additional Role Test
        );
    }

    public static List<DroolJudicialTestArguments> generateStandardSalariedTestArguments(String jrdResponseFileName,
                                                                                         String rasRequestFileName,
                                                                                         boolean additionalRoleTest) {
        return generateStandardSalariedTestArguments(
            jrdResponseFileName,
            rasRequestFileName,
            additionalRoleTest,
            null
        );
    }

    public static List<DroolJudicialTestArguments> generateStandardSalariedTestArguments(
        String jrdResponseFileName,
        String rasRequestFileName,
        String additionalRoleExpiredFallbackFileName
    ) {
        return generateStandardSalariedTestArguments(
            jrdResponseFileName,
            rasRequestFileName,
            // must be additional role test if using fallback file name
            StringUtils.isNotEmpty(additionalRoleExpiredFallbackFileName),
            additionalRoleExpiredFallbackFileName
        );
    }

    public static List<DroolJudicialTestArguments> generateStandardSalariedTestArguments(
        String jrdResponseFileName,
        String rasRequestFileName,
        boolean additionalRoleTest,
        String additionalRoleExpiredFallbackFileName
    ) {
        List<DroolJudicialTestArguments> arguments = new ArrayList<>();

        arguments.add(
            DroolJudicialTestArguments.builder()
                .description("SALARIED")
                .jrdResponseFileName(jrdResponseFileName)
                .rasRequestFileNameWithBooking(rasRequestFileName)
                // NB: With and Without Booking test output will match for these scenarios
                .rasRequestFileNameWithoutBooking(rasRequestFileName)
                .additionalRoleTest(additionalRoleTest)
                .additionalRoleExpiredFallbackFileName(additionalRoleExpiredFallbackFileName)
                .overrideMapValues(
                    generateCommonJudicialOverrideMapValues(SALARIED, REGION_02_MIDLANDS)
                )
                .build()
        );

        // expand to include additional tests for SPTW
        arguments.addAll(
            DroolJudicialTestArgumentsHelper.cloneListOfSalariedTestArgumentsForSptw(arguments)
        );

        return arguments;

    }

    public static List<DroolJudicialTestArguments> overrideTestArguments(
        List<DroolJudicialTestArguments> inputArguments,
        List<DroolJudicialTestArgumentOverrides> overrides
    ) {
        List<DroolJudicialTestArguments> outputArguments = new ArrayList<>();

        // for each test argument: find first override that matches and apply or just use clone of original if none
        inputArguments.forEach(argument -> {
            var findOverride = overrides.stream().filter(testOverride ->
                (
                    StringUtils.isEmpty(testOverride.getFindJrdResponseFileName())
                    || testOverride.getFindJrdResponseFileName().equals(argument.getJrdResponseFileName())
                )
                    && matchOverrideMapValues(
                    argument.getOverrideMapValues(),
                    testOverride.getFindOverrideMapValues()
                )
            ).findFirst();

            var argumentBuilder = argument.cloneBuilder();

            if (findOverride.isPresent()) {
                var override = findOverride.get();

                // NB: use flag off list in description to help identify test reports
                var overrideDescription = CollectionUtils.isNotEmpty(override.getOverrideTurnOffFlags())
                    ? expandDescription(
                        override.getOverrideDescription(),
                        "TurnOffFlags__" + String.join("__",
                            override.getOverrideTurnOffFlags().stream()
                                .map(Enum::name)
                                .toList()
                        )
                    )
                    : override.getOverrideDescription();

                if (overrideDescription != null) {
                    argumentBuilder.description(
                        expandDescription(
                            argument.getDescription(),
                            overrideDescription
                        )
                    );
                }
                if (override.getOverrideRasRequestFileNameWithoutBooking() != null) {
                    argumentBuilder.rasRequestFileNameWithoutBooking(
                        override.getOverrideRasRequestFileNameWithoutBooking()
                    );
                }
                if (override.getOverrideRasRequestFileNameWithBooking() != null) {
                    argumentBuilder.rasRequestFileNameWithBooking(
                        override.getOverrideRasRequestFileNameWithBooking()
                    );
                }
                if (override.getOverrideAdditionalRoleExpiredFallbackFileName() != null) {
                    argumentBuilder.additionalRoleExpiredFallbackFileName(
                        override.getOverrideAdditionalRoleExpiredFallbackFileName()
                    );
                }
                if (CollectionUtils.isNotEmpty(override.getOverrideTurnOffFlags())) {
                    argumentBuilder.turnOffFlags(override.getOverrideTurnOffFlags());
                }
            }

            outputArguments.add(argumentBuilder.build());
        });

        return outputArguments;
    }

    private static boolean matchOverrideMapValues(Map<String, String> overrideMapValues,
                                                  Map<String, String> matchOnOverrideMapValues) {
        if (matchOnOverrideMapValues == null || matchOnOverrideMapValues.isEmpty()) {
            return true; // default when not matching on any specific values
        }
        if (overrideMapValues == null || overrideMapValues.isEmpty()) {
            return false; // default when test argument has no override map values to try and match against
        }

        // search for any matchMap value that DOES NOT MATCH
        var foundMissingMatch = matchOnOverrideMapValues.entrySet().stream().anyMatch(entry -> {
            if (overrideMapValues.containsKey(entry.getKey())) {
                return !overrideMapValues.get(entry.getKey()).equals(entry.getValue());
            }
            return true; // i.e. no match if key missing
        });

        // return true if no missing match found (i.e. all match checks must have passed)
        return !foundMissingMatch;
    }

    private static TestScenarioBuilder createTestScenarioBuilderWithDefaults(DroolJudicialTestArguments testArguments) {
        return TestScenario.builder()
            .jurisdiction(testArguments.getJurisdiction())
            .testGroup(testArguments.getTestGroup())
            .testName(testArguments.getTestName());
    }

    private static String formatDisplayName(DroolJudicialTestArguments args,
                                            String jurisdiction) {
        return jurisdiction + ": " + joinFileNameAndDescription(args, "__");
    }

    private static String formatOutputLocation(DroolJudicialTestArguments args,
                                               String jurisdiction) {
        return jurisdiction + "/" + joinFileNameAndDescription(args, "/") + "/";
    }

    private static String formatJrdResponseFileName(String fileName, String jurisdiction) {
        return jurisdiction + "/InputFromJrd/" + fileName;
    }

    private static String formatRasRequestFileName(String fileName, String jurisdiction) {
        if (StringUtils.isEmpty(fileName)) {
            return null;
        }

        return EMPTY_ROLE_ASSIGNMENT_TEMPLATE.equals(fileName)
            ? EMPTY_ROLE_ASSIGNMENT_TEMPLATE
            : jurisdiction + "/OutputToRas/" + fileName;
    }

    private static String formatRasRequestFileNameWithSuffix(String fileName, String suffix) {
        return EMPTY_ROLE_ASSIGNMENT_TEMPLATE.equals(fileName)
            ? EMPTY_ROLE_ASSIGNMENT_TEMPLATE
            : fileName + "__" + suffix;
    }

    private static String formatTestName(DroolJudicialTestArguments args) {
        return joinFileNameAndDescription(args, "__");
    }

    private static String joinFileNameAndDescription(DroolJudicialTestArguments args, String separator) {

        return StringUtils.isEmpty(args.getDescription())
            ? args.getJrdResponseFileName()
            : args.getJrdResponseFileName() + separator + args.getDescription();
    }

}
