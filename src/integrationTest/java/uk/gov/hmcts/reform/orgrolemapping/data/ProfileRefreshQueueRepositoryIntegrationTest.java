package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProfileRefreshQueueRepositoryIntegrationTest extends BaseTestIntegration {

    @Autowired
    private ProfileRefreshQueueRepository profileRefreshQueueRepository;

    @Test
    public void shouldInsertOrganisationProfileIds() {
        List<String> organisationProfileIds = List.of("SOLICITOR_ORG", "DWP_GOV_ORG", "HMRC_GOV_ORG");
        String orgProfileIds = String.join(",", organisationProfileIds);

        profileRefreshQueueRepository.upsertOrganisationProfileIds(orgProfileIds, 1);

        List<ProfileRefreshQueueEntity> profileRefreshQueueEntities = profileRefreshQueueRepository.findAll();
        validateData(profileRefreshQueueEntities, 3, 1);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/insert_organisation_profiles.sql"
    })
    public void shouldHandleDuplicateOrganisationProfileIds() {
        List<ProfileRefreshQueueEntity> profileRefreshQueueEntities = profileRefreshQueueRepository.findAll();
        validateData(profileRefreshQueueEntities, 1, 1);

        List<String> organisationProfileIds = List.of("SOLICITOR_ORG");
        String orgProfileIds = String.join(",", organisationProfileIds);

        profileRefreshQueueRepository.upsertOrganisationProfileIds(orgProfileIds, 2);

        List<ProfileRefreshQueueEntity> newProfileRefreshQueueEntities = profileRefreshQueueRepository.findAll();
        validateData(newProfileRefreshQueueEntities, 1, 2);
    }

    private void validateData(List<ProfileRefreshQueueEntity> profileRefreshQueueEntities,
                                int expectedSize,
                                int expectedAccessTypesMinVersion) {
        assertEquals(expectedSize, profileRefreshQueueEntities.size());
        profileRefreshQueueEntities.forEach(el -> {
            assertNotNull(el.getOrganisationProfileId());
            assertEquals(expectedAccessTypesMinVersion, el.getAccessTypesMinVersion());
        });
    }
}
