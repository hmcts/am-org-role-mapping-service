package uk.gov.hmcts.reform.orgrolemapping.domain.service;


import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Appointment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Authorisation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
class JudicialRefreshOrchestratorTest {

    private final RetrieveDataService retrieveDataService = mock(RetrieveDataService.class);
    private final RequestMappingService requestMappingService = mock(RequestMappingService.class);
    private final ParseRequestService parseRequestService = new ParseRequestService();
    private final JudicialBookingService judicialBookingService = mock(JudicialBookingService.class);

    @InjectMocks
    private final JudicialRefreshOrchestrator sut = new JudicialRefreshOrchestrator(
            retrieveDataService,
            parseRequestService,
            judicialBookingService,
            requestMappingService);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void refreshJudicialRoleAssignmentRecords_emptyUserRequest() {
        UserRequest request = UserRequest.builder().build();
        Assert.assertThrows(BadRequestException.class, () -> sut.judicialRefresh(request));
    }

    @Test
    void refreshJudicialRoleAssignmentRecords_nullUserRequest() {
        Assert.assertThrows(BadRequestException.class, () -> sut.judicialRefresh(null));
    }

    @Test
    void refreshJudicialRoleAssignmentRecords_invalidUserRequest() {
        UserRequest request = UserRequest.builder().userIds(List.of("1234-?4567")).build();
        Assert.assertThrows(BadRequestException.class, () -> sut.judicialRefresh(request));
    }

    @Test
    void refreshJudicialRoleAssignmentRecords_emptyUserIds() {
        UserRequest request = UserRequest.builder().userIds(List.of("1234-4567", "")).build();
        Assert.assertThrows(BadRequestException.class, () -> sut.judicialRefresh(request));
    }

    @Test
    @SuppressWarnings("unchecked")
    void refreshJudicialRoleAssignmentRecords() throws IOException {
        String userId = "21334a2b-79ce-44eb-9168-2d49a744be9d";

        Map<String, Set<?>> userAccessProfiles = new HashMap<>();
        JudicialProfile judicialProfile = JudicialProfile.builder()
                .sidamId(userId)
                .appointments(List.of(Appointment.builder().build()))
                .authorisations(List.of(Authorisation.builder().build()))
                .build();
        userAccessProfiles.put(userId, Set.of(judicialProfile));

        Mockito.when(retrieveDataService.retrieveProfiles(any(), eq(UserType.JUDICIAL)))
                .thenReturn(userAccessProfiles);

        List<JudicialBooking> bookingsList = List.of(TestDataBuilder.buildJudicialBooking());
        Mockito.when(judicialBookingService.fetchJudicialBookings(any())).thenReturn(bookingsList);

        Mockito.when(requestMappingService.createAssignments(any(), any(), eq(UserType.JUDICIAL)))
                .thenReturn((ResponseEntity.status(HttpStatus.OK)
                        .body(List.of(ResponseEntity.status(HttpStatus.CREATED)
                                .body(new RoleAssignmentRequestResource(AssignmentRequest.builder().build()))))));

        ResponseEntity<Object> response = sut.judicialRefresh(TestDataBuilder.buildUserRequest());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response);
    }

    @Test
    @SuppressWarnings("unchecked")
    void refreshJudicialRoleAssignmentRecords_ras422() {

        String userId = "21334a2b-79ce-44eb-9168-2d49a744be9d";

        Map<String, Set<?>> userAccessProfiles = new HashMap<>();
        JudicialProfile judicialProfile = JudicialProfile.builder()
                .sidamId(userId)
                .appointments(List.of(Appointment.builder().build()))
                .authorisations(List.of(Authorisation.builder().build()))
                .build();
        userAccessProfiles.put(userId, Set.of(judicialProfile));

        Mockito.when(retrieveDataService.retrieveProfiles(any(), eq(UserType.JUDICIAL)))
                .thenReturn(userAccessProfiles);

        List<JudicialBooking> bookingsList =
                List.of(JudicialBooking.builder().userId(userId).endTime(ZonedDateTime.now().plusDays(5)).build());
        Mockito.when(judicialBookingService.fetchJudicialBookings(any())).thenReturn(bookingsList);

        Mockito.when(requestMappingService.createAssignments(any(), any(), eq(UserType.JUDICIAL)))
                .thenReturn((ResponseEntity.status(HttpStatus.OK)
                        .body(List.of(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                                .body(new RoleAssignmentRequestResource(AssignmentRequest.builder().build()))))));

        ResponseEntity<Object> response = sut.judicialRefresh(TestDataBuilder.buildUserRequest());

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response);
    }

    @Test
    @SuppressWarnings("unchecked")
    void refreshJudicialRoleAssignmentRecords_emptyJrdProfiles() {

        Mockito.when(retrieveDataService.retrieveProfiles(any(), eq(UserType.JUDICIAL)))
                .thenReturn(Collections.emptyMap());
        Mockito.when(judicialBookingService.fetchJudicialBookings(any()))
                .thenReturn(Collections.emptyList());

        Mockito.when(requestMappingService.createAssignments(any(), eq(Collections.emptyList()), eq(UserType.JUDICIAL)))
                .thenReturn((ResponseEntity.status(HttpStatus.OK)
                        .body(List.of(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                                .body(new RoleAssignmentRequestResource(AssignmentRequest.builder().build()))))));

        ResponseEntity<Object> response = sut.judicialRefresh(TestDataBuilder.buildUserRequest());

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response);
    }

    @Test
    @SuppressWarnings("unchecked")
    void refreshJudicialRoleAssignmentRecords_emptyJudicialBookings() {
        String userId = "21334a2b-79ce-44eb-9168-2d49a744be9d";

        Map<String, Set<?>> userAccessProfiles = new HashMap<>();
        JudicialProfile judicialProfile = JudicialProfile.builder()
                .sidamId(userId)
                .appointments(List.of(Appointment.builder().build()))
                .authorisations(List.of(Authorisation.builder().build()))
                .build();
        userAccessProfiles.put(userId, Set.of(judicialProfile));

        Mockito.when(retrieveDataService.retrieveProfiles(any(), eq(UserType.JUDICIAL)))
                .thenReturn(userAccessProfiles);

        Mockito.when(judicialBookingService.fetchJudicialBookings(any()))
                .thenReturn(Collections.emptyList());

        Mockito.when(requestMappingService.createAssignments(any(), eq(Collections.emptyList()), eq(UserType.JUDICIAL)))
                .thenReturn((ResponseEntity.status(HttpStatus.OK)
                        .body(List.of(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                                .body(new RoleAssignmentRequestResource(AssignmentRequest.builder().build()))))));

        ResponseEntity<Object> response = sut.judicialRefresh(TestDataBuilder.buildUserRequest());

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response);
    }
}
