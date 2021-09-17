package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_STCW;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_TCW;


@RunWith(MockitoJUnitRunner.class)
class ParseRequestServiceTest {

    ParseRequestService sut = new ParseRequestService();
    Set<JudicialProfile> invalidJudicialProfiles = new HashSet<>();
    UserRequest judicialUser = TestDataBuilder.buildUserRequest();
    AtomicInteger mockInteger = mock(AtomicInteger.class);
    ObjectMapper objectMapper = new ObjectMapper();
    JudicialProfile judicialProfile;

    @BeforeEach
    void setupReadFromFile() throws IOException {
        objectMapper.registerModule(new JavaTimeModule());
        judicialProfile =
                objectMapper.readValue(new File("src/main/resources/judicialProfileSample.json"),
                        JudicialProfile.class);
    }

    HashSet<CaseWorkerProfile> invalidProfiles;
    HashSet<CaseWorkerProfile> invalidProfilesSpy;

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

        verify(spyInteger, Mockito.times(0)).getAndIncrement();
        verify(invalidProfilesSpy, Mockito.times(0)).add(any(CaseWorkerProfile.class));

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
        verify(spyInteger, Mockito.times(0)).getAndIncrement();
        verify(invalidProfilesSpy, Mockito.times(0)).add(any(CaseWorkerProfile.class));
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
        verify(spyInteger, Mockito.times(0)).getAndIncrement();
        verify(invalidProfilesSpy, Mockito.times(0)).add(any(CaseWorkerProfile.class));
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
        verify(spyInteger, Mockito.times(0)).getAndIncrement();
        verify(invalidProfilesSpy, Mockito.times(0)).add(any(CaseWorkerProfile.class));
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
        verify(spyInteger, Mockito.times(0)).getAndIncrement();
        verify(invalidProfilesSpy, Mockito.times(0)).add(any(CaseWorkerProfile.class));
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
        UserRequest judicialUserRequest = TestDataBuilder.buildUserRequestIndividual();

        sut.validateUserProfiles(Collections.singletonList(judicialProfile),
                judicialUserRequest,
                new AtomicInteger(),
                invalidJudicialProfiles,
                UserType.JUDICIAL);
    }

    @Test
    void judicialValidationTest_noContractTypeId() {
        judicialProfile.getAppointments().get(0).setContractTypeId("");
        judicialProfile.getAppointments().get(1).setContractTypeId("");

        sut.validateUserProfiles(Collections.singletonList(judicialProfile),
                TestDataBuilder.buildUserRequestIndividual(),
                mockInteger,
                invalidJudicialProfiles,
                UserType.JUDICIAL);
        Mockito.verify(mockInteger, Mockito.times(1)).getAndIncrement();
    }

    @Test
    void judicialValidationTest_noBaseLocationId() {
        judicialProfile.getAppointments().get(0).setBaseLocationId("");
        judicialProfile.getAppointments().get(1).setBaseLocationId("");

        sut.validateUserProfiles(Collections.singletonList(judicialProfile),
                TestDataBuilder.buildUserRequestIndividual(),
                mockInteger,
                invalidJudicialProfiles,
                UserType.JUDICIAL);
        Mockito.verify(mockInteger, Mockito.times(1)).getAndIncrement();
    }

    @Test
    void judicialValidationTest_noLocationId() {
        judicialProfile.getAppointments().get(0).setLocationId("");
        judicialProfile.getAppointments().get(1).setLocationId("");

        sut.validateUserProfiles(Collections.singletonList(judicialProfile),
                TestDataBuilder.buildUserRequestIndividual(),
                mockInteger,
                invalidJudicialProfiles,
                UserType.JUDICIAL);
        Mockito.verify(mockInteger, Mockito.times(1)).getAndIncrement();
    }

    @Test
    void judicialValidationTest_NoAppointment() {
        judicialProfile.setAppointments(new ArrayList<>());

        sut.validateUserProfiles(Collections.singletonList(judicialProfile),
                judicialUser,
                mockInteger,
                invalidJudicialProfiles,
                UserType.JUDICIAL);
        Mockito.verify(mockInteger, Mockito.times(1)).getAndIncrement();
    }

    @Test
    void judicialValidationTest_NoAppointmentRoleId() {
        judicialProfile.getAppointments().get(0).setRoleId("");
        judicialProfile.getAppointments().get(1).setRoleId("");

        sut.validateUserProfiles(Collections.singletonList(judicialProfile),
                judicialUser,
                mockInteger,
                invalidJudicialProfiles,
                UserType.JUDICIAL);
        Mockito.verify(mockInteger, Mockito.times(1)).getAndIncrement();
    }

    @Test
    void judicialValidationTest_NoAuthorisation() {
        judicialProfile.setAuthorisations(new ArrayList<>());

        sut.validateUserProfiles(Collections.singletonList(judicialProfile),
                judicialUser,
                mockInteger,
                invalidJudicialProfiles,
                UserType.JUDICIAL);
        Mockito.verify(mockInteger, Mockito.times(1)).getAndIncrement();
    }

    @Test
    void judicialValidationTest_NoAuthorisationId() {
        judicialProfile.getAuthorisations().get(0).setTicketCode("");
        judicialProfile.getAuthorisations().get(1).setTicketCode("");
        judicialProfile.getAuthorisations().get(2).setTicketCode("");

        sut.validateUserProfiles(Collections.singletonList(judicialProfile),
                judicialUser,
                mockInteger,
                invalidJudicialProfiles,
                UserType.JUDICIAL);
        Mockito.verify(mockInteger, Mockito.times(1)).getAndIncrement();
    }

}
