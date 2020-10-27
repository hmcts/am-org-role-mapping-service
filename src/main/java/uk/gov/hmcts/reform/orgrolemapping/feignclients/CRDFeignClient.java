package uk.gov.hmcts.reform.orgrolemapping.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.CRDFeignClientFallback;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientConfiguration;

import java.util.List;


@FeignClient(value = "crdclient", url = "${feign.client.config.crdclient.url}",
        configuration = FeignClientConfiguration.class,
        fallback = CRDFeignClientFallback.class)

public interface CRDFeignClient {

    @GetMapping(value = "/")
    public String getServiceStatus();


    @PostMapping(value = "/refdata/case-worker/users/fetchUsersById")
    public ResponseEntity<List<UserProfile>> createRoleAssignment(UserRequest userRequest);

}