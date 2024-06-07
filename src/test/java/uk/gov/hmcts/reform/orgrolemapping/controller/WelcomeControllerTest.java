package uk.gov.hmcts.reform.orgrolemapping.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.ErrorConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.springdoc.core.Constants.SWAGGER_UI_URL;

class WelcomeControllerTest {
    static final String SWAGGER_UI_URL = "/swagger-ui/index.html?url=";
    private final WelcomeController sut = new WelcomeController();

    @Test
    void swaggerRedirect() {
        var response = sut.swaggerRedirect();

        assertNotNull(response);
        assertTrue(response.isRedirectView());
        assertEquals(SWAGGER_UI_URL, response.getUrl());
    }

    @Test
    void welcome() {
        assertEquals("Welcome to Organisation Role Mapping Service", sut.welcome());
    }

    @Test
    void functionalSleepTest() {
        ResponseEntity<Object> response =
                ResponseEntity.status(HttpStatus.OK).body("Sleep time for Functional tests is over");

        assertEquals(response, sut.waitFor(null));
    }

    @Test
    void errorConstantTest() {
        assertEquals(202, ErrorConstants.ACCEPTED.getErrorCode());
        assertEquals("Accepted", ErrorConstants.ACCEPTED.getErrorMessage());
    }

}
