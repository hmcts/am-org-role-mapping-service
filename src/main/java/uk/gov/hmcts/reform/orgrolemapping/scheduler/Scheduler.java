package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.OrganisationService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalUserService;

@Service
public class Scheduler {

    private final OrganisationService organisationService;
    private final ProfessionalUserService userService;

    public Scheduler(OrganisationService organisationService, ProfessionalUserService userService) {
        this.organisationService = organisationService;
        this.userService = userService;
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.organisationRefreshCleanup.cron}")
    void deleteActiveOrganisationRefreshRecords() {
        organisationService.deleteActiveOrganisationRefreshRecords();
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.userRefreshCleanup.cron}")
    void deleteActiveUserRefreshRecords() {
        userService.deleteActiveUserRefreshRecords();
    }

}
