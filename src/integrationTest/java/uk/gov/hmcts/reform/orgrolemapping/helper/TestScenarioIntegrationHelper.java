package uk.gov.hmcts.reform.orgrolemapping.helper;

import org.apache.commons.collections4.MapUtils;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.TestScenario;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TestScenarioIntegrationHelper {

    public static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    // COMMON replace values
    public static final String IDAM_ID = "[[IDAM_ID]]";
    public static final String ANY_UUID = "[[ANY_UUID]]";
    public static final String ANY_DATE_TIME = "[[ANY_DATE_TIME]]";
    public static final String NOW_DATE_TIME = "[[NOW_DATE_TIME]]";
    public static final String REGION_ID = "[[REGION_ID]]";
    public static final String REGION_NAME = "[[REGION_NAME]]";
    public static final String REGION_00_DEFAULT = "0";
    public static final String REGION_01_LONDON = "1";
    public static final String REGION_02_MIDLANDS = "2";
    public static final String REGION_03_NORTH_EAST = "3";
    public static final String REGION_04_NORTH_WEST = "4";
    public static final String REGION_05_SOUTH_EAST = "5";
    public static final String REGION_06_SOUTH_WEST = "6";
    public static final String REGION_07_WALES = "7";
    public static final String REGION_11_SCOTLAND = "11";
    public static final String REGION_12_NATIONAL = "12";
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

    public static Map<String, String> generateJudicialOverrideMapValues(String appointmentType, String region) {
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


    public static void addRegionOverrideMapValues(Map<String, String> overrideMapValues, String region) {
        switch (region) {
            case REGION_01_LONDON -> {
                overrideMapValues.put(REGION_ID, REGION_01_LONDON);
                overrideMapValues.put(REGION_NAME, "London");
            }
            case REGION_02_MIDLANDS -> {
                overrideMapValues.put(REGION_ID, REGION_02_MIDLANDS);
                overrideMapValues.put(REGION_NAME, "Midlands");
            }
            case REGION_03_NORTH_EAST -> {
                overrideMapValues.put(REGION_ID, REGION_03_NORTH_EAST);
                overrideMapValues.put(REGION_NAME, "North East");
            }
            case REGION_04_NORTH_WEST -> {
                overrideMapValues.put(REGION_ID, REGION_04_NORTH_WEST);
                overrideMapValues.put(REGION_NAME, "North West");
            }
            case REGION_05_SOUTH_EAST -> {
                overrideMapValues.put(REGION_ID, REGION_05_SOUTH_EAST);
                overrideMapValues.put(REGION_NAME, "South East");
            }
            case REGION_06_SOUTH_WEST -> {
                overrideMapValues.put(REGION_ID, REGION_06_SOUTH_WEST);
                overrideMapValues.put(REGION_NAME, "South West");
            }
            case REGION_07_WALES -> {
                overrideMapValues.put(REGION_ID, REGION_07_WALES);
                overrideMapValues.put(REGION_NAME, "Wales");
            }
            case REGION_11_SCOTLAND -> {
                overrideMapValues.put(REGION_ID, REGION_11_SCOTLAND);
                overrideMapValues.put(REGION_NAME, "Scotland");
            }
            case REGION_12_NATIONAL -> {
                overrideMapValues.put(REGION_ID, REGION_12_NATIONAL);
                overrideMapValues.put(REGION_NAME, "National");
            }
            default -> {
                overrideMapValues.put(REGION_ID, REGION_00_DEFAULT);
                overrideMapValues.put(REGION_NAME, "Default");
            }
        }
    }

    public static List<TestScenario> generateJudicialHappyPathScenarios(boolean includeAdditionalRoleScenario,
                                                                        boolean includeBookingScenario,
                                                                        Map<String, String> overrideMapValues) {
        List<TestScenario> testScenarios = new ArrayList<>();

        testScenarios.add(TestScenario.builder()
            .description(HAPPY_PATH__ALL_DATES_SUPPLIED)
            .replaceMap(createDefaultJudicialReplaceMap(overrideMapValues))
            .build());

        testScenarios.add(TestScenario.builder()
            .description(HAPPY_PATH__NO_APPOINTMENT_END_DATE)
            .replaceMap(
                useNullDateInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), APPOINTMENT_END_TIME)
            )
            .build());

        testScenarios.add(TestScenario.builder()
            .description(HAPPY_PATH__NO_AUTHORISATION_END_DATE)
            .replaceMap(
                useNullDateInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), AUTHORISATION_END_TIME)
            )
            .build());

        if (includeAdditionalRoleScenario) {
            testScenarios.add(TestScenario.builder()
                .description(HAPPY_PATH__NO_ADDITIONAL_ROLE_END_DATE)
                .replaceMap(
                    useNullDateInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), ROLE_END_TIME)
                )
                .build());
        }

        if (includeBookingScenario) {
            testScenarios.add(TestScenario.builder()
                .description(HAPPY_PATH__NO_BOOKING_END_DATE)
                .replaceMap(
                    useNullDateInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), JBS_END_TIME)
                )
                .build());
        }

        return testScenarios;
    }


    public static List<TestScenario> generateJudicialNegativePathScenarios(boolean includeAdditionalRoleScenario,
                                                                           Map<String, String> overrideMapValues) {
        List<TestScenario> testScenarios = new ArrayList<>();

        // NB: JBS only returns valid bookings so no need to test with expired booking end date

        testScenarios.add(TestScenario.builder()
            .description(NEGATIVE_TEST__APPOINTMENT_END_DATE_EXPIRED)
            .replaceMap(
                expireDateInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), APPOINTMENT_END_TIME)
            )
            .build());

        testScenarios.add(TestScenario.builder()
            .description(NEGATIVE_TEST__AUTHORISATION_END_DATE_EXPIRED)
            .replaceMap(
                expireDateInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), AUTHORISATION_END_TIME)
            )
            .build());

        if (includeAdditionalRoleScenario) {
            testScenarios.add(TestScenario.builder()
                .description(NEGATIVE_TEST__ADDITIONAL_ROLE_END_DATE_EXPIRED)
                .replaceMap(
                    expireDateInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), ROLE_END_TIME)
                )
                .build());
        }

        testScenarios.add(TestScenario.builder()
            .description(NEGATIVE_TEST__SOFT_DELETE_FLAG_SET)
            .replaceMap(
                setBooleanInReplaceMap(createDefaultJudicialReplaceMap(overrideMapValues), DELETED_FLAG, true))
            .build());

        return testScenarios;
    }

    public static List<String> getSidamIdsList(List<TestScenario> testScenarios) {
        return new ArrayList<>(getSidamIdsSet(testScenarios));
    }

    public static Set<String> getSidamIdsSet(List<TestScenario> testScenarios) {
        return testScenarios.stream()
            .map(testScenario -> testScenario.getReplaceMap().get(IDAM_ID))
            .collect(Collectors.toSet());
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

    public static Map<String, String> cloneAndExpandReplaceMap(Map<String, String> replaceMap) {
        // add extra values that don't need to match across all the stubs used by test
        return cloneAndOverrideMap(replaceMap, Map.of(
            ANY_UUID, UUID.randomUUID().toString(),
            ANY_DATE_TIME, LocalDateTime.now().minusDays(100).format(DTF),
            NOW_DATE_TIME, LocalDateTime.now().format(DTF)
        ));
    }

    public static Map<String, String> cloneAndOverrideMap(Map<String, String> replaceMap,
                                                          Map<String, String> overrideMapValues) {
        Map<String, String> replaceMapClone = replaceMap == null ? new HashMap<>() : new HashMap<>(replaceMap);

        if (MapUtils.isNotEmpty(overrideMapValues)) {
            replaceMapClone.putAll(overrideMapValues);
        }

        return replaceMapClone;
    }

    public static void adjustMapValueToDtz(Map<String, String> map, String key, int offset) {
        if (map.get(key) != null) {
            map.put(key, LocalDate.parse(map.get(key), DF).plusDays(offset).format(DF) + "T00:00:00Z");
        }
    }

    private static Map<String, String> expireDateInReplaceMap(Map<String, String> replaceMap, String expiredDateKey) {
        replaceMap.put(expiredDateKey, LocalDate.now().minusDays(10).format(DF));
        return replaceMap;
    }

    private static Map<String, String> useNullDateInReplaceMap(Map<String, String> replaceMap, String nullDateKey) {
        replaceMap.put(nullDateKey, null);
        return replaceMap;
    }

    @SuppressWarnings("SameParameterValue")
    private static Map<String, String> setBooleanInReplaceMap(Map<String, String> replaceMap,
                                                              String boolKey,
                                                              boolean boolValue) {
        replaceMap.put(boolKey, Boolean.toString(boolValue));
        return replaceMap;
    }

}
