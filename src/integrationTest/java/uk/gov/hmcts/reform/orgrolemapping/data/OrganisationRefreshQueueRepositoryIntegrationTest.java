package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildOrganisationInfo;

class OrganisationRefreshQueueRepositoryIntegrationTest extends BaseTestIntegration {

    private static final String RETRY_INTERVAL_1 = "100";
    private static final String RETRY_INTERVAL_2 = "200";
    private static final String RETRY_INTERVAL_3 = "300";

    @Autowired
    private OrganisationRefreshQueueRepository organisationRefreshQueueRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    public void shouldInsertIntoOrganisationRefreshQueue() {
        List<OrganisationInfo> organisationInfoList = List.of(buildOrganisationInfo(123));

        organisationRefreshQueueRepository.upsertToOrganisationRefreshQueue(
                jdbcTemplate, organisationInfoList, 1);

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

        organisationRefreshQueueRepository.upsertToOrganisationRefreshQueue(
                jdbcTemplate, organisationInfoList, 1);

        List<OrganisationRefreshQueueEntity> organisationEntities = organisationRefreshQueueRepository.findAll();
        OrganisationRefreshQueueEntity organisationEntity = organisationEntities.get(0);

        assertEquals("123", organisationEntity.getOrganisationId());
        assertEquals(1, organisationEntity.getAccessTypesMinVersion());
        assertEquals(0, organisationEntity.getRetry());
        assertNotNull(organisationEntity.getLastUpdated());
        assertNotNull(organisationEntity.getOrganisationLastUpdated());
        assertNotNull(organisationEntity.getRetryAfter());
    }

    @Test
    void shouldHandleConflict_whenUpsertingToOrganisationRefreshQueue() {
        // Arrange
        List<OrganisationInfo> organisationInfoList = List.of(buildOrganisationInfo(123));
        organisationRefreshQueueRepository.upsertToOrganisationRefreshQueueForLastUpdated(
                jdbcTemplate, organisationInfoList, 1);

        // Act
        organisationInfoList.forEach(
                organisationInfo -> organisationInfo.setOrganisationLastUpdated(LocalDateTime.now())
        );
        organisationRefreshQueueRepository.upsertToOrganisationRefreshQueueForLastUpdated(
                jdbcTemplate, organisationInfoList, 2);

        // Assert
        List<OrganisationRefreshQueueEntity> organisationEntities = organisationRefreshQueueRepository.findAll();
        assertEquals(1, organisationEntities.size());
        OrganisationRefreshQueueEntity organisationEntity = organisationEntities.get(0);
        assertEquals("123", organisationEntity.getOrganisationId());
        assertEquals(2, organisationEntity.getAccessTypesMinVersion());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/insert_organisation_profiles.sql"
    })
    void shouldUpdateRetry_whenRetry0() {
        // GIVEN
        List<OrganisationRefreshQueueEntity> organisationEntities = organisationRefreshQueueRepository.findAll();
        String orgId = organisationEntities.get(0).getOrganisationId();
        assertEquals(0, organisationEntities.get(0).getRetry());

        // WHEN
        organisationRefreshQueueRepository.updateRetry(orgId, RETRY_INTERVAL_1, RETRY_INTERVAL_2, RETRY_INTERVAL_3);

        // THEN
        OrganisationRefreshQueueEntity updatedEntity = organisationRefreshQueueRepository.findById(orgId).orElseThrow();
        assertEquals(1, updatedEntity.getRetry());
        // check retry time between now and interval 2 (i.e. because it is now at retry 1)
        assertTrue(
            updatedEntity.getRetryAfter().isAfter(LocalDateTime.now())
        );
        assertTrue(
            updatedEntity.getRetryAfter().isBefore(LocalDateTime.now().plusMinutes(Long.parseLong(RETRY_INTERVAL_2)))
        );
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation_profiles_retry_1.sql"
    })
    void shouldUpdateRetry_whenRetry1() {
        // GIVEN
        List<OrganisationRefreshQueueEntity> organisationEntities = organisationRefreshQueueRepository.findAll();
        String orgId = organisationEntities.get(0).getOrganisationId();
        assertEquals(1, organisationEntities.get(0).getRetry());

        // WHEN
        organisationRefreshQueueRepository.updateRetry(orgId, RETRY_INTERVAL_1, RETRY_INTERVAL_2, RETRY_INTERVAL_3);

        // THEN
        OrganisationRefreshQueueEntity updatedEntity = organisationRefreshQueueRepository.findById(orgId).orElseThrow();
        assertEquals(2, updatedEntity.getRetry());
        // check retry time between interval 1 and interval 3 (i.e. because it is now at retry 2)
        assertTrue(
            updatedEntity.getRetryAfter().isAfter(LocalDateTime.now().plusMinutes(Long.parseLong(RETRY_INTERVAL_1)))
        );
        assertTrue(
            updatedEntity.getRetryAfter().isBefore(LocalDateTime.now().plusMinutes(Long.parseLong(RETRY_INTERVAL_3)))
        );
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation_profiles_retry_2.sql"
    })
    void shouldUpdateRetry_whenRetry2() {
        // GIVEN
        List<OrganisationRefreshQueueEntity> organisationEntities = organisationRefreshQueueRepository.findAll();
        String orgId = organisationEntities.get(0).getOrganisationId();
        assertEquals(2, organisationEntities.get(0).getRetry());

        // WHEN
        organisationRefreshQueueRepository.updateRetry(orgId, RETRY_INTERVAL_1, RETRY_INTERVAL_2, RETRY_INTERVAL_3);

        // THEN
        OrganisationRefreshQueueEntity updatedEntity = organisationRefreshQueueRepository.findById(orgId).orElseThrow();
        assertEquals(3, updatedEntity.getRetry());
        // check retry time between interval 2 and 400 minutes (i.e. because it is now at retry 3)
        assertTrue(
            updatedEntity.getRetryAfter().isAfter(LocalDateTime.now().plusMinutes(Long.parseLong(RETRY_INTERVAL_2)))
        );
        assertTrue(
            updatedEntity.getRetryAfter().isBefore(LocalDateTime.now().plusMinutes(400L))
        );
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation_profiles_retry_3.sql"
    })
    void shouldUpdateRetry_whenRetry3() {

        // GIVEN
        List<OrganisationRefreshQueueEntity> organisationEntities = organisationRefreshQueueRepository.findAll();
        String orgId = organisationEntities.get(0).getOrganisationId();
        assertEquals(3, organisationEntities.get(0).getRetry());

        // WHEN
        organisationRefreshQueueRepository.updateRetry(orgId, RETRY_INTERVAL_1, RETRY_INTERVAL_2, RETRY_INTERVAL_3);

        // THEN
        OrganisationRefreshQueueEntity updatedEntity = organisationRefreshQueueRepository.findById(orgId).orElseThrow();
        assertEquals(4, updatedEntity.getRetry());
        // check retry time cleared (i.e. because it is now at retry 4)
        assertNull(updatedEntity.getRetryAfter());
    }

}
