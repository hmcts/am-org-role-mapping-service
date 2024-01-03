package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
class ProfileRefreshQueueTest {

    @Test
    void getProfileRefreshQueueById() {
        ProfileRefreshQueue profileRefreshQueue = ProfileRefreshQueue.builder()
                .organisationProfileId("NEW")
                .accessTypesMinVersion(1).active(false).build();

        assertNotNull(profileRefreshQueue.getAccessTypesMinVersion());
        assertEquals("NEW", profileRefreshQueue.getOrganisationProfileId());
    }

    @Test
    void getProfileRefreshQueueByIdAndVersion() {
        ProfileRefreshQueue profileRefreshQueue = ProfileRefreshQueue.builder().organisationProfileId("NEW")
                .accessTypesMinVersion(1).active(false).build();

        assertNotNull(profileRefreshQueue.getAccessTypesMinVersion());
        assertEquals("NEW", profileRefreshQueue.getOrganisationProfileId());
    }
}
