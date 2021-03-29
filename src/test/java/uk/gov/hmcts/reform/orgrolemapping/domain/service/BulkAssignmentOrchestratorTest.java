package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_TCW;

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
    @SuppressWarnings("unchecked")
    void createBulkAssignmentsRequestTest() {



        doReturn(TestDataBuilder.buildUserAccessProfileMap(false, false)).when(retrieveDataService)
                .retrieveProfiles(Mockito.any(),Mockito.any());

        Mockito.when(requestMappingService.createAssignments(Mockito.any(), Mockito.any()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(AssignmentRequestBuilder
                        .buildAssignmentRequest(false)));

        ResponseEntity<Object> response = sut.createBulkAssignmentsRequest(TestDataBuilder.buildUserRequest(),
                UserType.CASEWORKER);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        AssignmentRequest assignmentRequest = (AssignmentRequest) response.getBody();
        assert assignmentRequest != null;
        RoleAssignment roleAssignment = ((List<RoleAssignment>) assignmentRequest.getRequestedRoles()).get(0);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ROLE_NAME_TCW, roleAssignment.getRoleName());
        assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType());
        assertEquals(RoleCategory.LEGAL_OPERATIONS, roleAssignment.getRoleCategory());

        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserRequest(Mockito.any(UserRequest.class));
        Mockito.verify(retrieveDataService, Mockito.times(1))
                .retrieveProfiles(Mockito.any(UserRequest.class),Mockito.any());
        Mockito.verify(requestMappingService, Mockito.times(1))
                .createAssignments(Mockito.any(),Mockito.any());
    }


    @Test
    @SuppressWarnings("unchecked")
    void createBulkAssignmentsRequestForJudicial() {

        doReturn(TestDataBuilder.buildJudicialAccessProfileMap()).when(retrieveDataService)
                .retrieveProfiles(Mockito.any(),Mockito.any());

        Mockito.when(requestMappingService.createAssignments(Mockito.any(), Mockito.any()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(AssignmentRequestBuilder
                        .buildJudicialAssignmentRequest(false)));

        ResponseEntity<Object> response = sut.createBulkAssignmentsRequest(TestDataBuilder.buildUserRequest(),
                UserType.JUDICIAL);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        AssignmentRequest assignmentRequest = (AssignmentRequest) response.getBody();
        assert assignmentRequest != null;
        RoleAssignment roleAssignment = ((List<RoleAssignment>) assignmentRequest.getRequestedRoles()).get(0);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("salaried-judge", roleAssignment.getRoleName());
        assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType());
        assertEquals(RoleCategory.JUDICIAL, roleAssignment.getRoleCategory());

        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserRequest(Mockito.any(UserRequest.class));
        Mockito.verify(retrieveDataService, Mockito.times(1))
                .retrieveProfiles(Mockito.any(UserRequest.class),Mockito.any());
        Mockito.verify(requestMappingService, Mockito.times(1))
                .createAssignments(Mockito.any(),Mockito.any());
    }

}
