package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_TCW;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.buildAssignmentRequest;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserAccessProfiles;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserRequest;

@RunWith(MockitoJUnitRunner.class)
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


    @Test
    void shouldReturn200Response() {


        ResponseEntity<Object> entity = ResponseEntity.status(HttpStatus.OK)
                .body(buildAssignmentRequest(true));

        Map<String, Set<UserAccessProfile>> userAccessProfiles = buildUserAccessProfiles();

        doNothing().when(parseRequestService).validateUserRequest(any());
        when(retrieveDataService.retrieveCaseWorkerProfiles(any())).thenReturn(userAccessProfiles);
        when(requestMappingService.createCaseWorkerAssignments(userAccessProfiles)).thenReturn(entity);

        ResponseEntity<Object> actualResponse = sut.createBulkAssignmentsRequest(buildUserRequest());
        AssignmentRequest assignmentRequest = (AssignmentRequest) actualResponse.getBody();
        RoleAssignment roleAssignment = ((List<RoleAssignment>) assignmentRequest.getRequestedRoles()).get(0);
        assertNotNull(actualResponse);
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(ROLE_NAME_TCW, roleAssignment.getRoleName());
        assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType());
        assertEquals(RoleCategory.STAFF, roleAssignment.getRoleCategory());

    }

}
