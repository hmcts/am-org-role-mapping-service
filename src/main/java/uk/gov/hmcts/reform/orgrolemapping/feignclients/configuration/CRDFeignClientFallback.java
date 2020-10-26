package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;

@Component
public class CRDFeignClientFallback implements CRDFeignClient {

    public static final String CRD_API_NOT_AVAILABLE = "The data store Service is not available";

    @Override
    public String getServiceStatus() {
        return CRD_API_NOT_AVAILABLE;
    }

}