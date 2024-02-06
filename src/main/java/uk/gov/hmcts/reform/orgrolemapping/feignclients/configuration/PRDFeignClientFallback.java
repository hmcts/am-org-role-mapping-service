package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationStaleProfilesRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationStaleProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;

import static uk.gov.hmcts.reform.orgrolemapping.helper.OrganisationBuilder.buildOrganisationResponse;

@Component
public class PRDFeignClientFallback implements PRDFeignClient {

    @Override
    public ResponseEntity<OrganisationStaleProfilesResponse> getOrganisationStaleProfiles(
            Integer pageSize, String searchAfter, OrganisationStaleProfilesRequest organisationStaleProfilesRequest) {
        return ResponseEntity.ok(buildOrganisationResponse("organisationSample.json"));
    }
}
