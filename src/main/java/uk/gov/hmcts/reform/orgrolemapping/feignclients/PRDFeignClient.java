package uk.gov.hmcts.reform.orgrolemapping.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUsersResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationsResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.PRDFeignClientFallback;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.RdFeignClientInterceptor;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientInterceptor;


@FeignClient(value = "prdclient", url = "${feign.client.config.prdclient.url}",
        configuration = {FeignClientConfiguration.class, RdFeignClientInterceptor.class},
        fallback = PRDFeignClientFallback.class)

public interface PRDFeignClient {

    @GetMapping(value = "/")
    String getServiceStatus();

    @GetMapping(value = "/refdata/internal/v1/organisations/users")
    ResponseEntity<GetRefreshUsersResponse> getRefreshUsers(@RequestParam(value = "userId") String userId);

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
}
