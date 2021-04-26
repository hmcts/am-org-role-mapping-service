package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.io.File;
import java.io.IOException;
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
    AtomicInteger mockInteger = mock(AtomicInteger.class);
    ObjectMapper objectMapper = new ObjectMapper();

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
                ROLE_NAME_STCW, ROLE_NAME_TCW, true, true,
                false, true, "1", "2", false);
        caseWorkerProfiles.get(0).setBaseLocation(new ArrayList<>());
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        userRequest.getUserIds().add("testUser");
        sut.validateUserProfiles(caseWorkerProfiles, userRequest, mockInteger, new HashSet<>(),
                UserType.CASEWORKER);
        Mockito.verify(mockInteger, Mockito.times(1)).getAndIncrement();
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
        caseWorkerProfiles.get(1).setRole(new ArrayList<>());
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        sut.validateUserProfiles(caseWorkerProfiles, userRequest, mockInteger,new HashSet<>(),
                UserType.CASEWORKER);
        Mockito.verify(mockInteger, Mockito.times(2)).getAndIncrement();
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
    void validateJudicialProfilesTest() throws IOException {
        objectMapper.registerModule(new JavaTimeModule());
        JudicialProfile judicialProfile1 =
                objectMapper.readValue(new File("src/main/resources/judicialProfileSample.json"),
                        JudicialProfile.class);
        JudicialProfile judicialProfile2 =
                objectMapper.readValue(new File("src/main/resources/judicialProfileSample.json"),
                        JudicialProfile.class);
        judicialProfile2.setElinkId("37396");
        UserRequest judicialUserRequest = TestDataBuilder.buildUserRequestIndividual();
        List<JudicialProfile> judicialProfileList = new ArrayList<>();

        judicialProfileList.add(judicialProfile1);
        judicialProfileList.add(judicialProfile2);

        sut.validateUserProfiles(judicialProfileList,
                judicialUserRequest,
                new AtomicInteger(),
                invalidJudicialProfiles,
                UserType.JUDICIAL);
    }

    @Test
    void judicialProfiles_noContractTypeId_noBaseLocationId_noLocationId() throws IOException {
        objectMapper.registerModule(new JavaTimeModule());
        JudicialProfile profile1 =
                objectMapper.readValue(new File("src/main/resources/judicialProfileSample.json"),
                        JudicialProfile.class);
        profile1.getAppointments().get(0).setContractTypeId("");
        profile1.getAppointments().get(1).setContractTypeId("");
        JudicialProfile profile2 =
                objectMapper.readValue(new File("src/main/resources/judicialProfileSample.json"),
                        JudicialProfile.class);
        profile2.getAppointments().get(0).setBaseLocationId("");
        profile2.getAppointments().get(1).setBaseLocationId("");
        JudicialProfile profile3 =
                objectMapper.readValue(new File("src/main/resources/judicialProfileSample.json"),
                        JudicialProfile.class);
        profile3.getAppointments().get(0).setLocationId("");
        profile3.getAppointments().get(1).setLocationId("");
        List<JudicialProfile> judicialProfileList = new ArrayList<>();

        judicialProfileList.add(profile1);
        judicialProfileList.add(profile2);
        judicialProfileList.add(profile3);

        sut.validateUserProfiles(judicialProfileList,
                TestDataBuilder.buildUserRequestIndividual(),
                mockInteger,
                invalidJudicialProfiles,
                UserType.JUDICIAL);
        Mockito.verify(mockInteger, Mockito.times(3)).getAndIncrement();
    }

    @Test
    void judicialValidationTest_NoAppointment() throws IOException {
        objectMapper.registerModule(new JavaTimeModule());
        JudicialProfile profile =
                objectMapper.readValue(new File("src/main/resources/judicialProfileSample.json"),
                JudicialProfile.class);
        profile.setAppointments(new ArrayList<>());

        sut.validateUserProfiles(Collections.singletonList(profile),
                judicialUser,
                mockInteger,
                invalidJudicialProfiles,
                UserType.JUDICIAL);
        Mockito.verify(mockInteger, Mockito.times(1)).getAndIncrement();
    }

    @Test
    void judicialValidationTest_NoAppointmentRoleId() throws IOException {
        objectMapper.registerModule(new JavaTimeModule());
        JudicialProfile profile =
                objectMapper.readValue(new File("src/main/resources/judicialProfileSample.json"),
                        JudicialProfile.class);
        profile.getAppointments().get(0).setRoleId("");
        profile.getAppointments().get(1).setRoleId("");

        sut.validateUserProfiles(Collections.singletonList(profile),
                judicialUser,
                mockInteger,
                invalidJudicialProfiles,
                UserType.JUDICIAL);
        Mockito.verify(mockInteger, Mockito.times(1)).getAndIncrement();
    }

    @Test
    void judicialValidationTest_NoAuthorisation() throws IOException {
        objectMapper.registerModule(new JavaTimeModule());
        JudicialProfile profile =
                objectMapper.readValue(new File("src/main/resources/judicialProfileSample.json"),
                        JudicialProfile.class);
        profile.setAuthorisations(new ArrayList<>());

        sut.validateUserProfiles(Collections.singletonList(profile),
                judicialUser,
                mockInteger,
                invalidJudicialProfiles,
                UserType.JUDICIAL);
        Mockito.verify(mockInteger, Mockito.times(1)).getAndIncrement();
    }

    @Test
    void judicialValidationTest_NoAuthorisationId() throws IOException {
        objectMapper.registerModule(new JavaTimeModule());
        JudicialProfile profile =
                objectMapper.readValue(new File("src/main/resources/judicialProfileSample.json"),
                        JudicialProfile.class);
        profile.getAuthorisations().get(0).setAuthorisationId("");
        profile.getAuthorisations().get(1).setAuthorisationId("");
        profile.getAuthorisations().get(2).setAuthorisationId("");

        sut.validateUserProfiles(Collections.singletonList(profile),
                judicialUser,
                mockInteger,
                invalidJudicialProfiles,
                UserType.JUDICIAL);
        Mockito.verify(mockInteger, Mockito.times(1)).getAndIncrement();
    }

}
