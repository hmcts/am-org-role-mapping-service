package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.irm.IdamRecordType;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType.JUDICIAL;

public class IrmSchedulerProcessIntegrationTest extends BaseIrmSchedulerTestIntegration {

    @Autowired
    private IdamRoleManagementQueueRepository idamRoleManagementQueueRepository;

    @Autowired
    private IrmScheduler irmScheduler;

    /**
     * Process Judicial Queue - Success.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_caseworker.sql"
    })
    void testProcessJudicialQueue_Success() {
        testProcessJudicialQueue(2, true, 0);
    }

    /**
     * Process Judicial Queue - Retry 1.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_retry1.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_caseworker.sql"
    })
    void testProcessJudicialQueue_Retry1() {
        testProcessJudicialQueue(2, true, 0);
    }

    /**
     * Process Judicial Queue - Retry 2.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_retry2.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_caseworker.sql"
    })
    void testProcessJudicialQueue_Retry2() {
        testProcessJudicialQueue(2, true, 0);
    }

    /**
     * Process Judicial Queue - Retry 3.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_retry3.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_caseworker.sql"
    })
    void testProcessJudicialQueue_Retry3() {
        testProcessJudicialQueue(2, true, 0);
    }
    
    /**
     * Process Judicial Queue - Retry 4.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_retry4.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_caseworker.sql"
    })
    void testProcessJudicialQueue_Retry4() {
        testProcessJudicialQueue(2, false, 4);
    }

    void testProcessJudicialQueue(int expectedNoOfRecords, boolean isUpdated, int retry) {
        // GIVEN

        // WHEN
        ProcessMonitorDto processMonitorDto = irmScheduler.processJudicialQueue();

        // THEN
        assertNotNull(processMonitorDto);
        assertEquals(EndStatus.SUCCESS, processMonitorDto.getEndStatus());
        assertQueueRecords(IdamRecordType.USER, expectedNoOfRecords, isUpdated, retry);
    }

    private void assertQueueRecords(IdamRecordType idamRecordType, int expectedNoOfRecords,
                                    boolean isUpdated, int retry) {
        List<IdamRoleManagementQueueEntity> results = idamRoleManagementQueueRepository.findAll();
        assertEquals(expectedNoOfRecords, results.size());
        results.forEach(record -> {
            if (JUDICIAL.equals(record.getUserType())) {
                assertEquals(!isUpdated, record.getActive(), "Active mismatch");
                assertEquals(retry, record.getRetry(), "Retry mismatch");
                assertEquals(idamRecordType, record.getPublishedAs(), "PublishedAs mismatch");
                assertTrue(assertLastUpdatedNow(record.getLastUpdated()), "LastUpdated mismatch");
                if (isUpdated) {
                    assertTrue(assertLastUpdatedNow(record.getLastPublished()), "LastPublished mismatch");
                    assertTrue(assertLastUpdatedNow(record.getRetryAfter()), "RetryAfter mismatch");
                } else {
                    assertNull(record.getRetryAfter(), "RetryAfter not null");
                }
            } else {
                assertTrue(record.getActive());
            }
        });
    }

    private boolean assertLastUpdatedNow(LocalDateTime lastUpdated) {
        return lastUpdated.isAfter(LocalDateTime.now().minusMinutes(1));
    }
}
