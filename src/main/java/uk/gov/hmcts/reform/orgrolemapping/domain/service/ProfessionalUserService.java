package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfessionalUserService {

    private final PrdService prdService;
    private final OrganisationRefreshQueueRepository organisationRefreshQueueRepository;
    private final UserRefreshQueueRepository userRefreshQueueRepository;
    private final String pageSize;
    private final ObjectMapper objectMapper;

    public ProfessionalUserService(PrdService prdService,
                                   OrganisationRefreshQueueRepository organisationRefreshQueueRepository,
                                   UserRefreshQueueRepository userRefreshQueueRepository,
                                   @Value("${professional.refdata.pageSize}") String pageSize,
                                   ObjectMapper objectMapper) {
        this.prdService = prdService;
        this.organisationRefreshQueueRepository = organisationRefreshQueueRepository;
        this.userRefreshQueueRepository = userRefreshQueueRepository;
        this.pageSize = pageSize;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue() {
        OrganisationRefreshQueueEntity organisationRefreshQueueEntity
                = organisationRefreshQueueRepository.findAndLockSingleActiveOrganisationRecord();

        // base case to terminate recursive calls
        if (organisationRefreshQueueEntity == null) {
            return;
        }

        Integer accessTypesMinVersion = organisationRefreshQueueEntity.getAccessTypesMinVersion();
        String organisationIdentifier = organisationRefreshQueueEntity.getOrganisationId();

        UsersByOrganisationRequest request = new UsersByOrganisationRequest(
                List.of(organisationIdentifier)
        );

        UsersByOrganisationResponse response;
        response = prdService.fetchUsersByOrganisation(Integer.valueOf(pageSize), null, null, request).getBody();

        boolean moreAvailable;
        String searchAfterOrg;
        String searchAfterUser;
        List<ProfessionalUser> users;

        if (responseNotNull(response)) {
            moreAvailable = response.getMoreAvailable();
            searchAfterOrg = response.getLastOrgInPage();
            searchAfterUser = response.getLastUserInPage();
            users = getProfessionalUsers(response);

            writeAllToUserRefreshQueue(users, response.getOrganisationInfo().get(0), accessTypesMinVersion);

            while (moreAvailable) {
                response = prdService.fetchUsersByOrganisation(
                        Integer.valueOf(pageSize), searchAfterOrg, searchAfterUser, request).getBody();

                if (responseNotNull(response)) {
                    moreAvailable = response.getMoreAvailable();
                    searchAfterOrg = response.getLastOrgInPage();
                    searchAfterUser = response.getLastUserInPage();
                    users = getProfessionalUsers(response);

                    writeAllToUserRefreshQueue(users, response.getOrganisationInfo().get(0), accessTypesMinVersion);
                } else {
                    break;
                }
            }

            organisationRefreshQueueRepository.setActiveFalse(
                    organisationIdentifier,
                    accessTypesMinVersion,
                    organisationRefreshQueueEntity.getLastUpdated()
            );
        }

        // HLD: Each iteration of steps 2 - 5 requires a single database transaction.
        findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue();
    }

    private void writeAllToUserRefreshQueue(List<ProfessionalUser> users,
                                            OrganisationInfo organisationInfo,
                                            Integer accessTypesMinVersion) {
        users.forEach(user -> {
            String userAccessTypes;
            try {
                userAccessTypes = objectMapper.writeValueAsString(user.getUserAccessTypes());
            } catch (JsonProcessingException e) {
                throw new ServiceException("JsonProcessingException when serializing users access types");
            }

            userRefreshQueueRepository.upsertToUserRefreshQueue(
                    user.getUserIdentifier(),
                    user.getLastUpdated(),
                    accessTypesMinVersion,
                    user.getDeleted(),
                    userAccessTypes,
                    organisationInfo.getOrganisationIdentifier(),
                    organisationInfo.getStatus(),
                    String.join(",", organisationInfo.getOrganisationProfileIds())
            );
        });
    }

    private List<ProfessionalUser> getProfessionalUsers(UsersByOrganisationResponse response) {
        return response.getOrganisationInfo().stream()
                .flatMap(organisationInfo -> organisationInfo.getUsers().stream())
                .collect(Collectors.toList());
    }

    private boolean responseNotNull(UsersByOrganisationResponse response) {
        return response != null && !response.getOrganisationInfo().isEmpty();
    }
}
