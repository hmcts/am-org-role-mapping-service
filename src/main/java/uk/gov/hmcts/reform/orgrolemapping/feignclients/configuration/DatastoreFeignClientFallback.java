package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import uk.gov.hmcts.reform.orgrolemapping.feignclients.DataStoreFeignClient;

public class DatastoreFeignClientFallback implements DataStoreFeignClient {

    public static final String DATA_STORE_NOT_AVAILABLE = "The data store Service is not available";

    @Override
    public String getServiceStatus() {
        return DATA_STORE_NOT_AVAILABLE;
    }

    @Override
    public String getCaseDataV1(String uid, String jurisdictionId, String caseTypeId, String caseId) {
        return DATA_STORE_NOT_AVAILABLE;
    }

    @Override
    public String getCaseDataV2(String caseId) {
        return DATA_STORE_NOT_AVAILABLE;
    }
}
