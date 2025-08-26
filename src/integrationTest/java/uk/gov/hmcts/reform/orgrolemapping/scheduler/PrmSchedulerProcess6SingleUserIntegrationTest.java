package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalRefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SUCCESS_ROLE_REFRESH;

class PrmSchedulerProcess6SingleUserIntegrationTest extends BaseSchedulerTestIntegration {

    private static final String USERID = "USERX";

    @Autowired
    private UserRefreshQueueRepository userRefreshQueueRepository;

    @Mock
    private ProcessEventTracker processEventTracker;

    @Captor
    private ArgumentCaptor<ProcessMonitorDto> processMonitorDtoArgumentCaptor;

    @Inject
    private ProfessionalRefreshOrchestrator professionalRefreshOrchestrator;


    /**
     * Create User Roles List.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/init_access_types.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateUserRole() {

        // verify that a users is updated
        runTest(List.of("/SchedulerTests/PrdRetrieveUsers/userx_scenario_01.json"));

        // Verify 1 record in the refresh queue
        assertTotalUserRefreshQueueEntitiesInDb(1);
    }

    private void runTest(List<String> fileNames) {

        // GIVEN
        logBeforeStatus();
        stubPrdRefreshUser(fileNames, USERID, "false", "false");

        // WHEN
        ResponseEntity<Object> response = professionalRefreshOrchestrator
            .refreshProfessionalUser(USERID);

        // THEN
        if (!fileNames.isEmpty()) {
            verifyNoOfCallsToPrd(1);
        }
        logAfterStatus(response);

        // verify the response
        assertResponse(response);
    }

    //#region Assertion Helpers: DB Checks

    private void assertResponse(ResponseEntity<Object> actualResponse) {
        assertNotNull(actualResponse);
        assertNotNull(actualResponse.getBody());
        assertEquals(actualResponse.getBody(), Map.of("Message", SUCCESS_ROLE_REFRESH));
        //verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        //assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
        //    .isEqualTo(EndStatus.SUCCESS);
    }

    private void assertTotalUserRefreshQueueEntitiesInDb(int expectedNumberOfRecords) {
        var userRefreshQueueEntities = userRefreshQueueRepository.findAll();
        assertEquals(expectedNumberOfRecords, userRefreshQueueEntities.size(),
            "UserRefreshQueueEntity number of records mismatch");
        assertEquals(0, userRefreshQueueEntities.stream()
                .filter(entity -> entity.getActive()).count(),
            "UserRefreshQueueEntity number of active records mismatch");
    }

    //#endregion

    private void logAfterStatus(ResponseEntity<Object> response) {
        logObject("ProcessMonitorDto: AFTER", response);
        logObject("userRefreshQueueRepository: AFTER", userRefreshQueueRepository.findAll());
    }

    private void logBeforeStatus() {
        logObject("userRefreshQueueRepository: BEFORE", userRefreshQueueRepository.findAll());
    }


    private void verifyNoOfCallsToPrd(int noOfCalls) {
        var allCallEvents = logWiremockPostCalls(STUB_ID_PRD_REFRESH_USER);
        // verify number of calls
        assertEquals(noOfCalls, allCallEvents.size(),
                "Unexpected number of calls to PRD service");
        ServeEvent event  = allCallEvents.get(0);
        // verify response status
        assertEquals(HttpStatus.OK.value(), event.getResponse().getStatus(),
                "Response status mismatch");
    }
}
