package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.CaseDefinitionService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.OrganisationService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalUserService;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

@Slf4j
@Service
public class Scheduler {

    private final CaseDefinitionService caseDefinitionService;
    private final OrganisationService organisationService;
    private final ProfessionalUserService professionalUserService;

    public Scheduler(CaseDefinitionService caseDefinitionService,
                     OrganisationService organisationService,
                     ProfessionalUserService professionalUserService) {
        this.caseDefinitionService = caseDefinitionService;
        this.organisationService = organisationService;
        this.professionalUserService = professionalUserService;
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.organisationRefreshCleanup.cron}")
    @SchedulerLock(name = "PRM_organisationRefreshCleanup",
            lockAtLeastFor = "${professional.role.mapping.scheduling.organisationRefreshCleanup.lockAtLeastFor}",
            lockAtMostFor = "${professional.role.mapping.scheduling.organisationRefreshCleanup.lockAtMostFor}")
    public ProcessMonitorDto deleteInactiveOrganisationRefreshRecords() {
        return organisationService.deleteInactiveOrganisationRefreshRecords();
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.userRefreshCleanup.cron}")
    @SchedulerLock(name = "PRM_userRefreshCleanup",
            lockAtLeastFor = "${professional.role.mapping.scheduling.userRefreshCleanup.lockAtLeastFor}",
            lockAtMostFor = "${professional.role.mapping.scheduling.userRefreshCleanup.lockAtMostFor}")
    public ProcessMonitorDto deleteInactiveUserRefreshRecords() {
        return professionalUserService.deleteInactiveUserRefreshRecords();
    }

    // PRM Process 1
    @Scheduled(cron = "${professional.role.mapping.scheduling.findAndUpdateCaseDefinitionChanges.cron}")
    @SchedulerLock(name = "PRM_Process_1_findAndUpdateCaseDefinitionChanges",
            lockAtLeastFor =
                    "${professional.role.mapping.scheduling.findAndUpdateCaseDefinitionChanges.lockAtLeastFor}",
            lockAtMostFor =
                    "${professional.role.mapping.scheduling.findAndUpdateCaseDefinitionChanges.lockAtMostFor}")
    public ProcessMonitorDto findAndUpdateCaseDefinitionChanges() {
        return caseDefinitionService.findAndUpdateCaseDefinitionChanges();
    }

    // PRM Process 2
    @Scheduled(cron = "${professional.role.mapping.scheduling.findOrganisationsWithStaleProfiles.cron}")
    @SchedulerLock(name = "PRM_Process_2_findOrganisationsWithStaleProfiles",
            lockAtLeastFor =
                    "${professional.role.mapping.scheduling.findOrganisationsWithStaleProfiles.lockAtLeastFor}",
            lockAtMostFor =
                    "${professional.role.mapping.scheduling.findOrganisationsWithStaleProfiles.lockAtMostFor}")
    public ProcessMonitorDto findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueProcess() {
        return organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();
    }

    // PRM Process 3
    @Scheduled(cron = "${professional.role.mapping.scheduling.findOrganisationChanges.cron}")
    @SchedulerLock(name = "PRM_Process_3_findOrganisationChanges",
            lockAtLeastFor = "${professional.role.mapping.scheduling.findOrganisationChanges.lockAtLeastFor}",
            lockAtMostFor = "${professional.role.mapping.scheduling.findOrganisationChanges.lockAtMostFor}")
    public ProcessMonitorDto findOrganisationChangesAndInsertIntoOrganisationRefreshQueueProcess() {
        return organisationService
            .findOrganisationChangesAndInsertIntoOrganisationRefreshQueue();
    }

    // PRM Process 4
    @Scheduled(cron = "${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.cron}")
    @SchedulerLock(name = "PRM_Process_4_findUsersWithStaleOrganisations",
            lockAtLeastFor = "${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.lockAtLeastFor}",
            lockAtMostFor = "${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.lockAtMostFor}")
    public ProcessMonitorDto findUsersWithStaleOrganisationsAndInsertIntoRefreshQueueProcess() {
        return professionalUserService
            .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue();
    }

    // PRM Process 5
    @Scheduled(cron = "${professional.role.mapping.scheduling.findUserChanges.cron}")
    @SchedulerLock(name = "PRM_Process_5_findUserChanges",
            lockAtLeastFor = "${professional.role.mapping.scheduling.findUserChanges.lockAtLeastFor}",
            lockAtMostFor = "${professional.role.mapping.scheduling.findUserChanges.lockAtMostFor}")
    public ProcessMonitorDto findUserChangesAndInsertIntoUserRefreshQueue() {
        return professionalUserService
            .findUserChangesAndInsertIntoUserRefreshQueue();
    }

    // PRM Process 6
    @Scheduled(cron = "${professional.role.mapping.scheduling.userRefresh.cron}")
    @SchedulerLock(name = "PRM_Process_6_userRefresh",
            lockAtLeastFor = "${professional.role.mapping.scheduling.userRefresh.lockAtLeastFor}",
            lockAtMostFor = "${professional.role.mapping.scheduling.userRefresh.lockAtMostFor}")
    public ProcessMonitorDto processUserRefreshQueue() {
        return professionalUserService.refreshUsersBatchMode();
    }

}
