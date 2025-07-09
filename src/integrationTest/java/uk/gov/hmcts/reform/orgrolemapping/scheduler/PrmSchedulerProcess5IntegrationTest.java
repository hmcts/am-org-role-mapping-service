package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

class PrmSchedulerProcess5IntegrationTest extends BaseSchedulerTestIntegration {

    private static final Integer TOLERANCE_MINUTES = 1;

    @Autowired
    private UserRefreshQueueRepository userRefreshQueueRepository;

    @Autowired
    private BatchLastRunTimestampRepository batchLastRunTimestampRepository;

    @Autowired
    private Scheduler prmScheduler;

    /**
     * No change - Empty user list.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/batch_last_run_timestamp/init_batch_last_run_timestamp.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql"
    })
    void testNoUsers() {

        // verify that no users are updated
        runTest(List.of(), EndStatus.SUCCESS, 1);

        // Verify no active users in the refresh queue
        assertTotalUserRefreshQueueEntitiesInDb(0);

        // verify the last user run date time has NOT been updated
        assertBatchLastRunTimestampEntity(false);
    }

    private void runTest(List<String> fileNames, EndStatus endStatus, int noOfCallsToPrd) {

        // GIVEN
        logBeforeStatus();
        stubPrdRetrieveUsers(fileNames, "false");

        // WHEN
        ProcessMonitorDto processMonitorDto = prmScheduler
            .findUserChangesAndInsertIntoUserRefreshQueue();

        // THEN
        verifyNoOfCallsToPrd(noOfCallsToPrd);
        logAfterStatus(processMonitorDto);

        // verify that the process monitor reports the correct status
        assertEquals(endStatus, processMonitorDto.getEndStatus());
    }

    //#region Assertion Helpers: DB Checks

    private void assertBatchLastRunTimestampEntity(boolean isUpdated) {
        List<BatchLastRunTimestampEntity> allBatches = batchLastRunTimestampRepository.findAll();
        // verify that the BatchLastRunTimestampEntity contains a single record
        assertEquals(1, allBatches.size(),
            "BatchLastRunTimestampEntity single record not found");
        // verify the lastUserRunDatetime was updated or not dependent on passed in parameter
        assertEquals(isUpdated, assertLastUpdatedNow(allBatches.get(0).getLastUserRunDatetime(),
            TOLERANCE_MINUTES), "BatchLastRunTimestampEntity.LastUserRunDatetime "
            + (isUpdated ? "not " : "") + "updated");
    }

    private boolean assertLastUpdatedNow(LocalDateTime lastUpdated, int minutes) {
        return lastUpdated.isAfter(LocalDateTime.now().minusMinutes(minutes));
    }

    private void assertTotalUserRefreshQueueEntitiesInDb(int expectedNumberOfRecords) {
        var userRefreshQueueEntities = userRefreshQueueRepository.findAll();
        assertEquals(expectedNumberOfRecords, userRefreshQueueEntities.size(),
            "UserRefreshQueueEntity number of records mismatch");
    }

    //#endregion

    private void logAfterStatus(ProcessMonitorDto processMonitorDto) {
        logObject("ProcessMonitorDto: AFTER", processMonitorDto);
        logObject("userRefreshQueueRepository: AFTER", userRefreshQueueRepository.findAll());
    }

    private void logBeforeStatus() {
        logObject("userRefreshQueueRepository: BEFORE", userRefreshQueueRepository.findAll());
    }

    private void verifyNoOfCallsToPrd(int noOfCalls) {
        var allCallEvents = logWiremockPostCalls(STUB_ID_PRD_RETRIEVE_USERS);
        // verify number of calls
        assertEquals(noOfCalls, allCallEvents.size(),
            "Unexpected number of calls to PRD service");
        if (noOfCalls == 0) {
            return; // no need to check further if no calls were made
        }
        var event = allCallEvents.get(0);
        // verify response status
        assertEquals(TEST_PAGE_SIZE, event.getRequest().getQueryParams().get("pageSize").firstValue(),
            "Response pageSize mismatch");
        // verify response status
        assertEquals(HttpStatus.OK.value(), event.getResponse().getStatus(),
            "Response status mismatch");
        assertEquals("false",  event.getResponse().getHeaders().getHeader(MORE_AVAILABLE).firstValue(),
            "Response moreAvailable mismatch");
    }

}
