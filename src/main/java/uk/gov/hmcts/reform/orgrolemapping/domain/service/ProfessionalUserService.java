package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.DatabaseDateTime;
import uk.gov.hmcts.reform.orgrolemapping.data.DatabaseDateTimeRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUserResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUserData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUserAndOrganisation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersOrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.helper.ProfessionalUserBuilder;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.ISO_DATE_TIME_FORMATTER;
import static uk.gov.hmcts.reform.orgrolemapping.helper.ProfessionalUserBuilder.fromProfessionalUserAndOrganisationInfo;

@Service
@Slf4j
public class ProfessionalUserService {

    private final PrdService prdService;

    private final AccessTypesRepository accessTypesRepository;
    private final BatchLastRunTimestampRepository batchLastRunTimestampRepository;
    private final DatabaseDateTimeRepository databaseDateTimeRepository;
    private final OrganisationRefreshQueueRepository organisationRefreshQueueRepository;
    private final UserRefreshQueueRepository userRefreshQueueRepository;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final ProcessEventTracker processEventTracker;

    private final String retryOneIntervalMin;
    private final String retryTwoIntervalMin;
    private final String retryThreeIntervalMin;
    private final String pageSize;
    private final String tolerance;

    public ProfessionalUserService(
            PrdService prdService,
            AccessTypesRepository accessTypesRepository,
            BatchLastRunTimestampRepository batchLastRunTimestampRepository,
            DatabaseDateTimeRepository databaseDateTimeRepository,
            OrganisationRefreshQueueRepository organisationRefreshQueueRepository,
            UserRefreshQueueRepository userRefreshQueueRepository,
            NamedParameterJdbcTemplate jdbcTemplate,
            PlatformTransactionManager transactionManager,
            ProcessEventTracker processEventTracker,
            @Value("${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.retryOneIntervalMin}")
            String retryOneIntervalMin,
            @Value("${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.retryTwoIntervalMin}")
            String retryTwoIntervalMin,
            @Value("${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.retryThreeIntervalMin}")
            String retryThreeIntervalMin,
            @Value("${professional.refdata.pageSize}")
            String pageSize,
            @Value("${groupAccess.lastRunTimeTolerance}")
            String tolerance) {
        this.prdService = prdService;

        this.accessTypesRepository = accessTypesRepository;
        this.batchLastRunTimestampRepository = batchLastRunTimestampRepository;
        this.databaseDateTimeRepository = databaseDateTimeRepository;
        this.organisationRefreshQueueRepository = organisationRefreshQueueRepository;
        this.userRefreshQueueRepository = userRefreshQueueRepository;

        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.processEventTracker = processEventTracker;

        this.retryOneIntervalMin = retryOneIntervalMin;
        this.retryTwoIntervalMin = retryTwoIntervalMin;
        this.retryThreeIntervalMin = retryThreeIntervalMin;
        this.pageSize = pageSize;
        this.tolerance = tolerance;
    }


