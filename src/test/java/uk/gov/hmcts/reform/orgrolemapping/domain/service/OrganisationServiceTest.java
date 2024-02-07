package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class OrganisationServiceTest {

    private final PrdService prdService = Mockito.mock(PrdService.class);
    private final ProfileRefreshQueueRepository profileRefreshQueueRepository =
            Mockito.mock(ProfileRefreshQueueRepository.class);
    private final OrganisationRefreshQueueRepository organisationRefreshQueueRepository =
            Mockito.mock(OrganisationRefreshQueueRepository.class);
    OrganisationService organisationService = new
            OrganisationService(prdService, organisationRefreshQueueRepository, profileRefreshQueueRepository, "1");

    @Test
    void findAndInsertStaleOrganisationsIntoRefreshQueue_Test() {
        ProfileRefreshQueueEntity profileRefreshQueueEntity = ProfileRefreshQueueEntity.builder()
                .organisationProfileId("SOLICITOR_PROFILE")
                .accessTypesMinVersion(1)
                .active(true).build();

        when(profileRefreshQueueRepository.getActiveProfileEntities()).thenReturn(List.of(profileRefreshQueueEntity));

        OrganisationInfo organisationInfo = OrganisationInfo.builder()
                .organisationIdentifier("123")
                .status("ACTIVE")
                .lastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE")).build();

        OrganisationByProfileIdsResponse response = OrganisationByProfileIdsResponse.builder()
                .organisationInfo(List.of(organisationInfo))
                .lastRecordInPage("123")
                .moreAvailable(false).build();

        when(prdService.fetchOrganisationsByProfileIds(any(), eq(null), any()))
                .thenReturn(ResponseEntity.ok(response));

        organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();

        verify(profileRefreshQueueRepository, times(1))
                .getActiveProfileEntities();
        verify(organisationRefreshQueueRepository, times(1))
                .insertIntoOrganisationRefreshQueue(any(), any(), any());
        verify(profileRefreshQueueRepository, times(1))
                .setActiveFalse(any(), any());
    }

    @Test
    void findAndInsertStaleOrganisationsIntoRefreshQueue_WithPaginationTest() {
        ProfileRefreshQueueEntity profileRefreshQueueEntity = ProfileRefreshQueueEntity.builder()
                .organisationProfileId("SOLICITOR_PROFILE")
                .accessTypesMinVersion(1)
                .active(true).build();

        when(profileRefreshQueueRepository.getActiveProfileEntities()).thenReturn(List.of(profileRefreshQueueEntity));

        OrganisationInfo organisationInfo = OrganisationInfo.builder()
                .organisationIdentifier("123")
                .status("ACTIVE")
                .lastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE")).build();

        OrganisationByProfileIdsResponse page1 = OrganisationByProfileIdsResponse.builder()
                .organisationInfo(List.of(organisationInfo))
                .lastRecordInPage("123")
                .moreAvailable(true).build();

        when(prdService.fetchOrganisationsByProfileIds(any(), eq(null), any()))
                .thenReturn(ResponseEntity.ok(page1));

        OrganisationInfo organisationInfo2 = OrganisationInfo.builder()
                .organisationIdentifier("456")
                .status("ACTIVE")
                .lastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE")).build();

        OrganisationByProfileIdsResponse page2 = OrganisationByProfileIdsResponse.builder()
                .organisationInfo(List.of(organisationInfo2))
                .lastRecordInPage("456")
                .moreAvailable(false).build();

        when(prdService.fetchOrganisationsByProfileIds(any(), any(String.class), any()))
                .thenReturn(ResponseEntity.ok(page2));

        organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();

        verify(profileRefreshQueueRepository, times(1))
                .getActiveProfileEntities();
        verify(organisationRefreshQueueRepository, times(2))
                .insertIntoOrganisationRefreshQueue(any(), any(), any());
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
                .insertIntoOrganisationRefreshQueue(any(), any(), any());
        verify(profileRefreshQueueRepository, times(0))
                .setActiveFalse(any(), any());
    }
}
