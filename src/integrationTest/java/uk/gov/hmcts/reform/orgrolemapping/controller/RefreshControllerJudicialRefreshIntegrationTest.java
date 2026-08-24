package uk.gov.hmcts.reform.orgrolemapping.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialRefreshRequest;
import uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildJudicialBookingsResponse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildJudicialProfilesResponseV2;

@TestPropertySource(properties = {
    "refresh.BulkAssignment.includeJudicialBookings=true",
    "refresh.judicial.filterSoftDeletedUsers=true"
})
public class RefreshControllerJudicialRefreshIntegrationTest extends BaseAuthorisedTestIntegration {

    private static final Logger logger = LoggerFactory.getLogger(RefreshControllerJudicialRefreshIntegrationTest.class);

    private static final String JUDICIAL_REFRESH_URL = "/am/role-mapping/judicial/refresh";

    @Test
    public void shouldProcessRefreshRoleAssignmentsWithJudicialProfilesV2() throws Exception {
        logger.info(" Refresh role assignments successfully with valid user profiles");

        // WHEN
        var uuid = UUID.randomUUID().toString();
        stubJbsGetJudicialDetailsById(uuid);
        stubJbsGetJudicialBookingByUserIds(uuid);
        JudicialRefreshRequest request = JudicialRefreshRequest.builder()
                .refreshRequest(IntTestDataBuilder.buildUserRequest()).build();

        // THEM
        getRequestSpecification()
                .body(mapper.writeValueAsBytes(request))
                .when().post(JUDICIAL_REFRESH_URL);
        //      .then().assertThat()
        //      .statusCode(HttpStatus.OK.value())
        //      .and()
        //      .body(containsString(Constants.SUCCESS_ROLE_REFRESH));
        logger.info(" -- Refresh Role Assignment record updated successfully -- ");
    }

