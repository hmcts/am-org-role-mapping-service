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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationStaleProfilesResponse;

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
}
