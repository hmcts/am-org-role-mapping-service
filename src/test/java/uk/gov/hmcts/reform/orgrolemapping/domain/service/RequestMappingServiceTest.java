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
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

//TODO improve this test class
@RunWith(MockitoJUnitRunner.class)
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
    void createCaseWorkerAssignmentsTest() {
        Mockito.when(roleAssignmentService.createRoleAssignment(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED)
                        .body(AssignmentRequestBuilder.buildAssignmentRequest(false)));
        ResponseEntity<Object> responseEntity =
                requestMappingService.createCaseWorkerAssignments(TestDataBuilder.buildUserAccessProfileMap(true,
                        true));

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        Mockito.verify(roleAssignmentService, Mockito.times(1))
                .createRoleAssignment(any());
    }

    @Test
    void createCaseWorkerAssignmentsTest_() {
        ResponseEntity<Object> responseEntity =
                requestMappingService.createCaseWorkerAssignments(TestDataBuilder.buildUserAccessProfileMap(false,
                        false));

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
