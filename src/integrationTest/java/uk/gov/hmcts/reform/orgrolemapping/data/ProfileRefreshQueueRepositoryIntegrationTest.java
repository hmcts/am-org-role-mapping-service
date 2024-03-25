package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
public class ProfileRefreshQueueRepositoryIntegrationTest extends BaseTestIntegration {

    @Autowired
    private ProfileRefreshQueueRepository profileRefreshQueueRepository;

    @Test
    public void shouldInsertOrganisationProfileIds() {
        List<String> organisationProfileIds = List.of("SOLICITOR_ORG", "DWP_GOV_ORG", "HMRC_GOV_ORG");
        String orgProfileIds = String.join(",", organisationProfileIds);

        profileRefreshQueueRepository.upsertOrganisationProfileIds(orgProfileIds, 1L);

        List<ProfileRefreshQueueEntity> profileRefreshQueueEntities = profileRefreshQueueRepository.findAll();
        validateData(profileRefreshQueueEntities, 3, 1);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/insert_organisation_profiles.sql"
    })
    public void shouldHandleDuplicateOrganisationProfileIds() {
        List<String> organisationProfileIds = List.of("SOLICITOR_ORG");
        String orgProfileIds = String.join(",", organisationProfileIds);

        profileRefreshQueueRepository.upsertOrganisationProfileIds(orgProfileIds, 2L);

        List<ProfileRefreshQueueEntity> newProfileRefreshQueueEntities = profileRefreshQueueRepository.findAll();
        validateData(newProfileRefreshQueueEntities, 1, 2);
    }

    private void validateData(List<ProfileRefreshQueueEntity> profileRefreshQueueEntities,
                                int expectedSize,
                                int expectedAccessTypesMinVersion) {
        assertEquals(expectedSize, profileRefreshQueueEntities.size());
        profileRefreshQueueEntities.forEach(profileRefreshQueueEntity -> {
            assertNotNull(profileRefreshQueueEntity.getOrganisationProfileId());
            assertEquals(expectedAccessTypesMinVersion, profileRefreshQueueEntity.getAccessTypesMinVersion());
        });
    }
}
