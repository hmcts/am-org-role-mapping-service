package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.irm.IdamRecordType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.AccountStatus;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamUser;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.IdamFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;
import uk.gov.hmcts.reform.orgrolemapping.util.irm.IdamRoleDataJsonBConverter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@AllArgsConstructor
@Slf4j
public class IdamRoleMappingService {

    protected static final String NO_ENTITIES = "No entities to process";
    protected static final String QUEUE_NAME = "IRM Process %s Queue";
    protected static final String UPDATEUSER_NAME = "IRM Update User";

    private final IdamFeignClient idamClient;
    private final IdamRoleManagementQueueRepository idamRoleManagementQueueRepository;
    private final TransactionTemplate transactionTemplate;
    private final IdamRoleDataJsonBConverter idamRoleDataJsonBConverter;
    private final ProcessEventTracker processEventTracker;
    private final String retryOneIntervalMin;
    private final String retryTwoIntervalMin;
    private final String retryThreeIntervalMin;

    @Autowired
    public IdamRoleMappingService(
            IdamFeignClient idamClient,
            IdamRoleManagementQueueRepository idamRoleManagementQueueRepository,
            PlatformTransactionManager transactionManager,
            ProcessEventTracker processEventTracker,
            @Value("${idam.role.management.scheduling.retryOneIntervalMin}")
            String retryOneIntervalMin,
            @Value("${idam.role.management.scheduling.retryOneIntervalMin}")
            String retryTwoIntervalMin,
            @Value("${idam.role.management.scheduling.retryOneIntervalMin}")
            String retryThreeIntervalMin) {
        this.idamClient = idamClient;
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
            idamRoleManagementQueueRepository.upsert(userId, userType.name(),
                    idamRoleDataJsonBConverter.convertToDatabaseColumn(idamRoleData),
                    LocalDateTime.now());
        });
    }

    public ProcessMonitorDto processJudicialQueue() {
        return processQueue(UserType.JUDICIAL);
    }

    public ProcessMonitorDto processCaseWorkerQueue() {
        return processQueue(UserType.CASEWORKER);
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
            processMonitorDto.addProcessStep(queueName);
            boolean anyEntitiesInQueue = true;
            while (anyEntitiesInQueue) {
                // Get the next record to process and lock it.
                IdamRoleManagementQueueEntity idamRoleManagementQueueEntity
                        = idamRoleManagementQueueRepository.findAndLockSingleActiveRecord(userType.name());
                if (idamRoleManagementQueueEntity != null) {
                    errorMessage = processQueueEntry(idamRoleManagementQueueEntity, IdamRecordType.USER);
                    if (errorMessage.isEmpty()) {
                        successfulJobCount++;
                    } else {
                        failedJobCount++;
                        errorMessageBuilder.append(errorMessage);
                    }
                }
                // If there is another record to process then continue, otherwise exit the loop.
                anyEntitiesInQueue = idamRoleManagementQueueEntity != null;
            }
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

    @Transactional
    private String processQueueEntry(IdamRoleManagementQueueEntity idamRoleManagementQueueEntity,
                                     IdamRecordType idamRecordType) {
        StringBuilder errorMessageBuilder = new StringBuilder();
        boolean isSuccess = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                // Set the record as published.
                idamRoleManagementQueueRepository.setAsPublished(
                        idamRoleManagementQueueEntity.getUserId(),
                        idamRecordType.name());
                return true;
            } catch (Exception ex) {
                String message = String.format("Error occurred while processing queue entry: %s. "
                                + "Retry attempt %d. Rolling back.",
                        idamRoleManagementQueueEntity.getUserId(),
                        idamRoleManagementQueueEntity.getRetry());
                errorMessageBuilder.append(ex.getMessage());
                log.error(message, ex);
                status.setRollbackOnly();
                return false;
            }
        }));

        if (!isSuccess) {
            // Failed, so increase the retry count.
            idamRoleManagementQueueRepository.updateRetry(
                    idamRoleManagementQueueEntity.getUserId(),
                    retryOneIntervalMin, retryTwoIntervalMin, retryThreeIntervalMin);
        }
        return errorMessageBuilder.toString();
    }

    @Transactional
    public ProcessMonitorDto updateUser(String userId) {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(UPDATEUSER_NAME);
        processEventTracker.trackEventStarted(processMonitorDto);
        StringBuilder errorMessageBuilder = new StringBuilder();
        boolean isSuccess = false;

        IdamUser user = getIdamUser(userId);
        if  (user == null) {
            log.debug("No user found for userId {}", userId);
        } else {
            try {
                // Get the idam role data
                IdamRoleData idamRoleData = getIdamRoleData(user.getId());
                if (idamRoleData == null) {
                    String message = String.format("No idam role data found for userId %s", userId);
                    errorMessageBuilder.append(message);
                    log.error(message);
                } else {
                    // Patch the user with the idam role data
                    isSuccess = patchIdamUser(user, idamRoleData);
                    if (!isSuccess) {
                        String message = String.format("Failed to update user with userId %s", userId);
                        errorMessageBuilder.append(message);
                        log.error(message);
                    }
                }
            } catch (Exception ex) {
                String message = String.format("Error occurred while updating user with userId %s: %s",
                        userId, ex.getMessage());
                errorMessageBuilder.append(message);
                log.error(message, ex);
            }
        }

        markProcessStatus(processMonitorDto,
                isSuccess ? 1 : 0, isSuccess ? 0 : 1,
                errorMessageBuilder.toString());
        processEventTracker.trackEventCompleted(processMonitorDto);
        return processMonitorDto;
    }

    private IdamRoleData getIdamRoleData(String userId) {
        Optional<IdamRoleManagementQueueEntity> irmQueueEntity =
                idamRoleManagementQueueRepository.findById(userId);
        return irmQueueEntity.isPresent() ? irmQueueEntity.get().getData() : null;
    }

    protected IdamUser getIdamUser(String userId) {
        ResponseEntity<IdamUser> response = idamClient.getUserById(userId);
        return response != null ? response.getBody() : null;
    }

    protected boolean patchIdamUser(IdamUser user, IdamRoleData idamRoleData) {
        AtomicBoolean isPatched = new AtomicBoolean(false);
        // Update the user roles
        if ("Y".equalsIgnoreCase(idamRoleData.getDeletedFlag())) {
            // Delete user role
            idamRoleData.getRoles().forEach(roleData -> {
                if (user.getRoleNames().contains(roleData.getRoleName())) {
                    user.getRoleNames().remove(roleData.getRoleName());
                    isPatched.set(true);
                }
            });
        } else {
            // Add user role
            idamRoleData.getRoles().forEach(roleData -> {
                if (!user.getRoleNames().contains(roleData.getRoleName())) {
                    user.getRoleNames().add(roleData.getRoleName());
                    isPatched.set(true);
                }
            });
        }

        // Update status
        AccountStatus newAccountStatus = getIdamUserAccountStatus(idamRoleData.getActiveFlag());
        if (!newAccountStatus.equals(user.getAccountStatus())) {
            user.setAccountStatus(newAccountStatus);
            isPatched.set(true);
        }

        if  (!isPatched.get()) {
            // No data to patch, so return success without calling idam.
            log.debug("Idam role data unchanged for userId {}", user.getId());
            return true;
        } else {
            // Update the idam user
            log.debug("Idam role data patched for userId {}", user.getId());
            ResponseEntity<IdamUser> response = idamClient.updateUser(user.getId(), user);
            return HttpStatus.OK.equals(response.getStatusCode());
        }
    }

    private static AccountStatus getIdamUserAccountStatus(String activeFlag) {
        return "N".equalsIgnoreCase(activeFlag) ? AccountStatus.SUSPENDED : AccountStatus.ACTIVE;
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
