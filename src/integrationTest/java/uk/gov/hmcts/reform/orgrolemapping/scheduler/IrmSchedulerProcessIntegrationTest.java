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

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class IrmSchedulerProcessIntegrationTest extends BaseIrmSchedulerTestIntegration {

    @Autowired
    private IdamRoleManagementQueueRepository idamRoleManagementQueueRepository;

    @Autowired
    private IrmScheduler irmScheduler;

    /**
     * Create queue entry - Success.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue.sql"
    })
    void testProcessJudicialQueue_Success() {
        // GIVEN

        // WHEN
        ProcessMonitorDto processMonitorDto = irmScheduler.processJudicialQueue();

        // THEN
        assertNotNull(processMonitorDto);
        assertEquals(EndStatus.SUCCESS, processMonitorDto.getEndStatus());
        assertNoOfQueueRecords(IdamRecordType.USER, 1);
    }

    private void assertNoOfQueueRecords(IdamRecordType idamRecordType, int expectedNoOfRecords) {
        List<IdamRoleManagementQueueEntity> records = idamRoleManagementQueueRepository.findAll();
        assertEquals(expectedNoOfRecords, records.size());
        records.forEach(record -> {
            assertFalse(record.getActive());
            assertEquals(0, record.getRetry());
            assertLastUpdatedNow(record.getLastUpdated());
            assertLastUpdatedNow(record.getLastPublished());
            assertLastUpdatedNow(record.getRetryAfter());
            assertEquals(idamRecordType, record.getPublishedAs());
        });
    }

    private boolean assertLastUpdatedNow(LocalDateTime lastUpdated) {
        return lastUpdated.isAfter(LocalDateTime.now().minusMinutes(1));
    }
}