    public void findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue() {
        String processName = "PRM Process 4 - Find Users with Stale Organisations";
        log.info("Starting {}", processName);
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(processName);
        processEventTracker.trackEventStarted(processMonitorDto);

        try {
            OrganisationRefreshQueueEntity organisationRefreshQueueEntity
                    = organisationRefreshQueueRepository.findAndLockSingleActiveOrganisationRecord();

            if (organisationRefreshQueueEntity == null) {
                processMonitorDto.addProcessStep("No entities to process");
                processMonitorDto.markAsSuccess();
                processEventTracker.trackEventCompleted(processMonitorDto);
                log.info("Completed {}. No entities to process", processName);
                return;
            }

            Integer accessTypesMinVersion = organisationRefreshQueueEntity.getAccessTypesMinVersion();
            String organisationIdentifier = organisationRefreshQueueEntity.getOrganisationId();

            UsersByOrganisationRequest request = new UsersByOrganisationRequest(
                    List.of(organisationIdentifier)
            );

            boolean isSuccess = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
                try {
                    retrieveUsersByOrganisationAndUpsert(request, accessTypesMinVersion);

                    organisationRefreshQueueRepository.setActiveFalse(
                            organisationIdentifier,
                            accessTypesMinVersion,
                            organisationRefreshQueueEntity.getLastUpdated()
                    );

                    return true;
                } catch (Exception ex) {
                    String message = String.format("Error occurred while processing organisation: %s. Retry attempt "
                                    + "%d. Rolling back.",
                            organisationIdentifier, organisationRefreshQueueEntity.getRetry());
                    processMonitorDto.addProcessStep(message);
                    log.error(message, ex);
                    status.setRollbackOnly();
                    return false;
                }
            }));

            if (!isSuccess) {
                organisationRefreshQueueRepository.updateRetry(
                        organisationIdentifier, retryOneIntervalMin, retryTwoIntervalMin, retryThreeIntervalMin
                );

                // to avoid another round trip to the database, use the current retry attempt.
                if (organisationRefreshQueueEntity.getRetry() == 3) {
                    throw new ServiceException("Retry limit reached");
                }
            }
        } catch (Exception e) {
            processMonitorDto.markAsFailed(e.getMessage());
            processEventTracker.trackEventCompleted(processMonitorDto);
            throw e;
        }
        processMonitorDto.markAsSuccess();
        processEventTracker.trackEventCompleted(processMonitorDto);

        log.info("Completed {}", processName);
    }

    private void retrieveUsersByOrganisationAndUpsert(UsersByOrganisationRequest request,
                                                     Integer accessTypesMinVersion) {
        UsersByOrganisationResponse response;
        response = Objects.requireNonNull(
                prdService.fetchUsersByOrganisation(Integer.valueOf(pageSize), null, null, request).getBody()
        );

        boolean moreAvailable;
        String searchAfterOrg;
        String searchAfterUser;

        if (!response.getOrganisationInfo().isEmpty()) {
            moreAvailable = response.getMoreAvailable();
            searchAfterOrg = response.getLastOrgInPage();
            searchAfterUser = response.getLastUserInPage();

            writeAllToUserRefreshQueue(response, accessTypesMinVersion);

            while (moreAvailable) {
                response = Objects.requireNonNull(prdService.fetchUsersByOrganisation(
                        Integer.valueOf(pageSize), searchAfterOrg, searchAfterUser, request).getBody());

                if (!response.getOrganisationInfo().isEmpty()) {
                    moreAvailable = response.getMoreAvailable();
                    searchAfterOrg = response.getLastOrgInPage();
                    searchAfterUser = response.getLastUserInPage();

                    writeAllToUserRefreshQueue(response, accessTypesMinVersion);
                } else {
                    break;
                }
            }
        }
    }

    @Transactional
    public ProcessMonitorDto findUserChangesAndInsertIntoUserRefreshQueue() {
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
        return processMonitorDto;
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

    private void writeAllToUserRefreshQueue(UsersByOrganisationResponse response,
                                            Integer accessTypesMinVersion) {
        List<ProfessionalUserData> professionalUserData = getProfessionalUserData(response);

        if (!professionalUserData.isEmpty()) {
            userRefreshQueueRepository
                    .upsertToUserRefreshQueue(jdbcTemplate, professionalUserData, accessTypesMinVersion);
        }
    }

    private List<ProfessionalUserData> getProfessionalUserData(UsersByOrganisationResponse response) {
        List<ProfessionalUserData> professionalUserData = new ArrayList<>();

        for (UsersOrganisationInfo organisationInfo : response.getOrganisationInfo()) {
            List<ProfessionalUserData> professionalUsers = organisationInfo.getUsers().stream()
                    .map(user -> fromProfessionalUserAndOrganisationInfo(user, organisationInfo))
                    .toList();

            professionalUserData.addAll(professionalUsers);
        }

        return professionalUserData;
    }

    private void appendLastProcessStep(ProcessMonitorDto processMonitorDto, String message) {
        String last = processMonitorDto.getProcessSteps().get(processMonitorDto.getProcessSteps().size() - 1);
        processMonitorDto.getProcessSteps().remove(processMonitorDto.getProcessSteps().size() - 1);
        last = last + message;
        processMonitorDto.getProcessSteps().add(last);
    }

}
