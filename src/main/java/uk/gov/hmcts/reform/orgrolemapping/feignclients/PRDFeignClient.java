package uk.gov.hmcts.reform.orgrolemapping.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.RdFeignClientInterceptor;

import java.util.UUID;

@FeignClient(value = "prdClient", url = "${feign.client.config.prdClient.url}",
        configuration = {FeignClientConfiguration.class, RdFeignClientInterceptor.class})
public interface PRDFeignClient {

    @GetMapping(value = "/refdata/internal/v1/organisations/users")
    ResponseEntity<Object> getRefreshUsers(
            @RequestParam(value = "since", required = false) String since,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "searchAfter", required = false) UUID searchAfter
    );
}
