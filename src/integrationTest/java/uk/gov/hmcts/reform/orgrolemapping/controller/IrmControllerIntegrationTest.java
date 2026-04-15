package uk.gov.hmcts.reform.orgrolemapping.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures.OBJECT_MAPPER;

@TestPropertySource(properties = {
    "idam.role.management.scheduling.enabled=false",
    "testing.support.enabled=true" // NB: needed for access to test support URLs
})
public class IrmControllerIntegrationTest extends BaseAuthorisedTestIntegration {

    private static final String PROCESSJUDICIALQUEUE_URL = "/am/testing-support/irm/processJudicialQueue";
    private static final String UPDATEUSER_URL = "/am/testing-support/irm/user";

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql"
    })
    void processJudicialQueueTest() throws Exception {
        // WHEN
        String response = getRequestSpecification()
                .when().get(PROCESSJUDICIALQUEUE_URL)
                .then().assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        // THEN
        assertNotNull(response);
        ProcessMonitorDto processMonitorDto = OBJECT_MAPPER.readValue(response, ProcessMonitorDto.class);
        assertNotNull(processMonitorDto);
    }
}
