package uk.gov.hmcts.reform.orgrolemapping.feignclients;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamUser;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.IdamFeignClientFallback;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.IdamFeignClientInterceptor;


@FeignClient(value = "idamclient", url = "${feign.client.config.idamClient.url}",
        configuration = {FeignClientConfiguration.class, IdamFeignClientInterceptor.class},
        fallback = IdamFeignClientFallback.class)

public interface IdamFeignClient {

    @GetMapping("/api/v2/users/{userId}")
    ResponseEntity<IdamUser> getUserById(@PathVariable String userId);

    @PutMapping("/api/v2/users/{userId}")
    ResponseEntity<IdamUser> updateUser(@PathVariable String userId, @Valid @RequestBody IdamUser user);
}