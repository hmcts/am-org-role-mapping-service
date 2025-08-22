package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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

    private static final DateTimeFormatter DTF =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final LocalDateTime OLD_USER_LAST_UPDATED =
        LocalDateTime.parse("2020-01-01T13:30:01.046Z", DTF);
    private static final LocalDateTime NEW_USER_LAST_UPDATED =
        LocalDateTime.parse("2023-09-19T15:36:33.653Z", DTF);
    private static final String SINCE = "1999-12-31T23:57:00";
    private static final String ACTIVE = "ACTIVE";
    private static final String INACTIVE = "INACTIVE";
    private static final String ORGANISATION_ID_3 = "3";
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
        "classpath:sql/prm/access_types/insert_multipleprofile_access_type.sql",
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql"
    })
    void testNoUsers() {

        // verify that no users are updated
        runTest(List.of(), 1);

        // Verify no active users in the refresh queue
        assertTotalUserRefreshQueueEntitiesInDb(0);

        // verify the last user run date time has NOT been updated
        assertBatchLastRunTimestampEntity(false);
    }

    /**
     * 1 x No Change / 1 x Update / 1 x Delete.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/batch_last_run_timestamp/init_batch_last_run_timestamp.sql",
        "classpath:sql/prm/access_types/insert_multipleprofile_access_type.sql",
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation3.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user1organisation3.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user2organisation3.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user3organisation3.sql"
    })
    void testExistingUsers() {

        // verify that a user is updated
        runTest(List.of("/SchedulerTests/PrdRetrieveUsers/user1_scenario_02.json",
                "/SchedulerTests/PrdRetrieveUsers/user2_scenario_01.json",
                "/SchedulerTests/PrdRetrieveUsers/user3_scenario_01.json"),
            1);

        // Verify no active users in the refresh queue
        assertTotalUserRefreshQueueEntitiesInDb(3);

        // verify the last user run date time has been updated
        assertBatchLastRunTimestampEntity(true);
        // verify that user1 is NOT updated
        assertUserRefreshQueueEntitiesInDb("user1", ORGANISATION_ID_3, INACTIVE,
            new String[] {"SOLICITOR_PROFILE"}, 2,
            OLD_USER_LAST_UPDATED, false, false);
        // verify that user2 is updated
        assertUserRefreshQueueEntitiesInDb("user2", ORGANISATION_ID_3, ACTIVE,
            new String[] {"SOLICITOR_PROFILE", "ODG_PROFILE"}, 2,
            NEW_USER_LAST_UPDATED, true, false);
        // verify that user3 is deleted
        assertUserRefreshQueueEntitiesInDb("user3", ORGANISATION_ID_3, ACTIVE,
            new String[] {"SOLICITOR_PROFILE"}, 2,
            NEW_USER_LAST_UPDATED, true, true);
    }

    /**
     * Update - New User.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/batch_last_run_timestamp/init_batch_last_run_timestamp.sql",
        "classpath:sql/prm/access_types/insert_highversion_access_type.sql",
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation3.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql"
    })
    void testNewUser() {

        // verify that a user is updated
        runTest(List.of("/SchedulerTests/PrdRetrieveUsers/user1_scenario_01.json"),
            1);

        // Verify no active users in the refresh queue
        assertTotalUserRefreshQueueEntitiesInDb(1);

        // verify the last user run date time has been updated
        assertBatchLastRunTimestampEntity(true);
        assertUserRefreshQueueEntitiesInDb("user1", ORGANISATION_ID_3, ACTIVE,
            new String[] {"SOLICITOR_PROFILE"}, 50,
            NEW_USER_LAST_UPDATED, true, false);
    }

    private void runTest(List<String> fileNames, Integer pageSize) {

        // GIVEN
        logBeforeStatus();
        int roundingOffSet = pageSize - 1;
        Integer numberOfPages = fileNames.size() == 0 ? 1 :
            (fileNames.size() + roundingOffSet) / pageSize;
        String moreAvailable;
        String lastRecordInPage;
        String searchAfter;
        // loop the stub calls
        for (int pageNo = 1; pageNo <= numberOfPages; pageNo++) {
            moreAvailable = pageNo == numberOfPages ? "false" : "true";
            lastRecordInPage = pageNo == numberOfPages ? null : String.valueOf(pageNo);
            // 1st page has no searchAfter
            searchAfter = pageNo == 1 ? null : String.valueOf(pageNo - 1);
            // stub the PRD service call with response for test scenario
            stubPrdRetrieveUsers(fileNames, moreAvailable, lastRecordInPage, pageSize.toString(),
                searchAfter);
        }

        // WHEN
        ProcessMonitorDto processMonitorDto = prmScheduler
            .findUserChangesAndInsertIntoUserRefreshQueue();

        // THEN
        if (!fileNames.isEmpty()) {
            verifyNoOfCallsToPrd(numberOfPages);
        }
        logAfterStatus(processMonitorDto);

        // verify that the process monitor reports the correct status
        assertEquals(EndStatus.SUCCESS, processMonitorDto.getEndStatus());
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

    private void assertUserRefreshQueueEntitiesInDb(String userId, String organisationId,
        String organisationStatus, String[] organisationProfileIds, Integer accessTypeMinVersion,
        LocalDateTime userLastUpdated, boolean isUpdated, boolean isDeleted) {
        var userRefreshQueueEntity = userRefreshQueueRepository.findById(userId);
        assertTrue(userRefreshQueueEntity.isPresent(),
            "UserRefreshQueueEntity not found for userId: " + userId);
        assertTrue(userRefreshQueueEntity.get().getActive(),
            "UserRefreshQueueEntity is not active for userId: " + userId);
        assertEquals(organisationId, userRefreshQueueEntity.get().getOrganisationId(),
            "UserRefreshQueueEntity organisationId mismatch for userId: " + userId);
        assertEquals(organisationStatus, userRefreshQueueEntity.get().getOrganisationStatus(),
            "UserRefreshQueueEntity oragnisationStatus mismatch for userId: " + userId);
        assertEquals(accessTypeMinVersion, userRefreshQueueEntity.get().getAccessTypesMinVersion(),
            "UserRefreshQueueEntity accessTypesMinVersio mismatch for userId: " + userId);
        assertEquals(isUpdated,
            assertLastUpdatedNow(userRefreshQueueEntity.get().getLastUpdated(), TOLERANCE_MINUTES),
            "UserRefreshQueueEntity lastUpdated mismatch for userId: " + userId + ", "
                + userRefreshQueueEntity.get().getLastUpdated());
        assertEquals(userLastUpdated,
            userRefreshQueueEntity.get().getUserLastUpdated(),
            "UserRefreshQueueEntity userLastUpdated mismatch for userId: " + userId);
        assertEquals(isDeleted,
            userRefreshQueueEntity.get().getDeleted() != null,
            "UserRefreshQueueEntity deleted mismatch for userId: " + userId);
        assertOrganisationProfileIds(organisationProfileIds,
            userRefreshQueueEntity.get().getOrganisationProfileIds(), userId);
        if (isUpdated) {
            assertAccessTypes(organisationProfileIds, userRefreshQueueEntity.get().getAccessTypes(),
                userId);
        } else {
            assertEquals("[]", userRefreshQueueEntity.get().getAccessTypes(),
                "UserRefreshQueueEntity accessTYpes mismatch for userId: " + userId);
        }
    }

    private void assertAccessTypes(String[] expectedOrganisationProfileIds, String accessTypes,
        String userId) {
        Arrays.asList(expectedOrganisationProfileIds).forEach(profileId -> {
            assertTrue(accessTypes.contains(profileId),
                "UserRefreshQueueEntity accessTypes does not contain " + profileId
                + " for userId: " + userId);
        });
    }

    private void assertOrganisationProfileIds(String[] expectedOrganisationProfileIds,
        String[] actualOrganisationProfileIds, String userId) {
        assertEquals(expectedOrganisationProfileIds.length, actualOrganisationProfileIds.length,
            "UserRefreshQueueEntity organisationProfileIds.length mismatch for userId: " + userId);
        assertEquals(Arrays.asList(expectedOrganisationProfileIds),
            Arrays.asList(actualOrganisationProfileIds),
            "UserRefreshQueueEntity organisationProfileIds mismatch for userId: " + userId);
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
        ServeEvent event;
        for (int callNo = 1; callNo <= noOfCalls; callNo++) {
            event = allCallEvents.get(callNo - 1);
            // verify response status
            assertEquals(TEST_PAGE_SIZE,
                event.getRequest().getQueryParams().get("pageSize").firstValue(),
                "Response pageSize mismatch on call " + callNo);
            // verify response since
            assertEquals(SINCE, event.getRequest().getQueryParams().get("since").firstValue(),
                "Response since mismatch on call " + callNo);
            // verify response status
            assertEquals(HttpStatus.OK.value(), event.getResponse().getStatus(),
                "Response status mismatch on call " + callNo);
            // Calls are listed in reserve, so the first call is the last page
            assertEquals(callNo == 1 ? "false" : "true",
                event.getResponse().getHeaders().getHeader(MORE_AVAILABLE).firstValue(),
                "Response moreAvilable mismatch on call " + callNo);
            assertEquals(noOfCalls == callNo ? "" : String.valueOf(noOfCalls - callNo),
                event.getResponse().getHeaders().getHeader(SEARCH_AFTER).firstValue(),
                "Response searchAfter mismatch on call " + callNo);
        }
    }

}
