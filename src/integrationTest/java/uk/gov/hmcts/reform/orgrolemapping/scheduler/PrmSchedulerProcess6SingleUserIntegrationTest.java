package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.inject.Inject;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalRefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrmSchedulerProcess6SingleUserIntegrationTest extends BaseProcess6IntegrationTest {

    @Inject
    private ProfessionalRefreshOrchestrator professionalRefreshOrchestrator;

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
        runTest(List.of("/SchedulerTests/PrdRetrieveUsers/userx_scenario_01.json"),
                1, false, false, EndStatus.FAILED);
    }

    /**
     *  OrganisationStatus of PENDING.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user_refresh_queue_orgstatus_pending.sql"
    })
    void testCreateRole_orgstatus_pending() throws JsonProcessingException {
        runTest(List.of("/SchedulerTests/PrdRetrieveUsers/userx_scenario_04.json"),
                1, false, false, EndStatus.SUCCESS);
    }

    /**
     *  Delete Role Assignment.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_deleted.sql"
    })
    void testDeleteRole() throws JsonProcessingException {
        runTest(List.of("/SchedulerTests/PrdRetrieveUsers/userx_scenario_03.json"),
                1, false, false, EndStatus.SUCCESS);
    }

    protected void testCreateRoleAssignment(boolean organisation, boolean group) {
        runTest(organisation ? List.of("/SchedulerTests/PrdRetrieveUsers/userx_scenario_01.json")
                : List.of("/SchedulerTests/PrdRetrieveUsers/userx_scenario_02.json"),
                1, organisation, group, EndStatus.SUCCESS);
    }

    private void runTest(List<String> refreshUserfileNames, int expectedNumberOfRecords,
                         boolean organisation, boolean group, EndStatus endStatus) {

        // GIVEN
        logBeforeStatus();
        stubPrdRefreshUser(refreshUserfileNames, USERID, "false", "false");
        stubRasCreateRoleAssignment(endStatus);

        try {
            // WHEN
            ResponseEntity<Object> response = professionalRefreshOrchestrator
                    .refreshProfessionalUser(USERID);

            // THEN
            if (expectedNumberOfRecords != 0) {
                verifyNoOfCallsToPrd(1);
                verifyNoOfCallsToRas(1);
            }
            logAfterStatus(response);

            // verify the response
            assertResponse(response);

            if (expectedNumberOfRecords != 0) {
                assertAssignmentRequest(organisation, group);
            }
        } catch (ServiceException e) {
            assertEquals(EndStatus.FAILED, endStatus);
        }
    }
}
