package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersOrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUserData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.orgrolemapping.helper.ProfessionalUserBuilder.fromProfessionalUserAndOrganisationInfo;

@Service
@Slf4j
public class ProfessionalUserService {

    public static final String PROCESS4_NAME = "PRM Process 4 - Find Users with Stale Organisations";
    private final PrdService prdService;
    private final OrganisationRefreshQueueRepository organisationRefreshQueueRepository;
    private final UserRefreshQueueRepository userRefreshQueueRepository;
    private final String pageSize;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final String retryOneIntervalMin;
    private final String retryTwoIntervalMin;
    private final String retryThreeIntervalMin;

    private final ProcessEventTracker processEventTracker;

    public ProfessionalUserService(
            PrdService prdService,
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
            ProcessEventTracker processEventTracker) {
        this.prdService = prdService;
        this.organisationRefreshQueueRepository = organisationRefreshQueueRepository;
        this.userRefreshQueueRepository = userRefreshQueueRepository;
        this.pageSize = pageSize;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.retryOneIntervalMin = retryOneIntervalMin;
        this.retryTwoIntervalMin = retryTwoIntervalMin;
        this.retryThreeIntervalMin = retryThreeIntervalMin;
        this.processEventTracker = processEventTracker;
    }

    public OrganisationRefreshQueueEntity findAndLockSingleActiveOrganisationRecord() {
        return organisationRefreshQueueRepository.findAndLockSingleActiveOrganisationRecord();
    }

    public ProcessMonitorDto findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue(
        OrganisationRefreshQueueEntity organisationRefreshQueueEntity) {
        log.info("Starting {}", PROCESS4_NAME);
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(PROCESS4_NAME);
        processEventTracker.trackEventStarted(processMonitorDto);

        try {
            if (organisationRefreshQueueEntity == null) {
                processMonitorDto.addProcessStep("No entities to process");
                processMonitorDto.markAsSuccess();
                processEventTracker.trackEventCompleted(processMonitorDto);
                log.info("Completed {}. No entities to process", PROCESS4_NAME);
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

                    organisationRefreshQueueRepository.setActiveFalse(
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
        processEventTracker.trackEventCompleted(processMonitorDto);

        log.info("Completed {}", PROCESS4_NAME);
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