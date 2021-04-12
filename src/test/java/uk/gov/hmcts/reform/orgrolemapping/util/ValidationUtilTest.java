package uk.gov.hmcts.reform.orgrolemapping.util;

import org.apache.poi.ss.formula.functions.T;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.NUMBER_TEXT_HYPHEN_PATTERN;

class ValidationUtilTest {


    @Test
    void shouldThrowBadRequestException_ValidateLists() {
        List<T> list = List.of();
        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.validateLists(list)
        );
    }

    @Test
    void shouldValidate_ValidateLists() {
        List<String> list = new ArrayList<>();
        list.add("entry");
        Assertions.assertDoesNotThrow(() ->
                ValidationUtil.validateLists(list)
        );
    }

    @Test
    void shouldValidate_ValidateInputParams() {
        Assertions.assertDoesNotThrow(() ->
                ValidationUtil.validateInputParams(NUMBER_TEXT_HYPHEN_PATTERN, "request-1")
        );
    }

    @Test
    void shouldThrow_EmptyInputParams() {
        Assertions.assertThrows(BadRequestException.class, () ->
                ValidationUtil.validateInputParams(NUMBER_TEXT_HYPHEN_PATTERN, "")
        );
    }

    @Test
    void shouldThrow_DoesNotMatchPattern() {
        Assertions.assertThrows(BadRequestException.class, () ->
                ValidationUtil.validateInputParams(NUMBER_TEXT_HYPHEN_PATTERN, "req@@2")
        );
    }

    @Test
    void shouldValidate_ValidateDateTime() {
        Assertions.assertDoesNotThrow(() ->
                ValidationUtil.validateDateTime(LocalDateTime.now().plusDays(1L).toString(),
                        LocalDateTime.now().plusDays(2L).toString())
        );
    }

    @Test
    void shouldNotThrow_ValidateDateTimeLength() {
        Assertions.assertDoesNotThrow(() ->
                ValidationUtil.validateDateTime("2021-11-27T15:12",
                        "2021-11-27T15:12")
        );
    }

    @Test
    void shouldThrow_ValidateDateTimeLength() {
        String dateAfter =  LocalDateTime.now().plusDays(2L).toString();
        Assertions.assertThrows(BadRequestException.class, () ->
                ValidationUtil.validateDateTime("2020-11-27T15", dateAfter)
        );
    }

    @Test
    void shouldThrow_cannotBePriorToCurrentDate() {
        String time = LocalDateTime.now().minusDays(1L).toString();
        String time2 = LocalDateTime.now().minusDays(1L).toString();
        Assertions.assertThrows(BadRequestException.class, () ->
                ValidationUtil.validateDateTime(time, time2)
        );
    }

    @Test
    void shouldThrow_IncorrectDateFormat() {
        String time = "2020-11-27T15:13:06.------070894";
        String time2 = LocalDateTime.now().minusDays(1L).toString();
        Assertions.assertThrows(BadRequestException.class, () ->
                ValidationUtil.validateDateTime(time, time2)
        );
    }

    @Test
    void shouldThrow_NullInputValidateId() {
        Assertions.assertThrows(BadRequestException.class, () ->
                ValidationUtil.validateId(NUMBER_TEXT_HYPHEN_PATTERN, "")
        );
    }

    @Test
    void shouldValidate_CompareDateTime() {
        Assertions.assertDoesNotThrow(() ->
                ValidationUtil.validateDateTime(LocalDateTime.now().plusDays(1L).toString(),
                        LocalDateTime.now().plusDays(2L).toString())
        );
    }

    @Test
    void shouldThrow_BeginTimeBeforeCreateTime() {
        String previousDate = LocalDateTime.now().minusDays(1L).toString();
        String futureDate = LocalDateTime.now().plusDays(1L).toString();
        Assertions.assertThrows(BadRequestException.class, () ->
                ValidationUtil.compareDateOrder(previousDate, futureDate)
        );
    }

    @Test
    void shouldThrow_EndTimeBeforeCreateTime() {
        String previousDate = LocalDateTime.now().minusDays(1L).toString();
        String futureDate = LocalDateTime.now().plusDays(1L).toString();
        Assertions.assertThrows(BadRequestException.class, () ->
                ValidationUtil.compareDateOrder(futureDate, previousDate)
        );
    }

    @Test
    void shouldThrow_EndTimeBeforeBeginTime() {
        String futureDate1 = LocalDateTime.now().plusDays(10L).toString();
        String futureDate2 = LocalDateTime.now().plusDays(5L).toString();
        Assertions.assertThrows(BadRequestException.class, () ->
                ValidationUtil.compareDateOrder(futureDate1, futureDate2)
        );
    }

}
