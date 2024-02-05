package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUsersResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;

import static uk.gov.hmcts.reform.orgrolemapping.helper.ProfessionalRefreshUserBuilder.buildGetRefreshUsersResponse;

@Component
public class PRDFeignClientFallback implements PRDFeignClient {

    public static final String PRD_API_NOT_AVAILABLE = "The PRD API Service is not available";

    @Override
    public String getServiceStatus() {
        return PRD_API_NOT_AVAILABLE;
    }

    @Override
    public ResponseEntity<GetRefreshUsersResponse> getRefreshUsers(String userId) {
        return buildGetRefreshUsersResponse("prdRefreshUserSample.json", userId);
    }
}