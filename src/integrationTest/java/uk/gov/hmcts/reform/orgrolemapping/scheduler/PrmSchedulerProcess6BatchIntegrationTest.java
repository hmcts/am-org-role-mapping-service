package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
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
        runTest(0, false, false, EndStatus.SUCCESS);
    }

    /**
     *  No Update - UserRefreshQueue.accessTypeVersion >  PRM Access Version.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_version1.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_accessVersion() throws JsonProcessingException {
        runTest(1, false, false, EndStatus.FAILED);
    }

    /**
     *  accessDefault = Y, accessMandatory = N, groupAccessEnabled = Y, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user_refresh_queue_orgstatus_pending.sql"
    })
    void testCreateRole_orgstatus_pending() throws JsonProcessingException {
        runTest(1, false, false, EndStatus.SUCCESS);
    }

    protected void testCreateRoleAssignment(boolean orgRole, boolean groupRole) {
        runTest(1, orgRole, groupRole, EndStatus.SUCCESS);
    }

    private void runTest(int expectedNumberOfRecords, boolean organisation, boolean group,
                         EndStatus endStatus) {

        // GIVEN
        logBeforeStatus();
        stubRasCreateRoleAssignment(endStatus);

        // WHEN
        ProcessMonitorDto processMonitorDto = prmScheduler.processUserRefreshQueue();

        // THEN
        if (expectedNumberOfRecords != 0 && EndStatus.SUCCESS.equals(endStatus)) {
            verifyNoOfCallsToRas(1);
        }
        logAfterStatus(processMonitorDto);

        // verify that the process monitor reports the correct status
        assertEquals(endStatus, processMonitorDto.getEndStatus());

        // Verify the number of records in the user refresh queue
        assertTotalUserRefreshQueueEntitiesInDb(expectedNumberOfRecords, endStatus);

        if (expectedNumberOfRecords != 0 && EndStatus.SUCCESS.equals(endStatus)) {
            assertAssignmentRequest(organisation, group);
        }
    }
}
