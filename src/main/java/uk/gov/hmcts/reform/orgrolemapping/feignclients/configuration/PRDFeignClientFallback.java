package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;

import static uk.gov.hmcts.reform.orgrolemapping.helper.OrganisationBuilder.buildOrganisationResponse;

@Component
public class PRDFeignClientFallback implements PRDFeignClient {

    @Override
    public ResponseEntity<OrganisationByProfileIdsResponse> getOrganisationsByProfileIds(
            Integer pageSize, String searchAfter, OrganisationByProfileIdsRequest organisationByProfileIdsRequest) {
        return ResponseEntity.ok(buildOrganisationResponse("organisationSample.json"));
    }
}
