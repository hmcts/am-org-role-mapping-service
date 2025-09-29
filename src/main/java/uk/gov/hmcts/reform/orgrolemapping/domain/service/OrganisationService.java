package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.ISO_DATE_TIME_FORMATTER;

@Slf4j
@Service
public class OrganisationService {

    public static final String PROCESS_2_NAME = "PRM Process 2 - Find Organisations with Stale Profiles";
    public static final String PROCESS_3_NAME = "PRM Process 3 - Find organisation changes";

    private final PrdService prdService;
    private final ProfileRefreshQueueRepository profileRefreshQueueRepository;
    private final OrganisationRefreshQueueRepository organisationRefreshQueueRepository;
    private final AccessTypesRepository accessTypesRepository;
    private final BatchLastRunTimestampRepository batchLastRunTimestampRepository;
    private final DatabaseDateTimeRepository databaseDateTimeRepository;
    private final String pageSize;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ProcessEventTracker processEventTracker;
    private String tolerance;
    private static final String P2 = "P2";
    private static final String P3 = "P3";

    public OrganisationService(PrdService prdService,
                               OrganisationRefreshQueueRepository organisationRefreshQueueRepository,
                               ProfileRefreshQueueRepository profileRefreshQueueRepository,
                               @Value("${professional.refdata.pageSize}") String pageSize,
                               NamedParameterJdbcTemplate jdbcTemplate,
                               AccessTypesRepository accessTypesRepository,
                               BatchLastRunTimestampRepository batchLastRunTimestampRepository,
                               DatabaseDateTimeRepository databaseDateTimeRepository,
                               ProcessEventTracker processEventTracker,
                               @Value("${groupAccess.lastRunTimeTolerance}") String tolerance) {
        this.prdService = prdService;
        this.profileRefreshQueueRepository = profileRefreshQueueRepository;
        this.organisationRefreshQueueRepository = organisationRefreshQueueRepository;
        this.accessTypesRepository = accessTypesRepository;
        this.batchLastRunTimestampRepository = batchLastRunTimestampRepository;
        this.databaseDateTimeRepository = databaseDateTimeRepository;
        this.pageSize = pageSize;
        this.jdbcTemplate = jdbcTemplate;
        this.processEventTracker = processEventTracker;
        this.tolerance = tolerance;
    }

    @Transactional
    public ProcessMonitorDto findOrganisationChangesAndInsertIntoOrganisationRefreshQueue() {
        log.info("findOrganisationChangesAndInsertIntoOrganisationRefreshQueue started...");
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(PROCESS_3_NAME);
        processEventTracker.trackEventStarted(processMonitorDto);

        int page = 0;
        try {
            final DatabaseDateTime batchRunStartTime = databaseDateTimeRepository.getCurrentTimeStamp();
            List<AccessTypesEntity> allAccessTypes = accessTypesRepository.findAll();
            if (allAccessTypes.size() != 1) {
                throw new ServiceException("Single AccessTypesEntity not found");
            }
            AccessTypesEntity accessTypesEntity = allAccessTypes.get(0);
            BatchLastRunTimestampEntity batchLastRunTimestampEntity = getBatchLastRunTimestampEntity();
            LocalDateTime orgLastBatchRunTime = batchLastRunTimestampEntity.getLastOrganisationRunDatetime();

            int toleranceSeconds = Integer.parseInt(tolerance);
            LocalDateTime sinceTime = orgLastBatchRunTime.minusSeconds(toleranceSeconds);
            String formattedSince = ISO_DATE_TIME_FORMATTER.format(sinceTime);

            page = 1;
            Integer accessTypeMinVersion = accessTypesEntity.getVersion().intValue();
            OrganisationsResponse organisationsResponse = prdService
                    .retrieveOrganisations(formattedSince, page, Integer.valueOf(pageSize)).getBody();
            writeAllToOrganisationRefreshQueue(organisationsResponse.getOrganisations(),
                    accessTypeMinVersion, P3, processMonitorDto);

            page = 2;
            boolean moreAvailable = organisationsResponse.getMoreAvailable();
            while (moreAvailable) {
                organisationsResponse = prdService
                        .retrieveOrganisations(formattedSince, page, Integer.valueOf(pageSize)).getBody();
                writeAllToOrganisationRefreshQueue(organisationsResponse.getOrganisations(),
                        accessTypeMinVersion, P3, processMonitorDto);
                moreAvailable = organisationsResponse.getMoreAvailable();
                page++;
            }
            batchLastRunTimestampEntity.setLastOrganisationRunDatetime(LocalDateTime
                    .ofInstant(batchRunStartTime.getDate(), ZoneId.systemDefault()));
            batchLastRunTimestampRepository.save(batchLastRunTimestampEntity);
        } catch (Exception exception) {
            String pageFailMessage = (page == 0 ? "" : ", failed at page " + page);
            processMonitorDto.markAsFailed(exception.getMessage() + pageFailMessage);
            processEventTracker.trackEventCompleted(processMonitorDto);
            throw exception;
        }
        processMonitorDto.markAsSuccess();
        processEventTracker.trackEventCompleted(processMonitorDto);
        return processMonitorDto;
    }

