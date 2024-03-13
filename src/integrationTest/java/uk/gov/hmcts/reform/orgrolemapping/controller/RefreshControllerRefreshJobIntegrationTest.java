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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.UnauthorizedServiceException;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfileV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.PersistenceService;
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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.ABORTED;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.COMPLETED;
import static uk.gov.hmcts.reform.orgrolemapping.controller.RefreshControllerRefreshJobIntegrationTest.TEST_PAGE_SIZE;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_CCD_GW;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_ORM;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_RARB;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.getHttpHeaders;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildJudicialBookingsResponse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildJudicialProfilesResponseV2;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildUserIdList;
import static uk.gov.hmcts.reform.orgrolemapping.v1.V1.Error.UNAUTHORIZED_SERVICE;

@TestPropertySource(properties = {
    "refresh.Job.authorisedServices=" + S2S_ORM + "," + S2S_RARB,
    "refresh.Job.includeJudicialBookings=true",
    "refresh.Job.pageSize=" + TEST_PAGE_SIZE,
    "feign.client.config.jrdClient.v2Active=true"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RefreshControllerRefreshJobIntegrationTest extends BaseTestIntegration {

    private static final Logger logger = LoggerFactory.getLogger(RefreshControllerIntegrationTest.class);

    private final WiremockFixtures wiremockFixtures = new WiremockFixtures();

    private static final String AUTHORISED_JOB_SERVICE = S2S_RARB;
    private static final String UNAUTHORISED_JOB_SERVICE = S2S_CCD_GW;

    private static final String ROLE_NAME_STCW = "senior-tribunal-caseworker";
    private static final String ROLE_NAME_TCW = "tribunal-caseworker";

    private static final String REFRESH_JOB_URL = "/am/role-mapping/refresh";

    public static final int TEST_PAGE_SIZE = 5;

    private MockMvc mockMvc;

    @Inject
    private WebApplicationContext wac;

    @Autowired
    private PersistenceService persistenceService;

    @MockBean
    private CRDFeignClient crdFeignClient;

    @MockBean
    private JRDFeignClient jrdFeignClient;

    @MockBean
    private JBSFeignClient jbsFeignClient;

    @MockBean
    private RASFeignClient rasFeignClient;

    @MockBean
    private RequestMappingService<UserAccessProfile> requestMappingService;

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
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process successful");
        Long jobId = 1L;
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("NEW", refreshJob.getStatus());

        mockCRDService();
        mockRequestMappingServiceWithCaseworkerStatus(HttpStatus.CREATED);

        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
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
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process Aborted");
        Long jobId = 1L;

        mockCRDService();
        mockRequestMappingServiceWithCaseworkerStatus(UNPROCESSABLE_ENTITY);

        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
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
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process Non recoverable retain same state");
        Long jobId = 1L;

        mockCRDService();
        mockRequestMappingServiceWithCaseworkerStatus(UNPROCESSABLE_ENTITY);

        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
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

    //@Disabled("Intermittent AM-2919")
    @Test
    @Order(4)
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToPartialComplete_status422() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process Partial Success");
        Long jobId = 1L;

        mockCRDService();
        mockRequestMappingServiceWithCaseworkerStatus(UNPROCESSABLE_ENTITY);

        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
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
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

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
        mockRequestMappingServiceWithCaseworkerStatus(HttpStatus.CREATED);

        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
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
        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
                        .content(mapper.writeValueAsBytes(IntTestDataBuilder.buildUserRequest())))
                .andExpect(status().is(400))
                .andReturn();
    }

    @Test
    @Order(7)
    public void shouldFailProcessRefreshRoleAssignmentsWithEmptyJobID() throws Exception {
        logger.info(" Refresh Job with optional Users and without mandatory jobId as a param");
        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
                        .param("jobId", ""))
                .andExpect(status().is(400))
                .andReturn();
    }

    @Test
    @Order(8)
    public void shouldFailProcessRefreshRoleAssignmentsWithInvalidJobID() throws Exception {
        logger.info(" Refresh Job with optional Users and without mandatory jobId as a param");
        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
                        .param("jobId", "abc")
                        .content(mapper.writeValueAsBytes(IntTestDataBuilder.buildUserRequest())))
                .andExpect(status().is(400))
                .andReturn();
    }

    @Test
    @Order(9)
    public void shouldFailProcessRefreshRoleAssignmentsWithOutJobID() throws Exception {
        logger.info(" Refresh Job with optional Users and without mandatory jobId as a param");
        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE)))
                .andExpect(status().is(400))
                .andReturn();
    }

    @Test
    @Order(10)
    public void shouldFailProcessRefreshRoleAssignmentsWithInvalidServiceToken() throws Exception {
        logger.info("Refresh request rejected with invalid service token");

        when(securityUtils.getServiceName()).thenReturn(UNAUTHORISED_JOB_SERVICE);

        MvcResult result = mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(UNAUTHORISED_JOB_SERVICE))
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
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process fail");
        Long jobId = 1L;
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("NEW", refreshJob.getStatus());

        doThrow(RuntimeException.class).when(crdFeignClient).getCaseworkerDetailsByServiceName(
                anyString(), anyInt(), anyInt(), anyString(), anyString());

        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
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
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With JobId retry success third time to process successful");
        Long jobId = 1L;
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("NEW", refreshJob.getStatus());

        doThrow(RuntimeException.class).doThrow(RuntimeException.class).doReturn(buildUserProfileResponse())
                .when(crdFeignClient).getCaseworkerDetailsByServiceName(
                        anyString(), anyInt(), anyInt(), anyString(), anyString());
        mockRequestMappingServiceWithCaseworkerStatus(HttpStatus.CREATED);

        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
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

    /*
        IT for JRD refresh job scenarios start from here
     */

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @Order(13)
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete_Judicial(int numberOfBatches) throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process successful");
        RefreshJobEntity refreshJob = createRefreshJobJudicialTargetedUserList(TEST_PAGE_SIZE * numberOfBatches);

        mockJRDService(refreshJob.getUserIds());
        mockJBSService(refreshJob.getUserIds());
        mockRequestMappingServiceWithJudicialStatus(HttpStatus.CREATED);

        UserRequest userRequest = buildUserRequestFromRefreshJob(refreshJob);

        Long jobId = refreshJob.getJobId();
        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
                        .param("jobId", jobId.toString())
                        .content(mapper.writeValueAsBytes(userRequest)))
                .andExpect(status().is(202))
                .andReturn();

        await().timeout(60, TimeUnit.SECONDS).untilAsserted(() -> Assertions.assertTrue(
                isRefreshJobInStatus(jobId, COMPLETED)));

        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(COMPLETED, refreshJob.getStatus());
        assertNotNull(refreshJob.getLog());

        Mockito.verify(jrdFeignClient, Mockito.times(1)).getJudicialDetailsById(any(), any());
        Mockito.verify(jbsFeignClient, Mockito.times(numberOfBatches)).getJudicialBookingByUserIds(any());
    }

    @Test
    @Order(14)
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToAborted_Judicial() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process Aborted");
        RefreshJobEntity refreshJob = createRefreshJobJudicialTargetedUserList(1);

        mockJRDService(refreshJob.getUserIds());
        mockJBSService(refreshJob.getUserIds());
        mockRequestMappingServiceWithJudicialStatus(UNPROCESSABLE_ENTITY);

        UserRequest userRequest = buildUserRequestFromRefreshJob(refreshJob);

        Long jobId = refreshJob.getJobId();
        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
                        .param("jobId", jobId.toString())
                        .content(mapper.writeValueAsBytes(userRequest)))
                .andExpect(status().is(202))
                .andReturn();

        await().timeout(60, TimeUnit.SECONDS).untilAsserted(() -> Assertions.assertTrue(
                isRefreshJobInStatus(jobId, ABORTED)));

        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(ABORTED, refreshJob.getStatus());
        assertNotNull(refreshJob.getUserIds());
        assertThat(refreshJob.getLog(),containsString(String.join(",", refreshJob.getUserIds())));
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

    private void mockJRDService(String[] userIds) {
        ResponseEntity<List<JudicialProfileV2>> userProfilesResponse = buildJudicialProfilesResponseV2(userIds);
        doReturn(userProfilesResponse).when(jrdFeignClient).getJudicialDetailsById(any(), any());
    }

    private void mockJBSService(String[] userIds) {
        doReturn(buildJudicialBookingsResponse(userIds)).when(jbsFeignClient).getJudicialBookingByUserIds(any());
    }

    private void mockRequestMappingServiceWithCaseworkerStatus(HttpStatus status) {
        doReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(ResponseEntity.status(status).body(
                new RoleAssignmentRequestResource(AssignmentRequestBuilder.buildAssignmentRequest(
                        false))))))
                .when(requestMappingService).createCaseworkerAssignments(any());
    }

    private void mockRequestMappingServiceWithJudicialStatus(HttpStatus status) {
        doReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(ResponseEntity.status(status).body(
                new RoleAssignmentRequestResource(AssignmentRequestBuilder.buildAssignmentRequest(
                        false))))))
                .when(requestMappingService).createJudicialAssignments(any(), any());
    }

    private RefreshJobEntity createRefreshJobJudicialTargetedUserList(int numberOfUsers) {
        RefreshJobEntity refreshJob = createRefreshJobJudicial();
        refreshJob.setUserIds(buildUserIdList(numberOfUsers));
        // update LinkedJobId and re-save to allow test of a targeted refresh of judicial users
        refreshJob.setLinkedJobId(refreshJob.getJobId());
        return saveRecordInRefreshJobTable(refreshJob);
    }

    private RefreshJobEntity createRefreshJobJudicial() {
        return saveRecordInRefreshJobTable(RefreshJobEntity.builder()
                .roleCategory(RoleCategory.JUDICIAL.name())
                .jurisdiction("IA")
                .status("NEW")
                .build()
        );
    }

    private RefreshJobEntity getRecordsFromRefreshJobTable(Long jobId) {
        return persistenceService.fetchRefreshJobById(jobId).orElse(null);
    }

    private RefreshJobEntity saveRecordInRefreshJobTable(RefreshJobEntity refreshJobEntity) {
        return persistenceService.persistRefreshJob(refreshJobEntity);
    }

    private UserRequest buildUserRequestFromRefreshJob(RefreshJobEntity refreshJob) {
        return UserRequest.builder()
                .userIds(Arrays.stream(refreshJob.getUserIds()).toList())
                .build();
    }

    private boolean isRefreshJobInStatus(Long jobId, String status) {
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        return refreshJob.getStatus().equals(status);
    }

}
