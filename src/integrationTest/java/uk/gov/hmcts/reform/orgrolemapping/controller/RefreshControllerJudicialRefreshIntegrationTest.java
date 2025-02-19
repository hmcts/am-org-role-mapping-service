package uk.gov.hmcts.reform.orgrolemapping.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfileV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialRefreshRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JBSFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.RASFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.FAILED_ROLE_REFRESH;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_XUI;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.getHttpHeaders;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildJudicialBookingsResponse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildJudicialProfilesResponseV2;

@TestPropertySource(properties = {
    "refresh.BulkAssignment.includeJudicialBookings=true",
    "refresh.judicial.filterSoftDeletedUsers=true"
})
public class RefreshControllerJudicialRefreshIntegrationTest extends BaseTestIntegration {

    private static final Logger logger = LoggerFactory.getLogger(RefreshControllerJudicialRefreshIntegrationTest.class);

    private final WiremockFixtures wiremockFixtures = new WiremockFixtures();

    private static final String JUDICIAL_REFRESH_URL = "/am/role-mapping/judicial/refresh";

    private MockMvc mockMvc;

    @Inject
    private WebApplicationContext wac;

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

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        MockUtils.setSecurityAuthorities(authentication, MockUtils.ROLE_CASEWORKER);
        wiremockFixtures.resetRequests();
    }

    @Test
    public void shouldProcessRefreshRoleAssignmentsWithJudicialProfilesV2() throws Exception {
        logger.info(" Refresh role assignments successfully with valid user profiles");
        var uuid = UUID.randomUUID().toString();
        doReturn(buildJudicialProfilesResponseV2(uuid)).when(jrdFeignClient).getJudicialDetailsById(any(), any());
        doReturn(buildJudicialBookingsResponse(uuid)).when(jbsFeignClient).getJudicialBookingByUserIds(any());
        mockRequestMappingServiceBookingParamWithStatus(HttpStatus.CREATED);

        MvcResult result = mockMvc.perform(post(JUDICIAL_REFRESH_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(S2S_XUI))
                        .content(mapper.writeValueAsBytes(JudicialRefreshRequest.builder()
                                .refreshRequest(IntTestDataBuilder.buildUserRequest()).build())))
                .andExpect(status().is(200))
                .andReturn();

        var contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(Constants.SUCCESS_ROLE_REFRESH));
        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void shouldProcessRefreshRoleAssignmentsWithJudicialProfilesV2_deletedFlag(Boolean deletedFlagStatus)
            throws Exception {
        logger.info(" Refresh role assignments when judicial user deleted flag {}", deletedFlagStatus);
        var uuid = UUID.randomUUID().toString();

        ResponseEntity<List<JudicialProfileV2>> res = buildJudicialProfilesResponseV2(uuid);
        res.getBody().get(0).setDeletedFlag(deletedFlagStatus.toString());

        doReturn(res).when(jrdFeignClient).getJudicialDetailsById(any(), any());
        doReturn(buildJudicialBookingsResponse(uuid)).when(jbsFeignClient).getJudicialBookingByUserIds(any());
        mockRequestMappingServiceBookingParamWithStatus(HttpStatus.CREATED);

        MvcResult result = mockMvc.perform(post(JUDICIAL_REFRESH_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(S2S_XUI))
                        .content(mapper.writeValueAsBytes(JudicialRefreshRequest.builder()
                                .refreshRequest(IntTestDataBuilder.buildUserRequest()).build())))
                .andExpect(status().is(200))
                .andReturn();

        verify(requestMappingService, times(1))
                .createJudicialAssignments(usersAccessProfilesCaptor.capture(), any());

        Map<String, Set<UserAccessProfile>> usersAccessProfiles = usersAccessProfilesCaptor.getValue();
        assertEquals(deletedFlagStatus, usersAccessProfiles.get(uuid).isEmpty());

        var contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(Constants.SUCCESS_ROLE_REFRESH));
        logger.info(" -- Refresh Role Assignment record updated successfully when judicial user deleted flag {} -- ",
                deletedFlagStatus);
    }

    @Test
    public void shouldFailProcessRefreshRoleAssignmentsWithJudicialProfiles_withFailedRoleAssignmentsV2()
            throws Exception {
        logger.info(" Refresh role assignments failed with valid user profiles");
        var uuid = UUID.randomUUID().toString();
        doReturn(buildJudicialProfilesResponseV2(uuid)).when(jrdFeignClient).getJudicialDetailsById(any(), any());
        doReturn(buildJudicialBookingsResponse(uuid)).when(jbsFeignClient).getJudicialBookingByUserIds(any());
        mockRequestMappingServiceBookingParamWithStatus(HttpStatus.UNPROCESSABLE_ENTITY);

        MvcResult result = mockMvc.perform(post(JUDICIAL_REFRESH_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(S2S_XUI))
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
        var uuid = UUID.randomUUID().toString();
        doReturn(buildJudicialProfilesResponseV2(uuid)).when(jrdFeignClient).getJudicialDetailsById(any(), any());
        doReturn(buildJudicialBookingsResponse()).when(jbsFeignClient).getJudicialBookingByUserIds(any());
        mockRequestMappingServiceBookingParamWithStatus(HttpStatus.CREATED);

        MvcResult result = mockMvc.perform(post(JUDICIAL_REFRESH_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(S2S_XUI))
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
        ResponseEntity<Map<String, String>> response = ResponseEntity.status(404).body(Map.of(
                "errorDescription", "The User Profile data could not be found",
                "status", "Not Found"));
        doReturn(response).when(jrdFeignClient).getJudicialDetailsById(any(), any());
        doReturn(buildJudicialBookingsResponse()).when(jbsFeignClient).getJudicialBookingByUserIds(any());
        mockRequestMappingServiceBookingParamWithStatus(HttpStatus.CREATED);

        MvcResult result = mockMvc.perform(post(JUDICIAL_REFRESH_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(S2S_XUI))
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
        ResponseEntity<Map<String, String>> response = ResponseEntity.status(501).body(Map.of(
                "errorDescription", "The User Profile data could not be found",
                "status", "Not Found"));
        doReturn(response).when(jrdFeignClient).getJudicialDetailsById(any(), any());

        mockMvc.perform(post(JUDICIAL_REFRESH_URL)
                        .contentType(JSON_CONTENT_TYPE)
                        .headers(getHttpHeaders(S2S_XUI))
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
                        .headers(getHttpHeaders(S2S_XUI))
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
                        .headers(getHttpHeaders(S2S_XUI))
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
                        .headers(getHttpHeaders(S2S_XUI))
                        .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorDescription")
                        .value(containsString("The input parameter: \"abc-123$\", "
                                + "does not comply with the required pattern")))
                .andReturn();
    }

    private void mockRequestMappingServiceBookingParamWithStatus(HttpStatus status) {
        doReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(ResponseEntity.status(status).body(
                new RoleAssignmentRequestResource(AssignmentRequestBuilder.buildAssignmentRequest(
                        false))))))
                .when(requestMappingService).createJudicialAssignments(any(), any());
    }

}
