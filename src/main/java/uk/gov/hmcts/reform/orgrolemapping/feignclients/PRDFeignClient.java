package uk.gov.hmcts.reform.orgrolemapping.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PostMapping(value = "/refdata/internal/v1/organisations/getOrganisationsByProfile")
    ResponseEntity<OrganisationStaleProfilesResponse> getOrganisationStaleProfiles(
            @RequestParam(name = "pageSize") Integer pageSize,
            @RequestParam(name = "searchAfter") String searchAfter,
            @RequestBody OrganisationStaleProfilesRequest organisationStaleProfilesRequest
    );
}
