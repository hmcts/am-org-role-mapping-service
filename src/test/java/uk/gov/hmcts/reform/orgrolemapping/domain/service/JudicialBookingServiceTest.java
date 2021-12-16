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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class JudicialBookingServiceTest {


    JBSFeignClient feignClient = mock(JBSFeignClient.class);

    JudicialBookingService sut = new JudicialBookingService(feignClient);

    @Test
    void fetchJudicialBookings() {
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

        List<JudicialBooking> responseEntity = sut.fetchJudicialBookings(userRequest);

        assertEquals(1, responseEntity.size());
    }

    @Test
    void fetchJudicialBookings_emptyUserIds() {

        UserRequest userRequest = UserRequest.builder().userIds(List.of("")).build();
        JudicialBookingRequest bookingRequest = new JudicialBookingRequest(userRequest);
        doThrow(BadRequestException.class).when(feignClient).getJudicialBookingByUserIds(eq(bookingRequest));

        assertThrows(BadRequestException.class, () -> sut.fetchJudicialBookings(userRequest));
    }

    @Test
    void fetchJudicialBookings_emptyResponse() {

        UserRequest userRequest = UserRequest.builder().userIds(List.of("")).build();
        JudicialBookingRequest bookingRequest = new JudicialBookingRequest(userRequest);
        doReturn(ResponseEntity.status(HttpStatus.OK).body(new JudicialBookingResponse(Collections.emptyList())))
                .when(feignClient).getJudicialBookingByUserIds(eq(bookingRequest));

        List<JudicialBooking> responseEntity = sut.fetchJudicialBookings(userRequest);

        assertEquals(0, responseEntity.size());
    }
}
