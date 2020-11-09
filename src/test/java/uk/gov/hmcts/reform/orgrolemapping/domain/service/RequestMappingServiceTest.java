package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.StatelessKieSession;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_TCW;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.buildAssignmentRequest;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserAccessProfiles;

@RunWith(MockitoJUnitRunner.class)
class RequestMappingServiceTest {

    @Mock
    StatelessKieSession kieSessionMock = mock(StatelessKieSession.class);
    @Mock
    RoleAssignmentService roleAssignmentService = mock(RoleAssignmentService.class);

    @InjectMocks
    private final RequestMappingService sut = new RequestMappingService(roleAssignmentService, kieSessionMock);

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturn200Response() {
        ResponseEntity<Object> entity = ResponseEntity.status(HttpStatus.OK)
                .body(buildAssignmentRequest(true));
        when(roleAssignmentService.createRoleAssignment(any())).thenReturn(entity);
        Map<String, Set<UserAccessProfile>> userAccessProfiles = buildUserAccessProfiles();
        ResponseEntity<Object> response = sut.createCaseWorkerAssignments(userAccessProfiles);
        assertNotNull(response);
        List<AssignmentRequest> assignmentRequest = (List) response.getBody();
        RoleAssignment roleAssignment = ((List<RoleAssignment>) assignmentRequest.get(0).getRequestedRoles()).get(0);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ROLE_NAME_TCW, roleAssignment.getRoleName());
        assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType());
        assertEquals(RoleCategory.STAFF, roleAssignment.getRoleCategory());

    }
}
