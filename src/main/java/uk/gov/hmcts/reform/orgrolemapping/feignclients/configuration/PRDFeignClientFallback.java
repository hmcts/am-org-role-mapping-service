package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;

import static uk.gov.hmcts.reform.orgrolemapping.helper.ProfessionalUserBuilder.buildUsersByOrganisationResponse;

@Component
public class PRDFeignClientFallback implements PRDFeignClient {


    @Override
    public ResponseEntity<UsersByOrganisationResponse> getUsersByOrganisation(Integer pageSize,
                                  String searchAfterOrg, String searchAfterUser,
                                  UsersByOrganisationRequest usersByOrganisationRequest) {
        return ResponseEntity.ok(buildUsersByOrganisationResponse("usersByOrganisationSample.json"));
    }
}