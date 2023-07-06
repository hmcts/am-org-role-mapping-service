package uk.gov.hmcts.reform.orgrolemapping.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.ErrorConstants;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.BulkAssignmentOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.springdoc.core.Constants.SWAGGER_UI_URL;

class WelcomeControllerTest {

    @Mock
    private BulkAssignmentOrchestrator bulkAssignmentOrchestrator;


    @InjectMocks
    private final WelcomeController sut = new WelcomeController(bulkAssignmentOrchestrator);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

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
    void createOrgMappingTest() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        ResponseEntity<Object> response =
                ResponseEntity.status(HttpStatus.CREATED).body(userRequest);

        Mockito.when(bulkAssignmentOrchestrator.createBulkAssignmentsRequest(Mockito.any(UserRequest.class),
                eq(UserType.CASEWORKER)))
                .thenReturn(response);

        assertEquals(response, sut.createOrgMapping(userRequest, UserType.CASEWORKER));
    }

    @Test
    void createOrgMappingTest_Judicial() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        ResponseEntity<Object> response =
                ResponseEntity.status(HttpStatus.CREATED).body(userRequest);

        Mockito.when(bulkAssignmentOrchestrator.createBulkAssignmentsRequest(Mockito.any(UserRequest.class),
                        eq(UserType.JUDICIAL)))
                .thenReturn(response);

        assertEquals(response, sut.createOrgMapping(userRequest, UserType.JUDICIAL));
    }

    @Test
    void createOrgMappingTest_Unprocessable() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        ResponseEntity<Object> response =
                ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(null);

        Mockito.when(bulkAssignmentOrchestrator.createBulkAssignmentsRequest(Mockito.any(UserRequest.class),
                        eq(UserType.JUDICIAL)))
                .thenReturn(response);

        assertEquals(response, sut.createOrgMapping(userRequest, UserType.JUDICIAL));
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
