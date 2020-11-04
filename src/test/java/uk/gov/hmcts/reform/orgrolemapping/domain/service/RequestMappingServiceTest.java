package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.StatelessKieSession;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.buildAssignmentRequest;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserAccessProfiles;

@RunWith(MockitoJUnitRunner.class)
public class RequestMappingServiceTest {

    StatelessKieSession kieSessionMock = mock(StatelessKieSession.class);
    RoleAssignmentService roleAssignmentService = mock(RoleAssignmentService.class);

    @InjectMocks
    private final RequestMappingService sut = new RequestMappingService();

    @Test
    void shouldReturn200Response(){
        Mockito.verify(kieSessionMock, times(1)).execute((Iterable) any());
        ResponseEntity<Object> entity = ResponseEntity.status(HttpStatus.OK)
                .body(buildAssignmentRequest(true));
        when(roleAssignmentService.createRoleAssignment(buildAssignmentRequest(true))).thenReturn(entity);
        Map<String, Set<UserAccessProfile>> userAccessProfiles = buildUserAccessProfiles();
        ResponseEntity<Object> response = sut.createCaseWorkerAssignments(userAccessProfiles);
        assertNotNull(response);

    }
}
