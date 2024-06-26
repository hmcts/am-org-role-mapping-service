package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JRDUserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JRDFeignClient;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildJudicialProfileV2;


@Component
public class JRDFeignClientFallback  implements JRDFeignClient {

    public static final String JRD_API_NOT_AVAILABLE = "The JRD API Service is not available";

    @Override
    public String getServiceStatus() {
        return JRD_API_NOT_AVAILABLE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ResponseEntity<List<T>> getJudicialDetailsById(JRDUserRequest userRequest, Integer pageSize) {
        return ResponseEntity.ok((List<T>) new ArrayList<>(buildJudicialProfileV2(userRequest,
                "judicialProfileSampleV2.json")));
    }
}
