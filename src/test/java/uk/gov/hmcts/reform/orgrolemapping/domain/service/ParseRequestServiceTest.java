package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_STCW;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_TCW;


@RunWith(MockitoJUnitRunner.class)
class ParseRequestServiceTest {

    ParseRequestService sut = new ParseRequestService();

    HashSet<UserProfile> invalidProfiles;
    HashSet<UserProfile> invalidProfilesSpy;

    AtomicInteger atomicInteger;
    AtomicInteger spyInteger;

    @BeforeEach
    public void setUp() {
        invalidProfiles = new HashSet<>();
        invalidProfilesSpy = Mockito.spy(invalidProfiles);
        atomicInteger = new AtomicInteger(1);
        spyInteger = Mockito.spy(atomicInteger);
    }

    @Test
    void validateUserRequestTest() {
        String id1 = "7c12a4bc-450e-4290-8063-b387a5d5e0b7";
        String id2 = "21334a2b-79ce-44eb-9168-2d49a744be9c";

        List<String> userIds = new ArrayList<>();
        userIds.add(id1);
        userIds.add(id2);
        UserRequest userRequest = UserRequest.builder().userIds(userIds).build();
        UserRequest userRequestSpy = Mockito.spy(userRequest);
        sut.validateUserRequest(userRequestSpy);

        Mockito.verify(userRequestSpy, Mockito.times(2)).getUserIds();
    }


    @Test
    void validateUserRequest_throwsBadRequestTest() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        List<String> emptyUsers = new ArrayList<>();
        userRequest.setUserIds(emptyUsers);
        assertThrows(BadRequestException.class, () ->
                sut.validateUserRequest(userRequest)
        );
    }

    @Test
    void validateUserProfilesTest() {
        sut.validateUserProfiles(TestDataBuilder.buildListOfUserProfiles(true, false, "1", "2",
                ROLE_NAME_STCW, ROLE_NAME_TCW, true, true, false, true, "1", "2", false),
                TestDataBuilder.buildUserRequest(), spyInteger, invalidProfilesSpy);

        verify(spyInteger, Mockito.times(0)).getAndIncrement();
        verify(invalidProfilesSpy, Mockito.times(0)).add(any(UserProfile.class));
    }

    @Test
    void validateUserProfiles_throwsBadRequest_noBaseLocationTest() {
        List<UserProfile> userProfiles = TestDataBuilder.buildListOfUserProfiles(true, false, "1", "2",
                ROLE_NAME_STCW, ROLE_NAME_TCW, false, true, false, true, "1", "2", false);
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        userRequest.getUserIds().add("testUser");
        sut.validateUserProfiles(userProfiles, userRequest, spyInteger, invalidProfilesSpy);

        verify(spyInteger, Mockito.times(2)).getAndIncrement();
        verify(invalidProfilesSpy, Mockito.times(2)).add(any(UserProfile.class));
    }

    @Test
    void validateUserProfiles_throwsBadRequest_noWorkAreaTest() {
        List<UserProfile> userProfiles = TestDataBuilder.buildListOfUserProfiles(true, false, "1", "2",
                ROLE_NAME_STCW, ROLE_NAME_TCW, true, true, false, false, "1", "2", false);
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        sut.validateUserProfiles(userProfiles, userRequest, spyInteger, invalidProfilesSpy);

        verify(spyInteger, Mockito.times(2)).getAndIncrement();
        verify(invalidProfilesSpy, Mockito.times(2)).add(any(UserProfile.class));

    }

    @Test
    void validateUserProfiles_throwsBadRequest_noRolesTest() {
        List<UserProfile> userProfiles = TestDataBuilder.buildListOfUserProfiles(true, false, "1", "2",
                ROLE_NAME_STCW, ROLE_NAME_TCW, true, true, false, true, "1", "2", false);
        //set first profiles roles to empty
        userProfiles.get(0).setRole(new ArrayList<>());
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        sut.validateUserProfiles(userProfiles, userRequest, spyInteger, invalidProfilesSpy);

        verify(spyInteger, Mockito.times(1)).getAndIncrement();
        verify(invalidProfilesSpy, Mockito.times(1)).add(any(UserProfile.class));

    }

    @Test
    void validateUserProfiles_throwsBadRequest_tooManyPrimaryTest() {
        List<UserProfile> userProfiles = TestDataBuilder.buildListOfUserProfiles(true, false, "1", "2",
                ROLE_NAME_STCW, ROLE_NAME_TCW, true, true, true, true, "1", "2", false);
        UserRequest userRequest = TestDataBuilder.buildUserRequest();


        sut.validateUserProfiles(userProfiles, userRequest, spyInteger, invalidProfilesSpy);

        verify(spyInteger, Mockito.times(2)).getAndIncrement();
        verify(invalidProfilesSpy, Mockito.times(2)).add(any(UserProfile.class));

    }

    @Test
    void validateUserProfiles_throwsResourceNotFound_noProfilesTest() {
        List<UserProfile> userProfiles = new ArrayList<>();
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        AtomicInteger integer = new AtomicInteger();
        Set<UserProfile> invalidUserProfiles = new HashSet<>();
        assertThrows(ResourceNotFoundException.class, () ->
                sut.validateUserProfiles(userProfiles, userRequest, integer,invalidUserProfiles)
        );
    }

    @Test
    void validateUserProfiles_throwsResourceNotFound_someProfilesNotFoundTest() {
        List<UserProfile> userProfiles = new ArrayList<>();
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        AtomicInteger integer = new AtomicInteger();
        Set<UserProfile> invalidUserProfiles = new HashSet<>();
        assertThrows(ResourceNotFoundException.class, () ->
                sut.validateUserProfiles(userProfiles, userRequest, integer,invalidUserProfiles)
        );
    }
}
