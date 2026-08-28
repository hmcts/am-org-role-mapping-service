package uk.gov.hmcts.reform.orgrolemapping.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfileV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialRefreshRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JBSFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures.OBJECT_MAPPER;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildJudicialBookingsResponse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildJudicialProfilesResponseV2;

@TestPropertySource(properties = {
    "refresh.BulkAssignment.includeJudicialBookings=true",
    "refresh.judicial.filterSoftDeletedUsers=true"
})
public class RefreshControllerJudicialRefreshIntegrationTest extends BaseAuthorisedTestIntegration {

    private static final Logger logger = LoggerFactory.getLogger(RefreshControllerJudicialRefreshIntegrationTest.class);

    private static final String JUDICIAL_REFRESH_URL = "/am/role-mapping/judicial/refresh";

    @MockBean
    private JRDFeignClient jrdFeignClient;

    @MockBean
    private JBSFeignClient jbsFeignClient;

    @MockBean
    private RequestMappingService<UserAccessProfile> requestMappingService;

    @Captor
    private ArgumentCaptor<Map<String, Set<UserAccessProfile>>> usersAccessProfilesCaptor;

    @Test
    public void shouldProcessRefreshRoleAssignmentsWithJudicialProfilesV2() throws Exception {
        logger.info(" Refresh role assignments successfully with valid user profiles");
        var uuid = UUID.randomUUID().toString();
        doReturn(buildJudicialProfilesResponseV2(uuid)).when(jrdFeignClient).getJudicialDetailsById(any(), any());
        doReturn(buildJudicialBookingsResponse(uuid)).when(jbsFeignClient).getJudicialBookingByUserIds(any());
        mockRequestMappingServiceBookingParamWithStatus(HttpStatus.CREATED);
        JudicialRefreshRequest request = JudicialRefreshRequest.builder()
                .refreshRequest(IntTestDataBuilder.buildUserRequest()).build();

        getRequestSpecification()
                .body(OBJECT_MAPPER.writeValueAsString(request))
                .when().post(JUDICIAL_REFRESH_URL)
                .then().assertThat()
                .statusCode(HttpStatus.OK.value())
                .and()
                .body(containsString(Constants.SUCCESS_ROLE_REFRESH));
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
        JudicialRefreshRequest request = JudicialRefreshRequest.builder()
                .refreshRequest(IntTestDataBuilder.buildUserRequest()).build();

        getRequestSpecification()
                .body(OBJECT_MAPPER.writeValueAsString(request))
                .when().post(JUDICIAL_REFRESH_URL)
                .then().assertThat()
                .statusCode(HttpStatus.OK.value())
                .and()
                .body(containsString(Constants.SUCCESS_ROLE_REFRESH));

        verify(requestMappingService, times(1))
                .createJudicialAssignments(usersAccessProfilesCaptor.capture(), any());

        Map<String, Set<UserAccessProfile>> usersAccessProfiles = usersAccessProfilesCaptor.getValue();
        assertEquals(deletedFlagStatus, usersAccessProfiles.get(uuid).isEmpty());
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
        JudicialRefreshRequest request = JudicialRefreshRequest.builder()
                .refreshRequest(IntTestDataBuilder.buildUserRequest()).build();

        getRequestSpecification()
                .body(OBJECT_MAPPER.writeValueAsString(request))
                .when().post(JUDICIAL_REFRESH_URL)
                .then().assertThat()
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .and()
                .body(containsString(Constants.FAILED_ROLE_REFRESH));
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
        JudicialRefreshRequest request = JudicialRefreshRequest.builder()
                .refreshRequest(IntTestDataBuilder.buildUserRequest()).build();

        getRequestSpecification()
                .body(OBJECT_MAPPER.writeValueAsString(request))
                .when().post(JUDICIAL_REFRESH_URL)
                .then().assertThat()
                .statusCode(HttpStatus.OK.value())
                .and()
                .body(containsString(Constants.SUCCESS_ROLE_REFRESH));
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
        JudicialRefreshRequest request = JudicialRefreshRequest.builder()
                .refreshRequest(IntTestDataBuilder.buildUserRequest()).build();

        getRequestSpecification()
                .body(OBJECT_MAPPER.writeValueAsString(request))
                .when().post(JUDICIAL_REFRESH_URL)
                .then().assertThat()
                .statusCode(HttpStatus.OK.value())
                .and()
                .body(containsString(Constants.SUCCESS_ROLE_REFRESH));
    }

    @Test
    public void shouldFailProcessRefreshRoleAssignmentsWithJudicialProfiles_withEmptyJudicialProfiles()
            throws Exception {
        logger.info(" Refresh role assignments with empty bookings");
        ResponseEntity<Map<String, String>> response = ResponseEntity.status(501).body(Map.of(
                "errorDescription", "The User Profile data could not be found",
                "status", "Not Found"));
        doReturn(response).when(jrdFeignClient).getJudicialDetailsById(any(), any());
        JudicialRefreshRequest request = JudicialRefreshRequest.builder()
                .refreshRequest(IntTestDataBuilder.buildUserRequest()).build();

        getRequestSpecification()
                .body(OBJECT_MAPPER.writeValueAsString(request))
                .when().post(JUDICIAL_REFRESH_URL)
                .then().assertThat()
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .and()
                .body(containsString(Constants.FAILED_ROLE_REFRESH));
    }

    @Test
    public void shouldRejectJudicialRefreshRequest_withEmptyBody() throws Exception {
        logger.info(" Refresh request rejected with empty request");
        JudicialRefreshRequest request = JudicialRefreshRequest.builder().build();
        getRequestSpecification()
                .body(OBJECT_MAPPER.writeValueAsString(request))
                .when().post(JUDICIAL_REFRESH_URL)
                .then().assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .and()
                .body(containsString("Empty user request"));
    }

    @Test
    public void shouldRejectJudicialRefreshRequest_withEmptyUserList() throws Exception {
        logger.info(" Refresh request rejected with empty user request");
        JudicialRefreshRequest request = JudicialRefreshRequest.builder()
                .refreshRequest(UserRequest.builder().userIds(Collections.emptyList()).build()).build();
        getRequestSpecification()
                .body(OBJECT_MAPPER.writeValueAsString(request))
                .when().post(JUDICIAL_REFRESH_URL)
                .then().assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .and()
                .body(containsString("Empty user request"));

    }

    @Test
    public void shouldRejectJudicialRefreshRequest_withInvalidUserIdFormat() throws Exception {
        logger.info(" Refresh role assignments failed with invalid valid user profiles format");

        JudicialRefreshRequest request = JudicialRefreshRequest.builder()
                .refreshRequest(UserRequest.builder().userIds(List.of("abc-123$")).build()).build();
        getRequestSpecification()
                .body(OBJECT_MAPPER.writeValueAsString(request))
                .when().post(JUDICIAL_REFRESH_URL)
                .then().assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .and()
                .body(containsString("The input parameter: \\\"abc-123$\\\", "
                        + "does not comply with the required pattern"));
    }

    private void mockRequestMappingServiceBookingParamWithStatus(HttpStatus status) {
        doReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(ResponseEntity.status(status).body(
                new RoleAssignmentRequestResource(AssignmentRequestBuilder.buildAssignmentRequest(
                        false))))))
                .when(requestMappingService).createJudicialAssignments(any(), any());
    }
}