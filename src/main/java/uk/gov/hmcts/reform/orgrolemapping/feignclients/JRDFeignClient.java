package uk.gov.hmcts.reform.orgrolemapping.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.JRDFeignClientFallback;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.JrdFeignClientInterceptor;

import java.util.List;

@FeignClient(value = "jrdClient", url = "${feign.client.config.jrdClient.url}",
        configuration = {FeignClientConfiguration.class, JrdFeignClientInterceptor.class},
        fallback = JRDFeignClientFallback.class)
public interface JRDFeignClient {

    @GetMapping(value = "/")
    public String getServiceStatus();

    @PostMapping(value = "/refdata/judicial/users/fetch")
    public <T> ResponseEntity<List<T>> getJudicialDetailsById(@RequestParam(value = "page_size") Integer pageSize
            ,@RequestBody UserRequest userRequest);
}
