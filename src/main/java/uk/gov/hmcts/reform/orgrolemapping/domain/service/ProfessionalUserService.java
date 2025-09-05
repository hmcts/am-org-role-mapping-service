package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUsersResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUserData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersOrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.helper.ProfessionalUserBuilder;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

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

    public static final String PROCESS_4_NAME = "PRM Process 4 - Find Users with Stale Organisations";
    public static final String PROCESS_5_NAME = "PRM Process 5 - Find User Changes";

    private final PrdService prdService;

    private final AccessTypesRepository accessTypesRepository;
    private final BatchLastRunTimestampRepository batchLastRunTimestampRepository;
    private final DatabaseDateTimeRepository databaseDateTimeRepository;
    private final OrganisationRefreshQueueRepository organisationRefreshQueueRepository;
    private final UserRefreshQueueRepository userRefreshQueueRepository;
    private final String pageSize;
    private final String tolerance;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final String retryOneIntervalMin;
    private final String retryTwoIntervalMin;
    private final String retryThreeIntervalMin;

    private final ProcessEventTracker processEventTracker;

    public ProfessionalUserService(
            PrdService prdService,
            AccessTypesRepository accessTypesRepository,
            BatchLastRunTimestampRepository batchLastRunTimestampRepository,
            DatabaseDateTimeRepository databaseDateTimeRepository,
            OrganisationRefreshQueueRepository organisationRefreshQueueRepository,
            UserRefreshQueueRepository userRefreshQueueRepository,
            NamedParameterJdbcTemplate jdbcTemplate,
            PlatformTransactionManager transactionManager,
            @Value("${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.retryOneIntervalMin}")
            String retryOneIntervalMin,
            @Value("${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.retryTwoIntervalMin}")
            String retryTwoIntervalMin,
            @Value("${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.retryThreeIntervalMin}")
            String retryThreeIntervalMin,
            @Value("${professional.refdata.pageSize}")
            String pageSize,
            @Value("${groupAccess.lastRunTimeTolerance}")
            String tolerance,
            ProcessEventTracker processEventTracker) {
        this.prdService = prdService;

        this.accessTypesRepository = accessTypesRepository;
        this.batchLastRunTimestampRepository = batchLastRunTimestampRepository;
        this.databaseDateTimeRepository = databaseDateTimeRepository;
        this.organisationRefreshQueueRepository = organisationRefreshQueueRepository;
        this.userRefreshQueueRepository = userRefreshQueueRepository;
        this.pageSize = pageSize;
        this.tolerance = tolerance;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.retryOneIntervalMin = retryOneIntervalMin;
        this.retryTwoIntervalMin = retryTwoIntervalMin;
        this.retryThreeIntervalMin = retryThreeIntervalMin;
        this.processEventTracker = processEventTracker;
    }

    public ProcessMonitorDto findAndInsertUsersWithStaleOrganisationsIntoRefreshQueueById(String organisationId) {
        log.info("Starting with Id {}", PROCESS_4_NAME);
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(PROCESS_4_NAME);
        processEventTracker.trackEventStarted(processMonitorDto);
        Optional<OrganisationRefreshQueueEntity> organisationRefreshQueueEntity =
            organisationRefreshQueueRepository.findById(organisationId);
        if (organisationRefreshQueueEntity.isPresent()) {
            collateChildProcessMonitorDtos(processMonitorDto,
                findAndInsertUsersWithStaleOrganisationsIntoRefreshQueueByEntity(organisationRefreshQueueEntity.get()));

        } else {
            String message = String.format("Organisation with ID %s not found in the refresh queue", organisationId);
            processMonitorDto.addProcessStep(message);
            processMonitorDto.markAsFailed(message);
        }
        processEventTracker.trackEventCompleted(processMonitorDto);
        return processMonitorDto;
    }

    private void collateChildProcessMonitorDtos(
        ProcessMonitorDto mainProcessMonitorDto,
        ProcessMonitorDto childProcessMonitorDto) {
        childProcessMonitorDto.getProcessSteps().forEach(mainProcessMonitorDto::addProcessStep);
        // Only update the main process monitor if it has not already failed.
        if (!EndStatus.FAILED.equals(mainProcessMonitorDto.getEndStatus())) {
            // If the child process is successful, mark the main process as successful.
            if (EndStatus.SUCCESS.equals(childProcessMonitorDto.getEndStatus())) {
                mainProcessMonitorDto.markAsSuccess();
            } else {
                mainProcessMonitorDto.markAsFailed(childProcessMonitorDto.getEndDetail());
            }
        }
    }

    public ProcessMonitorDto findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue() {
        log.info("Starting {}", PROCESS_4_NAME);
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(PROCESS_4_NAME);
        processEventTracker.trackEventStarted(processMonitorDto);
        try {
            boolean anyEntitiesInQueue = true;
            while (anyEntitiesInQueue) {
                OrganisationRefreshQueueEntity organisationRefreshQueueEntity = organisationRefreshQueueRepository
                    .findAndLockSingleActiveOrganisationRecord();
                collateChildProcessMonitorDtos(processMonitorDto,
                    findAndInsertUsersWithStaleOrganisationsIntoRefreshQueueByEntity(organisationRefreshQueueEntity));
                anyEntitiesInQueue = organisationRefreshQueueEntity != null;
            }
        } catch (ServiceException ex) {
            String message = String.format("Error occurred while processing organisation: %s",
                ex.getMessage());
            log.error(message, ex);
            processMonitorDto.addProcessStep(message);
            processMonitorDto.markAsFailed(ex.getMessage());
            processEventTracker.trackEventCompleted(processMonitorDto);
            throw ex;
        }
        processEventTracker.trackEventCompleted(processMonitorDto);
        return processMonitorDto;
    }

    private ProcessMonitorDto findAndInsertUsersWithStaleOrganisationsIntoRefreshQueueByEntity(
        OrganisationRefreshQueueEntity organisationRefreshQueueEntity
    ) {

        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(PROCESS_4_NAME);

        try {
            if (organisationRefreshQueueEntity == null) {
                processMonitorDto.addProcessStep("No entities to process");
                processMonitorDto.markAsSuccess();
                log.info("Completed {}. No entities to process", PROCESS_4_NAME);
                return processMonitorDto;
            }

            Integer accessTypesMinVersion = organisationRefreshQueueEntity.getAccessTypesMinVersion();
            String organisationIdentifier = organisationRefreshQueueEntity.getOrganisationId();

            UsersByOrganisationRequest request = new UsersByOrganisationRequest(
                    List.of(organisationIdentifier)
            );

            boolean isSuccess = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
                try {
                    retrieveUsersByOrganisationAndUpsert(request, accessTypesMinVersion);

                    organisationRefreshQueueRepository.clearOrganisationRefreshRecord(
                            organisationIdentifier,
                            accessTypesMinVersion,
                            organisationRefreshQueueEntity.getLastUpdated()
                    );
                    processMonitorDto.markAsSuccess();

                    return true;
                } catch (Exception ex) {
                    String message = String.format("Error occurred while processing organisation: %s. Retry attempt "
                                    + "%d. Rolling back.",
                            organisationIdentifier, organisationRefreshQueueEntity.getRetry());
                    processMonitorDto.addProcessStep(message);
                    processMonitorDto.markAsFailed(ex.getMessage());
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

        log.info("Completed {}", PROCESS_4_NAME);
        return processMonitorDto;
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
        log.info("Starting {}", PROCESS_5_NAME);
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(PROCESS_5_NAME);
        processEventTracker.trackEventStarted(processMonitorDto);

        String lastRecordInPage = null; // declare here for use in catch block

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

            // prep for first call to retrieveUsers
            boolean foundUsers = false;
            boolean moreAvailable = true;
            String processStep = "attempting first retrieveUsers";

            while (moreAvailable) {
                processMonitorDto.addProcessStep(processStep);
                GetRefreshUsersResponse getRefreshUsersResponse = prdService
                        .retrieveUsers(formattedSince, Integer.valueOf(pageSize), lastRecordInPage).getBody();

                if (getRefreshUsersResponse != null && CollectionUtils.isNotEmpty(getRefreshUsersResponse.getUsers())) {
                    foundUsers = true;
                    writeAllToUserRefreshQueue(getRefreshUsersResponse, accessTypeMinVersion, processMonitorDto);
                } else {
                    break;
                }

                // prep for next call to retrieveUsers
                moreAvailable = getRefreshUsersResponse.isMoreAvailable();
                lastRecordInPage = getRefreshUsersResponse.getLastRecordInPage();
                processStep = "attempting retrieveUsers from lastRecordInPage=" + lastRecordInPage;
            }

            // if process complete and users were found, update the batch last run timestamp
            if (foundUsers) {
                batchLastRunTimestampEntity.setLastUserRunDatetime(LocalDateTime.ofInstant(batchRunStartTime.getDate(),
                        ZoneId.systemDefault()));
                batchLastRunTimestampRepository.save(batchLastRunTimestampEntity);
            } else {
                processMonitorDto.appendToLastProcessStep(" : No users found to process.");
            }
        } catch (Exception exception) {
            processMonitorDto.markAsFailed(exception.getMessage()
                    + (lastRecordInPage == null ? "" : ", failed at lastRecordInPage=" + lastRecordInPage));
            processEventTracker.trackEventCompleted(processMonitorDto);
            throw exception;
        }

        processMonitorDto.markAsSuccess();
        processEventTracker.trackEventCompleted(processMonitorDto);

        log.info("Completed {}", PROCESS_5_NAME);
        return processMonitorDto;
    }

    private void writeAllToUserRefreshQueue(GetRefreshUsersResponse getRefreshUsersResponse,
                                            Integer accessTypeMinVersion,
                                            ProcessMonitorDto processMonitorDto) {
        String processStep = "attempting writeAllToUserRefreshQueue for ";
        processMonitorDto.addProcessStep(processStep);

        List<ProfessionalUserData> professionalUserData = new ArrayList<>();
        for (RefreshUser user : getRefreshUsersResponse.getUsers()) {
            try {
                processMonitorDto.appendToLastProcessStep("user=" + user.getUserIdentifier() + ",");
                professionalUserData.add(ProfessionalUserBuilder.fromProfessionalRefreshUser(user));
            } catch (Exception e) {
                String errorMessage = "Error serializing user: " + user.getUserIdentifier();
                log.error(errorMessage, e);
                processMonitorDto.addProcessStep(errorMessage);
                throw new ServiceException(errorMessage, e);
            }
        }

        userRefreshQueueRepository
            .upsertToUserRefreshQueueForLastUpdated(jdbcTemplate, professionalUserData, accessTypeMinVersion);
        processMonitorDto.appendToLastProcessStep(" : COMPLETED");
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

}
