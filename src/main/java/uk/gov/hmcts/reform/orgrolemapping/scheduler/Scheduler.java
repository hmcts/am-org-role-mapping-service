package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.OrganisationService;

@Service
public class Scheduler {

    private final OrganisationService organisationService;

    public Scheduler(OrganisationService organisationService) {
        this.organisationService = organisationService;
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.cron}")
    void findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueProcess() {
        organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();
    }
}
