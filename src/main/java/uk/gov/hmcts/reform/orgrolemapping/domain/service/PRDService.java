package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUsersResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;

@Service
@AllArgsConstructor
public class PRDService {

    private final PRDFeignClient prdFeignClient;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 3))
    public ResponseEntity<GetRefreshUsersResponse> getRefreshUser(String userId) {
        return prdFeignClient.getRefreshUsers(userId);
    }
}
