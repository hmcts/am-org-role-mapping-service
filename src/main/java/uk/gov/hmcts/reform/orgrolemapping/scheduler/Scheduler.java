package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.OrganisationService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalUserService;

@Service
public class Scheduler {

    private final OrganisationService organisationService;
    private final ProfessionalUserService professionalUserService;
    private final UserRefreshQueueRepository userRefreshQueueRepository;

    public Scheduler(OrganisationService organisationService, ProfessionalUserService professionalUserService,
                     UserRefreshQueueRepository userRefreshQueueRepository) {
        this.organisationService = organisationService;
        this.professionalUserService = professionalUserService;
        this.userRefreshQueueRepository = userRefreshQueueRepository;
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
        while (userRefreshQueueRepository.getActiveUserRefreshQueueCount() >= 1) {
            professionalUserService.refreshUsers2();
        }
    }

}
