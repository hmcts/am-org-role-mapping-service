package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.FAILED_ROLE_REFRESH;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SUCCESS_ROLE_REFRESH;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType.JUDICIAL;

@Service
@Slf4j
public class JudicialRefreshOrchestrator {

    private final RetrieveDataService retrieveDataService;
    private final ParseRequestService parseRequestService;
    private final JudicialBookingService judicialBookingService;
    private final RequestMappingService requestMappingService;

    @Autowired
    public JudicialRefreshOrchestrator(RetrieveDataService retrieveDataService,
                                       ParseRequestService parseRequestService,
                                       JudicialBookingService judicialBookingService,
                                       RequestMappingService requestMappingService) {
        this.retrieveDataService = retrieveDataService;
        this.parseRequestService = parseRequestService;
        this.judicialBookingService = judicialBookingService;
        this.requestMappingService = requestMappingService;
    }

    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> judicialRefresh(UserRequest userRequest) {

        parseRequestService.validateUserRequest(userRequest);
        Map<String, Set<?>> userAccessProfiles = retrieveDataService.retrieveProfiles(userRequest, JUDICIAL);

        ResponseEntity<List<Object>> bookingsList = judicialBookingService.fetchJudicialBookings(userRequest);
        List<JudicialBooking> judicialBookings = (List<JudicialBooking>) (Object) bookingsList.getBody();

        ResponseEntity<Object> responseEntity = requestMappingService.createAssignments(userAccessProfiles,
                judicialBookings, JUDICIAL);
        if (responseEntity.getBody() == null || ((List<ResponseEntity>) responseEntity.getBody()).stream().anyMatch(s ->
                s.getStatusCode() != HttpStatus.CREATED)) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(FAILED_ROLE_REFRESH);
        }
        return ResponseEntity.ok().body(Map.of("Message", SUCCESS_ROLE_REFRESH));
    }
}
