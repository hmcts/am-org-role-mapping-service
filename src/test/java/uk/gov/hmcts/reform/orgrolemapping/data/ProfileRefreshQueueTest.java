package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ProfileRefreshQueueTest {

    private final ProfileRefreshQueueRepository profileRefreshQueueRepository
            = mock(ProfileRefreshQueueRepository.class);

    @Test
    void getProfileRefreshQueueById() {
        ProfileRefreshQueue profileRefreshQueue = ProfileRefreshQueue.builder()
                .organisationProfileId("NEW")
                .accessTypesMinVersion(1).active(false).build();
        Mockito.when(profileRefreshQueueRepository.findByOrganisationProfileId("NEW"))
                .thenReturn(profileRefreshQueue);

        Mockito.when(profileRefreshQueueRepository.save(profileRefreshQueue))
                .thenReturn(profileRefreshQueue);

        assertNotNull(profileRefreshQueue.getAccessTypesMinVersion());
        assertEquals("NEW", profileRefreshQueue.getOrganisationProfileId());
    }

    @Test
    void getProfileRefreshQueueByIdAndVersion() {
        ProfileRefreshQueue profileRefreshQueue = ProfileRefreshQueue.builder().organisationProfileId("NEW")
                .accessTypesMinVersion(1).active(false).build();
        Mockito.when(profileRefreshQueueRepository.findByOrganisationProfileIdAndAccessTypesMinVersion("NEW",
                        1L)).thenReturn(profileRefreshQueue);

        assertNotNull(profileRefreshQueue.getAccessTypesMinVersion());
        assertEquals("NEW", profileRefreshQueue.getOrganisationProfileId());
    }
}
