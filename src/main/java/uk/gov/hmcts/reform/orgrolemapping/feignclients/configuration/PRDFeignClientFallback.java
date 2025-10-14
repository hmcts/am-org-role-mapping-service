package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.DeleteOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUserResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationCreationRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfileUpdatedData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;

import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.GET_REFRESH_USERS_SAMPLE_MULTI_USER;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.GET_REFRESH_USERS_SAMPLE_SINGLE_USER;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.ORGANISATIONS_BY_PROFILE_IDS_SAMPLE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.ORGANISATION_SAMPLE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.RETRIEVE_ORGANISATIONS_SAMPLE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.USERS_BY_ORGANISATION_SAMPLE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.buildDeleteOrganisationResponse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.buildGetRefreshUsersResponse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.buildOrganisationByProfileIdsResponse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.buildOrganisationResponse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.buildOrganisationsResponse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.buildUsersByOrganisationResponse;

@Component
public class PRDFeignClientFallback implements PRDFeignClient {

    public static final String PRD_API_NOT_AVAILABLE = "The PRD API Service is not available";

    @Override
    public String getServiceStatus() {
        return PRD_API_NOT_AVAILABLE;
    }

    @Override
    public ResponseEntity<OrganisationByProfileIdsResponse> getOrganisationsByProfileIds(
            Integer pageSize, String searchAfter, OrganisationByProfileIdsRequest organisationByProfileIdsRequest) {
        return ResponseEntity.ok(buildOrganisationByProfileIdsResponse(ORGANISATIONS_BY_PROFILE_IDS_SAMPLE));
    }

    @Override
    public ResponseEntity<GetRefreshUserResponse> getRefreshUsers(String userId,
                                                                  String lastUpdatedSince,
                                                                  Integer pageSize,
                                                                  String searchAfter) {
        if (userId != null) {
            return ResponseEntity.ok(buildGetRefreshUsersResponse(GET_REFRESH_USERS_SAMPLE_SINGLE_USER, userId));
        }
        return ResponseEntity.ok(buildGetRefreshUsersResponse(GET_REFRESH_USERS_SAMPLE_MULTI_USER));
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
    public ResponseEntity<OrganisationResponse> createOrganisation(
            OrganisationCreationRequest organisationCreationRequest) {
        return ResponseEntity.ok(buildOrganisationResponse(ORGANISATION_SAMPLE));
    }

    @Override
    public ResponseEntity<OrganisationResponse> updatesOrganisation(
            OrganisationCreationRequest organisationCreationRequest,
            String organisationIdentifier, String userId) {
        return ResponseEntity.ok(buildOrganisationResponse(ORGANISATION_SAMPLE));
    }

    @Override
    public ResponseEntity<OrganisationResponse> modifyRolesForExistingUserOfOrganisation(
            UserProfileUpdatedData userProfileUpdatedData,
            String orgId,
            String userId,
            String origin) {
        return ResponseEntity.ok(buildOrganisationResponse(ORGANISATION_SAMPLE));
    }

    @Override
    public ResponseEntity<String> addUserToOrganisation(
            String organisationId,
            String userId) {
        return ResponseEntity.ok("User Added To Organisation");
    }

    @Override
    public ResponseEntity<DeleteOrganisationResponse> deleteOrganisation(
            String organisationId) {
        return ResponseEntity.ok(buildDeleteOrganisationResponse(
                HttpStatus.OK.value(),
                "Organisation Deleted"));
    }

}
