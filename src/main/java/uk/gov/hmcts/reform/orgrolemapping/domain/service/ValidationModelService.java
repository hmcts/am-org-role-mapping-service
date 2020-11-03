package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.springframework.stereotype.Service;

@Service
public class ValidationModelService {
    private ValidationModelService() {
    }

    //1. receive single UserAccessProfile for caseworker
    //2. receive initial requestedRole corresponding to above userAccessProfile
    //3. Run the rules for preparing the final requestedRole.

}
