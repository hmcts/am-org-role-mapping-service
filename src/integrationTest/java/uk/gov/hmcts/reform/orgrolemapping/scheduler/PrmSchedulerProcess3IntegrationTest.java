package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

class PrmSchedulerProcess3IntegrationTest extends BaseSchedulerTestIntegration {

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
            .findOrganisationChangesAndInsertIntoOrganisationRefreshQueueProcess();

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
        // verify page size
        assertEquals(TEST_PAGE_SIZE, event.getRequest().getQueryParams().get("pageSize").firstValue(),
            "Response pageSize mismatch");
        // verify response status
        assertEquals(HttpStatus.OK.value(), event.getResponse().getStatus(),
            "Response status mismatch");
        assertEquals("false",  event.getResponse().getHeaders().getHeader(MORE_AVAILABLE).firstValue(),
            "Response moreAvilable mismatch");
    }

}