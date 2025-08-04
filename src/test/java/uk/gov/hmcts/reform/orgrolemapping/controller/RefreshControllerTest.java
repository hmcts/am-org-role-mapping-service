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
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalRefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

class RefreshControllerTest {

    @Mock
    private RefreshOrchestrator refreshOrchestrator;

    @Mock
    private JudicialRefreshOrchestrator judicialRefreshOrchestrator;

    @Mock
    private ProfessionalRefreshOrchestrator professionalRefreshOrchestrator;

    @InjectMocks
    private final RefreshController sut =
        new RefreshController(refreshOrchestrator, judicialRefreshOrchestrator, professionalRefreshOrchestrator);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void refreshRoleAssignmentRecords() {

        // GIVEN
        long jobId = 1L;
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        // WHEN
        ResponseEntity<Object> response = sut.refresh(jobId, userRequest);

        // THEN
        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        Mockito.verify(refreshOrchestrator, Mockito.times(1)).validate(jobId, userRequest);
        Mockito.verify(refreshOrchestrator, Mockito.times(1)).refreshAsync(jobId, userRequest);
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

        assertEquals(response, sut.judicialRefresh(UUID.randomUUID().toString(),
                JudicialRefreshRequest.builder().build()));
        Mockito.verify(judicialRefreshOrchestrator, Mockito.times(1)).judicialRefresh(any());
    }

    @Test
    void refreshJudicialRoleAssignmentRecords_emptyRequest() {
        JudicialRefreshRequest request = JudicialRefreshRequest.builder().build();
        Assert.assertThrows(BadRequestException.class, () ->
            sut.judicialRefresh("1", request));

    }


    @Test
    void refreshJudicialRoleAssignmentRecords_emptyCorrelationId() {
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK).body(Map.of("Message",
                "Role assignments have been refreshed successfully"));
        Mockito.when(judicialRefreshOrchestrator.judicialRefresh(any())).thenReturn(response);

        assertEquals(response, sut.judicialRefresh("",
                JudicialRefreshRequest.builder().build()));
    }

    @Test
    void refreshProfessionalRoleAssignments() {
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK).body(Map.of("Message",
            "Role assignments have been refreshed successfully"));
        Mockito.when(professionalRefreshOrchestrator.refreshProfessionalUser(any())).thenReturn(response);

        assertEquals(response, sut.professionalRefresh(UUID.randomUUID().toString()));
        Mockito.verify(professionalRefreshOrchestrator, Mockito.times(1))
            .refreshProfessionalUser(any());
    }
}
