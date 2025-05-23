package uk.gov.hmcts.reform.orgrolemapping.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.PRDFeignClientFallback;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.RdFeignClientInterceptor;

@FeignClient(value = "prdClient", url = "${feign.client.config.prdClient.url}",
        configuration = {FeignClientConfiguration.class, RdFeignClientInterceptor.class},
        fallback = PRDFeignClientFallback.class
)
public interface PRDFeignClient {

    @PostMapping(value = "/refdata/internal/v1/organisations/getOrganisationsByProfile")
    ResponseEntity<OrganisationByProfileIdsResponse> getOrganisationsByProfileIds(
            @RequestParam(name = "pageSize") Integer pageSize,
            @RequestParam(name = "searchAfter") String searchAfter,
            @RequestBody OrganisationByProfileIdsRequest organisationByProfileIdsRequest
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
}