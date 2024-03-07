package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ProfessionalUserServiceTest {
    private final String numDays = "90";
    private final UserRefreshQueueRepository userRefreshQueueRepository =
            Mockito.mock(UserRefreshQueueRepository.class);
    private ProcessEventTracker processEventTracker = mock(ProcessEventTracker.class);

    ProfessionalUserService userService = new ProfessionalUserService(userRefreshQueueRepository,
            processEventTracker,
            "90");

    @Captor
    private ArgumentCaptor<ProcessMonitorDto> processMonitorDtoArgumentCaptor;

    @Test
    void deleteActiveUserRefreshRecordsTest() {
        userService.deleteActiveUserRefreshRecords();

        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        verify(userRefreshQueueRepository, times(1))
                .deleteActiveUserRefreshQueueEntitiesLastUpdatedBeforeNumberOfDays(numDays);
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.SUCCESS);
    }

    @Test
    void deleteActiveUserRefreshRecordsTestWithFailure() {
        doThrow(ServiceException.class).when(userRefreshQueueRepository)
                .deleteActiveUserRefreshQueueEntitiesLastUpdatedBeforeNumberOfDays(numDays);
        Assert.assertThrows(ServiceException.class, () ->
                userService.deleteActiveUserRefreshRecords()
        );

        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.FAILED);
    }

}
