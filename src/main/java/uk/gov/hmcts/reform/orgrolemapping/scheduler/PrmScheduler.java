package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

@Slf4j
@Service
@ConditionalOnProperty(name = "professional.role.mapping.scheduling.enabled")
public class PrmScheduler {

    @Scheduled(cron = "${idam.role.management.scheduling.judicial.cron}")
    public ProcessMonitorDto processJudicialQueue() {
        log.info("Starting PRM Scheduler Test");
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Test");
        processMonitorDto.markAsSuccess();
        return processMonitorDto;
    }
}
