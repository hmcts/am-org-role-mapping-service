package uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class UnauthorizedServiceException extends RuntimeException {

    private static final long serialVersionUID = 9L;

    public UnauthorizedServiceException(String  message) {
        super(message);
    }
}
