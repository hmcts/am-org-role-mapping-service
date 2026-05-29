package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.irm.IdamRecordType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamUser;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures.OBJECT_MAPPER;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType.JUDICIAL;
import static uk.gov.hmcts.reform.orgrolemapping.scheduler.IrmScheduler.DELETEINTERVALDAYS;

class IrmSchedulerProcessIntegrationTest extends BaseSchedulerTestIntegration {

    // Idam Feign Endpoints
    private static final String IDAM_GETUSER_URL = "/api/v2/users/";

    private static final UUID STUB_ID_GETUSER = UUID.randomUUID();
    private static final UUID STUB_ID_UPDATEUSER = UUID.randomUUID();

    @Autowired
    private IdamRoleManagementQueueRepository idamRoleManagementQueueRepository;

    @Autowired
    private IrmScheduler irmScheduler;


    /**
     * Delete Inactive Queue Entries - days not set.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql"
    })
    void testDeleteInactiveQueueEntries_noDaysSet() {
        // GIVEN
        System.setProperty(DELETEINTERVALDAYS, "");

        // WHEN
        ProcessMonitorDto processMonitorDto = irmScheduler.deleteInactiveQueueEntries();

        // THEN
        assertNotNull(processMonitorDto);
        assertEquals(EndStatus.FAILED, processMonitorDto.getEndStatus());
    }

    /**
     * Delete Inactive Queue Entries.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_caseworker.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_retry4.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_inactive_89days.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_inactive_90days.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_inactive_91days.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_norole_89days.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_norole_90days.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_norole_91days.sql"
    })
    void testDeleteInactiveQueueEntries() {
        // GIVEN
        Integer noOfDays = 90;
        System.setProperty(DELETEINTERVALDAYS, noOfDays.toString());

        // WHEN
        ProcessMonitorDto processMonitorDto = irmScheduler.deleteInactiveQueueEntries();

        // THEN
        assertNotNull(processMonitorDto);
        assertEquals(EndStatus.SUCCESS, processMonitorDto.getEndStatus());
        int expectedNoOfRecords = 4;
        List<IdamRoleManagementQueueEntity> results = assertNoOfRecords(expectedNoOfRecords);
        LocalDateTime nintyDays = LocalDateTime.now().minusDays(noOfDays);
        results.forEach(record -> assertTrue(record.getLastUpdated().isAfter(nintyDays)));
    }


    /**
     * Process Judicial Queue - Success.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_caseworker.sql"
    })
    void testProcessJudicialQueue_Success() throws JsonProcessingException {
        List<String> idamUserfilenames = List.of(
            "/SchedulerTests/Irm/IdamUser/someUser.json",
            "/SchedulerTests/Irm/IdamUser/someOtherUser.json"
        );
        testProcessJudicialQueue(idamUserfilenames, 2, true, 0);
    }

    /**
     * Process Judicial Queue - Retry 1.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_retry1.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_caseworker.sql"
    })
    void testProcessJudicialQueue_Retry1() throws JsonProcessingException {
        List<String> idamUserfilenames = List.of(
            "/SchedulerTests/Irm/IdamUser/someUser.json",
            "/SchedulerTests/Irm/IdamUser/someOtherUser.json"
        );
        testProcessJudicialQueue(idamUserfilenames, 2, true, 0);
    }

    /**
     * Process Judicial Queue - Retry 2.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_retry2.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_caseworker.sql"
    })
    void testProcessJudicialQueue_Retry2() throws JsonProcessingException {
        List<String> idamUserfilenames = List.of(
            "/SchedulerTests/Irm/IdamUser/someUser.json",
            "/SchedulerTests/Irm/IdamUser/someOtherUser.json"
        );
        testProcessJudicialQueue(idamUserfilenames, 2, true, 0);
    }

    /**
     * Process Judicial Queue - Retry 3.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_retry3.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_caseworker.sql"
    })
    void testProcessJudicialQueue_Retry3() throws JsonProcessingException {
        List<String> idamUserfilenames = List.of(
                "/SchedulerTests/Irm/IdamUser/someUser.json",
                "/SchedulerTests/Irm/IdamUser/someOtherUser.json"
        );
        testProcessJudicialQueue(idamUserfilenames, 2, true, 0);
    }
    
    /**
     * Process Judicial Queue - Retry 4.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_retry4.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue_caseworker.sql"
    })
    void testProcessJudicialQueue_Retry4() throws JsonProcessingException {
        List<String> idamUserfilenames = List.of(
                "/SchedulerTests/Irm/IdamUser/someUser.json",
                "/SchedulerTests/Irm/IdamUser/someOtherUser.json"
        );
        testProcessJudicialQueue(idamUserfilenames,2, false, 4);
    }

    void testProcessJudicialQueue(List<String> idamUserfilenames,
                                  int expectedNoOfRecords, boolean isUpdated, int retry)
            throws JsonProcessingException {
        // GIVEN
        stubGetIdamUser(idamUserfilenames);

        // WHEN
        ProcessMonitorDto processMonitorDto = irmScheduler.processJudicialQueue();

        // THEN
        assertNotNull(processMonitorDto);
        assertEquals(EndStatus.SUCCESS, processMonitorDto.getEndStatus());
        assertQueueRecords(IdamRecordType.USER, expectedNoOfRecords, isUpdated, retry);
    }

    private List<IdamRoleManagementQueueEntity> assertNoOfRecords(int expectedNoOfRecords) {
        List<IdamRoleManagementQueueEntity> results = idamRoleManagementQueueRepository.findAll();
        assertEquals(expectedNoOfRecords, results.size());
        return results;
    }

    private void assertQueueRecords(IdamRecordType idamRecordType, int expectedNoOfRecords,
                                    boolean isUpdated, int retry) {
        List<IdamRoleManagementQueueEntity> results = assertNoOfRecords(expectedNoOfRecords);
        results.forEach(record -> {
            if (JUDICIAL.equals(record.getUserType())) {
                assertEquals(!isUpdated, record.getActive(), "Active mismatch");
                assertEquals(retry, record.getRetry(), "Retry mismatch");
                assertEquals(idamRecordType, record.getPublishedAs(), "PublishedAs mismatch");
                assertTrue(assertLastUpdatedNow(record.getLastUpdated()), "LastUpdated mismatch");
                if (isUpdated) {
                    assertTrue(assertLastUpdatedNow(record.getLastPublished()), "LastPublished mismatch");
                    assertTrue(assertLastUpdatedNow(record.getRetryAfter()), "RetryAfter mismatch");
                } else {
                    assertNull(record.getRetryAfter(), "RetryAfter not null");
                }
            } else {
                assertTrue(record.getActive());
            }
        });
    }

    private boolean assertLastUpdatedNow(LocalDateTime lastUpdated) {
        return lastUpdated.isAfter(LocalDateTime.now().minusMinutes(1));
    }

    /**
     * Stubs.
     */
    private void stubGetIdamUser(List<String> idamUserfilenames)
            throws JsonProcessingException {
        IdamUser user;
        for (int i = 0; i < idamUserfilenames.size(); i++) {
            String json = jsonHelper.readJsonFromFile(idamUserfilenames.get(i));
            user = OBJECT_MAPPER.readValue(json, IdamUser.class);
            stubGetIdamUser(user);
            stubUpdateIdamUser(user);
        }
    }

    private void stubGetIdamUser(IdamUser user)
            throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching(IDAM_GETUSER_URL + user.getId()))
                .withId(STUB_ID_GETUSER)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(OBJECT_MAPPER.writeValueAsString(user))
                ));
    }

    private void stubUpdateIdamUser(IdamUser user)
            throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(put(urlPathMatching(IDAM_GETUSER_URL + user.getId()))
                .withId(STUB_ID_UPDATEUSER)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(OBJECT_MAPPER.writeValueAsString(user))
                ));
    }
}
