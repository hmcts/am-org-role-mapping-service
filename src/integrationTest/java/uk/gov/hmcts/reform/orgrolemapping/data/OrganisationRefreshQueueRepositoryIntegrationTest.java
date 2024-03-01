package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildOrganisationInfo;

@Transactional
class OrganisationRefreshQueueRepositoryIntegrationTest extends BaseTestIntegration {

    @Autowired
    private OrganisationRefreshQueueRepository organisationRefreshQueueRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    public void shouldInsertIntoOrganisationRefreshQueue() {
        List<OrganisationInfo> organisationInfoList = List.of(buildOrganisationInfo(123));

        organisationRefreshQueueRepository.upsertToOrganisationRefreshQueue(jdbcTemplate, organisationInfoList, 1);

        List<OrganisationRefreshQueueEntity> organisationEntities = organisationRefreshQueueRepository.findAll();
        OrganisationRefreshQueueEntity organisationEntity = organisationEntities.get(0);

        assertEquals("123", organisationEntity.getOrganisationId());
        assertEquals(1, organisationEntity.getAccessTypesMinVersion());
        assertEquals(0, organisationEntity.getRetry());
        assertNotNull(organisationEntity.getOrganisationLastUpdated());
        assertNotNull(organisationEntity.getLastUpdated());
        assertNotNull(organisationEntity.getRetryAfter());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/insert_organisation_profiles.sql"
    })
    public void shouldHandleInsertConflictIntoOrganisationRefreshQueue() {
        List<OrganisationInfo> organisationInfoList = List.of(buildOrganisationInfo(123));

        organisationRefreshQueueRepository.upsertToOrganisationRefreshQueue(jdbcTemplate, organisationInfoList, 1);

        List<OrganisationRefreshQueueEntity> organisationEntities = organisationRefreshQueueRepository.findAll();
        OrganisationRefreshQueueEntity organisationEntity = organisationEntities.get(0);

        assertEquals("123", organisationEntity.getOrganisationId());
        assertEquals(1, organisationEntity.getAccessTypesMinVersion());
        assertEquals(0, organisationEntity.getRetry());
        assertNotNull(organisationEntity.getOrganisationLastUpdated());
        assertNotNull(organisationEntity.getLastUpdated());
        assertNotNull(organisationEntity.getRetryAfter());
    }
}
