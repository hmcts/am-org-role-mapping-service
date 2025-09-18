package uk.gov.hmcts.reform.orgrolemapping.domain.service;

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
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUserResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUserData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersOrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.helper.ProfessionalUserBuilder;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private final ProfessionalRefreshOrchestrationHelper professionalRefreshOrchestrationHelper;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final ProcessEventTracker processEventTracker;

    private final String retryOneIntervalMin;
    private final String retryTwoIntervalMin;
    private final String retryThreeIntervalMin;
    private final String userRetryOneIntervalMin;
    private final String userRetryTwoIntervalMin;
    private final String userRetryThreeIntervalMin;

    private final String activeUserRefreshDays;
    private final String pageSize;
    private final String tolerance;

    public ProfessionalUserService(
            PrdService prdService,
            AccessTypesRepository accessTypesRepository,
            BatchLastRunTimestampRepository batchLastRunTimestampRepository,
            DatabaseDateTimeRepository databaseDateTimeRepository,
            OrganisationRefreshQueueRepository organisationRefreshQueueRepository,
            UserRefreshQueueRepository userRefreshQueueRepository,
            ProfessionalRefreshOrchestrationHelper professionalRefreshOrchestrationHelper,
            NamedParameterJdbcTemplate jdbcTemplate,
            PlatformTransactionManager transactionManager,
            ProcessEventTracker processEventTracker,
            @Value("${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.retryOneIntervalMin}")
            String retryOneIntervalMin,
            @Value("${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.retryTwoIntervalMin}")
            String retryTwoIntervalMin,
            @Value("${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.retryThreeIntervalMin}")
            String retryThreeIntervalMin,
            @Value("${professional.role.mapping.scheduling.userRefresh.retryOneIntervalMin}")
            String userRetryOneIntervalMin,
            @Value("${professional.role.mapping.scheduling.userRefresh.retryTwoIntervalMin}")
            String userRetryTwoIntervalMin,
            @Value("${professional.role.mapping.scheduling.userRefresh.retryThreeIntervalMin}")
            String userRetryThreeIntervalMin,
            @Value("${professional.role.mapping.scheduling.userRefreshCleanup.activeUserRefreshDays}")
            String activeUserRefreshDays,
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

        this.professionalRefreshOrchestrationHelper = professionalRefreshOrchestrationHelper;

        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.processEventTracker = processEventTracker;

        this.retryOneIntervalMin = retryOneIntervalMin;
        this.retryTwoIntervalMin = retryTwoIntervalMin;
        this.retryThreeIntervalMin = retryThreeIntervalMin;

        this.userRetryOneIntervalMin = userRetryOneIntervalMin;
        this.userRetryTwoIntervalMin = userRetryTwoIntervalMin;
        this.userRetryThreeIntervalMin = userRetryThreeIntervalMin;

        this.activeUserRefreshDays = activeUserRefreshDays;
        this.pageSize = pageSize;
        this.tolerance = tolerance;
    }

    public ProcessMonitorDto findAndInsertUsersWithStaleOrganisationsIntoRefreshQueueById(String organisationId) {
        log.info("Starting with Id {}", PROCESS_4_NAME);
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(PROCESS_4_NAME);
        processEventTracker.trackEventStarted(processMonitorDto);
        Optional<OrganisationRefreshQueueEntity> organisationRefreshQueueEntity =
            organisationRefreshQueueRepository.findById(organisationId);
        String errorMessage;
        if (organisationRefreshQueueEntity.isPresent()) {
            errorMessage =
                    findAndInsertUsersWithStaleOrganisationsIntoRefreshQueueByEntity(
                            organisationRefreshQueueEntity.get());
            if (errorMessage.isEmpty()) {
                addProcess4Steps(processMonitorDto,
                        List.of(organisationRefreshQueueEntity.get().getOrganisationId()));
            }
        } else {
            errorMessage = String.format("Organisation with ID %s not found in the refresh queue", organisationId);
            processMonitorDto.addProcessStep(errorMessage);
        }
        markProcessStatus(processMonitorDto,
                errorMessage.isEmpty() ? 1 : 0,
                errorMessage.isEmpty() ? 0 : 1,
                errorMessage);
        processEventTracker.trackEventCompleted(processMonitorDto);
        return processMonitorDto;
    }

    public ProcessMonitorDto findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue() {
        log.info("Starting {}", PROCESS_4_NAME);
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(PROCESS_4_NAME);
        processEventTracker.trackEventStarted(processMonitorDto);
        StringBuilder errorMessageBuilder = new StringBuilder();
        int successfulJobCount = 0;
        int failedJobCount = 0;
        List<String> organisationInfo = new ArrayList<>();
        String errorMessage;
        try {
            boolean anyEntitiesInQueue = true;
            while (anyEntitiesInQueue) {
                OrganisationRefreshQueueEntity organisationRefreshQueueEntity =
                        organisationRefreshQueueRepository.findAndLockSingleActiveOrganisationRecord();
                if (organisationRefreshQueueEntity != null) {
                    organisationInfo.add(organisationRefreshQueueEntity.getOrganisationId());
                    errorMessage =
                            findAndInsertUsersWithStaleOrganisationsIntoRefreshQueueByEntity(
                                    organisationRefreshQueueEntity);
                    boolean isSuccess = errorMessage.isEmpty();
                    if (isSuccess) {
                        successfulJobCount++;
                    } else {
                        failedJobCount++;
                        errorMessageBuilder.append(errorMessage);
                    }
                }
                anyEntitiesInQueue = organisationRefreshQueueEntity != null;
            }
            if (successfulJobCount == 0 && failedJobCount == 0) {
                processMonitorDto.addProcessStep("No entities to process");
                log.info("Completed {}. No entities to process", PROCESS_4_NAME);
            } else {
                addProcess4Steps(processMonitorDto, organisationInfo);
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
        markProcessStatus(processMonitorDto,
                successfulJobCount, failedJobCount,
                errorMessageBuilder.toString());
        processEventTracker.trackEventCompleted(processMonitorDto);
        return processMonitorDto;
    }

    private void addProcess4Steps(ProcessMonitorDto processMonitorDto, List<String> organisationInfo) {
        processMonitorDto.addProcessStep("attempting upsertToUserRefreshQueue for "
                + organisationInfo.size() + " organisations");
        String processStep = "=" + organisationInfo
                .stream().map(o -> o + ",").collect(Collectors.joining());
        processMonitorDto.appendToLastProcessStep(processStep);
    }

    private String findAndInsertUsersWithStaleOrganisationsIntoRefreshQueueByEntity(
        OrganisationRefreshQueueEntity organisationRefreshQueueEntity
    ) {
        StringBuilder errorMessageBuilder = new StringBuilder();
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

                return true;
            } catch (Exception ex) {
                String message = String.format("Error occurred while processing organisation: %s. Retry attempt "
                                + "%d. Rolling back.",
                        organisationIdentifier, organisationRefreshQueueEntity.getRetry());
                errorMessageBuilder.append(ex.getMessage());
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
        return errorMessageBuilder.toString();
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
                GetRefreshUserResponse getRefreshUserResponse = prdService
                        .retrieveUsers(formattedSince, Integer.valueOf(pageSize), lastRecordInPage).getBody();

                if (getRefreshUserResponse != null && CollectionUtils.isNotEmpty(getRefreshUserResponse.getUsers())) {
                    foundUsers = true;
                    writeAllToUserRefreshQueue(getRefreshUserResponse, accessTypeMinVersion, processMonitorDto);
                } else {
                    break;
                }

                // prep for next call to retrieveUsers
                moreAvailable = getRefreshUserResponse.isMoreAvailable();
                lastRecordInPage = getRefreshUserResponse.getLastRecordInPage();
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

    protected void markProcessStatus(ProcessMonitorDto processMonitorDto, int successfulJobCount,
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

    private void writeAllToUserRefreshQueue(GetRefreshUserResponse getRefreshUserResponse,
                                            Integer accessTypeMinVersion,
                                            ProcessMonitorDto processMonitorDto) {
        String processStep = "attempting writeAllToUserRefreshQueue for ";
        processMonitorDto.addProcessStep(processStep);

        List<ProfessionalUserData> professionalUserData = new ArrayList<>();
        for (RefreshUser user : getRefreshUserResponse.getUsers()) {
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
