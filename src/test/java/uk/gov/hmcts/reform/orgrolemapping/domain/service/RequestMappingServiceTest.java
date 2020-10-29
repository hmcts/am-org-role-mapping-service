package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.StatelessKieSession;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestMappingServiceTest {

    @Mock
    private RoleAssignmentService roleAssignmentService;

    @Mock
    private StatelessKieSession kieSession;

    @InjectMocks
    RequestMappingService requestMappingService = new RequestMappingService(roleAssignmentService, kieSession);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createCaseWorkerAssignments() {
        Mockito.when(roleAssignmentService.createRoleAssignment(Mockito.any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body("RoleAssignment"));
        ResponseEntity<Object> responseEntity =
                requestMappingService.createCaseWorkerAssignments(TestDataBuilder.buildUserAccessProfileMap());

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}