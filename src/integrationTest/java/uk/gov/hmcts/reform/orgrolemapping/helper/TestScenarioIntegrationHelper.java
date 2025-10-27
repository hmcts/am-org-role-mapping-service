package uk.gov.hmcts.reform.orgrolemapping.helper;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.TestScenario;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class TestScenarioIntegrationHelper {

    public static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    // COMMON replace values
    public static final String IDAM_ID = "[[IDAM_ID]]";
    public static final String ANY_UUID = "[[ANY_UUID]]";
    public static final String ANY_DATE_TIME = "[[ANY_DATE_TIME]]";
    public static final String NOW_DATE_TIME = "[[NOW_DATE_TIME]]";
    public static final String RANDOM_ID = "[[RANDOM_ID]]";
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

    private static final Random RANDOM = new Random();


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


    public static List<String> getSidamIdsList(List<TestScenario> testScenarios) {
        return new ArrayList<>(getSidamIdsSet(testScenarios));
    }

    public static Set<String> getSidamIdsSet(List<TestScenario> testScenarios) {
        return testScenarios.stream()
            .map(testScenario -> testScenario.getReplaceMap().get(IDAM_ID))
            .collect(Collectors.toSet());
    }

    public static Map<String, String> cloneAndExpandReplaceMap(Map<String, String> replaceMap) {
        // add extra values that don't need to match across all the stubs used by test
        return cloneAndOverrideMap(replaceMap, Map.of(
            ANY_UUID, UUID.randomUUID().toString(),
            RANDOM_ID, String.valueOf(RANDOM.nextInt(1000000)),
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

    public static void writeJsonToTestScenarioOutput(String json, TestScenario testScenario, String outputFileName) {
        writeJsonToOutput(json, testScenario.getOutputLocation(), outputFileName);
    }

    @SneakyThrows
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public static void writeJsonToOutput(String json, String outputLocation, String outputFileName) {
        log.info("--- Test Output: {} ---\n{}", outputFileName, json);

        if (!StringUtils.isEmpty(outputLocation)) {
            File outputDirectory = new File(outputLocation);
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }

            String outputFilePath = outputLocation + outputFileName + ".json";
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
            writer.write(json);
            writer.close();
        }
    }

    public static Map<String, String> expireDateInReplaceMap(Map<String, String> replaceMap, String expiredDateKey) {
        replaceMap.put(expiredDateKey, LocalDate.now().minusDays(10).format(DF));
        return replaceMap;
    }

    public static Map<String, String> useNullDateInReplaceMap(Map<String, String> replaceMap, String nullDateKey) {
        replaceMap.put(nullDateKey, null);
        return replaceMap;
    }

    @SuppressWarnings("SameParameterValue")
    public static Map<String, String> setBooleanInReplaceMap(Map<String, String> replaceMap,
                                                              String boolKey,
                                                              boolean boolValue) {
        replaceMap.put(boolKey, Boolean.toString(boolValue));
        return replaceMap;
    }

}
