package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

class ParseRequestServiceTest {

    ParseRequestService sut = new ParseRequestService();

    @Test
    void validateUserRequest() {
        sut.validateUserRequest(TestDataBuilder.buildUserRequest());
    }

    @Test
    void validateUserRequest_throwsBadRequest() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        userRequest.getUsers().add("bleep-bloop-bleep");
        assertThrows(BadRequestException.class, () ->
                sut.validateUserRequest(userRequest)
        );
    }

    @Test
    void validateUserProfiles() {
        sut.validateUserProfiles(TestDataBuilder.buildListOfUserProfiles(), TestDataBuilder.buildUserRequest());
    }

    @Test
    void validateUserProfiles_throwsBadRequest_noBaseLocation() {
        List<UserProfile> userProfiles = TestDataBuilder.buildListOfUserProfiles();
        userProfiles.get(0).setBaseLocation(new ArrayList<>());
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        assertThrows(BadRequestException.class, () ->
                sut.validateUserProfiles(userProfiles, userRequest)
        );
    }

    @Test
    void validateUserProfiles_throwsBadRequest_noWorkArea() {
        List<UserProfile> userProfiles = TestDataBuilder.buildListOfUserProfiles();
        userProfiles.get(0).setWorkArea(new ArrayList<>());
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        assertThrows(BadRequestException.class, () ->
                sut.validateUserProfiles(userProfiles, userRequest)
        );
    }

    @Test
    void validateUserProfiles_throwsBadRequest_noRoles() {
        List<UserProfile> userProfiles = TestDataBuilder.buildListOfUserProfiles();
        userProfiles.get(0).setRole(new ArrayList<>());
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        assertThrows(BadRequestException.class, () ->
                sut.validateUserProfiles(userProfiles, userRequest)
        );
    }

    @Test
    void validateUserProfiles_throwsBadRequest_tooManyPrimary() {
        List<UserProfile> userProfiles = TestDataBuilder.buildListOfUserProfiles();
        userProfiles.get(0).getBaseLocation().add(TestDataBuilder.buildBaseLocation(true));
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        assertThrows(BadRequestException.class, () ->
                sut.validateUserProfiles(userProfiles, userRequest)
        );
    }

    @Test
    void validateUserProfiles_throwsResourceNotFound_noProfiles() {
        List<UserProfile> userProfiles = new ArrayList<>();
        TestDataBuilder.buildUserProfile(TestDataBuilder.generateUniqueId());
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        assertThrows(ResourceNotFoundException.class, () ->
                sut.validateUserProfiles(userProfiles, userRequest)
        );
    }

    @Test
    void validateUserProfiles_throwsResourceNotFound_someProfilesNotFound() {
        List<UserProfile> userProfiles = TestDataBuilder.buildListOfUserProfiles();
        userProfiles.remove(0);
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        assertThrows(ResourceNotFoundException.class, () ->
                sut.validateUserProfiles(userProfiles, userRequest)
        );
    }
}