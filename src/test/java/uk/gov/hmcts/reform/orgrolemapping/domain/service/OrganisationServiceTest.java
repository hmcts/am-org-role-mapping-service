package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationStaleProfilesResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
    private final AccessTypesRepository accessTypesRepository = Mockito.mock(AccessTypesRepository.class);
    private final BatchLastRunTimestampRepository batchLastRunTimestampRepository =
            Mockito.mock(BatchLastRunTimestampRepository.class);
    OrganisationService organisationService = new
            OrganisationService(prdService, organisationRefreshQueueRepository, profileRefreshQueueRepository,
            accessTypesRepository, batchLastRunTimestampRepository, "1", "100");

    @Test
    void findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueTest() {
        ProfileRefreshQueueEntity profileRefreshQueueEntity = new ProfileRefreshQueueEntity();
        profileRefreshQueueEntity.setOrganisationProfileId("SOLICITOR_PROFILE");
        profileRefreshQueueEntity.setAccessTypesMinVersion(1);
        profileRefreshQueueEntity.setActive(true);

        when(profileRefreshQueueRepository.getActiveProfileEntities()).thenReturn(List.of(profileRefreshQueueEntity));

        OrganisationStaleProfilesResponse response = new OrganisationStaleProfilesResponse();
        OrganisationInfo organisationInfo = OrganisationInfo.builder()
                .organisationIdentifier("123")
                .status("ACTIVE")
                .lastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE"))
                .build();
        response.setOrganisationInfo(List.of(organisationInfo));
        response.setLastRecordInPage("123");
        response.setMoreAvailable(false);

        when(prdService.fetchOrganisationsWithStaleProfiles(any(), eq(null), any()))
                .thenReturn(ResponseEntity.ok(response));

        organisationService.findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueue();

        verify(profileRefreshQueueRepository, times(1))
                .getActiveProfileEntities();
        verify(organisationRefreshQueueRepository, times(1))
                .insertIntoOrganisationRefreshQueue(any(), any(), any());
        verify(profileRefreshQueueRepository, times(1))
                .setActiveFalse(any(), any());
    }

    @Test
    void findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueMoreAvailableTest() {
        ProfileRefreshQueueEntity profileRefreshQueueEntity = new ProfileRefreshQueueEntity();
        profileRefreshQueueEntity.setOrganisationProfileId("SOLICITOR_PROFILE");
        profileRefreshQueueEntity.setAccessTypesMinVersion(1);
        profileRefreshQueueEntity.setActive(true);

        when(profileRefreshQueueRepository.getActiveProfileEntities()).thenReturn(List.of(profileRefreshQueueEntity));

        OrganisationStaleProfilesResponse response = new OrganisationStaleProfilesResponse();
        OrganisationInfo organisationInfo = OrganisationInfo.builder()
                .organisationIdentifier("123")
                .status("ACTIVE")
                .lastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE"))
                .build();
        response.setOrganisationInfo(List.of(organisationInfo));
        response.setLastRecordInPage("123");
        response.setMoreAvailable(true);

        when(prdService.fetchOrganisationsWithStaleProfiles(any(), eq(null), any()))
                .thenReturn(ResponseEntity.ok(response));

        OrganisationStaleProfilesResponse response2 = new OrganisationStaleProfilesResponse();
        OrganisationInfo organisationInfo2 = OrganisationInfo.builder()
                .organisationIdentifier("456")
                .status("ACTIVE")
                .lastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE"))
                .build();
        response2.setOrganisationInfo(List.of(organisationInfo2));
        response2.setLastRecordInPage("456");
        response2.setMoreAvailable(false);

        when(prdService.fetchOrganisationsWithStaleProfiles(any(), any(String.class), any()))
                .thenReturn(ResponseEntity.ok(response2));

        organisationService.findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueue();

        verify(profileRefreshQueueRepository, times(1))
                .getActiveProfileEntities();
        verify(organisationRefreshQueueRepository, times(2))
                .insertIntoOrganisationRefreshQueue(any(), any(), any());
        verify(profileRefreshQueueRepository, times(1))
                .setActiveFalse(any(), any());
    }

    @Test
    void findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueNoActiveOrganisationTest() {
        when(profileRefreshQueueRepository.getActiveProfileEntities()).thenReturn(Collections.emptyList());

        organisationService.findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueue();

        verify(profileRefreshQueueRepository, times(1))
                .getActiveProfileEntities();
        verify(organisationRefreshQueueRepository, times(0))
                .insertIntoOrganisationRefreshQueue(any(), any(), any());
        verify(profileRefreshQueueRepository, times(0))
                .setActiveFalse(any(), any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void findOrganisationChangesAndInsertIntoOrganisationRefreshQueue() {
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

        ResponseEntity<OrganisationProfilesResponse> organisationProfiles1 = ResponseEntity
                .ok(OrganisationProfilesResponse.builder().organisations(allOrgs1).moreAvailable(true).build());
        ResponseEntity<OrganisationProfilesResponse> organisationProfiles2 = ResponseEntity
                .ok(OrganisationProfilesResponse.builder().organisations(allOrgs2).moreAvailable(false).build());

        when(prdService.retrieveOrganisations(isNull(), anyString(), isNull(), anyInt(), anyInt()))
                .thenReturn(organisationProfiles1, organisationProfiles2);

        organisationService.findOrganisationChangesAndInsertIntoOrganisationRefreshQueue();

        verify(organisationRefreshQueueRepository, times(3))
                .insertIntoOrganisationRefreshQueueForLastUpdated(anyString(), any(LocalDateTime.class), anyInt());
        verify(batchLastRunTimestampRepository, times(1)).save(any(BatchLastRunTimestampEntity.class));
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
    }

    private OrganisationInfo buildOrganisationInfo(int i) {
        return OrganisationInfo.builder()
                .organisationIdentifier("" + i)
                .status("ACTIVE")
                .lastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE"))
                .build();
    }

}
