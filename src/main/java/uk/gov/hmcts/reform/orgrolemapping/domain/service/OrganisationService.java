package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationStaleProfilesRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationStaleProfilesResponse;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrganisationService {

    private final PrdService prdService;
    private final ProfileRefreshQueueRepository profileRefreshQueueRepository;
    private final OrganisationRefreshQueueRepository organisationRefreshQueueRepository;
    private final String pageSize;

    public OrganisationService(PrdService prdService,
                               OrganisationRefreshQueueRepository organisationRefreshQueueRepository,
                               ProfileRefreshQueueRepository profileRefreshQueueRepository,
                               @Value("${professional.refdata.pageSize}") String pageSize) {
        this.prdService = prdService;
        this.profileRefreshQueueRepository = profileRefreshQueueRepository;
        this.organisationRefreshQueueRepository = organisationRefreshQueueRepository;
        this.pageSize = pageSize;
    }

    @Transactional
    public void findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueue() {
        log.info("hi");
        List<ProfileRefreshQueueEntity> profileRefreshQueueEntities
                = profileRefreshQueueRepository.getActiveProfileEntities();
        List<String> activeOrganisationProfileIds = profileRefreshQueueEntities.stream()
                .map(ProfileRefreshQueueEntity::getOrganisationProfileId)
                .collect(Collectors.toList());
        // HLD: Note that it is easier to take the maximum version number from step 2 and apply it to all organisations
        // This is consistent with the semantics of "this version number or higher", and will cause no problems
        Optional<Integer> maxVersion = profileRefreshQueueEntities.stream()
                .map(ProfileRefreshQueueEntity::getAccessTypesMinVersion)
                .max(Comparator.naturalOrder());

        if (activeOrganisationProfileIds.isEmpty()) {
            return;
        }

        OrganisationStaleProfilesResponse response;
        OrganisationStaleProfilesRequest request = new OrganisationStaleProfilesRequest(activeOrganisationProfileIds);

        response = prdService.fetchOrganisationsWithStaleProfiles(Integer.valueOf(pageSize), null, request).getBody();

        boolean moreAvailable;
        String lastRecordInPage;

        if (responseNotNull(response) && maxVersion.isPresent()) {
            moreAvailable = response.getMoreAvailable();
            lastRecordInPage = response.getLastRecordInPage();

            insertIntoOrganisationRefreshQueue(response.getOrganisationInfo(), maxVersion.get());

            while (moreAvailable) {
                response = prdService.fetchOrganisationsWithStaleProfiles(
                        Integer.valueOf(pageSize), lastRecordInPage, request).getBody();

                if (responseNotNull(response)) {
                    moreAvailable = response.getMoreAvailable();
                    lastRecordInPage = response.getLastRecordInPage();

                    insertIntoOrganisationRefreshQueue(response.getOrganisationInfo(), maxVersion.get());
                }
            }

            updateOrganisationRefreshQueueActiveStatus(activeOrganisationProfileIds, maxVersion.get());
        }
    }

    private void insertIntoOrganisationRefreshQueue(List<OrganisationInfo> organisationInfo,
                                                    Integer accessTypeMinVersion) {
        organisationInfo.forEach(orgInfo -> organisationRefreshQueueRepository.insertIntoOrganisationRefreshQueue(
                orgInfo.getOrganisationIdentifier(),
                orgInfo.getLastUpdated(),
                accessTypeMinVersion
        ));
    }

    private void updateOrganisationRefreshQueueActiveStatus(List<String> organisationProfileIds,
                                                            Integer accessTypeMaxVersion) {
        organisationProfileIds.forEach(organisationProfileId -> profileRefreshQueueRepository.setActiveFalse(
                organisationProfileId,
                accessTypeMaxVersion
        ));
    }

    private boolean responseNotNull(OrganisationStaleProfilesResponse response) {
        return response != null && response.getOrganisationInfo() != null && !response.getLastRecordInPage().isEmpty()
                && response.getMoreAvailable() != null;
    }
}