    @Transactional
    public ProcessMonitorDto findAndInsertStaleOrganisationsIntoRefreshQueue() {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(PROCESS_2_NAME);
        processEventTracker.trackEventStarted(processMonitorDto);

        try {
            List<ProfileRefreshQueueEntity> profileRefreshQueueEntities
                = profileRefreshQueueRepository.getActiveProfileEntities();

            if (profileRefreshQueueEntities.isEmpty()) {
                processMonitorDto.addProcessStep("No active organisation profiles found");
                processMonitorDto.markAsSuccess();
                processEventTracker.trackEventCompleted(processMonitorDto);
                return processMonitorDto;
            }

            List<String> activeOrganisationProfileIds = profileRefreshQueueEntities.stream()
                    .map(ProfileRefreshQueueEntity::getOrganisationProfileId).toList();
            // HLD: Note that it is easier to take the maximum version number from profile refresh queue and apply it to
            // all organisations.
            // This is consistent with the semantics of "this version number or higher", and will cause no problems.
            Optional<Integer> maxVersion = profileRefreshQueueEntities.stream()
                    .map(ProfileRefreshQueueEntity::getAccessTypesMinVersion)
                    .max(Comparator.naturalOrder());

            OrganisationByProfileIdsRequest request = new OrganisationByProfileIdsRequest(activeOrganisationProfileIds);

            if (maxVersion.isEmpty()) {
                processMonitorDto.addProcessStep("No max version found");
                processMonitorDto.markAsSuccess();
                processEventTracker.trackEventCompleted(processMonitorDto);
                return processMonitorDto;
            }

            retrieveOrganisationsByProfileIdsAndUpsert(request, maxVersion.get(), processMonitorDto);

            updateProfileRefreshQueueActiveStatus(activeOrganisationProfileIds, maxVersion.get());
        } catch (Exception e) {
            processMonitorDto.markAsFailed(e.getMessage());
            processEventTracker.trackEventCompleted(processMonitorDto);
            throw e;
        }
        processMonitorDto.markAsSuccess();
        processEventTracker.trackEventCompleted(processMonitorDto);
        return processMonitorDto;
    }

    public BatchLastRunTimestampEntity getBatchLastRunTimestampEntity() {
        List<BatchLastRunTimestampEntity> allBatchLastRunTimestampEntities = batchLastRunTimestampRepository
            .findAll();
        if (allBatchLastRunTimestampEntities.size() != 1) {
            throw new ServiceException("Single BatchLastRunTimestampEntity not found");
        }
        return allBatchLastRunTimestampEntities.get(0);
    }

    private void retrieveOrganisationsByProfileIdsAndUpsert(OrganisationByProfileIdsRequest request,
                                                            Integer accessTypesMinVersion,
                                                            ProcessMonitorDto processMonitorDto) {
        OrganisationByProfileIdsResponse response;
        response = Objects.requireNonNull(
                prdService.fetchOrganisationsByProfileIds(Integer.valueOf(pageSize), null, request).getBody()
        );

        boolean moreAvailable;
        String lastRecordInPage;

        if (!response.getOrganisationInfo().isEmpty()) {
            moreAvailable = response.getMoreAvailable();
            lastRecordInPage = response.getLastRecordInPage();

            writeAllToOrganisationRefreshQueue(response.getOrganisationInfo(),
                    accessTypesMinVersion, P2, processMonitorDto);

            while (moreAvailable) {
                response = Objects.requireNonNull(prdService.fetchOrganisationsByProfileIds(
                        Integer.valueOf(pageSize), lastRecordInPage, request).getBody());

                if (!response.getOrganisationInfo().isEmpty()) {
                    moreAvailable = response.getMoreAvailable();
                    lastRecordInPage = response.getLastRecordInPage();

                    writeAllToOrganisationRefreshQueue(response.getOrganisationInfo(),
                            accessTypesMinVersion, P2, processMonitorDto);
                } else {
                    break;
                }
            }
        }
    }

    private void writeAllToOrganisationRefreshQueue(List<OrganisationInfo> organisationInfo,
                                                    Integer accessTypeMinVersion, String process,
                                                    ProcessMonitorDto processMonitorDto) {
        String processStep;
        processStep = "attempting upsertToOrganisationRefreshQueue for " + organisationInfo.size() + " organisations";
        processStep = processStep + "=" + organisationInfo
                .stream().map(o -> o.getOrganisationIdentifier() + ",").collect(Collectors.joining());
        processMonitorDto.addProcessStep(processStep);

        if (process.equals(P2)) {
            organisationRefreshQueueRepository.upsertToOrganisationRefreshQueue(
                    jdbcTemplate, organisationInfo, accessTypeMinVersion);
        } else {
            organisationRefreshQueueRepository.upsertToOrganisationRefreshQueueForLastUpdated(
                    jdbcTemplate, organisationInfo, accessTypeMinVersion);
        }

        processMonitorDto.getProcessSteps().remove(processMonitorDto.getProcessSteps().size() - 1);
        processMonitorDto.addProcessStep(processStep + " : COMPLETED");
    }

    private void updateProfileRefreshQueueActiveStatus(List<String> organisationProfileIds,
                                                       Integer accessTypeMaxVersion) {
        organisationProfileIds.forEach(organisationProfileId -> profileRefreshQueueRepository.setActiveFalse(
                organisationProfileId,
                accessTypeMaxVersion
        ));
    }
}
