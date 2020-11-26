package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.drools.core.impl.StatelessKnowledgeSessionImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
class RequestMappingServiceTest {

    @Mock
    private RoleAssignmentService roleAssignmentService;

    @Spy
    private StatelessKieSession kieSession;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    RequestMappingService requestMappingService =
            new RequestMappingService(roleAssignmentService, kieSession, securityUtils);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    //    @Test
    //    void createCaseWorkerAssignmentsTest() {
    //
    //        Mockito.when(roleAssignmentService.createRoleAssignment(any()))
    //                .thenReturn(ResponseEntity.status(HttpStatus.CREATED)
    //                        .body(AssignmentRequestBuilder.buildAssignmentRequest(false)));
    //
    //        ResponseEntity<Object> responseEntity =
    //                requestMappingService.createCaseWorkerAssignments(TestDataBuilder.buildUserAccessProfileMap(true,
    //                        true));
    //
    //        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    //
    //        Mockito.verify(roleAssignmentService, Mockito.times(1))
    //                .createRoleAssignment(any());
    //    }

    //    @Test
    //    void createCaseWorkerAssignmentsTest_() {
    //        ResponseEntity<Object> responseEntity =
    //                requestMappingService.createCaseWorkerAssignments(TestDataBuilder.buildUserAccessProfileMap(false,
    //                        false));
    //
    //        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    //    }
}
