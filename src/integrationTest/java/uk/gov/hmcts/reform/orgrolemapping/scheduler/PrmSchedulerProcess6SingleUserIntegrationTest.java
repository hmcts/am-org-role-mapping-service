package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import jakarta.inject.Inject;
import java.util.List;

import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalRefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;

class PrmSchedulerProcess6SingleUserIntegrationTest extends BaseProcess6IntegrationTest {

    @Inject
    private ProfessionalRefreshOrchestrator professionalRefreshOrchestrator;

    @Override
    protected void testSingleRole(boolean user) {
        // verify that no users are updated
        testCreateRole(user ? List.of("/SchedulerTests/PrdRetrieveUsers/userx_scenario_01.json")
                : List.of("/SchedulerTests/PrdRetrieveUsers/userx_scenario_02.json"),
                List.of("/SchedulerTests/role_assignments/senior_tribunal_caseworker.json"),
                1, user);
    }

    private void testCreateRole(List<String> refreshUserfileNames, List<String> roleAssignmentfileNames,
                                int expectedNumberOfRecords, boolean user) {
        // verify that no users are updated
        runTest(refreshUserfileNames, roleAssignmentfileNames);

        // Verify the number of records in the user refresh queue
        assertTotalUserRefreshQueueEntitiesInDb(expectedNumberOfRecords);

        // Verify the access types against the user record
        assertAccessTypes(user ? "BEFTA_ACCESSTYPE_1" : "BEFTA_ACCESSTYPE_2",
                user ? "" : "\"ORGPROFILE1\"", "\"BEFTA_JURISDICTION_1\"", user);
    }

    private void runTest(List<String> refreshUserfileNames, List<String> roleAssignmentfileNames) {

        // GIVEN
        logBeforeStatus();
        stubPrdRefreshUser(refreshUserfileNames, USERID, "false", "false");
        stubRasCreateRoleAssignment(roleAssignmentfileNames, EndStatus.SUCCESS);

        // WHEN
        ResponseEntity<Object> response = professionalRefreshOrchestrator
            .refreshProfessionalUser(USERID);

        // THEN
        if (!refreshUserfileNames.isEmpty()) {
            verifyNoOfCallsToPrd(1);
        }
        if (!roleAssignmentfileNames.isEmpty()) {
            verifyNoOfCallsToRas(1);
        }
        logAfterStatus(response);

        // verify the response
        assertResponse(response);
    }
}
