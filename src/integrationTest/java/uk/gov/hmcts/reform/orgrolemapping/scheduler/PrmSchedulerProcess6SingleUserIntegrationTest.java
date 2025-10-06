package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import java.util.List;

import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.controller.RefreshController;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_ORM;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.getHttpHeaders;

class PrmSchedulerProcess6SingleUserIntegrationTest extends BaseProcess6IntegrationTest {

    private static final String BASE_URL = "http://localhost:";

    @LocalServerPort
    private String port;

    @Inject
    private RefreshController refreshController;

    /**
     *  No Update - UserRefreshQueue.accessTypeVersion >  PRM Access Version.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_version1.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_accessVersion() throws JsonProcessingException {
        runTest(List.of("/SchedulerTests/PrdRetrieveUsers/userx_scenario_01.json"),
                1, false, false, EndStatus.FAILED);
    }

    /**
     *  OrganisationStatus of PENDING.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user_refresh_queue_orgstatus_pending.sql"
    })
    void testCreateRole_orgstatus_pending() throws JsonProcessingException {
        runTest(List.of("/SchedulerTests/PrdRetrieveUsers/userx_scenario_04.json"),
                1, false, false, EndStatus.SUCCESS);
    }

    /**
     *  Delete Role Assignment.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_deleted.sql"
    })
    void testDeleteRole() throws JsonProcessingException {
        runTest(List.of("/SchedulerTests/PrdRetrieveUsers/userx_scenario_03.json"),
                1, false, false, EndStatus.SUCCESS);
    }

    protected void testCreateRoleAssignment(boolean organisation, boolean group) {
        runTest(organisation ? List.of("/SchedulerTests/PrdRetrieveUsers/userx_scenario_01.json")
                : List.of("/SchedulerTests/PrdRetrieveUsers/userx_scenario_02.json"),
                1, organisation, group, EndStatus.SUCCESS);
    }

    private void runTest(List<String> refreshUserfileNames, int expectedNumberOfRecords,
                         boolean organisation, boolean group, EndStatus endStatus) {

        try {
            // GIVEN
            logBeforeStatus();
            stubS2SCall();
            stubIdamCall();
            stubPrdRefreshUser(refreshUserfileNames, USERID, "false", "false");
            stubRasCreateRoleAssignment(endStatus);

            // WHEN
            //ResponseEntity<Object> response1 = refreshController
            //        .professionalRefresh(USERID);
            Response response =
                    SerenityRest.given()
                            .log().all()
                            .baseUri(BASE_URL + port)
                            .headers(getHttpHeaders(S2S_ORM))
                            .post("/am/role-mapping/professional/refresh?userId=" + USERID)
                            .then().log().all()
                            .assertThat()
                            .statusCode(HttpStatus.OK.value())
                            .extract().response();

            // THEN
            if (expectedNumberOfRecords != 0) {
                verifyNoOfCallsToPrd(1);
                verifyNoOfCallsToRas(1);
            }
            logAfterStatus(response);

            // verify the response
            //assertResponse(response);

            if (expectedNumberOfRecords != 0) {
                assertAssignmentRequest(organisation, group);
            }
        } catch (ServiceException e) {
            assertEquals(EndStatus.FAILED, endStatus);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
