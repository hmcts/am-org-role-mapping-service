package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.springframework.stereotype.Service;

@Service
public class RequestMappingService {
    //1. This service will apply all the mapping rules - either by some config file or drools.
    //2. If mapping rule is matched then prepare the role assignment requests.
    //3. Send the role assignment request to RAS service.
    //4. Log the response.
}
