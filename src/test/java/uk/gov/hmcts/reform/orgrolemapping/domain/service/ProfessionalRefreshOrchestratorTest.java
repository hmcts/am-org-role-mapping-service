package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SUCCESS_ROLE_REFRESH;

@RunWith(MockitoJUnitRunner.class)
class ProfessionalRefreshOrchestratorTest {

    @Mock
    private AccessTypesRepository accessTypesRepository;
    @Mock
    private UserRefreshQueueRepository userRefreshQueueRepository;
    @Mock
    private PrdService prdService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private RoleAssignmentService roleAssignmentService;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    ProcessEventTracker processEventTracker;
    @Mock
    private ProfessionalRefreshOrchestrationHelper professionalRefreshOrchestrationHelper;
    @InjectMocks
    private ProfessionalRefreshOrchestrator professionalRefreshOrchestrator;
    @Captor
    private ArgumentCaptor<ProcessMonitorDto> processMonitorDtoArgumentCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void refreshProfessionalRoleAssignmentRecordsExitStep1() throws IOException {

        doReturn(ResponseEntity.status(HttpStatus.OK).body(TestDataBuilder.buildGetRefreshUsersResponse("ID")))
                .when(prdService).getRefreshUser(any());

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body("RoleAssignment"))
                .when(roleAssignmentService).createRoleAssignment(any());

        String accessType = """
                [
                  {
                 "jurisdictionId": "1",
                "organisationProfileId": "1",
                    "accessTypeId": "1",
                "enabled": true
                 }
                ]""";

        doReturn(accessType)
                .when(objectMapper).writeValueAsString(any());

        Optional<AccessTypesEntity> accessTypesEntity = Optional.of(new AccessTypesEntity());

        accessTypesEntity.get().setVersion(1L);
        doReturn(accessTypesEntity)
               .when(accessTypesRepository).findFirstByOrderByVersionDesc();

        String userId = "21334a2b-79ce-44eb-9168-2d49a744be9d";
        UserRefreshQueueEntity userRefreshQueueEntity = new UserRefreshQueueEntity();
        userRefreshQueueEntity.setUserId(userId);
        userRefreshQueueEntity.setAccessTypesMinVersion(10);

        doReturn(userRefreshQueueEntity)
                .when(userRefreshQueueRepository).findByUserId(any());

