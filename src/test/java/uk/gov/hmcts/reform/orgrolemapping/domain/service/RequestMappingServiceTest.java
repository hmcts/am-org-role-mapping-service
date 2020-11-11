package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.runner.RunWith;
import org.kie.api.runtime.StatelessKieSession;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
class RequestMappingServiceTest {

    @Mock
    StatelessKieSession kieSessionMock = mock(StatelessKieSession.class);
    @Mock
    RoleAssignmentService roleAssignmentService = mock(RoleAssignmentService.class);
    @Mock
    SecurityUtils securityUtils = mock(SecurityUtils.class);

    @InjectMocks
    private final RequestMappingService sut = new RequestMappingService(roleAssignmentService,
            kieSessionMock,securityUtils);

   /* @Test
    @SuppressWarnings("unchecked")
    void shouldReturn200Response() {
        ResponseEntity<Object> entity = ResponseEntity.status(HttpStatus.OK)
                .body(buildAssignmentRequest(true));
        when(roleAssignmentService.createRoleAssignment(any())).thenReturn(entity);
        when(kieSessionMock.execute(any())).thenReturn();

        Map<String, Set<UserAccessProfile>> userAccessProfiles = buildUserAccessProfiles();
        ResponseEntity<Object> response = sut.createCaseWorkerAssignments(userAccessProfiles);
        assertNotNull(response);
        List<AssignmentRequest> assignmentRequest = (List) response.getBody();
        RoleAssignment roleAssignment = ((List<RoleAssignment>) assignmentRequest.get(0).getRequestedRoles()).get(0);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ROLE_NAME_TCW, roleAssignment.getRoleName());
        assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType());
        assertEquals(RoleCategory.STAFF, roleAssignment.getRoleCategory());

    }*/
}
