package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;

import static uk.gov.hmcts.reform.orgrolemapping.helper.OrganisationBuilder.buildOrganisationProfileResponse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.OrganisationBuilder.buildOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;

import static uk.gov.hmcts.reform.orgrolemapping.helper.ProfessionalUserBuilder.buildUsersByOrganisationResponse;

@Component
public class PRDFeignClientFallback implements PRDFeignClient {

    @Override
    public ResponseEntity<OrganisationByProfileIdsResponse> getOrganisationsByProfileIds(
            Integer pageSize, String searchAfter, OrganisationByProfileIdsRequest organisationByProfileIdsRequest) {
        return ResponseEntity.ok(buildOrganisationResponse("organisationSample.json"));
    }

    @Override
    public ResponseEntity<OrganisationsResponse> retrieveOrganisations(
            String id, String lastUpdatedSince, String status, Integer page, Integer size) {
        return ResponseEntity.ok(buildOrganisationProfileResponse("organisationsResponseSample.json"));
    }

    @Override
    public ResponseEntity<UsersByOrganisationResponse> getUsersByOrganisation(Integer pageSize,
                                  String searchAfterOrg, String searchAfterUser,
                                  UsersByOrganisationRequest usersByOrganisationRequest) {
        return ResponseEntity.ok(buildUsersByOrganisationResponse("usersByOrganisationSample.json"));
    }
}