package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypesResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CCDFeignClient;

import static uk.gov.hmcts.reform.orgrolemapping.helper.CDDFallbackResponseBuilder.ACCESS_TYPES_SAMPLE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.CDDFallbackResponseBuilder.buildAccessTypesResponse;

@Component
public class CCDFeignClientFallback implements CCDFeignClient {

    @Override
    public ResponseEntity<AccessTypesResponse> getAccessTypes() {
        return ResponseEntity.ok(buildAccessTypesResponse(ACCESS_TYPES_SAMPLE));
    }

}