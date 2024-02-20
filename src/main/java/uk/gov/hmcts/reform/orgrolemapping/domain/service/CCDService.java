package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypesResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CCDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.CCDFeignClientFallback;

@Service
@AllArgsConstructor
public class CCDService {

    private final CCDFeignClient ccdFeignClient;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 3))
    public ResponseEntity<AccessTypesResponse> fetchAccessTypes() {
        return ccdFeignClient.getAccessTypes();
    }
}
