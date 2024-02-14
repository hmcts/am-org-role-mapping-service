package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrganisationService {

    private final PrdService prdService;
    private final ProfileRefreshQueueRepository profileRefreshQueueRepository;
    private final OrganisationRefreshQueueRepository organisationRefreshQueueRepository;
    private final String pageSize;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public OrganisationService(PrdService prdService,
                               OrganisationRefreshQueueRepository organisationRefreshQueueRepository,
                               ProfileRefreshQueueRepository profileRefreshQueueRepository,
                               @Value("${professional.refdata.pageSize}") String pageSize,
                               NamedParameterJdbcTemplate jdbcTemplate) {
        this.prdService = prdService;
        this.profileRefreshQueueRepository = profileRefreshQueueRepository;
        this.organisationRefreshQueueRepository = organisationRefreshQueueRepository;
        this.pageSize = pageSize;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void findAndInsertStaleOrganisationsIntoRefreshQueue() {
        List<ProfileRefreshQueueEntity> profileRefreshQueueEntities
                = profileRefreshQueueRepository.getActiveProfileEntities();

        if (profileRefreshQueueEntities.isEmpty()) {
            return;
        }

        List<String> activeOrganisationProfileIds = profileRefreshQueueEntities.stream()
                .map(ProfileRefreshQueueEntity::getOrganisationProfileId)
                .collect(Collectors.toList());
        // HLD: Note that it is easier to take the maximum version number from profile refresh queue and apply it to
        // all organisations.
        // This is consistent with the semantics of "this version number or higher", and will cause no problems.
        Optional<Integer> maxVersion = profileRefreshQueueEntities.stream()
                .map(ProfileRefreshQueueEntity::getAccessTypesMinVersion)
                .max(Comparator.naturalOrder());

        OrganisationByProfileIdsRequest request = new OrganisationByProfileIdsRequest(activeOrganisationProfileIds);

        OrganisationByProfileIdsResponse response;
        response = Objects.requireNonNull(
                prdService.fetchOrganisationsByProfileIds(Integer.valueOf(pageSize), null, request).getBody()
        );

        boolean moreAvailable;
        String lastRecordInPage;

        if (!response.getOrganisationInfo().isEmpty()) {
            moreAvailable = response.getMoreAvailable();
            lastRecordInPage = response.getLastRecordInPage();

            writeAllToOrganisationRefreshQueue(response.getOrganisationInfo(), maxVersion.get());

            while (moreAvailable) {
                response = Objects.requireNonNull(prdService.fetchOrganisationsByProfileIds(
                        Integer.valueOf(pageSize), lastRecordInPage, request).getBody());

                if (!response.getOrganisationInfo().isEmpty()) {
                    moreAvailable = response.getMoreAvailable();
                    lastRecordInPage = response.getLastRecordInPage();

                    writeAllToOrganisationRefreshQueue(response.getOrganisationInfo(), maxVersion.get());
                } else {
                    break;
                }
            }
        }

        // fine to set all profiles `active` status to false, even if no organisations identified for given profile ids
        updateProfileRefreshQueueActiveStatus(activeOrganisationProfileIds, maxVersion.get());
    }

    private void writeAllToOrganisationRefreshQueue(List<OrganisationInfo> organisationInfo,
                                                    Integer accessTypeMinVersion) {
        organisationRefreshQueueRepository.upsertToOrganisationRefreshQueue(
                jdbcTemplate, organisationInfo, accessTypeMinVersion
        );
    }

    private void updateProfileRefreshQueueActiveStatus(List<String> organisationProfileIds,
                                                       Integer accessTypeMaxVersion) {
        organisationProfileIds.forEach(organisationProfileId -> profileRefreshQueueRepository.setActiveFalse(
                organisationProfileId,
                accessTypeMaxVersion
        ));
    }
}
