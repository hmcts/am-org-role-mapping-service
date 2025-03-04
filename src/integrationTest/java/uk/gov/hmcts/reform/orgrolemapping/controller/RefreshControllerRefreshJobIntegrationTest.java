package uk.gov.hmcts.reform.orgrolemapping.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.inject.Inject;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.FeignClientException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.UnauthorizedServiceException;
import uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport.domain.RefreshJob;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfileV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JBSFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.RASFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.ABORTED;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.COMPLETED;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.NEW;
import static uk.gov.hmcts.reform.orgrolemapping.controller.RefreshControllerRefreshJobIntegrationTest.TEST_PAGE_SIZE;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_CCD_GW;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_ORM;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_RARB;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.getHttpHeaders;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshOrchestrator.ERROR_REFRESH_JOB_INVALID_STATE;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshOrchestrator.ERROR_REFRESH_JOB_NOT_FOUND;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildJudicialBookingsResponse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildJudicialProfilesResponseV2;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildUserIdList;
import static uk.gov.hmcts.reform.orgrolemapping.v1.V1.Error.UNAUTHORIZED_SERVICE;

@TestPropertySource(properties = {
    "refresh.Job.authorisedServices=" + S2S_ORM + "," + S2S_RARB,
    "refresh.Job.includeJudicialBookings=true",
    "refresh.Job.pageSize=" + TEST_PAGE_SIZE,
    "refresh.judicial.filterSoftDeletedUsers=true",
    "testing.support.enabled=true" // NB: needed for access to test support URLs
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RefreshControllerRefreshJobIntegrationTest extends BaseTestIntegration {

    private static final Logger logger = LoggerFactory.getLogger(RefreshControllerRefreshJobIntegrationTest.class);

    private final WiremockFixtures wiremockFixtures = new WiremockFixtures();

    private static final String AUTHORISED_JOB_SERVICE = S2S_RARB;
    private static final String UNAUTHORISED_JOB_SERVICE = S2S_CCD_GW;

    private static final String ROLE_NAME_STCW = "senior-tribunal-caseworker";
    private static final String ROLE_NAME_TCW = "tribunal-caseworker";

    private static final String REFRESH_JOB_URL = "/am/role-mapping/refresh";

    // test support URLs: to create and verify RefreshJob records
    private static final String CREATE_REFRESH_JOB_URL = "/am/testing-support/job";
    private static final String GET_REFRESH_JOB_URL = "/am/testing-support/jobs/{jobId}";

    public static final int TEST_PAGE_SIZE = 5;
    public static final int WAIT_FOR_ASYNC_TO_COMPLETE = 5;
    public static final int WAIT_FOR_ASYNC_TO_TIMEOUT = 60;

    private MockMvc mockMvc;

    @Inject
    private WebApplicationContext wac;

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
    private SecurityUtils securityUtils;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Captor
    private ArgumentCaptor<Map<String, Set<UserAccessProfile>>> usersAccessProfilesCaptor;

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
        MockUtils.setSecurityAuthorities(authentication, MockUtils.ROLE_CASEWORKER);
        wiremockFixtures.resetRequests();
    }

    @AfterEach
    public void tearDown() {
        sequential.unlock();
    }

    @Test
    @Order(1)
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process successful");
        Long jobId = createRefreshJobLegalOperations(NEW, null, null);

        mockCRDService();
        mockRequestMappingServiceWithCaseworkerStatus(HttpStatus.CREATED);

        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
                        .param("jobId", jobId.toString()))
                .andExpect(status().is(200))
                .andReturn();

        await().pollDelay(WAIT_FOR_ASYNC_TO_COMPLETE, TimeUnit.SECONDS)
                .timeout(WAIT_FOR_ASYNC_TO_TIMEOUT, TimeUnit.SECONDS)
                .untilAsserted(() -> Assertions.assertTrue(isRefreshJobInStatus(jobId, COMPLETED)));

        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
        assertEquals(COMPLETED, refreshJob.getStatus());
        assertNull(refreshJob.getUserIds());
        assertNotNull(refreshJob.getLog());
    }

    @Test
    @Order(2)
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToAborted() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process Aborted");
        Long jobId = createRefreshJobLegalOperations(NEW, null, null);

        mockCRDService();
        mockRequestMappingServiceWithCaseworkerStatus(UNPROCESSABLE_ENTITY);

        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
                        .param("jobId", jobId.toString()))
                .andExpect(status().is(200))
                .andReturn();

        await().pollDelay(WAIT_FOR_ASYNC_TO_COMPLETE, TimeUnit.SECONDS)
                .timeout(WAIT_FOR_ASYNC_TO_TIMEOUT, TimeUnit.SECONDS)
                .untilAsserted(() -> Assertions.assertTrue(isRefreshJobInStatus(jobId, ABORTED)));

        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
        assertEquals(ABORTED, refreshJob.getStatus());
        assertNotNull(refreshJob.getUserIds());
        assertThat(refreshJob.getLog(), containsString(String.join(",", refreshJob.getUserIds())));
    }

    @Test
    @Order(3)
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToAborted_status422() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process Non recoverable retain same state");
        Long jobId = createRefreshJobLegalOperations(NEW, null, null);

        mockCRDService();
        mockRequestMappingServiceWithCaseworkerStatus(UNPROCESSABLE_ENTITY);

        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
                        .param("jobId", jobId.toString()))
                .andExpect(status().is(200))
                .andReturn();

        await().pollDelay(WAIT_FOR_ASYNC_TO_COMPLETE, TimeUnit.SECONDS)
                .timeout(WAIT_FOR_ASYNC_TO_TIMEOUT, TimeUnit.SECONDS)
                .untilAsserted(() -> Assertions.assertTrue(isRefreshJobInStatus(jobId, ABORTED)));

        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
        logger.info(" -- Refresh Role Assignment record updated -- " + refreshJob.getStatus());
        assertEquals(ABORTED, refreshJob.getStatus());
        assertNotNull(refreshJob.getUserIds());
        assertThat(refreshJob.getLog(), containsString(String.join(",", refreshJob.getUserIds())));
    }

    @Test
    @Order(4)
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToPartialComplete_status422() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process Partial Success");
        Long jobId = createRefreshJobLegalOperations(NEW, null, null);

        mockCRDService();
        mockRequestMappingServiceWithCaseworkerStatus(UNPROCESSABLE_ENTITY);

        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
                        .param("jobId", jobId.toString()))
                .andExpect(status().is(200))
                .andReturn();

        await().pollDelay(WAIT_FOR_ASYNC_TO_COMPLETE, TimeUnit.SECONDS)
                .timeout(WAIT_FOR_ASYNC_TO_TIMEOUT, TimeUnit.SECONDS)
                .untilAsserted(() -> Assertions.assertTrue(isRefreshJobInStatus(jobId, ABORTED)));

        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
        assertEquals(ABORTED, refreshJob.getStatus());
        assertNotNull(refreshJob.getUserIds());
        assertThat(refreshJob.getLog(), containsString(String.join(",", refreshJob.getUserIds())));
    }

    @Test
    @Order(5)
    public void shouldProcessRefreshRoleAssignmentsWithFailedUsersToComplete() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With JobId and failed UserIds to process successful");
        Long jobIdAborted = createRefreshJobLegalOperations(ABORTED, null, buildUserIdList(1));
        Long jobId = createRefreshJobLegalOperations(NEW, jobIdAborted, null);

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
                .andExpect(status().is(200))
                .andReturn();

        await().pollDelay(WAIT_FOR_ASYNC_TO_COMPLETE, TimeUnit.SECONDS)
                .timeout(WAIT_FOR_ASYNC_TO_TIMEOUT, TimeUnit.SECONDS)
                .untilAsserted(() -> Assertions.assertTrue(isRefreshJobInStatus(jobId, COMPLETED)));

        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
        assertEquals(COMPLETED, refreshJob.getStatus());
        assertNull(refreshJob.getUserIds());
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
                .andExpect(status().is(400)) // param not present
                .andReturn();
    }

    @Test
    @Order(7)
    public void shouldFailProcessRefreshRoleAssignmentsWithEmptyJobID() throws Exception {
        logger.info(" Refresh Job without optional Users and with empty jobId as a param");
        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
                        .param("jobId", ""))
                .andExpect(status().is(400)) // param converts to null
                .andReturn();
    }

    @Test
    @Order(8)
    public void shouldFailProcessRefreshRoleAssignmentsWithInvalidJobID() throws Exception {
        logger.info(" Refresh Job with optional Users and with invalid jobId as a param");
        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
                        .param("jobId", "abc")
                        .content(mapper.writeValueAsBytes(IntTestDataBuilder.buildUserRequest())))
                .andExpect(status().is(400)) // param conversion failed
                .andReturn();
    }

    @Test
    @Order(9)
    public void shouldFailProcessRefreshRoleAssignmentsWithOutJobID() throws Exception {
        logger.info(" Refresh Job without optional Users and without mandatory jobId as a param");
        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE)))
                .andExpect(status().is(400)) // param not present
                .andReturn();
    }

    @Test
    @Order(10)
    public void shouldFailProcessRefreshRoleAssignmentsWithJobIDNotFound() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" Refresh Job when job ID does not exist");
        MvcResult result = mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
                        .param("jobId", "9999")) // i.e. job-id that does not exist
                .andExpect(status().is(422))
                .andReturn();

        var contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(ERROR_REFRESH_JOB_NOT_FOUND));
    }

    @Test
    @Order(11)
    public void shouldFailProcessRefreshRoleAssignmentsWithJobInvalidState() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        Long jobIdAborted = createRefreshJobLegalOperations(ABORTED, null, null);

        logger.info(" Refresh Job when job is in an invalid state");
        MvcResult result = mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
                        .param("jobId", jobIdAborted.toString()))
                .andExpect(status().is(422))
                .andReturn();

        var contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(ERROR_REFRESH_JOB_INVALID_STATE));
    }

    @Test
    @Order(12)
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
    @Order(13)
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete_retryFail() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process fail");
        Long jobId = createRefreshJobLegalOperations(NEW, null, null);

        doThrow(FeignClientException.class).when(crdFeignClient).getCaseworkerDetailsByServiceName(
                anyString(), anyInt(), anyInt(), anyString(), anyString());

        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
                        .param("jobId", jobId.toString()))
                .andExpect(status().is(400));
        await().pollDelay(WAIT_FOR_ASYNC_TO_COMPLETE, TimeUnit.SECONDS)
                .timeout(WAIT_FOR_ASYNC_TO_TIMEOUT, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(crdFeignClient, times(3))
                                .getCaseworkerDetailsByServiceName(any(), any(), any(), any(), any())
        );

        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
        assertEquals("NEW", refreshJob.getStatus());// failed process should change the status to IN-PROGRESS
    }

    @Test
    @Order(14)
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete_CRDRetry() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With JobId retry success third time to process successful");
        Long jobId = createRefreshJobLegalOperations(NEW, null, null);

        doThrow(RuntimeException.class).doThrow(RuntimeException.class).doReturn(buildUserProfileResponse())
                .when(crdFeignClient).getCaseworkerDetailsByServiceName(
                        anyString(), anyInt(), anyInt(), anyString(), anyString());
        mockRequestMappingServiceWithCaseworkerStatus(HttpStatus.CREATED);

        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
                        .param("jobId", jobId.toString()))
                .andExpect(status().is(200))
                .andReturn();

        await().pollDelay(WAIT_FOR_ASYNC_TO_COMPLETE, TimeUnit.SECONDS)
                .timeout(WAIT_FOR_ASYNC_TO_TIMEOUT, TimeUnit.SECONDS)
                .untilAsserted(() -> Assertions.assertTrue(isRefreshJobInStatus(jobId, COMPLETED)));

        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
        assertEquals(COMPLETED, refreshJob.getStatus());
        assertNull(refreshJob.getUserIds());
        assertNotNull(refreshJob.getLog());
    }

    /*
        IT for JRD refresh job scenarios start from here
     */
    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @Order(15)
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete_Judicial(int numberOfBatches) throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process successful");
        String[] userIds = buildUserIdList(TEST_PAGE_SIZE * numberOfBatches);

        mockJRDService(userIds);
        mockJBSService(userIds);
        mockRequestMappingServiceWithJudicialStatus(HttpStatus.CREATED);

        Long jobId = createRefreshJobJudicialTargetedUserList(userIds);
        UserRequest userRequest = buildUserRequestWithUserIds(userIds);

        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
                        .param("jobId", jobId.toString())
                        .content(mapper.writeValueAsBytes(userRequest)))
                .andExpect(status().is(200))
                .andReturn();

        await().pollDelay(WAIT_FOR_ASYNC_TO_COMPLETE, TimeUnit.SECONDS)
                .timeout(WAIT_FOR_ASYNC_TO_TIMEOUT, TimeUnit.SECONDS)
                .untilAsserted(() -> Assertions.assertTrue(isRefreshJobInStatus(jobId, COMPLETED)));

        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
        assertEquals(COMPLETED, refreshJob.getStatus());
        assertNotNull(refreshJob.getLog());

        Mockito.verify(jrdFeignClient, times(1)).getJudicialDetailsById(any(), any());
        Mockito.verify(jbsFeignClient, times(numberOfBatches)).getJudicialBookingByUserIds(any());
    }

    @Test
    @Order(16)
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToAborted_Judicial() throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record With Only JobId to process Aborted");
        String[] userIds = buildUserIdList(TEST_PAGE_SIZE);

        mockJRDService(userIds);
        mockJBSService(userIds);
        mockRequestMappingServiceWithJudicialStatus(UNPROCESSABLE_ENTITY);

        Long jobId = createRefreshJobJudicialTargetedUserList(userIds);
        UserRequest userRequest = buildUserRequestWithUserIds(userIds);

        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
                        .param("jobId", jobId.toString())
                        .content(mapper.writeValueAsBytes(userRequest)))
                .andExpect(status().is(200))
                .andReturn();

        await().pollDelay(WAIT_FOR_ASYNC_TO_COMPLETE, TimeUnit.SECONDS)
                .timeout(WAIT_FOR_ASYNC_TO_TIMEOUT, TimeUnit.SECONDS)
                .untilAsserted(() -> Assertions.assertTrue(isRefreshJobInStatus(jobId, ABORTED)));

        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
        assertEquals(ABORTED, refreshJob.getStatus());
        assertNotNull(refreshJob.getUserIds());
        assertThat(refreshJob.getLog(), containsString(String.join(",", refreshJob.getUserIds())));
    }

    @Order(17)
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void shouldProcessRefreshRoleAssignments_deletedFlag(Boolean deletedFlagStatus) throws Exception {
        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);

        logger.info(" RefreshJob record with judicial user deleted flag {}", deletedFlagStatus);
        String[] userIds = buildUserIdList(1);

        ResponseEntity<List<JudicialProfileV2>> res = buildJudicialProfilesResponseV2(userIds);
        res.getBody().get(0).setDeletedFlag(deletedFlagStatus.toString());
        doReturn(res).when(jrdFeignClient).getJudicialDetailsById(any(), any());

        mockJBSService(userIds);
        mockRequestMappingServiceWithJudicialStatus(HttpStatus.CREATED);

        Long jobId = createRefreshJobJudicialTargetedUserList(userIds);
        UserRequest userRequest = buildUserRequestWithUserIds(userIds);

        mockMvc.perform(post(REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
                        .param("jobId", jobId.toString())
                        .content(mapper.writeValueAsBytes(userRequest)))
                .andExpect(status().is(200))
                .andReturn();

        await().pollDelay(WAIT_FOR_ASYNC_TO_COMPLETE, TimeUnit.SECONDS)
                .timeout(WAIT_FOR_ASYNC_TO_TIMEOUT, TimeUnit.SECONDS)
                .untilAsserted(() -> Assertions.assertTrue(isRefreshJobInStatus(jobId, COMPLETED)));

        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
        assertEquals(COMPLETED, refreshJob.getStatus());
        assertNotNull(refreshJob.getLog());

        verify(jrdFeignClient, times(1)).getJudicialDetailsById(any(), any());
        verify(jbsFeignClient, deletedFlagStatus ? times(0) : times(1)).getJudicialBookingByUserIds(any());
        verify(requestMappingService, times(1)).createJudicialAssignments(usersAccessProfilesCaptor.capture(), any());

        Map<String, Set<UserAccessProfile>> usersAccessProfiles = usersAccessProfilesCaptor.getValue();
        assertEquals(deletedFlagStatus, usersAccessProfiles.get(userIds[0]).isEmpty());
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

    private UserRequest buildUserRequestWithUserIds(String[] userIds) {
        return UserRequest.builder()
                .userIds(Arrays.stream(userIds).toList())
                .build();
    }

    public Long createRefreshJobJudicialTargetedUserList(String[] userIds) throws Exception {
        return callTestSupportCreateJobApi(RoleCategory.JUDICIAL, NEW, true, null, userIds);
    }

    private Long createRefreshJobLegalOperations(String status,
                                                 Long linkedJobId,
                                                 String[] userIds) throws Exception {
        return callTestSupportCreateJobApi(RoleCategory.LEGAL_OPERATIONS, status, false, linkedJobId, userIds);
    }

    private Long callTestSupportCreateJobApi(RoleCategory roleCategory,
                                             String status,
                                             Boolean linkJob,
                                             Long linkedJobId,
                                             String[] userIds) throws Exception {

        MvcResult result = mockMvc.perform(post(CREATE_REFRESH_JOB_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(S2S_ORM))
                        .param("roleCategory", roleCategory.name())
                        .param("jurisdiction", "IA")
                        .param("linkJob", linkJob.toString())
                        .param("linkedJobId", linkedJobId != null ? linkedJobId.toString() : null)
                        .param("status", status)
                        .content(createUserRequestContent(userIds)))
                .andExpect(status().is(201))
                .andReturn();

        // verify response is as expected
        RefreshJob refreshJob = mapper.readValue(result.getResponse().getContentAsString(), RefreshJob.class);
        assertNotNull(refreshJob);

        // check jobId is set
        Long jobId = refreshJob.getJobId();
        assertNotNull(jobId);
        assertEquals(status, refreshJob.getStatus());

        // check linkedJobId is as expected
        if (BooleanUtils.isTrue(linkJob)) {
            assertEquals(jobId, refreshJob.getLinkedJobId());
        } else if (linkedJobId != null) {
            assertEquals(linkedJobId, refreshJob.getLinkedJobId());
        } else {
            assertTrue(refreshJob.getLinkedJobId() == null || refreshJob.getLinkedJobId() == 0);
        }

        // check userIds are as expected
        if (userIds != null) {
            assertNotNull(refreshJob.getUserIds());
            assertEquals(Array.getLength(userIds), Array.getLength(refreshJob.getUserIds()));
            assertTrue(Arrays.stream(userIds).toList().containsAll(Arrays.asList(refreshJob.getUserIds())));
        } else {
            assertTrue(ArrayUtils.isEmpty(refreshJob.getUserIds()));
        }

        return jobId;
    }

    private RefreshJob callTestSupportGetJobApi(Long jobId) throws Exception {

        MvcResult result = mockMvc.perform(get(GET_REFRESH_JOB_URL, jobId.toString())
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(S2S_ORM)))
                .andExpect(status().is(200))
                .andReturn();

        return mapper.readValue(result.getResponse().getContentAsString(), RefreshJob.class);
    }

    private boolean isRefreshJobInStatus(Long jobId, String status) throws Exception {
        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
        return refreshJob.getStatus().equals(status);
    }

    private byte[] createUserRequestContent(String[] userIds) throws JsonProcessingException {
        if (userIds == null) {
            return null;
        }

        return mapper.writeValueAsBytes(UserRequest.builder()
                .userIds(Arrays.stream(userIds).toList())
                .build());
    }

}
