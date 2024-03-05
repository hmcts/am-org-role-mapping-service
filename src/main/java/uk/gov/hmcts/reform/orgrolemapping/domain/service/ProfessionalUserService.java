package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
public class ProfessionalUserService {

    private final PrdService prdService;
    private final OrganisationRefreshQueueRepository organisationRefreshQueueRepository;
    private final UserRefreshQueueRepository userRefreshQueueRepository;
    private final String pageSize;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final ProcessEventTracker processEventTracker;

    public ProfessionalUserService(PrdService prdService,
                                   OrganisationRefreshQueueRepository organisationRefreshQueueRepository,
                                   UserRefreshQueueRepository userRefreshQueueRepository,
                                   @Value("${professional.refdata.pageSize}") String pageSize,
                                   NamedParameterJdbcTemplate jdbcTemplate, ProcessEventTracker processEventTracker) {
        this.prdService = prdService;
        this.organisationRefreshQueueRepository = organisationRefreshQueueRepository;
        this.userRefreshQueueRepository = userRefreshQueueRepository;
        this.pageSize = pageSize;
        this.jdbcTemplate = jdbcTemplate;
        this.processEventTracker = processEventTracker;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue() {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(
                "PRM Process 4 - Find Users with Stale Organisations");
        processEventTracker.trackEventStarted(processMonitorDto);

        try {
            OrganisationRefreshQueueEntity organisationRefreshQueueEntity
                    = organisationRefreshQueueRepository.findAndLockSingleActiveOrganisationRecord();

            if (organisationRefreshQueueEntity == null) {
                processMonitorDto.markAsSuccess();
                processEventTracker.trackEventCompleted(processMonitorDto);
                return;
            }

            Integer accessTypesMinVersion = organisationRefreshQueueEntity.getAccessTypesMinVersion();
            String organisationIdentifier = organisationRefreshQueueEntity.getOrganisationId();

            UsersByOrganisationRequest request = new UsersByOrganisationRequest(
                    List.of(organisationIdentifier)
            );

            retrieveUsersByOrganisationAndUpsert(request, accessTypesMinVersion);

            organisationRefreshQueueRepository.setActiveFalse(
                    organisationIdentifier,
                    accessTypesMinVersion,
                    organisationRefreshQueueEntity.getLastUpdated()
            );
        } catch (Exception e){
            processMonitorDto.markAsFailed(e.getMessage());
            processEventTracker.trackEventCompleted(processMonitorDto);
            throw e;
        }
        processMonitorDto.markAsSuccess();
        processEventTracker.trackEventCompleted(processMonitorDto);
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