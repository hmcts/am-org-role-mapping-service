package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import feign.FeignException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.COMPLETED;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.NEW;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshOrchestrator.ERROR_INVALID_JOB_ID;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshOrchestrator.ERROR_REFRESH_JOB_INVALID_STATE;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshOrchestrator.ERROR_REFRESH_JOB_NOT_FOUND;
import static uk.gov.hmcts.reform.orgrolemapping.v1.V1.Error.UNAUTHORIZED_SERVICE;

@ExtendWith(MockitoExtension.class)
class RefreshOrchestratorTest {

    static final String S2S_CCD_GW = "ccd_gw";
    static final String S2S_ORM = "am_org_role_mapping_service";
    static final String S2S_RARB = "am_role_assignment_refresh_batch";

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

    @Captor
    private ArgumentCaptor<List<String>> userIdsCaptor;

    @InjectMocks
    private final RefreshOrchestrator sut = createRefreshOrchestrator(true);

    private RefreshOrchestrator createRefreshOrchestrator(boolean includeJudicialBookings) {
        return new RefreshOrchestrator(
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
                List.of(S2S_ORM, S2S_RARB),
                includeJudicialBookings);
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

        mockFetchRefreshJobById(1L, RoleCategory.LEGAL_OPERATIONS, NEW);

        ResponseEntity<Object> response = sut.refresh(1L, TestDataBuilder.buildUserRequest());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response);
    }

    @Test
    void refreshRoleAssignmentRecords_async() {

        // GIVEN
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

        List<ResponseEntity<Object>> responseEntities = List.of(
            ResponseEntity.status(HttpStatus.CREATED).body(new RoleAssignmentRequestResource(AssignmentRequestBuilder
                .buildAssignmentRequest(false))));
        Mockito.when(requestMappingService.createCaseworkerAssignments(any()))
            .thenReturn((ResponseEntity.status(HttpStatus.OK)
                .body(responseEntities)));

        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());

        mockFetchRefreshJobById(1L, RoleCategory.LEGAL_OPERATIONS, NEW);

        // WHEN
        // NB: not async when called from here
        sut.refreshAsync(1L, TestDataBuilder.buildUserRequest());

        // THEN
        // verify has gone on to complete process
        verify(persistenceService, Mockito.times(1)).persistRefreshJob(any());
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

        mockFetchRefreshJobById(1L, RoleCategory.LEGAL_OPERATIONS, NEW);

        assertNull(sut.refresh(1L, TestDataBuilder.buildUserRequest()));

    }

    @Test
    void refreshRoleAssignmentRecords_nullUserRequest() {

        mockFetchRefreshJobById(1L, RoleCategory.LEGAL_OPERATIONS, NEW);

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

        mockFetchRefreshJobById(1L, RoleCategory.JUDICIAL, NEW);

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

        mockFetchRefreshJobById(1L, RoleCategory.LEGAL_OPERATIONS, NEW);

        Mockito.when(crdService.fetchCaseworkerDetailsByServiceName(
                any(), any(), any(), any(), any()))
                .thenThrow(feignClientException);

        UserRequest userRequestSpy = Mockito.spy(UserRequest.builder().build());

        sut.refresh(1L, userRequestSpy);

        verify(persistenceService, Mockito.times(1)).persistRefreshJob(any());
    }

    @Test
    void validateTest_errorNullJobId() {

        // GIVEN
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        Mockito.when(securityUtils.getServiceName()).thenReturn(S2S_RARB);

        // WHEN
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            sut.validate(null, userRequest)
        );

        // THEN
        assertTrue(exception.getLocalizedMessage().contains(ERROR_INVALID_JOB_ID));
    }

    @Test
    void validateTest_errorInvokingService() {

        // GIVEN
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());
        Mockito.when(securityUtils.getServiceName()).thenReturn(S2S_CCD_GW); // i.e. invalid service

        // WHEN
        UnauthorizedServiceException exception = assertThrows(UnauthorizedServiceException.class, () ->
            sut.validate(1L, userRequest)
        );

        // THEN
        assertTrue(exception.getLocalizedMessage().contains(UNAUTHORIZED_SERVICE));
    }

    @Test
    void validateTest_errorJobIdNotFound() {

        // GIVEN
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        Mockito.when(securityUtils.getServiceName()).thenReturn(S2S_RARB);
        Mockito.when(persistenceService.fetchRefreshJobById(1L))
                .thenReturn(Optional.empty()); // i.e. NOT FOUND

        // WHEN
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,() ->
            sut.validate(1L, userRequest)
        );

        // THEN
        assertTrue(exception.getLocalizedMessage().contains(ERROR_REFRESH_JOB_NOT_FOUND));
    }

    @Test
    void validateTest_errorJobInvalidState() {

        // GIVEN
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        Mockito.when(securityUtils.getServiceName()).thenReturn(S2S_RARB);
        mockFetchRefreshJobById(1L, RoleCategory.LEGAL_OPERATIONS, COMPLETED); // i.e wrong STATE

        // WHEN
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,() ->
                sut.validate(1L, userRequest)
        );

        // THEN
        assertTrue(exception.getLocalizedMessage().contains(ERROR_REFRESH_JOB_INVALID_STATE));
    }

    @Test
    void validateTest_errorPropagatedFromValidateUserRequest() {

        // GIVEN
        String errorMessage = "TEST ERROR";
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        Mockito.when(securityUtils.getServiceName()).thenReturn(S2S_RARB);
        mockFetchRefreshJobById(1L, RoleCategory.LEGAL_OPERATIONS, NEW);
        Mockito.doThrow(new BadRequestException(errorMessage))
                .when(parseRequestService).validateUserRequest(userRequest);

        // WHEN
        BadRequestException exception = assertThrows(BadRequestException.class,() ->
            sut.validate(1L, userRequest)
        );

        // THEN
        assertTrue(exception.getLocalizedMessage().contains(errorMessage));
    }

    @Test
    void validateTest_withUserIds() {

        // GIVEN
        UserRequest userRequest = TestDataBuilder.buildUserRequest(); // NB: includes usersIds
        Mockito.doNothing().when(parseRequestService).validateUserRequest(userRequest);
        Mockito.when(securityUtils.getServiceName()).thenReturn(S2S_RARB);
        mockFetchRefreshJobById(1L, RoleCategory.LEGAL_OPERATIONS, NEW);

        // WHEN
        sut.validate(1L, userRequest);

        // THEN
        Mockito.verify(parseRequestService, Mockito.times(1)).validateUserRequest(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void validateTest_emptyOrNullUserIds(List<String> userIds) {

        // GIVEN
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        userRequest.setUserIds(userIds);
        Mockito.when(securityUtils.getServiceName()).thenReturn(S2S_RARB);
        mockFetchRefreshJobById(1L, RoleCategory.LEGAL_OPERATIONS, NEW);

        // WHEN
        sut.validate(1L, userRequest);

        // THEN
        // no validation call made as no users to validate
        Mockito.verify(parseRequestService, Mockito.times(0)).validateUserRequest(any());
    }

    @Test
    void validateTest_nullRequest() {

        // GIVEN
        Mockito.when(securityUtils.getServiceName()).thenReturn(S2S_RARB);
        mockFetchRefreshJobById(2L, RoleCategory.LEGAL_OPERATIONS, NEW);

        // WHEN
        sut.validate(2L, null);

        // THEN
        // no validation call made as no users to validate
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
        assertEquals(responseEntities.size(),((List<ResponseEntity<Object>>)result.getBody()).size());
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
    void refreshRoleAssignmentRecords_judicial_includeJudicialBookingsDisabled() {

        // GIVEN
        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());
        Map<String, Set<UserAccessProfile>> userAccessProfiles = new HashMap<>();

        var ap1 = TestDataBuilder.buildJudicialAccessProfile();
        ap1.setUserId(TestDataBuilder.id_1);
        Set<UserAccessProfile> userAccessProfileSet1 = new HashSet<>();
        userAccessProfileSet1.add(ap1);
        userAccessProfiles.put("1", userAccessProfileSet1);

        Mockito.when(retrieveDataService.retrieveProfiles(any(), eq(UserType.JUDICIAL)))
                .thenReturn(userAccessProfiles);

        Mockito.when(requestMappingService.createJudicialAssignments(any(), any()))
                .thenReturn((ResponseEntity.status(HttpStatus.OK)
                        .body(Collections.emptyList())));

        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());

        mockFetchRefreshJobById(1L, RoleCategory.JUDICIAL, NEW);

        // WHEN
        // NB: override SUT with disabled bookings
        RefreshOrchestrator sutBookingsDisabled = createRefreshOrchestrator(false);
        ResponseEntity<Object> response = sutBookingsDisabled.refresh(1L, TestDataBuilder.buildUserRequest());

        // THEN
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // verify data passed to mapping service includes accessProfiles but NO bookings as they are disabled
        Mockito.verify(requestMappingService, Mockito.times(1))
                .createJudicialAssignments(userAccessProfiles, Collections.emptyList());

        Mockito.verify(judicialBookingService, Mockito.never()) // NB: never called as bookings disabled
                .fetchJudicialBookingsInBatches(userIdsCaptor.capture(), any());

    }

    @Test
    void refreshRoleAssignmentRecords_judicial_includeJudicialBookingsEnabled() throws IOException {

        // GIVEN
        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());
        Map<String, Set<UserAccessProfile>> userAccessProfiles = new HashMap<>();

        var ap1 = TestDataBuilder.buildJudicialAccessProfile();
        ap1.setUserId(TestDataBuilder.id_1);
        Set<UserAccessProfile> userAccessProfileSet1 = new HashSet<>();
        userAccessProfileSet1.add(ap1);
        userAccessProfiles.put("1", userAccessProfileSet1);

        var ap2 = TestDataBuilder.buildJudicialAccessProfile();
        ap1.setUserId(TestDataBuilder.id_2);
        Set<UserAccessProfile> userAccessProfileSet2 = new HashSet<>();
        userAccessProfileSet2.add(ap2);
        userAccessProfiles.put("2", userAccessProfileSet2);

        Mockito.when(retrieveDataService.retrieveProfiles(any(), eq(UserType.JUDICIAL)))
                .thenReturn(userAccessProfiles);

        List<JudicialBooking> bookingsList = List.of(TestDataBuilder.buildJudicialBooking());
        Mockito.when(judicialBookingService.fetchJudicialBookingsInBatches(any(), any()))
                .thenReturn(bookingsList);

        Mockito.when(requestMappingService.createJudicialAssignments(any(), any()))
                .thenReturn((ResponseEntity.status(HttpStatus.OK)
                        .body(Collections.emptyList())));

        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());

        mockFetchRefreshJobById(1L, RoleCategory.JUDICIAL, NEW);

        // WHEN
        RefreshOrchestrator refreshOrchestrator = Mockito.spy(sut);
        ResponseEntity<Object> response = refreshOrchestrator.refresh(1L, TestDataBuilder.buildUserRequest());

        // THEN
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Mockito.verify(refreshOrchestrator,Mockito.times(1)).buildSuccessAndFailureBucket(any(),any());

        // verify call to JBS uses flattened list of userIds
        Mockito.verify(judicialBookingService, Mockito.times(1))
                .fetchJudicialBookingsInBatches(userIdsCaptor.capture(), any());
        List<String> userIds = userIdsCaptor.getValue();
        assertEquals(2, userIds.size());
        assertTrue(userIds.contains(TestDataBuilder.id_1));
        assertTrue(userIds.contains(TestDataBuilder.id_2));

        // verify data passed to mapping service includes accessProfiles and bookings
        Mockito.verify(requestMappingService, Mockito.times(1))
                .createJudicialAssignments(userAccessProfiles, bookingsList);
    }

    @Test
    @DisplayName("refreshRoleAssignmentRecordsCouldNotBeRetrieved_Exception")
    void refreshRoleAssignmentRecordsCouldNotBeRetrieved_Exception() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,() ->
            sut.refresh(1L, userRequest)
        );

        assertTrue(exception.getLocalizedMessage().contains(ERROR_REFRESH_JOB_NOT_FOUND));
    }

    @Test
    @DisplayName("refreshRoleAssignmentRecordsInvalidStatus_Exception")
    void refreshRoleAssignmentRecordsInvalidStatus_Exception() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        mockFetchRefreshJobById(1L, RoleCategory.JUDICIAL, COMPLETED);

        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,() ->
            sut.refresh(1L, userRequest)
        );

        assertTrue(exception.getLocalizedMessage().contains(ERROR_REFRESH_JOB_INVALID_STATE));
    }

    private void mockFetchRefreshJobById(Long jobId, RoleCategory category, String status) {
        Mockito.when(persistenceService.fetchRefreshJobById(jobId))
                .thenReturn(Optional.of(
                        RefreshJobEntity.builder()
                                .roleCategory(category.toString())
                                .status(status)
                                .build()));
    }

}
