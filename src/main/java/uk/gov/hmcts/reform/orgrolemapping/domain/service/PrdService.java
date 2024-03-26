package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUsersResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationsResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;

import java.util.ArrayList;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PrdService {

    private final PRDFeignClient prdFeignClient;
    //Uncomment this to run with your stubs
    // for testing purpose private final PRDFeignClientFallback prdFeignClient;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 3))
    public ResponseEntity<OrganisationByProfileIdsResponse> fetchOrganisationsByProfileIds(
            Integer pageSize, String searchAfter, OrganisationByProfileIdsRequest organisationByProfileIdsRequest) {
        return prdFeignClient.getOrganisationsByProfileIds(pageSize, searchAfter, organisationByProfileIdsRequest);
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 3))
    public ResponseEntity<OrganisationsResponse> retrieveOrganisations(
            String lastUpdatedSince, int page, int size) {

        try {
            return prdFeignClient.retrieveOrganisations(null, lastUpdatedSince, null, page, size);
        } catch (FeignException feignException) {
            if (feignException.status() != 404) {
                throw feignException;
            } else {
                OrganisationsResponse emptyOrg = new OrganisationsResponse(
                        new ArrayList<>(), false);
                        new ArrayList<OrganisationInfo>(), false);
                return ResponseEntity.of(Optional.of(emptyOrg));
            }
        }
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 3))
    public ResponseEntity<GetRefreshUsersResponse> getRefreshUser(String userId) {
        return prdFeignClient.getRefreshUsers(userId);
    }
}
