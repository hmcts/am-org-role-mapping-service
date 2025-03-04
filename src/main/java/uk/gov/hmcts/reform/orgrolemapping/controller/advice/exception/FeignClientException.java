package uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception;

public class FeignClientException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FeignClientException(String message) {
        super(message);
    }

    public FeignClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
