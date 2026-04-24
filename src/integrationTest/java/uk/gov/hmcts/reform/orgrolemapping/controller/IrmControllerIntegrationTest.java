package uk.gov.hmcts.reform.orgrolemapping.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.admin.model.ServeEventQuery;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.irm.IdamRecordType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamInvitation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamUser;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures.OBJECT_MAPPER;

@TestPropertySource(properties = {
    "idam.role.management.scheduling.enabled=false",
    "idam.role.management.scheduling.judicial.cron=0 0 0 31 12 7", // block cron schedule
    "testing.support.enabled=true" // NB: needed for access to test support URLs
})
class IrmControllerIntegrationTest extends BaseAuthorisedTestIntegration {

    // Idam Feign Endpoints
    private static final String IDAM_DELETEINVITATIONS_URL = "/api/v2/invitations/";
    private static final String IDAM_GETINVITATIONS_URL = "/api/v2/invitations-by-user-email/";
    private static final String IDAM_GETUSER_URL = "/api/v2/users/";
    private static final String IDAM_GETUSERBYEMAIL_URL = "/api/v2/users-by-email/";
    private static final String IDAM_INVITEUSER_URL = "/api/v2/invitations";

    // Controller Endpoints
    private static final String INVITEUSER_URL = "/am/testing-support/irm/user/invite";
    private static final String PROCESSJUDICIALQUEUE_URL = "/am/testing-support/irm/processJudicialQueue";
    private static final String UPDATEUSER_URL = "/am/testing-support/irm/user";

    private static final UUID STUB_ID_DELETEINVITATION = UUID.randomUUID();
    private static final UUID STUB_ID_GETUSER = UUID.randomUUID();
    private static final UUID STUB_ID_GETINVITATIONS = UUID.randomUUID();
    private static final UUID STUB_ID_INVITEUSER = UUID.randomUUID();
    private static final UUID STUB_ID_UPDATEUSER = UUID.randomUUID();

