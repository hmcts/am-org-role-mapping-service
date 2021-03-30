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
    public ResponseEntity<List<UserProfile>> getCaseworkerDetailsById(UserRequest userRequest);

    @PostMapping(value = "/refdata/case-worker/get-users-by-service-name")
    public ResponseEntity<UserProfilesResponse> getUsersByServiceName(@RequestParam List<String> ccdServiceNames,
                                                                      @RequestParam int pageSize,
                                                                      @RequestParam int pageNumber,
                                                                      @RequestParam String sortDirection,
                                                                      @RequestParam String sortColumn);

}