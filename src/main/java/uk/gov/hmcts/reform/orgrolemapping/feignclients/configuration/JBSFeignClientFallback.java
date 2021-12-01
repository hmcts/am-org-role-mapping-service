package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JBSFeignClient;

import java.util.ArrayList;
import java.util.List;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildJudicialBookings;


@Component
public class JBSFeignClientFallback implements JBSFeignClient {

    public static final String JBS_API_NOT_AVAILABLE = "The Judicial Booking Service API Service is not available";

    @Override
    public String getServiceStatus() {
        return JBS_API_NOT_AVAILABLE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ResponseEntity<List<T>> getJudicialBookingByUserIds(UserRequest userRequest) {
        return ResponseEntity.ok((List<T>) new ArrayList<>(buildJudicialBookings(userRequest,
                "judicialBookingSample.json")));
    }

}
