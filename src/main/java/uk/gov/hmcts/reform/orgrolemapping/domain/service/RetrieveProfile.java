package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;

import java.util.Map;

public interface RetrieveProfile<K,V> {

    Map<K,V> retrieveCaseWorkerProfiles(UserRequest userRequest, UserType userType);
}
