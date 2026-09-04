package uk.gov.hmcts.reform.orgrolemapping.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport.domain.RefreshJob;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JBSFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.RASFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.COMPLETED;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.NEW;
import static uk.gov.hmcts.reform.orgrolemapping.controller.RefreshControllerRefreshJobIntegrationTest.TEST_PAGE_SIZE;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_CCD_GW;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_ORM;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_RARB;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures.ACTOR_ID1;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures.OBJECT_MAPPER;

@TestPropertySource(properties = {
    "refresh.Job.authorisedServices=" + S2S_ORM + "," + S2S_RARB,
    "refresh.Job.includeJudicialBookings=true",
    "refresh.Job.pageSize=" + TEST_PAGE_SIZE,
    "refresh.judicial.filterSoftDeletedUsers=true",
    "testing.support.enabled=true" // NB: needed for access to test support URLs
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RefreshControllerRefreshJobIntegrationTest extends BaseAuthorisedTestIntegration {

    private static final Logger logger = LoggerFactory.getLogger(RefreshControllerRefreshJobIntegrationTest.class);

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

    @Inject
    private WebApplicationContext wac;

    @MockBean
    private PRDFeignClient prdFeignClient;

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

    @Captor
    private ArgumentCaptor<Map<String, Set<UserAccessProfile>>> usersAccessProfilesCaptor;

    Lock sequential = new ReentrantLock();

    @Test
    @Order(1)
    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete() throws Exception {
        logger.info(" RefreshJob record With Only JobId to process successful");
        Long jobId = createRefreshJobLegalOperations(NEW, null, null);

        mockCRDService();
        mockRequestMappingServiceWithCaseworkerStatus(HttpStatus.CREATED);

        getRequestSpecification(AUTHORISED_JOB_SERVICE, ACTOR_ID1)
                .when().post(REFRESH_JOB_URL + "?jobId=" + jobId.toString())
                .then().assertThat()
                .statusCode(HttpStatus.ACCEPTED.value());

        await().pollDelay(WAIT_FOR_ASYNC_TO_COMPLETE, TimeUnit.SECONDS)
                .timeout(WAIT_FOR_ASYNC_TO_TIMEOUT, TimeUnit.SECONDS)
                .untilAsserted(() -> Assertions.assertTrue(isRefreshJobInStatus(jobId, COMPLETED)));

        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
        assertEquals(COMPLETED, refreshJob.getStatus());
        assertNull(refreshJob.getUserIds());
        assertNotNull(refreshJob.getLog());
    }

    //    @Test
    //    @Order(2)
    //    public void shouldProcessRefreshRoleAssignmentsWithJobIdToAborted() throws Exception {
    //        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);
    //
    //        logger.info(" RefreshJob record With Only JobId to process Aborted");
    //        Long jobId = createRefreshJobLegalOperations(NEW, null, null);
    //
    //        mockCRDService();
    //        mockRequestMappingServiceWithCaseworkerStatus(UNPROCESSABLE_ENTITY);
    //
    //        mockMvc.perform(post(REFRESH_JOB_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
    //                        .param("jobId", jobId.toString()))
    //                .andExpect(status().is(202))
    //                .andReturn();
    //
    //        await().pollDelay(WAIT_FOR_ASYNC_TO_COMPLETE, TimeUnit.SECONDS)
    //                .timeout(WAIT_FOR_ASYNC_TO_TIMEOUT, TimeUnit.SECONDS)
    //                .untilAsserted(() -> Assertions.assertTrue(isRefreshJobInStatus(jobId, ABORTED)));
    //
    //        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
    //        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
    //        assertEquals(ABORTED, refreshJob.getStatus());
    //        assertNotNull(refreshJob.getUserIds());
    //        assertThat(refreshJob.getLog(), containsString(String.join(",", refreshJob.getUserIds())));
    //    }
    //
    //    @Test
    //    @Order(3)
    //    public void shouldProcessRefreshRoleAssignmentsWithJobIdToAborted_status422() throws Exception {
    //        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);
    //
    //        logger.info(" RefreshJob record With Only JobId to process Non recoverable retain same state");
    //        Long jobId = createRefreshJobLegalOperations(NEW, null, null);
    //
    //        mockCRDService();
    //        mockRequestMappingServiceWithCaseworkerStatus(UNPROCESSABLE_ENTITY);
    //
    //        mockMvc.perform(post(REFRESH_JOB_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
    //                        .param("jobId", jobId.toString()))
    //                .andExpect(status().is(202))
    //                .andReturn();
    //
    //        await().pollDelay(WAIT_FOR_ASYNC_TO_COMPLETE, TimeUnit.SECONDS)
    //                .timeout(WAIT_FOR_ASYNC_TO_TIMEOUT, TimeUnit.SECONDS)
    //                .untilAsserted(() -> Assertions.assertTrue(isRefreshJobInStatus(jobId, ABORTED)));
    //
    //        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
    //        logger.info(" -- Refresh Role Assignment record updated -- " + refreshJob.getStatus());
    //        assertEquals(ABORTED, refreshJob.getStatus());
    //        assertNotNull(refreshJob.getUserIds());
    //        assertThat(refreshJob.getLog(), containsString(String.join(",", refreshJob.getUserIds())));
    //    }
    //
    //    @Test
    //    @Order(4)
    //    public void shouldProcessRefreshRoleAssignmentsWithJobIdToPartialComplete_status422() throws Exception {
    //        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);
    //
    //        logger.info(" RefreshJob record With Only JobId to process Partial Success");
    //        Long jobId = createRefreshJobLegalOperations(NEW, null, null);
    //
    //        mockCRDService();
    //        mockRequestMappingServiceWithCaseworkerStatus(UNPROCESSABLE_ENTITY);
    //
    //        mockMvc.perform(post(REFRESH_JOB_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
    //                        .param("jobId", jobId.toString()))
    //                .andExpect(status().is(202))
    //                .andReturn();
    //
    //        await().pollDelay(WAIT_FOR_ASYNC_TO_COMPLETE, TimeUnit.SECONDS)
    //                .timeout(WAIT_FOR_ASYNC_TO_TIMEOUT, TimeUnit.SECONDS)
    //                .untilAsserted(() -> Assertions.assertTrue(isRefreshJobInStatus(jobId, ABORTED)));
    //
    //        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
    //        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
    //        assertEquals(ABORTED, refreshJob.getStatus());
    //        assertNotNull(refreshJob.getUserIds());
    //        assertThat(refreshJob.getLog(), containsString(String.join(",", refreshJob.getUserIds())));
    //    }
    //
    //    @Test
    //    @Order(5)
    //    public void shouldProcessRefreshRoleAssignmentsWithFailedUsersToComplete() throws Exception {
    //        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);
    //
    //        logger.info(" RefreshJob record With JobId and failed UserIds to process successful");
    //        Long jobIdAborted = createRefreshJobLegalOperations(ABORTED, null, buildUserIdList(1));
    //        Long jobId = createRefreshJobLegalOperations(NEW, jobIdAborted, null);
    //
    //        doReturn(new ResponseEntity<>(IntTestDataBuilder
    //                .buildListOfUserProfiles(false, false, "1", "2",
    //                        ROLE_NAME_STCW, ROLE_NAME_TCW,
    //                        true, true, false,
    //                        true, "BFA1", "BFA2",
    //                        false), HttpStatus.OK)).when(crdFeignClient).getCaseworkerDetailsById(any());
    //        mockRequestMappingServiceWithCaseworkerStatus(HttpStatus.CREATED);
    //
    //        mockMvc.perform(post(REFRESH_JOB_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
    //                        .content(mapper.writeValueAsBytes(IntTestDataBuilder.buildUserRequest()))
    //                        .param("jobId", jobId.toString()))
    //                .andExpect(status().is(202))
    //                .andReturn();
    //
    //        await().pollDelay(WAIT_FOR_ASYNC_TO_COMPLETE, TimeUnit.SECONDS)
    //                .timeout(WAIT_FOR_ASYNC_TO_TIMEOUT, TimeUnit.SECONDS)
    //                .untilAsserted(() -> Assertions.assertTrue(isRefreshJobInStatus(jobId, COMPLETED)));
    //
    //        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
    //        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
    //        assertEquals(COMPLETED, refreshJob.getStatus());
    //        assertNull(refreshJob.getUserIds());
    //        assertNotNull(refreshJob.getLog());
    //    }
    //
    //    @Test
    //    @Order(6)
    //    public void shouldFailProcessRefreshRoleAssignmentsWithFailedUsersAndWithOutJobID() throws Exception {
    //        logger.info(" Refresh Job with optional Users and without mandatory jobId as a param");
    //        mockMvc.perform(post(REFRESH_JOB_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
    //                        .content(mapper.writeValueAsBytes(IntTestDataBuilder.buildUserRequest())))
    //                .andExpect(status().is(400)) // param not present
    //                .andReturn();
    //    }
    //
    //    @Test
    //    @Order(7)
    //    public void shouldFailProcessRefreshRoleAssignmentsWithEmptyJobID() throws Exception {
    //        logger.info(" Refresh Job without optional Users and with empty jobId as a param");
    //        mockMvc.perform(post(REFRESH_JOB_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
    //                        .param("jobId", ""))
    //                .andExpect(status().is(400)) // param converts to null
    //                .andReturn();
    //    }
    //
    //    @Test
    //    @Order(8)
    //    public void shouldFailProcessRefreshRoleAssignmentsWithInvalidJobID() throws Exception {
    //        logger.info(" Refresh Job with optional Users and with invalid jobId as a param");
    //        mockMvc.perform(post(REFRESH_JOB_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
    //                        .param("jobId", "abc")
    //                        .content(mapper.writeValueAsBytes(IntTestDataBuilder.buildUserRequest())))
    //                .andExpect(status().is(400)) // param conversion failed
    //                .andReturn();
    //    }
    //
    //    @Test
    //    @Order(9)
    //    public void shouldFailProcessRefreshRoleAssignmentsWithOutJobID() throws Exception {
    //        logger.info(" Refresh Job without optional Users and without mandatory jobId as a param");
    //        mockMvc.perform(post(REFRESH_JOB_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE)))
    //                .andExpect(status().is(400)) // param not present
    //                .andReturn();
    //    }
    //
    //    @Test
    //    @Order(10)
    //    public void shouldFailProcessRefreshRoleAssignmentsWithJobIDNotFound() throws Exception {
    //        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);
    //
    //        logger.info(" Refresh Job when job ID does not exist");
    //        MvcResult result = mockMvc.perform(post(REFRESH_JOB_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
    //                        .param("jobId", "9999")) // i.e. job-id that does not exist
    //                .andExpect(status().is(422))
    //                .andReturn();
    //
    //        var contentAsString = result.getResponse().getContentAsString();
    //        assertTrue(contentAsString.contains(ERROR_REFRESH_JOB_NOT_FOUND));
    //    }
    //
    //    @Test
    //    @Order(11)
    //    public void shouldFailProcessRefreshRoleAssignmentsWithJobInvalidState() throws Exception {
    //        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);
    //
    //        Long jobIdAborted = createRefreshJobLegalOperations(ABORTED, null, null);
    //
    //        logger.info(" Refresh Job when job is in an invalid state");
    //        MvcResult result = mockMvc.perform(post(REFRESH_JOB_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
    //                        .param("jobId", jobIdAborted.toString()))
    //                .andExpect(status().is(422))
    //                .andReturn();
    //
    //        var contentAsString = result.getResponse().getContentAsString();
    //        assertTrue(contentAsString.contains(ERROR_REFRESH_JOB_INVALID_STATE));
    //    }
    //
    //    @Test
    //    @Order(12)
    //    public void shouldFailProcessRefreshRoleAssignmentsWithInvalidServiceToken() throws Exception {
    //        logger.info("Refresh request rejected with invalid service token");
    //
    //        when(securityUtils.getServiceName()).thenReturn(UNAUTHORISED_JOB_SERVICE);
    //
    //        MvcResult result = mockMvc.perform(post(REFRESH_JOB_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(UNAUTHORISED_JOB_SERVICE))
    //                        .param("jobId", String.valueOf(1L)))
    //                .andExpect(status().is(403))
    //                .andReturn();
    //
    //        assertTrue(result.getResolvedException() instanceof UnauthorizedServiceException);
    //        assertThat(result.getResolvedException().getMessage(), equalTo(UNAUTHORIZED_SERVICE));
    //    }
    //
    //    @Test
    //    @Order(13)
    //    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete_retryFail() throws Exception {
    //        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);
    //
    //        logger.info(" RefreshJob record With Only JobId to process fail");
    //        Long jobId = createRefreshJobLegalOperations(NEW, null, null);
    //
    //        doThrow(RuntimeException.class).when(crdFeignClient).getCaseworkerDetailsByServiceName(
    //                anyString(), anyInt(), anyInt(), anyString(), anyString());
    //
    //        mockMvc.perform(post(REFRESH_JOB_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
    //                        .param("jobId", jobId.toString()))
    //                .andExpect(status().is(202)) // NB: no failure in refresh API as CRD call is in a background
    //                process
    //                .andReturn();
    //
    //        await().pollDelay(WAIT_FOR_ASYNC_TO_COMPLETE, TimeUnit.SECONDS)
    //                .timeout(WAIT_FOR_ASYNC_TO_TIMEOUT, TimeUnit.SECONDS)
    //                .untilAsserted(() ->
    //                        verify(crdFeignClient, times(3))
    //                                .getCaseworkerDetailsByServiceName(any(), any(), any(), any(), any())
    //        );
    //
    //        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
    //        assertEquals("NEW", refreshJob.getStatus());// failed process should change the status to IN-PROGRESS
    //    }
    //
    //    @Test
    //    @Order(14)
    //    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete_CRDRetry() throws Exception {
    //        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);
    //
    //        logger.info(" RefreshJob record With JobId retry success third time to process successful");
    //        Long jobId = createRefreshJobLegalOperations(NEW, null, null);
    //
    //        doThrow(RuntimeException.class).doThrow(RuntimeException.class).doReturn(buildUserProfileResponse())
    //                .when(crdFeignClient).getCaseworkerDetailsByServiceName(
    //                        anyString(), anyInt(), anyInt(), anyString(), anyString());
    //        mockRequestMappingServiceWithCaseworkerStatus(HttpStatus.CREATED);
    //
    //        mockMvc.perform(post(REFRESH_JOB_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
    //                        .param("jobId", jobId.toString()))
    //                .andExpect(status().is(202))
    //                .andReturn();
    //
    //        await().pollDelay(WAIT_FOR_ASYNC_TO_COMPLETE, TimeUnit.SECONDS)
    //                .timeout(WAIT_FOR_ASYNC_TO_TIMEOUT, TimeUnit.SECONDS)
    //                .untilAsserted(() -> Assertions.assertTrue(isRefreshJobInStatus(jobId, COMPLETED)));
    //
    //        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
    //        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
    //        assertEquals(COMPLETED, refreshJob.getStatus());
    //        assertNull(refreshJob.getUserIds());
    //        assertNotNull(refreshJob.getLog());
    //    }
    //
    //    /*
    //        IT for JRD refresh job scenarios start from here
    //     */
    //    @ParameterizedTest
    //    @ValueSource(ints = {1, 2})
    //    @Order(15)
    //    public void shouldProcessRefreshRoleAssignmentsWithJobIdToComplete_Judicial(int numberOfBatches)
    //    throws Exception {
    //        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);
    //
    //        logger.info(" RefreshJob record With Only JobId to process successful");
    //        String[] userIds = buildUserIdList(TEST_PAGE_SIZE * numberOfBatches);
    //
    //        mockJRDService(userIds);
    //        mockJBSService(userIds);
    //        mockRequestMappingServiceWithJudicialStatus(HttpStatus.CREATED);
    //
    //        Long jobId = createRefreshJobJudicialTargetedUserList(userIds);
    //        UserRequest userRequest = buildUserRequestWithUserIds(userIds);
    //
    //        mockMvc.perform(post(REFRESH_JOB_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
    //                        .param("jobId", jobId.toString())
    //                        .content(mapper.writeValueAsBytes(userRequest)))
    //                .andExpect(status().is(202))
    //                .andReturn();
    //
    //        await().pollDelay(WAIT_FOR_ASYNC_TO_COMPLETE, TimeUnit.SECONDS)
    //                .timeout(WAIT_FOR_ASYNC_TO_TIMEOUT, TimeUnit.SECONDS)
    //                .untilAsserted(() -> Assertions.assertTrue(isRefreshJobInStatus(jobId, COMPLETED)));
    //
    //        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
    //        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
    //        assertEquals(COMPLETED, refreshJob.getStatus());
    //        assertNotNull(refreshJob.getLog());
    //
    //        Mockito.verify(jrdFeignClient, times(1)).getJudicialDetailsById(any(), any());
    //        Mockito.verify(jbsFeignClient, times(numberOfBatches)).getJudicialBookingByUserIds(any());
    //    }
    //
    //    @Test
    //    @Order(16)
    //    public void shouldProcessRefreshRoleAssignmentsWithJobIdToAborted_Judicial() throws Exception {
    //        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);
    //
    //        logger.info(" RefreshJob record With Only JobId to process Aborted");
    //        String[] userIds = buildUserIdList(TEST_PAGE_SIZE);
    //
    //        mockJRDService(userIds);
    //        mockJBSService(userIds);
    //        mockRequestMappingServiceWithJudicialStatus(UNPROCESSABLE_ENTITY);
    //
    //        Long jobId = createRefreshJobJudicialTargetedUserList(userIds);
    //        UserRequest userRequest = buildUserRequestWithUserIds(userIds);
    //
    //        mockMvc.perform(post(REFRESH_JOB_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
    //                        .param("jobId", jobId.toString())
    //                        .content(mapper.writeValueAsBytes(userRequest)))
    //                .andExpect(status().is(202))
    //                .andReturn();
    //
    //        await().pollDelay(WAIT_FOR_ASYNC_TO_COMPLETE, TimeUnit.SECONDS)
    //                .timeout(WAIT_FOR_ASYNC_TO_TIMEOUT, TimeUnit.SECONDS)
    //                .untilAsserted(() -> Assertions.assertTrue(isRefreshJobInStatus(jobId, ABORTED)));
    //
    //        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
    //        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
    //        assertEquals(ABORTED, refreshJob.getStatus());
    //        assertNotNull(refreshJob.getUserIds());
    //        assertThat(refreshJob.getLog(), containsString(String.join(",", refreshJob.getUserIds())));
    //    }
    //
    //    @Order(17)
    //    @ParameterizedTest
    //    @ValueSource(booleans = {true, false})
    //    public void shouldProcessRefreshRoleAssignments_deletedFlag(Boolean deletedFlagStatus) throws Exception {
    //        when(securityUtils.getServiceName()).thenReturn(AUTHORISED_JOB_SERVICE);
    //
    //        logger.info(" RefreshJob record with judicial user deleted flag {}", deletedFlagStatus);
    //        String[] userIds = buildUserIdList(1);
    //
    //        ResponseEntity<List<JudicialProfileV2>> res = buildJudicialProfilesResponseV2(userIds);
    //        res.getBody().get(0).setDeletedFlag(deletedFlagStatus.toString());
    //        doReturn(res).when(jrdFeignClient).getJudicialDetailsById(any(), any());
    //
    //        mockJBSService(userIds);
    //        mockRequestMappingServiceWithJudicialStatus(HttpStatus.CREATED);
    //
    //        Long jobId = createRefreshJobJudicialTargetedUserList(userIds);
    //        UserRequest userRequest = buildUserRequestWithUserIds(userIds);
    //
    //        mockMvc.perform(post(REFRESH_JOB_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(AUTHORISED_JOB_SERVICE))
    //                        .param("jobId", jobId.toString())
    //                        .content(mapper.writeValueAsBytes(userRequest)))
    //                .andExpect(status().is(202))
    //                .andReturn();
    //
    //        await().pollDelay(WAIT_FOR_ASYNC_TO_COMPLETE, TimeUnit.SECONDS)
    //                .timeout(WAIT_FOR_ASYNC_TO_TIMEOUT, TimeUnit.SECONDS)
    //                .untilAsserted(() -> Assertions.assertTrue(isRefreshJobInStatus(jobId, COMPLETED)));
    //
    //        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
    //        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
    //        assertEquals(COMPLETED, refreshJob.getStatus());
    //        assertNotNull(refreshJob.getLog());
    //
    //        verify(jrdFeignClient, times(1)).getJudicialDetailsById(any(), any());
    //        verify(jbsFeignClient, deletedFlagStatus ? times(0) : times(1)).getJudicialBookingByUserIds(any());
    //        verify(requestMappingService,
    //        times(1)).createJudicialAssignments(usersAccessProfilesCaptor.capture(), any());
    //
    //        Map<String, Set<UserAccessProfile>> usersAccessProfiles = usersAccessProfilesCaptor.getValue();
    //        assertEquals(deletedFlagStatus, usersAccessProfiles.get(userIds[0]).isEmpty());
    //    }

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

    //    private void mockJRDService(String[] userIds) {
    //        ResponseEntity<List<JudicialProfileV2>> userProfilesResponse = buildJudicialProfilesResponseV2(userIds);
    //        doReturn(userProfilesResponse).when(jrdFeignClient).getJudicialDetailsById(any(), any());
    //    }
    //
    //    private void mockJBSService(String[] userIds) {
    //        doReturn(buildJudicialBookingsResponse(userIds)).when(jbsFeignClient).getJudicialBookingByUserIds(any());
    //    }

    private void mockRequestMappingServiceWithCaseworkerStatus(HttpStatus status) {
        doReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(ResponseEntity.status(status).body(
                new RoleAssignmentRequestResource(AssignmentRequestBuilder.buildAssignmentRequest(
                        false))))))
                .when(requestMappingService).createCaseworkerAssignments(any());
    }

    //    private void mockRequestMappingServiceWithJudicialStatus(HttpStatus status) {
    //        doReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(ResponseEntity.status(status).body(
    //                new RoleAssignmentRequestResource(AssignmentRequestBuilder.buildAssignmentRequest(
    //                        false))))))
    //                .when(requestMappingService).createJudicialAssignments(any(), any());
    //    }
    //
    //    private UserRequest buildUserRequestWithUserIds(String[] userIds) {
    //        return UserRequest.builder()
    //                .userIds(Arrays.stream(userIds).toList())
    //                .build();
    //    }
    //
    //    public Long createRefreshJobJudicialTargetedUserList(String[] userIds) throws Exception {
    //        return callTestSupportCreateJobApi(RoleCategory.JUDICIAL, NEW, true, null, userIds);
    //    }

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

        String response = getRequestSpecification(AUTHORISED_JOB_SERVICE, ACTOR_ID1)
                .body(createUserRequestContent(userIds))
                .when().post(CREATE_REFRESH_JOB_URL
                        + "?roleCategory=" + roleCategory.name()
                        + "&jurisdiction=IA"
                        + "&linkJob=" + linkJob.toString()
                        + "&linkedJobId=" + (linkedJobId != null ? linkedJobId.toString() : "0")
                        + "&status=" + status)
                .then().assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .extract().asString();

        // verify response is as expected
        RefreshJob refreshJob = OBJECT_MAPPER.readValue(response, RefreshJob.class);
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

        String response = getRequestSpecification(S2S_ORM, ACTOR_ID1)
                .when().get(GET_REFRESH_JOB_URL, jobId.toString())
                .then().assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract().asString();

        return OBJECT_MAPPER.readValue(response, RefreshJob.class);
    }

    private boolean isRefreshJobInStatus(Long jobId, String status) throws Exception {
        RefreshJob refreshJob = callTestSupportGetJobApi(jobId);
        return refreshJob.getStatus().equals(status);
    }

    private String createUserRequestContent(String[] userIds) throws JsonProcessingException {
        if (userIds == null) {
            return "";
        }

        return OBJECT_MAPPER.writeValueAsString(UserRequest.builder()
                .userIds(Arrays.stream(userIds).toList())
                .build());
    }

}