        assertResponse(professionalRefreshOrchestrator.refreshProfessionalUser(userId));
    }

    @Test
    void refreshProfessionalRoleAssignmentRecordsExitStep2() throws IOException {
        doReturn(ResponseEntity.status(HttpStatus.OK).body(TestDataBuilder.buildGetRefreshUsersResponse("ID")))
                .when(prdService).getRefreshUser(any());

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body("RoleAssignment"))
                .when(roleAssignmentService).createRoleAssignment(any());

        String accessType = """
                [
                  {
                 "jurisdictionId": "1",
                "organisationProfileId": "1",
                    "accessTypeId": "1",
                "enabled": true
                 }
                ]""";

        doReturn(accessType)
                .when(objectMapper).writeValueAsString(any());

        Optional<AccessTypesEntity> accessTypesEntity = Optional.of(new AccessTypesEntity());

        accessTypesEntity.get().setVersion(11L);
        doReturn(accessTypesEntity)
                .when(accessTypesRepository).findFirstByOrderByVersionDesc();

        String userId = "21334a2b-79ce-44eb-9168-2d49a744be9d";
        UserRefreshQueueEntity userRefreshQueueEntity = new UserRefreshQueueEntity();
        userRefreshQueueEntity.setUserId(userId);
        userRefreshQueueEntity.setAccessTypesMinVersion(10);
        userRefreshQueueEntity.setDeleted(LocalDateTime.now());

        doReturn(userRefreshQueueEntity)
                .when(userRefreshQueueRepository).findByUserId(any());

        assertResponse(professionalRefreshOrchestrator.refreshProfessionalUser(userId));
    }

    @Test
    void refreshProfessionalRoleAssignmentRecordsExitStep3() throws IOException {
        doReturn(ResponseEntity.status(HttpStatus.OK).body(TestDataBuilder.buildGetRefreshUsersResponse("ID")))
                .when(prdService).getRefreshUser(any());

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body("RoleAssignment"))
                .when(roleAssignmentService).createRoleAssignment(any());

        String accessType = """
                [
                  {
                 "jurisdictionId": "1",
                "organisationProfileId": "1",
                    "accessTypeId": "1",
                "enabled": true
                 }
                ]""";

        doReturn(accessType)
                .when(objectMapper).writeValueAsString(any());

        Optional<AccessTypesEntity> accessTypesEntity = Optional.of(new AccessTypesEntity());

        accessTypesEntity.get().setVersion(11L);
        doReturn(accessTypesEntity)
                .when(accessTypesRepository).findFirstByOrderByVersionDesc();

        String userId = "21334a2b-79ce-44eb-9168-2d49a744be9d";
        UserRefreshQueueEntity userRefreshQueueEntity = new UserRefreshQueueEntity();
        userRefreshQueueEntity.setUserId(userId);
        userRefreshQueueEntity.setAccessTypesMinVersion(10);
        userRefreshQueueEntity.setDeleted(null);
        userRefreshQueueEntity.setOrganisationStatus("abcdefg");
        //TODO this should be non ACTIVE status
        doReturn(userRefreshQueueEntity)
                .when(userRefreshQueueRepository).findByUserId(any());

        assertResponse(professionalRefreshOrchestrator.refreshProfessionalUser(userId));
    }

    @Test
    void refreshProfessionalRoleAssignmentRecordsExitStep4() throws IOException {
        doReturn(ResponseEntity.status(HttpStatus.OK).body(TestDataBuilder.buildGetRefreshUsersResponse("ID")))
                .when(prdService).getRefreshUser(any());

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body("RoleAssignment"))
                .when(roleAssignmentService).createRoleAssignment(any());

        String accessType = """
                [
                  {
                 "jurisdictionId": "1",
                "organisationProfileId": "1",
                    "accessTypeId": "1",
                "enabled": true
                 }
                ]""";

        doReturn(accessType)
                .when(objectMapper).writeValueAsString(any());

        Optional<AccessTypesEntity> accessTypesEntity = Optional.of(new AccessTypesEntity());

        accessTypesEntity.get().setVersion(11L);
        doReturn(accessTypesEntity)
                .when(accessTypesRepository).findFirstByOrderByVersionDesc();

        String userId = "21334a2b-79ce-44eb-9168-2d49a744be9d";
        UserRefreshQueueEntity userRefreshQueueEntity = new UserRefreshQueueEntity();
        userRefreshQueueEntity.setUserId(userId);
        userRefreshQueueEntity.setAccessTypesMinVersion(10);
        userRefreshQueueEntity.setDeleted(null);
        userRefreshQueueEntity.setOrganisationStatus("abcdefg");
        //TODO this should be non ACTIVE status
        doReturn(userRefreshQueueEntity)
                .when(userRefreshQueueRepository).findByUserId(any());

        assertResponse(professionalRefreshOrchestrator.refreshProfessionalUser(userId));
    }

    @Test
    void refreshProfessionalRoleAssignmentRecordsExitNormalProcess() throws IOException {
        doReturn(ResponseEntity.status(HttpStatus.OK).body(TestDataBuilder.buildGetRefreshUsersResponse("ID")))
                .when(prdService).getRefreshUser(any());

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body("RoleAssignment"))
                .when(roleAssignmentService).createRoleAssignment(any());

        String accessType = "[\n  {\n \"jurisdictionId\": \"1\",\n\"organisationProfileId\": \"1\",\n"
                +  "    \"accessTypeId\": \"1\",\n\"enabled\": true\n }\n]";

        doReturn(accessType)
                .when(objectMapper).writeValueAsString(any());


        Optional<AccessTypesEntity> accessTypesEntity = Optional.of(TestDataBuilder.buildAccessTypesEntity());

        doReturn(accessTypesEntity)
                .when(accessTypesRepository).findFirstByOrderByVersionDesc();


        String userId = "21334a2b-79ce-44eb-9168-2d49a744be9d";
        doReturn(TestDataBuilder.buildUserRefreshQueueEntity(userId))
                .when(userRefreshQueueRepository).findByUserId(any());

        assertResponse(professionalRefreshOrchestrator.refreshProfessionalUser(userId));
    }

    private void assertResponse(ResponseEntity<Object> actualResponse) {
        assertNotNull(actualResponse);
        assertNotNull(actualResponse.getBody());
        assertEquals(actualResponse.getBody(), Map.of("Message", SUCCESS_ROLE_REFRESH));
        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
            .isEqualTo(EndStatus.SUCCESS);
    }
}
