package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
class OrganisationRefreshQueueRepositoryIntegrationTest extends BaseTestIntegration {

    @Autowired
    private OrganisationRefreshQueueRepository organisationRefreshQueueRepository;

    @Test
    public void shouldInsertIntoOrganisationRefreshQueue() {
        LocalDateTime time = LocalDateTime.of(2024, 2, 7, 12, 0, 0);

        organisationRefreshQueueRepository.upsertToOrganisationRefreshQueue("123", time, 1);

        List<OrganisationRefreshQueueEntity> organisationEntities = organisationRefreshQueueRepository.findAll();
        OrganisationRefreshQueueEntity organisationEntity = organisationEntities.get(0);

        assertEquals(organisationEntity.getOrganisationId(), "123");
        assertEquals(organisationEntity.getLastUpdated(), time);
        assertEquals(organisationEntity.getAccessTypesMinVersion(), 1);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/insert_organisation_profiles.sql"
    })
    public void shouldHandleInsertConflictIntoOrganisationRefreshQueue() {
        LocalDateTime time = LocalDateTime.of(2024, 2, 7, 12, 0, 0);

        organisationRefreshQueueRepository.upsertToOrganisationRefreshQueue("123", time, 2);

        List<OrganisationRefreshQueueEntity> organisationEntities = organisationRefreshQueueRepository.findAll();
        OrganisationRefreshQueueEntity organisationEntity = organisationEntities.get(0);

        assertEquals(organisationEntity.getOrganisationId(), "123");
        assertEquals(organisationEntity.getLastUpdated(), time);
        assertEquals(organisationEntity.getAccessTypesMinVersion(), 2);
    }
}
