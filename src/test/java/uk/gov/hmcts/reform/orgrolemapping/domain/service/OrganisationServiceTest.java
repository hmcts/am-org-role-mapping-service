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

        OrganisationInfo organisationInfo = buildOrganisationInfo(1);
        OrganisationByProfileIdsResponse response =
                buildOrganisationByProfileIdsResponse(organisationInfo, "123", false);

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

    @Test
    void findAndInsertStaleOrganisationsIntoRefreshQueue_NullResponseTest() {
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
                .insertIntoOrganisationRefreshQueue(any(), any(), any());
        verify(profileRefreshQueueRepository, times(0))
                .setActiveFalse(any(), any());
    }

    @Test
    void findAndInsertStaleOrganisationsIntoRefreshQueue_EmptyOrgInfoTest() {
        ProfileRefreshQueueEntity profileRefreshQueueEntity =
                buildProfileRefreshQueueEntity("SOLICITOR_PROFILE", 1, true);

        when(profileRefreshQueueRepository.getActiveProfileEntities()).thenReturn(List.of(profileRefreshQueueEntity));
        when(prdService.fetchOrganisationsByProfileIds(any(), eq(null), any()))
                .thenReturn(ResponseEntity.ok(null));

        organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();

        verify(profileRefreshQueueRepository, times(1))
                .getActiveProfileEntities();
        verify(organisationRefreshQueueRepository, times(0))
                .insertIntoOrganisationRefreshQueue(any(), any(), any());
        verify(profileRefreshQueueRepository, times(0))
                .setActiveFalse(any(), any());
    }

    private OrganisationInfo buildOrganisationInfo(int i) {
        return OrganisationInfo.builder()
                .organisationIdentifier("" + i)
                .status("ACTIVE")
                .lastUpdated(LocalDateTime.now())
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
