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


@RunWith(MockitoJUnitRunner.class)
class ParseRequestServiceTest {

    ParseRequestService sut = new ParseRequestService();

    @Test
    void validateUserRequestTest() {
        sut.validateUserRequest(TestDataBuilder.buildUserRequest());
    }

    @Test
    void validateUserRequest_throwsBadRequestTest() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        userRequest.getUsers().add("User-@007");
        assertThrows(BadRequestException.class, () ->
                sut.validateUserRequest(userRequest)
        );
    }

    @Test
    void validateUserProfilesTest() {
        sut.validateUserProfiles(TestDataBuilder.buildListOfUserProfiles(), TestDataBuilder.buildUserRequest());
    }

    @Test
    void validateUserProfiles_throwsBadRequest_noBaseLocationTest() {
        List<UserProfile> userProfiles = TestDataBuilder.buildListOfUserProfiles();
        userProfiles.get(0).setBaseLocation(new ArrayList<>());
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        assertThrows(BadRequestException.class, () ->
                sut.validateUserProfiles(userProfiles, userRequest)
        );
    }

    @Test
    void validateUserProfiles_throwsBadRequest_noWorkAreaTest() {
        List<UserProfile> userProfiles = TestDataBuilder.buildListOfUserProfiles();
        userProfiles.get(0).setWorkArea(new ArrayList<>());
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        assertThrows(BadRequestException.class, () ->
                sut.validateUserProfiles(userProfiles, userRequest)
        );
    }

    @Test
    void validateUserProfiles_throwsBadRequest_noRolesTest() {
        List<UserProfile> userProfiles = TestDataBuilder.buildListOfUserProfiles();
        userProfiles.get(0).setRole(new ArrayList<>());
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        assertThrows(BadRequestException.class, () ->
                sut.validateUserProfiles(userProfiles, userRequest)
        );
    }

    @Test
    void validateUserProfiles_throwsBadRequest_tooManyPrimaryTest() {
        List<UserProfile> userProfiles = TestDataBuilder.buildListOfUserProfiles();
        userProfiles.get(0).getBaseLocation().add(TestDataBuilder.buildBaseLocation(true));
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        assertThrows(BadRequestException.class, () ->
                sut.validateUserProfiles(userProfiles, userRequest)
        );
    }

    @Test
    void validateUserProfiles_throwsResourceNotFound_noProfilesTest() {
        List<UserProfile> userProfiles = new ArrayList<>();
        TestDataBuilder.buildUserProfile(TestDataBuilder.generateUniqueId());
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        assertThrows(ResourceNotFoundException.class, () ->
                sut.validateUserProfiles(userProfiles, userRequest)
        );
    }

    @Test
    void validateUserProfiles_throwsResourceNotFound_someProfilesNotFoundTest() {
        List<UserProfile> userProfiles = TestDataBuilder.buildListOfUserProfiles();
        userProfiles.remove(0);
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        assertThrows(ResourceNotFoundException.class, () ->
                sut.validateUserProfiles(userProfiles, userRequest)
        );
    }
}
