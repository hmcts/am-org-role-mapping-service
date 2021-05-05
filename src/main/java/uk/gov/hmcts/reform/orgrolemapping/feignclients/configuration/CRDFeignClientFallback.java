package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    @SuppressWarnings("unchecked")
    public <T> ResponseEntity<List<T>> getCaseworkerDetailsById(UserRequest userRequest) {
        return ResponseEntity.ok((List<T>) new ArrayList<>(buildUserProfile(userRequest)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ResponseEntity<List<T>> getCaseworkerDetailsByServiceName(String ccd_service_names, Integer page_size,
                                                                   Integer page_number, String sort_direction,
                                                                   String sort_column) {

        return ResponseEntity.ok((List<T>) Arrays.asList(UserProfilesResponse.builder()
                .serviceName(ccd_service_names).userProfiles(buildUserProfile( UserRequest.builder().userIds(
                        Collections.singletonList("21334a2b-79ce-44eb-9168-2d49a744be9c"))
                        .build())).build()));


          }

}