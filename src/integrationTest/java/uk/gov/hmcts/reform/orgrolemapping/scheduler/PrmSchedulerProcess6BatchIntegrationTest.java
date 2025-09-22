package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrmSchedulerProcess6BatchIntegrationTest extends BaseProcess6IntegrationTest {

    @Autowired
    private Scheduler prmScheduler;

    /**
     * No Change - Empty User Roles List.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/init_access_types.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql"
    })
    void testNoUserRoles() {
        runTest(0, false, false);
    }

    protected void testCreateRoleAssignment(boolean orgRole, boolean groupRole) {
        runTest(1, orgRole, groupRole);
    }

    private void runTest(int expectedNumberOfRecords, boolean organisation, boolean group) {

        // GIVEN
        logBeforeStatus();
        stubRasCreateRoleAssignment(EndStatus.SUCCESS);

        // WHEN
        ProcessMonitorDto processMonitorDto = prmScheduler.processUserRefreshQueue();

        // THEN
        if (expectedNumberOfRecords != 0) {
            verifyNoOfCallsToRas(1);
        }
        logAfterStatus(processMonitorDto);

        // verify that the process monitor reports the correct status
        assertEquals(EndStatus.SUCCESS, processMonitorDto.getEndStatus());

        // Verify the number of records in the user refresh queue
        assertTotalUserRefreshQueueEntitiesInDb(expectedNumberOfRecords);

        if (expectedNumberOfRecords != 0) {
            assertAssignmentRequest(organisation, group);
        }
    }
}
