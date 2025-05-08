package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.AUTHORIZATION;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SERVICE_AUTHORIZATION;

import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

@Slf4j
class PrmSchedulerProcess2IntegrationTest extends BaseSchedulerTestIntegration {

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
    void testNoChange_emptyPrdResponse() {

        // verify that no organisations are updated
        runTest(List.of());

        // verify that the ProfileRefreshQueue remains empty
        assertTotalProfileRefreshQueueEntitiesInDb(0);

        // verify that the OrganisationRefreshQueue remains empty
        assertTotalOrganisationRefreshQueueEntitiesInDb(0);
    }

    /**
     * New Organisations - Insert three organisations to an empty list.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/profile_refresh_queue/insert_Solicitor_Profile.sql",
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql"
    })
    void testNewOrganisation_singlePrdResponse() {

        // verify that the Organisations are updated (i.e. version 1) and has 1 organisation profile
        runTest(List.of(
            "/SchedulerTests/PrdOrganisationInfo/organisation1_scenario_01.json",
            "/SchedulerTests/PrdOrganisationInfo/organisation2_scenario_01.json",
            "/SchedulerTests/PrdOrganisationInfo/organisation3_scenario_01.json"
        ));

        // verify that the ProfileRefreshQueue contains the expected OrganisationProfileId and set to inactive
        assertProfileRefreshQueueEntityInDb(SOLICITOR_PROFILE, 1, false);

        // verify that the OranisationRefreshQueue contains the expected OrganisationProfileId and set to active
        assertOrganisationRefreshQueueEntitiesInDb("1", 1, true, true);
        assertOrganisationRefreshQueueEntitiesInDb("2", 1, true, true);
        assertOrganisationRefreshQueueEntitiesInDb("3", 1, true, true);
    }

    private void runTest(List<String> jurisdictionFileNames) {

        // GIVEN
        logBeforeStatus();
        // stub the PRD service call with response for test scenario
        stubPrdRetrieveOrganisations(jurisdictionFileNames);

        // WHEN
        ProcessMonitorDto processMonitorDto = prmScheduler
            .findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueProcess();

        // THEN
        verifySingleCallToPrd();
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
        boolean expectedOrganisationLastUpdatedNow) {
        var profileRefreshQueueEntity = organisationRefreshQueueRepository.findById(organisationIdentifierId);
        assertTrue(profileRefreshQueueEntity.isPresent(), "OrganisationRefreshQueueEntity not found");
        assertEquals(expectedAccessTypesMinVersion, profileRefreshQueueEntity.get().getAccessTypesMinVersion(),
            "OrganisationRefreshQueueEntity.AccessTypesMinVersion mismatch");
        assertEquals(expectedActive, profileRefreshQueueEntity.get().getActive(),
            "OrganisationRefreshQueueEntity.Active status mismatch");
        assertEquals(expectedOrganisationLastUpdatedNow,
            assertLastUpdatedNow(profileRefreshQueueEntity.get().getLastUpdated()),
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

    private void verifySingleCallToPrd() {
        var allCallEvents = logWiremockPostCalls(STUB_ID_PRD_RETRIEVE_ORGANISATIONS);
        // verify single call
        assertEquals(1, allCallEvents.size(),
            "Unexpected number of calls to PRD service");
        var event = allCallEvents.get(0);
        // verify request headers contain Auth tokens
        var request = event.getRequest();
        // verify response status
        assertEquals(HttpStatus.OK.value(), event.getResponse().getStatus(),
            "Response status mismatch");
    }

}
