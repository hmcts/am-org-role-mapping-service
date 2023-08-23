package uk.gov.hmcts.reform.orgrolemapping.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AppointmentV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.Judicial;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilityFunctionsTest {

    static final String FALLBACK_APPOINTMENT_TYPE = "FALLBACK_APPOINTMENT_TYPE";

    @ParameterizedTest
    @CsvSource(
        value = {
            "0," + Judicial.ContractType.SALARIED,
            "1," + Judicial.ContractType.FEE_PAID,
            "2," + Judicial.ContractType.VOLUNTARY,
            "3," + Judicial.ContractType.SPTW,
            "4," + Judicial.ContractType.SPTW,
            "5," + Judicial.ContractType.SPTW,
            "6," + Judicial.ContractType.SPTW,
            "7," + Judicial.ContractType.SPTW,
            "8," + Judicial.ContractType.SPTW,
            "9," + Judicial.ContractType.SPTW,
            "unknown," + FALLBACK_APPOINTMENT_TYPE,
            "null," + FALLBACK_APPOINTMENT_TYPE
        },
        nullValues = { "null" }
    )
    void shouldReturnCorrectContractTypeFromAppointment(String inputContractTypeId, String expectedOutput) {

        // GIVEN
        var appointment = AppointmentV2.builder()
                .contractTypeId(inputContractTypeId)
                .appointmentType(FALLBACK_APPOINTMENT_TYPE)
                .build();

        // WHEN
        var output = UtilityFunctions.getContractTypeFromAppointment(appointment);

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
        List<String> input = csvInput != null
            ? (!csvInput.equals("empty") ? (Arrays.stream(csvInput.split("\\|")).toList()) : new ArrayList<>())
            : null;
        List<String> expectedOutput = !csvExpectedOutput.equals("empty")
            ? Arrays.stream(csvExpectedOutput.split("\\|")).toList()
            : new ArrayList<>();

        // WHEN
        var output = UtilityFunctions.stringListToDistinctList(input);

        // THEN
        assertNotNull(output);
        assertEquals(expectedOutput.size(), output.size());
        assertTrue(output.containsAll(expectedOutput));

    }

}
