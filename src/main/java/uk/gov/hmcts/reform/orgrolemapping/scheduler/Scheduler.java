package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.OrganisationService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.CaseDefinitionService;

@Service
public class Scheduler {

    private final CaseDefinitionService caseDefinitionService;
    private final OrganisationService organisationService;

    public Scheduler(CaseDefinitionService caseDefinitionService, OrganisationService organisationService) {
        this.caseDefinitionService = caseDefinitionService;
        this.organisationService = organisationService;
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.findAndUpdateCaseDefinitionChanges.cron}")
    void findAndUpdateCaseDefinitionChanges() {
        caseDefinitionService.findAndUpdateCaseDefinitionChanges();
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.findOrganisationsWithStaleProfiles.cron}")
    void findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueProcess() {
        organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();
    }
}
