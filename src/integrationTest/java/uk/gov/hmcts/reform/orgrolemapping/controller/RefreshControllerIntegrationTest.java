package uk.gov.hmcts.reform.orgrolemapping.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Request;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.CRDService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RetrieveDataService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RoleAssignmentService;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.RASFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.CRDFeignClientFallback;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RefreshControllerIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(RefreshControllerIntegrationTest.class);

    private static final String REFRESH_JOB_RECORDS_QUERY = "SELECT job_id, status, user_ids, linked_job_id, comments " +
            "FROM refresh_jobs where job_id=?";
    private static final String AUTHORISED_SERVICE = "ccd_gw";
    private static final String ROLE_NAME_STCW = "senior-tribunal-caseworker";
    private static final String ROLE_NAME_TCW = "tribunal-caseworker";
    private static final String URL = "/am/role-mapping/refresh";

    private MockMvc mockMvc;
    private JdbcTemplate template;

    @Inject
    private WebApplicationContext wac;

    @Autowired
    private DataSource ds;

    private CRDFeignClient crdFeignClient = mock(CRDFeignClient.class);

    @MockBean
    private RASFeignClient rasFeignClient;

    @MockBean
    StatelessKieSession kieSession;

    @MockBean
    RequestMappingService requestMappingService;

   // @InjectMocks
   // CRDService crdService = new CRDService(crdFeignClient);

    @MockBean
    RoleAssignmentService roleAssignmentService;

    @MockBean
    RetrieveDataService retrieveDataService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    SecurityUtils securityUtils;

    @ClassRule
    public static WireMockRule roleAssignmentServiceMock = new WireMockRule(wireMockConfig().port(4096));

    @ClassRule
    public static final WireMockRule crdClient = new WireMockRule(wireMockConfig().port(8095));

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

        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        // Mock for the CRD Service
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("total_records", "2");
        ResponseEntity<List<UserProfilesResponse>> userProfilesResponse = new ResponseEntity<>(IntTestDataBuilder
                .buildListOfUserProfilesResponse("IAC",false, "1", "2", ROLE_NAME_STCW,
                        ROLE_NAME_TCW, true, true,false,
                        true, "BFA1", "BFA2",false),
                headers, HttpStatus.OK);
        //doReturn(userProfilesResponse, userProfilesResponse).when(crdService).fetchCaseworkerDetailsByServiceName(
        //        "IAC", 2, 0, "ASC", "");
        //doReturn(userProfilesResponse).when(crdService).fetchCaseworkerDetailsByServiceName(
         //       any(), any(), any(), any(), any());

        //batch executing the rules process
        kieSession = KieServices.Factory.get().getKieClasspathContainer().newStatelessKieSession("org-role-mapping-validation-session");
        //requestMappingService = new RequestMappingService(roleAssignmentService, kieSession, securityUtils);

        //
        Mockito.when(roleAssignmentService.createRoleAssignment(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED)
                        .body(AssignmentRequestBuilder.buildAssignmentRequest(false)));

    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete() throws Exception {
        //  logger.info(" RefreshJob record count before refresh assignments {}", getHistoryRecordsCount());
        Long jobId = 1L;
        // Mock for the CRD Service
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("total_records", "2");
        ResponseEntity<List<UserProfilesResponse>> userProfilesResponse = new ResponseEntity<>(IntTestDataBuilder
                .buildListOfUserProfilesResponse("IAC",false, "1", "2", ROLE_NAME_STCW,
                        ROLE_NAME_TCW, true, true,false,
                        true, "BFA1", "BFA2",false),
                headers, HttpStatus.OK);
        doReturn(userProfilesResponse).when(crdFeignClient).getCaseworkerDetailsByServiceName(
                any(), any(), any(), any(), any());

        mockMvc.perform(post(URL)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .param("jobId", jobId.toString()))
                .andExpect(status().is(202))
                .andReturn();

        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("Completed", refreshJob.getStatus());

    }


   // @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToPartialComplete() throws Exception {
        //  logger.info(" RefreshJob record count before refresh assignments {}", getHistoryRecordsCount());
        Long jobId = 1L;

        //Mockito.when(crdFeignClient.getCaseworkerDetailsByServiceName(any(), any(), any(), any(), any()))
        //        .thenReturn(new ResponseEntity<>(IntTestDataBuilder.buildListOfUserProfilesResponse("IAC",
         //               false, "1", "2",
         //                       ROLE_NAME_STCW, ROLE_NAME_TCW,true, true,
          //                      false, true, "BFA1", "BFA2",
          //                      false), HttpStatus.OK));
        //setCRDWireMock();
        mockMvc.perform(post(URL)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .param("jobId", jobId.toString()))
                .andExpect(status().is(202))
                .andReturn();

        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("ABORTED", refreshJob.getStatus());
        assertNotNull(refreshJob.getUserIds());

    }

    //@Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithFailedUsersToComplete() throws Exception {
        logger.info(" Method shouldDeleteRoleAssignmentsByProcessAndReference starts :");
        Long jobId = 3L;

        mockMvc.perform(post(URL)
                .contentType(CONTENT_TYPE)
                .headers(getHttpHeaders())
                .param("jobId", jobId.toString())
                .content(mapper.writeValueAsBytes(TestDataBuilder.createUserRequest())))
                .andExpect(status().is(202))
                .andReturn();

        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("Completed", refreshJob.getStatus());

    }

    //@Test
    public void shouldFailProcessRefreshRoleAssignmentsWithOutJobID() throws Exception{
    logger.info(" Method shouldDeleteRoleAssignmentsByAssignmentId starts : ");
        mockMvc.perform(post(URL)
                .contentType(CONTENT_TYPE)
                .headers(getHttpHeaders()))
                .andExpect(status().is(400))
                .andReturn();
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
            //entity.setLog(result.getString("log"));
            entity.setComments(result.getString("comments"));
            entity.setLinkedJobId(result.getLong("linked_job_id"));
            if (result.getArray("user_ids") != null)
                entity.setUserIds((String[])result.getArray("user_ids").getArray());
            return entity;
        };
        return template.queryForObject(REFRESH_JOB_RECORDS_QUERY, new Object[]{jobId}, rm);
    }

    public void setCRDWireMock() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        //this stub is overruled by the mocking of its bean at the top of this test class
        crdClient.stubFor(WireMock.get(urlEqualTo("/refdata/internal/staff/usersByServiceName"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(new CRDFeignClientFallback()
                                .getCaseworkerDetailsByServiceName("IAC",2,0,
                                        "ASC","")))
                        .withStatus(HttpStatus.OK.value())
                ));
    }
}

