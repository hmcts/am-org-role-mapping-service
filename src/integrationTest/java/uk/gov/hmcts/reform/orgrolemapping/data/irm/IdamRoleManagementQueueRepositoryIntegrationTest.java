package uk.gov.hmcts.reform.orgrolemapping.data.irm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.irm.IdamRecordType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleDataRole;
import uk.gov.hmcts.reform.orgrolemapping.util.irm.IdamRoleDataJsonBConverter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
public class IdamRoleManagementQueueRepositoryIntegrationTest extends BaseTestIntegration {

    private static final IdamRoleDataJsonBConverter IDAM_ROLE_DATA_JSON_B_CONVERTER =
            new IdamRoleDataJsonBConverter();
    private static final String USER_ID = "some-user-id";
    private static final String RETRY_INTERVAL1 = "10";
    private static final String RETRY_INTERVAL2 = "20";
    private static final String RETRY_INTERVAL3 = "30";
    private static final IdamRoleData DATA = IdamRoleData.builder()
            .emailId("someone@somewhere.com")
            .roles(List.of(
                    IdamRoleDataRole.builder().roleName("Role1").build(),
                    IdamRoleDataRole.builder().roleName("Role2").build()))
            .build();
    private static final String JSON_DATA =
            IDAM_ROLE_DATA_JSON_B_CONVERTER.convertToDatabaseColumn(DATA);

