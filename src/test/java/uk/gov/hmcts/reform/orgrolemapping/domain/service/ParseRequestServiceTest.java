package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_STCW;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_TCW;


@RunWith(MockitoJUnitRunner.class)
class ParseRequestServiceTest {

    ParseRequestService sut = new ParseRequestService();
    Set<JudicialProfile> invalidJudicialProfiles = new HashSet<>();
    UserRequest judicialUser = TestDataBuilder.buildUserRequest();
    AtomicInteger integer = new AtomicInteger();

    AtomicInteger mockInteger = mock(AtomicInteger.class);

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
    void invalidUserRequestTest() {
        UserRequest userRequest = TestDataBuilder.buildInvalidRequest();
        assertThrows(BadRequestException.class, () ->
                sut.validateUserRequest(userRequest));
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
        sut.validateUserProfiles(caseWorkerProfiles, userRequest, mockInteger, new HashSet<>(),
                UserType.CASEWORKER);
        Mockito.verify(mockInteger, Mockito.times(2)).getAndIncrement();
    }

    @Test
    void validateUserProfiles_throwsBadRequest_noWorkAreaTest() {
        List<CaseWorkerProfile> caseWorkerProfiles = TestDataBuilder.buildListOfUserProfiles(true,
                false, "1", "2",
                ROLE_NAME_STCW, ROLE_NAME_TCW, true, true, false,
                false, "1", "2", false);
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        sut.validateUserProfiles(caseWorkerProfiles, userRequest, mockInteger,new HashSet<>(),
                UserType.CASEWORKER);
        Mockito.verify(mockInteger, Mockito.times(2)).getAndIncrement();
    }

    @Test
    void validateUserProfiles_throwsBadRequest_noRolesTest() {
        List<CaseWorkerProfile> caseWorkerProfiles = TestDataBuilder.buildListOfUserProfiles(true,
                false, "1", "2",
                ROLE_NAME_STCW, ROLE_NAME_TCW, true, true, false,
                true, "1", "2", false);
        caseWorkerProfiles.get(0).setRole(new ArrayList<>());
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        sut.validateUserProfiles(caseWorkerProfiles, userRequest, mockInteger,new HashSet<>(),
                UserType.CASEWORKER);
        Mockito.verify(mockInteger, Mockito.times(1)).getAndIncrement();

    }

    @Test
    void validateUserProfiles_throwsBadRequest_tooManyPrimaryTest() {
        List<CaseWorkerProfile> caseWorkerProfiles = TestDataBuilder.buildListOfUserProfiles(true,
                false, "1", "2",
                ROLE_NAME_STCW, ROLE_NAME_TCW, true, true, true,
                true, "1", "2", false);
        UserRequest userRequest = TestDataBuilder.buildUserRequest();


        sut.validateUserProfiles(caseWorkerProfiles, userRequest, mockInteger,new HashSet<>(),
                UserType.CASEWORKER);
        Mockito.verify(mockInteger, Mockito.times(2)).getAndIncrement();


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
        List<JudicialProfile> judicialProfileList = new ArrayList<>();
        UserRequest judicialUserRequest = TestDataBuilder.buildUserRequestIndividual();

        JudicialProfile profile1 = TestDataBuilder.buildJudicialProfile("37395", "EMP37395",
                "Magistrate", "Joe", "Bloggs", "Joe Bloggs", "Miss",
                "1", "Fee Paid Judiciary 5 Days Mon - Fri", "EMP62506@ejudiciary.net",
                LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                "2020-04-28T16:00:49", "TRUE",
                Collections.singletonList(TestDataBuilder.buildJPAppointment("84",
                        "5",
                        "1351",
                        "1",
                        "north-east",
                        LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                        LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                        "1")),
                Collections.singletonList(TestDataBuilder.buildJPAuthorisation("52149")));

        JudicialProfile profile2 = TestDataBuilder.buildJudicialProfile("37396", "EMP37395",
                "Magistrate", "Joe", "Bloggs", "Joe Bloggs", "Miss",
                "1", "Fee Paid Judiciary 5 Days Mon - Fri", "EMP62506@ejudiciary.net",
                LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                "2020-04-28T16:00:49", "TRUE",
                Collections.singletonList(TestDataBuilder.buildJPAppointment("84",
                        "5",
                        "1351",
                        "1",
                        "north-east",
                        LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                        LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                        "1")),
                Collections.singletonList(TestDataBuilder.buildJPAuthorisation("52150")));

        judicialProfileList.add(profile1);
        judicialProfileList.add(profile2);

        sut.validateUserProfiles(judicialProfileList,
                judicialUserRequest,
                integer,
                invalidJudicialProfiles,
                UserType.JUDICIAL);
    }

