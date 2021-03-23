package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public  interface ParseRequestBase<T> {

    void validateUserProfiles(List<T> profiles, UserRequest userRequest,
                                              AtomicInteger invalidUserProfilesCount,
                                              Set<T> invalidProfiles, UserType userType);


}
