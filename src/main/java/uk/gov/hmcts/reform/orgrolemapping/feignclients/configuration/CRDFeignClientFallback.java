package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserProfile;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserAccessProfile;

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
        return ResponseEntity.ok((List<T>) new ArrayList<>(buildUserProfile(userRequest, "userProfileSample.json")));

    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ResponseEntity<List<T>> getCaseworkerDetailsByServiceName(String ccdServiceNames,
                                                                                        Integer pageSize,
                                                                                        Integer pageNumber,
                                                                                        String sortDirection,
                                                                                        String sortColumn) {

        ResponseEntity<List<CaseWorkerProfilesResponse>> responseEntity = ResponseEntity.ok(Arrays
                .asList(CaseWorkerProfilesResponse.builder()
                .serviceName(ccdServiceNames).userProfile(buildUserAccessProfile(UserRequest.builder().userIds(
                        List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                        .build(), "userProfileSample.json").get(0)).build()));

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(
                "total_records", "4");

        return ResponseEntity.status(HttpStatus.OK).headers(responseHeaders).body(
                (List<T>) responseEntity.getBody());




    }


}