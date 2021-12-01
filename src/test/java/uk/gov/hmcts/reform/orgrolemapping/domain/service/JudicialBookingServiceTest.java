package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.JBSFeignClientFallback;

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


    JBSFeignClientFallback feignClient = mock(JBSFeignClientFallback.class);

    JudicialBookingService sut = new JudicialBookingService(feignClient);

    @Test
    void fetchJudicialBookings() {
        UserRequest userRequest = UserRequest.builder()
                .userIds(Arrays.asList("123e4567-e89b-42d3-a456-556642445000", "123e4567-e89b-42d3-a456-556642445111"))
                .build();

        List<JudicialBooking> bookings = List.of(
                JudicialBooking.builder().userId(userRequest.getUserIds().get(0))
                        .endTime(ZonedDateTime.now().plusDays(5))
                        .build());
        doReturn(ResponseEntity.status(HttpStatus.OK).body(bookings)).when(feignClient)
                .getJudicialBookingByUserIds(userRequest);

        ResponseEntity<List<JudicialBooking>> responseEntity = sut.fetchJudicialBookings(userRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void fetchJudicialBookings_emptyUserIds() {

        UserRequest userRequest = UserRequest.builder().userIds(List.of("")).build();
        doThrow(BadRequestException.class).when(feignClient).getJudicialBookingByUserIds(eq(userRequest));

        assertThrows(BadRequestException.class, () -> sut.fetchJudicialBookings(userRequest));
    }

    @Test
    void fetchJudicialBookings_emptyResponse() {

        UserRequest userRequest = UserRequest.builder().userIds(List.of("")).build();
        doReturn(ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList()))
                .when(feignClient).getJudicialBookingByUserIds(eq(userRequest));

        ResponseEntity<List<JudicialBooking>> responseEntity = sut.fetchJudicialBookings(userRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
