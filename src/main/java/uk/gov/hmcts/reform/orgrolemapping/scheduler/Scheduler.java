package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalUserService;

@Slf4j
@Service
public class Scheduler {

    private final ProfessionalUserService professionalUserService;

    public Scheduler(ProfessionalUserService professionalUserService) {
        this.professionalUserService = professionalUserService;
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.cron}")
    void findUsersWithStaleOrganisationsAndInsertIntoRefreshQueueProcess() {
        professionalUserService.findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue();
    }
}
