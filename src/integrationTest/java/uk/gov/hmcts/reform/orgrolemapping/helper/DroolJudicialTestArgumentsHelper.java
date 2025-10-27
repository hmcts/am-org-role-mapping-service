package uk.gov.hmcts.reform.orgrolemapping.helper;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArguments;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.TestScenario;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.TestScenario.TestScenarioBuilder;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.orgrolemapping.drool.BaseDroolTestIntegration.EMPTY_ROLE_ASSIGNMENT_TEMPLATE;
import static uk.gov.hmcts.reform.orgrolemapping.drool.RunJudicialDroolIntegrationTests.DROOL_JUDICIAL_TEST_OUTPUT_PATH;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.DF;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.IDAM_ID;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.addRegionOverrideMapValues;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.cloneAndOverrideMap;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.expireDateInReplaceMap;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.setBooleanInReplaceMap;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.useNullDateInReplaceMap;

@SuppressWarnings({
    "java:S115" // Constant names should comply with a naming convention
})
public class DroolJudicialTestArgumentsHelper {

    private static final String HAPPY_PATH__ALL_DATES_SUPPLIED = "HappyPath - all dates supplied";
    private static final String HAPPY_PATH__NO_APPOINTMENT_END_DATE = "HappyPath - no appointment end date";
    private static final String HAPPY_PATH__NO_AUTHORISATION_END_DATE = "HappyPath - no authorisation end date";
    private static final String HAPPY_PATH__NO_ADDITIONAL_ROLE_END_DATE = "HappyPath - no additional role end date";
    private static final String HAPPY_PATH__NO_BOOKING_END_DATE = "HappyPath - no booking end date";

