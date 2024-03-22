package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.OrganisationService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalUserService;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

@Service
@Slf4j
public class Scheduler {

    private final OrganisationService organisationService;
    private final ProfessionalUserService professionalUserService;
    private final UserRefreshQueueRepository userRefreshQueueRepository;
    private final ProcessEventTracker processEventTracker;

    public Scheduler(OrganisationService organisationService, ProfessionalUserService professionalUserService,
                     UserRefreshQueueRepository userRefreshQueueRepository, ProcessEventTracker processEventTracker) {
        this.organisationService = organisationService;
        this.professionalUserService = professionalUserService;
        this.userRefreshQueueRepository = userRefreshQueueRepository;
        this.processEventTracker = processEventTracker;
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.findOrganisationsWithStaleProfiles.cron}")
    void findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueProcess() {
        organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.findOrganisationChanges.cron}")
    void findOrganisationChangesAndInsertIntoOrganisationRefreshQueueProcess() {
        organisationService.findOrganisationChangesAndInsertIntoOrganisationRefreshQueue();
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.userRefresh.cron}")
    void processUserRefreshQueue() {
        StringBuilder errorMessageBuilder = new StringBuilder("");
        int successfulJobCount = 0;
        int failedJobCount = 0;
        String processName = "PRM Process 6 - Refresh users - Batch mode";
        log.info("Starting {}", processName);
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(processName);
        processEventTracker.trackEventStarted(processMonitorDto);

        while (userRefreshQueueRepository.getActiveUserRefreshQueueCount() >= 1) {
            try {
                boolean success = professionalUserService.refreshUsers(processMonitorDto);
                if (success) {
                    successfulJobCount++;
                } else {
                    failedJobCount++;
                }
            } catch (Exception e) {
                errorMessageBuilder.append(e.getMessage());
                failedJobCount++;
                log.error("Error occurred while processing user refresh queue", e);
            }
        }

        markProcessStatus(processMonitorDto, successfulJobCount > 0, failedJobCount > 0,
                errorMessageBuilder.toString());
        processEventTracker.trackEventCompleted(processMonitorDto);
    }

    private void markProcessStatus(ProcessMonitorDto processMonitorDto, boolean hasSuccessfulStep, boolean hasFailedAStep, String errorMessage) {
        if (!hasSuccessfulStep && hasFailedAStep) {
            processMonitorDto.markAsFailed(errorMessage);
        }

        if (hasSuccessfulStep && hasFailedAStep) {
            processMonitorDto.markAsPartialSuccess(errorMessage);
        }

        if (hasSuccessfulStep && !hasFailedAStep) {
            processMonitorDto.markAsSuccess();
        }
    }
}
