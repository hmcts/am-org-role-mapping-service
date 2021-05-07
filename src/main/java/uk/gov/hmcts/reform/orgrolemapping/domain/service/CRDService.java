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
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.CRDFeignClientFallback;

import java.util.List;

@Service
@AllArgsConstructor
public class CRDService {

    //This class is reserved to extract the RAS response and play with the resource object.
    private final CRDFeignClientFallback crdFeignClient;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 3))
    public ResponseEntity<List<UserProfile>> fetchUserProfiles(UserRequest userRequest) {
        return crdFeignClient.getCaseworkerDetailsById(userRequest);
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 3))
    public ResponseEntity<List<UserProfilesResponse>> fetchCaseworkerDetailsByServiceName(String serviceName,
                                                                                          Integer page_size,
                                                                                          Integer page_number,
                                                                                          String sort_direction,
                                                                                          String sort_column) {
        return crdFeignClient.getCaseworkerDetailsByServiceName(serviceName, page_size,
                page_number, sort_direction, sort_column);

    }

}
