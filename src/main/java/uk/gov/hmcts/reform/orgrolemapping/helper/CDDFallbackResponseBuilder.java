package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypesResponse;

import java.io.File;

public class CDDFallbackResponseBuilder {

    public static final String ACCESS_TYPES_SAMPLE = "accessTypesSample.json";

    private static final String RESOURCES_PATH = "src/main/resources/samples/ccd/";

    private CDDFallbackResponseBuilder() {
        // Hide Utility Class Constructor : Utility classes should not have a public or
        // default constructor (squid:S1118)
    }

    public static AccessTypesResponse buildAccessTypesResponse(String resource) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(
                new File(RESOURCES_PATH + resource),
                AccessTypesResponse.class);
        } catch (Exception e) {
            throw new BadRequestException("Invalid sample json file or missing for buildAccessTypesResponse.");
        }
    }

}
