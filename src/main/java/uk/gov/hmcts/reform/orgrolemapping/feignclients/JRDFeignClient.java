package uk.gov.hmcts.reform.orgrolemapping.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping(value = "/refdata/judicial/users")
    <T> ResponseEntity<List<T>> getJudicialDetailsByServiceName(
            @RequestBody JRDUserRequest userRequest,
            @RequestParam(value = "page_size", required = false) Integer pageSize,
            @RequestParam(value = "page_number", required = false) Integer pageNumber,
            @RequestParam(value = "sort_direction", required = false) String sortDirection,
            @RequestParam(value = "sort_column", required = false) String sortColumn);
}
