package uk.gov.hmcts.reform.orgrolemapping.controller;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.config.RefreshJobRowMapper;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RefreshControllerIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(RefreshControllerIntegrationTest.class);

    private static final String ACTOR_ID = "123e4567-e89b-42d3-a456-556642445612";
    private static final String REFRESH_JOB_RECORDS_QUERY = "SELECT job_id, status, user_ids, linked_job_id " +
            "FROM refresh_jobs where job_id=?";
    private static final String AUTHORISED_SERVICE = "ccd_gw";

    private MockMvc mockMvc;
    private JdbcTemplate template;

    @Inject
    private WebApplicationContext wac;

    @Autowired
    private DataSource ds;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    private static final MediaType JSON_CONTENT_TYPE = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            StandardCharsets.UTF_8
    );

    @Before
    public void setUp() throws Exception {
        template = new JdbcTemplate(ds);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockitoAnnotations.initMocks(this);
        String uid = "6b36bfc6-bb21-11ea-b3de-0242ac130006";

        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        //doReturn(true).when(featureConditionEvaluation).preHandle(any(),any(),any());

    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithComplete() throws Exception {
      //  logger.info(" RefreshJob record count before refresh assignments {}", getHistoryRecordsCount());
        Long jobId = 1L;
        final String url = "/am/role-mapping/refresh";

        mockMvc.perform(post(url)
                            .contentType(JSON_CONTENT_TYPE)
                            .headers(getHttpHeaders())
                            .param("jobId", jobId.toString())
        ).andExpect(status().is(202)).andReturn();


        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("Completed", refreshJob.getStatus());

    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithPartialComplete() throws Exception {
        //  logger.info(" RefreshJob record count before refresh assignments {}", getHistoryRecordsCount());
        Long jobId = 1L;
        final String url = "/am/role-mapping/refresh";

        mockMvc.perform(post(url)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .param("jobId", jobId.toString())
        ).andExpect(status().is(202)).andReturn();


        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("Completed", refreshJob.getStatus());

    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithFailedUsersToComplete() throws Exception {

        logger.info(" Method shouldDeleteRoleAssignmentsByProcessAndReference starts :");
        Long jobId = 3L;
        final String url = "/am/role-mapping/refresh";

        mockMvc.perform(post(url)
                            .contentType(CONTENT_TYPE)
                            .headers(getHttpHeaders())
                            .param("jobId", jobId.toString())
                            .content(mapper.writeValueAsBytes(TestDataBuilder.createUserRequest()))
        )
            .andExpect(status().is(202))
            .andReturn();

        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("Completed", refreshJob.getStatus());

    }

    @Test

    public void shouldDeleteRoleAssignmentsByAssignmentId() throws Exception {

        logger.info(" Method shouldDeleteRoleAssignmentsByAssignmentId starts : ");
        final String url = "/am/role-mapping/refresh?jobId=" + 1;

        mockMvc.perform(delete(url)
                            .contentType(JSON_CONTENT_TYPE)
                            .headers(getHttpHeaders())
                            .param("process", "S-052")
                            .param("reference", "S-052")
        )
            .andExpect(status().is(204))
            .andReturn();

        //assertAssignmentRecords();

    }

    private Integer getRefreshJobRecordsCount() {
        return template.queryForObject(REFRESH_JOB_RECORDS_QUERY, Integer.class);
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
        return template.queryForObject(REFRESH_JOB_RECORDS_QUERY, new Object[]{jobId},
                new RefreshJobRowMapper());
    }
}
