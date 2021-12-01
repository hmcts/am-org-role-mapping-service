package uk.gov.hmcts.reform.orgrolemapping.controller;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialRefreshRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.JudicialRefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

class RefreshControllerTest {

    @Mock
    private RefreshOrchestrator refreshOrchestrator;

    @Mock
    private JudicialRefreshOrchestrator judicialRefreshOrchestrator;

    @InjectMocks
    private final RefreshController sut = new RefreshController(refreshOrchestrator, judicialRefreshOrchestrator);

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

    @Test
    void refreshJudicialRoleAssignments() {
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK).body(Map.of("Message",
                        "Role assignments have been refreshed successfully"));
        Mockito.when(judicialRefreshOrchestrator.judicialRefresh(any())).thenReturn(response);

        assertEquals(response, sut.judicialRefresh(JudicialRefreshRequest.builder().build()));
        Mockito.verify(judicialRefreshOrchestrator, Mockito.times(1)).judicialRefresh(any());
    }

    @Test
    void refreshJudicialRoleAssignmentRecords_emptyRequest() {
        Mockito.when(judicialRefreshOrchestrator.judicialRefresh(any())).thenThrow(BadRequestException.class);

        Assert.assertThrows(BadRequestException.class, () ->
            sut.judicialRefresh(JudicialRefreshRequest.builder().build()));

    }
}
