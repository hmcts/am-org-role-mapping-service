package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;

@Service
@AllArgsConstructor
public class PrdService {

    private final PRDFeignClient prdFeignClient;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 3))
    public ResponseEntity<UsersByOrganisationResponse> fetchUsersByOrganisation(
            Integer pageSize,
            String searchAfterOrg,
            String searchAfterUser,
            UsersByOrganisationRequest usersByOrganisationRequest) {
        return prdFeignClient.getUsersByOrganisation(pageSize, searchAfterOrg, searchAfterUser,
                usersByOrganisationRequest);
    }
}
