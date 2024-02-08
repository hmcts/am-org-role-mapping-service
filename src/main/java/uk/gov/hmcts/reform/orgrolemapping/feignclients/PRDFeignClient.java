package uk.gov.hmcts.reform.orgrolemapping.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationStaleProfilesRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationStaleProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientInterceptor;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.PRDFeignClientFallback;

@FeignClient(value = "prdClient", url = "${feign.client.config.prdClient.url}",
        configuration = {FeignClientConfiguration.class, FeignClientInterceptor.class},
        fallback = PRDFeignClientFallback.class
)
public interface PRDFeignClient {

    @PostMapping(value = "/refdata/internal/v1/getOrganisationsByProfile")
    ResponseEntity<OrganisationStaleProfilesResponse> getOrganisationStaleProfiles(
            @RequestHeader(name = "pageSize") Integer pageSize,
            @RequestHeader(name = "searchAfter") String searchAfter,
            @RequestBody OrganisationStaleProfilesRequest organisationStaleProfilesRequest
    );

    @GetMapping(value = "/refdata/internal/v1/organisations")
    ResponseEntity<OrganisationProfilesResponse> retrieveOrganisations(
            @RequestParam(name = "id") String id,
            @RequestParam(name = "since") String lastUpdatedSince,
            @RequestParam(name = "status") String status,
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "size") Integer size
    );
}
