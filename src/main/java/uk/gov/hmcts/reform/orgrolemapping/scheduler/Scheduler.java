package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalUserService;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

@Slf4j
@Service
public class Scheduler {

    private final ProfessionalUserService professionalUserService;
    private final OrganisationRefreshQueueRepository organisationRefreshQueueRepository;
    private final ProcessEventTracker processEventTracker;

    public Scheduler(ProfessionalUserService professionalUserService,
                     OrganisationRefreshQueueRepository organisationRefreshQueueRepository,
                     ProcessEventTracker processEventTracker) {
        this.professionalUserService = professionalUserService;
        this.organisationRefreshQueueRepository = organisationRefreshQueueRepository;
        this.processEventTracker = processEventTracker;
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.cron}")
    void findUsersWithStaleOrganisationsAndInsertIntoRefreshQueueProcess() {
        StringBuilder errorMessageBuilder = new StringBuilder();
        int successfulJobCount = 0;
        int failedJobCount = 0;

        String processName = "PRM Process 4 - Find Users with Stale Organisations";
        log.info("Starting {}", processName);
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(processName);
        processEventTracker.trackEventStarted(processMonitorDto);

        while (organisationRefreshQueueRepository.getActiveOrganisationRefreshQueueCount() >= 1) {
            try {
                boolean success = professionalUserService
                        .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue(processMonitorDto);
                if (success) {
                    successfulJobCount++;
                } else {
                    failedJobCount++;
                }
            } catch (Exception e) {
                log.error("Error occurred while processing the job", e);
                errorMessageBuilder.append(e.getMessage());
                failedJobCount++;
            }
        }
        markProcessStatus(processMonitorDto, successfulJobCount > 0, failedJobCount > 0,
                errorMessageBuilder.toString());
        processEventTracker.trackEventCompleted(processMonitorDto);
    }

    private void markProcessStatus(ProcessMonitorDto processMonitorDto, boolean hasSuccessfulStep,
                                   boolean hasFailedAStep, String errorMessage) {
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
