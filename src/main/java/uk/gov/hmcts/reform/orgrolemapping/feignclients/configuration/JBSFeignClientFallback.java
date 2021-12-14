package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBookingRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBookingResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JBSFeignClient;

import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildJudicialBookings;


@Component
public class JBSFeignClientFallback implements JBSFeignClient {

    public static final String JBS_API_NOT_AVAILABLE = "The Judicial Booking Service API is not available";

    @Override
    public String getServiceStatus() {
        return JBS_API_NOT_AVAILABLE;
    }

    @Override
    public ResponseEntity<JudicialBookingResponse> getJudicialBookingByUserIds(JudicialBookingRequest userRequest) {
        return ResponseEntity.ok().body(new JudicialBookingResponse(buildJudicialBookings(
                userRequest.getQueryRequest(), "judicialBookingSample.json")));
    }

}
