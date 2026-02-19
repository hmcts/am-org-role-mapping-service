package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
class ProfileRefreshQueueEntityTest {

    @Test
    void getProfileRefreshQueueById() {
        ProfileRefreshQueueEntity profileRefreshQueueEntity = ProfileRefreshQueueEntity.builder()
                .organisationProfileId("NEW")
                .accessTypesMinVersion(1).active(false).build();

        assertNotNull(profileRefreshQueueEntity.getAccessTypesMinVersion());
        assertEquals("NEW", profileRefreshQueueEntity.getOrganisationProfileId());
    }

}
