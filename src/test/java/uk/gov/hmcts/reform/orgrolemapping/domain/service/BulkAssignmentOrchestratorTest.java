package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import feign.FeignException;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_SJ;
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
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @SuppressWarnings("unchecked")
    void createBulkAssignmentsRequestTest() {



        doReturn(TestDataBuilder.buildUserAccessProfileMap(false, false)).when(retrieveDataService)
                .retrieveProfiles(Mockito.any(),Mockito.any());
        List<ResponseEntity<Object>> responseEntities = new ArrayList<>();
        responseEntities.add(ResponseEntity.ok(AssignmentRequestBuilder
                .buildAssignmentRequest(false)));

        Mockito.when(requestMappingService.createAssignments(Mockito.any(), Mockito.any()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(responseEntities));

        ResponseEntity<Object> response = sut.createBulkAssignmentsRequest(TestDataBuilder.buildUserRequest(),
                UserType.CASEWORKER);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<AssignmentRequest> entity = (List<AssignmentRequest>) response.getBody();

        AssignmentRequest assignmentRequest = entity.get(0);
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
        List<ResponseEntity<Object>> responseEntities = new ArrayList<>();
        responseEntities.add(ResponseEntity.ok(AssignmentRequestBuilder
                .buildJudicialAssignmentRequest(false)));

        Mockito.when(requestMappingService.createAssignments(Mockito.any(), Mockito.any()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(responseEntities));

        ResponseEntity<Object> response = sut.createBulkAssignmentsRequest(TestDataBuilder.buildUserRequest(),
                UserType.JUDICIAL);

        assertEquals(HttpStatus.OK, response.getStatusCode());


        List<AssignmentRequest> assignmentRequests = (List<AssignmentRequest>) response.getBody();
        assert assignmentRequests != null;

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        RoleAssignment roleAssignment = ((List<RoleAssignment>) assignmentRequests.get(0).getRequestedRoles()).get(0);
        assertEquals(ROLE_NAME_SJ, roleAssignment.getRoleName());
        assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType());
        assertEquals(RoleCategory.JUDICIAL, roleAssignment.getRoleCategory());

        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserRequest(Mockito.any(UserRequest.class));
        Mockito.verify(retrieveDataService, Mockito.times(1))
                .retrieveProfiles(Mockito.any(UserRequest.class),Mockito.any());
        Mockito.verify(requestMappingService, Mockito.times(1))
                .createAssignments(Mockito.any(),Mockito.any());
    }

    @Test
    void createBulkAssignmentsRequestForJudicial_clientNotAvailable() {

        doThrow(FeignException.NotFound.class).when(retrieveDataService)
                .retrieveProfiles(Mockito.any(), Mockito.any());
        ResponseEntity<Object> responseEntity = sut.createBulkAssignmentsRequest(TestDataBuilder.buildUserRequest(), UserType.JUDICIAL);
        try {
            responseEntity.getStatusCode();
            Assert.fail("Expected an ResourceNotFoundException to be thrown");
        } catch (ResourceNotFoundException e) {
            assertNotNull(e);
        }
    }

}
