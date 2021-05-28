package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import feign.FeignException;
import feign.Response;
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
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
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
import static org.mockito.Mockito.mock;

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
            requestMappingService, parseRequestService, crdService, persistenceService);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void refreshRoleAssignmentRecords() throws IOException {

        Mockito.when(persistenceService.fetchRefreshJobById(Mockito.any()))
                .thenReturn(Optional.of(
                RefreshJobEntity.builder().build()));

        Mockito.doNothing().when(parseRequestService).validateUserRequest(Mockito.any());

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

//        TODO - classCastException RoleAssignmentRequestResource to ResponseEntity
        List<RoleAssignmentRequestResource> roleAssignmentRequestResourceList = new ArrayList<>();
        roleAssignmentRequestResourceList.add(TestDataBuilder
                .buildRoleAssignmentRequestResource());

        Mockito.when(retrieveDataService.retrieveCaseWorkerProfiles(Mockito.any()))
                .thenReturn(userAccessProfiles);

        Mockito.when(requestMappingService.createCaseWorkerAssignments(Mockito.any()))
                .thenReturn((ResponseEntity.status(HttpStatus.OK)
                        .body(roleAssignmentRequestResourceList)));

        Mockito.doNothing().when(parseRequestService).validateUserRequest(Mockito.any());

        ResponseEntity<Object> response = sut.refresh(1L, TestDataBuilder.buildUserRequest());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response);
    }

    @Test
    void refreshRoleAssignmentRecords_nullUserRequest() {

        Mockito.when(persistenceService.fetchRefreshJobById(Mockito.any()))
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
                Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any()))
        .thenReturn(responseEntity);

        Mockito.doNothing().when(parseRequestService).validateUserRequest(Mockito.any());

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

        Mockito.when(retrieveDataService.retrieveCaseWorkerProfiles(Mockito.any()))
                .thenReturn(userAccessProfiles);

        Mockito.when(requestMappingService.createCaseWorkerAssignments(Mockito.any()))
                .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));

        Mockito.doNothing().when(parseRequestService).validateUserRequest(Mockito.any());

        ResponseEntity<Object> response = sut.refresh(1L, UserRequest.builder().build());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response);
    }

    @Test
    void refreshRoleAssignmentRecords_nullUserRequest_feignException() {

        Mockito.when(persistenceService.fetchRefreshJobById(Mockito.any()))
                .thenReturn(Optional.of(
                        RefreshJobEntity.builder()
                                .roleCategory(RoleCategory.LEGAL_OPERATIONS.toString())
                                .build()));

        Mockito.when(crdService.fetchCaseworkerDetailsByServiceName(
                Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any()))
                .thenThrow(feignClientException);

        ResponseEntity<Object> response = sut.refresh(1L, UserRequest.builder().build());
    }

    @Test
    void refreshRoleAssignmentRecords_FeignException() {

        Mockito.when(persistenceService.fetchRefreshJobById(Mockito.any()))
                .thenReturn(Optional.of(
                        RefreshJobEntity.builder().build()));

        Mockito.doNothing().when(parseRequestService).validateUserRequest(Mockito.any());
        
        Mockito.when(retrieveDataService.retrieveCaseWorkerProfiles(Mockito.any()))
                .thenThrow(feignClientException);

        ResponseEntity<Object> response = sut.refresh(1L, TestDataBuilder.buildUserRequest());

    }
}
