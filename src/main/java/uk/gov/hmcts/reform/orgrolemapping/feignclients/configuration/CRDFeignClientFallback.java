package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserProfile;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserRequest;

@Component
public class CRDFeignClientFallback implements CRDFeignClient {

    public static final String CRD_API_NOT_AVAILABLE = "The CRD API Service is not available";

    @Override
    public String getServiceStatus() {
        return CRD_API_NOT_AVAILABLE;
    }

    @Override
    public ResponseEntity<List<UserProfile>> getCaseworkerDetailsById(UserRequest userRequest) {
        return ResponseEntity.ok(new ArrayList<>(buildUserProfile(userRequest)));
    }

    @Override
    public ResponseEntity<UserProfilesResponse> getUsersByServiceName(@RequestParam List<String> ccdServiceNames,
                                                                      @RequestParam int pageSize,
                                                                      @RequestParam int pageNumber,
                                                                      @RequestParam String sortDirection,
                                                                      @RequestParam String sortColumn) {
        return ResponseEntity.ok(UserProfilesResponse.builder()
                .totalRecords(1).userProfiles(buildUserProfile(buildUserRequest())).build());
    }

}