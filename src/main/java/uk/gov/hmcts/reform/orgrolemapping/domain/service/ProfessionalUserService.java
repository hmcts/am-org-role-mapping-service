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
import java.time.ZoneOffset;
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
            LocalDateTime orgLastBatchRunTime = batchLastRunTimestampEntity.getLastOrganisationRunDatetime();

            int toleranceSeconds = Integer.parseInt(tolerance);
            LocalDateTime sinceTime = orgLastBatchRunTime.minusSeconds(toleranceSeconds);
            String formattedSince = ISO_DATE_TIME_FORMATTER.format(sinceTime);

            Integer accessTypeMinVersion = accessTypesEntity.getVersion().intValue();

            GetRefreshUserResponse refreshUserResponse = prdService
                    .retrieveUsers(formattedSince, Integer.valueOf(pageSize), null).getBody();
            writeAllToUserRefreshQueue(refreshUserResponse, accessTypeMinVersion);

            boolean moreAvailable;
            String lastRecordInPage;

            if (!refreshUserResponse.getUsers().isEmpty()) {
                moreAvailable = refreshUserResponse.isMoreAvailable();
                lastRecordInPage = refreshUserResponse.getLastRecordInPage();

                while (moreAvailable) {
                    refreshUserResponse = prdService
                            .retrieveUsers(formattedSince, Integer.valueOf(pageSize), lastRecordInPage).getBody();

                    if (!refreshUserResponse.getUsers().isEmpty()) {
                        moreAvailable = refreshUserResponse.isMoreAvailable();
                        lastRecordInPage = refreshUserResponse.getLastRecordInPage();

                        writeAllToUserRefreshQueue(refreshUserResponse, accessTypeMinVersion);
                    } else {
                        break;
                    }

                }
                batchLastRunTimestampEntity.setLastUserRunDatetime(LocalDateTime.ofInstant(batchRunStartTime.getDate(),
                        ZoneOffset.systemDefault()));
                batchLastRunTimestampRepository.save(batchLastRunTimestampEntity);
                log.info("..findUserChangesAndInsertIntoUserRefreshQueue finished");
            }
        } catch (ServiceException serviceException) {
            processMonitorDto.markAsFailed(serviceException.getMessage());
        } finally {
            processEventTracker.trackEventCompleted(processMonitorDto);
        }
    }

    private void writeAllToUserRefreshQueue(GetRefreshUserResponse usersResponse, Integer accessTypeMinVersion) {
        List<RefreshUserAndOrganisation> serializedUsers = new ArrayList<>();

        for (RefreshUser user : usersResponse.getUsers()) {
            serializedUsers.add(ProfessionalUserBuilder.getSerializedRefreshUser(user));
        }

        userRefreshQueueRepository.insertIntoUserRefreshQueueForLastUpdated(
                jdbcTemplate, serializedUsers, accessTypeMinVersion);
    }
}


