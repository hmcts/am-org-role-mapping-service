
package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.CaseDefinitionService;

@Service
public class Scheduler {

    private final CaseDefinitionService caseDefinitionService;

    public Scheduler(CaseDefinitionService caseDefinitionService) {
        this.caseDefinitionService = caseDefinitionService;
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.cron}")
    void findAndUpdateCaseDefinitionChanges() throws JsonProcessingException {
        caseDefinitionService.findAndUpdateCaseDefinitionChanges();
    }
}