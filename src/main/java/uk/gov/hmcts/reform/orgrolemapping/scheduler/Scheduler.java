package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalUserService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.OrganisationService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.CaseDefinitionService;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

@Slf4j
@Service
public class Scheduler {

    private final CaseDefinitionService caseDefinitionService;
    private final OrganisationService organisationService;
    private final ProfessionalUserService professionalUserService;
    private final OrganisationRefreshQueueRepository organisationRefreshQueueRepository;

    public Scheduler(CaseDefinitionService caseDefinitionService, OrganisationService organisationService,
                     ProfessionalUserService professionalUserService,
                     OrganisationRefreshQueueRepository organisationRefreshQueueRepository) {
        this.caseDefinitionService = caseDefinitionService;
        this.organisationService = organisationService;
        this.professionalUserService = professionalUserService;
        this.organisationRefreshQueueRepository = organisationRefreshQueueRepository;
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.findAndUpdateCaseDefinitionChanges.cron}")
    public ProcessMonitorDto findAndUpdateCaseDefinitionChanges() {
        ProcessMonitorDto processMonitorDto = caseDefinitionService.findAndUpdateCaseDefinitionChanges();
        return processMonitorDto;
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.findOrganisationsWithStaleProfiles.cron}")
    public ProcessMonitorDto findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueProcess() {
        ProcessMonitorDto processMonitorDto = organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();
        return processMonitorDto;
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.findOrganisationChanges.cron}")
    public ProcessMonitorDto findOrganisationChangesAndInsertIntoOrganisationRefreshQueueProcess() {
        ProcessMonitorDto processMonitorDto = organisationService
            .findOrganisationChangesAndInsertIntoOrganisationRefreshQueue();
        return processMonitorDto;
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.cron}")
    public ProcessMonitorDto findUsersWithStaleOrganisationsAndInsertIntoRefreshQueueProcess() {
        ProcessMonitorDto processMonitorDto = professionalUserService
            .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue();
        return processMonitorDto;
    }

}
