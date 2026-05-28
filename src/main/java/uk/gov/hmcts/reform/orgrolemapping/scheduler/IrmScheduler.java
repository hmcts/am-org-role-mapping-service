package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.IdamRoleMappingService;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

@Slf4j
@Service
@ConditionalOnExpression(
        "${idam.role.management.scheduling.enabled} || ${testing.support.enabled}")
public class IrmScheduler {

    public static final String DELETEINTERVALDAYS = "idam.role.management.scheduling.housekeeping.deleteIntervalDays";
    private final IdamRoleMappingService idamRoleMappingService;

    public IrmScheduler(IdamRoleMappingService idamRoleMappingService) {
        this.idamRoleMappingService = idamRoleMappingService;
    }

    @Scheduled(cron = "${idam.role.management.scheduling.judicial.cron}")
    public ProcessMonitorDto processJudicialQueue() {
        log.info("Starting IRM Scheduler for Judicial Queue");
        return idamRoleMappingService.processJudicialQueue();
    }

    @Scheduled(cron = "${idam.role.management.scheduling.housekeeping.cron}")
    public ProcessMonitorDto deleteInactiveQueueEntries() {
        log.info("Starting IRM Scheduler for Delete Inactive Queue Entries");
        String deleteIntervalDays = System.getProperty(DELETEINTERVALDAYS);
        log.debug(String.format("deleteIntervalDays=%s", deleteIntervalDays));
        return idamRoleMappingService.deleteInactiveQueueEntries(deleteIntervalDays);
    }
}
