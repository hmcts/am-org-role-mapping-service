package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;

import java.util.ArrayList;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PrdService {

    private final PRDFeignClient prdFeignClient;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 3))
    public ResponseEntity<OrganisationByProfileIdsResponse> fetchOrganisationsByProfileIds(
            Integer pageSize, String searchAfter, OrganisationByProfileIdsRequest organisationByProfileIdsRequest) {
        return prdFeignClient.getOrganisationsByProfileIds(pageSize, searchAfter, organisationByProfileIdsRequest);
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 3))
    public ResponseEntity<OrganisationProfilesResponse> retrieveOrganisations(
            String lastUpdatedSince, int page, int size) {

        try {
            return prdFeignClient.retrieveOrganisations(null, lastUpdatedSince, null, page, size);
        } catch (FeignException feignException) {
            if (feignException.status() != 404) {
                throw feignException;
            } else {
                OrganisationProfilesResponse emptyOrg = new OrganisationProfilesResponse(
                        new ArrayList<OrganisationInfo>(), false);
                return ResponseEntity.of(Optional.of(emptyOrg));
            }
        }
    }
}
