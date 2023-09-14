package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBookingRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBookingResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JBSFeignClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class JudicialBookingService {

    private final JBSFeignClient jbsFeignClient;


    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 3))
    public List<JudicialBooking> fetchJudicialBookings(UserRequest userRequest) {
        JudicialBookingResponse response =
                jbsFeignClient.getJudicialBookingByUserIds(new JudicialBookingRequest(userRequest)).getBody();

        return Objects.requireNonNullElse(response, JudicialBookingResponse.builder().build()).getBookings();
    }

    //New service takes List of USERIDs and batchSize configured in refresh.job.pageSize
    public List<JudicialBooking> fetchJudicialBookingsInBatches(List<String> userIds,String batchSize) {
        log.info(" fetching Judicial Bookings for userIds {} and the batchSize is {}", userIds,batchSize);
        List<JudicialBooking> judicialBookings = new ArrayList<>();
        final int defaultBatchSize = Integer.getInteger(batchSize);
        AtomicInteger counter = new AtomicInteger();

        userIds
                .stream()
                .collect(Collectors.groupingBy(gr -> counter.getAndIncrement() / defaultBatchSize))
                .values()
                .forEach(batchUserIds -> {
                    UserRequest userRequest = UserRequest.builder().userIds(batchUserIds).build();
                    judicialBookings.addAll(fetchJudicialBookings(userRequest));
                });

        return judicialBookings;
    }

}
