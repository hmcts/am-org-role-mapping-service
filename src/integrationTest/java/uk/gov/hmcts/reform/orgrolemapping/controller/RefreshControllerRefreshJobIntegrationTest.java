package uk.gov.hmcts.reform.orgrolemapping.controller;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBookingResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.ABORTED;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.COMPLETED;
import static uk.gov.hmcts.reform.orgrolemapping.controller.RefreshControllerRefreshJobIntegrationTest.TEST_PAGE_SIZE;
//import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_CCD_GW;
//import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_ORM;
//import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_RARB;
//import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.getHttpHeaders;
//import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildJudicialBookingsResponse;
//import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildJudicialProfilesResponseV2;
//import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildUserIdList;
import static uk.gov.hmcts.reform.orgrolemapping.v1.V1.Error.UNAUTHORIZED_SERVICE;

@TestPropertySource(properties = {
        "refresh.Job.includeJudicialBookings=true",
        "refresh.Job.pageSize=" + TEST_PAGE_SIZE,
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RefreshControllerRefreshJobIntegrationTest extends BaseTestIntegration {

    private static final Logger logger = LoggerFactory.getLogger(RefreshControllerRefreshJobIntegrationTest.class);

    private final WiremockFixtures wiremockFixtures = new WiremockFixtures();

    private static final String AUTHORISED_JOB_SERVICE =  "am_role_assignment_refresh_batch";
    private static final String UNAUTHORISED_JOB_SERVICE = "ccd_gw";

    private static final String AUTHORISED_SERVICE = "am_role_assignment_refresh_batch";

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
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process successful");
        RefreshJobEntity refreshJob = createRefreshJobLegalOperations();
        Long jobId = refreshJob.getJobId();

        mockCRDService();
        mockRequestMappingServiceWithCaseworkerStatus(HttpStatus.CREATED);

        mockMvc.perform(post(REFRESH_JOB_URL)
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
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToAborted() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process Aborted");
        RefreshJobEntity refreshJob = createRefreshJobLegalOperations();
        Long jobId = refreshJob.getJobId();

        mockCRDService();
        mockRequestMappingServiceWithCaseworkerStatus(UNPROCESSABLE_ENTITY);

        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .param("jobId", jobId.toString()))
                .andExpect(status().is(202))
                .andReturn();

        await().timeout(60, TimeUnit.SECONDS).untilAsserted(() -> Assertions.assertTrue(
                isRefreshJobInStatus(jobId, ABORTED)));

        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJobEntity afterRefreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(ABORTED, afterRefreshJob.getStatus());
        assertNotNull(afterRefreshJob.getUserIds());
        assertThat(afterRefreshJob.getLog(),containsString(String.join(",", afterRefreshJob.getUserIds())));
    }

    @Test
    @Order(3)
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToAborted_status422() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process Non recoverable retain same state");
        RefreshJobEntity refreshJob = createRefreshJobLegalOperations();
        Long jobId = refreshJob.getJobId();

        mockCRDService();
        mockRequestMappingServiceWithCaseworkerStatus(UNPROCESSABLE_ENTITY);

        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .param("jobId", jobId.toString()))
                .andExpect(status().is(202))
                .andReturn();

        await().timeout(60, TimeUnit.SECONDS).untilAsserted(() -> Assertions.assertTrue(
                isRefreshJobInStatus(jobId, ABORTED)));

        RefreshJobEntity afterRefreshJob = getRecordsFromRefreshJobTable(jobId);
        logger.info(" -- Refresh Role Assignment record updated -- " + afterRefreshJob.getStatus());
        assertEquals("ABORTED", afterRefreshJob.getStatus());
        assertNotNull(afterRefreshJob.getUserIds());
        assertThat(afterRefreshJob.getLog(),containsString(String.join(",", afterRefreshJob.getUserIds())));
    }

    @Test
    @Order(4)
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToPartialComplete_status422() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process Partial Success");
        RefreshJobEntity refreshJob = createRefreshJobLegalOperations();
        Long jobId = refreshJob.getJobId();

        mockCRDService();
        mockRequestMappingServiceWithCaseworkerStatus(UNPROCESSABLE_ENTITY);

        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .param("jobId", jobId.toString()))
                .andExpect(status().is(202))
                .andReturn();

        await().timeout(60, TimeUnit.SECONDS).untilAsserted(() -> Assertions.assertTrue(
                isRefreshJobInStatus(jobId, ABORTED)));

        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJobEntity afterRefreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals(ABORTED, afterRefreshJob.getStatus());
        assertNotNull(afterRefreshJob.getUserIds());
        assertThat(afterRefreshJob.getLog(), containsString(String.join(",", afterRefreshJob.getUserIds())));
    }

    @Test
    @Order(5)
    public void shouldProcessRefreshRoleAssignmentsWithFailedUsersToComplete() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With JobId and failed UserIds to process successful");
        //Long jobId = 3L;
        RefreshJobEntity refreshJobAborted = createRefreshJobLegalOperations();
        Long jobIdAborted = refreshJobAborted.getJobId();
        refreshJobAborted.setStatus(ABORTED);
        refreshJobAborted.setUserIds(buildUserIdList(1));

        RefreshJobEntity refreshJob = createRefreshJobLegalOperations();
        refreshJob.setLinkedJobId(jobIdAborted);
        Long jobId = refreshJob.getJobId();

        doReturn(new ResponseEntity<>(IntTestDataBuilder
                .buildListOfUserProfiles(false, false, "1", "2",
                        ROLE_NAME_STCW, ROLE_NAME_TCW,
                        true, true, false,
                        true, "BFA1", "BFA2",
                        false), HttpStatus.OK)).when(crdFeignClient).getCaseworkerDetailsById(any());
        mockRequestMappingServiceWithCaseworkerStatus(HttpStatus.CREATED);

        mockMvc.perform(post(REFRESH_JOB_URL)
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
        mockMvc.perform(post(REFRESH_JOB_URL)
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
        mockMvc.perform(post(REFRESH_JOB_URL)
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
        mockMvc.perform(post(REFRESH_JOB_URL)
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
        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders()))
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
                        .headers(getHttpHeaders())
                        .param("jobId", String.valueOf(1L)))
                .andExpect(status().is(403))
                .andReturn();

        assertTrue(result.getResolvedException() instanceof UnauthorizedServiceException);
        assertThat(result.getResolvedException().getMessage(), equalTo(UNAUTHORIZED_SERVICE));
    }

    @Test
    @Order(11)
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete_retryFail() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process fail");
        RefreshJobEntity refreshJob = createRefreshJobLegalOperations();
        Long jobId = refreshJob.getJobId();

        doThrow(RuntimeException.class).when(crdFeignClient).getCaseworkerDetailsByServiceName(
                anyString(), anyInt(), anyInt(), anyString(), anyString());

        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders())
                        .param("jobId", jobId.toString()))
                .andExpect(status().is(202))
                .andReturn();

        await().timeout(120, TimeUnit.SECONDS).untilAsserted(() -> verify(crdFeignClient,
                times(3)).getCaseworkerDetailsByServiceName(any(), any(), any(), any(), any()));

        refreshJob = getRecordsFromRefreshJobTable(jobId);
        assertEquals("NEW", refreshJob.getStatus());// failed process should change the status to IN-PROGRESS
    }

    @Test
    @Order(12)
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete_CRDRetry() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With JobId retry success third time to process successful");
        RefreshJobEntity refreshJob = createRefreshJobLegalOperations();
        Long jobId = refreshJob.getJobId();

        doThrow(RuntimeException.class).doThrow(RuntimeException.class).doReturn(buildUserProfileResponse())
                .when(crdFeignClient).getCaseworkerDetailsByServiceName(
                        anyString(), anyInt(), anyInt(), anyString(), anyString());
        mockRequestMappingServiceWithCaseworkerStatus(HttpStatus.CREATED);

        mockMvc.perform(post(REFRESH_JOB_URL)
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

    private void mockRequestMappingServiceWithCaseworkerStatus(HttpStatus status) {
        doReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(ResponseEntity.status(status).body(
                new RoleAssignmentRequestResource(AssignmentRequestBuilder.buildAssignmentRequest(
                        false))))))
                .when(requestMappingService).createCaseworkerAssignments(any());
    }

    private RefreshJobEntity createRefreshJobJudicial() {
        return saveRecordInRefreshJobTable(RefreshJobEntity.builder()
                .roleCategory(RoleCategory.JUDICIAL.name())
                .jurisdiction("IA")
                .status("NEW")
                .build()
        );
    }

    private RefreshJobEntity createRefreshJobLegalOperations() {
        return saveRecordInRefreshJobTable(RefreshJobEntity.builder()
                .roleCategory(RoleCategory.LEGAL_OPERATIONS.name())
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

    private boolean isRefreshJobInStatus(Long jobId, String status) {
        RefreshJobEntity refreshJob = getRecordsFromRefreshJobTable(jobId);
        return refreshJob.getStatus().equals(status);
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

    public static String[] buildUserIdList(int size) {
        String[] ids = new String[size];
        for (int i = 0; i < size; i++) {
            ids[i] = generateUniqueId();
        }
        return ids;
    }

    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }
    
}