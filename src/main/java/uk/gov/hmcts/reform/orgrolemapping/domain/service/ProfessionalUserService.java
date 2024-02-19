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

    public ProfessionalUserService(PrdService prdService,
                                   OrganisationRefreshQueueRepository organisationRefreshQueueRepository,
                                   UserRefreshQueueRepository userRefreshQueueRepository,
                                   @Value("${professional.refdata.pageSize}") String pageSize,
                                   NamedParameterJdbcTemplate jdbcTemplate) {
        this.prdService = prdService;
        this.organisationRefreshQueueRepository = organisationRefreshQueueRepository;
        this.userRefreshQueueRepository = userRefreshQueueRepository;
        this.pageSize = pageSize;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue() {
        OrganisationRefreshQueueEntity organisationRefreshQueueEntity
                = organisationRefreshQueueRepository.findAndLockSingleActiveOrganisationRecord();

        if (organisationRefreshQueueEntity == null) {
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