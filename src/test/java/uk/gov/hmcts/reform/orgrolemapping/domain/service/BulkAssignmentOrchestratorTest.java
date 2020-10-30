package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

class BulkAssignmentOrchestratorTest {

    @Mock
    private ParseRequestService parseRequestService;
    @Mock
    private RetrieveDataService retrieveDataService;
    @Mock
    private RequestMappingService requestMappingService;

    @InjectMocks
    BulkAssignmentOrchestrator sut = new BulkAssignmentOrchestrator();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createBulkAssignmentsRequestTest() {

        Mockito.when(retrieveDataService.retrieveCaseWorkerProfiles(Mockito.any(UserRequest.class)))
                .thenReturn(TestDataBuilder.buildUserAccessProfileMap(false, false));

        Mockito.when(requestMappingService.createCaseWorkerAssignments(Mockito.any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(AssignmentRequestBuilder
                        .buildAssignmentRequest(false)));

        ResponseEntity<Object> response = sut.createBulkAssignmentsRequest(TestDataBuilder.buildUserRequest());

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserRequest(Mockito.any(UserRequest.class));
        Mockito.verify(retrieveDataService, Mockito.times(1))
                .retrieveCaseWorkerProfiles(Mockito.any(UserRequest.class));
        Mockito.verify(requestMappingService, Mockito.times(1))
                .createCaseWorkerAssignments(Mockito.any());
    }
}