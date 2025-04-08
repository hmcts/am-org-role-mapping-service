package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.OrganisationService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.CaseDefinitionService;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;

@Slf4j
@Service
public class Scheduler {

    private final CaseDefinitionService caseDefinitionService;
    private final OrganisationService organisationService;

    public Scheduler(CaseDefinitionService caseDefinitionService,
        OrganisationService organisationService) {
        this.caseDefinitionService = caseDefinitionService;
        this.organisationService = organisationService;
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.findAndUpdateCaseDefinitionChanges.cron}")
    public ProcessMonitorDto findAndUpdateCaseDefinitionChanges() {
        ProcessMonitorDto processMonitorDto = caseDefinitionService.findAndUpdateCaseDefinitionChanges();
        logAsJson(processMonitorDto);
        return processMonitorDto;
    }

    private void logAsJson(ProcessMonitorDto processMonitorDto) {
        log.debug("Process Monitor: {}", JacksonUtils.writeValueAsPrettyJson(processMonitorDto));
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.findOrganisationsWithStaleProfiles.cron}")
    public ProcessMonitorDto findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueProcess() {
        ProcessMonitorDto processMonitorDto = organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();
        logAsJson(processMonitorDto);
        return processMonitorDto;
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.findOrganisationChanges.cron}")
    void findOrganisationChangesAndInsertIntoOrganisationRefreshQueueProcess() {
        organisationService.findOrganisationChangesAndInsertIntoOrganisationRefreshQueue();
    }
}
