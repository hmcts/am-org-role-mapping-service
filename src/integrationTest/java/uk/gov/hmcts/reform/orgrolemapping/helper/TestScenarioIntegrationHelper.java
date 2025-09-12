package uk.gov.hmcts.reform.orgrolemapping.helper;

import org.apache.commons.collections4.MapUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestScenarioIntegrationHelper {

    public static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    // COMMON replace values
    public static final String IDAM_ID = "[[IDAM_ID]]";
    public static final String ANY_UUID = "[[ANY_UUID]]";
    public static final String ANY_DATE_TIME = "[[ANY_DATE_TIME]]";
    public static final String NOW_DATE_TIME = "[[NOW_DATE_TIME]]";

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

}
