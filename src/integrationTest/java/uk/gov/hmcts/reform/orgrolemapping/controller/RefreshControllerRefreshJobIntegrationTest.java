package uk.gov.hmcts.reform.orgrolemapping.controller;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.UnauthorizedServiceException;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Appointment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBookingResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JBSFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.RASFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureConditionEvaluator;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.ABORTED;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.COMPLETED;
import static uk.gov.hmcts.reform.orgrolemapping.v1.V1.Error.UNAUTHORIZED_SERVICE;

@TestPropertySource(properties = {
    "refresh.Job.authorisedServices=am_org_role_mapping_service,am_role_assignment_refresh_batch",
    "feign.client.config.jrdClient.v2Active=false"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RefreshControllerRefreshJobIntegrationTest extends BaseTestIntegration {

    private static final Logger logger = LoggerFactory.getLogger(RefreshControllerIntegrationTest.class);

    private final WiremockFixtures wiremockFixtures = new WiremockFixtures();

    private static final String REFRESH_JOB_RECORDS_QUERY = "SELECT job_id, status, user_ids, linked_job_id,"
            + " comments, log FROM refresh_jobs where job_id=?";
    private static final String AUTHORISED_SERVICE = "am_role_assignment_refresh_batch";
    private static final String ROLE_NAME_STCW = "senior-tribunal-caseworker";
    private static final String ROLE_NAME_TCW = "tribunal-caseworker";
    private static final String URL = "/am/role-mapping/refresh";

    private MockMvc mockMvc;
    private JdbcTemplate template;

    @Inject
    private WebApplicationContext wac;

    @Autowired
    private DataSource ds;

    @MockBean
    private CRDFeignClient crdFeignClient;

    @MockBean
    private JRDFeignClient jrdFeignClient;

    @MockBean
    private JBSFeignClient jbsFeignClient;

    @MockBean
    private RASFeignClient rasFeignClient;

    @MockBean
    private RequestMappingService requestMappingService;

    @MockBean
    private FeatureConditionEvaluator featureConditionEvaluation;

    @MockBean
    private SecurityUtils securityUtils;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    Lock sequential = new ReentrantLock();

    private static final MediaType JSON_CONTENT_TYPE = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            StandardCharsets.UTF_8
    );

    @BeforeEach
    public void setUp() throws Exception {
        sequential.lock();
        template = new JdbcTemplate(ds);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        doReturn(true).when(featureConditionEvaluation).preHandle(any(),any(),any());
        MockUtils.setSecurityAuthorities(authentication, MockUtils.ROLE_CASEWORKER);
        wiremockFixtures.resetRequests();
    }

    @AfterEach
    public void tearDown() throws Exception {
        sequential.unlock();
    }

    @Test
    @Order(1)
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process successful");
        Long jobId = 1L;
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("NEW", refreshJob.getStatus());

        mockCRDService();
        mockRequestMappingServiceWithStatus(HttpStatus.CREATED);

        mockMvc.perform(post(URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .param("jobId", jobId.toString()))
                .andExpect(status().is(202))
                .andReturn();

        await().timeout(60, TimeUnit.SECONDS).untilAsserted(() -> Assertions.assertTrue(
                isRefreshJobInStatus(jobId, COMPLETED)));

        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(COMPLETED, refreshJob.getStatus());
        assertEquals(0, refreshJob.getUserIds().length);
        assertNotNull(refreshJob.getLog());
    }

    @Test
    @Order(2)
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToAborted() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process Aborted");
        Long jobId = 1L;

        mockCRDService();
        mockRequestMappingServiceWithStatus(UNPROCESSABLE_ENTITY);

        mockMvc.perform(post(URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .param("jobId", jobId.toString()))
                .andExpect(status().is(202))
                .andReturn();

        await().timeout(60, TimeUnit.SECONDS).untilAsserted(() -> Assertions.assertTrue(
                isRefreshJobInStatus(jobId, ABORTED)));

        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(ABORTED, refreshJob.getStatus());
        assertNotNull(refreshJob.getUserIds());
        assertThat(refreshJob.getLog(),containsString(String.join(",", refreshJob.getUserIds())));
    }

    @Disabled("Intermittent DTSAM-111")
    @Test
    @Order(3)
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToAborted_status422() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process Non recoverable retain same state");
        Long jobId = 1L;

        mockCRDService();
        mockRequestMappingServiceWithStatus(UNPROCESSABLE_ENTITY);

        mockMvc.perform(post(URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .param("jobId", jobId.toString()))
                .andExpect(status().is(202))
                .andReturn();

        await().timeout(60, TimeUnit.SECONDS).untilAsserted(() -> Assertions.assertTrue(
                isRefreshJobInStatus(jobId, ABORTED)));

        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        logger.info(" -- Refresh Role Assignment record updated -- " + refreshJob.getStatus());
        assertEquals("ABORTED", refreshJob.getStatus());
        assertNotNull(refreshJob.getUserIds());
        assertThat(refreshJob.getLog(),containsString(String.join(",", refreshJob.getUserIds())));
    }

    @Disabled("Intermittent AM-2919")
    @Test
    @Order(4)
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToPartialComplete_status422() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process Partial Success");
        Long jobId = 1L;

        mockCRDService();
        mockRequestMappingServiceWithStatus(UNPROCESSABLE_ENTITY);

        mockMvc.perform(post(URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .param("jobId", jobId.toString()))
                .andExpect(status().is(202))
                .andReturn();

        await().timeout(60, TimeUnit.SECONDS).untilAsserted(() -> Assertions.assertTrue(
                isRefreshJobInStatus(jobId, ABORTED)));

        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(ABORTED, refreshJob.getStatus());
        assertNotNull(refreshJob.getUserIds());
        assertThat(refreshJob.getLog(), containsString(String.join(",", refreshJob.getUserIds())));
    }

    //@Disabled("Intermittent DTSAM-111")
    @Test
    @Order(5)
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithFailedUsersToComplete() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_SERVICE);

        logger.info(" RefreshJob record With JobId and failed UserIds to process successful");
        Long jobId = 3L;

        //Verify before job
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("NEW", refreshJob.getStatus());
        RefreshJobEntity linkedJob = getRecordsFromRefreshJobTable(refreshJob.getLinkedJobId());
        assertNotNull(linkedJob.getUserIds());
        assertEquals(ABORTED, linkedJob.getStatus());
        assertNotEquals(0, linkedJob.getUserIds().length);

        doReturn(new ResponseEntity<>(IntTestDataBuilder
                .buildListOfUserProfiles(false, false, "1", "2",
                        ROLE_NAME_STCW, ROLE_NAME_TCW,
                        true, true, false,
                        true, "BFA1", "BFA2",
                        false), HttpStatus.OK)).when(crdFeignClient).getCaseworkerDetailsById(any());
        mockRequestMappingServiceWithStatus(HttpStatus.CREATED);

        mockMvc.perform(post(URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .content(mapper.writeValueAsBytes(IntTestDataBuilder.buildUserRequest()))
                        .param("jobId", jobId.toString()))
                .andExpect(status().is(202))
                .andReturn();

        await().timeout(60, TimeUnit.SECONDS).untilAsserted(() -> Assertions.assertTrue(
                isRefreshJobInStatus(jobId, COMPLETED)));

        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(COMPLETED, refreshJob.getStatus());
        assertEquals(0, refreshJob.getUserIds().length);
        assertNotNull(refreshJob.getLog());
    }

    @Test
    @Order(6)
    public void shouldFailProcessRefreshRoleAssignmentsWithFailedUsersAndWithOutJobID() throws Exception {
        logger.info(" Refresh Job with optional Users and without mandatory jobId as a param");
        mockMvc.perform(post(URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .content(mapper.writeValueAsBytes(IntTestDataBuilder.buildUserRequest())))
                .andExpect(status().is(400))
                .andReturn();
    }

    @Test
    @Order(7)
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
    @Order(8)
    public void shouldFailProcessRefreshRoleAssignmentsWithInvalidJobID() throws Exception {
        logger.info(" Refresh Job with optional Users and without mandatory jobId as a param");
        mockMvc.perform(post(URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .param("jobId", "abc")
                        .content(mapper.writeValueAsBytes(IntTestDataBuilder.buildUserRequest())))
                .andExpect(status().is(400))
                .andReturn();
    }

    @Test
    @Order(9)
    public void shouldFailProcessRefreshRoleAssignmentsWithOutJobID() throws Exception {
        logger.info(" Refresh Job with optional Users and without mandatory jobId as a param");
        mockMvc.perform(post(URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders()))
                .andExpect(status().is(400))
                .andReturn();
    }

    @Test
    @Order(10)
    public void shouldFailProcessRefreshRoleAssignmentsWithInvalidServiceToken() throws Exception {
        logger.info("Refresh request rejected with invalid service token");

        when(securityUtils.getServiceName()).thenReturn("ccd_gw");

        MvcResult result = mockMvc.perform(post(URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .param("jobId", String.valueOf(1L)))
                .andExpect(status().is(403))
                .andReturn();

        assertTrue(result.getResolvedException() instanceof UnauthorizedServiceException);
        assertThat(result.getResolvedException().getMessage(), equalTo(UNAUTHORIZED_SERVICE));
    }

    @Test
    @Order(11)
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete_retryFail() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process fail");
        Long jobId = 1L;
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("NEW", refreshJob.getStatus());

        doThrow(RuntimeException.class).when(crdFeignClient).getCaseworkerDetailsByServiceName(
                anyString(), anyInt(), anyInt(), anyString(), anyString());

        mockMvc.perform(post(URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .param("jobId", jobId.toString()))
                .andExpect(status().is(202))
                .andReturn();

        refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("NEW", refreshJob.getStatus());// failed process should change the status to IN-PROGRESS
    }

    @Disabled("Intermittent DTSAM-111")
    @Test
    @Order(12)
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete_CRDRetry() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_SERVICE);

        logger.info(" RefreshJob record With JobId retry success third time to process successful");
        Long jobId = 1L;
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("NEW", refreshJob.getStatus());

        doThrow(RuntimeException.class).doThrow(RuntimeException.class).doReturn(buildUserProfileResponse())
                .when(crdFeignClient).getCaseworkerDetailsByServiceName(
                        anyString(), anyInt(), anyInt(), anyString(), anyString());
        mockRequestMappingServiceWithStatus(HttpStatus.CREATED);

        mockMvc.perform(post(URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .param("jobId", jobId.toString()))
                .andExpect(status().is(202))
                .andReturn();

        await().timeout(60, TimeUnit.SECONDS).untilAsserted(() -> Assertions.assertTrue(
                isRefreshJobInStatus(jobId, COMPLETED)));

        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(COMPLETED, refreshJob.getStatus());
        assertEquals(0, refreshJob.getUserIds().length);
        assertNotNull(refreshJob.getLog());
    }

    private ResponseEntity<JudicialBookingResponse> buildJudicialBookingsResponse(String... userIds) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("total_records", "" + userIds.length);
        List<JudicialBooking> bookings = new ArrayList<>();
        for (var userId:userIds) {
            bookings.add(JudicialBooking.builder().beginTime(ZonedDateTime.now())
                    .endTime(ZonedDateTime.now().plusDays(5)).userId(userId)
                    .locationId("location").regionId("region").build());
        }
        return new ResponseEntity<>(new JudicialBookingResponse(bookings), headers, HttpStatus.OK);
    }

    private ResponseEntity<List<JudicialProfile>> buildJudicialProfilesResponse(String... userIds) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("total_records", "" + userIds.length);
        List<JudicialProfile> bookings = new ArrayList<>();
        for (var userId:userIds) {
            bookings.add(JudicialProfile.builder().sidamId(userId)
                    .appointments(List.of(Appointment.builder().appointment("Tribunal Judge")
                            .appointmentType("Fee Paid").build())).build());
        }
        return new ResponseEntity<>(bookings, headers, HttpStatus.OK);
    }

    @NotNull
    private ResponseEntity<List<CaseWorkerProfilesResponse>> buildUserProfileResponse() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("total_records", "2");
        return new ResponseEntity<>(IntTestDataBuilder
                .buildListOfUserProfilesResponse("IAC", false, "1", "2", ROLE_NAME_STCW,
                        ROLE_NAME_TCW, true, true, false,
                        true, "BFA1", "BFA2", false),
                headers, HttpStatus.OK);
    }


    private void mockCRDService() {
        ResponseEntity<List<CaseWorkerProfilesResponse>> userProfilesResponse = buildUserProfileResponse();
        doReturn(userProfilesResponse).when(crdFeignClient).getCaseworkerDetailsByServiceName(
                anyString(), anyInt(), anyInt(), anyString(), anyString());
    }

    @SuppressWarnings("unchecked")
    private void mockRequestMappingServiceWithStatus(HttpStatus status) {
        doReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(ResponseEntity.status(status).body(
                new RoleAssignmentRequestResource(AssignmentRequestBuilder.buildAssignmentRequest(
                        false))))))
                .when(requestMappingService).createAssignments(any(), any());
    }

    @NotNull
    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, "Bearer user1");
        var s2SToken = MockUtils.generateDummyS2SToken(AUTHORISED_SERVICE);
        headers.add("ServiceAuthorization", "Bearer " + s2SToken);
        headers.add(Constants.CORRELATION_ID_HEADER_NAME, "38a90097-434e-47ee-8ea1-9ea2a267f51d");
        return headers;
    }

    public RefreshJobEntity getRecordsFromRefreshJobTable(Long jobId) {

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

    public boolean isRefreshJobInStatus(Long jobId, String status) {
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        if (refreshJob.getStatus().equals(status)) {
            return true;
        } else {
            return false;
        }
    }
}
