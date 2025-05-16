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
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

class PrmSchedulerProcess3IntegrationTest extends BaseSchedulerTestIntegration {

    private static final DateTimeFormatter DTF =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final LocalDateTime OLD_ORGANISATION_LAST_UPDATED =
        LocalDateTime.parse("2020-01-01T13:20:01.046Z", DTF);
    private static final LocalDateTime NEW_ORGANISATION_LAST_UPDATED =
        LocalDateTime.parse("2023-11-20T15:51:33.046Z", DTF);

    private static final Integer TOLERANCE_MINUTES = 1;

    @Autowired
    private OrganisationRefreshQueueRepository organisationRefreshQueueRepository;

    @Autowired
    private BatchLastRunTimestampRepository batchLastRunTimestampRepository;

    @Autowired
    private Scheduler prmScheduler;

    /**
     * No change - Empty organisation list with no new organisation.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/init_access_types.sql",
        "classpath:sql/prm/batch_last_run_timestamp/init_batch_last_run_timestamp.sql",
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql"
    })
    void testNoOrgChangeNoExistingProfiles() {

        // verify that no organisations are updated
        runTest(List.of());

        // verify that the OrganisationRefreshQueue remains empty
        assertTotalOrganisationRefreshQueueEntitiesInDb(0);
    }

    /**
     * Add Organisation - Empty organisation list with a new organisation.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/init_access_types.sql",
        "classpath:sql/prm/batch_last_run_timestamp/init_batch_last_run_timestamp.sql",
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql"
    })
    void testAddOrgNoExistingProfile() {

        // verify that the Organisation is added
        runTest(List.of(
            "/SchedulerTests/PrdOrganisationInfo/organisation1_scenario_01.json"
        ));

        // verify that the OrganisationRefreshQueue contains 3 records
        assertTotalOrganisationRefreshQueueEntitiesInDb(1);

        // verify that the OranisationRefreshQueue contains the expected OrganisationProfileId and set to active
        assertOrganisationRefreshQueueEntitiesInDb("1", 0, true, NEW_ORGANISATION_LAST_UPDATED, true);
    }

    /**
     * Update Organisation - Existing organisation list with an organisation update.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/init_access_types.sql",
        "classpath:sql/prm/batch_last_run_timestamp/init_batch_last_run_timestamp.sql",
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation1.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation2.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation3.sql"
    })
    void testUpdateOrgExistingProfile() {

        // verify that the Organisations are updated
        runTest(List.of(
            "/SchedulerTests/PrdOrganisationInfo/organisation1_scenario_01.json"
        ));

        // verify that the OrganisationRefreshQueue contains 3 records
        assertTotalOrganisationRefreshQueueEntitiesInDb(3);

        // verify that the OranisationRefreshQueue contains the expected OrganisationProfileId and set to active
        assertOrganisationRefreshQueueEntitiesInDb("1", 2, true, NEW_ORGANISATION_LAST_UPDATED, true);
        assertOrganisationRefreshQueueEntitiesInDb("2", 2, true, OLD_ORGANISATION_LAST_UPDATED, false);
        assertOrganisationRefreshQueueEntitiesInDb("3", 1, false, OLD_ORGANISATION_LAST_UPDATED, false);
    }

    /**
     * Add and Update Organisations - Existing organisation list with an organisation added and another updated.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/init_access_types.sql",
        "classpath:sql/prm/batch_last_run_timestamp/init_batch_last_run_timestamp.sql",
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation1.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation3.sql"
    })
    void testAddAndUpdateOrgExistingProfile() {

        // verify that the Organisations are added/updated (plus pagination as pagesize=3)
        runTest(List.of(
            "/SchedulerTests/PrdOrganisationInfo/organisation1_scenario_01.json",
            "/SchedulerTests/PrdOrganisationInfo/organisation2_scenario_01.json",
            "/SchedulerTests/PrdOrganisationInfo/organisation3_scenario_01.json",
            "/SchedulerTests/PrdOrganisationInfo/organisation4_scenario_01.json"
        ));

        // verify that the OrganisationRefreshQueue contains 4 records
        assertTotalOrganisationRefreshQueueEntitiesInDb(4);

        // verify that the OranisationRefreshQueue contains the expected OrganisationProfileId and set to active
        assertOrganisationRefreshQueueEntitiesInDb("1", 2, true, NEW_ORGANISATION_LAST_UPDATED, true);
        assertOrganisationRefreshQueueEntitiesInDb("2", 0, true, NEW_ORGANISATION_LAST_UPDATED, true);
        assertOrganisationRefreshQueueEntitiesInDb("3", 1, true, NEW_ORGANISATION_LAST_UPDATED, true);
        assertOrganisationRefreshQueueEntitiesInDb("4", 0, true, NEW_ORGANISATION_LAST_UPDATED, true);
    }

    /**
     * Add New Organisation - Test Higher AccessTypeMinVersion.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_highversion_access_type.sql",
        "classpath:sql/prm/batch_last_run_timestamp/init_batch_last_run_timestamp.sql",
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql"
    })
    void testOrgHigherAccessTypesMinVersion() {

        // verify that the Organisations are added
        runTest(List.of(
            "/SchedulerTests/PrdOrganisationInfo/organisation1_scenario_01.json"
        ));

        // verify that the OrganisationRefreshQueue contains 1 record
        assertTotalOrganisationRefreshQueueEntitiesInDb(1);

        // verify that the OranisationRefreshQueue contains the expected OrganisationProfileId and set to active
        assertOrganisationRefreshQueueEntitiesInDb("1", 50, true, NEW_ORGANISATION_LAST_UPDATED, true);
    }

    /**
     * Add New Organisation - Test Same AccessTypeMinVersion.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_civil_access_type.sql",
        "classpath:sql/prm/batch_last_run_timestamp/init_batch_last_run_timestamp.sql",
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql"
    })
    void testOrgSameAccessTypesMinVersion() {

        // verify that the Organisations are added
        runTest(List.of(
            "/SchedulerTests/PrdOrganisationInfo/organisation3_scenario_01.json"
        ));

        // verify that the OrganisationRefreshQueue contains 1 record
        assertTotalOrganisationRefreshQueueEntitiesInDb(1);

        // verify that the OranisationRefreshQueue contains the expected OrganisationProfileId and set to active
        assertOrganisationRefreshQueueEntitiesInDb("3", 1, true, NEW_ORGANISATION_LAST_UPDATED, true);
    }

    private void runTest(List<String> fileNames) {

        // GIVEN
        logBeforeStatus();
        stubPrdRetrieveOrganisations(fileNames, "false", null);

        // WHEN
        ProcessMonitorDto processMonitorDto = prmScheduler
            .findOrganisationChangesAndInsertIntoOrganisationRefreshQueueProcess();

        // THEN
        if (!fileNames.isEmpty()) {
            verifySingleCallToPrd();
        }
        logAfterStatus(processMonitorDto);

        // verify that the process monitor reports success
        assertEquals(EndStatus.SUCCESS, processMonitorDto.getEndStatus(),
            "Invalid process monitor end status");

        // verify the last organisation run date time has been updated
        assertBatchLastRunTimestampEntity();
    }

    //#region Assertion Helpers: DB Checks

    private void assertBatchLastRunTimestampEntity() {
        List<BatchLastRunTimestampEntity> allBatches = batchLastRunTimestampRepository.findAll();
        // verify that the BatchLastRunTimestampEntity contains a single record
        assertEquals(1, allBatches.size(),
            "BatchLastRunTimestampEntity single record not found");
        assertTrue(assertLastUpdatedNow(allBatches.get(0).getLastOrganisationRunDatetime(),
                TOLERANCE_MINUTES), "BatchLastRunTimestampEntity.LastOrganisationRunDatetime not updated");
    }

    private void assertTotalOrganisationRefreshQueueEntitiesInDb(int expectedNumberOfRecords) {
        var organisationRefreshQueueEntities = organisationRefreshQueueRepository.findAll();
        assertEquals(expectedNumberOfRecords, organisationRefreshQueueEntities.size(),
            "OrganisationRefreshQueueEntity number of records mismatch");
    }

    private void assertOrganisationRefreshQueueEntitiesInDb(String organisationIdentifierId,
        int expectedAccessTypesMinVersion,
        boolean expectedActive,
        LocalDateTime expectedOrganisationLastUpdated,
        boolean lastUpdatedNow) {
        var profileRefreshQueueEntity = organisationRefreshQueueRepository.findById(organisationIdentifierId);
        assertTrue(profileRefreshQueueEntity.isPresent(), "OrganisationRefreshQueueEntity not found");
        assertEquals(expectedAccessTypesMinVersion, profileRefreshQueueEntity.get().getAccessTypesMinVersion(),
            "OrganisationRefreshQueueEntity.AccessTypesMinVersion mismatch");
        assertEquals(expectedActive, profileRefreshQueueEntity.get().getActive(),
            "OrganisationRefreshQueueEntity.Active status mismatch");
        assertEquals(expectedOrganisationLastUpdated,
            profileRefreshQueueEntity.get().getOrganisationLastUpdated(),
            "OrganisationRefreshQueueEntity.OrganisationLastUpdated mismatch");
        assertEquals(lastUpdatedNow, assertLastUpdatedNow(profileRefreshQueueEntity.get().getLastUpdated(), 1),
            "OrganisationRefreshQueueEntity.LastUpdated mismatch");
    }

    private boolean assertLastUpdatedNow(LocalDateTime lastUpdated, int minutes) {
        return lastUpdated.isAfter(LocalDateTime.now().minusMinutes(minutes));
    }

    //#endregion

    private void logAfterStatus(ProcessMonitorDto processMonitorDto) {
        logObject("ProcessMonitorDto: AFTER", processMonitorDto);
        logObject("OrganisationRefreshQueueRepository: AFTER", organisationRefreshQueueRepository.findAll());
    }

    private void logBeforeStatus() {
        logObject("OrganisationRefreshQueueRepository: BEFORE", organisationRefreshQueueRepository.findAll());
    }

    private void verifySingleCallToPrd() {
        var allCallEvents = logWiremockPostCalls(STUB_ID_PRD_RETRIEVE_ORGANISATIONS);
        // verify single call
        assertEquals(1, allCallEvents.size(),
            "Unexpected number of calls to PRD service");
        var event = allCallEvents.get(0);
        // verify response status
        assertEquals(HttpStatus.OK.value(), event.getResponse().getStatus(),
            "Response status mismatch");
        assertEquals("false",  event.getResponse().getHeaders().getHeader(MORE_AVAILABLE).firstValue(),
            "Response moreAvilable mismatch");
    }

}