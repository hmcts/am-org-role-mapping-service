package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.OrganisationService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalUserService;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class SchedulerTest {

    @Mock
    private OrganisationService organisationService;

    @Mock
    private ProfessionalUserService professionalUserService;

    @Mock
    private UserRefreshQueueRepository userRefreshQueueRepository;

    @Mock
    private ProcessEventTracker processEventTracker;

    @InjectMocks
    private Scheduler scheduler;

    @Captor
    private ArgumentCaptor<ProcessMonitorDto> processMonitorDtoArgumentCaptor;

    @Captor
    private ArgumentCaptor<ProcessEventTracker> processEventTrackerArgumentCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processUserRefreshQueue_start_and_mark_success_and_completed_when_all_entities_are_succesful() {
        // arrange
        when(userRefreshQueueRepository.getActiveUserRefreshQueueCount()).thenReturn(1L).thenReturn(0L);
        when(professionalUserService.refreshUsers(any())).thenReturn(true);

        // act
        scheduler.processUserRefreshQueue();

        // assert
        verify(processEventTracker, times(1)).trackEventStarted(processMonitorDtoArgumentCaptor.capture());
        verify(processEventTracker, times(1)).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());

        List<ProcessMonitorDto> capturedArguments = processMonitorDtoArgumentCaptor.getAllValues();
        assertEquals(EndStatus.SUCCESS, capturedArguments.get(0).getEndStatus());
    }

    @Test
    void processUserRefreshQueue_start_and_mark_failure_and_completed_when_no_entities_are_successful() {
        // arrange
        when(userRefreshQueueRepository.getActiveUserRefreshQueueCount()).thenReturn(1L).thenReturn(0L);
        when(professionalUserService.refreshUsers(any())).thenReturn(false);

        // act
        scheduler.processUserRefreshQueue();

        // assert
        verify(processEventTracker, times(1)).trackEventStarted(processMonitorDtoArgumentCaptor.capture());
        verify(processEventTracker, times(1)).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());

        List<ProcessMonitorDto> capturedArguments = processMonitorDtoArgumentCaptor.getAllValues();
        assertEquals(EndStatus.FAILED, capturedArguments.get(0).getEndStatus());
    }

    @Test
    void processUserRefreshQueue_start_and_mark_partial_success_and_completed_when_some_entities_are_successful() {
        // arrange
        when(userRefreshQueueRepository.getActiveUserRefreshQueueCount()).thenReturn(2L).thenReturn(1L).thenReturn(0L);
        when(professionalUserService.refreshUsers(any())).thenReturn(true).thenReturn(false);

        // act
        scheduler.processUserRefreshQueue();

        // assert
        verify(processEventTracker, times(1)).trackEventStarted(processMonitorDtoArgumentCaptor.capture());
        verify(processEventTracker, times(1)).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());

        List<ProcessMonitorDto> capturedArguments = processMonitorDtoArgumentCaptor.getAllValues();
        assertEquals(EndStatus.PARTIAL_SUCCESS, capturedArguments.get(0).getEndStatus());
    }

    @Test
    void processUserRefreshQueue_treatAnExceptionAsFailedStep() {
        // arrange
        when(userRefreshQueueRepository.getActiveUserRefreshQueueCount()).thenReturn(1L).thenReturn(0L);
        when(professionalUserService.refreshUsers(any()))
                .thenThrow(new ServiceException("Single AccessTypesEntity not found"));

        // act
        scheduler.processUserRefreshQueue();

        // assert
        verify(processEventTracker, times(1)).trackEventStarted(processMonitorDtoArgumentCaptor.capture());
        verify(processEventTracker, times(1)).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());

        List<ProcessMonitorDto> capturedArguments = processMonitorDtoArgumentCaptor.getAllValues();
        assertEquals(EndStatus.FAILED, capturedArguments.get(0).getEndStatus());
    }
}