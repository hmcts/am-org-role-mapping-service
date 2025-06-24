package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter DTF =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final LocalDateTime OLD_USER_LAST_UPDATED =
        LocalDateTime.parse("2020-01-01T13:30:01.046Z", DTF);

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
        runTest(List.of(), EndStatus.SUCCESS);

        // verify that the OrganisationRefreshQueue remains empty
        assertTotalOrganisationRefreshQueueEntitiesInDb(0, 0);

        // Verify no active users in the refresh queue
        assertTotalUserRefreshQueueEntitiesInDb(0);
    }

    /**
     * Update - Stale Organisation, 2 Existing Users, 1 New User.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation1.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user1organisation1.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user2organisation1.sql"
    })
    void testStaleOrg2ExistingUsers1New() {

        // verify that no organisations are updated
        runTest(List.of("/SchedulerTests/PrdUsersByOrganisation/userOrganisation1_scenario_01.json"),
            EndStatus.SUCCESS);

        // verify that the OrganisationRefreshQueue contains 1 record, 0 active
        assertTotalOrganisationRefreshQueueEntitiesInDb(1, 0);

        // Verify 3 active users in the refresh queue
        assertTotalUserRefreshQueueEntitiesInDb(3);
        assertUserRefreshQueueEntitiesInDb("user1", true);
        assertUserRefreshQueueEntitiesInDb("user2", true);
        assertUserRefreshQueueEntitiesInDb("user3", true);
    }

    /**
     * No Change - Stale Organisation, 3 Existing Users.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation1.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user1organisation1.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user2organisation1.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user3organisation1.sql"
    })
    void testStaleOrg3ExistingUsersNoChange() {

        // verify that no organisations are updated
        runTest(List.of("/SchedulerTests/PrdUsersByOrganisation/userOrganisation1_scenario_02.json"),
            EndStatus.SUCCESS);

        // verify that the OrganisationRefreshQueue contains 1 record, 0 active
        assertTotalOrganisationRefreshQueueEntitiesInDb(1, 0);

        // Verify 3 active users in the refresh queue, no changes
        assertTotalUserRefreshQueueEntitiesInDb(3);
        assertUserRefreshQueueEntitiesInDb("user1", false);
        assertUserRefreshQueueEntitiesInDb("user2", false);
        assertUserRefreshQueueEntitiesInDb("user3", false);
    }

    private void runTest(List<String> fileNames, EndStatus endStatus) {

        // GIVEN
        logBeforeStatus();
        stubPrdRetrieveUsersByOrg(fileNames, "false", null);

        // WHEN
        ProcessMonitorDto processMonitorDto = prmScheduler
            .findUsersWithStaleOrganisationsAndInsertIntoRefreshQueueProcess();

        // THEN
        if (!fileNames.isEmpty()) {
            verifySingleCallToPrd();
        }
        logAfterStatus(processMonitorDto);

        // verify that the process monitor reports the correct status
        assertEquals(endStatus, processMonitorDto.getEndStatus());
    }

    //#region Assertion Helpers: DB Checks

    private void assertTotalOrganisationRefreshQueueEntitiesInDb(int expectedNumberOfRecords, int expectedActiveOrgs) {
        var organisationRefreshQueueEntities = organisationRefreshQueueRepository.findAll();
        assertEquals(expectedNumberOfRecords, organisationRefreshQueueEntities.size(),
            "OrganisationRefreshQueueEntity number of records mismatch");
        int activeOrgs = 0;
        for (var entity : organisationRefreshQueueEntities) {
            if (entity.getActive()) {
                activeOrgs++;
            }
        }
        assertEquals(expectedActiveOrgs, activeOrgs,
            "OrganisationRefreshQueueEntity active organisations count mismatch");
    }

    private void assertTotalUserRefreshQueueEntitiesInDb(int expectedNumberOfRecords) {
        var userRefreshQueueEntities = userRefreshQueueRepository.findAll();
        assertEquals(expectedNumberOfRecords, userRefreshQueueEntities.size(),
            "UserRefreshQueueEntity number of records mismatch");
    }

    private void assertUserRefreshQueueEntitiesInDb(String userId, boolean isUpdated) {
        var userRefreshQueueEntity = userRefreshQueueRepository.findById(userId);
        assertTrue(userRefreshQueueEntity.isPresent(),
            "UserRefreshQueueEntity not found for userId: " + userId);
        assertTrue(userRefreshQueueEntity.get().getActive(),
            "UserRefreshQueueEntity is not active for userId: " + userId);
        if (isUpdated) {
            assertLastUpdatedNow(userRefreshQueueEntity.get().getUserLastUpdated());
        } else {
            assertEquals(OLD_USER_LAST_UPDATED,
                userRefreshQueueEntity.get().getUserLastUpdated(),
                "UserRefreshQueueEntity userLastUpdated mismatch for userId: " + userId);
        }
    }

    private boolean assertLastUpdatedNow(LocalDateTime lastUpdated) {
        return lastUpdated.isAfter(LocalDateTime.now().minusMinutes(1));
    }

    //#endregion

    private void logAfterStatus(ProcessMonitorDto processMonitorDto) {
        logObject("ProcessMonitorDto: AFTER", processMonitorDto);
        logObject("organisationRefreshQueueRepository: AFTER", organisationRefreshQueueRepository.findAll());
    }

    private void logBeforeStatus() {
        logObject("organisationRefreshQueueRepository: BEFORE", organisationRefreshQueueRepository.findAll());
    }

    private void verifySingleCallToPrd() {
        var allCallEvents = logWiremockPostCalls(STUB_ID_PRD_RETRIEVE_USERSBYORG);
        // verify single call
        assertEquals(1, allCallEvents.size(),
            "Unexpected number of calls to PRD service");
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
