package uk.gov.hmcts.reform.orgrolemapping.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AppointmentV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.buildJudicialAccessProfile;

class UtilityFunctionsTest {

    static final String FALLBACK_APPOINTMENT_TYPE = "FALLBACK_APPOINTMENT_TYPE";

    @ParameterizedTest
    @CsvSource(
        value = {
            "0," + JudicialAccessProfile.AppointmentType.SALARIED,
            "1," + JudicialAccessProfile.AppointmentType.FEE_PAID,
            "2," + JudicialAccessProfile.AppointmentType.VOLUNTARY,
            "3," + JudicialAccessProfile.AppointmentType.SPTW,
            "4," + JudicialAccessProfile.AppointmentType.SPTW,
            "5," + JudicialAccessProfile.AppointmentType.SPTW,
            "6," + JudicialAccessProfile.AppointmentType.SPTW,
            "7," + JudicialAccessProfile.AppointmentType.SPTW,
            "8," + JudicialAccessProfile.AppointmentType.SPTW,
            "9," + JudicialAccessProfile.AppointmentType.SPTW,
            "unknown," + FALLBACK_APPOINTMENT_TYPE,
            "null," + FALLBACK_APPOINTMENT_TYPE
        },
        nullValues = { "null" }
    )
    void shouldReturnCorrectAppointmentTypeFromAppointment(String inputContractTypeId, String expectedOutput) {

        // GIVEN
        var appointment = AppointmentV2.builder()
                .contractTypeId(inputContractTypeId)
                .appointmentType(FALLBACK_APPOINTMENT_TYPE)
                .build();

        // WHEN
        var output = UtilityFunctions.getAppointmentTypeFromAppointment(appointment);

        // THEN
        assertEquals(expectedOutput, output);
    }


    @Test
    void shouldReturnCorrectJurisdictionFromServiceCodeBBA3() {
        assertEquals("SSCS", UtilityFunctions.getJurisdictionFromServiceCode("BBA3"));
    }

    @Test
    void shouldReturnCorrectJurisdictionFromServiceCodeAAA6() {
        assertEquals("CIVIL", UtilityFunctions.getJurisdictionFromServiceCode("AAA6"));
    }

    @Test
    void shouldReturnCorrectJurisdictionFromServiceCodeAAA7() {
        assertEquals("CIVIL", UtilityFunctions.getJurisdictionFromServiceCode("AAA7"));
    }

    @Test
    void shouldReturnCorrectJurisdictionFromServiceCodeABA5() {
        assertEquals("PRIVATELAW", UtilityFunctions.getJurisdictionFromServiceCode("ABA5"));
    }

    @Test
    void shouldReturnCorrectJurisdictionFromServiceCodeABA3() {
        assertEquals("PUBLICLAW", UtilityFunctions.getJurisdictionFromServiceCode("ABA3"));
    }

    @Test
    void shouldReturnCorrectJurisdictionFromServiceCodeBHA1() {
        assertEquals("EMPLOYMENT", UtilityFunctions.getJurisdictionFromServiceCode("BHA1"));
    }

    @Test
    void shouldReturnCorrectJurisdictionFromServiceCodeBFA1() {
        assertEquals("IA", UtilityFunctions.getJurisdictionFromServiceCode("BFA1"));
    }

    @Test
    void shouldReturnNullFromServiceCodeNotRecognised() {
        assertNull(UtilityFunctions.getJurisdictionFromServiceCode("XXX6"));
    }

    @Test
    void shouldReturnCorrectLocalDateTimeFromLocalDate() {

        // GIVEN
        LocalDate input = LocalDate.of(2020, 1, 2);
        LocalDateTime expectedOutput = LocalDateTime.of(2020, 1, 2, 0,0);

        // WHEN
        var output = UtilityFunctions.localDateToLocalDateTime(input);

        // THEN
        assertEquals(expectedOutput, output);

    }

    @Test
    @SuppressWarnings({"ConstantValue"})
    void shouldReturnNullLocalDateTimeFromNullLocalDate() {
        assertNull(UtilityFunctions.localDateToLocalDateTime(null));
    }

    @Test
    void shouldReturnCorrectZonedDateTimeTimeFromLocalDate() {

        // GIVEN
        LocalDate input = LocalDate.of(2020, 1, 2);
        ZonedDateTime expectedOutput = ZonedDateTime.of(2020, 1, 2, 0, 0, 0, 0, ZoneId.of("UTC"));

        // WHEN
        var output = UtilityFunctions.localDateToZonedDateTime(input);

        // THEN
        assertEquals(expectedOutput, output);

    }

