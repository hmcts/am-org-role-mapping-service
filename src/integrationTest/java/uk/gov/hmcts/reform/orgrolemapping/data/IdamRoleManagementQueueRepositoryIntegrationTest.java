package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
public class IdamRoleManagementQueueRepositoryIntegrationTest extends BaseTestIntegration {

    private static final String USER_ID = "some-user-id";
    private static final String USER_TYPE = "JUDICIAL";
    private static final String PUBLISHED_AS = "User";
    private static final String RETRY_INTERVAL1 = "10";
    private static final String RETRY_INTERVAL2 = "20";
    private static final String RETRY_INTERVAL3 = "30";
    private static final String DATA = """
            {"email_id": "someone@somewhere.com", "roles": [{"role_name": "Role1"}, {"role_name": "Role2"}]}
            """;

    @Autowired
    private IdamRoleManagementQueueRepository idamRoleManagementQueueRepository;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/init_idam_role_management_queue.sql"})
    public void shouldUpsertToIdamRoleManagementQueue() {
        // WHEN
        idamRoleManagementQueueRepository.upsert(USER_ID, USER_TYPE, PUBLISHED_AS, DATA,
                LocalDateTime.now());
        Optional<IdamRoleManagementQueueEntity> idamRoleManagementQueueEntity =
                idamRoleManagementQueueRepository.findById(USER_ID);

        // THEN
        assertIdamRoleManagementQueueEntity(idamRoleManagementQueueEntity,
                USER_ID, USER_TYPE, PUBLISHED_AS, DATA, null, 0);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/init_idam_role_management_queue.sql",
        "classpath:sql/irm/insert_idam_role_management_queue.sql"})
    public void shouldSetAsPublished() {
        // WHEN
        int result = idamRoleManagementQueueRepository.setAsPublished(USER_ID);
        Optional<IdamRoleManagementQueueEntity> idamRoleManagementQueueEntity =
                idamRoleManagementQueueRepository.findById(USER_ID);

        // THEN
        assertEquals(1, result, "One record should be updated");
        assertIdamRoleManagementQueueEntity(idamRoleManagementQueueEntity,
                USER_ID, USER_TYPE, PUBLISHED_AS, DATA, LocalDateTime.now(), 0);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/init_idam_role_management_queue.sql",
        "classpath:sql/irm/insert_idam_role_management_queue_published.sql"})
    public void shouldNotSetAsPublished() {
        // WHEN
        int result = idamRoleManagementQueueRepository.setAsPublished(USER_ID);

        // THEN
        assertEquals(0, result, "Zero records should be updated");
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/init_idam_role_management_queue.sql",
        "classpath:sql/irm/insert_idam_role_management_queue_future.sql"})
    public void shouldSetAsFuturePublished() {
        // WHEN
        int result = idamRoleManagementQueueRepository.setAsPublished(USER_ID);
        Optional<IdamRoleManagementQueueEntity> idamRoleManagementQueueEntity =
                idamRoleManagementQueueRepository.findById(USER_ID);

        // THEN
        assertEquals(1, result, "One record should be updated");
        assertIdamRoleManagementQueueEntity(idamRoleManagementQueueEntity,
                USER_ID, USER_TYPE, PUBLISHED_AS, DATA, LocalDateTime.now().plusYears(1), 0);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/init_idam_role_management_queue.sql",
        "classpath:sql/irm/insert_idam_role_management_queue.sql"})
    public void shouldUpdateRetry() {
        // WHEN
        idamRoleManagementQueueRepository.updateRetry(USER_ID,
                RETRY_INTERVAL1, RETRY_INTERVAL2, RETRY_INTERVAL3);
        Optional<IdamRoleManagementQueueEntity> idamRoleManagementQueueEntity =
                    idamRoleManagementQueueRepository.findById(USER_ID);

        // THEN
        assertIdamRoleManagementQueueEntity(idamRoleManagementQueueEntity,
                USER_ID, USER_TYPE, PUBLISHED_AS, DATA, null, 1);
    }

    private void assertIdamRoleManagementQueueEntity(
            Optional<IdamRoleManagementQueueEntity> idamRoleManagementQueueEntity,
            String userId, String userType, String publishedAs, String data,
            LocalDateTime lastPublished, Integer retry
    ) {
        assertTrue(idamRoleManagementQueueEntity.isPresent(),
                "IdamRoleManagementQueueEntity should be present");
        IdamRoleManagementQueueEntity result = idamRoleManagementQueueEntity.get();

        assertEquals(userId, result.getUserId(), "User ID should match");
        assertEquals(userType, result.getUserType(), "User Type should match");
        assertNotNull(result.getData(), "Data should not be null");
        assertEquals(publishedAs, result.getPublishedAs(), "Published As should match");
        assertEquals(retry, result.getRetry(), "Retry should match");
        if (lastPublished != null) {
            assertNotNull(result.getLastPublished(), "Last Published should not be null");
            assertThat(result.getLastPublished().minusMinutes(1)).isBefore(lastPublished);
        }
    }
}
