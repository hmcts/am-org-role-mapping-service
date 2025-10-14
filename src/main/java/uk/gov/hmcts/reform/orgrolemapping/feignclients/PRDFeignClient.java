package uk.gov.hmcts.reform.orgrolemapping.feignclients;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.PRDFeignClientFallback;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.RdFeignClientInterceptor;

@FeignClient(value = "prdClient", url = "${feign.client.config.prdClient.url}",
        configuration = {FeignClientConfiguration.class, RdFeignClientInterceptor.class},
        fallback = PRDFeignClientFallback.class)

public interface PRDFeignClient {

    @GetMapping(value = "/")
    String getServiceStatus();

    @PostMapping(value = "/refdata/internal/v1/organisations/getOrganisationsByProfile")
    ResponseEntity<OrganisationByProfileIdsResponse> getOrganisationsByProfileIds(
            @RequestParam(name = "pageSize") Integer pageSize,
            @RequestParam(name = "searchAfter") String searchAfter,
            @RequestBody OrganisationByProfileIdsRequest organisationByProfileIdsRequest
    );

    @GetMapping(value = "/refdata/internal/v1/organisations/users")
    ResponseEntity<GetRefreshUserResponse> getRefreshUsers(
            @RequestParam(name = "userId") String userId,
            @RequestParam(name = "since") String lastUpdatedSince,
            @RequestParam(name = "pageSize") Integer pageSize,
            @RequestParam(name = "searchAfter") String searchAfter
    );

    @GetMapping(value = "/refdata/internal/v1/organisations")
    ResponseEntity<OrganisationsResponse> retrieveOrganisations(
            @RequestParam(name = "id") String id,
            @RequestParam(name = "since") String lastUpdatedSince,
            @RequestParam(name = "status") String status,
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "size") Integer size
    );

    @PostMapping(value = "/refdata/internal/v2/organisations/users")
    ResponseEntity<UsersByOrganisationResponse> getUsersByOrganisation(
            @RequestParam(name = "pageSize") Integer pageSize,
            @RequestParam(name = "searchAfterOrg") String searchAfterOrg,
            @RequestParam(name = "searchAfterUser") String searchAfterUser,
            @RequestBody UsersByOrganisationRequest usersByOrganisationRequest
    );

    @PostMapping(value = "/refdata/internal/v1/organisations")
    ResponseEntity<OrganisationResponse> createOrganisation(
            @RequestBody OrganisationCreationRequest organisationCreationRequest
    );

    @PutMapping(value = "/refdata/internal/v1/organisations/{orgId}")
    ResponseEntity<OrganisationResponse> updatesOrganisation(
        @RequestBody OrganisationCreationRequest organisationCreationRequest,
        @RequestParam(name = "orgId") String organisationIdentifier,
        @RequestParam(name = "userId")  String userId
    );

    @PutMapping(value = "/refdata/internal/v1/organisations/{orgId}/users/{userId}")
    ResponseEntity<OrganisationResponse> modifyRolesForExistingUserOfOrganisation(
        @RequestBody UserProfileUpdatedData userProfileUpdatedData,
        @RequestParam(name = "orgId") String orgId,
        @RequestParam(name = "userId")  String userId,
        @RequestParam(name = "origin", required = false, defaultValue = "EXUI") String origin
    );


    @PostMapping(value = "/refdata/internal/v1/organisations/{orgId}/users}")
    ResponseEntity<String> addUserToOrganisation(
            @RequestParam(name = "orgId") String organisationId,
            @RequestParam(name = "userId") String userId
    );

    @DeleteMapping(value = "/refdata/internal/v1/organisations/deleteOrganisation")
    ResponseEntity<DeleteOrganisationResponse> deleteOrganisation(
            @RequestParam(name = "orgId") String organisationId
    );
}
