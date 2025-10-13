package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import jakarta.inject.Inject;
import java.util.List;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.orgrolemapping.controller.RefreshController;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUserResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.RASFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SUCCESS_ROLE_REFRESH;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_XUI;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.getHttpHeaders;

class PrmSchedulerProcess6SingleUserIntegrationTest extends BaseProcess6IntegrationTest {

    private static final String REFRESH_URL = "/am/role-mapping/professional/refresh";
    private MockMvc mockMvc;

    @MockBean
    private PRDFeignClient prdFeignClient;

    @MockBean
    private RASFeignClient rasFeignClient;

    @Inject
    private RefreshController refreshController;

    @Inject
    private WebApplicationContext wac;

    @MockBean
    private SecurityUtils securityUtils;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Captor
    ArgumentCaptor<AssignmentRequest> assignmentRequestCaptor;

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        MockUtils.setSecurityAuthorities(authentication, MockUtils.ROLE_CASEWORKER);
        wiremockFixtures.resetRequests();
    }

    /**
     *  No Update - UserRefreshQueue.accessTypeVersion >  PRM Access Version.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_version1.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_accessVersion() {
        MvcResult result = runTest(List.of("/SchedulerTests/PrdRetrieveUsers/userx_scenario_01.json"),
                1, false, false, EndStatus.FAILED);
        
        // Validate the exception class and message
        assertNotNull(result);
        assertServiceException(result.getResolvedException(),
                String.format("User %s has access types version %d which is higher than the latest version %d",
                        USERID, 2, 1));
    }

    /**
     *  OrganisationStatus of PENDING.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_user_refresh_queue_orgstatus_pending.sql"
    })
    void testCreateRole_orgstatus_pending() {
        runTest(List.of("/SchedulerTests/PrdRetrieveUsers/userx_scenario_04.json"),
                1, false, false, EndStatus.SUCCESS);
    }

    /**
     *  Delete Role Assignment.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_deleted.sql"
    })
    void testDeleteRole() {
        runTest(List.of("/SchedulerTests/PrdRetrieveUsers/userx_scenario_03.json"),
                1, false, false, EndStatus.SUCCESS);
    }

    protected void testCreateRoleAssignment(boolean organisation, boolean group) {
        runTest(organisation ? List.of("/SchedulerTests/PrdRetrieveUsers/userx_scenario_01.json")
                : List.of("/SchedulerTests/PrdRetrieveUsers/userx_scenario_02.json"),
                1, organisation, group, EndStatus.SUCCESS);
    }

    @SneakyThrows
    private MvcResult runTest(List<String> refreshUserfileNames, int expectedNumberOfRecords,
                         boolean organisation, boolean group, EndStatus endStatus) {

        // GIVEN
        logBeforeStatus();
        stubPrdRefreshUser(refreshUserfileNames, USERID, "false", "false");
        stubRasCreateRoleAssignment(endStatus);
        HttpStatus expectedStatus = endStatus.equals(EndStatus.FAILED)
                ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK;

        // WHEN
        MvcResult result = mockMvc.perform(post(REFRESH_URL + "?userId=" + USERID)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(getHttpHeaders(S2S_XUI)))
                .andExpect(status().is(expectedStatus.value()))
                .andReturn();

        // THEN
        if (HttpStatus.OK.equals(expectedStatus)) {
            String response = result.getResponse().getContentAsString();

            logAfterStatus(response);

            // verify the response
            assertEquals(String.format("{\"Message\":\"%s\"}", SUCCESS_ROLE_REFRESH), response);

            if (expectedNumberOfRecords != 0) {
                assertAssignmentRequest(organisation, group);
            }
        } else {
            assertNotNull(result);
            assertNotNull(result.getResolvedException());
            logAfterStatus(String.format("Exception=%s",result.getResolvedException().getMessage()));
        }
        return result;
    }

    @Override
    @SneakyThrows
    protected void stubPrdRefreshUser(String body, String userId) {
        doReturn(ResponseEntity.ok(mapper.readValue(body, GetRefreshUserResponse.class)))
            .when(prdFeignClient).getRefreshUsers(any(), any(), any(), any());
    }

    @Override
    @SneakyThrows
    protected void stubRasCreateRoleAssignment(EndStatus endStatus) {
        doReturn(ResponseEntity.ok("{}"))
            .when(rasFeignClient).createRoleAssignment(any(), any());
    }

    @Override
    protected AssignmentRequest getAssignmentRequest() {
        var assignment = verify(rasFeignClient, times(1))
                .createRoleAssignment(assignmentRequestCaptor.capture(), any());
        return assignmentRequestCaptor.getValue();
    }

    private void assertServiceException(Exception exception, String errorMessage) {
        assertNotNull(exception);
        assertEquals(ServiceException.class, exception.getClass());
        assertEquals(errorMessage, exception.getMessage());
    }
}
