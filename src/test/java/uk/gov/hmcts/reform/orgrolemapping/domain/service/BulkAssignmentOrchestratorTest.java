package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
//import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
//import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
//import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
//import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_TCW;
//import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.buildAssignmentRequest;
//import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserAccessProfiles;
//import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserRequest;

class BulkAssignmentOrchestratorTest {

    @Mock
    private final ParseRequestService parseRequestService = mock(ParseRequestService.class);

    @Mock
    private final RetrieveDataService retrieveDataService = mock(RetrieveDataService.class);

    @Mock
    private final RequestMappingService requestMappingService = mock(RequestMappingService.class);

    @InjectMocks
    private BulkAssignmentOrchestrator sut = new BulkAssignmentOrchestrator(parseRequestService,
            retrieveDataService,
            requestMappingService);
  
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    //    @Test
    //    void createBulkAssignmentsRequestTest() {
    //
    //        Mockito.when(retrieveDataService.retrieveCaseWorkerProfiles(Mockito.any(UserRequest.class)))
    //                .thenReturn(TestDataBuilder.buildUserAccessProfileMap(false, false));
    //
    //        Mockito.when(requestMappingService.createCaseWorkerAssignments(Mockito.any()))
    //                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(AssignmentRequestBuilder
    //                        .buildAssignmentRequest(false)));
    //
    //        ResponseEntity<Object> response = sut.createBulkAssignmentsRequest(TestDataBuilder.buildUserRequest());
    //
    //        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
    //        Mockito.verify(parseRequestService, Mockito.times(1))
    //                .validateUserRequest(Mockito.any(UserRequest.class));
    //        Mockito.verify(retrieveDataService, Mockito.times(1))
    //                .retrieveCaseWorkerProfiles(Mockito.any(UserRequest.class));
    //        Mockito.verify(requestMappingService, Mockito.times(1))
    //                .createCaseWorkerAssignments(Mockito.any());
    //    }


    //    @Test
    //    void shouldReturn200Response() {
    //
    //
    //        ResponseEntity<Object> entity = ResponseEntity.status(HttpStatus.OK)
    //                .body(buildAssignmentRequest(true));
    //
    //        Map<String, Set<UserAccessProfile>> userAccessProfiles = buildUserAccessProfiles();
    //
    //        doNothing().when(parseRequestService).validateUserRequest(any());
    //        when(retrieveDataService.retrieveCaseWorkerProfiles(any())).thenReturn(userAccessProfiles);
    //        when(requestMappingService.createCaseWorkerAssignments(userAccessProfiles)).thenReturn(entity);
    //
    //        ResponseEntity<Object> actualResponse = sut.createBulkAssignmentsRequest(buildUserRequest());
    //        AssignmentRequest assignmentRequest = (AssignmentRequest) actualResponse.getBody();
    //        RoleAssignment roleAssignment = ((List<RoleAssignment>) assignmentRequest.getRequestedRoles()).get(0);
    //        assertNotNull(actualResponse);
    //        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    //        assertEquals(ROLE_NAME_TCW, roleAssignment.getRoleName());
    //        assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType());
    //        assertEquals(RoleCategory.STAFF, roleAssignment.getRoleCategory());
    //
    //    }

}
