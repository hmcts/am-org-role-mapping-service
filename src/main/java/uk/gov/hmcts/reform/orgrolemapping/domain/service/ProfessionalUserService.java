package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProfessionalUserService {


    private final PrdService prdService;
    private final UserRefreshQueueRepository userRefreshQueueRepository;
    private final AccessTypesRepository accessTypesRepository;
    private final BatchLastRunTimestampRepository batchLastRunTimestampRepository;
    private final DatabaseDateTimeRepository databaseDateTimeRepository;
    private final String pageSize;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ProcessEventTracker processEventTracker;
    private final ProfessionalRefreshOrchestrationHelper professionalRefreshOrchestrationHelper;

    private String tolerance;
    private String activeUserRefreshDays;

    public ProfessionalUserService(PrdService prdService,
                                   UserRefreshQueueRepository userRefreshQueueRepository,
                                   @Value("${professional.refdata.pageSize}") String pageSize,
                                   NamedParameterJdbcTemplate jdbcTemplate,
                                   AccessTypesRepository accessTypesRepository,
                                   BatchLastRunTimestampRepository batchLastRunTimestampRepository,
                                   DatabaseDateTimeRepository databaseDateTimeRepository,
                                   ProcessEventTracker processEventTracker,
                                   ProfessionalRefreshOrchestrationHelper professionalRefreshOrchestrationHelper,
                                   @Value("${groupAccess.lastRunTimeTolerance}") String tolerance,
                                   @Value("${professional.role.mapping.scheduling.userRefreshCleanup.activeUserRefreshDays}") String activeUserRefreshDays) {
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

    @Transactional
    public UserRefreshQueueEntity refreshAndClearUserRecord(UserRefreshQueueEntity userRefreshQueueEntity,
                                                            AccessTypesEntity accessTypesEntity,
                                                            ProcessMonitorDto processMonitorDto) {
        processMonitorDto.addProcessStep("attempting refreshAndClearUserRecord for userId="
                + userRefreshQueueEntity.getUserId());
        professionalRefreshOrchestrationHelper.refreshSingleUser(userRefreshQueueEntity, accessTypesEntity);
        processMonitorDto.appendToLastProcessStep(" : COMPLETED");

        processMonitorDto.addProcessStep("attempting clearUserRefreshRecord for userId="
                + userRefreshQueueEntity.getUserId());
        userRefreshQueueRepository.clearUserRefreshRecord(userRefreshQueueEntity.getUserId(),
                LocalDateTime.now(), accessTypesEntity.getVersion());
        processMonitorDto.appendToLastProcessStep(" : COMPLETED");

        processMonitorDto.addProcessStep("attempting next retrieveSingleActiveRecord");
        UserRefreshQueueEntity userRefreshQueue =  userRefreshQueueRepository.retrieveSingleActiveRecord();
        String completionPrefix = (userRefreshQueue==null ? " - none": " - one") + " found";
        processMonitorDto.appendToLastProcessStep(completionPrefix + " : COMPLETED");
        return userRefreshQueue;
    }
}
