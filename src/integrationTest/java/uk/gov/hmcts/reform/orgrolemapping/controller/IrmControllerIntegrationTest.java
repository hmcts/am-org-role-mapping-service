package uk.gov.hmcts.reform.orgrolemapping.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.admin.model.ServeEventQuery;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamInvitation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamUser;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures.OBJECT_MAPPER;

@Slf4j
@TestPropertySource(properties = {
    "idam.role.management.scheduling.enabled=false",
    "testing.support.enabled=true" // NB: needed for access to test support URLs
})
class IrmControllerIntegrationTest extends BaseAuthorisedTestIntegration {

    // Idam Feign Endpoints
    private static final String IDAM_DELETEINVITATIONS_URL = "/api/v2/invitations/";
    private static final String IDAM_GETINVITATIONS_URL = "/api/v2/invitations-by-user-email/";
    private static final String IDAM_GETUSER_URL = "/api/v2/users/";
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

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
        "classpath:sql/irm/queue/insert_idam_role_management_queue.sql"
    })
    void updateUserTest() throws Exception {
        // GIVEN
        String userId = "some-user-id";
        RequestSpecification requestSpecification = getRequestSpecification();
        // stub the calls after the getRequestSpecification (as it resets the wiremockserver).
        stubUpdateUser(userId);

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
        verifyUpdateUserStubs();
    }

    private void stubUpdateUser(String userId) throws JsonProcessingException {
        stubGetIdamUser(userId, "email");
        stubUpdateIdamUser(userId);
    }

    private void verifyUpdateUserStubs() {
        verifyGetUserStub(1);
        verifyUpdateUserStub(1);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/queue/init_idam_role_management_queue.sql",
    })        
    void inviteUserTest() throws Exception {
        // GIVEN
        log.info("AM_ORG_ROLE_MAPPING_SERVICE_SECRET = " + System.getenv("AM_ORG_ROLE_MAPPING_SERVICE_SECRET"));
        log.info("totp_secret = " + System.getProperty("idam.s2s-auth.totp_secret"));
        String userId = "some-user-id";
        String email = "someone@somewhere.com";
        RequestSpecification requestSpecification = getRequestSpecification();
        // stub the calls after the getRequestSpecification (as it resets the wiremockserver).
        stubInviteUser(userId, email);

        // WHEN
        String response = requestSpecification
                .queryParam("userId", userId)
                .when().get(INVITEUSER_URL)
                .then().assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        // THEN
        assertNotNull(response);
        ProcessMonitorDto processMonitorDto = OBJECT_MAPPER.readValue(response, ProcessMonitorDto.class);
        assertNotNull(processMonitorDto);
        verifyInviteUserStubs();
    }

    private void stubInviteUser(String userId, String email)
            throws JsonProcessingException, UnsupportedEncodingException {
        stubGetIdamUser(userId, email);
        stubGetInvitations(userId, getUrlSafe(email));
        stubDeleteInviatations(userId);
        stubInviteIdamUser(userId);
    }

    private void verifyInviteUserStubs() {
        verifyGetUserStub(1);
        verifyGetInvitationsStub(1);
        verifyDeleteInvitation(1);
        verifyInviteIdamUserStub(1);
    }

    /**
     * Stubs.
     */
    private void stubGetIdamUser(String userId, String email)
            throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching(IDAM_GETUSER_URL + userId))
                .withId(STUB_ID_GETUSER)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(OBJECT_MAPPER.writeValueAsString(getIdamUser(userId, email)))
                ));
    }

    private void stubUpdateIdamUser(String userId)
            throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(put(urlPathMatching(IDAM_GETUSER_URL + userId))
                .withId(STUB_ID_UPDATEUSER)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(OBJECT_MAPPER.writeValueAsString(getIdamUser(userId, "email")))
                ));
    }

    private void stubGetInvitations(String userId, String email)
            throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching(IDAM_GETINVITATIONS_URL + email))
                .withId(STUB_ID_GETINVITATIONS)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(OBJECT_MAPPER.writeValueAsString(List.of(getIdamInvitation(userId))))
                ));
    }

    private void stubInviteIdamUser(String userId)
            throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(post(urlPathMatching(IDAM_INVITEUSER_URL))
                .withId(STUB_ID_INVITEUSER)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(OBJECT_MAPPER.writeValueAsString(getIdamInvitation(userId)))
                ));
    }

    private void stubDeleteInviatations(String userId)
            throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(delete(urlPathMatching(IDAM_DELETEINVITATIONS_URL))
                .withId(STUB_ID_DELETEINVITATION)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(OBJECT_MAPPER.writeValueAsString(getIdamInvitation(userId)))
                ));
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
        getWireMockEvents(STUB_ID_INVITEUSER, HttpStatus.OK, times, "inviteUser");
    }

    private List<ServeEvent> getWireMockEvents(UUID stubId, HttpStatus httpStatus, int times, String endPoint) {
        List<ServeEvent> result =
                WIRE_MOCK_SERVER.getServeEvents(ServeEventQuery.forStubMapping(stubId)).getServeEvents();
        // verify number of calls
        assertEquals(times, result.size(),
                String.format("Unexpected number of calls to %s endpoint", endPoint));
        // verify response status
        assertEquals(httpStatus.value(), result.getFirst().getResponse().getStatus(),
                "Response status mismatch");
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
                .userId(userId)
                .build();
    }

    private String getUrlSafe(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }
}
