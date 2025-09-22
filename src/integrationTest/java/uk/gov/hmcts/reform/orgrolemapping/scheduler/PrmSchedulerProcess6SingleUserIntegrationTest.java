package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import jakarta.inject.Inject;
import java.util.List;

import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalRefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;

class PrmSchedulerProcess6SingleUserIntegrationTest extends BaseProcess6IntegrationTest {

    @Inject
    private ProfessionalRefreshOrchestrator professionalRefreshOrchestrator;

    protected void testCreateRoleAssignment(boolean organisation, boolean group) {
        runTest(organisation ? List.of("/SchedulerTests/PrdRetrieveUsers/userx_scenario_01.json")
                : List.of("/SchedulerTests/PrdRetrieveUsers/userx_scenario_02.json"),
                1, organisation, group);
    }

    private void runTest(List<String> refreshUserfileNames, int expectedNumberOfRecords,
                         boolean organisation, boolean group) {

        // GIVEN
        logBeforeStatus();
        stubPrdRefreshUser(refreshUserfileNames, USERID, "false", "false");
        stubRasCreateRoleAssignment(EndStatus.SUCCESS);

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
    }
}
