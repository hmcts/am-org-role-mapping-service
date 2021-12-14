package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.AllArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBookingRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBookingResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JBSFeignClient;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class JudicialBookingService {

    private final JBSFeignClient jbsFeignClient;


    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 3))
    public List<JudicialBooking> fetchJudicialBookings(UserRequest userRequest) {
        JudicialBookingResponse response =
                jbsFeignClient.getJudicialBookingByUserIds(new JudicialBookingRequest(userRequest)).getBody();

        return Objects.requireNonNullElse(response, JudicialBookingResponse.builder().build()).getBookings();
    }
}
