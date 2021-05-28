package uk.gov.hmcts.reform.orgrolemapping.controller;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.TopicConsumer;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.ABORTED;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.COMPLETED;

public class RefreshControllerIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(RefreshControllerIntegrationTest.class);

    private static final String REFRESH_JOB_RECORDS_QUERY = "SELECT job_id, status, user_ids, linked_job_id,"
            + " comments, log FROM refresh_jobs where job_id=?";
    private static final String AUTHORISED_SERVICE = "orm_batch";
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
    private RequestMappingService requestMappingService;

    @MockBean
    private TopicConsumer topicConsumer;

    private static final MediaType JSON_CONTENT_TYPE = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            StandardCharsets.UTF_8
    );

    @Before
    public void setUp() {
        template = new JdbcTemplate(ds);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete() throws Exception {
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

        Thread.sleep(1000);
        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(COMPLETED, refreshJob.getStatus());
        assertEquals(0, refreshJob.getUserIds().length);
        assertNotNull(refreshJob.getLog());
    }

    @Ignore
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToAborted() throws Exception {
        logger.info(" RefreshJob record With Only JobId to process Aborted");
        Long jobId = 1L;

        mockCRDService();
        mockRequestMappingServiceWithStatus(INTERNAL_SERVER_ERROR);

        mockMvc.perform(post(URL)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .param("jobId", jobId.toString()))
                .andExpect(status().is(202))
                .andReturn();

        Thread.sleep(1000);
        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(ABORTED, refreshJob.getStatus());
        assertNotNull(refreshJob.getUserIds());
        assertThat(refreshJob.getLog(),containsString(String.join(",", refreshJob.getUserIds())));
    }

    @Ignore
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToAborted_status422() throws Exception {
        logger.info(" RefreshJob record With Only JobId to process Non recoverable retain same state");
        Long jobId = 1L;

        mockCRDService();
        mockRequestMappingServiceWithStatus(INTERNAL_SERVER_ERROR);

        mockMvc.perform(post(URL)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .param("jobId", jobId.toString()))
                .andExpect(status().is(202))
                .andReturn();

        Thread.sleep(5000);
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

        mockCRDService();
        mockRequestMappingServiceWithStatus(INTERNAL_SERVER_ERROR);

        mockMvc.perform(post(URL)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .param("jobId", jobId.toString()))
                .andExpect(status().is(202))
                .andReturn();

        Thread.sleep(1000);
        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(ABORTED, refreshJob.getStatus());
        assertNotNull(refreshJob.getUserIds());
        assertThat(refreshJob.getLog(), containsString(String.join(",", refreshJob.getUserIds())));
    }

    @Ignore
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToPartialComplete_status422() throws Exception {
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

        Thread.sleep(2000);
        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(ABORTED, refreshJob.getStatus());
        assertNotNull(refreshJob.getUserIds());
        assertThat(refreshJob.getLog(), containsString(String.join(",", refreshJob.getUserIds())));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithFailedUsersToComplete() throws Exception {
        logger.info(" RefreshJob record With JobId and failed UserIds to process successful");
        Long jobId = 3L;

        //Verify before job
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("NEW", refreshJob.getStatus());
        RefreshJobEntity linkedJob = getRecordsFromRefreshJobTable(refreshJob.getLinkedJobId());
        assertNotNull(linkedJob.getUserIds());
        assertEquals(ABORTED, linkedJob.getStatus());
        assertNotEquals(0, linkedJob.getUserIds().length);

        // Mock for the CRD Service
        Mockito.when(crdFeignClient.getCaseworkerDetailsById(any()))
                .thenReturn(new ResponseEntity<>(
                        IntTestDataBuilder.buildListOfUserProfiles(false, false, "1",
                                "2", ROLE_NAME_STCW, ROLE_NAME_TCW,true, true,
                                false,true, "BFA1", "BFA2",
                                false),
                        HttpStatus.OK));
        mockCRDService();
        mockRequestMappingServiceWithStatus(HttpStatus.CREATED);

        mockMvc.perform(post(URL)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .content(mapper.writeValueAsBytes(IntTestDataBuilder.buildUserRequest()))
                .param("jobId", jobId.toString()))
                .andExpect(status().is(202))
                .andReturn();

        Thread.sleep(1000);
        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(COMPLETED, refreshJob.getStatus());
        assertEquals(0, refreshJob.getUserIds().length);
        assertNotNull(refreshJob.getLog());
    }

    @Test
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
                .content(mapper.writeValueAsBytes(IntTestDataBuilder.buildUserRequest())))
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

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete_retryFail() throws Exception {
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

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete_CRDRetry() throws Exception {
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

        Thread.sleep(9000);
        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(COMPLETED, refreshJob.getStatus());
        assertEquals(0, refreshJob.getUserIds().length);
        assertNotNull(refreshJob.getLog());
    }

    @NotNull
    private ResponseEntity<List<UserProfilesResponse>> buildUserProfileResponse() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("total_records", "2");
        return new ResponseEntity<>(IntTestDataBuilder
                .buildListOfUserProfilesResponse("IAC", false, "1", "2", ROLE_NAME_STCW,
                        ROLE_NAME_TCW, true, true, false,
                        true, "BFA1", "BFA2", false),
                headers, HttpStatus.OK);
    }

    private void mockCRDService() {
        ResponseEntity<List<UserProfilesResponse>> userProfilesResponse = buildUserProfileResponse();
        doReturn(userProfilesResponse).when(crdFeignClient).getCaseworkerDetailsByServiceName(
                anyString(), anyInt(), anyInt(), anyString(), anyString());
    }

    private void mockRequestMappingServiceWithStatus(HttpStatus status) {
        doReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(ResponseEntity.status(status).body(
                new RoleAssignmentRequestResource(AssignmentRequestBuilder.buildAssignmentRequest(
                        false))))))
                .when(requestMappingService).createCaseWorkerAssignments(any());
    }

    @NotNull
    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, "Bearer user1");
        String s2SToken = MockUtils.generateDummyS2SToken(AUTHORISED_SERVICE);
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
        return template.queryForObject(REFRESH_JOB_RECORDS_QUERY, new Object[]{jobId}, rm);
    }

}