package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

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

        // verify that no users are updated
        runTest(List.of());

        // Verify no records in the refresh queue
        assertTotalUserRefreshQueueEntitiesInDb(0);
    }

    protected void testCreateRoleAssignment(boolean orgRole, boolean groupRole) {
        // verify that no users are updated
        testCreateRole(List.of("/SchedulerTests/role_assignments/senior_tribunal_caseworker.json"),
            1, orgRole, groupRole);
    }

    /**
     * Create multiple roles test.
     */
//    @Test
//    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
//        "classpath:sql/prm/access_types/insert_accesstypes_yyy.sql",
//        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
//        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
//    })
//    void testMultipleRoles() throws JsonProcessingException {
//        testCreateRole(List.of("/SchedulerTests/role_assignments/senior_tribunal_caseworker.json",
//            "/SchedulerTests/role_assignments/case_allocator.json"), 1, true);
//    }

    private void testCreateRole(List<String> fileNames, int expectedNumberOfRecords,
                                boolean organisation, boolean group) {
        // verify that no users are updated
        runTest(fileNames);

        // Verify the number of records in the user refresh queue
        assertTotalUserRefreshQueueEntitiesInDb(expectedNumberOfRecords);

        assertAssignmentRequest(organisation, group);

        // Verify the access types against the user record
//        assertAccessTypes(user ? "BEFTA_ACCESSTYPE_1" : "BEFTA_ACCESSTYPE_2",
//            user ? "" : "\"ORGPROFILE1\"", "\"BEFTA_JURISDICTION_1\"", user);
    }

    private void runTest(List<String> fileNames) {

        // GIVEN
        logBeforeStatus();
        stubRasCreateRoleAssignment(fileNames, EndStatus.SUCCESS);

        // WHEN
        ProcessMonitorDto processMonitorDto = prmScheduler.processUserRefreshQueue();

        // THEN
        if (!fileNames.isEmpty()) {
            verifyNoOfCallsToRas(1);
        }
        logAfterStatus(processMonitorDto);

        // verify that the process monitor reports the correct status
        assertEquals(EndStatus.SUCCESS, processMonitorDto.getEndStatus());
    }
}
