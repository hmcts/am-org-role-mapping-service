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
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.UnauthorizedServiceException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.UnprocessableEntityException;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.COMPLETED;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.NEW;

@RunWith(MockitoJUnitRunner.class)
class RefreshOrchestratorTest {

    @SuppressWarnings("unchecked")
    private final RequestMappingService<UserAccessProfile> requestMappingService
            = (RequestMappingService<UserAccessProfile>)mock(RequestMappingService.class);
    private final RetrieveDataService retrieveDataService = mock(RetrieveDataService.class);
    private final ParseRequestService parseRequestService = mock(ParseRequestService.class);
    private final CRDService crdService = mock(CRDService.class);
    private final PersistenceService persistenceService = mock(PersistenceService.class);
    private final FeignException feignClientException = mock(FeignException.NotFound.class);
    private final SecurityUtils securityUtils = mock(SecurityUtils.class);
    private final JudicialBookingService judicialBookingService = mock(JudicialBookingService.class);

    @InjectMocks
    private final RefreshOrchestrator sut = new RefreshOrchestrator(
            retrieveDataService,
            requestMappingService,
            parseRequestService,
            crdService,
            persistenceService,
            securityUtils,
            judicialBookingService,
            "1",
            "descending",
            "1",
            List.of("am_org_role_mapping_service", "am_role_assignment_refresh_batch"));

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void refreshRoleAssignmentRecords() {

        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());

        Map<String, Set<UserAccessProfile>> userAccessProfiles = new HashMap<>();
        Set<UserAccessProfile> userAccessProfileSet = new HashSet<>();
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

