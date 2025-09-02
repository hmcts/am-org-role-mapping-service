package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
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

    public Scheduler(CaseDefinitionService caseDefinitionService, OrganisationService organisationService,
                     ProfessionalUserService professionalUserService) {
        this.caseDefinitionService = caseDefinitionService;
        this.organisationService = organisationService;
        this.professionalUserService = professionalUserService;
    }

    // PRM Process 1
    @Scheduled(cron = "${professional.role.mapping.scheduling.findAndUpdateCaseDefinitionChanges.cron}")
    public ProcessMonitorDto findAndUpdateCaseDefinitionChanges() {
        return caseDefinitionService.findAndUpdateCaseDefinitionChanges();
    }

    // PRM Process 2
    @Scheduled(cron = "${professional.role.mapping.scheduling.findOrganisationsWithStaleProfiles.cron}")
    public ProcessMonitorDto findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueProcess() {
        return organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();
    }

    // PRM Process 3
    @Scheduled(cron = "${professional.role.mapping.scheduling.findOrganisationChanges.cron}")
    public ProcessMonitorDto findOrganisationChangesAndInsertIntoOrganisationRefreshQueueProcess() {
        return organisationService
            .findOrganisationChangesAndInsertIntoOrganisationRefreshQueue();
    }

    // PRM Process 4
    @Scheduled(cron = "${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.cron}")
    public ProcessMonitorDto findUsersWithStaleOrganisationsAndInsertIntoRefreshQueueProcess() {
        return professionalUserService
            .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue();
    }

    // PRM Process 5
    @Scheduled(cron = "${professional.role.mapping.scheduling.findUserChanges.cron}")
    public ProcessMonitorDto findUserChangesAndInsertIntoUserRefreshQueue() {
        return professionalUserService
            .findUserChangesAndInsertIntoUserRefreshQueue();
    }
}
