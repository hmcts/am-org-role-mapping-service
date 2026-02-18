package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleData;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;
import uk.gov.hmcts.reform.orgrolemapping.util.irm.IdamRoleDataJsonBConverter;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class IdamRoleMappingService {

    private static final String NO_ENTITIES = "No entities to process";
    private static final String QUEUE_NAME = "IRM Process {} Queue";

    private final IdamRoleManagementQueueRepository idamRoleManagementQueueRepository;
    private final TransactionTemplate transactionTemplate;
    private final IdamRoleDataJsonBConverter idamRoleDataJsonBConverter;
    private final ProcessEventTracker processEventTracker;
    private final String retryOneIntervalMin;
    private final String retryTwoIntervalMin;
    private final String retryThreeIntervalMin;

    @Autowired
    public IdamRoleMappingService(
            IdamRoleManagementQueueRepository idamRoleManagementQueueRepository,
            PlatformTransactionManager transactionManager,
            ProcessEventTracker processEventTracker,
            @Value("${idam.role.management.scheduling.retryOneIntervalMin}")
            String retryOneIntervalMin,
            @Value("${idam.role.management.scheduling.retryOneIntervalMin}")
            String retryTwoIntervalMin,
            @Value("${idam.role.management.scheduling.retryOneIntervalMin}")
            String retryThreeIntervalMin) {
        this.idamRoleManagementQueueRepository = idamRoleManagementQueueRepository;
        this.idamRoleDataJsonBConverter = new IdamRoleDataJsonBConverter();
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.processEventTracker = processEventTracker;
        this.retryOneIntervalMin = retryOneIntervalMin;
        this.retryTwoIntervalMin = retryTwoIntervalMin;
        this.retryThreeIntervalMin = retryThreeIntervalMin;
    }

    @Transactional
    public void addToQueue(UserType userType, Map<String, IdamRoleData> idamRoleList) {
        log.info("Adding users to idam role mapping queue, total users: {}", idamRoleList.size());
        idamRoleList.forEach((userId, idamRoleData) -> {
            idamRoleManagementQueueRepository.upsert(userId, userType.name(), "user",
                    idamRoleDataJsonBConverter.convertToDatabaseColumn(idamRoleData),
                    LocalDateTime.now());
        });
    }

    public ProcessMonitorDto processJudicialQueue() {
        return processQueue(UserType.JUDICIAL);
    }

    private ProcessMonitorDto processQueue(UserType userType) {
        String queueName = String.format(QUEUE_NAME, userType.name());
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(queueName);
        processEventTracker.trackEventStarted(processMonitorDto);
        StringBuilder errorMessageBuilder = new StringBuilder();
        int successfulJobCount = 0;
        int failedJobCount = 0;
        String errorMessage;
        try {
            // Get the next record to process and lock it.
            IdamRoleManagementQueueEntity idamRoleManagementQueueEntity
                    = idamRoleManagementQueueRepository.findAndLockSingleActiveRecord(userType.name());
            // If nothing was processed then record that fact in the process steps.
            if (successfulJobCount == 0 && failedJobCount == 0) {
                processMonitorDto.addProcessStep(NO_ENTITIES);
            }
        } catch (ServiceException ex) {
            String message = String.format("Error occurred while processing idam role mapping: %s",
                    ex.getMessage());
            log.error(message, ex);
            processMonitorDto.addProcessStep(message);
            processMonitorDto.markAsFailed(ex.getMessage());
            processEventTracker.trackEventCompleted(processMonitorDto);
            throw ex;
        }
        markProcessStatus(processMonitorDto,
                successfulJobCount, failedJobCount,
                errorMessageBuilder.toString());
        processEventTracker.trackEventCompleted(processMonitorDto);
        return processMonitorDto;
    }

    private void markProcessStatus(ProcessMonitorDto processMonitorDto, int successfulJobCount,
                                     int failedJobCount, String errorMessage) {
        boolean hasSuccessfulStep = successfulJobCount > 0 || (successfulJobCount == 0 && failedJobCount == 0);
        boolean hasFailedAStep = failedJobCount > 0;
        if (!hasSuccessfulStep && hasFailedAStep) {
            processMonitorDto.markAsFailed(errorMessage);
        }

        if (hasSuccessfulStep && hasFailedAStep) {
            processMonitorDto.markAsPartialSuccess(errorMessage);
        }

        if (hasSuccessfulStep && !hasFailedAStep) {
            processMonitorDto.markAsSuccess();
        }
    }
}
