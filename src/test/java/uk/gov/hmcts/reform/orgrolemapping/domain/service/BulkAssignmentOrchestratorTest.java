package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_SJ;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_TCW;

@ExtendWith(MockitoExtension.class)
class BulkAssignmentOrchestratorTest {

    private final ParseRequestService parseRequestService = mock(ParseRequestService.class);

    private final RetrieveDataService retrieveDataService = mock(RetrieveDataService.class);
    private final JudicialBookingService judicialBookingService = mock(JudicialBookingService.class);

    @SuppressWarnings("unchecked")
    private final RequestMappingService<UserAccessProfile> requestMappingService
            = (RequestMappingService<UserAccessProfile>)mock(RequestMappingService.class);

    @InjectMocks
    private final BulkAssignmentOrchestrator sut = new BulkAssignmentOrchestrator(parseRequestService,
            retrieveDataService,
            requestMappingService,
            judicialBookingService,
            true);
  
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createBulkAssignmentsRequestForCaseworker() {

        // GIVEN
        doReturn(TestDataBuilder.buildUserAccessProfileMap(false, false)).when(retrieveDataService)
                .retrieveProfiles(any(), any());
        List<ResponseEntity<Object>> responseEntities = List.of(ResponseEntity.ok(AssignmentRequestBuilder
                .buildAssignmentRequest(false)));

        Mockito.when(requestMappingService.createCaseworkerAssignments(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(responseEntities));

        UserRequest request = TestDataBuilder.buildUserRequest();

        // WHEN
        ResponseEntity<Object> response = sut.createBulkAssignmentsRequest(request, UserType.CASEWORKER);

        // THEN
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        AssignmentRequest assignmentRequest = getFirstAssignmentRequestFromResponse(response);
        RoleAssignment roleAssignment = ((List<RoleAssignment>) assignmentRequest.getRequestedRoles()).get(0);

        assertEquals(ROLE_NAME_TCW, roleAssignment.getRoleName());
        assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType());
        assertEquals(RoleCategory.LEGAL_OPERATIONS, roleAssignment.getRoleCategory());

        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserRequest(request);
        Mockito.verify(retrieveDataService, Mockito.times(1))
                .retrieveProfiles(request, UserType.CASEWORKER);
        Mockito.verify(requestMappingService, Mockito.times(1))
                .createCaseworkerAssignments(any());
    }

    @Test
    void createBulkAssignmentsRequestForJudicial_includeJudicialBookingsDisabled() {

        // GIVEN
        doReturn(TestDataBuilder.buildJudicialAccessProfileMap()).when(retrieveDataService)
                .retrieveProfiles(any(), any());
        List<ResponseEntity<Object>> responseEntities = List.of(ResponseEntity.ok(AssignmentRequestBuilder
                .buildJudicialAssignmentRequest(false)));

        Mockito.when(requestMappingService.createJudicialAssignments(any(), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(responseEntities));

        UserRequest request = TestDataBuilder.buildUserRequest();

        // WHEN
        BulkAssignmentOrchestrator sutBookingsDisabled = new BulkAssignmentOrchestrator(parseRequestService,
                retrieveDataService,
                requestMappingService,
                judicialBookingService,
                false); // NB: override SUT with disabled bookings flag
        ResponseEntity<Object> response = sutBookingsDisabled.createBulkAssignmentsRequest(request, UserType.JUDICIAL);

        // THEN
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        AssignmentRequest assignmentRequest = getFirstAssignmentRequestFromResponse(response);
        RoleAssignment roleAssignment = ((List<RoleAssignment>) assignmentRequest.getRequestedRoles()).get(0);

        assertEquals(ROLE_NAME_SJ, roleAssignment.getRoleName());
        assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType());
        assertEquals(RoleCategory.JUDICIAL, roleAssignment.getRoleCategory());

        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserRequest(request);
        Mockito.verify(retrieveDataService, Mockito.times(1))
                .retrieveProfiles(request, UserType.JUDICIAL);
        Mockito.verify(requestMappingService, Mockito.times(1))
                .createJudicialAssignments(any(), any());
        Mockito.verify(judicialBookingService, Mockito.never()) // NB: never called as bookings disabled
                .fetchJudicialBookings(any());
    }

    @Test
    void createBulkAssignmentsRequestForJudicial_includeJudicialBookingsEnabled() {

        // GIVEN
        doReturn(TestDataBuilder.buildJudicialAccessProfileMap()).when(retrieveDataService)
                .retrieveProfiles(any(), any());
        List<ResponseEntity<Object>> responseEntities = List.of(ResponseEntity.ok(AssignmentRequestBuilder
                .buildJudicialAssignmentRequest(false)));

        Mockito.when(judicialBookingService.fetchJudicialBookings(any()))
                        .thenReturn(Collections.emptyList());

        Mockito.when(requestMappingService.createJudicialAssignments(any(), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(responseEntities));

        UserRequest request = TestDataBuilder.buildUserRequest();

        // WHEN
        ResponseEntity<Object> response = sut.createBulkAssignmentsRequest(request, UserType.JUDICIAL);

        // THEN
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        AssignmentRequest assignmentRequest = getFirstAssignmentRequestFromResponse(response);
        RoleAssignment roleAssignment = ((List<RoleAssignment>) assignmentRequest.getRequestedRoles()).get(0);

        assertEquals(ROLE_NAME_SJ, roleAssignment.getRoleName());
        assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType());
        assertEquals(RoleCategory.JUDICIAL, roleAssignment.getRoleCategory());

        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserRequest(request);
        Mockito.verify(retrieveDataService, Mockito.times(1))
                .retrieveProfiles(request, UserType.JUDICIAL);
        Mockito.verify(requestMappingService, Mockito.times(1))
                .createJudicialAssignments(any(), any());
        Mockito.verify(judicialBookingService, Mockito.times(1))
                .fetchJudicialBookings(request);
    }

    @Test
    void createBulkAssignmentsRequestForJudicial_clientNotAvailable() {
        // GIVEN
        doThrow(FeignException.NotFound.class).when(retrieveDataService)
                .retrieveProfiles(any(), any());

        UserRequest request = TestDataBuilder.buildUserRequest();

        // WHEN / THEN
        assertThrows(ResourceNotFoundException.class, () ->
                sut.createBulkAssignmentsRequest(request, UserType.JUDICIAL));
    }

    @SuppressWarnings("unchecked")
    private AssignmentRequest getFirstAssignmentRequestFromResponse(ResponseEntity<Object> response) {
        List<AssignmentRequest> entity = (List<AssignmentRequest>) response.getBody();

        assertNotNull(entity);
        AssignmentRequest assignmentRequest = entity.get(0);

        assertNotNull(assignmentRequest);
        return assignmentRequest;
    }

}
