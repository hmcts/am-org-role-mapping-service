package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUserResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;

import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.ORGANISATIONS_BY_PROFILE_IDS_SAMPLE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.RETRIEVE_ORGANISATIONS_SAMPLE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.RETRIEVE_USERS_SAMPLE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.USERS_BY_ORGANISATION_SAMPLE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.buildOrganisationsResponse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.buildOrganisationByProfileIdsResponse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.buildRefreshUserResponse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.buildUsersByOrganisationResponse;

@Component
public class PRDFeignClientFallback implements PRDFeignClient {

    @Override
    public ResponseEntity<OrganisationByProfileIdsResponse> getOrganisationsByProfileIds(
            Integer pageSize, String searchAfter, OrganisationByProfileIdsRequest organisationByProfileIdsRequest) {
        return ResponseEntity.ok(buildOrganisationByProfileIdsResponse(ORGANISATIONS_BY_PROFILE_IDS_SAMPLE));
    }

    @Override
    public ResponseEntity<OrganisationsResponse> retrieveOrganisations(
            String id, String lastUpdatedSince, String status, Integer page, Integer size) {
        return ResponseEntity.ok(buildOrganisationsResponse(RETRIEVE_ORGANISATIONS_SAMPLE));
    }

    @Override
    public ResponseEntity<UsersByOrganisationResponse> getUsersByOrganisation(Integer pageSize,
                                  String searchAfterOrg, String searchAfterUser,
                                  UsersByOrganisationRequest usersByOrganisationRequest) {
        return ResponseEntity.ok(buildUsersByOrganisationResponse(USERS_BY_ORGANISATION_SAMPLE));
    }

    @Override
    public ResponseEntity<GetRefreshUserResponse> retrieveUsers(
            String lastUpdatedSince, Integer pageSize, String searchAfter) {
        return ResponseEntity.ok(buildRefreshUserResponse(RETRIEVE_USERS_SAMPLE));
    }

}
