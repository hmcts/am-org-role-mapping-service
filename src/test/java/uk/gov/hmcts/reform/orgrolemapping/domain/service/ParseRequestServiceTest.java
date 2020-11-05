package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;

import java.util.Arrays;

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
    void shouldThrowTheBadRequest() {

        UserRequest userRequest = UserRequest.builder()
                .users(Arrays.asList("1234567-89b-423-456-556642445678@"))
                .build();
        Assertions.assertThrows(BadRequestException.class, () ->
                sut.validateUserRequest(userRequest)
        );

    }


    @Test
    void shouldValidateUserProfile() {

        sut.validateUserProfiles(buildUserProfile(buildUserRequest()), buildUserRequest());

    }
}
