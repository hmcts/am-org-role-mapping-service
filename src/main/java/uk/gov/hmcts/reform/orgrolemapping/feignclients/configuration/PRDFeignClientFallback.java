package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUsersResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationsResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;

import static uk.gov.hmcts.reform.orgrolemapping.helper.OrganisationBuilder.buildOrganisationProfileResponse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.OrganisationBuilder.buildOrganisationResponse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.ProfessionalRefreshUserBuilder.buildGetRefreshUsersResponse;

@Component
public class PRDFeignClientFallback implements PRDFeignClient {

    public static final String PRD_API_NOT_AVAILABLE = "The PRD API Service is not available";

    @Override
    public String getServiceStatus() {
        return PRD_API_NOT_AVAILABLE;
    }

    @Override
    public ResponseEntity<GetRefreshUsersResponse> getRefreshUsers(String userId) {
        return buildGetRefreshUsersResponse("prdRefreshUserSample138.json", userId);
    }

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
}
