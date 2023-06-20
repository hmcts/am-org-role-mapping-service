package uk.gov.hmcts.reform.orgrolemapping.controller;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialRefreshRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureConditionEvaluator;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.ABORTED;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.COMPLETED;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.setSecurityAuthorities;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildUserRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("itest")
@EnableConfigurationProperties
public class RefreshControllerIntegrationTest extends BaseTest {

    private final WiremockFixtures wiremockFixtures = new WiremockFixtures();

    private static final Logger logger = LoggerFactory.getLogger(RefreshControllerIntegrationTest.class);

    private static final String REFRESH_JOB_RECORDS_QUERY = "SELECT job_id, status, user_ids, linked_job_id,"
            + " comments, log FROM refresh_jobs where job_id=?";
    private static final String AUTHORISED_SERVICE = "orm_batch";
    private static final String ROLE_NAME_STCW = "senior-tribunal-caseworker";
    private static final String ROLE_NAME_TCW = "tribunal-caseworker";
    private static final String URL = "/am/role-mapping/refresh";
    private static final String JUDICIAL_REFRESH_URL = "/am/role-mapping/judicial/refresh";
    private static final String CORRELATION_ID = "38a90097-434e-47ee-8ea1-9ea2a267f51d";

    private MockMvc mockMvc;

    @Autowired
    private RefreshController controller;

    @Inject
    private WebApplicationContext wac;

    @Autowired
    private DataSource ds;

    @MockBean
    private FeatureConditionEvaluator featureConditionEvaluation;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    private JdbcTemplate template;

