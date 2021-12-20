package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import feign.FeignException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.UnprocessableEntityException;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
class RefreshOrchestratorTest {


    private final RetrieveDataService retrieveDataService = mock(RetrieveDataService.class);
    private final RequestMappingService requestMappingService = mock(RequestMappingService.class);
    private final ParseRequestService parseRequestService = mock(ParseRequestService.class);
    private final CRDService crdService = mock(CRDService.class);
    private final PersistenceService persistenceService = mock(PersistenceService.class);
    private final FeignException feignClientException = mock(FeignException.NotFound.class);

    @InjectMocks
    private final RefreshOrchestrator sut = new RefreshOrchestrator(
            retrieveDataService,
            requestMappingService,
            parseRequestService,
            crdService,
            persistenceService,
            "1",
            "descending",
            "1");

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    void refreshRoleAssignmentRecords() throws IOException {

        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());

        Map<String, Set<?>> userAccessProfiles = new HashMap<>();
        Set<CaseWorkerAccessProfile> userAccessProfileSet = new HashSet<>();
        userAccessProfileSet.add(CaseWorkerAccessProfile.builder()
                .id("1")
                .roleId("1")
                .roleName("roleName")
                .primaryLocationName("primary")
                .primaryLocationId("1")
                .areaOfWorkId("1")
                .serviceCode("1")
                .suspended(false)
                .build());
        userAccessProfiles.put("1", userAccessProfileSet);

        Mockito.when(retrieveDataService.retrieveProfiles(any(), eq(UserType.CASEWORKER)))
                .thenReturn(userAccessProfiles);

