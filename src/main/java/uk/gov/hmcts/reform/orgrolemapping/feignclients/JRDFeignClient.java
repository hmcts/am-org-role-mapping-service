package uk.gov.hmcts.reform.orgrolemapping.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JRDUserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.JRDFeignClientFallback;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.RdFeignClientInterceptor;

import java.util.List;

@FeignClient(value = "jrdClient", url = "${feign.client.config.jrdClient.url}",
        configuration = {FeignClientConfiguration.class, RdFeignClientInterceptor.class},
        fallback = JRDFeignClientFallback.class)
public interface JRDFeignClient {

    @GetMapping(value = "/")
    String getServiceStatus();

    @PostMapping(value = "/refdata/judicial/users")
    <T> ResponseEntity<List<T>> getJudicialDetailsById(JRDUserRequest userRequest,
                                                       @RequestHeader(name = "page_size") Integer pageSize);

    @PostMapping(value = "/refdata/judicial/users/v2")
    <T> ResponseEntity<List<T>> getJudicialDetailsByIdV2(JRDUserRequest userRequest,
                                                       @RequestHeader(name = "page_size") Integer pageSize);
}
