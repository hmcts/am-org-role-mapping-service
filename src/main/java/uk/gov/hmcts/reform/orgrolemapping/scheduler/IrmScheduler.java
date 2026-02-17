package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.IdamRoleMappingService;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

@Slf4j
@Service
@ConditionalOnProperty(name = "idam.role.management.scheduling.enabled")
public class IrmScheduler {

    private final IdamRoleMappingService idamRoleMappingService;

    public IrmScheduler(IdamRoleMappingService idamRoleMappingService) {
        this.idamRoleMappingService = idamRoleMappingService;
    }

    @Scheduled(cron = "${idam.role.management.scheduling.judicial.cron}")
    public ProcessMonitorDto processJudicialQueue() {
        log.info("Starting IRM Scheduler for Judicial Queue");
        return idamRoleMappingService.processJudicialQueue();
    }
}