        Mockito.when(requestMappingService.createAssignments(any(), eq(UserType.CASEWORKER)))
                .thenReturn((ResponseEntity.status(HttpStatus.OK)
                        .body(Collections.emptyList())));

        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());

        Mockito.when(persistenceService.fetchRefreshJobById(any()))
                .thenReturn(Optional.of(
                        RefreshJobEntity.builder()
                                .roleCategory(RoleCategory.LEGAL_OPERATIONS.toString())
                                .build()));

        ResponseEntity<Object> response = sut.refresh(1L, TestDataBuilder.buildUserRequest());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response);
    }

    @SuppressWarnings("unchecked")
    @Test
    void refreshRoleAssignmentRecords_nullUserRequest() {

        Mockito.when(persistenceService.fetchRefreshJobById(any()))
                .thenReturn(Optional.of(
                        RefreshJobEntity.builder()
                                .roleCategory(RoleCategory.LEGAL_OPERATIONS.toString())
                                .build()));
        List<CaseWorkerProfilesResponse> userProfilesResponseList = new ArrayList<>();
        userProfilesResponseList.add(TestDataBuilder.buildUserProfilesResponse());
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("total_records", "4");

        ResponseEntity<List<CaseWorkerProfilesResponse>> responseEntity
                = new ResponseEntity<>(userProfilesResponseList, headers, HttpStatus.OK);


        doReturn(responseEntity).when(crdService)
                .fetchCaseworkerDetailsByServiceName(any(), any(), any(), any(), any());

        Mockito.doNothing().when(parseRequestService)
                .validateUserRequest(any());

        Map<String, Set<CaseWorkerAccessProfile>> userAccessProfiles = new HashMap<>();
        Set<CaseWorkerAccessProfile> userAccessProfileSet = new HashSet<>();
        userAccessProfileSet.add(CaseWorkerAccessProfile.builder()
                .id("1")
                .roleId("1")
                .roleName("roleName")
                .primaryLocationName("primary")
                .primaryLocationId("1")
                .areaOfWorkId("1")
                .serviceCode("1")
                .suspended(false)
                .build());
        userAccessProfiles.put("1", userAccessProfileSet);

        doReturn(userAccessProfiles).when(retrieveDataService)
                .retrieveProfiles(any(), eq(UserType.CASEWORKER));

        Mockito.when(requestMappingService.createAssignments(any(), eq(UserType.CASEWORKER)))
                .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));

        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());

        ResponseEntity<Object> response = sut.refresh(1L, UserRequest.builder().build());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("refreshRoleAssignmentJudicialRecords_nullUserRequest")
    void refreshRoleAssignmentJudicialRecords_nullUserRequest() throws IOException {

        Mockito.when(persistenceService.fetchRefreshJobById(any()))
                .thenReturn(Optional.of(
                        RefreshJobEntity.builder()
                                .roleCategory(RoleCategory.JUDICIAL.toString())
                                .build()));
        List<CaseWorkerProfilesResponse> userProfilesResponseList = new ArrayList<>();
        userProfilesResponseList.add(TestDataBuilder.buildUserProfilesResponse());
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("total_records", "4");

        ResponseEntity<List<CaseWorkerProfilesResponse>> responseEntity
                = new ResponseEntity<>(userProfilesResponseList, headers, HttpStatus.OK);


        doReturn(responseEntity).when(crdService)
                .fetchCaseworkerDetailsByServiceName(any(), any(), any(), any(), any());

        Mockito.doNothing().when(parseRequestService)
                .validateUserRequest(any());

        Map<String, Set<CaseWorkerAccessProfile>> userAccessProfiles = new HashMap<>();
        Set<CaseWorkerAccessProfile> userAccessProfileSet = new HashSet<>();
        userAccessProfileSet.add(CaseWorkerAccessProfile.builder()
                .id("1")
                .roleId("1")
                .roleName("roleName")
                .primaryLocationName("primary")
                .primaryLocationId("1")
                .areaOfWorkId("1")
                .serviceCode("1")
                .suspended(false)
                .build());
        userAccessProfiles.put("1", userAccessProfileSet);

        doReturn(userAccessProfiles).when(retrieveDataService)
                .retrieveProfiles(any(), eq(UserType.JUDICIAL));

        Mockito.when(requestMappingService.createAssignments(any(), eq(UserType.JUDICIAL)))
                .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));

        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());

        ResponseEntity<Object> response = sut.refresh(1L, UserRequest.builder().build());

        assertNull(response);
    }


    @Test
    void refreshRoleAssignmentRecords_nullUserRequest_feignException() {
        Mockito.when(persistenceService.fetchRefreshJobById(any()))
                .thenReturn(Optional.of(
                        RefreshJobEntity.builder()
                                .roleCategory(RoleCategory.LEGAL_OPERATIONS.toString())
                                .build()));

        Mockito.when(crdService.fetchCaseworkerDetailsByServiceName(
                any(), any(), any(), any(), any()))
                .thenThrow(feignClientException);

        UserRequest userRequestSpy = Mockito.spy(UserRequest.builder().build());

        sut.refresh(1L, userRequestSpy);

        verify(persistenceService, Mockito.times(1)).persistRefreshJob(any());
        verify(userRequestSpy, Mockito.times(1)).getUserIds();
    }

    @Test
    @DisplayName("nullJobIdTest_validate")
    void nullJobIdTest_validate() {
        StringBuilder builder = new StringBuilder();
        //builder.append("uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException: ");
        builder.append("Invalid JobId request");

        BadRequestException exception = Assertions.assertThrows(BadRequestException.class, () ->
                sut.validate(null, TestDataBuilder.buildUserRequest()));
        Assertions.assertTrue(exception.getLocalizedMessage().contains(builder.toString()));

    }

    @Test
    void validateTest() {
        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());
        sut.validate(1L, TestDataBuilder.buildUserRequest());
        Mockito.verify(parseRequestService, Mockito.times(1)).validateUserRequest(any());
    }

    @Test
    void validateTest_emptyUserIds() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        userRequest.setUserIds(new ArrayList<>());
        sut.validate(1L, userRequest);
        Mockito.verify(parseRequestService, Mockito.times(0)).validateUserRequest(any());
    }

    @Test
    void validateTest_nullRequest() {
        sut.validate(1L, null);
        Mockito.verify(parseRequestService, Mockito.times(0)).validateUserRequest(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void refreshJobByServiceName() {

        List<CaseWorkerProfilesResponse> userProfilesResponseList = new ArrayList<>();
        userProfilesResponseList.add(TestDataBuilder.buildUserProfilesResponse());
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("total_records", "4");

        ResponseEntity<List<CaseWorkerProfilesResponse>> responseEntity
                = new ResponseEntity<>(userProfilesResponseList, headers, HttpStatus.OK);


        doReturn(responseEntity).when(crdService)
                .fetchCaseworkerDetailsByServiceName(any(), any(), any(), any(), any());

        Mockito.when(requestMappingService.createAssignments(any(), eq(UserType.CASEWORKER)))
                .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));

        RefreshJobEntity refreshJobEntity =
                RefreshJobEntity.builder().roleCategory(RoleCategory.ADMIN.name()).jurisdiction("LDN").build();
        RefreshJobEntity refreshJobEntitySpy = Mockito.spy(refreshJobEntity);

        Map<String, HttpStatus> responseCodeWithUserId = new HashMap<>();
        responseCodeWithUserId.put("1234", HttpStatus.CREATED);
        Map<String, HttpStatus> responseCodeWithUserIdSpy = Mockito.spy(responseCodeWithUserId);

        sut.refreshJobByServiceName(responseCodeWithUserIdSpy, refreshJobEntitySpy, UserType.CASEWORKER);

        verify(responseCodeWithUserIdSpy, Mockito.times(4)).entrySet();

        verify(refreshJobEntitySpy, Mockito.times(1)).getRoleCategory();
        verify(refreshJobEntitySpy, Mockito.times(1)).setStatus(any());
        verify(refreshJobEntitySpy, Mockito.times(1)).setCreated(any());
        verify(refreshJobEntitySpy, Mockito.times(1)).setLog(any());
        verify(persistenceService, Mockito.times(1)).persistRefreshJob(any());
        //Judicial profile
        sut.refreshJobByServiceName(responseCodeWithUserIdSpy, refreshJobEntitySpy, UserType.JUDICIAL);
    }


    @SuppressWarnings("unchecked")
    @Test
    void refreshJobByServiceName_FeignException() {
        Map<String, HttpStatus> responseCodeWithUserId = new HashMap<>();
        responseCodeWithUserId.put("1234", HttpStatus.CREATED);
        Map<String, HttpStatus> responseCodeWithUserIdSpy = Mockito.spy(responseCodeWithUserId);

        Mockito.when(crdService.fetchCaseworkerDetailsByServiceName(
                any(), any(), any(), any(), any()))
                .thenThrow(feignClientException);

        Mockito.when(requestMappingService.createAssignments(any(), eq(UserType.CASEWORKER)))
                .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));

        RefreshJobEntity refreshJobEntity =
                RefreshJobEntity.builder().roleCategory(RoleCategory.ADMIN.name()).jurisdiction("LDN").build();
        RefreshJobEntity refreshJobEntitySpy = Mockito.spy(refreshJobEntity);

        sut.refreshJobByServiceName(responseCodeWithUserIdSpy, refreshJobEntitySpy, UserType.CASEWORKER);

        verify(responseCodeWithUserIdSpy, Mockito.times(1)).put(any(), any());

        verify(refreshJobEntitySpy, Mockito.times(1)).getRoleCategory();
        verify(refreshJobEntitySpy, Mockito.times(1)).setStatus(any());
        verify(refreshJobEntitySpy, Mockito.times(1)).setCreated(any());
        verify(refreshJobEntitySpy, Mockito.times(1)).setLog(any());
        verify(persistenceService, Mockito.times(1)).persistRefreshJob(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void prepareResponseCodes() {
        Map<String, HttpStatus> responseEntityMap = new HashMap<>();
        responseEntityMap.put("1234", HttpStatus.CREATED);

        Map<String, Set<?>> userAccessProfiles = new HashMap<>();
        Set<CaseWorkerAccessProfile> userAccessProfileSet = new HashSet<>();
        userAccessProfileSet.add(CaseWorkerAccessProfile.builder()
                .id("1")
                .roleId("1")
                .roleName("roleName")
                .primaryLocationName("primary")
                .primaryLocationId("1")
                .areaOfWorkId("1")
                .serviceCode("1")
                .suspended(false)
                .build());
        userAccessProfiles.put("1", userAccessProfileSet);

        Mockito.when(requestMappingService.createAssignments(any(), eq(UserType.CASEWORKER)))
                .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));

        ResponseEntity<Object> result = sut.prepareResponseCodes(responseEntityMap, userAccessProfiles,
                UserType.CASEWORKER);
        assertNotNull(result);
        assertNotNull(result.getBody());

    }



    @Test
    void buildSuccessAndFailureBucket_Success() {

        Map<String, HttpStatus> responseEntityMap = new HashMap<>();
        responseEntityMap.put("1234", HttpStatus.CREATED);

        RefreshJobEntity refreshJobEntitySpy = Mockito.spy(TestDataBuilder.buildRefreshJobEntity());

        sut.buildSuccessAndFailureBucket(responseEntityMap, refreshJobEntitySpy);


        verify(refreshJobEntitySpy, Mockito.times(1)).setStatus(any());
        verify(refreshJobEntitySpy, Mockito.times(1)).setCreated(any());
        verify(refreshJobEntitySpy, Mockito.times(1)).setLog(any());
        verify(persistenceService, Mockito.times(1)).persistRefreshJob(any());
    }

    @Test
    void buildSuccessAndFailureBucket_Failure() {

        Map<String, HttpStatus> responseEntityMap = new HashMap<>();
        responseEntityMap.put("1234", HttpStatus.CONFLICT);

        RefreshJobEntity refreshJobEntitySpy = Mockito.spy(TestDataBuilder.buildRefreshJobEntity());

        sut.buildSuccessAndFailureBucket(responseEntityMap, refreshJobEntitySpy);


        verify(refreshJobEntitySpy, Mockito.times(1)).setStatus(any());
        verify(refreshJobEntitySpy, Mockito.times(1)).setUserIds(any());
        verify(refreshJobEntitySpy, Mockito.times(1)).setCreated(any());
        verify(refreshJobEntitySpy, Mockito.times(1)).setLog(any());
        verify(persistenceService, Mockito.times(1)).persistRefreshJob(any());
    }

    @Test
    void updateJobStatus_Success() {
        String successId = "1234";
        RefreshJobEntity refreshJobEntitySpy = Mockito.spy(TestDataBuilder.buildRefreshJobEntity());
        sut.updateJobStatus(
                Collections.singletonList(successId),
                new ArrayList<>(),
                refreshJobEntitySpy);

        verify(refreshJobEntitySpy, Mockito.times(1)).setStatus(any());
        verify(refreshJobEntitySpy, Mockito.times(1)).setCreated(any());
        verify(refreshJobEntitySpy, Mockito.times(1)).setLog(any());
        verify(persistenceService, Mockito.times(1)).persistRefreshJob(any());

    }

    @Test
    void updateJobStatus_Failure() {
        String failureId = "1234";
        RefreshJobEntity refreshJobEntitySpy = Mockito.spy(TestDataBuilder.buildRefreshJobEntity());
        sut.updateJobStatus(
                new ArrayList<>(),
                Collections.singletonList(failureId),
                refreshJobEntitySpy);

        verify(refreshJobEntitySpy, Mockito.times(1)).setStatus(any());
        verify(refreshJobEntitySpy, Mockito.times(1)).setUserIds(any());
        verify(refreshJobEntitySpy, Mockito.times(1)).setCreated(any());
        verify(refreshJobEntitySpy, Mockito.times(1)).setLog(any());
        verify(persistenceService, Mockito.times(1)).persistRefreshJob(any());

    }

    @Test
    @DisplayName("updateJobStatus_EmptyList")
    void updateJobStatus_EmptyList() {
        RefreshJobEntity refreshJobEntitySpy = Mockito.spy(TestDataBuilder.buildRefreshJobEntity());
        sut.updateJobStatus(
                Collections.emptyList(),
                Collections.emptyList(),
                refreshJobEntitySpy);
    }

    @SuppressWarnings("unchecked")
    @Test
    void refreshRoleAssignmentRecords_judicial() {

        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());

        Map<String, Set<?>> userAccessProfiles = new HashMap<>();
        Set<CaseWorkerAccessProfile> userAccessProfileSet = new HashSet<>();
        userAccessProfileSet.add(CaseWorkerAccessProfile.builder()
                .id("1")
                .roleId("1")
                .roleName("roleName")
                .primaryLocationName("primary")
                .primaryLocationId("1")
                .areaOfWorkId("1")
                .serviceCode("1")
                .suspended(false)
                .build());
        userAccessProfiles.put("1", userAccessProfileSet);

        Mockito.when(retrieveDataService.retrieveProfiles(any(), eq(UserType.JUDICIAL)))
                .thenReturn(userAccessProfiles);

        Mockito.when(requestMappingService.createAssignments(any(), eq(UserType.JUDICIAL)))
                .thenReturn((ResponseEntity.status(HttpStatus.OK)
                        .body(Collections.emptyList())));

        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());

        Mockito.when(persistenceService.fetchRefreshJobById(any()))
                .thenReturn(Optional.of(
                        RefreshJobEntity.builder()
                                .roleCategory(RoleCategory.JUDICIAL.toString())
                                .build()));

        ResponseEntity<Object> response = sut.refresh(1L, TestDataBuilder.buildUserRequest());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("refreshRoleAssignmentRecords_Exception")
    void refreshRoleAssignmentRecords_Exception() {

        UnprocessableEntityException exception = Assertions.assertThrows(UnprocessableEntityException.class, () ->
            sut.refresh(1L, TestDataBuilder.buildUserRequest()));
        assertTrue(exception.getLocalizedMessage().contains("Provided refresh job couldn't be retrieved."));
    }

}
