package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBookingRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBookingResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JBSFeignClient;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class JudicialBookingServiceTest {

    JBSFeignClient feignClient = mock(JBSFeignClient.class);

    JudicialBookingService sut = new JudicialBookingService(feignClient);

    @Test
    void fetchJudicialBookings() {

        // GIVEN
        UserRequest userRequest = UserRequest.builder()
                .userIds(Arrays.asList("123e4567-e89b-42d3-a456-556642445000", "123e4567-e89b-42d3-a456-556642445111"))
                .build();
        JudicialBookingRequest bookingRequest = new JudicialBookingRequest(userRequest);
        List<JudicialBooking> bookings = List.of(
                JudicialBooking.builder().userId(userRequest.getUserIds().get(0))
                        .endTime(ZonedDateTime.now().plusDays(5))
                        .build());
        doReturn(ResponseEntity.status(HttpStatus.OK).body(new JudicialBookingResponse(bookings))).when(feignClient)
                .getJudicialBookingByUserIds(bookingRequest);

        // WHEN
        List<JudicialBooking> responseEntity = sut.fetchJudicialBookings(userRequest);

        // THEN
        assertEquals(1, responseEntity.size());
    }

    @Test
    void fetchJudicialBookings_emptyUserIds() {

        // GIVEN
        UserRequest userRequest = UserRequest.builder().userIds(List.of("")).build();
        JudicialBookingRequest bookingRequest = new JudicialBookingRequest(userRequest);
        doThrow(BadRequestException.class).when(feignClient).getJudicialBookingByUserIds(bookingRequest);

        // WHEN / THEN
        assertThrows(BadRequestException.class, () -> sut.fetchJudicialBookings(userRequest));
    }

    @Test
    void fetchJudicialBookings_emptyResponse() {

        // GIVEN
        UserRequest userRequest = UserRequest.builder().userIds(List.of("")).build();
        JudicialBookingRequest bookingRequest = new JudicialBookingRequest(userRequest);
        doReturn(ResponseEntity.status(HttpStatus.OK).body(new JudicialBookingResponse(Collections.emptyList())))
                .when(feignClient).getJudicialBookingByUserIds(bookingRequest);

        // WHEN
        List<JudicialBooking> responseEntity = sut.fetchJudicialBookings(userRequest);

        // THEN
        assertEquals(0, responseEntity.size());
    }

    @Test
    void testFetchJudicialBookingsInBatchesEmptyUsers() {

        // GIVEN
        List<String> userIds = Collections.emptyList();
        String batchSize = "400";

        // WHEN
        List<JudicialBooking> judBookings = sut.fetchJudicialBookingsInBatches(userIds, batchSize);

        // THEN
        assertEquals(0, judBookings.size());
    }

    @Test
    void testFetchJudicialBookingsInBatchesWithUsers() {

        // GIVEN
        List<String> userIds = Arrays.asList("1","2");

        List<JudicialBooking> expectedBookings = new ArrayList<>();
        expectedBookings.add(JudicialBooking.builder().userId("1").build());
        expectedBookings.add(JudicialBooking.builder().userId("2").build());

        UserRequest userRequest = UserRequest.builder().userIds(List.of("1","2")).build();
        JudicialBookingRequest bookingRequest = new JudicialBookingRequest(userRequest);

        doReturn(ResponseEntity.status(HttpStatus.OK).body(new JudicialBookingResponse(expectedBookings)))
                .when(feignClient).getJudicialBookingByUserIds(bookingRequest);

        String batchSize = "400";

        // WHEN
        List<JudicialBooking> actualBookings = sut.fetchJudicialBookingsInBatches(userIds, batchSize);

        // THEN
        assertEquals(expectedBookings.size(), actualBookings.size());
        assertTrue(actualBookings.containsAll(expectedBookings));
    }

    @Test
    void testFetchJudicialBookingsInBatchesWithUsersIn3Batches() {

        // GIVEN
        List<JudicialBooking> expectedBookings1 = new ArrayList<>();
        expectedBookings1.add(JudicialBooking.builder().userId("1").build());
        expectedBookings1.add(JudicialBooking.builder().userId("2").build());

        List<JudicialBooking> expectedBookings2 = new ArrayList<>();
        expectedBookings2.add(JudicialBooking.builder().userId("3").build());
        expectedBookings2.add(JudicialBooking.builder().userId("4").build());

        List<JudicialBooking> expectedBookings3 = new ArrayList<>();
        expectedBookings3.add(JudicialBooking.builder().userId("5").build());

        UserRequest userRequest1 = UserRequest.builder().userIds(List.of("1","2")).build();
        JudicialBookingRequest bookingRequest1 = new JudicialBookingRequest(userRequest1);

        UserRequest userRequest2 = UserRequest.builder().userIds(List.of("3","4")).build();
        JudicialBookingRequest bookingRequest2 = new JudicialBookingRequest(userRequest2);

        UserRequest userRequest3 = UserRequest.builder().userIds(List.of("5")).build();
        JudicialBookingRequest bookingRequest3 = new JudicialBookingRequest(userRequest3);

        doReturn(ResponseEntity.status(HttpStatus.OK).body(new JudicialBookingResponse(expectedBookings1)))
                .when(feignClient).getJudicialBookingByUserIds(bookingRequest1);

        doReturn(ResponseEntity.status(HttpStatus.OK).body(new JudicialBookingResponse(expectedBookings2)))
                .when(feignClient).getJudicialBookingByUserIds(bookingRequest2);

        doReturn(ResponseEntity.status(HttpStatus.OK).body(new JudicialBookingResponse(expectedBookings3)))
                .when(feignClient).getJudicialBookingByUserIds(bookingRequest3);

        String batchSize = "2";
        List<String> userIds = Arrays.asList("1","2","3","4","5");

        // WHEN
        List<JudicialBooking> actualBookings = sut.fetchJudicialBookingsInBatches(userIds, batchSize);

        // THEN
        assertEquals(5, actualBookings.size());
        assertTrue(actualBookings.containsAll(expectedBookings1));
        assertTrue(actualBookings.containsAll(expectedBookings2));
        assertTrue(actualBookings.containsAll(expectedBookings3));
    }

    @Test
    void testFetchJudicialBookingsInBatchesWithUsersInBatches_butNoBookingsFound() {

        // GIVEN
        List<JudicialBooking> expectedBookings1 = Collections.emptyList(); // i.e. no bookings

        List<JudicialBooking> expectedBookings2 = new ArrayList<>();
        expectedBookings2.add(JudicialBooking.builder().userId("3").build());
        expectedBookings2.add(JudicialBooking.builder().userId("4").build());

        List<JudicialBooking> expectedBookings3 = Collections.emptyList(); // i.e. no bookings

        UserRequest userRequest1 = UserRequest.builder().userIds(List.of("1","2")).build();
        JudicialBookingRequest bookingRequest1 = new JudicialBookingRequest(userRequest1);

        UserRequest userRequest2 = UserRequest.builder().userIds(List.of("3","4")).build();
        JudicialBookingRequest bookingRequest2 = new JudicialBookingRequest(userRequest2);

        UserRequest userRequest3 = UserRequest.builder().userIds(List.of("5")).build();
        JudicialBookingRequest bookingRequest3 = new JudicialBookingRequest(userRequest3);

        doReturn(ResponseEntity.status(HttpStatus.OK).body(new JudicialBookingResponse(expectedBookings1)))
                .when(feignClient).getJudicialBookingByUserIds(bookingRequest1);

        doReturn(ResponseEntity.status(HttpStatus.OK).body(new JudicialBookingResponse(expectedBookings2)))
                .when(feignClient).getJudicialBookingByUserIds(bookingRequest2);

        doReturn(ResponseEntity.status(HttpStatus.OK).body(new JudicialBookingResponse(expectedBookings3)))
                .when(feignClient).getJudicialBookingByUserIds(bookingRequest3);

        String batchSize = "2";
        List<String> userIds = Arrays.asList("1","2","3","4","5");

        // WHEN
        List<JudicialBooking> actualBookings = sut.fetchJudicialBookingsInBatches(userIds, batchSize);

        // THEN
        // NB: only the bookings from 2nd call will be returned
        assertEquals(2, actualBookings.size());
        assertTrue(actualBookings.containsAll(expectedBookings2));
    }

    @Test
    void testFetchJudicialBookingsInBatchesWithUsersNoBatchSize() {

        // GIVEN
        List<String> userIds = Arrays.asList("1","2","3","4","5");

        List<JudicialBooking> expectedBookings = new ArrayList<>();
        expectedBookings.add(JudicialBooking.builder().userId("1").build());
        expectedBookings.add(JudicialBooking.builder().userId("2").build());
        expectedBookings.add(JudicialBooking.builder().userId("3").build());
        expectedBookings.add(JudicialBooking.builder().userId("4").build());
        expectedBookings.add(JudicialBooking.builder().userId("5").build());

        UserRequest userRequest = UserRequest.builder().userIds(List.of("1","2","3","4","5")).build();
        JudicialBookingRequest bookingRequest = new JudicialBookingRequest(userRequest);

        doReturn(ResponseEntity.status(HttpStatus.OK).body(new JudicialBookingResponse(expectedBookings)))
                .when(feignClient).getJudicialBookingByUserIds(bookingRequest);

        // WHEN
        List<JudicialBooking> actualBookings = sut.fetchJudicialBookingsInBatches(userIds,null);

        // THEN
        assertEquals(5, actualBookings.size());
        assertTrue(actualBookings.containsAll(expectedBookings));
    }

}
