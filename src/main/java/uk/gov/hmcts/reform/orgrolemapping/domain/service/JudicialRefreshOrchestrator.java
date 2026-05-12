package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.FAILED_ROLE_REFRESH;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SUCCESS_ROLE_REFRESH;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.PredicateValidator.httpStatusPredicate;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType.JUDICIAL;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.UnprocessableEntityException;

@Service
@Slf4j
public class JudicialRefreshOrchestrator {

    private final RetrieveDataService retrieveDataService;
    private final ParseRequestService parseRequestService;
    private final JudicialBookingService judicialBookingService;
    private final RequestMappingService<UserAccessProfile> requestMappingService;

    @Autowired
    public JudicialRefreshOrchestrator(RetrieveDataService retrieveDataService,
                                       ParseRequestService parseRequestService,
                                       JudicialBookingService judicialBookingService,
                                       RequestMappingService<UserAccessProfile> requestMappingService) {
        this.retrieveDataService = retrieveDataService;
        this.parseRequestService = parseRequestService;
        this.judicialBookingService = judicialBookingService;
        this.requestMappingService = requestMappingService;
    }

    public ResponseEntity<Object> judicialRefresh(UserRequest userRequest) {

        parseRequestService.validateUserRequest(userRequest);
        Map<String, Set<UserAccessProfile>> userAccessProfiles
                = retrieveDataService.retrieveProfiles(userRequest, JUDICIAL);

        List<JudicialBooking> judicialBookings = judicialBookingService.fetchJudicialBookings(userRequest);
        log.info("{} profile(s) got {} booking(s)", userAccessProfiles.size(), judicialBookings.size());
        ResponseEntity<Object> responseEntity = requestMappingService.createJudicialAssignments(userAccessProfiles,
                judicialBookings);
        Object responseBody = Objects.requireNonNull(responseEntity.getBody());
        if (!(responseBody instanceof List<?> rawResponses)) {
            throw new UnprocessableEntityException(FAILED_ROLE_REFRESH);
        }
        var responses = rawResponses.stream()
                .filter(ResponseEntity.class::isInstance)
                .map(ResponseEntity.class::cast)
                .collect(Collectors.toList());
        if (responses.stream().anyMatch(response -> httpStatusPredicate(
                HttpStatus.valueOf(response.getStatusCode().value())
        ).negate().test(HttpStatus.CREATED))) {
            var failedResponses = responses.stream()
                    .filter(response -> response.getStatusCode() != HttpStatus.CREATED)
                    .map(response -> Map.of(
                            "status", response.getStatusCode().value(),
                            "body", response.getBody()
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                    "message", FAILED_ROLE_REFRESH,
                    "failedResponses", failedResponses
            ));
        }
        return ResponseEntity.ok().body(Map.of("Message", SUCCESS_ROLE_REFRESH));
    }
}
