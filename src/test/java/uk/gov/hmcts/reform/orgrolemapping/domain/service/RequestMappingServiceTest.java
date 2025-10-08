
package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.FeignException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.config.EnvironmentConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Status;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class RequestMappingServiceTest {

    private static final String TEST_ENVIRONMENT = "pr";

    @Mock
    private RoleAssignmentService roleAssignmentService;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    PersistenceService persistenceService;

    @Mock
    EnvironmentConfiguration environmentConfiguration;

    RequestMappingService<CaseWorkerAccessProfile> sutCaseworker;
    RequestMappingService<JudicialAccessProfile> sutJudicial;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        KieServices ks = KieServices.Factory.get();
        KieContainer kieContainer = ks.getKieClasspathContainer();
        StatelessKieSession kieSession = kieContainer.newStatelessKieSession("org-role-mapping-validation-session");

        sutCaseworker = new RequestMappingService<>(
            persistenceService, environmentConfiguration, roleAssignmentService, kieSession, securityUtils
        );
        sutJudicial = new RequestMappingService<>(
            persistenceService, environmentConfiguration, roleAssignmentService, kieSession, securityUtils
        );

        setUpEnvironmentConfig(TEST_ENVIRONMENT);
    }

    private void setUpEnvironmentConfig(String testEnvironment) {

        Mockito.reset(environmentConfiguration);
        when(environmentConfiguration.getEnvironment()).thenReturn(testEnvironment);
    }

    @Test
    void createCaseWorkerAssignmentsTest_whenNonProdEnvironmentLoadFlagsFromDB() {

        final String actorId = "123e4567-e89b-42d3-a456-556642445612";

        Mockito.when(roleAssignmentService.createRoleAssignment(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED)
                        .body(AssignmentRequestBuilder.buildAssignmentRequest(false)));
        Mockito.when(persistenceService.getStatusByParam("iac_1_1", TEST_ENVIRONMENT))
                .thenReturn(true);

        ResponseEntity<Object> responseEntity =
                sutCaseworker.createCaseworkerAssignments(TestDataBuilder.buildUserAccessProfileMap(false, false));

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode resultNode = objectMapper.convertValue(responseEntity.getBody(),
                JsonNode.class);
        assertEquals(2, resultNode.size());
        assertEquals("staff-organisational-role-mapping",
                resultNode.get(0).get("body").get("roleRequest").get("process")
                        .asText());
        assertEquals("tribunal-caseworker",
                resultNode.get(0).get("body").get("requestedRoles").get(0).get("roleName").asText());
        assertEquals(actorId,
                resultNode.get(0).get("body").get("requestedRoles").get(0).get("actorId").asText());



        Mockito.verify(roleAssignmentService, Mockito.times(2))
                .createRoleAssignment(any());

        // verify when in none PROD environment: all flags are loaded from DB/persistenceService
        Mockito.verify(persistenceService, Mockito.times(FeatureFlagEnum.values().length))
            .getStatusByParam(any(), eq(TEST_ENVIRONMENT));
    }

    @Test
    void createCaseWorkerAssignmentsTest_whenProdEnvironmentLoadFlagsFromDB() {

        // pretend to be in PROD environment
        setUpEnvironmentConfig("prod");

        final String actorId = "123e4567-e89b-42d3-a456-556642445612";

        Mockito.when(roleAssignmentService.createRoleAssignment(any()))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED)
                .body(AssignmentRequestBuilder.buildAssignmentRequest(false)));
        Mockito.when(persistenceService.getStatusByParam("iac_1_1", TEST_ENVIRONMENT))
            .thenReturn(true);

        ResponseEntity<Object> responseEntity =
            sutCaseworker.createCaseworkerAssignments(TestDataBuilder.buildUserAccessProfileMap(false, false));

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode resultNode = objectMapper.convertValue(responseEntity.getBody(),
            JsonNode.class);
        assertEquals(2, resultNode.size());
        assertEquals("staff-organisational-role-mapping",
            resultNode.get(0).get("body").get("roleRequest").get("process")
                .asText());
        assertEquals("tribunal-caseworker",
            resultNode.get(0).get("body").get("requestedRoles").get(0).get("roleName").asText());
        assertEquals(actorId,
            resultNode.get(0).get("body").get("requestedRoles").get(0).get("actorId").asText());



        Mockito.verify(roleAssignmentService, Mockito.times(2))
            .createRoleAssignment(any());

        // verify when in PROD environment: the flag cache is used: i.e. not data from DB/persistenceService
        Mockito.verify(persistenceService, Mockito.never()).getStatusByParam(any(), any());
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
        Mockito.when(persistenceService.getStatusByParam("iac_1_1", TEST_ENVIRONMENT))
                .thenReturn(true);

        ResponseEntity<Object> responseEntity =
                sutCaseworker.createCaseworkerAssignments(TestDataBuilder.buildUserAccessProfileMap(false, false));

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.registerModule(new JavaTimeModule());

        JsonNode resultNode = objectMapper.convertValue(responseEntity.getBody(),
                JsonNode.class);
        assertEquals(2, resultNode.size());
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
        Mockito.when(persistenceService.getStatusByParam("iac_1_1", TEST_ENVIRONMENT))
                .thenReturn(true);

        ResponseEntity<Object> responseEntity = sutCaseworker.createCaseworkerAssignments(
                TestDataBuilder.buildUserAccessProfileMap(false, false));

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode resultNode = objectMapper.convertValue(responseEntity.getBody(),
                JsonNode.class);
        assertEquals(2, resultNode.size());
    }

    @Test
    void updateCaseworkerRoleAssignments() {

        AtomicInteger atomicInteger = new AtomicInteger(1);
        AtomicInteger spyInteger = Mockito.spy(atomicInteger);

        Mockito.when(roleAssignmentService.createRoleAssignment(any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        ResponseEntity<Object> responseEntity = sutCaseworker.updateProfileRoleAssignments(
                "1",
                TestDataBuilder.buildRequestedRoleCollection(Status.CREATED),
                spyInteger,UserType.CASEWORKER);

        assertNotNull(responseEntity);
    }

    @Test
    void updateJudicialRoleAssignments() {

        AtomicInteger atomicInteger = new AtomicInteger(1);
        AtomicInteger spyInteger = Mockito.spy(atomicInteger);

        Mockito.when(roleAssignmentService.createRoleAssignment(any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        ResponseEntity<Object> responseEntity = sutJudicial.updateProfileRoleAssignments(
                "1",
                TestDataBuilder.buildRequestedRoleCollection(Status.CREATED),
                spyInteger,UserType.JUDICIAL);

        assertNotNull(responseEntity);
    }

    @Test
    void updateCaseworkerRoleAssignments_Failure() {

        AtomicInteger atomicInteger = new AtomicInteger(1);
        AtomicInteger spyInteger = Mockito.spy(atomicInteger);

        Mockito.when(roleAssignmentService.createRoleAssignment(any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.CONFLICT));

        sutCaseworker.updateProfileRoleAssignments(
                "1",
                TestDataBuilder.buildRequestedRoleCollection(Status.DELETED),
                spyInteger,UserType.CASEWORKER);

        verify(spyInteger, Mockito.times(1)).getAndIncrement();
    }

    @Test
    void updateJudicialRoleAssignments_Failure() {

        AtomicInteger atomicInteger = new AtomicInteger(1);
        AtomicInteger spyInteger = Mockito.spy(atomicInteger);

        Mockito.when(roleAssignmentService.createRoleAssignment(any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        Assertions.assertNotNull(sutJudicial.updateProfileRoleAssignments(
                "1",
                TestDataBuilder.buildRequestedRoleCollection(Status.DELETED),
                spyInteger,UserType.JUDICIAL));
    }



    @Test
    void createJudicialAssignmentsTest() {

        RequestMappingService<JudicialAccessProfile> requestMappingService = Mockito.spy(sutJudicial);

        final String actorId = "123e4567-e89b-42d3-a456-556642445612";

        Mockito.when(roleAssignmentService.createRoleAssignment(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED)
                        .body(AssignmentRequestBuilder.buildJudicialAssignmentRequest(false)));

        ResponseEntity<Object> responseEntity =
            requestMappingService.createJudicialAssignments(TestDataBuilder.buildJudicialAccessProfileMap(),
                    Collections.emptyList());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode resultNode = objectMapper.convertValue(responseEntity.getBody(),
            JsonNode.class);
        assertEquals(2, resultNode.size());
        assertEquals("judicial-organisational-role-mapping",
            resultNode.get(0).get("body").get("roleRequest").get("process")
                .asText());
        assertEquals("judge",
            resultNode.get(0).get("body").get("requestedRoles").get(0).get("roleName").asText());
        assertEquals(UserType.JUDICIAL.toString(),
            resultNode.get(0).get("body").get("requestedRoles").get(0).get("roleCategory").asText());

        assertEquals(actorId,
            resultNode.get(0).get("body").get("requestedRoles").get(0).get("actorId").asText());

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        Mockito.verify(roleAssignmentService, Mockito.times(2))
                .createRoleAssignment(any());
    }

}


