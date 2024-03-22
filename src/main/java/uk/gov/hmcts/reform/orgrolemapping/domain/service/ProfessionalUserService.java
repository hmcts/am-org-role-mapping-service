package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersOrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUserData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.orgrolemapping.helper.ProfessionalUserBuilder.fromProfessionalUserAndOrganisationInfo;

@Service
@Slf4j
public class ProfessionalUserService {

    private final PrdService prdService;
    private final OrganisationRefreshQueueRepository organisationRefreshQueueRepository;
    private final UserRefreshQueueRepository userRefreshQueueRepository;
    private final String pageSize;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final String retryOneIntervalMin;
    private final String retryTwoIntervalMin;
    private final String retryThreeIntervalMin;

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
            String pageSize) {
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
    }

    public boolean findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue(ProcessMonitorDto processMonitorDto) {


        OrganisationRefreshQueueEntity organisationRefreshQueueEntity
                = organisationRefreshQueueRepository.findAndLockSingleActiveOrganisationRecord();

        if (organisationRefreshQueueEntity == null) {
            processMonitorDto.addProcessStep("No entities to process");
            log.info("{} - No entities to process", processMonitorDto.getProcessType());
            return true;
        }

        Integer accessTypesMinVersion = organisationRefreshQueueEntity.getAccessTypesMinVersion();
        String organisationIdentifier = organisationRefreshQueueEntity.getOrganisationId();

        UsersByOrganisationRequest request = new UsersByOrganisationRequest(
                List.of(organisationIdentifier)
        );

        boolean isSuccess = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                processMonitorDto.addProcessStep("attempting retrieveUsersByOrganisationAndUpsert for organisationId="
                        + organisationRefreshQueueEntity.getOrganisationId());
                retrieveUsersByOrganisationAndUpsert(request, accessTypesMinVersion);

                organisationRefreshQueueRepository.setActiveFalse(
                        organisationIdentifier,
                        accessTypesMinVersion,
                        organisationRefreshQueueEntity.getLastUpdated()
                );
                processMonitorDto.appendToLastProcessStep(" : COMPLETED");

                return true;
            } catch (Exception ex) {
                processMonitorDto.appendToLastProcessStep(" : FAILED");
                String message = String.format("Error occurred while processing organisation: %s. Retry attempt "
                                + "%d. Rolling back.",
                        organisationIdentifier, organisationRefreshQueueEntity.getRetry() + 1);
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
        }
        return isSuccess;
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