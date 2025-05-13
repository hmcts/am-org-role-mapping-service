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
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

class PrmSchedulerProcess2IntegrationTest extends BaseSchedulerTestIntegration {

    private static final DateTimeFormatter DTF =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final LocalDateTime OLD_ORGANISATION_LAST_UPDATED =
        LocalDateTime.parse("2020-01-01T13:20:01.046Z", DTF);
    private static final LocalDateTime NEW_ORGANISATION_LAST_UPDATED =
        LocalDateTime.parse("2023-11-20T15:51:33.046Z", DTF);

    @Autowired
    private ProfileRefreshQueueRepository profileRefreshQueueRepository;

    @Autowired
    private OrganisationRefreshQueueRepository organisationRefreshQueueRepository;

    @Autowired
    private Scheduler prmScheduler;

    /**
     * No change - Empty organisation list with no new organisation.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/profile_refresh_queue/init_profile_refresh_queue.sql",
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql"
    })
    void testNoOrgChangeNoExistingProfiles() {

        // verify that no organisations are updated
        runTest(List.of(), Integer.valueOf(TEST_PAGE_SIZE));

        // verify that the ProfileRefreshQueue remains empty
        assertTotalProfileRefreshQueueEntitiesInDb(0);

        // verify that the OrganisationRefreshQueue remains empty
        assertTotalOrganisationRefreshQueueEntitiesInDb(0);
    }

    /**
     * New Organisations - Insert organisations to an empty list.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/profile_refresh_queue/init_profile_refresh_queue.sql",
        "classpath:sql/prm/profile_refresh_queue/insert_Solicitor_Profile.sql",
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql"
    })
    void testOrgChangeExistingProfile() {

        // verify that the Organisations are updated
        runTest(List.of(
            "/SchedulerTests/PrdOrganisationInfo/organisation1_scenario_01.json",
            "/SchedulerTests/PrdOrganisationInfo/organisation2_scenario_01.json",
            "/SchedulerTests/PrdOrganisationInfo/organisation3_scenario_01.json"
        ), Integer.valueOf(TEST_PAGE_SIZE));

        // verify that the ProfileRefreshQueue contains the expected OrganisationProfileId and set to inactive
        assertProfileRefreshQueueEntityInDb(SOLICITOR_PROFILE, 1, false);

        // verify that the OranisationRefreshQueue contains the expected OrganisationProfileId and set to active
        assertOrganisationRefreshQueueEntitiesInDb("1", 1, true, NEW_ORGANISATION_LAST_UPDATED, true);
        assertOrganisationRefreshQueueEntitiesInDb("2", 1, true, NEW_ORGANISATION_LAST_UPDATED, true);
        assertOrganisationRefreshQueueEntitiesInDb("3", 1, true, NEW_ORGANISATION_LAST_UPDATED, true);
    }

    /**
     * New Organisations (PageSize 1) - Insert organisations to an empty list.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/profile_refresh_queue/init_profile_refresh_queue.sql",
        "classpath:sql/prm/profile_refresh_queue/insert_Solicitor_Profile.sql",
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql"
    })
    void testOrgChangeExistingProfilePageSize1() {

        // verify that the Organisations are updated
        runTest(List.of(
            "/SchedulerTests/PrdOrganisationInfo/organisation1_scenario_01.json",
            "/SchedulerTests/PrdOrganisationInfo/organisation2_scenario_01.json",
            "/SchedulerTests/PrdOrganisationInfo/organisation3_scenario_01.json"
        ), 1);

        // verify that the ProfileRefreshQueue contains the expected OrganisationProfileId and set to inactive
        assertProfileRefreshQueueEntityInDb(SOLICITOR_PROFILE, 1, false);

        // verify that the OranisationRefreshQueue contains the expected OrganisationProfileId and set to active
        assertOrganisationRefreshQueueEntitiesInDb("1", 1, true, NEW_ORGANISATION_LAST_UPDATED, true);
        assertOrganisationRefreshQueueEntitiesInDb("2", 1, true, NEW_ORGANISATION_LAST_UPDATED, true);
        assertOrganisationRefreshQueueEntitiesInDb("3", 1, true, NEW_ORGANISATION_LAST_UPDATED, true);
    }

    /**
     * New Organisations - Insert organisations to an already populated list.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/profile_refresh_queue/init_profile_refresh_queue.sql",
        "classpath:sql/prm/profile_refresh_queue/insert_OGD_Profile.sql",
        "classpath:sql/prm/profile_refresh_queue/insert_Solicitor_Profile.sql",
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation1.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation2.sql"
    })
    void testOrgChangeExistingMultipleProfileUpdate() {

        // verify that the Organisation 3 is new and updated, 1 and 2 are existing and not updated
        runTest(List.of(
            "/SchedulerTests/PrdOrganisationInfo/organisation1_scenario_02.json",
            "/SchedulerTests/PrdOrganisationInfo/organisation3_scenario_01.json"
        ), Integer.valueOf(TEST_PAGE_SIZE));

        // verify that the ProfileRefreshQueue contains the expected OrganisationProfileId and set to inactive
        assertProfileRefreshQueueEntityInDb(SOLICITOR_PROFILE, 1, false);
        assertProfileRefreshQueueEntityInDb(OGD_PROFILE, 2, false);

        // verify that the OranisationRefreshQueue contains the expected OrganisationProfileId and set to active
        assertOrganisationRefreshQueueEntitiesInDb("1", 2, true, OLD_ORGANISATION_LAST_UPDATED, false);
        assertOrganisationRefreshQueueEntitiesInDb("2", 2, true, OLD_ORGANISATION_LAST_UPDATED, false);
        assertOrganisationRefreshQueueEntitiesInDb("3", 2, true, NEW_ORGANISATION_LAST_UPDATED, true);
    }

    /**
     * New Organisations (PageSize=2) - Insert organisations to an already populated list.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/profile_refresh_queue/init_profile_refresh_queue.sql",
        "classpath:sql/prm/profile_refresh_queue/insert_OGD_Profile.sql",
        "classpath:sql/prm/profile_refresh_queue/insert_Solicitor_Profile.sql",
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation1.sql",
        "classpath:sql/prm/organisation_refresh_queue/insert_organisation2.sql"
    })
    void testOrgChangeExistingProfileUpdatesPageSize2() {

        // verify that the Organisation 3 is new and updated, 1 and 2 are existing and not updated
        runTest(List.of(
            "/SchedulerTests/PrdOrganisationInfo/organisation1_scenario_02.json",
            "/SchedulerTests/PrdOrganisationInfo/organisation3_scenario_01.json"
        ), 2);

        // verify that the ProfileRefreshQueue contains the expected OrganisationProfileId and set to inactive
        assertProfileRefreshQueueEntityInDb(SOLICITOR_PROFILE, 1, false);
        assertProfileRefreshQueueEntityInDb(OGD_PROFILE, 2, false);

        // verify that the OranisationRefreshQueue contains the expected OrganisationProfileId and set to active
        assertOrganisationRefreshQueueEntitiesInDb("1", 2, true, OLD_ORGANISATION_LAST_UPDATED, false);
        assertOrganisationRefreshQueueEntitiesInDb("2", 2, true, OLD_ORGANISATION_LAST_UPDATED, false);
        assertOrganisationRefreshQueueEntitiesInDb("3", 2, true, NEW_ORGANISATION_LAST_UPDATED, true);
    }

    private void runTest(List<String> fileNames, Integer pageSize) {

        // GIVEN
        logBeforeStatus();
        int roundingOffSet = pageSize - 1;
        Integer numberOfPages = (fileNames.size() + roundingOffSet) / pageSize;
        String moreAvailable;
        String lastRecordInPage;
        // loop the stub calls
        for (int pageNo = 1; pageNo <= numberOfPages; pageNo++) {
            moreAvailable = pageNo == numberOfPages ? "false" : "true";
            lastRecordInPage = pageNo == numberOfPages ? null : String.valueOf(pageNo);
            // stub the PRD service call with response for test scenario
            stubPrdRetrieveOrganisations(fileNames, moreAvailable, lastRecordInPage, pageSize.toString());
        }

        // WHEN
        ProcessMonitorDto processMonitorDto = prmScheduler
            .findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueProcess();

        // THEN
        if (!fileNames.isEmpty()) {
            verifySingleCallToPrd(pageSize);
        }
        logAfterStatus(processMonitorDto);

        // verify that the process monitor reports success
        assertEquals(EndStatus.SUCCESS, processMonitorDto.getEndStatus(),
            "Invalid process monitor end status");
    }

    //#region Assertion Helpers: DB Checks

    private void assertTotalProfileRefreshQueueEntitiesInDb(int expectedNumberOfRecords) {
        var profileRefreshQueueEntities = profileRefreshQueueRepository.findAll();
        assertEquals(expectedNumberOfRecords, profileRefreshQueueEntities.size(),
            "ProfileRefreshQueueEntity number of records mismatch");
    }

    private void assertTotalOrganisationRefreshQueueEntitiesInDb(int expectedNumberOfRecords) {
        var organisationRefreshQueueEntities = organisationRefreshQueueRepository.findAll();
        assertEquals(expectedNumberOfRecords, organisationRefreshQueueEntities.size(),
            "OrganisationRefreshQueueEntity number of records mismatch");
    }

    private void assertProfileRefreshQueueEntityInDb(String organisationProfileId,
        int expectedAccessTypesMinVersion, boolean expectedActive) {
        var profileRefreshQueueEntity = profileRefreshQueueRepository.findById(organisationProfileId);
        assertTrue(profileRefreshQueueEntity.isPresent(), "ProfileRefreshQueueEntity not found");
        assertEquals(expectedAccessTypesMinVersion, profileRefreshQueueEntity.get().getAccessTypesMinVersion(),
            "ProfileRefreshQueueEntity.AccessTypesMinVersion mismatch");
        assertEquals(expectedActive, profileRefreshQueueEntity.get().getActive(),
            "ProfileRefreshQueueEntity.Active status mismatch");
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
        assertEquals(lastUpdatedNow, assertLastUpdatedNow(profileRefreshQueueEntity.get().getLastUpdated()),
            "OrganisationRefreshQueueEntity.LastUpdated mismatch");
    }

    private boolean assertLastUpdatedNow(LocalDateTime lastUpdated) {
        return lastUpdated.isAfter(LocalDateTime.now().minusMinutes(1));
    }

    //#endregion

    private void logAfterStatus(ProcessMonitorDto processMonitorDto) {
        logObject("ProcessMonitorDto: AFTER", processMonitorDto);
        logObject("ProfileRefreshQueueRepository: AFTER", profileRefreshQueueRepository.getActiveProfileEntities());
        logObject("OrganisationRefreshQueueRepository: AFTER", organisationRefreshQueueRepository.findAll());
    }

    private void logBeforeStatus() {
        logObject("ProfileRefreshQueueRepository: BEFORE", profileRefreshQueueRepository.getActiveProfileEntities());
        logObject("OrganisationRefreshQueueRepository: BEFORE", organisationRefreshQueueRepository.findAll());
    }

    private void verifySingleCallToPrd(Integer pageSize) {
        var allCallEvents = logWiremockPostCalls(STUB_ID_PRD_RETRIEVE_ORGANISATIONS);
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
            "Response moreAvilable mismatch");
    }

}
