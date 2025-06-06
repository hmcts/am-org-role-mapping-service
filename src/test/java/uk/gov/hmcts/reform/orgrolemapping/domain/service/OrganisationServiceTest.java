package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationsResponse;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganisationServiceTest {

    private final PrdService prdService = Mockito.mock(PrdService.class);
    private final ProfileRefreshQueueRepository profileRefreshQueueRepository =
            Mockito.mock(ProfileRefreshQueueRepository.class);
    private final OrganisationRefreshQueueRepository organisationRefreshQueueRepository =
            Mockito.mock(OrganisationRefreshQueueRepository.class);
    private final NamedParameterJdbcTemplate jdbcTemplate =
            Mockito.mock(NamedParameterJdbcTemplate.class);
    private final AccessTypesRepository accessTypesRepository = mock(AccessTypesRepository.class);
    private final DatabaseDateTimeRepository databaseDateTimeRepository = mock(DatabaseDateTimeRepository.class);
    private final BatchLastRunTimestampRepository batchLastRunTimestampRepository =
            mock(BatchLastRunTimestampRepository.class);
    private final ProcessEventTracker processEventTracker = Mockito.mock(ProcessEventTracker.class);

    @Captor
    private ArgumentCaptor<ProcessMonitorDto> processMonitorDtoArgumentCaptor;

    OrganisationService organisationService = new OrganisationService(
            prdService,
            organisationRefreshQueueRepository,
            profileRefreshQueueRepository,
            "1",
            jdbcTemplate,
            accessTypesRepository, batchLastRunTimestampRepository, databaseDateTimeRepository,
            processEventTracker, "10"
    );

    @Test
    void findAndInsertStaleOrganisationsIntoRefreshQueue_Test() {
        ProfileRefreshQueueEntity profileRefreshQueueEntity =
                buildProfileRefreshQueueEntity("SOLICITOR_PROFILE", 1, true);

        when(profileRefreshQueueRepository.getActiveProfileEntities()).thenReturn(List.of(profileRefreshQueueEntity));

        OrganisationInfo organisationInfo = buildOrganisationInfo(1);
        OrganisationByProfileIdsResponse response =
                buildOrganisationByProfileIdsResponse(organisationInfo, "123", false);

        when(prdService.fetchOrganisationsByProfileIds(any(), eq(null), any()))
                .thenReturn(ResponseEntity.ok(response));

        organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();

        verify(profileRefreshQueueRepository, times(1))
                .getActiveProfileEntities();
        verify(organisationRefreshQueueRepository, times(1))
                .upsertToOrganisationRefreshQueue(any(), any(), any(), eq("P2"));
        verify(profileRefreshQueueRepository, times(1))
                .setActiveFalse(any(), any());
    }

    @Test
    void findAndInsertStaleOrganisationsIntoRefreshQueue_WithPaginationTest() {
        ProfileRefreshQueueEntity profileRefreshQueueEntity =
                buildProfileRefreshQueueEntity("SOLICITOR_PROFILE", 1, true);

        when(profileRefreshQueueRepository.getActiveProfileEntities()).thenReturn(List.of(profileRefreshQueueEntity));

        OrganisationInfo organisationInfo = buildOrganisationInfo(1);
        OrganisationByProfileIdsResponse page1 =
                buildOrganisationByProfileIdsResponse(organisationInfo, "123", true);

        when(prdService.fetchOrganisationsByProfileIds(any(), eq(null), any()))
                .thenReturn(ResponseEntity.ok(page1));

        OrganisationInfo organisationInfo2 = buildOrganisationInfo(2);
        OrganisationByProfileIdsResponse page2 =
                buildOrganisationByProfileIdsResponse(organisationInfo2, "456", false);

        when(prdService.fetchOrganisationsByProfileIds(any(), any(String.class), any()))
                .thenReturn(ResponseEntity.ok(page2));

        organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();

        verify(profileRefreshQueueRepository, times(1))
                .getActiveProfileEntities();
        verify(organisationRefreshQueueRepository, times(2))
                .upsertToOrganisationRefreshQueue(any(), any(), any(), eq("P2"));
        verify(profileRefreshQueueRepository, times(1))
                .setActiveFalse(any(), any());
    }

    @Test
    void findAndInsertStaleOrganisationsIntoRefreshQueue_NoActiveOrganisationsTest() {
        when(profileRefreshQueueRepository.getActiveProfileEntities()).thenReturn(Collections.emptyList());

        organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();

        verify(profileRefreshQueueRepository, times(1))
                .getActiveProfileEntities();
        verify(organisationRefreshQueueRepository, times(0))
                .upsertToOrganisationRefreshQueue(any(), any(), any(), eq("P2"));
        verify(profileRefreshQueueRepository, times(0))
                .setActiveFalse(any(), any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void findOrganisationChangesAndInsertIntoOrganisationRefreshQueue() {
        DatabaseDateTime databaseDateTime = mock(DatabaseDateTime.class);
        when(databaseDateTime.getDate()).thenReturn(mock(Instant.class));
        when(databaseDateTimeRepository.getCurrentTimeStamp()).thenReturn(databaseDateTime);
        List<AccessTypesEntity> allAccessTypes = new ArrayList<>();
        allAccessTypes.add(new AccessTypesEntity(1L, "some json"));
        when(accessTypesRepository.findAll()).thenReturn(allAccessTypes);

        List<BatchLastRunTimestampEntity> allBatches = new ArrayList<>();
        allBatches.add(new BatchLastRunTimestampEntity(1L, LocalDateTime.of(2023, 12, 30, 0, 0, 0, 0),
                LocalDateTime.of(2023, 12, 31, 12, 34, 56, 789)));
        when(batchLastRunTimestampRepository.findAll()).thenReturn(allBatches);
        List<OrganisationInfo> allOrgs1 = new ArrayList<>();
        OrganisationInfo organisationInfo1 = buildOrganisationInfo(1);
        OrganisationInfo organisationInfo2 = buildOrganisationInfo(2);
        allOrgs1.add(organisationInfo1);
        allOrgs1.add(organisationInfo2);

        List<OrganisationInfo> allOrgs2 = new ArrayList<>();
        OrganisationInfo organisationInfo3 = buildOrganisationInfo(3);
        allOrgs2.add(organisationInfo3);

        ResponseEntity<OrganisationsResponse> organisationsResponse1 = ResponseEntity
                .ok(OrganisationsResponse.builder().organisations(allOrgs1).moreAvailable(true).build());
        ResponseEntity<OrganisationsResponse> organisationsResponse2 = ResponseEntity
                .ok(OrganisationsResponse.builder().organisations(allOrgs2).moreAvailable(false).build());

        when(prdService.retrieveOrganisations(anyString(), anyInt(), anyInt()))
                .thenReturn(organisationsResponse1, organisationsResponse2);

        organisationService.findOrganisationChangesAndInsertIntoOrganisationRefreshQueue();

        verify(organisationRefreshQueueRepository, times(2))
                .upsertToOrganisationRefreshQueue(any(), any(), any(), eq("P3"));
        verify(batchLastRunTimestampRepository, times(1)).save(any(BatchLastRunTimestampEntity.class));
        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps()).hasSize(2);
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(0))
                .isEqualTo("attempting upsertToOrganisationRefreshQueue for "
                        + "2 organisations=orgIdentifier1,orgIdentifier2, : COMPLETED");
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(1))
                .isEqualTo("attempting upsertToOrganisationRefreshQueue for "
                        + "1 organisations=orgIdentifier3, : COMPLETED");
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.SUCCESS);
    }

    @SuppressWarnings("unchecked")
    @Test
    void findOrganisationChangesAndInsertIntoOrganisationRefreshQueueFailedInsert() {
        List<AccessTypesEntity> allAccessTypes = new ArrayList<>();
        allAccessTypes.add(new AccessTypesEntity(1L, "some json"));
        when(accessTypesRepository.findAll()).thenReturn(allAccessTypes);

        List<BatchLastRunTimestampEntity> allBatches = new ArrayList<>();
        allBatches.add(new BatchLastRunTimestampEntity(1L, LocalDateTime.of(2023, 12, 30, 0, 0, 0, 0),
                LocalDateTime.of(2023, 12, 31, 12, 34, 56, 789)));
        when(batchLastRunTimestampRepository.findAll()).thenReturn(allBatches);
        List<OrganisationInfo> allOrgs1 = new ArrayList<>();
        OrganisationInfo organisationInfo1 = buildOrganisationInfo(1);
        OrganisationInfo organisationInfo2 = buildOrganisationInfo(2);
        allOrgs1.add(organisationInfo1);
        allOrgs1.add(organisationInfo2);

        List<OrganisationInfo> allOrgs2 = new ArrayList<>();
        OrganisationInfo organisationInfo3 = buildOrganisationInfo(3);
        allOrgs2.add(organisationInfo3);

        ResponseEntity<OrganisationsResponse> organisationsResponse1 = ResponseEntity
                .ok(OrganisationsResponse.builder().organisations(allOrgs1).moreAvailable(true).build());
        ResponseEntity<OrganisationsResponse> organisationsResponse2 = ResponseEntity
                .ok(OrganisationsResponse.builder().organisations(allOrgs2).moreAvailable(false).build());

        when(prdService.retrieveOrganisations(anyString(), anyInt(), anyInt()))
                .thenReturn(organisationsResponse1, organisationsResponse2);

        doThrow(new ServiceException("Insert exception")).when(organisationRefreshQueueRepository)
                .upsertToOrganisationRefreshQueue(any(), any(), any(), eq("P3"));

        Assertions.assertThrows(ServiceException.class, () ->
            organisationService.findOrganisationChangesAndInsertIntoOrganisationRefreshQueue()
        );

        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.FAILED);
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndDetail())
                .isEqualTo("Insert exception, failed at page 1");
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps()).hasSize(1);
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(0))
                .isEqualTo("attempting upsertToOrganisationRefreshQueue "
                        + "for 2 organisations=orgIdentifier1,orgIdentifier2,");
    }

    @SuppressWarnings("unchecked")
    @Test
    void findOrganisationChangesAndInsertIntoOrganisationRefreshQueueFailedRetrieveOrgs() {
        List<AccessTypesEntity> allAccessTypes = new ArrayList<>();
        allAccessTypes.add(new AccessTypesEntity(1L, "some json"));
        when(accessTypesRepository.findAll()).thenReturn(allAccessTypes);

        List<BatchLastRunTimestampEntity> allBatches = new ArrayList<>();
        allBatches.add(new BatchLastRunTimestampEntity(1L, LocalDateTime.of(2023, 12, 30, 0, 0, 0, 0),
                LocalDateTime.of(2023, 12, 31, 12, 34, 56, 789)));
        when(batchLastRunTimestampRepository.findAll()).thenReturn(allBatches);
        List<OrganisationInfo> allOrgs1 = new ArrayList<>();
        OrganisationInfo organisationInfo1 = buildOrganisationInfo(1);
        OrganisationInfo organisationInfo2 = buildOrganisationInfo(2);
        allOrgs1.add(organisationInfo1);
        allOrgs1.add(organisationInfo2);

        List<OrganisationInfo> allOrgs2 = new ArrayList<>();
        OrganisationInfo organisationInfo3 = buildOrganisationInfo(3);
        allOrgs2.add(organisationInfo3);

        ResponseEntity<OrganisationsResponse> organisationsResponse1 = ResponseEntity
                .ok(OrganisationsResponse.builder().organisations(allOrgs1).moreAvailable(true).build());
        ResponseEntity<OrganisationsResponse> organisationsResponse2 = ResponseEntity
                .ok(OrganisationsResponse.builder().organisations(allOrgs2).moreAvailable(false).build());

        when(prdService.retrieveOrganisations(anyString(), anyInt(), anyInt()))
                .thenReturn(organisationsResponse1, organisationsResponse2);

        doThrow(new ServiceException("Retrieve exception")).when(prdService)
                .retrieveOrganisations(anyString(), anyInt(), anyInt());

        Assertions.assertThrows(ServiceException.class, () ->
            organisationService.findOrganisationChangesAndInsertIntoOrganisationRefreshQueue()
        );

        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.FAILED);
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndDetail())
                .isEqualTo("Retrieve exception, failed at page 1");
        assertTrue(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().isEmpty());

    }

    @Test
    void findOrganisationChangesAndInsertIntoOrganisationRefreshQueueWithBatchServiceException() {
        List<AccessTypesEntity> allAccessTypes = new ArrayList<>();
        allAccessTypes.add(new AccessTypesEntity(1L, "some json"));
        when(accessTypesRepository.findAll()).thenReturn(allAccessTypes);
        List<BatchLastRunTimestampEntity> allBatches = new ArrayList<>();
        allBatches.add(new BatchLastRunTimestampEntity(1L, LocalDateTime.of(2023, 12, 30, 0, 0, 0, 0),
                LocalDateTime.of(2023, 12, 31, 12, 34, 56, 789)));
        allBatches.add(new BatchLastRunTimestampEntity(2L, LocalDateTime.of(2023, 12, 30, 0, 0, 0, 0),
                LocalDateTime.of(2023, 12, 31, 12, 34, 56, 789)));
        when(batchLastRunTimestampRepository.findAll()).thenReturn(allBatches);

        Assertions.assertThrows(ServiceException.class, () ->
            organisationService.findOrganisationChangesAndInsertIntoOrganisationRefreshQueue()
        );
        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.FAILED);
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndDetail())
                .isEqualTo("Single BatchLastRunTimestampEntity not found");
        assertTrue(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().isEmpty());
    }

    @Test
    void findOrganisationChangesAndInsertIntoOrganisationRefreshQueueWithAccessTypesServiceException() {
        List<AccessTypesEntity> allAccessTypes = new ArrayList<>();
        allAccessTypes.add(new AccessTypesEntity(1L, "some json"));
        allAccessTypes.add(new AccessTypesEntity(2L, "some json"));
        when(accessTypesRepository.findAll()).thenReturn(allAccessTypes);

        Assertions.assertThrows(ServiceException.class, () ->
            organisationService.findOrganisationChangesAndInsertIntoOrganisationRefreshQueue()
        );

        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.FAILED);
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndDetail())
                .isEqualTo("Single AccessTypesEntity not found");
        assertTrue(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().isEmpty());
    }

    @Test
    void findAndInsertStaleOrganisationsIntoRefreshQueue_EmptyOrgInfoTest() {
        ProfileRefreshQueueEntity profileRefreshQueueEntity =
                buildProfileRefreshQueueEntity("SOLICITOR_PROFILE", 1, true);

        when(profileRefreshQueueRepository.getActiveProfileEntities()).thenReturn(List.of(profileRefreshQueueEntity));

        OrganisationByProfileIdsResponse responseWithEmptyOrgInfo = OrganisationByProfileIdsResponse.builder()
                .organisationInfo(Collections.emptyList())
                .lastRecordInPage("123")
                .moreAvailable(true).build();

        when(prdService.fetchOrganisationsByProfileIds(any(), eq(null), any()))
                .thenReturn(ResponseEntity.ok(responseWithEmptyOrgInfo));

        organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();

        verify(profileRefreshQueueRepository, times(1))
                .getActiveProfileEntities();
        verify(organisationRefreshQueueRepository, times(0))
                .upsertToOrganisationRefreshQueue(any(), any(), any(), eq("P2"));
        verify(profileRefreshQueueRepository, times(1))
                .setActiveFalse(any(), any());
    }

    private OrganisationInfo buildOrganisationInfo(int i) {
        return OrganisationInfo.builder()
                .organisationIdentifier("orgIdentifier" + i)
                .status("ACTIVE")
                .organisationLastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE"))
                .build();
    }

    private OrganisationByProfileIdsResponse buildOrganisationByProfileIdsResponse(OrganisationInfo orgInfo,
                                                                                   String lastRecord,
                                                                                   boolean moreAvailable) {
        return OrganisationByProfileIdsResponse.builder()
                .organisationInfo(List.of(orgInfo))
                .lastRecordInPage(lastRecord)
                .moreAvailable(moreAvailable)
                .build();
    }

    private ProfileRefreshQueueEntity buildProfileRefreshQueueEntity(String organisationProfileId,
                                                                     Integer accessTypesMinVersion,
                                                                     boolean active) {
        return ProfileRefreshQueueEntity.builder()
                .organisationProfileId(organisationProfileId)
                .accessTypesMinVersion(accessTypesMinVersion)
                .active(active)
                .build();
    }
}