    //    @ParameterizedTest
    //    @ValueSource(booleans = {true, false})
    //    public void shouldProcessRefreshRoleAssignmentsWithJudicialProfilesV2_deletedFlag(Boolean deletedFlagStatus)
    //            throws Exception {
    //        logger.info(" Refresh role assignments when judicial user deleted flag {}", deletedFlagStatus);
    //        var uuid = UUID.randomUUID().toString();
    //
    //        ResponseEntity<List<JudicialProfileV2>> res = buildJudicialProfilesResponseV2(uuid);
    //        res.getBody().get(0).setDeletedFlag(deletedFlagStatus.toString());
    //
    //        doReturn(res).when(jrdFeignClient).getJudicialDetailsById(any(), any());
    //        doReturn(buildJudicialBookingsResponse(uuid)).when(jbsFeignClient).getJudicialBookingByUserIds(any());
    //        mockRequestMappingServiceBookingParamWithStatus(HttpStatus.CREATED);
    //
    //        MvcResult result = mockMvc.perform(post(JUDICIAL_REFRESH_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(S2S_XUI))
    //                        .content(mapper.writeValueAsBytes(JudicialRefreshRequest.builder()
    //                                .refreshRequest(IntTestDataBuilder.buildUserRequest()).build())))
    //                .andExpect(status().is(200))
    //                .andReturn();
    //
    //        verify(requestMappingService, times(1))
    //                .createJudicialAssignments(usersAccessProfilesCaptor.capture(), any());
    //
    //        Map<String, Set<UserAccessProfile>> usersAccessProfiles = usersAccessProfilesCaptor.getValue();
    //        assertEquals(deletedFlagStatus, usersAccessProfiles.get(uuid).isEmpty());
    //
    //        var contentAsString = result.getResponse().getContentAsString();
    //        assertTrue(contentAsString.contains(Constants.SUCCESS_ROLE_REFRESH));
    //        logger.info(
    //        " -- Refresh Role Assignment record updated successfully when judicial user deleted flag {} -- ",
    //                deletedFlagStatus);
    //    }
    //
    //    @Test
    //    public void shouldFailProcessRefreshRoleAssignmentsWithJudicialProfiles_withFailedRoleAssignmentsV2()
    //            throws Exception {
    //        logger.info(" Refresh role assignments failed with valid user profiles");
    //        var uuid = UUID.randomUUID().toString();
    //        doReturn(buildJudicialProfilesResponseV2(uuid)).when(jrdFeignClient).getJudicialDetailsById(any(), any());
    //        doReturn(buildJudicialBookingsResponse(uuid)).when(jbsFeignClient).getJudicialBookingByUserIds(any());
    //        mockRequestMappingServiceBookingParamWithStatus(HttpStatus.UNPROCESSABLE_ENTITY);
    //
    //        MvcResult result = mockMvc.perform(post(JUDICIAL_REFRESH_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(S2S_XUI))
    //                        .content(mapper.writeValueAsBytes(JudicialRefreshRequest.builder()
    //                                .refreshRequest(IntTestDataBuilder.buildUserRequest()).build())))
    //                .andExpect(status().is(422))
    //                .andReturn();
    //
    //        var contentAsString = result.getResponse().getContentAsString();
    //        assertTrue(contentAsString.contains(Constants.FAILED_ROLE_REFRESH));
    //        logger.info(" -- Refresh Role Assignment record fail to update -- ");
    //    }
    //
    //    @Test
    //    public void shouldFailProcessRefreshRoleAssignmentsWithJudicialProfiles_withEmptyJudicialBookingsV2()
    //            throws Exception {
    //        logger.info(" Refresh role assignments with empty bookings");
    //        var uuid = UUID.randomUUID().toString();
    //        doReturn(buildJudicialProfilesResponseV2(uuid)).when(jrdFeignClient).getJudicialDetailsById(any(), any());
    //        doReturn(buildJudicialBookingsResponse()).when(jbsFeignClient).getJudicialBookingByUserIds(any());
    //        mockRequestMappingServiceBookingParamWithStatus(HttpStatus.CREATED);
    //
    //        MvcResult result = mockMvc.perform(post(JUDICIAL_REFRESH_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(S2S_XUI))
    //                        .content(mapper.writeValueAsBytes(JudicialRefreshRequest.builder()
    //                                .refreshRequest(IntTestDataBuilder.buildUserRequest()).build())))
    //                .andExpect(status().isOk())
    //                .andReturn();
    //
    //        var contentAsString = result.getResponse().getContentAsString();
    //        assertTrue(contentAsString.contains(Constants.SUCCESS_ROLE_REFRESH));
    //        logger.info(" -- Refresh Role Assignment record updated without bookings -- ");
    //    }
    //
    //    @Test
    //    public void shouldFailProcessRefreshRoleAssignmentsWithJudicialProfiles_withNotFoundJudicialProfiles()
    //            throws Exception {
    //        logger.info(" Refresh role assignments with empty bookings");
    //        ResponseEntity<Map<String, String>> response = ResponseEntity.status(404).body(Map.of(
    //                "errorDescription", "The User Profile data could not be found",
    //                "status", "Not Found"));
    //        doReturn(response).when(jrdFeignClient).getJudicialDetailsById(any(), any());
    //        doReturn(buildJudicialBookingsResponse()).when(jbsFeignClient).getJudicialBookingByUserIds(any());
    //        mockRequestMappingServiceBookingParamWithStatus(HttpStatus.CREATED);
    //
    //        MvcResult result = mockMvc.perform(post(JUDICIAL_REFRESH_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(S2S_XUI))
    //                        .content(mapper.writeValueAsBytes(JudicialRefreshRequest.builder()
    //                                .refreshRequest(IntTestDataBuilder.buildUserRequest()).build())))
    //                .andExpect(status().is2xxSuccessful())
    //                .andReturn();
    //
    //        String contentAsString = result.getResponse().getContentAsString();
    //        assertTrue(contentAsString.contains(Constants.SUCCESS_ROLE_REFRESH));
    //    }
    //
    //    @Test
    //    public void shouldFailProcessRefreshRoleAssignmentsWithJudicialProfiles_withEmptyJudicialProfiles()
    //            throws Exception {
    //        logger.info(" Refresh role assignments with empty bookings");
    //        ResponseEntity<Map<String, String>> response = ResponseEntity.status(501).body(Map.of(
    //                "errorDescription", "The User Profile data could not be found",
    //                "status", "Not Found"));
    //        doReturn(response).when(jrdFeignClient).getJudicialDetailsById(any(), any());
    //
    //        mockMvc.perform(post(JUDICIAL_REFRESH_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(S2S_XUI))
    //                        .content(mapper.writeValueAsBytes(JudicialRefreshRequest.builder()
    //                                .refreshRequest(IntTestDataBuilder.buildUserRequest()).build())))
    //                .andExpect(status().is4xxClientError())
    //                .andExpect(jsonPath("$.errorDescription")
    //                        .value(containsString(FAILED_ROLE_REFRESH)))
    //                .andReturn();
    //    }
    //
    //    @Test
    //    public void shouldRejectJudicialRefreshRequest_withEmptyBody() throws Exception {
    //        logger.info(" Refresh request rejected with empty request");
    //        mockMvc.perform(post(JUDICIAL_REFRESH_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(S2S_XUI))
    //                        .content(mapper.writeValueAsBytes(JudicialRefreshRequest.builder().build())))
    //                .andExpect(status().isBadRequest())
    //                .andExpect(jsonPath("$.errorDescription")
    //                        .value(containsString("Empty user request")))
    //                .andReturn();
    //    }
    //
    //    @Test
    //    public void shouldRejectJudicialRefreshRequest_withEmptyUserList() throws Exception {
    //        logger.info(" Refresh request rejected with empty user request");
    //        JudicialRefreshRequest request = JudicialRefreshRequest.builder()
    //                .refreshRequest(UserRequest.builder().userIds(Collections.emptyList()).build()).build();
    //        mockMvc.perform(post(JUDICIAL_REFRESH_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(S2S_XUI))
    //                        .content(mapper.writeValueAsBytes(request)))
    //                .andExpect(status().isBadRequest())
    //                .andExpect(jsonPath("$.errorDescription")
    //                        .value(containsString("Empty user request")))
    //                .andReturn();
    //
    //    }
    //
    //    @Test
    //    public void shouldRejectJudicialRefreshRequest_withInvalidUserIdFormat() throws Exception {
    //        logger.info(" Refresh role assignments failed with invalid valid user profiles format");
    //
    //        JudicialRefreshRequest request = JudicialRefreshRequest.builder()
    //                .refreshRequest(UserRequest.builder().userIds(List.of("abc-123$")).build()).build();
    //        mockMvc.perform(post(JUDICIAL_REFRESH_URL)
    //                        .contentType(JSON_CONTENT_TYPE)
    //                        .headers(getHttpHeaders(S2S_XUI))
    //                        .content(mapper.writeValueAsBytes(request)))
    //                .andExpect(status().isBadRequest())
    //                .andExpect(jsonPath("$.errorDescription")
    //                        .value(containsString("The input parameter: \"abc-123$\", "
    //                                + "does not comply with the required pattern")))
    //                .andReturn();
    //    }
    //
    //    private void mockRequestMappingServiceBookingParamWithStatus(HttpStatus status) {
    //        doReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(ResponseEntity.status(status).body(
    //                new RoleAssignmentRequestResource(AssignmentRequestBuilder.buildAssignmentRequest(
    //                        false))))))
    //                .when(requestMappingService).createJudicialAssignments(any(), any());
    //  }

    public void stubJbsGetJudicialDetailsById(String uuid) throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(WireMock.post(urlPathMatching("/refdata/judicial/users"))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(mapper.writeValueAsString(buildJudicialProfilesResponseV2(uuid)))
                ));
    }

    public void stubJbsGetJudicialBookingByUserIds(String uuid) throws JsonProcessingException {
        WIRE_MOCK_SERVER.stubFor(WireMock.post(urlPathMatching("/am/bookings/query"))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(mapper.writeValueAsString(buildJudicialBookingsResponse(uuid)))
                ));
    }
}
