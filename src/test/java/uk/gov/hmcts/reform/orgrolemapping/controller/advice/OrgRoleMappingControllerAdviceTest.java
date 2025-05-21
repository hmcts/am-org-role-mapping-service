package uk.gov.hmcts.reform.orgrolemapping.controller.advice;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ForbiddenException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.UnprocessableEntityException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrgRoleMappingControllerAdviceTest {

    private final transient uk.gov.hmcts.reform.orgrolemapping.controller.advice.OrgRoleMappingControllerAdvice csda
        = new uk.gov.hmcts.reform.orgrolemapping.controller.advice.OrgRoleMappingControllerAdvice();

    private final transient HttpServletRequest servletRequestMock = mock(HttpServletRequest.class);

    @Test
    void customValidationError() {
        InvalidRequest invalidRequestException = mock(InvalidRequest.class);
        ResponseEntity<Object> responseEntity = csda.customValidationError(invalidRequestException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    void handleMethodArgumentNotValidException() {
        MethodArgumentNotValidException methodArgumentNotValidException = mock(MethodArgumentNotValidException.class);
        ResponseEntity<Object> responseEntity = csda.handleMethodArgumentNotValidException(
                servletRequestMock, methodArgumentNotValidException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    void handleResourceNotFoundException() {
        ResourceNotFoundException resourceNotFoundException = mock(ResourceNotFoundException.class);
        ResponseEntity<Object> responseEntity = csda.handleResourceNotFoundException(
                servletRequestMock, resourceNotFoundException);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    void handleHttpMessageConversionException() {
        HttpMessageConversionException httpMessageConversionException = mock(HttpMessageConversionException.class);
        ResponseEntity<Object> responseEntity = csda.handleHttpMessageConversionException(
                servletRequestMock, httpMessageConversionException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }


    @Test
    void handleBadRequestError() {
        BadRequestException badRequestException = mock(BadRequestException.class);
        ResponseEntity<Object> responseEntity = csda.handleBadRequestError(badRequestException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    void handleUnprocessableRequestError() {
        UnprocessableEntityException unprocessableError = mock(UnprocessableEntityException.class);
        ResponseEntity<Object> responseEntity = csda.handleUnprocessableEntityException(unprocessableError);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, responseEntity.getStatusCode());
    }

    @Test
    void getTimeStamp() {
        String time = csda.getTimeStamp();
        assertEquals(time.substring(0, 16), new SimpleDateFormat("dd-MM-yyyy HH:mm",
                Locale.ENGLISH).format(new Date()));
    }

    @Test
    void handleForbiddenException() {
        ForbiddenException resourceNotFoundException = mock(ForbiddenException.class);
        ResponseEntity<Object> responseEntity = csda.handleForbiddenException(resourceNotFoundException);
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Test
    void handleRootExceptionException() {
        Throwable exception = mock(Throwable.class);
        assertNotNull(OrgRoleMappingControllerAdvice.getRootException(exception));
    }

    @Test
    void handleRootException() {
        Throwable exception = mock(Throwable.class);
        when(exception.getCause()).thenReturn(new BadRequestException("Bad Req"));
        Throwable cause = OrgRoleMappingControllerAdvice.getRootException(exception);
        assertNotNull(cause);
        assertEquals(exception.getCause(), cause);
    }

}
