package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
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
    private final OrganisationRefreshQueueRepository organisationRefreshQueueRepository;

    public Scheduler(CaseDefinitionService caseDefinitionService, OrganisationService organisationService,
                     ProfessionalUserService professionalUserService,
                     OrganisationRefreshQueueRepository organisationRefreshQueueRepository) {
        this.caseDefinitionService = caseDefinitionService;
        this.organisationService = organisationService;
        this.professionalUserService = professionalUserService;
        this.organisationRefreshQueueRepository = organisationRefreshQueueRepository;
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.findAndUpdateCaseDefinitionChanges.cron}")
    public ProcessMonitorDto findAndUpdateCaseDefinitionChanges() {
        ProcessMonitorDto processMonitorDto = caseDefinitionService.findAndUpdateCaseDefinitionChanges();
        return processMonitorDto;
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.findOrganisationsWithStaleProfiles.cron}")
    public ProcessMonitorDto findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueProcess() {
        ProcessMonitorDto processMonitorDto = organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();
        return processMonitorDto;
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.findOrganisationChanges.cron}")
    public ProcessMonitorDto findOrganisationChangesAndInsertIntoOrganisationRefreshQueueProcess() {
        ProcessMonitorDto processMonitorDto = organisationService
            .findOrganisationChangesAndInsertIntoOrganisationRefreshQueue();
        return processMonitorDto;
    }

    @Scheduled(cron = "${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.cron}")
    public List<ProcessMonitorDto> findUsersWithStaleOrganisationsAndInsertIntoRefreshQueueProcess() {
        List<ProcessMonitorDto> processMonitorDtos = new ArrayList<>();
        try {
            while (organisationRefreshQueueRepository.getActiveOrganisationRefreshQueueCount()
                >= 1) {
                processMonitorDtos.add(professionalUserService
                    .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue());
            }
        } catch (ServiceException ex) {
            String message = String.format("Error occurred while processing organisation: %s",
                ex.getMessage());
            log.error(message, ex);
            ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(professionalUserService.PROCESS4_NAME);
            processMonitorDto.addProcessStep(message);
            processMonitorDto.markAsFailed(ex.getMessage());
            processMonitorDtos.add(processMonitorDto);
        }
        // Make sure the process runs at least once even if the queue is empty
        if (processMonitorDtos.isEmpty()) {
            processMonitorDtos.add(professionalUserService
                .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue());
        }
        return processMonitorDtos;
    }

}