        Mockito.when(requestMappingService.createCaseworkerAssignments(any()))
                .thenReturn((ResponseEntity.status(HttpStatus.OK)
                        .body(Collections.emptyList())));

        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());

        Mockito.when(persistenceService.fetchRefreshJobById(any()))
                .thenReturn(Optional.of(
                        RefreshJobEntity.builder()
                                .roleCategory(RoleCategory.LEGAL_OPERATIONS.toString())
                                .status(NEW)
                                .build()));

        ResponseEntity<Object> response = sut.refresh(1L, TestDataBuilder.buildUserRequest());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response);
    }

    @Test
    void refreshRoleAssignmentRecords_profileNotFound() {

        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());

        Mockito.when(retrieveDataService.retrieveProfiles(any(), eq(UserType.CASEWORKER)))
                .thenThrow(FeignException.NotFound.class);

        Mockito.when(requestMappingService.createCaseworkerAssignments(any()))
                .thenReturn((ResponseEntity.status(HttpStatus.OK)
                        .body(Collections.emptyList())));

        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());

        Mockito.when(persistenceService.fetchRefreshJobById(any()))
                .thenReturn(Optional.of(
                        RefreshJobEntity.builder()
                                .roleCategory(RoleCategory.LEGAL_OPERATIONS.toString())
                                .status(NEW)
                                .build()));

        assertNull(sut.refresh(1L, TestDataBuilder.buildUserRequest()));

    }

    @Test
    void refreshRoleAssignmentRecords_nullUserRequest() {

        Mockito.when(persistenceService.fetchRefreshJobById(any()))
                .thenReturn(Optional.of(
                        RefreshJobEntity.builder()
                                .roleCategory(RoleCategory.LEGAL_OPERATIONS.toString())
                                .status(NEW)
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

        Mockito.when(requestMappingService.createCaseworkerAssignments(any()))
                .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));

        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());

        ResponseEntity<Object> response = sut.refresh(1L, UserRequest.builder().build());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response);
    }

    @Test
    @DisplayName("refreshRoleAssignmentJudicialRecords_nullUserRequest")
    void refreshRoleAssignmentJudicialRecords_nullUserRequest() {

        Mockito.when(persistenceService.fetchRefreshJobById(any()))
                .thenReturn(Optional.of(
                        RefreshJobEntity.builder()
                                .roleCategory(RoleCategory.JUDICIAL.toString())
                                .status(NEW)
                                .build()));
        List<CaseWorkerProfilesResponse> userProfilesResponseList = new ArrayList<>();
        userProfilesResponseList.add(TestDataBuilder.buildUserProfilesResponse());
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("total_records", "4");

        ResponseEntity<List<CaseWorkerProfilesResponse>> responseEntity
                = new ResponseEntity<>(userProfilesResponseList, headers, HttpStatus.OK);

        //TODO NBP but after AM-2902 mergerd into Master
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
        //TODO should call Judicial but after AM-2902 mergerd into Master
        Mockito.when(requestMappingService.createCaseworkerAssignments(any()))
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
                                .status(NEW)
                                .build()));

        Mockito.when(crdService.fetchCaseworkerDetailsByServiceName(
                any(), any(), any(), any(), any()))
                .thenThrow(feignClientException);

        UserRequest userRequestSpy = Mockito.spy(UserRequest.builder().build());

        sut.refresh(1L, userRequestSpy);

        verify(persistenceService, Mockito.times(1)).persistRefreshJob(any());
    }

    @Test
    @DisplayName("nullJobIdTest_validate")
    void nullJobIdTest_validate() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        String errorMessage = "Invalid JobId request";
        Mockito.when(securityUtils.getServiceName())
                .thenReturn("am_role_assignment_refresh_batch");

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                sut.validate(null, userRequest));
        assertTrue(exception.getLocalizedMessage().contains(errorMessage));
    }

    @Test
    @DisplayName("invalidServiceTokenTest_validate")
    void invalidServiceTokenTest_validate() {
        String errorMessage = "Invoking service is not permitted to call the Refresh API";
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());
        Mockito.when(securityUtils.getServiceName()).thenReturn("ccd_gw");

        UnauthorizedServiceException exception = assertThrows(UnauthorizedServiceException.class, () ->
                sut.validate(1L, userRequest));
        assertTrue(exception.getLocalizedMessage().contains(errorMessage));
    }

    @Test
    void validateTest() {
        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());
        Mockito.when(securityUtils.getServiceName())
                .thenReturn("am_role_assignment_refresh_batch");
        sut.validate(1L, TestDataBuilder.buildUserRequest());
        Mockito.verify(parseRequestService, Mockito.times(1)).validateUserRequest(any());
    }

    @Test
    void validateTest_emptyUserIds() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        userRequest.setUserIds(new ArrayList<>());
        Mockito.when(securityUtils.getServiceName())
                .thenReturn("am_role_assignment_refresh_batch");
        sut.validate(1L, userRequest);
        Mockito.verify(parseRequestService, Mockito.times(0)).validateUserRequest(any());
    }

    @Test
    void validateTest_nullRequest() {
        Mockito.when(securityUtils.getServiceName())
                .thenReturn("am_role_assignment_refresh_batch");
        sut.validate(1L, null);
        Mockito.verify(parseRequestService, Mockito.times(0)).validateUserRequest(any());
    }

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

        Mockito.when(requestMappingService.createCaseworkerAssignments(any()))
                .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));

        RefreshJobEntity refreshJobEntity =
                RefreshJobEntity.builder().roleCategory(RoleCategory.ADMIN.name()).jurisdiction("LDN").build();
        RefreshJobEntity refreshJobEntitySpy = Mockito.spy(refreshJobEntity);

        Map<String, HttpStatus> responseCodeWithUserId = new HashMap<>();
        responseCodeWithUserId.put("1234", HttpStatus.CREATED);
        sut.refreshJobByServiceName(responseCodeWithUserId, refreshJobEntitySpy, UserType.CASEWORKER);
        verify(refreshJobEntitySpy, Mockito.times(1)).setStatus(any());
        verify(refreshJobEntitySpy, Mockito.times(1)).setCreated(any());
        verify(refreshJobEntitySpy, Mockito.times(1)).setLog(any());
        verify(persistenceService, Mockito.times(1)).persistRefreshJob(any());
    }

    @Test
    void refreshJobByServiceWithInvalidRoleCategory() {

        List<CaseWorkerProfilesResponse> userProfilesResponseList = new ArrayList<>();
        userProfilesResponseList.add(TestDataBuilder.buildUserProfilesResponse());
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("total_records", "4");

        ResponseEntity<List<CaseWorkerProfilesResponse>> responseEntity
            = new ResponseEntity<>(userProfilesResponseList, headers, HttpStatus.OK);


        doReturn(responseEntity).when(crdService)
            .fetchCaseworkerDetailsByServiceName(any(), any(), any(), any(), any());

        Mockito.when(requestMappingService.createCaseworkerAssignments(any()))
            .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));

        RefreshJobEntity refreshJobEntity =
            RefreshJobEntity.builder().roleCategory("ABC").jurisdiction("LDN").build();
        RefreshJobEntity refreshJobEntitySpy = Mockito.spy(refreshJobEntity);

        Map<String, HttpStatus> responseCodeWithUserId = new HashMap<>();
        responseCodeWithUserId.put("1234", HttpStatus.CREATED);

        Assertions.assertThrows(BadRequestException.class, () ->
            sut.refreshJobByServiceName(responseCodeWithUserId, refreshJobEntitySpy, UserType.CASEWORKER)
        );
    }

    @Test
    void refreshJobByServiceNameWithNoPageSize() {

        List<CaseWorkerProfilesResponse> userProfilesResponseList = new ArrayList<>();
        userProfilesResponseList.add(TestDataBuilder.buildUserProfilesResponse());
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("total_records", "4");

        ResponseEntity<List<CaseWorkerProfilesResponse>> responseEntity
            = new ResponseEntity<>(userProfilesResponseList, headers, HttpStatus.OK);


        doReturn(responseEntity).when(crdService)
            .fetchCaseworkerDetailsByServiceName(any(), any(), any(), any(), any());

        Mockito.when(requestMappingService.createCaseworkerAssignments(any()))
            .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));

        RefreshJobEntity refreshJobEntity =
            RefreshJobEntity.builder().roleCategory(RoleCategory.ADMIN.name()).jurisdiction("LDN").build();
        RefreshJobEntity refreshJobEntitySpy = Mockito.spy(refreshJobEntity);

        Map<String, HttpStatus> responseCodeWithUserId = new HashMap<>();
        responseCodeWithUserId.put("1234", HttpStatus.CREATED);
        sut.pageSize = "0";
        sut.refreshJobByServiceName(responseCodeWithUserId, refreshJobEntitySpy, UserType.CASEWORKER);
        verify(refreshJobEntitySpy, Mockito.times(1)).setStatus(any());
    }

    @Test
    void refreshJobByServiceNameWithDecimalPageSize() {

        List<CaseWorkerProfilesResponse> userProfilesResponseList = new ArrayList<>();
        userProfilesResponseList.add(TestDataBuilder.buildUserProfilesResponse());
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("total_records", "3");

        ResponseEntity<List<CaseWorkerProfilesResponse>> responseEntity
            = new ResponseEntity<>(userProfilesResponseList, headers, HttpStatus.OK);


        doReturn(responseEntity).when(crdService)
            .fetchCaseworkerDetailsByServiceName(any(), any(), any(), any(), any());

        Mockito.when(requestMappingService.createCaseworkerAssignments(any()))
            .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));

        RefreshJobEntity refreshJobEntity =
            RefreshJobEntity.builder().roleCategory(RoleCategory.ADMIN.name()).jurisdiction("LDN").build();
        RefreshJobEntity refreshJobEntitySpy = Mockito.spy(refreshJobEntity);

        Map<String, HttpStatus> responseCodeWithUserId = new HashMap<>();
        responseCodeWithUserId.put("1234", HttpStatus.CREATED);
        sut.pageSize = "2";
        sut.refreshJobByServiceName(responseCodeWithUserId, refreshJobEntitySpy, UserType.CASEWORKER);
        verify(refreshJobEntitySpy, Mockito.times(1)).setStatus(any());
    }

    @Test
    void refreshJobByServiceName_FeignException() {
        Map<String, HttpStatus> responseCodeWithUserId = new HashMap<>();
        responseCodeWithUserId.put("1234", HttpStatus.CREATED);

        Mockito.when(crdService.fetchCaseworkerDetailsByServiceName(
                any(), any(), any(), any(), any()))
                .thenThrow(feignClientException);

        Mockito.when(requestMappingService.createCaseworkerAssignments(any()))
                .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));

        RefreshJobEntity refreshJobEntity =
                RefreshJobEntity.builder().roleCategory(RoleCategory.ADMIN.name()).jurisdiction("LDN").build();
        RefreshJobEntity refreshJobEntitySpy = Mockito.spy(refreshJobEntity);

        sut.refreshJobByServiceName(responseCodeWithUserId, refreshJobEntitySpy, UserType.CASEWORKER);

        verify(refreshJobEntitySpy, Mockito.times(1)).setStatus(any());
        verify(refreshJobEntitySpy, Mockito.times(1)).setCreated(any());
        verify(refreshJobEntitySpy, Mockito.times(1)).setLog(any());
        verify(persistenceService, Mockito.times(1)).persistRefreshJob(any());
    }

    @Test
    void prepareResponseCodes() {
        Map<String, HttpStatus> responseEntityMap = new HashMap<>();
        responseEntityMap.put("1234", HttpStatus.CREATED);

        Map<String, Set<UserAccessProfile>> userAccessProfiles = new HashMap<>();
        Set<UserAccessProfile> userAccessProfileSet = new HashSet<>();
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

        Mockito.when(requestMappingService.createCaseworkerAssignments(any()))
                .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));

        ResponseEntity<Object> result = sut.prepareResponseCodes(responseEntityMap, userAccessProfiles,
                UserType.CASEWORKER);
        assertNotNull(result);
        assertNotNull(result.getBody());

    }

    @Test
    @SuppressWarnings("unchecked")
    void prepareResponseCodesWithValue() {
        Map<String, HttpStatus> responseEntityMap = new HashMap<>();
        responseEntityMap.put("1234", HttpStatus.CREATED);

        Map<String, Set<UserAccessProfile>> userAccessProfiles = new HashMap<>();
        Set<UserAccessProfile> userAccessProfileSet = new HashSet<>();
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
        List<ResponseEntity<Object>> responseEntities = List.of(
            ResponseEntity.ok(new RoleAssignmentRequestResource(AssignmentRequestBuilder
            .buildAssignmentRequest(false))),
            ResponseEntity.ok(new RoleAssignmentRequestResource(AssignmentRequestBuilder
                .buildAssignmentRequest(false))));
        Mockito.when(requestMappingService.createCaseworkerAssignments(any()))
            .thenReturn((ResponseEntity.status(HttpStatus.OK)
                    .body(responseEntities)));

        ResponseEntity<Object> result = sut.prepareResponseCodes(responseEntityMap, userAccessProfiles,
            UserType.CASEWORKER);
        assertNotNull(result);
        assertNotNull(result.getBody());
        assertEquals(responseEntities.size(),((List<ResponseEntity>)result.getBody()).size());
        assertEquals(responseEntities.size(),responseEntityMap.size());

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
        verify(refreshJobEntitySpy, Mockito.times(0)).setStatus(any());
    }

    @Test
    void refreshRoleAssignmentRecords_judicial() {
        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());
        Map<String, Set<UserAccessProfile>> userAccessProfiles = new HashMap<>();
        Set<UserAccessProfile> userAccessProfileSet = new HashSet<>();

        userAccessProfileSet.add(JudicialAccessProfile.builder()
                .userId("1")
                .roleId("1")
                .primaryLocationId("1")
                .serviceCode("1")
                .build());

        userAccessProfiles.put("1", userAccessProfileSet);
        RefreshOrchestrator refreshOrchestrator = Mockito.spy(sut);
        Mockito.when(retrieveDataService.retrieveProfiles(any(), eq(UserType.JUDICIAL)))
                .thenReturn(userAccessProfiles);

        Mockito.when(requestMappingService.createJudicialAssignments(any(), any()))
                .thenReturn((ResponseEntity.status(HttpStatus.OK)
                        .body(Collections.emptyList())));

        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());

        Mockito.when(persistenceService.fetchRefreshJobById(any()))
                .thenReturn(Optional.of(
                        RefreshJobEntity.builder()
                                .roleCategory(RoleCategory.JUDICIAL.toString())
                                .status(NEW)
                                .build()));

        ResponseEntity<Object> response = refreshOrchestrator.refresh(1L, TestDataBuilder.buildUserRequest());
        Mockito.verify(refreshOrchestrator,Mockito.times(1)).buildSuccessAndFailureBucket(any(),any());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response);
    }

    @Test
    @DisplayName("refreshRoleAssignmentRecordsCouldNotBeRetrieved_Exception")
    void refreshRoleAssignmentRecordsCouldNotBeRetrieved_Exception() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        String uee = "Provided refresh job couldn't be retrieved.";
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,() ->
                sut.refresh(1L, userRequest));
        assertTrue(exception.getLocalizedMessage().contains(uee));
    }

    @Test
    @DisplayName("refreshRoleAssignmentRecordsInvalidStatus_Exception")
    void refreshRoleAssignmentRecordsInvalidStatus_Exception() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        String uee = "Provided refresh job is in an invalid state.";

        Mockito.when(persistenceService.fetchRefreshJobById(any()))
                .thenReturn(Optional.of(
                        RefreshJobEntity.builder()
                                .roleCategory(RoleCategory.JUDICIAL.toString())
                                .status(COMPLETED)
                                .build()));

        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,() ->
                sut.refresh(1L, userRequest));

        assertTrue(exception.getLocalizedMessage().contains(uee));
    }
}
