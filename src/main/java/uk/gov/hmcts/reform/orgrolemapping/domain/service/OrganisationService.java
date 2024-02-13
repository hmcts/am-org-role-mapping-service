package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.DatabaseDateTime;
import uk.gov.hmcts.reform.orgrolemapping.data.DatabaseDateTimeRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationsResponse;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
    private final AccessTypesRepository accessTypesRepository;
    private final BatchLastRunTimestampRepository batchLastRunTimestampRepository;
    private final DatabaseDateTimeRepository databaseDateTimeRepository;
    private final String pageSize;
    private static final String SINCE_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern(SINCE_TIMESTAMP_FORMAT);

    @Value("${groupAccess.lastRunTimeTolerance}")
    private String tolerance;

    public OrganisationService(PrdService prdService,
                               OrganisationRefreshQueueRepository organisationRefreshQueueRepository,
                               ProfileRefreshQueueRepository profileRefreshQueueRepository,
                               AccessTypesRepository accessTypesRepository,
                               BatchLastRunTimestampRepository batchLastRunTimestampRepository,
                               DatabaseDateTimeRepository databaseDateTimeRepository,
                               @Value("${professional.refdata.pageSize}") String pageSize,
                               @Value("${groupAccess.lastRunTimeTolerance}") String tolerance
                               ) {
        this.prdService = prdService;
        this.profileRefreshQueueRepository = profileRefreshQueueRepository;
        this.organisationRefreshQueueRepository = organisationRefreshQueueRepository;
        this.accessTypesRepository = accessTypesRepository;
        this.batchLastRunTimestampRepository = batchLastRunTimestampRepository;
        this.databaseDateTimeRepository = databaseDateTimeRepository;
        this.pageSize = pageSize;
        this.tolerance = tolerance;
    }

    @Transactional
    public void findOrganisationChangesAndInsertIntoOrganisationRefreshQueue() {
        log.info("findOrganisationChangesAndInsertIntoOrganisationRefreshQueue started...");
        final DatabaseDateTime batchRunStartTime = databaseDateTimeRepository.getCurrentTimeStamp();
        List<AccessTypesEntity> allAccessTypes = accessTypesRepository.findAll();
        if (allAccessTypes.size() != 1) {
            throw new ServiceException("Single AccessTypesEntity not found");
        }
        AccessTypesEntity accessTypesEntity = allAccessTypes.get(0);
        List<BatchLastRunTimestampEntity> allBatchLastRunTimestampEntities = batchLastRunTimestampRepository
                .findAll();
        if (allBatchLastRunTimestampEntities.size() != 1) {
            throw new ServiceException("Single BatchLastRunTimestampEntity not found");
        }
        BatchLastRunTimestampEntity batchLastRunTimestampEntity = allBatchLastRunTimestampEntities.get(0);
        LocalDateTime orgLastBatchRunTime = batchLastRunTimestampEntity.getLastOrganisationRunDatetime();

        int toleranceSeconds = Integer.parseInt(tolerance);
        LocalDateTime sinceTime = orgLastBatchRunTime.minusSeconds(toleranceSeconds);
        String formattedSince = ISO_DATE_TIME_FORMATTER.format(sinceTime);

        Integer accessTypeMinVersion = accessTypesEntity.getVersion().intValue();
        OrganisationsResponse organisationProfiles = prdService
                .retrieveOrganisations(formattedSince, 1, Integer.valueOf(pageSize)).getBody();
        writeAllToOrganisationRefreshQueue(organisationProfiles, accessTypeMinVersion);

        int page = 2;
        while (organisationProfiles.getMoreAvailable()) {

            organisationProfiles = prdService
                    .retrieveOrganisations(formattedSince, page, Integer.valueOf(pageSize)).getBody();
            writeAllToOrganisationRefreshQueue(organisationProfiles, accessTypeMinVersion);
            page++;
        }
        batchLastRunTimestampEntity.setLastOrganisationRunDatetime(LocalDateTime.ofInstant(batchRunStartTime.getDate(),
                ZoneOffset.systemDefault()));
        batchLastRunTimestampRepository.save(batchLastRunTimestampEntity);
        log.info("...findOrganisationChangesAndInsertIntoOrganisationRefreshQueue finished");
    }

    @Transactional
    public void findAndInsertStaleOrganisationsIntoRefreshQueue() {
        List<ProfileRefreshQueueEntity> profileRefreshQueueEntities
                = profileRefreshQueueRepository.getActiveProfileEntities();
        List<String> activeOrganisationProfileIds = profileRefreshQueueEntities.stream()
                .map(ProfileRefreshQueueEntity::getOrganisationProfileId)
                .collect(Collectors.toList());
        // HLD: Note that it is easier to take the maximum version number from profile refresh queue and apply it to
        // all organisations.
        // This is consistent with the semantics of "this version number or higher", and will cause no problems.
        Optional<Integer> maxVersion = profileRefreshQueueEntities.stream()
                .map(ProfileRefreshQueueEntity::getAccessTypesMinVersion)
                .max(Comparator.naturalOrder());

        if (activeOrganisationProfileIds.isEmpty()) {
            return;
        }

        OrganisationByProfileIdsRequest request = new OrganisationByProfileIdsRequest(activeOrganisationProfileIds);

        OrganisationByProfileIdsResponse response;
        response = prdService.fetchOrganisationsByProfileIds(Integer.valueOf(pageSize), null, request).getBody();

        boolean moreAvailable;
        String lastRecordInPage;

        if (responseNotNull(response)) {
            moreAvailable = response.getMoreAvailable();
            lastRecordInPage = response.getLastRecordInPage();

            writeAllToOrganisationRefreshQueue(response.getOrganisationInfo(), maxVersion.get());

            while (moreAvailable) {
                response = prdService.fetchOrganisationsByProfileIds(
                        Integer.valueOf(pageSize), lastRecordInPage, request).getBody();

                if (responseNotNull(response)) {
                    moreAvailable = response.getMoreAvailable();
                    lastRecordInPage = response.getLastRecordInPage();

                    writeAllToOrganisationRefreshQueue(response.getOrganisationInfo(), maxVersion.get());
                } else {
                    break;
                }
            }

            updateProfileRefreshQueueActiveStatus(activeOrganisationProfileIds, maxVersion.get());
        }
    }

    private void updateProfileRefreshQueueActiveStatus(List<String> organisationProfileIds,
                                                       Integer accessTypeMaxVersion) {
        organisationProfileIds.forEach(organisationProfileId -> profileRefreshQueueRepository.setActiveFalse(
                organisationProfileId,
                accessTypeMaxVersion
        ));
    }

    private boolean responseNotNull(OrganisationByProfileIdsResponse response) {
        return response != null && !response.getOrganisationInfo().isEmpty();
    }

    private void writeAllToOrganisationRefreshQueue(List<OrganisationInfo> organisationInfo,
                                                    Integer accessTypeMinVersion) {
        organisationInfo.forEach(orgInfo -> organisationRefreshQueueRepository.upsertToOrganisationRefreshQueue(
                orgInfo.getOrganisationIdentifier(),
                orgInfo.getLastUpdated(),
                accessTypeMinVersion
        ));
    }

    private void writeAllToOrganisationRefreshQueue(OrganisationsResponse organisationProfiles,
                                                    Integer accessTypeMinVersion) {
        organisationProfiles.getOrganisations().stream().forEach(orgInfo ->
                insertIntoOrganisationRefreshQueueForLastUpdated(orgInfo, accessTypeMinVersion));
    }

    private void insertIntoOrganisationRefreshQueueForLastUpdated(OrganisationInfo orgInfo,
                                                                  Integer accessTypeMinVersion) {
        organisationRefreshQueueRepository
                .insertIntoOrganisationRefreshQueueForLastUpdated(orgInfo.getOrganisationIdentifier(),
                        orgInfo.getLastUpdated(), accessTypeMinVersion);
    }

}
