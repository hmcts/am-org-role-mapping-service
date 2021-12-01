package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;

import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class JudicialRefreshOrchestrator {

    private final RetrieveDataService retrieveDataService;
    private final ParseRequestService parseRequestService;

    private final RequestMappingService requestMappingService;

    @Autowired
    public JudicialRefreshOrchestrator(RetrieveDataService retrieveDataService,
                                       ParseRequestService parseRequestService,
                                       RequestMappingService requestMappingService) {
        this.retrieveDataService = retrieveDataService;
        this.parseRequestService = parseRequestService;
        this.requestMappingService = requestMappingService;
    }

    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> judicialRefresh(UserRequest userRequest) {
        parseRequestService.validateUserRequest(userRequest);

        Map<String, Set<?>> userAccessProfiles = retrieveDataService.retrieveProfiles(userRequest, UserType.JUDICIAL);


        ResponseEntity<Object> responseEntity = requestMappingService.createAssignments(userAccessProfiles,
                UserType.JUDICIAL);
        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
            return ResponseEntity.ok().body(Map.of("Message",
                    "Role assignments have been refreshed successfully"));
        } else {
            return ResponseEntity.status(422).body(responseEntity.getBody());
        }
    }
}
