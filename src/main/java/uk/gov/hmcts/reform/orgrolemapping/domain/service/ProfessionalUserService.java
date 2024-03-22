package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
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
        this.professionalRefreshOrchestrationHelper = professionalRefreshOrchestrationHelper;
        this.tolerance = tolerance;
        this.activeUserRefreshDays = activeUserRefreshDays;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.userRetryOneIntervalMin = userRetryOneIntervalMin;
        this.userRetryTwoIntervalMin = userRetryTwoIntervalMin;
        this.userRetryThreeIntervalMin = userRetryThreeIntervalMin;
    }

    public boolean refreshUsers(ProcessMonitorDto processMonitorDto) throws ServiceException {

        UserRefreshQueueEntity userRefreshQueueEntity
                = userRefreshQueueRepository.retrieveSingleActiveRecord();

        if (userRefreshQueueEntity == null) {
            processMonitorDto.addProcessStep("No entities to process");
            log.info("{} - No entities to process", processMonitorDto.getProcessType());
            return true;
        }

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
                professionalRefreshOrchestrationHelper.refreshSingleUser(userRefreshQueueEntity,
                        accessTypesEntity);
                userRefreshQueueRepository.clearUserRefreshRecord(userId,
                        LocalDateTime.now(), accessTypesEntity.getVersion());
                processMonitorDto.appendToLastProcessStep(" : COMPLETED");


                return true;
            } catch (Exception ex) {
                processMonitorDto.appendToLastProcessStep(" : FAILED");
                String message = String.format("Error occurred while processing user: %s. Retry attempt "
                                + "%d. Rolling back.",
                        userId, userRefreshQueueEntity.getRetry() + 1);
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
        }

        return isSuccess;
    }
}
