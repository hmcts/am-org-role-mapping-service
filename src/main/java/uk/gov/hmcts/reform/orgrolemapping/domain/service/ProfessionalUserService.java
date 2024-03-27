package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.DatabaseDateTime;
import uk.gov.hmcts.reform.orgrolemapping.data.DatabaseDateTimeRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUserResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUserAndOrganisation;
import uk.gov.hmcts.reform.orgrolemapping.helper.ProfessionalUserBuilder;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.ISO_DATE_TIME_FORMATTER;

@Slf4j
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

    private String tolerance;

    public ProfessionalUserService(PrdService prdService,
                                   UserRefreshQueueRepository userRefreshQueueRepository,
                                   @Value("${professional.refdata.pageSize}") String pageSize,
                                   NamedParameterJdbcTemplate jdbcTemplate,
                                   AccessTypesRepository accessTypesRepository,
                                   BatchLastRunTimestampRepository batchLastRunTimestampRepository,
                                   DatabaseDateTimeRepository databaseDateTimeRepository,
                                   ProcessEventTracker processEventTracker,
                                   @Value("${groupAccess.lastRunTimeTolerance}") String tolerance
    ) {
        this.prdService = prdService;
        this.userRefreshQueueRepository = userRefreshQueueRepository;
        this.accessTypesRepository = accessTypesRepository;
        this.batchLastRunTimestampRepository = batchLastRunTimestampRepository;
        this.databaseDateTimeRepository = databaseDateTimeRepository;
        this.pageSize = pageSize;
        this.jdbcTemplate = jdbcTemplate;
        this.processEventTracker = processEventTracker;
        this.tolerance = tolerance;
    }

    @Transactional
    public void findUserChangesAndInsertIntoUserRefreshQueue() {
        log.info("findUserChangesAndInsertIntoUserRefreshQueue started..");
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("PRM Process 5 - Find User Changes");
        processEventTracker.trackEventStarted(processMonitorDto);

        String lastRecordInPage = null;
        try {
            final DatabaseDateTime batchRunStartTime = databaseDateTimeRepository.getCurrentTimeStamp();
            List<AccessTypesEntity> allAccessTypes = accessTypesRepository.findAll();
            AccessTypesEntity accessTypesEntity = allAccessTypes.get(0);
            if (allAccessTypes.size() != 1) {
                throw new ServiceException("Single AccessTypesEntity not found");
            }
            List<BatchLastRunTimestampEntity> allBatchLastRunTimestampEntities = batchLastRunTimestampRepository
                    .findAll();
            if (allBatchLastRunTimestampEntities.size() != 1) {
                throw new ServiceException("Single BatchLastRunTimestampEntity not found");
            }
            BatchLastRunTimestampEntity batchLastRunTimestampEntity = allBatchLastRunTimestampEntities.get(0);
            LocalDateTime orgLastBatchRunTime = batchLastRunTimestampEntity.getLastUserRunDatetime();

            int toleranceSeconds = Integer.parseInt(tolerance);
            LocalDateTime sinceTime = orgLastBatchRunTime.minusSeconds(toleranceSeconds);
            String formattedSince = ISO_DATE_TIME_FORMATTER.format(sinceTime);

            Integer accessTypeMinVersion = accessTypesEntity.getVersion().intValue();

            String processStep = "attempting first retrieveUsers";
            processMonitorDto.addProcessStep(processStep);
            GetRefreshUserResponse refreshUserResponse = prdService
                    .retrieveUsers(formattedSince, Integer.valueOf(pageSize), null).getBody();
            writeAllToUserRefreshQueue(refreshUserResponse, accessTypeMinVersion, processMonitorDto);

            boolean moreAvailable;

            if (!refreshUserResponse.getUsers().isEmpty()) {
                moreAvailable = refreshUserResponse.isMoreAvailable();
                lastRecordInPage = refreshUserResponse.getLastRecordInPage();

                while (moreAvailable) {
                    processStep = "attempting retrieveUsers from lastRecordInPage=" + lastRecordInPage;
                    processMonitorDto.addProcessStep(processStep);
                    refreshUserResponse = prdService
                            .retrieveUsers(formattedSince, Integer.valueOf(pageSize), lastRecordInPage).getBody();

                    if (!refreshUserResponse.getUsers().isEmpty()) {
                        moreAvailable = refreshUserResponse.isMoreAvailable();
                        lastRecordInPage = refreshUserResponse.getLastRecordInPage();

                        writeAllToUserRefreshQueue(refreshUserResponse, accessTypeMinVersion, processMonitorDto);
                    } else {
                        break;
                    }

                }
                batchLastRunTimestampEntity.setLastUserRunDatetime(LocalDateTime.ofInstant(batchRunStartTime.getDate(),
                        ZoneId.systemDefault()));
                batchLastRunTimestampRepository.save(batchLastRunTimestampEntity);
            }
        } catch (Exception exception) {
            processMonitorDto.markAsFailed(exception.getMessage()
                    + (lastRecordInPage == null ? "" : ", failed at lastRecordInPage=" + lastRecordInPage));
            processEventTracker.trackEventCompleted(processMonitorDto);
            throw exception;
        }
        processMonitorDto.markAsSuccess();
        processEventTracker.trackEventCompleted(processMonitorDto);
        log.info("..findUserChangesAndInsertIntoUserRefreshQueue finished");
    }

    private void writeAllToUserRefreshQueue(GetRefreshUserResponse usersResponse, Integer accessTypeMinVersion,
                                            ProcessMonitorDto processMonitorDto) {
        String processStep = "attempting writeAllToUserRefreshQueue for ";
        processMonitorDto.addProcessStep(processStep);

        List<RefreshUserAndOrganisation> serializedUsers = new ArrayList<>();
        for (RefreshUser user : usersResponse.getUsers()) {
            appendLastProcessStep(processMonitorDto, "user=" + user.getUserIdentifier() + ",");
            serializedUsers.add(ProfessionalUserBuilder.getSerializedRefreshUser(user));
        }

        userRefreshQueueRepository.insertIntoUserRefreshQueueForLastUpdated(
                jdbcTemplate, serializedUsers, accessTypeMinVersion);
        appendLastProcessStep(processMonitorDto, " : COMPLETED");
    }

    private void appendLastProcessStep(ProcessMonitorDto processMonitorDto, String message) {
        String last = processMonitorDto.getProcessSteps().get(processMonitorDto.getProcessSteps().size() - 1);
        processMonitorDto.getProcessSteps().remove(processMonitorDto.getProcessSteps().size() - 1);
        last = last + message;
        processMonitorDto.getProcessSteps().add(last);
    }
}


