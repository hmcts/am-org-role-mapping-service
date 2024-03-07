package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrganisationServiceTest {
    private final String numDays = "90";

    private ProcessEventTracker processEventTracker = mock(ProcessEventTracker.class);
    private final OrganisationRefreshQueueRepository organisationRefreshQueueRepository =
            mock(OrganisationRefreshQueueRepository.class);

    private final OrganisationService organisationService = new OrganisationService(
            organisationRefreshQueueRepository, processEventTracker, numDays);

    @Captor
    private ArgumentCaptor<ProcessMonitorDto> processMonitorDtoArgumentCaptor;

    @Test
    void deleteActiveOrganisationRefreshRecordsTest() {
        organisationService.deleteActiveOrganisationRefreshRecords();

        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        verify(organisationRefreshQueueRepository, times(1))
                .deleteActiveOrganisationRefreshQueueEntitiesLastUpdatedBeforeNumberOfDays(numDays);
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.SUCCESS);
    }

    @Test
    void deleteActiveOrganisationRefreshRecordsTestWithFailure() {
        doThrow(ServiceException.class).when(organisationRefreshQueueRepository)
                .deleteActiveOrganisationRefreshQueueEntitiesLastUpdatedBeforeNumberOfDays(numDays);
        Assert.assertThrows(ServiceException.class, () ->
                organisationService
                        .deleteActiveOrganisationRefreshRecords()
        );

        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.FAILED);
    }
}