    @Test
    void judicialValidationTest_NoAppointment() {
        List<JudicialProfile.Appointment> noAppointmentList = new ArrayList<>();

        JudicialProfile profile = TestDataBuilder.buildJudicialProfile("37395", "EMP37395",
                "Magistrate", "Joe", "Bloggs", "Joe Bloggs", "Miss",
                "1", "Fee Paid Judiciary 5 Days Mon - Fri", "EMP62506@ejudiciary.net",
                LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                "2020-04-28T16:00:49", "TRUE",
                noAppointmentList,
                Collections.singletonList(TestDataBuilder.buildJPAuthorisation("52149")));

        sut.validateUserProfiles(Collections.singletonList(profile),
                judicialUser,
                integer,
                invalidJudicialProfiles,
                UserType.JUDICIAL);
    }

    @Test
    void judicialValidationTest_NoAppointmentRoleId() {
        JudicialProfile profile = TestDataBuilder.buildJudicialProfile("37395", "EMP37395",
                "Magistrate", "Joe", "Bloggs", "Joe Bloggs", "Miss",
                "1", "Fee Paid Judiciary 5 Days Mon - Fri", "EMP62506@ejudiciary.net",
                LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                "2020-04-28T16:00:49", "TRUE",
                Collections.singletonList(TestDataBuilder.buildJPAppointment("",
                        "5",
                        "1351",
                        "1",
                        "north-east",
                        LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                        LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                        "1")),
                Collections.singletonList(TestDataBuilder.buildJPAuthorisation("52149")));

        sut.validateUserProfiles(Collections.singletonList(profile),
                judicialUser,
                integer,
                invalidJudicialProfiles,
                UserType.JUDICIAL);
    }

    @Test
    void judicialValidationTest_NoAuthorisation() {
        List<JudicialProfile.Authorisation> noAuthorisationList = new ArrayList<>();

        JudicialProfile profile = TestDataBuilder.buildJudicialProfile("37395", "EMP37395",
                "Magistrate", "Joe", "Bloggs", "Joe Bloggs", "Miss",
                "1", "Fee Paid Judiciary 5 Days Mon - Fri", "EMP62506@ejudiciary.net",
                LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                "2020-04-28T16:00:49", "TRUE",
                Collections.singletonList(TestDataBuilder.buildJPAppointment("84",
                        "5",
                        "1351",
                        "1",
                        "north-east",
                        LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                        LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                        "1")),
                noAuthorisationList);

        sut.validateUserProfiles(Collections.singletonList(profile),
                judicialUser,
                integer,
                invalidJudicialProfiles,
                UserType.JUDICIAL);
    }

    @Test
    void judicialValidationTest_NoAuthorisationId() {
        JudicialProfile profile = TestDataBuilder.buildJudicialProfile("37395", "EMP37395",
                "Magistrate", "Joe", "Bloggs", "Joe Bloggs", "Miss",
                "1", "Fee Paid Judiciary 5 Days Mon - Fri", "EMP62506@ejudiciary.net",
                LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                "2020-04-28T16:00:49", "TRUE",
                Collections.singletonList(TestDataBuilder.buildJPAppointment("84",
                        "5",
                        "1351",
                        "1",
                        "north-east",
                        LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                        LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                        "1")),
                Collections.singletonList(TestDataBuilder.buildJPAuthorisation("")));

        sut.validateUserProfiles(Collections.singletonList(profile),
                judicialUser,
                integer,
                invalidJudicialProfiles,
                UserType.JUDICIAL);
    }

}
