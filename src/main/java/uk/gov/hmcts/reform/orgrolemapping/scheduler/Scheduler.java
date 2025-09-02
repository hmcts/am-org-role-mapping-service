package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.CaseDefinitionService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.OrganisationService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalUserService;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

@Slf4j
@Service
public class Scheduler {

    private final CaseDefinitionService caseDefinitionService;
    private final OrganisationService organisationService;
    private final ProfessionalUserService professionalUserService;

    private final UserRefreshQueueRepository userRefreshQueueRepository;

    private final ProcessEventTracker processEventTracker;

    public Scheduler(CaseDefinitionService caseDefinitionService,
                     OrganisationService organisationService,
                     ProfessionalUserService professionalUserService,
                     UserRefreshQueueRepository userRefreshQueueRepository,
                     ProcessEventTracker processEventTracker) {
        this.caseDefinitionService = caseDefinitionService;
        this.organisationService = organisationService;
        this.professionalUserService = professionalUserService;

        this.userRefreshQueueRepository = userRefreshQueueRepository;

        this.processEventTracker = processEventTracker;
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

    @Scheduled(cron = "${professional.role.mapping.scheduling.userRefresh.cron}")
    public ProcessMonitorDto processUserRefreshQueue() {
        return professionalUserService.refreshUsersBatchMode();
    }
}
