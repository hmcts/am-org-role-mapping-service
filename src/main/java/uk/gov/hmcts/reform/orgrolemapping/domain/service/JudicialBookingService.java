package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.JBSFeignClientFallback;

import java.util.List;

@Service
@AllArgsConstructor
public class JudicialBookingService {

    private final JBSFeignClientFallback jbsFeignClient;


    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 3))
    public <T> ResponseEntity<List<T>> fetchJudicialBookings(UserRequest userRequest) {
        return jbsFeignClient.getJudicialBookingByUserIds(userRequest);
    }
}
