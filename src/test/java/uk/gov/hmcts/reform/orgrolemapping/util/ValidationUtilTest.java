package uk.gov.hmcts.reform.orgrolemapping.util;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.NUMBER_TEXT_HYPHEN_PATTERN;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ValidationUtilTest {


    @Test
    void shouldThrowBadRequestException_ValidateLists() {
        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.validateLists(new ArrayList())
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
    void shouldThrow_ValidateDateTimeLength() {
        Assertions.assertThrows(BadRequestException.class, () ->
                ValidationUtil.validateDateTime("2020-11-27T15",
                        LocalDateTime.now().plusDays(2L).toString())
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
        Assertions.assertThrows(BadRequestException.class, () ->
                ValidationUtil.compareDateOrder(LocalDateTime.now().minusDays(1L).toString(),
                        LocalDateTime.now().plusDays(1L).toString())
        );
    }

    @Test
    void shouldThrow_EndTimeBeforeCreateTime() {
        Assertions.assertThrows(BadRequestException.class, () ->
                ValidationUtil.compareDateOrder(LocalDateTime.now().plusDays(1L).toString(),
                        LocalDateTime.now().minusDays(1L).toString())
        );
    }

    @Test
    void shouldThrow_EndTimeBeforeBeginTime() {
        Assertions.assertThrows(BadRequestException.class, () ->
                ValidationUtil.compareDateOrder(LocalDateTime.now().plusDays(10L).toString(),
                        LocalDateTime.now().plusDays(5L).toString())
        );
    }

}