    @Autowired
    private IdamRoleManagementQueueRepository idamRoleManagementQueueRepository;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue.sql"
    })
    void processJudicialQueueTest() throws Exception {
        String userId = "some-user-id";
        String email = "someone@somewhere.com";
        testProcessJudicialQueue(userId, email, getIdamUser(userId, email), IdamRecordType.USER, CREATED);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue.sql"
    })
    void processJudicialQueueTest_InvalidUser() throws Exception {
        String userId = "some-user-id";
        String email = "someone@somewhere.com";
        testProcessJudicialQueue(userId, email, null, IdamRecordType.INVITE, CREATED);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
            "classpath:sql/irm/queue/insert_idam_role_management_queue.sql"
    })
    void processJudicialQueueTest_Retry() throws Exception {
        String userId = "some-user-id";
        String email = "someone@somewhere.com";
        testProcessJudicialQueue(userId, email, null, IdamRecordType.USER, BAD_REQUEST);
    }

    private void testProcessJudicialQueue(String userId, String email, IdamUser user,
                                          IdamRecordType idamRecordType, HttpStatus inviteStatus)
            throws Exception {
        RequestSpecification requestSpecification = getRequestSpecification();
        // stub the calls after the getRequestSpecification (as it resets the wiremockserver).
        stubUpdateUser(userId, email, user, inviteStatus);
        // WHEN
        String response = requestSpecification
                .when().get(PROCESSJUDICIALQUEUE_URL)
                .then().assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        // THEN
        assertNotNull(response);
        ProcessMonitorDto processMonitorDto = OBJECT_MAPPER.readValue(response, ProcessMonitorDto.class);
        assertNotNull(processMonitorDto);
        List<IdamRoleManagementQueueEntity> irmQueue = getIrmQueueEntities(1);
        int retry = CREATED.equals(inviteStatus) ? 0 : 1;
        assertIrmQueueEntity("some-user-id", idamRecordType, retry, irmQueue.getFirst());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue.sql"
    })
    void updateUserTest() throws Exception {
        // GIVEN
        String userId = "some-user-id";
        String email = "someone@somewhere.com";
        testUpdateUser(userId, email, getIdamUser(userId, email), 1, CREATED);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql"
    })
    void updateUserTest_InvalidUser() throws Exception {
        // GIVEN
        String userId = "non-existant-user-id";
        String email = "nobody@nowhere.com";
        testUpdateUser(userId, email, null, 0, CREATED);
    }

    private void testUpdateUser(String userId, String email, IdamUser user, int times,
                                HttpStatus inviteStatus) throws Exception {
        RequestSpecification requestSpecification = getRequestSpecification();
        // stub the calls after the getRequestSpecification (as it resets the wiremockserver).
        stubUpdateUser(userId, email, user, inviteStatus);

        // WHEN
        String response = requestSpecification
                .queryParam("userId", userId)
                .when().get(UPDATEUSER_URL)
                .then().assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        // THEN
        assertNotNull(response);
        ProcessMonitorDto processMonitorDto = OBJECT_MAPPER.readValue(response, ProcessMonitorDto.class);
        assertNotNull(processMonitorDto);
        verifyUpdateUserStubs(times);
    }

    private void stubUpdateUser(String userId, String email, IdamUser user, HttpStatus inviteStatus)
            throws JsonProcessingException, UnsupportedEncodingException {
        stubGetIdamUser(userId, user);
        if (user != null) {
            stubUpdateIdamUser(userId, user);
        }
        stubInviteUser(userId, email, user, Collections.emptyList(), inviteStatus);
    }

    private void verifyUpdateUserStubs(int times) {
        verifyGetUserStub(times);
        verifyUpdateUserStub(times);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
    })        
    void inviteUserTest() throws Exception {
        String userId = "some-user-id";
        String email = "someone@somewhere.com";
        IdamUser user =  getIdamUser(userId, email);
        List<IdamInvitation> oldInvitations = List.of(getIdamInvitation(userId), getIdamInvitation(userId));
        testInviteUser(userId, email, user, user.getRoleNames(), oldInvitations,
                1, CREATED);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
    })
    void inviteUserTest_InvalidUser() throws Exception {
        String userId = "non-existant-user-id";
        String email = "invalid@email.com";
        testInviteUser(userId, email, null, Collections.emptyList(), Collections.emptyList(),
                1, CREATED);
    }

    private void testInviteUser(String userId, String email, IdamUser user, List<String> roleNames,
                                List<IdamInvitation> oldInvitations, int times,
                                HttpStatus inviteStatus) throws Exception {
        // GIVEN
        RequestSpecification requestSpecification = getRequestSpecification();
        // stub the calls after the getRequestSpecification (as it resets the wiremockserver).
        stubInviteUser(userId, email, user, oldInvitations, inviteStatus);
        String roleNamesCsv = String.join(",", roleNames);

        // WHEN
        String response = requestSpecification
                .queryParam("email", email)
                .queryParam("roleNames", roleNamesCsv)
                .when().get(INVITEUSER_URL)
                .then().assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        // THEN
        assertNotNull(response);
        ProcessMonitorDto processMonitorDto = OBJECT_MAPPER.readValue(response, ProcessMonitorDto.class);
        assertNotNull(processMonitorDto);
        verifyInviteUserStubs(oldInvitations.size(), times);
    }

    private void stubInviteUser(String userId, String email, IdamUser user, List<IdamInvitation> oldInvitations,
                                HttpStatus inviteStatus)
            throws JsonProcessingException, UnsupportedEncodingException {
        stubGetIdamUserByEmail(getUrlSafe(email), user);
        stubGetInvitations(getUrlSafe(email), oldInvitations);
        stubDeleteInviatations(oldInvitations);
        stubInviteIdamUser(userId, inviteStatus);
    }

    private void verifyInviteUserStubs(int getInvitationsTimes, int inviteTimes) {
        verifyGetInvitationsStub(inviteTimes);
        verifyDeleteInvitation(getInvitationsTimes);
        verifyInviteIdamUserStub(inviteTimes);
    }

    /**
     * Stubs.
     */
    private void stubGetIdamUser(String userId, IdamUser user)
            throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching(IDAM_GETUSER_URL + userId))
                .withId(STUB_ID_GETUSER)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(OBJECT_MAPPER.writeValueAsString(user))
                ));
    }

    private void stubGetIdamUserByEmail(String email, IdamUser user)
            throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching(IDAM_GETUSERBYEMAIL_URL + email))
                .withId(STUB_ID_GETUSER)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(OBJECT_MAPPER.writeValueAsString(user))
                ));
    }

    private void stubUpdateIdamUser(String userId, IdamUser user)
            throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(put(urlPathMatching(IDAM_GETUSER_URL + userId))
                .withId(STUB_ID_UPDATEUSER)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(OBJECT_MAPPER.writeValueAsString(user))
                ));
    }

    private void stubGetInvitations(String email, List<IdamInvitation> invitations)
            throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching(IDAM_GETINVITATIONS_URL + email))
                .withId(STUB_ID_GETINVITATIONS)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(OBJECT_MAPPER.writeValueAsString(invitations))
                ));
    }

    private void stubInviteIdamUser(String userId, HttpStatus httpStatus)
            throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(post(urlPathMatching(IDAM_INVITEUSER_URL))
                .withId(STUB_ID_INVITEUSER)
                .willReturn(aResponse()
                        .withStatus(httpStatus.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(OBJECT_MAPPER.writeValueAsString(getIdamInvitation(userId)))
                ));
    }

    private void stubDeleteInviatations(List<IdamInvitation> invitations) {
        invitations.forEach(invitation -> {
            try {
                WIRE_MOCK_SERVER.stubFor(delete(urlPathMatching(IDAM_DELETEINVITATIONS_URL + invitation.getId()))
                        .withId(STUB_ID_DELETEINVITATION)
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", "application/json")
                                .withBody(OBJECT_MAPPER.writeValueAsString(invitation))
                        ));
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    /**
     * Verify Data.
     */
    private List<IdamRoleManagementQueueEntity> getIrmQueueEntities(int expectedNumberOfRecords) {
        List<IdamRoleManagementQueueEntity> irmQueue = idamRoleManagementQueueRepository.findAll();
        assertEquals(expectedNumberOfRecords, irmQueue.size(),
                "Unexpected number of records in IdamRoleManagementQueue");
        return irmQueue;
    }

    private void assertIrmQueueEntity(String userId, IdamRecordType idamRecordType, int retry,
                                                IdamRoleManagementQueueEntity irmQueueEntity) {
        assertEquals(userId, irmQueueEntity.getUserId(), "UserId mismatch");
        assertEquals(retry, irmQueueEntity.getRetry(), "Retry mismatch");
        assertEquals(retry != 0, irmQueueEntity.getActive(), "Active flag mismatch");
        assertEquals(idamRecordType, irmQueueEntity.getPublishedAs(), "PublishAs mismatch");
    }

    /**
     * Verify Stubs.
     */
    private void verifyGetUserStub(int times) {
        getWireMockEvents(STUB_ID_GETUSER, HttpStatus.OK, times, "getUser");
    }

    private void verifyUpdateUserStub(int times) {
        getWireMockEvents(STUB_ID_UPDATEUSER, HttpStatus.OK, times, "updateUser");
    }

    private void verifyGetInvitationsStub(int times) {
        getWireMockEvents(STUB_ID_GETINVITATIONS, HttpStatus.OK, times, "getInvitations");
    }

    private void verifyDeleteInvitation(int times) {
        getWireMockEvents(STUB_ID_DELETEINVITATION, HttpStatus.OK, times, "deleteInvitation");
    }

    private void verifyInviteIdamUserStub(int times) {
        getWireMockEvents(STUB_ID_INVITEUSER, CREATED, times, "inviteUser");
    }

    private List<ServeEvent> getWireMockEvents(UUID stubId, HttpStatus httpStatus, int times, String endPoint) {
        List<ServeEvent> result =
                WIRE_MOCK_SERVER.getServeEvents(ServeEventQuery.forStubMapping(stubId)).getServeEvents();
        // verify number of calls
        assertEquals(times, result.size(),
                String.format("Unexpected number of calls to %s endpoint", endPoint));
        // verify response status
        if (times != 0) {
            assertEquals(httpStatus.value(), result.getFirst().getResponse().getStatus(),
                    "Response status mismatch");
        }
        return result;
    }

    /**
     * Data.
     */
    private IdamUser getIdamUser(String userId, String email) {
        return IdamUser.builder()
                .id(userId)
                .email(email)
                .forename("forename")
                .surname("surname")
                .roleNames(List.of("oldRole1", "oldRole2"))
                .build();
    }

    private IdamInvitation getIdamInvitation(String userId) {
        return IdamInvitation.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .build();
    }

    private String getUrlSafe(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }
}
