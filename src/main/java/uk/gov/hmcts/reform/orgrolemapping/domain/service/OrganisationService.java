package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import javax.transaction.Transactional;

@Service
public class OrganisationService {

    private final OrganisationRefreshQueueRepository organisationRefreshQueueRepository;
    private final ProcessEventTracker processEventTracker;

    private final String activeOrganisationRefreshDays;

    public OrganisationService(OrganisationRefreshQueueRepository organisationRefreshQueueRepository,
            ProcessEventTracker processEventTracker,
            @Value("${professional.role.mapping.scheduling.organisationRefreshCleanup.activeOrganisationRefreshDays}")
            String activeOrganisationRefreshDays) {
        this.organisationRefreshQueueRepository = organisationRefreshQueueRepository;
        this.processEventTracker = processEventTracker;
        this.activeOrganisationRefreshDays = activeOrganisationRefreshDays;
    }


    @Transactional
    public void deleteActiveOrganisationRefreshRecords() {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("PRM Cleanup Process - Organisation");
        processEventTracker.trackEventStarted(processMonitorDto);
        try {
            organisationRefreshQueueRepository
                .deleteActiveOrganisationRefreshQueueEntitiesLastUpdatedBeforeNumberOfDays(
                        activeOrganisationRefreshDays);
        } catch (Exception exception) {
            processMonitorDto.markAsFailed(exception.getMessage());
            processEventTracker.trackEventCompleted(processMonitorDto);
            throw exception;
        }

        processMonitorDto.markAsSuccess();
        processEventTracker.trackEventCompleted(processMonitorDto);
    }
}
