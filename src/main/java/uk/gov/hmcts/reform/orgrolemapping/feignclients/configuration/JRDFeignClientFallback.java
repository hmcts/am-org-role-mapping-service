package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JRDFeignClient;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildJudicialProfile;


@Component
public class JRDFeignClientFallback  implements JRDFeignClient {

    public static final String JRD_API_NOT_AVAILABLE = "The JRD API Service is not available";

    @Override
    public String getServiceStatus() {
        return JRD_API_NOT_AVAILABLE;
    }

    @Override
    public ResponseEntity<List<JudicialProfile>> getJudicialDetailsById(UserRequest userRequest) {
        return ResponseEntity.ok(new ArrayList<>(buildJudicialProfile(userRequest)));
    }
}
