package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
class ProfileRefreshQueueRepositoryIntegrationTest extends BaseTestIntegration {

    @Autowired
    private ProfileRefreshQueueRepository profileRefreshQueueRepository;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/insert_profile_refresh_queue.sql"
    })
    public void shouldGetActiveProfilesOnly() {
        List<ProfileRefreshQueueEntity> activeProfiles = profileRefreshQueueRepository.getActiveProfileEntities();

        assertEquals(1, activeProfiles.size());
        activeProfiles.forEach(profile -> assertEquals(true, profile.getActive()));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/insert_profile_refresh_queue.sql"
    })
    public void shouldSetActiveProfilesToFalse() {
        profileRefreshQueueRepository.setActiveFalse("SOLICITOR_ORG", 1);

        List<ProfileRefreshQueueEntity> activeProfiles = profileRefreshQueueRepository.getActiveProfileEntities();
        assertEquals(0, activeProfiles.size());
    }
}
