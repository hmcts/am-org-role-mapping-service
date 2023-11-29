package uk.gov.hmcts.reform.orgrolemapping.controller;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AppointmentV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBookingResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfileV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialRefreshRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.COMPLETED;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.FAILED_ROLE_REFRESH;
import static uk.gov.hmcts.reform.orgrolemapping.v1.V1.Error.UNAUTHORIZED_SERVICE;

@TestPropertySource(properties = {
    "refresh.Job.authorisedServices=am_org_role_mapping_service,am_role_assignment_refresh_batch",
    "feign.client.config.jrdClient.v2Active=true"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("itest")
@EnableConfigurationProperties
public class RefreshControllerIntegrationV2Test extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(RefreshControllerIntegrationV2Test.class);

    private final WiremockFixtures wiremockFixtures = new WiremockFixtures();

    private static final String REFRESH_JOB_RECORDS_QUERY = "SELECT job_id, status, user_ids, linked_job_id,"
            + " comments, log FROM refresh_jobs where job_id=?";
    private static final String AUTHORISED_SERVICE = "am_role_assignment_refresh_batch";
    private static final String ROLE_NAME_STCW = "senior-tribunal-caseworker";
    private static final String ROLE_NAME_TCW = "tribunal-caseworker";
    private static final String URL = "/am/role-mapping/refresh";
    private static final String JUDICIAL_REFRESH_URL = "/am/role-mapping/judicial/refresh";

    private MockMvc mockMvc;
    private JdbcTemplate template;

    @Autowired
    private RefreshController controller;

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

    private static final MediaType JSON_CONTENT_TYPE = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            StandardCharsets.UTF_8
    );

    @Before
    public void setUp() throws Exception {
        template = new JdbcTemplate(ds);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockitoAnnotations.openMocks(this);
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        doReturn(true).when(featureConditionEvaluation).preHandle(any(),any(),any());
        MockUtils.setSecurityAuthorities(authentication, MockUtils.ROLE_CASEWORKER);
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

    @Ignore("Intermittent AM-2919")
    @Test
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

    @Ignore("Intermittent AM-2919")
    @Test
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

    @Ignore("Intermittent AM-2919")
    @Test
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

        Thread.sleep(9000);
        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(COMPLETED, refreshJob.getStatus());
        assertEquals(0, refreshJob.getUserIds().length);
        assertNotNull(refreshJob.getLog());
    }

    @Test
    public void shouldProcessRefreshRoleAssignmentsWithJudicialProfilesV2() throws Exception {
        logger.info(" Refresh role assignments successfully with valid user profiles");
        var uuid = java.util.UUID.randomUUID().toString();
        doReturn(buildJudicialProfilesResponseV2(uuid)).when(jrdFeignClient).getJudicialDetailsById(any(), any());
        doReturn(buildJudicialBookingsResponse(uuid)).when(jbsFeignClient).getJudicialBookingByUserIds(any());
        mockRequestMappingServiceBookingParamWithStatus(HttpStatus.CREATED);

        MvcResult result = mockMvc.perform(post(JUDICIAL_REFRESH_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .content(mapper.writeValueAsBytes(JudicialRefreshRequest.builder()
                                .refreshRequest(IntTestDataBuilder.buildUserRequest()).build())))
                .andExpect(status().is(200))
                .andReturn();
        var contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(Constants.SUCCESS_ROLE_REFRESH));
        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
    }

    @Test
    public void shouldFailProcessRefreshRoleAssignmentsWithJudicialProfiles_withFailedRoleAssignmentsV2()
            throws Exception {
        logger.info(" Refresh role assignments failed with valid user profiles");
        var uuid = java.util.UUID.randomUUID().toString();
        doReturn(buildJudicialProfilesResponseV2(uuid)).when(jrdFeignClient).getJudicialDetailsById(any(), any());
        doReturn(buildJudicialBookingsResponse(uuid)).when(jbsFeignClient).getJudicialBookingByUserIds(any());
        mockRequestMappingServiceBookingParamWithStatus(HttpStatus.UNPROCESSABLE_ENTITY);

        MvcResult result = mockMvc.perform(post(JUDICIAL_REFRESH_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .content(mapper.writeValueAsBytes(JudicialRefreshRequest.builder()
                                .refreshRequest(IntTestDataBuilder.buildUserRequest()).build())))
                .andExpect(status().is(422))
                .andReturn();
        var contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(Constants.FAILED_ROLE_REFRESH));
        logger.info(" -- Refresh Role Assignment record fail to update -- ");
    }

    @Test
    public void shouldFailProcessRefreshRoleAssignmentsWithJudicialProfiles_withEmptyJudicialBookingsV2()
            throws Exception {
        logger.info(" Refresh role assignments with empty bookings");
        var uuid = java.util.UUID.randomUUID().toString();
        doReturn(buildJudicialProfilesResponseV2(uuid)).when(jrdFeignClient).getJudicialDetailsById(any(), any());
        doReturn(buildJudicialBookingsResponse()).when(jbsFeignClient).getJudicialBookingByUserIds(any());
        mockRequestMappingServiceBookingParamWithStatus(HttpStatus.CREATED);

        MvcResult result = mockMvc.perform(post(JUDICIAL_REFRESH_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .content(mapper.writeValueAsBytes(JudicialRefreshRequest.builder()
                                .refreshRequest(IntTestDataBuilder.buildUserRequest()).build())))
                .andExpect(status().isOk())
                .andReturn();
        var contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(Constants.SUCCESS_ROLE_REFRESH));
        logger.info(" -- Refresh Role Assignment record updated without bookings -- ");
    }

    @Test
    public void shouldFailProcessRefreshRoleAssignmentsWithJudicialProfiles_withNotFoundJudicialProfiles()
            throws Exception {
        logger.info(" Refresh role assignments with empty bookings");
        ResponseEntity response = ResponseEntity.status(404).body(Map.of(
                "errorDescription", "The User Profile data could not be found",
                "status", "Not Found"));
        doReturn(response).when(jrdFeignClient).getJudicialDetailsById(any(), any());
        doReturn(buildJudicialBookingsResponse()).when(jbsFeignClient).getJudicialBookingByUserIds(any());
        mockRequestMappingServiceBookingParamWithStatus(HttpStatus.CREATED);

        MvcResult result = mockMvc.perform(post(JUDICIAL_REFRESH_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .content(mapper.writeValueAsBytes(JudicialRefreshRequest.builder()
                                .refreshRequest(IntTestDataBuilder.buildUserRequest()).build())))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(Constants.SUCCESS_ROLE_REFRESH));
    }

    @Test
    public void shouldFailProcessRefreshRoleAssignmentsWithJudicialProfiles_withEmptyJudicialProfiles()
            throws Exception {
        logger.info(" Refresh role assignments with empty bookings");
        ResponseEntity response = ResponseEntity.status(501).body(Map.of(
                "errorDescription", "The User Profile data could not be found",
                "status", "Not Found"));
        doReturn(response).when(jrdFeignClient).getJudicialDetailsById(any(), any());

        mockMvc.perform(post(JUDICIAL_REFRESH_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .content(mapper.writeValueAsBytes(JudicialRefreshRequest.builder()
                                .refreshRequest(IntTestDataBuilder.buildUserRequest()).build())))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorDescription")
                        .value(containsString(FAILED_ROLE_REFRESH)))
                .andReturn();
    }

    @Test
    public void shouldRejectJudicialRefreshRequest_withEmptyBody() throws Exception {
        logger.info(" Refresh request rejected with empty request");
        mockMvc.perform(post(JUDICIAL_REFRESH_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .content(mapper.writeValueAsBytes(JudicialRefreshRequest.builder().build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorDescription")
                        .value(containsString("Empty user request")))
                .andReturn();
    }

    @Test
    public void shouldRejectJudicialRefreshRequest_withEmptyUserList() throws Exception {
        logger.info(" Refresh request rejected with empty user request");
        JudicialRefreshRequest request = JudicialRefreshRequest.builder()
                .refreshRequest(UserRequest.builder().userIds(Collections.emptyList()).build()).build();
        mockMvc.perform(post(JUDICIAL_REFRESH_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorDescription")
                        .value(containsString("Empty user request")))
                .andReturn();

    }

    @Test
    public void shouldRejectJudicialRefreshRequest_withInvalidUserIdFormat() throws Exception {
        logger.info(" Refresh role assignments failed with invalid valid user profiles format");

        JudicialRefreshRequest request = JudicialRefreshRequest.builder()
                .refreshRequest(UserRequest.builder().userIds(List.of("abc-123$")).build()).build();
        mockMvc.perform(post(JUDICIAL_REFRESH_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorDescription")
                        .value(containsString("The input parameter: \"abc-123$\", "
                                + "does not comply with the required pattern")))
                .andReturn();
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

    private ResponseEntity<List<JudicialProfileV2>> buildJudicialProfilesResponseV2(String... userIds) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("total_records", "" + userIds.length);
        List<JudicialProfileV2> bookings = new ArrayList<>();
        for (var userId:userIds) {
            bookings.add(JudicialProfileV2.builder().sidamId(userId)
                    .appointments(List.of(AppointmentV2.builder().appointment("Tribunal Judge")
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

    @SuppressWarnings("unchecked")
    private void mockRequestMappingServiceBookingParamWithStatus(HttpStatus status) {
        doReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(ResponseEntity.status(status).body(
                new RoleAssignmentRequestResource(AssignmentRequestBuilder.buildAssignmentRequest(
                        false))))))
                .when(requestMappingService).createAssignments(any(), any(), any());
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
}