    private static final MediaType JSON_CONTENT_TYPE = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            StandardCharsets.UTF_8
    );

    @BeforeEach
    public void setUp() throws Exception {
        template = new JdbcTemplate(ds);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        setSecurityAuthorities(authentication, MockUtils.ROLE_CASEWORKER);

        doReturn(true).when(featureConditionEvaluation).preHandle(any(),any(),any());

        wiremockFixtures.resetRequests();
        // stub idam and s2s request
        wiremockFixtures.stubIdam();
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete() throws Exception {
        logger.info(" RefreshJob record With Only JobId to process successful");
        Long jobId = 1L;
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("NEW", refreshJob.getStatus());

        // stub crd request
        wiremockFixtures.stubForGetCaseworkerDetailsByServiceName(buildUserProfileResponse());
        // stub RAS
        wiremockFixtures.stubRoleAssignmentServiceWithStatus(CREATED);

        controller.refresh(jobId, null);

        Thread.sleep(9000);
        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(COMPLETED, refreshJob.getStatus());
        assertEquals(0, refreshJob.getUserIds().length);
        assertNotNull(refreshJob.getLog());
    }


    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToAborted() throws Exception {
        logger.info(" RefreshJob record With Only JobId to process Aborted");
        Long jobId = 1L;

        // stub crd request
        wiremockFixtures.stubForGetCaseworkerDetailsByServiceName(buildUserProfileResponse());
        // stub RAS request
        wiremockFixtures.stubRoleAssignmentServiceError();

        controller.refresh(jobId, null);

        // allow time for createRoleAssignment to fail 3 times as per @Retryable configuration
        Thread.sleep(9000);
        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(ABORTED, refreshJob.getStatus());
        assertNotNull(refreshJob.getUserIds());
        assertThat(refreshJob.getLog(),containsString(String.join(",", refreshJob.getUserIds())));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToAborted_status422() throws Exception {
        logger.info(" RefreshJob record With Only JobId to process Non recoverable retain same state");
        Long jobId = 1L;

        // stub crd request
        wiremockFixtures.stubForGetCaseworkerDetailsByServiceName(buildUserProfileResponse());
        // stub RAS request
        wiremockFixtures.stubRoleAssignmentServiceError();

        controller.refresh(jobId, null);

        // allow time for createRoleAssignment to fail 3 times as per @Retryable configuration
        Thread.sleep(9000);
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        logger.info(" -- Refresh Role Assignment record updated -- " + refreshJob.getStatus());
        assertEquals("ABORTED", refreshJob.getStatus());
        assertNotNull(refreshJob.getUserIds());
        assertThat(refreshJob.getLog(),containsString(String.join(",", refreshJob.getUserIds())));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToPartialComplete() throws Exception {
        logger.info(" RefreshJob record With Only JobId to process Partial Success");
        Long jobId = 1L;

        // stub crd request
        wiremockFixtures.stubForGetCaseworkerDetailsByServiceName(buildUserProfileResponse());
        // stub RAS request
        wiremockFixtures.stubRoleAssignmentServiceError();

        controller.refresh(jobId, null);

        // allow time for createRoleAssignment to fail 3 times as per @Retryable configuration
        Thread.sleep(9000);
        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(ABORTED, refreshJob.getStatus());
        assertNotNull(refreshJob.getUserIds());
        assertThat(refreshJob.getLog(), containsString(String.join(",", refreshJob.getUserIds())));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToPartialComplete_status422() throws Exception {
        logger.info(" RefreshJob record With Only JobId to process Partial Success");
        Long jobId = 1L;

        // stub crd request
        wiremockFixtures.stubForGetCaseworkerDetailsByServiceName(buildUserProfileResponse());
        // stub RAS request
        wiremockFixtures.stubRoleAssignmentServiceError();

        controller.refresh(jobId, null);

        // allow time for createRoleAssignment to fail 3 times as per @Retryable configuration
        Thread.sleep(9000);
        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(ABORTED, refreshJob.getStatus());
        assertNotNull(refreshJob.getUserIds());
        assertThat(refreshJob.getLog(), containsString(String.join(",", refreshJob.getUserIds())));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithFailedUsersToComplete() throws Exception {
        logger.info("RefreshJob record With JobId and failed UserIds to process successful");
        Long jobId = 3L;

        // stub crd request
        wiremockFixtures.stubForGetCaseworkerDetailsById(buildUserRequest(), buildCaseWorkerProfileResponse());
        // stub RAS
        wiremockFixtures.stubRoleAssignmentServiceWithStatus(CREATED);

        //Verify before job
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("NEW", refreshJob.getStatus());
        RefreshJobEntity linkedJob = getRecordsFromRefreshJobTable(refreshJob.getLinkedJobId());
        assertNotNull(linkedJob.getUserIds());
        assertEquals(ABORTED, linkedJob.getStatus());
        assertNotEquals(0, linkedJob.getUserIds().length);

        controller.refresh(jobId, buildUserRequest());

        Thread.sleep(1000);
        logger.info("-- Refresh Role Assignment record updated successfully --");
        refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(COMPLETED, refreshJob.getStatus());
        assertEquals(0, refreshJob.getUserIds().length);
        assertNotNull(refreshJob.getLog());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete_retryFail() throws Exception {
        logger.info(" RefreshJob record With Only JobId to process fail");
        Long jobId = 1L;
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("NEW", refreshJob.getStatus());

        // stub crd request
        wiremockFixtures.stubForFailureGetCaseworkerDetailsByServiceName();

        controller.refresh(jobId, null);

        refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("NEW", refreshJob.getStatus());// failed process should change the status to IN-PROGRESS
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete_CRDRetry() throws Exception {
        logger.info(" RefreshJob record With JobId retry success third time to process successful");
        Long jobId = 1L;
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("NEW", refreshJob.getStatus());

        // stub crd request
        wiremockFixtures.stubForFailureRetryAndSuccessGetCaseworkerDetailsByServiceName(buildUserProfileResponse());
        // stub RAS
        wiremockFixtures.stubRoleAssignmentServiceWithStatus(CREATED);

        controller.refresh(jobId, null);

        Thread.sleep(9000);
        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(COMPLETED, refreshJob.getStatus());
        assertEquals(0, refreshJob.getUserIds().length);
        assertNotNull(refreshJob.getLog());
    }

    @Test
    public void shouldProcessRefreshRoleAssignmentsWithJudicialProfiles() throws Exception {
        logger.info(" Refresh role assignments successfully with valid user profiles");
        var uuid = UUID.randomUUID().toString();

        // stub RAS
        wiremockFixtures.stubRoleAssignmentServiceWithStatus(CREATED);
        // stub jrd request
        wiremockFixtures.stubForGetJudicialDetailsById(OK, uuid);
        // stub jbs request
        wiremockFixtures.stubForGetJudicialBookingByUserIds(uuid);

        var result = controller.judicialRefresh(CORRELATION_ID, getJudicialRefreshRequest());

        assertTrue(result.getStatusCode().is2xxSuccessful());
        var contentAsString = Objects.requireNonNull(result.getBody()).toString();
        assertTrue(contentAsString.contains(Constants.SUCCESS_ROLE_REFRESH));
        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
    }

    @Test
    public void shouldFailProcessRefreshRoleAssignmentsWithJudicialProfiles_withFailedRoleAssignments()
            throws Exception {
        logger.info(" Refresh role assignments failed with valid user profiles");
        var uuid = UUID.randomUUID().toString();

        // stub RAS request
        wiremockFixtures.stubRoleAssignmentServiceWithStatus(UNPROCESSABLE_ENTITY);
        // stub jrd request
        wiremockFixtures.stubForGetJudicialDetailsById(OK, uuid);
        // stub jbs request
        wiremockFixtures.stubForGetJudicialBookingByUserIds(uuid);

        var result = controller.judicialRefresh(CORRELATION_ID, getJudicialRefreshRequest());

        assertTrue(result.getStatusCode().is4xxClientError());
        var contentAsString = Objects.requireNonNull(result.getBody()).toString();
        assertTrue(contentAsString.contains(Constants.FAILED_ROLE_REFRESH));
        logger.info(" -- Refresh Role Assignment record fail to update -- ");
    }

    @Test
    public void shouldFailProcessRefreshRoleAssignmentsWithJudicialProfiles_withEmptyJudicialBookings()
            throws Exception {
        logger.info(" Refresh role assignments with empty bookings");
        var uuid = UUID.randomUUID().toString();

        // stub RAS
        wiremockFixtures.stubRoleAssignmentServiceWithStatus(CREATED);
        // stub jrd request
        wiremockFixtures.stubForGetJudicialDetailsById(OK, uuid);
        // stub jbs request
        wiremockFixtures.stubForGetJudicialBookingByUserIds();

        var result = controller.judicialRefresh(CORRELATION_ID, getJudicialRefreshRequest());

        assertTrue(result.getStatusCode().is2xxSuccessful());
        var contentAsString = Objects.requireNonNull(result.getBody()).toString();
        assertTrue(contentAsString.contains(Constants.SUCCESS_ROLE_REFRESH));
        logger.info(" -- Refresh Role Assignment record updated without bookings -- ");
    }

    @Test
    public void shouldFailProcessRefreshRoleAssignmentsWithJudicialProfiles_withNotFoundJudicialProfiles()
            throws Exception {
        logger.info(" Refresh role assignments with empty bookings");

        // stub RAS
        wiremockFixtures.stubRoleAssignmentServiceWithStatus(CREATED);
        // stub jrd request
        wiremockFixtures.stubForGetJudicialDetailsByIdFailure(NOT_FOUND);
        // stub jbs request
        wiremockFixtures.stubForGetJudicialBookingByUserIds();

        var result = controller.judicialRefresh(CORRELATION_ID, getJudicialRefreshRequest());

        assertTrue(result.getStatusCode().is2xxSuccessful());
        var contentAsString = Objects.requireNonNull(result.getBody()).toString();
        assertTrue(contentAsString.contains(Constants.SUCCESS_ROLE_REFRESH));
    }

    //TODO: fix issue with response
    @Test
    public void shouldFailProcessRefreshRoleAssignmentsWithJudicialProfiles_withEmptyJudicialProfiles()
            throws Exception {
        logger.info(" Refresh role assignments with empty bookings");

        // stub RAS request
        wiremockFixtures.stubRoleAssignmentServiceError();
        // stub jrd request
        wiremockFixtures.stubForGetJudicialDetailsByIdFailure(NOT_FOUND);
        // stub jbs request
        wiremockFixtures.stubForGetJudicialBookingByUserIds();

        var result = controller.judicialRefresh(CORRELATION_ID, getJudicialRefreshRequest());

        assertTrue(result.getStatusCode().is4xxClientError());
        var contentAsString = Objects.requireNonNull(result.getBody()).toString();
        assertTrue(contentAsString.contains(Constants.FAILED_ROLE_REFRESH));
    }

    @Test
    public void shouldRejectJudicialRefreshRequest_withEmptyBody() throws Exception {
        logger.info(" Refresh request rejected with empty request");

        Exception exception = assertThrows(BadRequestException.class, () ->
                controller.judicialRefresh(CORRELATION_ID, JudicialRefreshRequest.builder().build()));

        var contentAsString = exception.getMessage();
        assertTrue(contentAsString.contains("Empty user request"));
    }

    @Test
    public void shouldRejectJudicialRefreshRequest_withEmptyUserList() throws Exception {
        logger.info(" Refresh request rejected with empty user request");

        JudicialRefreshRequest request = JudicialRefreshRequest.builder()
                .refreshRequest(UserRequest.builder().userIds(Collections.emptyList()).build())
                .build();

        Exception exception = assertThrows(BadRequestException.class, () ->
                controller.judicialRefresh(CORRELATION_ID, request));

        var contentAsString = exception.getMessage();
        assertTrue(contentAsString.contains("Empty user request"));
    }

    @Test
    public void shouldRejectJudicialRefreshRequest_withInvalidUserIdFormat() throws Exception {
        logger.info(" Refresh role assignments failed with invalid valid user profiles format");

        JudicialRefreshRequest request = JudicialRefreshRequest.builder()
                .refreshRequest(UserRequest.builder().userIds(List.of("abc-123$")).build()).build();

        Exception exception = assertThrows(BadRequestException.class, () ->
                controller.judicialRefresh(CORRELATION_ID, request));

        var contentAsString = exception.getMessage();
        assertTrue(contentAsString.contains(
                "The input parameter: \"abc-123$\", does not comply with the required pattern")
        );
    }

    @Test
    public void shouldFailProcessRefreshRoleAssignmentsWithFailedUsersAndWithOutJobID() throws Exception {
        logger.info(" Refresh Job with optional Users and without mandatory jobId as a param");
        mockMvc.perform(post(URL)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .content(mapper.writeValueAsBytes(buildUserRequest())))
                .andExpect(status().is(400))
                .andReturn();
    }

    @Test
    public void shouldFailProcessRefreshRoleAssignmentsWithEmptyJobID() throws Exception {
        logger.info(" Refresh Job with optional Users and without mandatory jobId as a param");
        mockMvc.perform(post(URL)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .param("jobId", ""))
                .andExpect(status().is(400))
                .andReturn();
    }

    @Test
    public void shouldFailProcessRefreshRoleAssignmentsWithInvalidJobID() throws Exception {
        logger.info(" Refresh Job with optional Users and without mandatory jobId as a param");
        mockMvc.perform(post(URL)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .param("jobId", "abc")
                .content(mapper.writeValueAsBytes(buildUserRequest())))
                .andExpect(status().is(400))
                .andReturn();
    }

    @Test
    public void shouldFailProcessRefreshRoleAssignmentsWithOutJobID() throws Exception {
        logger.info(" Refresh Job with optional Users and without mandatory jobId as a param");
        mockMvc.perform(post(URL)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders()))
                .andExpect(status().is(400))
                .andReturn();
    }

    @NotNull
    private JudicialRefreshRequest getJudicialRefreshRequest() {
        return JudicialRefreshRequest.builder().refreshRequest(buildUserRequest()).build();
    }

    @NotNull
    private List<CaseWorkerProfilesResponse> buildUserProfileResponse() {
        return IntTestDataBuilder
                .buildListOfUserProfilesResponse("IAC", false, "1", "2", ROLE_NAME_STCW,
                        ROLE_NAME_TCW, true, true, false,
                        true, "BFA1", "BFA2", false);
    }

    @NotNull
    private List<CaseWorkerProfile> buildCaseWorkerProfileResponse() {
        return IntTestDataBuilder
                .buildListOfUserProfiles(false, false, "1", "2",
                        ROLE_NAME_STCW, ROLE_NAME_TCW,
                        true, true, false,
                        true, "BFA1", "BFA2",
                        false);
    }

    @NotNull
    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, "Bearer user1");
        var s2SToken = MockUtils.generateDummyS2SToken(AUTHORISED_SERVICE);
        headers.add("ServiceAuthorization", "Bearer " + s2SToken);
        headers.add(Constants.CORRELATION_ID_HEADER_NAME, CORRELATION_ID);
        return headers;
    }

    private RefreshJobEntity getRecordsFromRefreshJobTable(Long jobId) {

        var rm = (RowMapper<RefreshJobEntity>) (ResultSet result, int rowNum) -> {
            var entity = new RefreshJobEntity();
            entity.setJobId(result.getLong("job_id"));
            entity.setStatus(result.getString("status"));
            entity.setLog(result.getString("log"));
            entity.setComments(result.getString("comments"));
            entity.setLinkedJobId(result.getLong("linked_job_id"));
            if (result.getArray("user_ids") != null) {
                entity.setUserIds((String[]) result.getArray("user_ids").getArray());
            }
            return entity;
        };

        return template.queryForObject(REFRESH_JOB_RECORDS_QUERY, rm, jobId);
    }
}
