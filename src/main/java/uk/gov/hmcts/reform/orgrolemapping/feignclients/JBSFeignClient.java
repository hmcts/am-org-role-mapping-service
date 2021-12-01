package uk.gov.hmcts.reform.orgrolemapping.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientInterceptor;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.JBSFeignClientFallback;

import java.util.List;

@FeignClient(value = "jbsClient", url = "${feign.client.config.jbsClient.url}",
        configuration = {FeignClientConfiguration.class, FeignClientInterceptor.class},
        fallback = JBSFeignClientFallback.class)
public interface JBSFeignClient {

    @GetMapping(value = "/")
    String getServiceStatus();

    @PostMapping(value = "/am/bookings/query")
    <T> ResponseEntity<List<T>> getJudicialBookingByUserIds(UserRequest userRequest);
}
