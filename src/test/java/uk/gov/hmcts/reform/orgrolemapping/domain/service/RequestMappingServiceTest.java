package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
class RequestMappingServiceTest {

    @Mock
    private RoleAssignmentService roleAssignmentService;


    private StatelessKieSession kieSession;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    RequestMappingService requestMappingService;

    @BeforeEach
    public void setUp() {
        KieServices ks = KieServices.Factory.get();
        KieContainer kieContainer = ks.getKieClasspathContainer();
        this.kieSession = kieContainer.newStatelessKieSession("org-role-mapping-validation-session");
        requestMappingService = new RequestMappingService(roleAssignmentService, kieSession,
                securityUtils);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createCaseWorkerAssignmentsTest() {

        final String actorId = "123e4567-e89b-42d3-a456-556642445612";

        Mockito.when(roleAssignmentService.createRoleAssignment(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED)
                        .body(AssignmentRequestBuilder.buildAssignmentRequest(false)));

        ResponseEntity<Object> responseEntity =
                requestMappingService.createCaseWorkerAssignments(TestDataBuilder.buildUserAccessProfileMap(false,
                        false));

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode resultNode = objectMapper.convertValue(responseEntity.getBody(),
                JsonNode.class);
        assertEquals(2, resultNode.size());
        assertEquals("staff-organisational-role-mapping",
                resultNode.get(0).get("roleRequest").get("process").asText());
        assertEquals("tribunal-caseworker",
                resultNode.get(0).get("requestedRoles").get(0).get("roleName").asText());
        assertEquals(actorId,
                resultNode.get(0).get("requestedRoles").get(0).get("actorId").asText());

        assertEquals("staff-organisational-role-mapping",
                resultNode.get(1).get("roleRequest").get("process").asText());
        assertEquals("tribunal-caseworker",
                resultNode.get(1).get("requestedRoles").get(0).get("roleName").asText());
        assertEquals(actorId,
                resultNode.get(1).get("requestedRoles").get(0).get("actorId").asText());

        Mockito.verify(roleAssignmentService, Mockito.times(2))
                .createRoleAssignment(any());
    }
}
