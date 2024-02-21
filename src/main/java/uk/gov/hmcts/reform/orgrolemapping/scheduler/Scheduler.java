package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.OrganisationService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalUserService;

@Service
public class Scheduler {

    private final OrganisationService organisationService;
    private final ProfessionalUserService professionalUserService;

    public Scheduler(OrganisationService organisationService, ProfessionalUserService professionalUserService) {
        this.organisationService = organisationService;
        this.professionalUserService = professionalUserService;
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.findOrganisationsWithStaleProfiles.cron}")
    void findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueProcess() {
        organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.findOrganisationChanges.cron}")
    void findOrganisationChangesAndInsertIntoOrganisationRefreshQueueProcess() {
        organisationService.findOrganisationChangesAndInsertIntoOrganisationRefreshQueue();
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.findUserChanges.cron}")
    void findUserChangesAndInsertIntoUserRefreshQueue() {
        professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue();
    }
}
