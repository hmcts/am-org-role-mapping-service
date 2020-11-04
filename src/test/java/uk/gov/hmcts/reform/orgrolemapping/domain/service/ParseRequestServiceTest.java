package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserProfile;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserRequest;

@RunWith(MockitoJUnitRunner.class)
class ParseRequestServiceTest {


    @InjectMocks
    private ParseRequestService sut = new ParseRequestService();

    @Test
    void shouldValidateUserRequest() {

        sut.validateUserRequest(buildUserRequest());

    }

    @Test
    void shouldValidateUserProfile() {

        sut.validateUserProfiles(buildUserProfile(buildUserRequest()), buildUserRequest());

    }
}
