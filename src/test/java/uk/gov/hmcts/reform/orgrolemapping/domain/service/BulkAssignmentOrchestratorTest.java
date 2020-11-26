package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_TCW;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
class BulkAssignmentOrchestratorTest {

    private final ParseRequestService parseRequestService = mock(ParseRequestService.class);

    private final RetrieveDataService retrieveDataService = mock(RetrieveDataService.class);

    private final RequestMappingService requestMappingService = mock(RequestMappingService.class);

    @InjectMocks
    private final BulkAssignmentOrchestrator sut = new BulkAssignmentOrchestrator(parseRequestService,
            retrieveDataService,
            requestMappingService);
  
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createBulkAssignmentsRequestTest() {

        Mockito.when(retrieveDataService.retrieveCaseWorkerProfiles(Mockito.any()))
                .thenReturn(TestDataBuilder.buildUserAccessProfileMap(false, false));

        Mockito.when(requestMappingService.createCaseWorkerAssignments(Mockito.any()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(AssignmentRequestBuilder
                        .buildAssignmentRequest(false)));

        ResponseEntity<Object> response = sut.createBulkAssignmentsRequest(TestDataBuilder.buildUserRequest());

        assertEquals(HttpStatus.OK, response.getStatusCode());

        AssignmentRequest assignmentRequest = (AssignmentRequest) response.getBody();
        assert assignmentRequest != null;
        RoleAssignment roleAssignment = ((List<RoleAssignment>) assignmentRequest.getRequestedRoles()).get(0);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ROLE_NAME_TCW, roleAssignment.getRoleName());
        assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType());
        assertEquals(RoleCategory.STAFF, roleAssignment.getRoleCategory());

        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserRequest(Mockito.any(UserRequest.class));
        Mockito.verify(retrieveDataService, Mockito.times(1))
                .retrieveCaseWorkerProfiles(Mockito.any(UserRequest.class));
        Mockito.verify(requestMappingService, Mockito.times(1))
                .createCaseWorkerAssignments(Mockito.any());
    }

}
