package uk.gov.hmcts.reform.orgrolemapping.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

class RefreshControllerTest {

    @Mock
    private RefreshOrchestrator refreshOrchestrator;

    @InjectMocks
    private final RefreshController sut = new RefreshController(refreshOrchestrator);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void refreshRoleAssignmentRecords() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        ResponseEntity<Object> response =
                ResponseEntity.status(HttpStatus.CREATED).body(userRequest);

        Mockito.when(refreshOrchestrator.refresh(any(),any()))
                .thenReturn(response);

        assertEquals(response, sut.refresh(1L, UserRequest.builder().build()));

        Mockito.verify(refreshOrchestrator, Mockito.times(1))
                .validate(any(), any());
    }

    @Test
    void refreshRoleAssignmentRecords_emptyJobId() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        String nfe = "java.lang.NumberFormatException: For input string: \"\"";

        try {
            sut.refresh(Long.valueOf(""), userRequest);
        } catch (NumberFormatException e) {
            assertEquals(nfe, e.toString());
        }
    }

    @Test
    void refreshRoleAssignmentRecords_invalidJobId() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        String nfe = "java.lang.NumberFormatException: For input string: \"abc\"";

        try {
            sut.refresh(Long.valueOf("abc"), userRequest);
        } catch (NumberFormatException e) {
            assertEquals(nfe, e.toString());
        }
    }
}
