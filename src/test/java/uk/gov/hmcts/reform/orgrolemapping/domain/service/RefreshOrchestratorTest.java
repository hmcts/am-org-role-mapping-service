package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void refreshRoleAssignmentRecords() throws IOException {

        Mockito.when(persistenceService.fetchRefreshJobById(any()))
                .thenReturn(Optional.of(
                RefreshJobEntity.builder().build()));

        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());

        Map<String, Set<UserAccessProfile>> userAccessProfiles = new HashMap<>();
        Set<UserAccessProfile> userAccessProfileSet = new HashSet<>();
        userAccessProfileSet.add(UserAccessProfile.builder()
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

        Mockito.when(retrieveDataService.retrieveCaseWorkerProfiles(any()))
                .thenReturn(userAccessProfiles);

        Mockito.when(requestMappingService.createCaseWorkerAssignments(any()))
                .thenReturn((ResponseEntity.status(HttpStatus.OK)
                        .body(Collections.emptyList())));

        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());

        ResponseEntity<Object> response = sut.refresh(1L, TestDataBuilder.buildUserRequest());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response);
    }

    @Test
    void refreshRoleAssignmentRecords_nullUserRequest() {

        Mockito.when(persistenceService.fetchRefreshJobById(any()))
                .thenReturn(Optional.of(
                        RefreshJobEntity.builder()
                                .roleCategory(RoleCategory.LEGAL_OPERATIONS.toString())
                                .build()));
        List<UserProfilesResponse> userProfilesResponseList = new ArrayList<>();
        userProfilesResponseList.add(TestDataBuilder.buildUserProfilesResponse());
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("total_records", "4");

        ResponseEntity<List<UserProfilesResponse>> responseEntity
                = new ResponseEntity<>(userProfilesResponseList,headers, HttpStatus.OK);

        Mockito.when(crdService.fetchCaseworkerDetailsByServiceName(
                any(), any(), any(), any(), any()))
                .thenReturn(responseEntity);

        Mockito.doNothing().when(parseRequestService)
                .validateUserRequest(any());

        Map<String, Set<UserAccessProfile>> userAccessProfiles = new HashMap<>();
        Set<UserAccessProfile> userAccessProfileSet = new HashSet<>();
        userAccessProfileSet.add(UserAccessProfile.builder()
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

        Mockito.when(retrieveDataService.retrieveCaseWorkerProfiles(any()))
                .thenReturn(userAccessProfiles);

        Mockito.when(requestMappingService.createCaseWorkerAssignments(any()))
                .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));

        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());

        ResponseEntity<Object> response = sut.refresh(1L, UserRequest.builder().build());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response);

    }

    @Test
    void refreshRoleAssignmentRecords_nullUserRequest_feignException() {
        //TODO
        Mockito.when(persistenceService.fetchRefreshJobById(any()))
                .thenReturn(Optional.of(
                        RefreshJobEntity.builder()
                                .roleCategory(RoleCategory.LEGAL_OPERATIONS.toString())
                                .build()));

        Mockito.when(crdService.fetchCaseworkerDetailsByServiceName(
                any(), any(), any(), any(), any()))
                .thenThrow(feignClientException);

        sut.refresh(1L, UserRequest.builder().build());
    }

    @Test
    void refreshRoleAssignmentRecords_FeignException() {
        //TODO
        Mockito.when(persistenceService.fetchRefreshJobById(any()))
                .thenReturn(Optional.of(
                        RefreshJobEntity.builder().build()));

        Mockito.doNothing().when(parseRequestService).validateUserRequest(any());

        Mockito.when(retrieveDataService.retrieveCaseWorkerProfiles(any()))
                .thenThrow(feignClientException);

        sut.refresh(1L, TestDataBuilder.buildUserRequest());
    }

    @Test
    void nullJobIdTest_validate() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        assertThrows(BadRequestException.class, () -> sut.validate(null, userRequest));
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

    @Test
    void refreshJobByServiceName() {
        Map<String, HttpStatus> stringHttpStatusMap = new HashMap<>();
        stringHttpStatusMap.put("1234", HttpStatus.CREATED);

        List<UserProfilesResponse> userProfilesResponseList = new ArrayList<>();
        userProfilesResponseList.add(TestDataBuilder.buildUserProfilesResponse());
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("total_records", "4");

        ResponseEntity<List<UserProfilesResponse>> responseEntity
                = new ResponseEntity<>(userProfilesResponseList,headers, HttpStatus.OK);

        Mockito.when(crdService.fetchCaseworkerDetailsByServiceName(
                any(), any(), any(), any(), any()))
                .thenReturn(responseEntity);

        Mockito.when(requestMappingService.createCaseWorkerAssignments(any()))
                .thenReturn((ResponseEntity.status(HttpStatus.OK)
                        .body(Collections.emptyList())));

        RefreshJobEntity refreshJobEntity =
                RefreshJobEntity.builder().roleCategory(RoleCategory.ADMIN.name()).jurisdiction("LDN").build();
        RefreshJobEntity refreshJobEntitySpy = Mockito.spy(refreshJobEntity);

        sut.refreshJobByServiceName(stringHttpStatusMap, refreshJobEntitySpy);

        verify(refreshJobEntitySpy, Mockito.times(1)).getRoleCategory();
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
        userAccessProfileSet.add(UserAccessProfile.builder()
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

        Mockito.when(requestMappingService.createCaseWorkerAssignments(any()))
                .thenReturn((ResponseEntity.status(HttpStatus.OK)
                        .body(Collections.EMPTY_LIST)));
        ResponseEntity<Object> result = sut.prepareResponseCodes(responseEntityMap, userAccessProfiles);
        assertNotNull(result);
        assertNotNull(result.getBody());

    }

    @Test
    void buildSuccessAndFailureBucket() {

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
    void buildSuccessAndFailureBucket_() {

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
}
