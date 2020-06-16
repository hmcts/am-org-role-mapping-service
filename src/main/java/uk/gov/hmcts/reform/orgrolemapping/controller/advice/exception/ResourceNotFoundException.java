package uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 4L;

    public ResourceNotFoundException(String message) {
        super(String.format(Constants.RESOURCE_NOT_FOUND + " %s", message));
    }
}
