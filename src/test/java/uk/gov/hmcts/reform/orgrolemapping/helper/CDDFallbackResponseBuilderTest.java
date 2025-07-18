package uk.gov.hmcts.reform.orgrolemapping.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypesResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.orgrolemapping.helper.CDDFallbackResponseBuilder.ACCESS_TYPES_SAMPLE;

class CDDFallbackResponseBuilderTest {

    private static final String INVALID_JSON = "invalid.json";

    @Test
    void buildAccessTypesResponseTest() {
        AccessTypesResponse accessTypesResponse
            = CDDFallbackResponseBuilder.buildAccessTypesResponse(ACCESS_TYPES_SAMPLE);

        assertNotNull(accessTypesResponse);
        accessTypesResponse.getJurisdictions().forEach(jurisdiction -> {
            assertNotNull(jurisdiction.getJurisdictionId());
            assertNotNull(jurisdiction.getJurisdictionName());
            assertNotNull(jurisdiction.getAccessTypes());
        });
        assertEquals(3, accessTypesResponse.getJurisdictions().size());
    }

    @Test
    void buildAccessTypesResponseThrowsExceptionTest() {
        assertThrows(BadRequestException.class, () ->
            CDDFallbackResponseBuilder.buildAccessTypesResponse(INVALID_JSON));
    }

}
