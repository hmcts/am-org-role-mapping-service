package uk.gov.hmcts.reform.orgrolemapping.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.CRDFeignClientFallback;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientInterceptor;

import java.util.List;


@FeignClient(value = "crdclient", url = "${feign.client.config.crdclient.url}",
        configuration = {FeignClientConfiguration.class, FeignClientInterceptor.class},
        fallback = CRDFeignClientFallback.class)

public interface CRDFeignClient {

    @GetMapping(value = "/")
    public String getServiceStatus();

    @PostMapping(value = "/refdata/case-worker/users/fetchUsersById")
    ResponseEntity<List<UserProfile>> getCaseworkerDetailsById(UserRequest userRequest);

    @GetMapping(value = "/refdata/internal/staff/usersByServiceName")
    ResponseEntity<List<UserProfilesResponse>> getCaseworkerDetailsByServiceName(
                  @RequestParam(value = "ccd_service_names", required = true) String ccdServiceName,
                  @RequestParam(value = "page_size", required = false) Integer pageSize,
                  @RequestParam(value = "page_number", required = false) Integer pageNumber,
                  @RequestParam(value = "sort_direction", required = false) String sortDirection,
                  @RequestParam(value = "sort_column", required = false) String sortColumn);

}