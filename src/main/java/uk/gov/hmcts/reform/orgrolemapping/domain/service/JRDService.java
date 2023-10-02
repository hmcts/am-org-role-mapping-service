package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JRDUserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JRDFeignClient;

import java.util.List;

@Service
@AllArgsConstructor
public class JRDService {

    private final JRDFeignClient jrdFeignClient;

    // page_size from jrd mapped to appointment, one profile may have upto 5 appointments
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 3))
    public <T> ResponseEntity<List<T>> fetchJudicialProfiles(JRDUserRequest userRequest) {
        return jrdFeignClient.getJudicialDetailsById(userRequest, userRequest.getSidamIds().size() * 5);
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 3))
    public <T> ResponseEntity<List<T>> fetchJudicialDetailsByServiceName(String serviceName,
                                                                           Integer pageSize,
                                                                           Integer pageNumber,
                                                                           String sortDirection,
                                                                           String sortColumn) {
        JRDUserRequest userRequest = JRDUserRequest.builder().ccdServiceNames(serviceName).build();
        return jrdFeignClient.getJudicialDetailsByServiceName(userRequest, pageSize,
                pageNumber, sortDirection, sortColumn);

    }

}