    @Test
    void shouldReturnNullZonedDateTimeFromNullLocalDate() {
        assertNull(UtilityFunctions.localDateToZonedDateTime(null));
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "empty,empty",
            "null,empty",
            "singleValue,singleValue",
            "repeatedValue|repeatedValue,repeatedValue",
            "multipleValue1|repeatedValue|repeatedValue|multipleValue2,multipleValue1|repeatedValue|multipleValue2",
            "ignoreBlanks||ignoreWhitespace| ,ignoreBlanks|ignoreWhitespace"
        },
        nullValues = { "null" }
    )
    void shouldReturnCorrectListFromStringListToDistinctList(String csvInput, String csvExpectedOutput) {

        // GIVEN
        List<String> input = splitCsvSourceParamIntoList(csvInput);
        List<String> expectedOutput = splitCsvSourceParamIntoList(csvExpectedOutput);

        // WHEN
        var output = UtilityFunctions.stringListToDistinctList(input);

        // THEN
        assertNotNull(output);
        assertEquals(expectedOutput.size(), output.size());
        assertTrue(output.containsAll(expectedOutput));

    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "null,null,empty",
            "null,empty,empty",
            "singleValue,null,singleValue",
            "repeatedValue,repeatedValue,repeatedValue",
            "repeatedValue|repeatedValue,null,repeatedValue",
            "multipleValue1|repeatedValue,repeatedValue|multipleValue2,multipleValue1|repeatedValue|multipleValue2"
        },
        nullValues = { "null" }
    )
    void shouldReturnCorrectListFrom_getUserIdsFromJudicialAccessProfileMap(String csvInput1,
                                                                            String csvInput2,
                                                                            String csvExpectedOutput) {

        // GIVEN
        List<String> input1 = splitCsvSourceParamIntoList(csvInput1);
        List<String> input2 = splitCsvSourceParamIntoList(csvInput2);
        Map<String, Set<UserAccessProfile>> inputMap = new HashMap<>();
        if (input1 != null) {
            inputMap.put(csvInput1, createJudicialAccessProfileSet(input1));
        }
        if (input2 != null) {
            inputMap.put(csvInput2, createJudicialAccessProfileSet(input2));
        }
        List<String> expectedOutput = splitCsvSourceParamIntoList(csvExpectedOutput);

        // WHEN
        var output = UtilityFunctions.getUserIdsFromJudicialAccessProfileMap(inputMap);

        // THEN
        assertNotNull(output);
        assertEquals(expectedOutput.size(), output.size());
        assertTrue(output.containsAll(expectedOutput));
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "null,empty,null,null,null",
            "400,empty,null,null,null",
            "null,1|2|3|4|5,1|2|3|4|5,null,null",
            "400,1|2|3|4|5,1|2|3|4|5,null,null",
            "3,1|2|3|4|5,1|2|3,4|5,null",
            "2,1|2|3|4|5,1|2,3|4,5",
            "bad-batch-size,1|2|3|4|5,1|2|3|4|5,null,null",
        },
        nullValues = { "null" }
    )
    void shouldReturnCorrectBatchesFrom_splitListIntoBatches(String inputPageSize,
                                                             String csvInputList,
                                                             String csvExpectedBatch1,
                                                             String csvExpectedBatch2,
                                                             String csvExpectedBatch3) {
        // GIVEN
        final List<String> inputList = splitCsvSourceParamIntoList(csvInputList);
        final List<String> expectedBatch1 = splitCsvSourceParamIntoList(csvExpectedBatch1);
        final List<String> expectedBatch2 = splitCsvSourceParamIntoList(csvExpectedBatch2);
        final List<String> expectedBatch3 = splitCsvSourceParamIntoList(csvExpectedBatch3);

        // WHEN
        var output = UtilityFunctions.splitListIntoBatches(inputList, inputPageSize);

        // THEN
        assertNotNull(output);
        assertBatchList(output, 1, expectedBatch1);
        assertBatchList(output, 2, expectedBatch2);
        assertBatchList(output, 3, expectedBatch3);
    }

    private void assertBatchList(List<List<String>> output, int batchNumber, List<String> expectedBatch) {

        if (expectedBatch == null) {
            assertTrue(output.size() < batchNumber);
        } else {
            assertTrue(output.size() >= batchNumber);
            List<String> batch = output.get(batchNumber - 1);
            assertNotNull(batch);
            assertEquals(expectedBatch.size(), batch.size());
            assertTrue(expectedBatch.containsAll(batch));
        }
    }

    private Set<UserAccessProfile> createJudicialAccessProfileSet(List<String> inputList) {

        return inputList.stream()
            .map(userId -> {
                var accessProfile = buildJudicialAccessProfile();
                accessProfile.setUserId(userId);
                return accessProfile;
            }).collect(Collectors.toSet());
    }

    private List<String> splitCsvSourceParamIntoList(String csvParam) {
        if (csvParam == null) {
            return null;
        } else if (csvParam.equals("empty")) {
            return new ArrayList<>();
        } else {
            return Arrays.stream(csvParam.split("\\|")).toList();
        }
    }

    @Test
    void testHelperFunction() {
        assertNull(splitCsvSourceParamIntoList(null));
        assertTrue(splitCsvSourceParamIntoList("empty").isEmpty());

        List<String> singleTestOutput = splitCsvSourceParamIntoList("test");
        assertEquals(1, singleTestOutput.size());
        assertThat(singleTestOutput, containsInAnyOrder("test"));


        List<String> multipleTestOutput = splitCsvSourceParamIntoList("test1|test2");
        assertEquals(2, multipleTestOutput.size());
        assertThat(multipleTestOutput, containsInAnyOrder("test1","test2"));
    }

}
