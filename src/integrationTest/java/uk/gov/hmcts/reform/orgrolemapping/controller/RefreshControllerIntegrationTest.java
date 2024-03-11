package uk.gov.hmcts.reform.orgrolemapping.controller;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Appointment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
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
import uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureConditionEvaluator;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.FAILED_ROLE_REFRESH;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildJudicialBookingsResponse;

@TestPropertySource(properties = {
    "feign.client.config.jrdClient.v2Active=false"
})
public class RefreshControllerIntegrationTest extends BaseTestIntegration {

    private static final Logger logger = LoggerFactory.getLogger(RefreshControllerIntegrationTest.class);

    private final WiremockFixtures wiremockFixtures = new WiremockFixtures();
    private static final String AUTHORISED_SERVICE = "am_role_assignment_refresh_batch";
    private static final String ROLE_NAME_STCW = "senior-tribunal-caseworker";
    private static final String ROLE_NAME_TCW = "tribunal-caseworker";
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

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        doReturn(true).when(featureConditionEvaluation).preHandle(any(),any(),any());
        MockUtils.setSecurityAuthorities(authentication, MockUtils.ROLE_CASEWORKER);
        wiremockFixtures.resetRequests();
    }

    @Test
    public void shouldProcessRefreshRoleAssignmentsWithJudicialProfiles() throws Exception {
        logger.info(" Refresh role assignments successfully with valid user profiles");
        var uuid = UUID.randomUUID().toString();
        doReturn(buildJudicialProfilesResponse(uuid)).when(jrdFeignClient).getJudicialDetailsById(any(), any());
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
    public void shouldFailProcessRefreshRoleAssignmentsWithJudicialProfiles_withFailedRoleAssignments()
            throws Exception {
        logger.info(" Refresh role assignments failed with valid user profiles");
        var uuid = UUID.randomUUID().toString();
        doReturn(buildJudicialProfilesResponse(uuid)).when(jrdFeignClient).getJudicialDetailsById(any(), any());
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
    public void shouldFailProcessRefreshRoleAssignmentsWithJudicialProfiles_withEmptyJudicialBookings()
            throws Exception {
        logger.info(" Refresh role assignments with empty bookings");
        var uuid = UUID.randomUUID().toString();
        doReturn(buildJudicialProfilesResponse(uuid)).when(jrdFeignClient).getJudicialDetailsById(any(), any());
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
        ResponseEntity<Map<String, String>> response = ResponseEntity.status(404).body(Map.of(
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
        ResponseEntity<Map<String, String>> response = ResponseEntity.status(501).body(Map.of(
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

    private ResponseEntity<List<JudicialProfile>> buildJudicialProfilesResponse(String... userIds) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("total_records", "" + userIds.length);
        List<JudicialProfile> bookings = new ArrayList<>();
        for (var userId:userIds) {
            bookings.add(JudicialProfile.builder().sidamId(userId)
                    .appointments(List.of(Appointment.builder().appointment("Tribunal Judge")
                            .appointmentType("Fee Paid").build())).build());
        }
        return new ResponseEntity<>(bookings, headers, HttpStatus.OK);
    }

    private void mockRequestMappingServiceBookingParamWithStatus(HttpStatus status) {
        doReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(ResponseEntity.status(status).body(
                new RoleAssignmentRequestResource(AssignmentRequestBuilder.buildAssignmentRequest(
                        false))))))
                .when(requestMappingService).createJudicialAssignments(any(), any());
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
}
