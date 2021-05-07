package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserProfile;

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
    public  ResponseEntity<List<UserProfilesResponse>> getCaseworkerDetailsByServiceName(String ccd_service_names, Integer page_size,
                                                                         Integer page_number, String sort_direction,
                                                                         String sort_column) {

       ResponseEntity<List<UserProfilesResponse>> responseEntity = ResponseEntity.ok(Arrays.asList(UserProfilesResponse.builder()
                .serviceName(ccd_service_names).userProfiles(buildUserProfile( UserRequest.builder().userIds(
                        Arrays.asList(UUID.randomUUID().toString(),UUID.randomUUID().toString()))
                        .build())).build()));

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(
                "total_records","4");

        return ResponseEntity.status(HttpStatus.OK).headers(responseHeaders).body(
                responseEntity.getBody() );


    }


}