package uk.gov.hmcts.reform.orgrolemapping.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springdoc.core.Constants.SWAGGER_UI_URL;

public class SwaggerRedirectControllerTest {

    private final SwaggerRedirectController sut = new SwaggerRedirectController();

    @Test
    void swaggerRedirect() {
        var response = sut.swaggerRedirect();

        assertNotNull(response);
        assertTrue(response.isRedirectView());
        assertEquals(SWAGGER_UI_URL, response.getUrl());
    }
}
