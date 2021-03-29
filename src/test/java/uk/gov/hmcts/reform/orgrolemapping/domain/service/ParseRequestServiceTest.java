package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_STCW;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_TCW;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildJudicialProfile;


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
        List<String> emptyUsers = new ArrayList<>();
        userRequest.setUserIds(emptyUsers);
        assertThrows(BadRequestException.class, () ->
                sut.validateUserRequest(userRequest)
        );
    }

    @Test
    void validateUserProfilesTest() {
        sut.validateUserProfiles(TestDataBuilder.buildListOfUserProfiles(true, false,
                "1", "2",
                ROLE_NAME_STCW, ROLE_NAME_TCW, true, true, false,
                true, "1", "2", false),
                TestDataBuilder.buildUserRequest(), new AtomicInteger(),new HashSet<>(), UserType.CASEWORKER);
    }

    @Test
    void validateUserProfiles_throwsBadRequest_noBaseLocationTest() {
        List<CaseWorkerProfile> caseWorkerProfiles = TestDataBuilder.buildListOfUserProfiles(true,
                false, "1", "2",
                ROLE_NAME_STCW, ROLE_NAME_TCW, false, true,
                false, true, "1", "2", false);
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        userRequest.getUserIds().add("testUser");
        sut.validateUserProfiles(caseWorkerProfiles, userRequest, new AtomicInteger(),new HashSet<>(),
                UserType.CASEWORKER);
    }

    @Test
    void validateUserProfiles_throwsBadRequest_noWorkAreaTest() {
        List<CaseWorkerProfile> caseWorkerProfiles = TestDataBuilder.buildListOfUserProfiles(true,
                false, "1", "2",
                ROLE_NAME_STCW, ROLE_NAME_TCW, true, true, false,
                false, "1", "2", false);
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        sut.validateUserProfiles(caseWorkerProfiles, userRequest, new AtomicInteger(),new HashSet<>(),
                UserType.CASEWORKER);

    }

    @Test
    void validateUserProfiles_throwsBadRequest_noRolesTest() {
        List<CaseWorkerProfile> caseWorkerProfiles = TestDataBuilder.buildListOfUserProfiles(true,
                false, "1", "2",
                ROLE_NAME_STCW, ROLE_NAME_TCW, true, true, false,
                true, "1", "2", false);
        caseWorkerProfiles.get(0).setRole(new ArrayList<>());
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        sut.validateUserProfiles(caseWorkerProfiles, userRequest, new AtomicInteger(),new HashSet<>(),
                UserType.CASEWORKER);

    }

    @Test
    void validateUserProfiles_throwsBadRequest_tooManyPrimaryTest() {
        List<CaseWorkerProfile> caseWorkerProfiles = TestDataBuilder.buildListOfUserProfiles(true,
                false, "1", "2",
                ROLE_NAME_STCW, ROLE_NAME_TCW, true, true, true,
                true, "1", "2", false);
        UserRequest userRequest = TestDataBuilder.buildUserRequest();


        sut.validateUserProfiles(caseWorkerProfiles, userRequest, new AtomicInteger(),new HashSet<>(),
                UserType.CASEWORKER);

    }

    @Test
    void validateUserProfiles_throwsResourceNotFound_noProfilesTest() {
        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        AtomicInteger integer = new AtomicInteger();
        Set<CaseWorkerProfile> invalidCaseWorkerProfiles = new HashSet<>();
        assertThrows(ResourceNotFoundException.class, () ->
                sut.validateUserProfiles(caseWorkerProfiles, userRequest, integer, invalidCaseWorkerProfiles,
                        UserType.CASEWORKER)
        );
    }

    @Test
    void validateUserProfiles_throwsResourceNotFound_someProfilesNotFoundTest() {
        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        AtomicInteger integer = new AtomicInteger();
        Set<CaseWorkerProfile> invalidCaseWorkerProfiles = new HashSet<>();
        assertThrows(ResourceNotFoundException.class, () ->
                sut.validateUserProfiles(caseWorkerProfiles, userRequest, integer, invalidCaseWorkerProfiles,
                        UserType.CASEWORKER)
        );
    }

    @Test
    void validateJudicialProfilesTest() {
        sut.validateUserProfiles(buildJudicialProfile(TestDataBuilder.buildUserRequest()),
                TestDataBuilder.buildUserRequest(), new AtomicInteger(),new HashSet<>(), UserType.JUDICIAL);
    }
}
