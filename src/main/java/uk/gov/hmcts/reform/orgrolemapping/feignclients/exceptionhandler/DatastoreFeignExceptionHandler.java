package uk.gov.hmcts.reform.orgrolemapping.feignclients.exceptionhandler;

import feign.Response;
import feign.codec.ErrorDecoder;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;

public class DatastoreFeignExceptionHandler implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {

        switch (response.status()) {
            case 400:
                return new BadRequestException(response.toString());
            case 404:
                return new ResourceNotFoundException(response.toString());
            default:
                return new Exception("The Data Store application is down " + response.toString());
        }
    }
}
