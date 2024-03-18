package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.DatabaseDateTimeRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessionalUserServiceTest {

    private final PrdService prdService = mock(PrdService.class);
    private final UserRefreshQueueRepository userRefreshQueueRepository =
            Mockito.mock(UserRefreshQueueRepository.class);
    private final NamedParameterJdbcTemplate jdbcTemplate =
            Mockito.mock(NamedParameterJdbcTemplate.class);
    private final AccessTypesRepository accessTypesRepository = mock(AccessTypesRepository.class);
    private final DatabaseDateTimeRepository databaseDateTimeRepository = mock(DatabaseDateTimeRepository.class);
    private final BatchLastRunTimestampRepository batchLastRunTimestampRepository =
            mock(BatchLastRunTimestampRepository.class);
    private final ProcessEventTracker processEventTracker = Mockito.mock(ProcessEventTracker.class);
    private final ProfessionalRefreshOrchestrationHelper professionalRefreshOrchestrationHelper =
            Mockito.mock(ProfessionalRefreshOrchestrationHelper.class);
    private final PlatformTransactionManager transactionManager =
            Mockito.mock(PlatformTransactionManager.class);
    @Captor
    private ArgumentCaptor<ProcessMonitorDto> processMonitorDtoArgumentCaptor;

    ProfessionalUserService professionalUserService  = new ProfessionalUserService(
            prdService,
            userRefreshQueueRepository,
            "1",
            jdbcTemplate,
            accessTypesRepository, batchLastRunTimestampRepository, databaseDateTimeRepository,
            processEventTracker, professionalRefreshOrchestrationHelper, "10", "90",transactionManager,
            "2",
            "15",
            "60");

    @Test
    void refreshUsersTest() {
        List<AccessTypesEntity> allAccessTypes = new ArrayList<>();
        AccessTypesEntity accessTypesEntity1 = new AccessTypesEntity(1L, "some json");
        allAccessTypes.add(accessTypesEntity1);
        when(accessTypesRepository.findAll()).thenReturn(allAccessTypes);

        UserRefreshQueueEntity activeUser1 = UserRefreshQueueEntity.builder().userId("1").build();
        UserRefreshQueueEntity activeUser2 = UserRefreshQueueEntity.builder().userId("2").build();
        when(userRefreshQueueRepository.retrieveSingleActiveRecord())
                .thenReturn(activeUser1)
                .thenReturn(activeUser2)
                .thenReturn(null);

        professionalUserService.refreshUsers();

        verify(professionalRefreshOrchestrationHelper).refreshSingleUser(activeUser1, accessTypesEntity1);
        verify(professionalRefreshOrchestrationHelper).refreshSingleUser(activeUser2, accessTypesEntity1);
        verify(userRefreshQueueRepository, times(2)).clearUserRefreshRecord(any(), any(), any());
        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());

        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.SUCCESS);
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().size())
                .isEqualTo(7);
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(0))
                .isEqualTo("attempting retrieveSingleActiveRecord : COMPLETED");
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(1))
                .isEqualTo("attempting refreshAndClearUserRecord for userId=1 : COMPLETED");
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(2))
                .isEqualTo("attempting clearUserRefreshRecord for userId=1 : COMPLETED");
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(3))
                .isEqualTo("attempting next retrieveSingleActiveRecord - one found : COMPLETED");

        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(4))
                .isEqualTo("attempting refreshAndClearUserRecord for userId=2 : COMPLETED");
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(5))
                .isEqualTo("attempting clearUserRefreshRecord for userId=2 : COMPLETED");
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(6))
                .isEqualTo("attempting next retrieveSingleActiveRecord - none found : COMPLETED");
    }

    @Test
    void refreshUsersTestWithSingleAccessEntityError() {
        List<AccessTypesEntity> allAccessTypes = new ArrayList<>();
        AccessTypesEntity accessTypesEntity1 = new AccessTypesEntity(1L, "some json");
        AccessTypesEntity accessTypesEntity2 = new AccessTypesEntity(1L, "some json");
        allAccessTypes.add(accessTypesEntity1);
        allAccessTypes.add(accessTypesEntity2);
        when(accessTypesRepository.findAll()).thenReturn(allAccessTypes);

        Assertions.assertThrows(ServiceException.class, () ->
                professionalUserService.refreshUsers()
        );

        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.FAILED);
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndDetail())
                .isEqualTo("Single AccessTypesEntity not found");
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().size()).isEqualTo(0);
    }

    @Test
    void refreshUsersTestFailOnRefreshSingleUser() {
        List<AccessTypesEntity> allAccessTypes = new ArrayList<>();
        AccessTypesEntity accessTypesEntity1 = new AccessTypesEntity(1L, "some json");
        allAccessTypes.add(accessTypesEntity1);
        when(accessTypesRepository.findAll()).thenReturn(allAccessTypes);

        UserRefreshQueueEntity activeUser1 = UserRefreshQueueEntity.builder().userId("1").build();
        UserRefreshQueueEntity activeUser2 = UserRefreshQueueEntity.builder().userId("2").build();
        when(userRefreshQueueRepository.retrieveSingleActiveRecord())
                .thenReturn(activeUser1)
                .thenReturn(activeUser2)
                .thenReturn(null);

        doThrow(new ServiceException("Insert exception"))
                .when(professionalRefreshOrchestrationHelper).refreshSingleUser(activeUser2, accessTypesEntity1);

        Assertions.assertThrows(ServiceException.class, () ->
            professionalUserService.refreshUsers()
        );

        verify(professionalRefreshOrchestrationHelper).refreshSingleUser(activeUser1, accessTypesEntity1);
        verify(userRefreshQueueRepository).clearUserRefreshRecord(any(), any(), any());
        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());

        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.FAILED);
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().size())
                .isEqualTo(5);
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(0))
                .isEqualTo("attempting retrieveSingleActiveRecord : COMPLETED");
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(1))
                .isEqualTo("attempting refreshAndClearUserRecord for userId=1 : COMPLETED");
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(2))
                .isEqualTo("attempting clearUserRefreshRecord for userId=1 : COMPLETED");
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(3))
                .isEqualTo("attempting next retrieveSingleActiveRecord - one found : COMPLETED");

        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(4))
                .isEqualTo("attempting refreshAndClearUserRecord for userId=2");

        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndDetail())
                .isEqualTo("Insert exception");
    }

}