    @Autowired
    private IdamRoleManagementQueueRepository idamRoleManagementQueueRepository;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql"})
    public void shouldInsertToIdamRoleManagementQueue() {
        // WHEN
        idamRoleManagementQueueRepository.upsert(USER_ID, UserType.JUDICIAL.name(),
                JSON_DATA, LocalDateTime.now());
        Optional<IdamRoleManagementQueueEntity> idamRoleManagementQueueEntity =
                idamRoleManagementQueueRepository.findById(USER_ID);

        // THEN
        assertIdamRoleManagementQueueEntity(idamRoleManagementQueueEntity,
                USER_ID, UserType.JUDICIAL, null, JSON_DATA, null, 0, null, true);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_retry4.sql"})
    public void shouldUpdateIdamRoleManagementQueue() {
        // GIVEN
        IdamRoleData newData = IdamRoleData.builder()
                .emailId("someoneelse@somewhereelse.com")
                .roles(List.of(
                        IdamRoleDataRole.builder().roleName("Role4").build(),
                        IdamRoleDataRole.builder().roleName("Role5").build()))
                .build();
        String newJsonData =
                IDAM_ROLE_DATA_JSON_B_CONVERTER.convertToDatabaseColumn(newData);

        // WHEN
        idamRoleManagementQueueRepository.upsert(USER_ID, UserType.JUDICIAL.name(),
                newJsonData, LocalDateTime.now());
        Optional<IdamRoleManagementQueueEntity> idamRoleManagementQueueEntity =
                idamRoleManagementQueueRepository.findById(USER_ID);

        // THEN
        assertIdamRoleManagementQueueEntity(idamRoleManagementQueueEntity,
                USER_ID, UserType.JUDICIAL, IdamRecordType.USER, newJsonData, null,
                0, null, true);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue.sql"})
    public void shouldSetAsPublished() {
        // WHEN
        int result = idamRoleManagementQueueRepository.setAsPublished(USER_ID,
                IdamRecordType.USER.name());
        Optional<IdamRoleManagementQueueEntity> idamRoleManagementQueueEntity =
                idamRoleManagementQueueRepository.findById(USER_ID);

        // THEN
        assertEquals(1, result, "One record should be updated");
        assertIdamRoleManagementQueueEntity(idamRoleManagementQueueEntity,
                USER_ID, UserType.JUDICIAL, IdamRecordType.USER, JSON_DATA,
                LocalDateTime.now(), 0, null, false);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_published.sql"})
    public void shouldNotSetAsPublished() {
        // WHEN
        int result = idamRoleManagementQueueRepository.setAsPublished(USER_ID,
                IdamRecordType.USER.name());

        // THEN
        assertEquals(0, result, "Zero records should be updated");
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_future.sql"})
    public void shouldSetAsFuturePublished() {
        // WHEN
        int result = idamRoleManagementQueueRepository.setAsPublished(USER_ID,
                IdamRecordType.INVITE.name());
        Optional<IdamRoleManagementQueueEntity> idamRoleManagementQueueEntity =
                idamRoleManagementQueueRepository.findById(USER_ID);

        // THEN
        assertEquals(1, result, "One record should be updated");
        assertIdamRoleManagementQueueEntity(idamRoleManagementQueueEntity,
                USER_ID, UserType.JUDICIAL, IdamRecordType.INVITE, JSON_DATA,
                LocalDateTime.now().plusYears(1), 0, null, false);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue.sql"})
    public void shouldUpdateRetry0To1() {
        testRetry(1, RETRY_INTERVAL1);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_retry1.sql"})
    public void shouldUpdateRetry1To2() {
        testRetry(2, RETRY_INTERVAL2);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_retry2.sql"})
    public void shouldUpdateRetry2To3() {
        testRetry(3, RETRY_INTERVAL3);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_retry3.sql"})
    public void shouldUpdateRetry3To4() {
        testRetry(4, null);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_retry4.sql"})
    public void shouldUpdateRetryAtMax() {
        testRetry(4, null);
    }

    private void testRetry(Integer retry, String retryInterval) {
        // WHEN
        idamRoleManagementQueueRepository.updateRetry(USER_ID,
                RETRY_INTERVAL1, RETRY_INTERVAL2, RETRY_INTERVAL3);
        Optional<IdamRoleManagementQueueEntity> idamRoleManagementQueueEntity =
                idamRoleManagementQueueRepository.findById(USER_ID);

        // THEN
        assertIdamRoleManagementQueueEntity(idamRoleManagementQueueEntity,
                USER_ID, UserType.JUDICIAL, IdamRecordType.USER, JSON_DATA,
                null, retry, retryInterval, true);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue.sql"})
    public void findAndLockSingleActiveRecordTest() {
        // WHEN
        IdamRoleManagementQueueEntity idamRoleManagementQueueEntity =
                idamRoleManagementQueueRepository.findAndLockSingleActiveRecord(
                        UserType.JUDICIAL.name());

        // THEN
        assertIdamRoleManagementQueueEntity(
                Optional.ofNullable(idamRoleManagementQueueEntity),
                USER_ID, UserType.JUDICIAL, IdamRecordType.USER, JSON_DATA,
                null, 0, null, true);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_retry4.sql"})
    public void shouldNotFindAndLockSingleActiveRecordTest() {
        assertNull(idamRoleManagementQueueRepository.findAndLockSingleActiveRecord(
                UserType.JUDICIAL.name()),
                "Records at max retry should not be found");
    }

    private void assertIdamRoleManagementQueueEntity(
            Optional<IdamRoleManagementQueueEntity> idamRoleManagementQueueEntity,
            String userId, UserType userType, IdamRecordType publishedAs, String data,
            LocalDateTime lastPublished, Integer retry,
            String retryInterval, boolean active) {
        assertTrue(idamRoleManagementQueueEntity.isPresent(),
                "IdamRoleManagementQueueEntity should be present");
        IdamRoleManagementQueueEntity result = idamRoleManagementQueueEntity.get();

        assertEquals(userId, result.getUserId(), "User ID should match");
        assertEquals(userType, result.getUserType(), "User Type should match");
        assertEquals(active, result.getActive(), "Active should match");
        assertNotNull(result.getData(), "Data should not be null");
        assertEquals(data,
                IDAM_ROLE_DATA_JSON_B_CONVERTER.convertToDatabaseColumn(result.getData()),
                "Data should match");
        assertEquals(publishedAs, result.getPublishedAs(), "Published As should match");
        assertEquals(retry, result.getRetry(), "Retry should match");
        if (lastPublished != null) {
            assertNotNull(result.getLastPublished(), "Last Published should not be null");
            assertThat(result.getLastPublished().minusMinutes(1)).isBefore(lastPublished);
        }
        if (retry == 4) {
            assertNull(result.getRetryAfter(), "Retry After should be null");
        } else if (retry != 0) {
            assertNotNull(result.getRetryAfter(), "Retry After should not be null");
            assertThat(result.getRetryAfter()
                    .minusMinutes(Integer.parseInt(retryInterval))).isBefore(LocalDateTime.now());
        }
    }
}
