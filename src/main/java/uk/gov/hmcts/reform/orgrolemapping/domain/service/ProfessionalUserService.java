package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.DatabaseDateTimeRepository;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ProfessionalUserService {


    private final PRDService prdService;
    private final UserRefreshQueueRepository userRefreshQueueRepository;
    private final AccessTypesRepository accessTypesRepository;
    private final BatchLastRunTimestampRepository batchLastRunTimestampRepository;
    private final DatabaseDateTimeRepository databaseDateTimeRepository;
    private final String pageSize;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ProcessEventTracker processEventTracker;
    private final ProfessionalRefreshOrchestrationHelper professionalRefreshOrchestrationHelper;

    private final TransactionTemplate transactionTemplate;
    private final String userRetryOneIntervalMin;
    private final String userRetryTwoIntervalMin;
    private final String userRetryThreeIntervalMin;
    private String tolerance;
    private String activeUserRefreshDays;

    public ProfessionalUserService(PRDService prdService,
            UserRefreshQueueRepository userRefreshQueueRepository,
            @Value("${professional.refdata.pageSize}") String pageSize,
            NamedParameterJdbcTemplate jdbcTemplate,
            AccessTypesRepository accessTypesRepository,
            BatchLastRunTimestampRepository batchLastRunTimestampRepository,
            DatabaseDateTimeRepository databaseDateTimeRepository,
            ProcessEventTracker processEventTracker,
            ProfessionalRefreshOrchestrationHelper professionalRefreshOrchestrationHelper,
            @Value("${groupAccess.lastRunTimeTolerance}") String tolerance,
            @Value("${professional.role.mapping.scheduling.userRefreshCleanup.activeUserRefreshDays}")
                                   String activeUserRefreshDays,
            PlatformTransactionManager transactionManager,
            @Value("${professional.role.mapping.scheduling.userRefresh.retryOneIntervalMin}")
                                   String userRetryOneIntervalMin,
            @Value("${professional.role.mapping.scheduling.userRefresh.retryTwoIntervalMin}")
                                   String userRetryTwoIntervalMin,
            @Value("${professional.role.mapping.scheduling.userRefresh.retryThreeIntervalMin}")
                                   String userRetryThreeIntervalMin) {
        this.prdService = prdService;
        this.userRefreshQueueRepository = userRefreshQueueRepository;
        this.accessTypesRepository = accessTypesRepository;
        this.batchLastRunTimestampRepository = batchLastRunTimestampRepository;
        this.databaseDateTimeRepository = databaseDateTimeRepository;
        this.pageSize = pageSize;
        this.jdbcTemplate = jdbcTemplate;
        this.processEventTracker = processEventTracker;
        this.professionalRefreshOrchestrationHelper = professionalRefreshOrchestrationHelper;
        this.tolerance = tolerance;
        this.activeUserRefreshDays = activeUserRefreshDays;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.userRetryOneIntervalMin = userRetryOneIntervalMin;
        this.userRetryTwoIntervalMin = userRetryTwoIntervalMin;
        this.userRetryThreeIntervalMin = userRetryThreeIntervalMin;
    }

    public void refreshUsers2() {
        String processName = "PRM Process 6 - Refresh users - Batch mode";
        log.info("Starting {}", processName);
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(processName);
        processEventTracker.trackEventStarted(processMonitorDto);

        try {
            UserRefreshQueueEntity userRefreshQueueEntity
                    = userRefreshQueueRepository.retrieveSingleActiveRecord();

            if (userRefreshQueueEntity == null) {
                processMonitorDto.addProcessStep("No entities to process");
                processMonitorDto.markAsSuccess();
                processEventTracker.trackEventCompleted(processMonitorDto);
                log.info("Completed {}. No entities to process", processName);
                return;
            };

            List<AccessTypesEntity> accessTypesEntities = accessTypesRepository.findAll();
            if (accessTypesEntities.size() != 1) {
                throw new ServiceException("Single AccessTypesEntity not found");
            }
            AccessTypesEntity accessTypesEntity = accessTypesEntities.get(0);

            String userId = userRefreshQueueEntity.getUserId();

            boolean isSuccess = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
                try {
                    processMonitorDto.addProcessStep("attempting clearUserRefreshRecord for userId="
                            + userId);
                    userRefreshQueueRepository.clearUserRefreshRecord(userId,
                            LocalDateTime.now(), accessTypesEntity.getVersion());
                    processMonitorDto.appendToLastProcessStep(" : COMPLETED");


                    return true;
                } catch (Exception ex) {
                    String message = String.format("Error occurred while processing user: %s. Retry attempt "
                                    + "%d. Rolling back.",
                            userId, userRefreshQueueEntity.getRetry());
                    processMonitorDto.addProcessStep(message);
                    log.error(message, ex);
                    status.setRollbackOnly();

                    return false;
                }
            }));

            if (!isSuccess) {
                userRefreshQueueRepository.updateRetry(
                        userId, userRetryOneIntervalMin, userRetryTwoIntervalMin, userRetryThreeIntervalMin
                );

                // to avoid another round trip to the database, use the current retry attempt.
                if (userRefreshQueueEntity.getRetry() == 3) {
                    throw new ServiceException("Retry limit reached");
                }
            }

            processMonitorDto.addProcessStep("attempting next retrieveSingleActiveRecord");
            UserRefreshQueueEntity userRefreshQueue =  userRefreshQueueRepository.retrieveSingleActiveRecord();
            String completionPrefix = (userRefreshQueue == null ? " - none" : " - one") + " found";
            processMonitorDto.appendToLastProcessStep(completionPrefix + " : COMPLETED");

        } catch (Exception e) {
            processMonitorDto.markAsFailed(e.getMessage());
            processEventTracker.trackEventCompleted(processMonitorDto);
            throw e;
        }
        processMonitorDto.markAsSuccess();
        processEventTracker.trackEventCompleted(processMonitorDto);

        log.info("Completed {}", processName);
    }


    public void refreshUsers() {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("PRM Process 6 - Refresh users - Batch mode");
        processEventTracker.trackEventStarted(processMonitorDto);
        try {
            List<AccessTypesEntity> accessTypesEntities = accessTypesRepository.findAll();
            if (accessTypesEntities.size() != 1) {
                throw new ServiceException("Single AccessTypesEntity not found");
            }
            AccessTypesEntity accessTypesEntity = accessTypesEntities.get(0);
            refreshUsersForAccessType(accessTypesEntity, processMonitorDto);

            processMonitorDto.markAsSuccess();
            processEventTracker.trackEventCompleted(processMonitorDto);
        } catch (Exception exception) {
            processMonitorDto.markAsFailed(exception.getMessage());
            processEventTracker.trackEventCompleted(processMonitorDto);
            throw exception;
        }
    }


    private void refreshUsersForAccessType(AccessTypesEntity accessTypesEntity, ProcessMonitorDto processMonitorDto) {
        processMonitorDto.addProcessStep("attempting retrieveSingleActiveRecord");
        UserRefreshQueueEntity userRefreshQueueEntity = userRefreshQueueRepository.retrieveSingleActiveRecord();
        processMonitorDto.appendToLastProcessStep(" : COMPLETED");
        while (userRefreshQueueEntity != null) {
            userRefreshQueueEntity = refreshAndClearUserRecord(userRefreshQueueEntity, accessTypesEntity,
                    processMonitorDto);
        }
    }

    public UserRefreshQueueEntity refreshAndClearUserRecord(UserRefreshQueueEntity userRefreshQueueEntity,
                                                            AccessTypesEntity accessTypesEntity,
                                                            ProcessMonitorDto processMonitorDto) {
        String userId = userRefreshQueueEntity.getUserId();
        processMonitorDto.addProcessStep("attempting refreshAndClearUserRecord for userId="
                + userId);
        try {
            professionalRefreshOrchestrationHelper.refreshSingleUser(userRefreshQueueEntity, accessTypesEntity);
            processMonitorDto.appendToLastProcessStep(" : COMPLETED");

            boolean isSuccess = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
                try {
                    processMonitorDto.addProcessStep("attempting clearUserRefreshRecord for userId="
                            + userId);
                    userRefreshQueueRepository.clearUserRefreshRecord(userId,
                            LocalDateTime.now(), accessTypesEntity.getVersion());
                    processMonitorDto.appendToLastProcessStep(" : COMPLETED");


                    return true;
                } catch (Exception ex) {
                    String message = String.format("Error occurred while processing user: %s. Retry attempt "
                                    + "%d. Rolling back.",
                            userId, userRefreshQueueEntity.getRetry());
                    processMonitorDto.addProcessStep(message);
                    log.error(message, ex);
                    status.setRollbackOnly();

                    return false;
                }
            }));

            if (!isSuccess) {
                userRefreshQueueRepository.updateRetry(
                        userId, userRetryOneIntervalMin, userRetryTwoIntervalMin, userRetryThreeIntervalMin
                );

                // to avoid another round trip to the database, use the current retry attempt.
                if (userRefreshQueueEntity.getRetry() == 3) {
                    throw new ServiceException("Retry limit reached");
                }
            }

            processMonitorDto.addProcessStep("attempting next retrieveSingleActiveRecord");
            UserRefreshQueueEntity userRefreshQueue =  userRefreshQueueRepository.retrieveSingleActiveRecord();
            String completionPrefix = (userRefreshQueue == null ? " - none" : " - one") + " found";
            processMonitorDto.appendToLastProcessStep(completionPrefix + " : COMPLETED");
            return userRefreshQueue;

        } catch (Exception ex) {
            userRefreshQueueRepository.updateRetry(userId, userRetryOneIntervalMin,
                    userRetryTwoIntervalMin, userRetryThreeIntervalMin);
            throw ex;
        }
    }
}
