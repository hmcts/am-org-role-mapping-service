package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

class PrmSchedulerProcess4IntegrationTest extends BaseSchedulerTestIntegration {

    @Autowired
    private OrganisationRefreshQueueRepository organisationRefreshQueueRepository;

    @Autowired
    private UserRefreshQueueRepository userRefreshQueueRepository;

    @Autowired
    private Scheduler prmScheduler;

    /**
     * No change - Empty organisation list.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql"
    })
    void testNoStaleOrgs() {

        // verify that no organisations are updated
        runTest(List.of());

        // verify that the OrganisationRefreshQueue remains empty
        assertTotalOrganisationRefreshQueueEntitiesInDb(0);

        // Verify active users in the refresh queue
        assertTotalUserRefreshQueueEntitiesInDb(0, 0);
    }

    /**
     * No change - Empty organisation list.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation1.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user1organisation1.sql"
    })
    void testStaleOrg() {

        // verify that no organisations are updated
        runTest(List.of("/SchedulerTests/PrdUsersByOrganisation/userOrganisation1_scenario_01.json"));

        // verify that the OrganisationRefreshQueue contains 1 record
        assertTotalOrganisationRefreshQueueEntitiesInDb(1);

        // Verify active users in the refresh queue
        assertTotalUserRefreshQueueEntitiesInDb(1, 0);
    }

    private void runTest(List<String> fileNames) {

        // GIVEN
        logBeforeStatus();
        stubPrdRetrieveUsersByOrg(fileNames, "false", null);

        // WHEN
        List<ProcessMonitorDto> processMonitorDtos = prmScheduler
            .findUsersWithStaleOrganisationsAndInsertIntoRefreshQueueProcess();

        // THEN
//        if (!fileNames.isEmpty()) {
//            verifySingleCallToPrd();
//        }
        logAfterStatus(processMonitorDtos);

        // verify that the process monitor reports success
//        processMonitorDtos.forEach(processMonitorDto -> {
//            assertEquals(EndStatus.SUCCESS, processMonitorDto.getEndStatus(),
//                "Invalid process monitor end status");
//        });
    }

    //#region Assertion Helpers: DB Checks

    private void assertTotalOrganisationRefreshQueueEntitiesInDb(int expectedNumberOfRecords) {
        var organisationRefreshQueueEntities = organisationRefreshQueueRepository.findAll();
        assertEquals(expectedNumberOfRecords, organisationRefreshQueueEntities.size(),
            "OrganisationRefreshQueueEntity number of records mismatch");
    }

    private void assertTotalUserRefreshQueueEntitiesInDb(int expectedNumberOfRecords, int expectedActiveUsers) {
        var userRefreshQueueEntities = userRefreshQueueRepository.findAll();
        assertEquals(expectedNumberOfRecords, userRefreshQueueEntities.size(),
            "UserRefreshQueueEntity number of records mismatch");
        int activeUsers = 0;
        for (var entity : userRefreshQueueEntities) {
            if (entity.getActive()) {
                activeUsers++;
            }
        }
        assertEquals(expectedActiveUsers, activeUsers,
            "UserRefreshQueueEntity active users count mismatch");
    }

    //#endregion

    private void logAfterStatus(List<ProcessMonitorDto> processMonitorDtos) {
        logObject("ProcessMonitorDto: AFTER", processMonitorDtos);
        logObject("organisationRefreshQueueRepository: AFTER", organisationRefreshQueueRepository.findAll());
    }

    private void logBeforeStatus() {
        logObject("organisationRefreshQueueRepository: BEFORE", organisationRefreshQueueRepository.findAll());
    }

//    private void verifySingleCallToPrd() {
//        var allCallEvents = logWiremockPostCalls(STUB_ID_PRD_RETRIEVE_ORGANISATIONS);
//        // verify single call
//        assertEquals(1, allCallEvents.size(),
//            "Unexpected number of calls to PRD service");
//        var event = allCallEvents.get(0);
//        // verify response status
//        assertEquals(TEST_PAGE_SIZE, event.getRequest().getQueryParams().get("pageSize").firstValue(),
//            "Response pageSize mismatch");
//        // verify response status
//        assertEquals(HttpStatus.OK.value(), event.getResponse().getStatus(),
//            "Response status mismatch");
//        assertEquals("false",  event.getResponse().getHeaders().getHeader(MORE_AVAILABLE).firstValue(),
//            "Response moreAvilable mismatch");
//    }

}
