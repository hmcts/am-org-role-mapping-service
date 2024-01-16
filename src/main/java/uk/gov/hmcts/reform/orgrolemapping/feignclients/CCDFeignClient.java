package uk.gov.hmcts.reform.orgrolemapping.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypesResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientInterceptor;

@FeignClient(value = "ccdClient", url = "${feign.client.config.ccdClient.url}",
        configuration = {FeignClientConfiguration.class, FeignClientInterceptor.class})
public interface CCDFeignClient {

    @PostMapping(value = "/api/retrieve-access-types")
    ResponseEntity<AccessTypesResponse> getAccessTypes();
}