    private static final String NEGATIVE_TEST__APPOINTMENT_END_DATE_EXPIRED
        = "NegativeTest - appointment end date expired";
    private static final String NEGATIVE_TEST__AUTHORISATION_END_DATE_EXPIRED
        = "NegativeTest - authorisation end date expired";
    private static final String NEGATIVE_TEST__ADDITIONAL_ROLE_END_DATE_EXPIRED
        = "NegativeTest - additional role end date expired";
    private static final String NEGATIVE_TEST__SOFT_DELETE_FLAG_SET = "NegativeTest - soft delete flag set";

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
    // JUDICIAL BOOKING replace values
    public static final String JBS_BEGIN_TIME = "[[JBS_BEGIN_TIME]]";
    public static final String JBS_END_TIME = "[[JBS_END_TIME]]";

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
                    .build()
            )
            .toList();
    }

    public static List<DroolJudicialTestArguments> cloneListOfSalariedTestArgumentsForSptw(
        List<DroolJudicialTestArguments> originalList
    ) {
        // override Appointment Type values with SPTW values
        Map<String, String> overrideMapValues = new HashMap<>();
        overrideMapValues.put(APPOINTMENT_TYPE, "SPTW-50%");
        overrideMapValues.put(CONTRACT_TYPE_ID, "5");

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

        addRegionOverrideMapValues(overrideMapValues, region);

        return overrideMapValues;
    }

    public static List<TestScenario> generateJudicialHappyPathScenarios(DroolJudicialTestArguments testArguments,
                                                                        boolean includeBookingScenario) {
        List<TestScenario> testScenarios = new ArrayList<>();

        Map<String, String> overrideMapValues = testArguments.getOverrideMapValues();

        String scenarioOutputPath = "HappyPath/" + (includeBookingScenario ? "WithBooking/" : "WithoutBooking/");

        testScenarios.add(createTestScenarioBuilderWithDefaults(testArguments)
            .description(HAPPY_PATH__ALL_DATES_SUPPLIED)
            .outputLocation(
                formatJudicialTestOutputLocation(testArguments, scenarioOutputPath + "AllDatesSupplied/")
            )
            .replaceMap(createDefaultJudicialReplaceMap(overrideMapValues))
            .build());

        testScenarios.add(createTestScenarioBuilderWithDefaults(testArguments)
            .description(HAPPY_PATH__NO_APPOINTMENT_END_DATE)
            .outputLocation(
                formatJudicialTestOutputLocation(testArguments, scenarioOutputPath + "NoAppointmentEndDate/")
            )
            .replaceMap(
                useNullDateInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), APPOINTMENT_END_TIME)
            )
            .build());

        testScenarios.add(createTestScenarioBuilderWithDefaults(testArguments)
            .description(HAPPY_PATH__NO_AUTHORISATION_END_DATE)
            .outputLocation(
                formatJudicialTestOutputLocation(testArguments, scenarioOutputPath + "NoAuthorisationEndDate/")
            )
            .replaceMap(
                useNullDateInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), AUTHORISATION_END_TIME)
            )
            .build());

        if (testArguments.isAdditionalRoleTest()) {
            testScenarios.add(createTestScenarioBuilderWithDefaults(testArguments)
                .description(HAPPY_PATH__NO_ADDITIONAL_ROLE_END_DATE)
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
                .description(HAPPY_PATH__NO_BOOKING_END_DATE)
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
            .description(NEGATIVE_TEST__APPOINTMENT_END_DATE_EXPIRED)
            .outputLocation(
                formatJudicialTestOutputLocation(testArguments, "NegativeTest/AppointmentEndDateExpired/")
            )
            .replaceMap(
                expireDateInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), APPOINTMENT_END_TIME)
            )
            .build());

        testScenarios.add(createTestScenarioBuilderWithDefaults(testArguments)
            .description(NEGATIVE_TEST__AUTHORISATION_END_DATE_EXPIRED)
            .outputLocation(
                formatJudicialTestOutputLocation(testArguments, "NegativeTest/AuthorisationEndDateExpired/")
            )
            .replaceMap(
                expireDateInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), AUTHORISATION_END_TIME)
            )
            .build());

        if (testArguments.isAdditionalRoleTest()) {
            testScenarios.add(createTestScenarioBuilderWithDefaults(testArguments)
                .description(NEGATIVE_TEST__ADDITIONAL_ROLE_END_DATE_EXPIRED)
                .outputLocation(
                    formatJudicialTestOutputLocation(testArguments, "NegativeTest/AdditionalRoleEndDateExpired/")
                )
                .replaceMap(
                    expireDateInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), ROLE_END_TIME)
                )
                .build());
        }

        testScenarios.add(createTestScenarioBuilderWithDefaults(testArguments)
            .description(NEGATIVE_TEST__SOFT_DELETE_FLAG_SET)
            .outputLocation(
                formatJudicialTestOutputLocation(testArguments, "NegativeTest/SoftDeleteFlagSet/")
            )
            .replaceMap(
                setBooleanInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), DELETED_FLAG, true))
            .build());

        return testScenarios;
    }

    @SuppressWarnings({"SameParameterValue"})
    public static List<DroolJudicialTestArguments> generateStandardFeePaidTestArguments(String jrdResponseFileName,
                                                                                        String rasRequestFileName,
                                                                                        boolean additionalRoleTest) {
        return List.of(
            DroolJudicialTestArguments.builder()
                .jrdResponseFileName(jrdResponseFileName)
                .rasRequestFileNameWithBooking(rasRequestFileName + "__withBooking")
                .rasRequestFileNameWithoutBooking(rasRequestFileName + "__withoutBooking")
                .additionalRoleTest(additionalRoleTest)
                .overrideMapValues(null)
                .build()
        );

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
        return EMPTY_ROLE_ASSIGNMENT_TEMPLATE.equals(fileName)
            ? fileName
            : jurisdiction + "/OutputToRas/" + fileName;
    }

    private static String formatTestName(DroolJudicialTestArguments args) {
        return joinFileNameAndDescription(args, "__");
    }

    private static String joinFileNameAndDescription(DroolJudicialTestArguments args, String separator) {

        return org.apache.commons.lang.StringUtils.isEmpty(args.getDescription())
            ? args.getJrdResponseFileName()
            : args.getJrdResponseFileName() + separator + args.getDescription();
    }

}
