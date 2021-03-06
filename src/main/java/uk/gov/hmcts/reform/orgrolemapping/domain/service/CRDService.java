package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;

import java.util.List;

@Service
@AllArgsConstructor
public class CRDService {

    //This class is reserved to extract the RAS response and play with the resource object.
    private final CRDFeignClient crdFeignClient;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 3))
    public ResponseEntity<List<UserProfile>> fetchUserProfiles(UserRequest userRequest) {
        return crdFeignClient.getCaseworkerDetailsById(userRequest);
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 3))
    public ResponseEntity<List<UserProfilesResponse>> fetchCaseworkerDetailsByServiceName(String serviceName,
                                                                                          Integer pageSize,
                                                                                          Integer pageNumber,
                                                                                          String sortDirection,
                                                                                          String sortColumn) {
        return crdFeignClient.getCaseworkerDetailsByServiceName(serviceName, pageSize,
                pageNumber, sortDirection, sortColumn);

    }

}
