package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

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
    private static final LocalDateTime NEW_USER_LAST_UPDATED =
        LocalDateTime.parse("2023-09-19T15:36:33.653Z", DTF);
    private static final String ORGANISATION_ID_1 = "1";
    private static final String ORGANISATION_ID_2 = "2";
    private static final String ORGANISATION_ID_4 = "4";
    private static final String EMPTY_ACCESS_TYPES = "[]";
    private static final String SOLICITOR_ACCESS_TYPE = """
        { 
          "jurisdictionId": "CIVIL",
          "organisationProfileId": "SOLICITOR_PROFILE",
          "accessTypeId": "civil-cases-1",
          "enabled": true
        }
        """;
    private static final String OGD_ACCESS_TYPE = """
        { 
          "jurisdictionId": "CIVIL",
          "organisationProfileId": "OGD_PROFILE",
          "accessTypeId": "ogd-1",
          "enabled": true
        }
        """;

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
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql"
    })
    void testNoStaleOrgs() {

        // verify that no organisations are updated
        runTest(List.of(), EndStatus.SUCCESS, 0);

        // verify that the OrganisationRefreshQueue remains empty
        assertTotalOrganisationRefreshQueueEntitiesInDb(0, 0, 0);

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

        // verify that the organisations are updated
        runTest(List.of("/SchedulerTests/PrdUsersByOrganisation/userOrganisation1_scenario_01.json"),
            EndStatus.SUCCESS, 1);

        // verify that the OrganisationRefreshQueue contains 1 record, 0 active, 0 retries
        assertTotalOrganisationRefreshQueueEntitiesInDb(1, 0, 0);

        // Verify 3 active users in the refresh queue, all updated
        assertTotalUserRefreshQueueEntitiesInDb(3);
        assertUserRefreshQueueEntitiesInDb("user1", ORGANISATION_ID_1, SOLICITOR_ACCESS_TYPE,
            NEW_USER_LAST_UPDATED, true, false);
        assertUserRefreshQueueEntitiesInDb("user2", ORGANISATION_ID_1, SOLICITOR_ACCESS_TYPE,
            NEW_USER_LAST_UPDATED, true, false);
        assertUserRefreshQueueEntitiesInDb("user3", ORGANISATION_ID_1, SOLICITOR_ACCESS_TYPE,
            NEW_USER_LAST_UPDATED, true, false);
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

        // verify that the organisations are updated
        runTest(List.of("/SchedulerTests/PrdUsersByOrganisation/userOrganisation1_scenario_02.json"),
            EndStatus.SUCCESS, 1);

        // verify that the OrganisationRefreshQueue contains 1 record, 0 active, 0 retries
        assertTotalOrganisationRefreshQueueEntitiesInDb(1, 0, 0);

        // Verify 3 active users in the refresh queue, no changes
        assertTotalUserRefreshQueueEntitiesInDb(3);
        assertUserRefreshQueueEntitiesInDb("user1", ORGANISATION_ID_1, EMPTY_ACCESS_TYPES,
            OLD_USER_LAST_UPDATED, true, false);
        assertUserRefreshQueueEntitiesInDb("user2", ORGANISATION_ID_1, EMPTY_ACCESS_TYPES,
            OLD_USER_LAST_UPDATED, true, false);
        assertUserRefreshQueueEntitiesInDb("user3", ORGANISATION_ID_1, EMPTY_ACCESS_TYPES,
            OLD_USER_LAST_UPDATED, true, false);
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
    void testStaleOrg2ExistingUsersNoChange1New() {

        // verify that the organisations are updated
        runTest(List.of("/SchedulerTests/PrdUsersByOrganisation/userOrganisation1_scenario_02.json"),
            EndStatus.SUCCESS, 1);

        // verify that the OrganisationRefreshQueue contains 1 record, 0 active, 0 retries
        assertTotalOrganisationRefreshQueueEntitiesInDb(1, 0, 0);

        // Verify 3 active users in the refresh queue, no changes
        assertTotalUserRefreshQueueEntitiesInDb(3);
        assertUserRefreshQueueEntitiesInDb("user1", ORGANISATION_ID_1, EMPTY_ACCESS_TYPES,
            OLD_USER_LAST_UPDATED, true, false);
        assertUserRefreshQueueEntitiesInDb("user2", ORGANISATION_ID_1, EMPTY_ACCESS_TYPES,
            OLD_USER_LAST_UPDATED, true, false);
        assertUserRefreshQueueEntitiesInDb("user3", ORGANISATION_ID_1, SOLICITOR_ACCESS_TYPE,
            OLD_USER_LAST_UPDATED, true, false);
    }

    /**
     * Update - Stale Organisation, 3 Existing Users, 1 Updated User.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation1.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation2.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user1organisation1.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user2organisation1.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user3organisation1.sql"
    })
    void testStaleOrg3ExistingUsersNoChange1Update() {

        // verify that the organisations are updated
        runTest(List.of("/SchedulerTests/PrdUsersByOrganisation/userOrganisation1_scenario_02.json",
                "/SchedulerTests/PrdUsersByOrganisation/userOrganisation2_scenario_01.json"),
            EndStatus.SUCCESS, 2);

        // verify that the OrganisationRefreshQueue contains 1 record, 0 active, 0 retries
        assertTotalOrganisationRefreshQueueEntitiesInDb(2, 0, 0);

        // Verify 3 active users in the refresh queue, no changes
        assertTotalUserRefreshQueueEntitiesInDb(4);
        assertUserRefreshQueueEntitiesInDb("user1", ORGANISATION_ID_1, EMPTY_ACCESS_TYPES,
            OLD_USER_LAST_UPDATED, true, false);
        assertUserRefreshQueueEntitiesInDb("user2", ORGANISATION_ID_1, EMPTY_ACCESS_TYPES,
            OLD_USER_LAST_UPDATED, true, false);
        assertUserRefreshQueueEntitiesInDb("user3", ORGANISATION_ID_1, EMPTY_ACCESS_TYPES,
            OLD_USER_LAST_UPDATED, true, false);
        assertUserRefreshQueueEntitiesInDb("userA", ORGANISATION_ID_2, SOLICITOR_ACCESS_TYPE,
            NEW_USER_LAST_UPDATED, true, false);
    }

    /**
     * Update - Multiple Stale Organisations, 3 Existing Users.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation1.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation2.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user1organisation1.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user2organisation1.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user3organisation1.sql"
    })
    void testMultipleStaleOrgs() {

        // verify that the organisations are updated
        runTest(List.of("/SchedulerTests/PrdUsersByOrganisation/userOrganisation1_scenario_01.json",
                "/SchedulerTests/PrdUsersByOrganisation/userOrganisation2_scenario_01.json"),
            EndStatus.SUCCESS, 2);

        // verify that the OrganisationRefreshQueue contains 2 records, 0 active, 0 retries
        assertTotalOrganisationRefreshQueueEntitiesInDb(2, 0, 0);

        // Verify 4 active users in the refresh queue, nall updated
        assertTotalUserRefreshQueueEntitiesInDb(4);
        assertUserRefreshQueueEntitiesInDb("user1", ORGANISATION_ID_1, SOLICITOR_ACCESS_TYPE,
            NEW_USER_LAST_UPDATED, true, false);
        assertUserRefreshQueueEntitiesInDb("user2", ORGANISATION_ID_1, SOLICITOR_ACCESS_TYPE,
            NEW_USER_LAST_UPDATED, true, false);
        assertUserRefreshQueueEntitiesInDb("user3", ORGANISATION_ID_1, SOLICITOR_ACCESS_TYPE,
            NEW_USER_LAST_UPDATED, true, false);
        assertUserRefreshQueueEntitiesInDb("userA", ORGANISATION_ID_2, SOLICITOR_ACCESS_TYPE,
            NEW_USER_LAST_UPDATED, true, false);
    }

    /**
     * Delete - Stale Organisations, 2 Existing Users updated, 1 Deleted.
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
    void testStaleOrgs2Updated1DeletedUser() {

        // verify that the organisations are updated
        runTest(List.of("/SchedulerTests/PrdUsersByOrganisation/userOrganisation1_scenario_03.json"),
            EndStatus.SUCCESS, 1);

        // verify that the OrganisationRefreshQueue contains 1 record, 0 active, 0 retries
        assertTotalOrganisationRefreshQueueEntitiesInDb(1, 0, 0);

        // Verify 3 active users in the refresh queue, no changes
        assertTotalUserRefreshQueueEntitiesInDb(3);
        assertUserRefreshQueueEntitiesInDb("user1", ORGANISATION_ID_1, SOLICITOR_ACCESS_TYPE,
            NEW_USER_LAST_UPDATED, true, false);
        assertUserRefreshQueueEntitiesInDb("user2", ORGANISATION_ID_1, SOLICITOR_ACCESS_TYPE,
            NEW_USER_LAST_UPDATED, true, false);
        assertUserRefreshQueueEntitiesInDb("user3", ORGANISATION_ID_1, SOLICITOR_ACCESS_TYPE,
            NEW_USER_LAST_UPDATED, true, true);
    }

    /**
     * Retry.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation4.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user2organisation4.sql"
    })
    void testRetrySuccess() {

        // verify that the organisations are updated
        runTest(List.of("/SchedulerTests/PrdUsersByOrganisation/userOrganisation4_scenario_01.json"),
            EndStatus.SUCCESS, 1);

        // verify that the OrganisationRefreshQueue contains 1 record, 0 active, 0 retries
        assertTotalOrganisationRefreshQueueEntitiesInDb(1, 0, 0);

        // Verify 2 active users in the refresh queue, all updated
        assertTotalUserRefreshQueueEntitiesInDb(3);
        assertUserRefreshQueueEntitiesInDb("user11", ORGANISATION_ID_4, SOLICITOR_ACCESS_TYPE,
            NEW_USER_LAST_UPDATED, true, false);
        assertUserRefreshQueueEntitiesInDb("user12", ORGANISATION_ID_4, OGD_ACCESS_TYPE,
            NEW_USER_LAST_UPDATED, true, false);
    }

    /**
     * Retry - Failed.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation4.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql"
    })
    void testRetryFailed() {


        // verify that the organisations are attempted to be updated 9 (3 x 3 retries) times
        runTest(List.of("/SchedulerTests/PrdUsersByOrganisation/userOrganisation4_scenario_01.json"),
            EndStatus.FAILED, 9);

        // verify that the OrganisationRefreshQueue contains 1 record, 1 active, 4 retries
        assertTotalOrganisationRefreshQueueEntitiesInDb(1, 1, 1);

        // Verify no active users in the refresh queue
        assertTotalUserRefreshQueueEntitiesInDb(0);
    }

    /**
     * Partial Success - Failed.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation1.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation4.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user1organisation1.sql"
    })
    void testPartialSuccess() {
        // verify that the organisations are attempted to be updated with a failure for org 4
        runTest(List.of("/SchedulerTests/PrdUsersByOrganisation/userOrganisation1_scenario_01.json",
                        "/SchedulerTests/PrdUsersByOrganisation/userOrganisation4_scenario_01.json"),
                EndStatus.PARTIAL_SUCCESS, 10);

        // verify that the OrganisationRefreshQueue contains 2 records, 1 active, retry=1 (end value)
        assertTotalOrganisationRefreshQueueEntitiesInDb(2, 1, 1);

        // Verify 5 active user in the refresh queue
        assertTotalUserRefreshQueueEntitiesInDb(5);
        assertUserRefreshQueueEntitiesInDb("user1", ORGANISATION_ID_1, SOLICITOR_ACCESS_TYPE,
                NEW_USER_LAST_UPDATED, true, false);
        assertUserRefreshQueueEntitiesInDb("user2", ORGANISATION_ID_1, SOLICITOR_ACCESS_TYPE,
                NEW_USER_LAST_UPDATED, true, false);
        assertUserRefreshQueueEntitiesInDb("user3", ORGANISATION_ID_1, SOLICITOR_ACCESS_TYPE,
                NEW_USER_LAST_UPDATED, true, false);
        assertUserRefreshQueueEntitiesInDb("user11", ORGANISATION_ID_4, SOLICITOR_ACCESS_TYPE,
                NEW_USER_LAST_UPDATED, true, false);
        assertUserRefreshQueueEntitiesInDb("user12", ORGANISATION_ID_4, OGD_ACCESS_TYPE,
                NEW_USER_LAST_UPDATED, true, false);
    }

    private void runTest(List<String> fileNames, EndStatus endStatus, int noOfCallsToPrd) {

        // GIVEN
        logBeforeStatus();
        stubPrdRetrieveUsersByOrg(fileNames, "false", null, endStatus);

        // WHEN
        ProcessMonitorDto processMonitorDto = prmScheduler
            .findUsersWithStaleOrganisationsAndInsertIntoRefreshQueueProcess();

        // THEN
        verifyNoOfCallsToPrd(noOfCallsToPrd, endStatus);
        logAfterStatus(processMonitorDto);

        // verify that the process monitor reports the correct status
        assertNotNull(processMonitorDto);
        assertEquals(endStatus, processMonitorDto.getEndStatus());
    }

    //#region Assertion Helpers: DB Checks

    private void assertTotalOrganisationRefreshQueueEntitiesInDb(int expectedNumberOfRecords, int expectedActiveOrgs,
        int expectedRetries) {
        var organisationRefreshQueueEntities = organisationRefreshQueueRepository.findAll();
        assertEquals(expectedNumberOfRecords, organisationRefreshQueueEntities.size(),
            "OrganisationRefreshQueueEntity number of records mismatch");
        int activeOrgs = 0;
        int retries = 0;
        for (var entity : organisationRefreshQueueEntities) {
            if (entity.getActive()) {
                activeOrgs++;
            }
            retries += entity.getRetry() != null ? entity.getRetry() : 0;
            assertRetryAfter(entity.getOrganisationId(), entity.getRetry(), entity.getRetryAfter());
        }
        assertEquals(expectedActiveOrgs, activeOrgs,
            "OrganisationRefreshQueueEntity active organisations count mismatch");
        assertEquals(expectedRetries, retries,
            "OrganisationRefreshQueueEntity retries count mismatch");
    }

    private void assertRetryAfter(String organisationId, Integer retry, LocalDateTime retryAfter) {
        // Successful retry or retry limit exceeded
        // NOTE: If retry = 0 then retryAfter is always set to now() on update,  4 is NULL
        if (retry >= 4) {
            assertNull(retryAfter,
                "OrganisationRefreshQueueEntity retryAfter not NULL for organisationId: "
                    + organisationId);
        } else {
            assertTrue(retryAfter != null
                    && assertLastUpdatedNow(retryAfter),
                "UserRefreshQueueEntity retryAfter mismatch for organisationId: "
                    + organisationId + ", " + retry + ", "
                    + retryAfter);
        }
    }

    private void assertTotalUserRefreshQueueEntitiesInDb(int expectedNumberOfRecords) {
        var userRefreshQueueEntities = userRefreshQueueRepository.findAll();
        assertEquals(expectedNumberOfRecords, userRefreshQueueEntities.size(),
            "UserRefreshQueueEntity number of records mismatch");
    }

    private void assertUserRefreshQueueEntitiesInDb(String userId, String organisationId,
        String expectedAccessTypes,
        LocalDateTime userLastUpdated, boolean isUpdated, boolean isDeleted) {
        var userRefreshQueueEntity = userRefreshQueueRepository.findById(userId);
        assertTrue(userRefreshQueueEntity.isPresent(),
            "UserRefreshQueueEntity not found for userId: " + userId);
        assertTrue(userRefreshQueueEntity.get().getActive(),
            "UserRefreshQueueEntity is not active for userId: " + userId);
        assertEquals(organisationId, userRefreshQueueEntity.get().getOrganisationId(),
            "UserRefreshQueueEntity organisationId mismatch for userId: " + userId);
        assertEquals(isUpdated,
            assertLastUpdatedNow(userRefreshQueueEntity.get().getLastUpdated()),
                "UserRefreshQueueEntity lastUpdated mismatch for userId: " + userId + ", "
                    + userRefreshQueueEntity.get().getLastUpdated());
        assertEquals(userLastUpdated,
            userRefreshQueueEntity.get().getUserLastUpdated(),
            "UserRefreshQueueEntity userLastUpdated mismatch for userId: " + userId);
        assertEquals(isDeleted,
            userRefreshQueueEntity.get().getDeleted() != null,
            "UserRefreshQueueEntity deleted mismatch for userId: " + userId);
        assertAccessTypes(expectedAccessTypes,
            userRefreshQueueEntity.get().getAccessTypes(),
            userId);
        assertEquals(0, userRefreshQueueEntity.get().getRetry(),
                "UserRefreshQueueEntity organisationId mismatch for userId: " + userId);
        assertEquals(isUpdated,
                assertLastUpdatedNow(userRefreshQueueEntity.get().getRetryAfter()),
                "UserRefreshQueueEntity retryAfter mismatch for userId: " + userId + ", "
                        + userRefreshQueueEntity.get().getRetryAfter());
    }

    private void assertAccessTypes(String expectedAccessTypes, String actualAccessTypes,
        String userId) {
        Map<String, String> expectedAccessTypesMap = getAccessTypesMap(expectedAccessTypes);
        Map<String, String> actualAccessTypesMap = getAccessTypesMap(actualAccessTypes);
        assertEquals(expectedAccessTypesMap.size(),
            getAccessTypesMap(actualAccessTypes).size(),
            "UserRefreshQueueEntity accessTypes.size mismatch for userId: " + userId);
        expectedAccessTypesMap.forEach((key, value) -> {
            assertTrue(actualAccessTypesMap.containsKey(key),
                "UserRefreshQueueEntity accessTypes does not contain key " + key
                    + " for userId: " + userId);
            assertEquals(value, actualAccessTypesMap.get(key),
                "UserRefreshQueueEntity accessTypes does not contain value " + value
                    + " for key " + key
                    + " for userId: " + userId);
        });
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

    private void verifyNoOfCallsToPrd(int noOfCalls, EndStatus endStatus) {
        var allCallEvents = logWiremockPostCalls(STUB_ID_PRD_RETRIEVE_USERSBYORG);
        // verify single call
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
        int httpStatus = !EndStatus.SUCCESS.equals(endStatus)
            ? HttpStatus.UNAUTHORIZED.value() : HttpStatus.OK.value();
        assertEquals(httpStatus, event.getResponse().getStatus(),
            "Response status mismatch");
        assertEquals("false",  event.getResponse().getHeaders().getHeader(MORE_AVAILABLE).firstValue(),
            "Response moreAvailable mismatch");
    }

}
