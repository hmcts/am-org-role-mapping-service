package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.orgrolemapping.config.DBFlagConfigurtion;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Status;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
class RequestMappingServiceTest {

    @Mock
    private RoleAssignmentService roleAssignmentService;


    private StatelessKieSession kieSession;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    PersistenceService persistenceService;

    @Mock
    DBFlagConfigurtion dbFlagConfigurtion;

    @InjectMocks
    RequestMappingService sut;

    @BeforeEach
    public void setUp() {
        KieServices ks = KieServices.Factory.get();
        KieContainer kieContainer = ks.getKieClasspathContainer();
        this.kieSession = kieContainer.newStatelessKieSession("org-role-mapping-validation-session");
        sut = new RequestMappingService("pr", persistenceService, roleAssignmentService, kieSession,
                securityUtils);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createCaseWorkerAssignmentsTest() {

        final String actorId = "123e4567-e89b-42d3-a456-556642445612";

        Mockito.when(roleAssignmentService.createRoleAssignment(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED)
                        .body(AssignmentRequestBuilder.buildAssignmentRequest(false)));
        Mockito.when(persistenceService.getStatusByParam("iac_1_0", "pr"))
                .thenReturn(true);
        ResponseEntity<Object> responseEntity =
                sut.createCaseWorkerAssignments(TestDataBuilder.buildUserAccessProfileMap(false,
                        false));

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode resultNode = objectMapper.convertValue(responseEntity.getBody(),
                JsonNode.class);
        assertEquals(1, resultNode.size());
        assertEquals("staff-organisational-role-mapping",
                resultNode.get(0).get("body").get("roleRequest").get("process")
                        .asText());
        assertEquals("tribunal-caseworker",
                resultNode.get(0).get("body").get("requestedRoles").get(0)
                        .get("roleName")
                        .asText());

        assertEquals(actorId,
                resultNode.get(0).get("body").get("requestedRoles").get(0).get("actorId")
                        .asText());




        Mockito.verify(roleAssignmentService, Mockito.times(1))
                .createRoleAssignment(any());
    }

    @Test
    void createCaseWorkerAssignmentTestFeignException() {

        String content = "{\"roleRequest\":{\"id\":\"484144da-2ce0-4496-aa4d-8910a5582cba\",\"authenticatedUserId\""
                + ":\"0a677766-69f7-4add-994a-79966616ee50\",\"correlationId\":\"8f54ebd0-4226-4cac-a272-faf7bd88a3"
                + "5d\",\"assignerId\":\"0a677766-69f7-4add-994a-79966616ee50\",\"requestType\":\"CREATE\",\"proces"
                + "s\":\"staff-organisational-role-mapping\",\"reference\":\"123e4567-e89b-42d3-a456-556642445678\",\""
                + "replaceExisting\":true,\"status\":\"REJECTED\",\"created\":\"2020-12-17T10:08:51Z\",\"log\""
                + ":\"Request has been rejected due to following assignment Ids :[f1b514de-7a16-4482-951b-26121c447f5"
                + "7, 657a7b75-2a85-4a01-80ce-74b08ac105cc]\"},\"requestedRoles\":[{\"id\":\"f1b514de-7a16-4482-95"
                + "1b-26121c447f57\",\"actorIdType\":\"IDAM\",\"actorId\":\"123e4567-e89b-42d3-a456-55664244567"
                + "8\",\"roleType\":\"ORGANISATION\",\"roleName\":\"senior-tribunal-caseworker\",\"classification\":\"P"
                + "UBLIC\",\"grantType\":\"STANDARD\",\"roleCategory\":\"LEGAL_OPERATIONS\",\"readOnly\":false,"
                + "\"process\":\"s"
                + "taff-organisational-role-mapping\",\"reference\":\"123e4567-e89b-42d3-a456-556642445678\",\"sta"
                + "tus\":\"REJECTED\",\"created\":\"2020-12-17T10:08:51Z\",\"log\":\"Create approved : sta"
                + "ff_organisational_role_mapping_service_create\\nCreate not approved by any rule, hence rejected  "
                + ": reject_unapproved_create_role_assignments\",\"attributes\":{\"primaryLocation\":null,\"jurisdi"
                + "ction\":\"IA\"},\"notes\":null},{\"id\":\"657a7b75-2a85-4a01-80ce-74b08ac105cc\",\"actorIdTyp"
                + "e\":\"IDAM\",\"actorId\":\"123e4567-e89b-42d3-a456-556642445678\",\"roleType\":\"ORGANISATION\",\"r"
                + "oleName\":\"tribunal-caseworker\",\"classification\":\"PUBLIC\",\"grantType\":\"STANDARD\",\"roleCa"
                + "tegory\":\"LEGAL_OPERATIONS\",\"readOnly\":false,\"process\":\"staff-organisational-role-mapping\","
                + "\"referenc"
                + "e\":\"123e4567-e89b-42d3-a456-556642445678\",\"status\":\"REJECTED\",\"created\":\"2020-12-17T10:0"
                + "8:51Z\",\"log\":\"Create approved : staff_organisational_role_mapping_service_create\\nCreate"
                + " not approved by any rule, hence rejected  : reject_unapproved_create_role_assignments\",\"attribut"
                + "es\":{\"primaryLocation\":null,\"jurisdiction\":\"IA\"},\"notes\":null}]}";

        FeignException.FeignClientException feignClientException = mock(FeignException.FeignClientException.class);
        Mockito.when(roleAssignmentService.createRoleAssignment(any()))
                .thenThrow(feignClientException);
        Mockito.when(feignClientException.contentUTF8())
                .thenReturn(content);
        Mockito.when(persistenceService.getStatusByParam("iac_1_0", "pr"))
                .thenReturn(true);
        ResponseEntity<Object> responseEntity =
                sut.createCaseWorkerAssignments(TestDataBuilder.buildUserAccessProfileMap(false,
                        false));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode resultNode = objectMapper.convertValue(responseEntity.getBody(),
                JsonNode.class);
        assertEquals(1, resultNode.size());
        assertEquals("staff-organisational-role-mapping",
                resultNode.get(0).get("body").get("roleAssignmentResponse").get("roleRequest").get("process").asText());
        assertEquals("senior-tribunal-caseworker",
                resultNode.get(0).get("body").get("roleAssignmentResponse").get("requestedRoles").get(0)
                        .get("roleName").asText()
        );

    }

    @Test
    void createCaseWorkerAssignmentJsonProcessingException() {

        String content = "}";

        FeignException.FeignClientException feignClientException = mock(FeignException.FeignClientException.class);
        Mockito.when(roleAssignmentService.createRoleAssignment(any()))
                .thenThrow(feignClientException);
        Mockito.when(feignClientException.contentUTF8())
                .thenReturn(content);
        Mockito.when(persistenceService.getStatusByParam("iac_1_0", "pr"))
                .thenReturn(true);
        ResponseEntity<Object> responseEntity = sut.createCaseWorkerAssignments(
                TestDataBuilder.buildUserAccessProfileMap(false, false));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode resultNode = objectMapper.convertValue(responseEntity.getBody(),
                JsonNode.class);
        assertEquals(1, resultNode.size());
    }

    @Test
    void updateCaseworkerRoleAssignments() throws IOException {

        AtomicInteger atomicInteger = new AtomicInteger(1);
        AtomicInteger spyInteger = Mockito.spy(atomicInteger);

        Mockito.when(roleAssignmentService.createRoleAssignment(any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        ResponseEntity<Object> responseEntity = sut.updateCaseworkerRoleAssignments(
                "1",
                TestDataBuilder.buildRequestedRoleCollection(Status.CREATED),
                spyInteger);

        assertNotNull(responseEntity);
    }

    @Test
    void updateCaseworkerRoleAssignments_Failure() throws IOException {

        AtomicInteger atomicInteger = new AtomicInteger(1);
        AtomicInteger spyInteger = Mockito.spy(atomicInteger);

        Mockito.when(roleAssignmentService.createRoleAssignment(any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.CONFLICT));

        sut.updateCaseworkerRoleAssignments(
                "1",
                TestDataBuilder.buildRequestedRoleCollection(Status.CREATED),
                spyInteger);

        verify(spyInteger, Mockito.times(1)).getAndIncrement();
    }

    @Test
    void createCaseWorkerAssignmentsForProdEnv() {

        final String actorId = "123e4567-e89b-42d3-a456-556642445612";

        Mockito.when(roleAssignmentService.createRoleAssignment(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED)
                        .body(AssignmentRequestBuilder.buildAssignmentRequest(false)));
        Mockito.when(persistenceService.getStatusByParam("iac_1_0", "prod"))
                .thenReturn(true);

        ConcurrentHashMap<String, Boolean> droolFlagStates = new ConcurrentHashMap<>();
        droolFlagStates.put(FeatureFlagEnum.IAC_1_0.getValue(), true);
        try (MockedStatic<DBFlagConfigurtion> theMock = Mockito.mockStatic(DBFlagConfigurtion.class)) {
            theMock.when(() -> dbFlagConfigurtion.getDroolFlagStates()).thenReturn(droolFlagStates);
            ReflectionTestUtils.setField(sut, "environment", "prod");
            ResponseEntity<Object> responseEntity =
                    sut.createCaseWorkerAssignments(TestDataBuilder.buildUserAccessProfileMap(false,
                            false));

            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertNotNull(responseEntity.getBody());

            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode resultNode = objectMapper.convertValue(responseEntity.getBody(),
                    JsonNode.class);
            assertEquals(1, resultNode.size());
            assertEquals("staff-organisational-role-mapping",
                    resultNode.get(0).get("body").get("roleRequest").get("process")
                            .asText());
            assertEquals("tribunal-caseworker",
                    resultNode.get(0).get("body").get("requestedRoles").get(0)
                            .get("roleName")
                            .asText());
            assertEquals(actorId,
                    resultNode.get(0).get("body").get("requestedRoles").get(0).get("actorId")
                            .asText());
            Mockito.verify(roleAssignmentService, Mockito.times(1))
                    .createRoleAssignment(any());
        }
    }
}
