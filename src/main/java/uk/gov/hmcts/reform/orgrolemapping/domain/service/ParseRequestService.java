package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;

@Service
@Slf4j
public class ParseRequestService {
    //1. This will parse the list of userIds and validate them.
    //2. This will parse and validate the user details received from CRD

    public void validateUserRequest(UserRequest userRequest) {
        //parse the user List and validate each user Id to be valid string
    }

    public void validateUserProfile(UserRequest userRequest) {
        // parse the list of userProfiles received from CRD
        // and validate all important fields.
    }
}
