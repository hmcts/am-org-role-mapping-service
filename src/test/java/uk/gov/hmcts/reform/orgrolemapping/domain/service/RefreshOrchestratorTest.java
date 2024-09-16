package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import feign.FeignException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfileV2;
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
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.COMPLETED;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.NEW;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshOrchestrator.ERROR_INVALID_JOB_ID;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshOrchestrator.ERROR_REFRESH_JOB_INVALID_STATE;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshOrchestrator.ERROR_REFRESH_JOB_NOT_FOUND;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildJudicialProfileV2;
import static uk.gov.hmcts.reform.orgrolemapping.v1.V1.Error.UNAUTHORIZED_SERVICE;

@ExtendWith(MockitoExtension.class)
class RefreshOrchestratorTest {

    private static final String JURISDICTION = "LDN";
    private static final int PAGE_SIZE = 400;

    private static final String S2S_CCD_GW = "ccd_gw";
    private static final String S2S_ORM = "am_org_role_mapping_service";
    private static final String S2S_RARB = "am_role_assignment_refresh_batch";

    @SuppressWarnings("unchecked")
    private final RequestMappingService<UserAccessProfile> requestMappingService
            = (RequestMappingService<UserAccessProfile>)mock(RequestMappingService.class);
    private final RetrieveDataService retrieveDataService = mock(RetrieveDataService.class);
    private final ParseRequestService parseRequestService = mock(ParseRequestService.class);
    private final CRDService crdService = mock(CRDService.class);
    private final JRDService jrdService = mock(JRDService.class);
    private final PersistenceService persistenceService = mock(PersistenceService.class);
    private final FeignException feignClientException = mock(FeignException.NotFound.class);
    private final SecurityUtils securityUtils = mock(SecurityUtils.class);
    private final JudicialBookingService judicialBookingService = mock(JudicialBookingService.class);

    @Captor
    private ArgumentCaptor<List<String>> userIdsCaptor;

    @Captor
    private ArgumentCaptor<List<JudicialBooking>> judicialBookingsCaptor;

    @InjectMocks
    private final RefreshOrchestrator sut = createRefreshOrchestrator(true);

    private RefreshOrchestrator createRefreshOrchestrator(boolean includeJudicialBookings) {
        return new RefreshOrchestrator(
                retrieveDataService,
                requestMappingService,
                parseRequestService,
                crdService,
                jrdService,
                persistenceService,
                securityUtils,
                judicialBookingService,
                String.valueOf(PAGE_SIZE),
                "descending",
                "1",
                List.of(S2S_ORM, S2S_RARB),
                includeJudicialBookings);
    }


    @Nested
    class RefreshTestsForBothCaseworkerJudicial {

        @Test
        void refreshRoleAssignmentRecordsCouldNotBeRetrieved_Exception() {
            UserRequest userRequest = TestDataBuilder.buildUserRequest();

            UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,() ->
                    sut.refresh(1L, userRequest)
            );

            assertTrue(exception.getLocalizedMessage().contains(ERROR_REFRESH_JOB_NOT_FOUND));
        }

        @ParameterizedTest
        @EnumSource(value = RoleCategory.class, names = {"JUDICIAL", "LEGAL_OPERATIONS"})
        void refreshRoleAssignmentRecordsInvalidStatus_Exception(RoleCategory roleCategory) {
            UserRequest userRequest = TestDataBuilder.buildUserRequest();

            mockFetchRefreshJobById(1L, roleCategory, COMPLETED);

            UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,() ->
                    sut.refresh(1L, userRequest)
            );

            assertTrue(exception.getLocalizedMessage().contains(ERROR_REFRESH_JOB_INVALID_STATE));
        }

    }


    @Nested
    class RefreshTestsForCaseworker {

        @Test
        void refreshRoleAssignmentRecords() {

            doNothing().when(parseRequestService).validateUserRequest(any());

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

            when(retrieveDataService.retrieveProfiles(any(), eq(UserType.CASEWORKER)))
                    .thenReturn(userAccessProfiles);

            when(requestMappingService.createCaseworkerAssignments(any()))
                    .thenReturn((ResponseEntity.status(HttpStatus.OK)
                            .body(Collections.emptyList())));

            doNothing().when(parseRequestService).validateUserRequest(any());

            mockFetchRefreshJobById(1L, RoleCategory.LEGAL_OPERATIONS, NEW);

            ResponseEntity<Object> response = sut.refresh(1L, TestDataBuilder.buildUserRequest());

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response);
        }

        @Test
        void refreshRoleAssignmentRecords_profileNotFound() {

            doNothing().when(parseRequestService).validateUserRequest(any());

            when(retrieveDataService.retrieveProfiles(any(), eq(UserType.CASEWORKER)))
                    .thenThrow(FeignException.NotFound.class);

            when(requestMappingService.createCaseworkerAssignments(any()))
                    .thenReturn((ResponseEntity.status(HttpStatus.OK)
                            .body(Collections.emptyList())));

            doNothing().when(parseRequestService).validateUserRequest(any());

            mockFetchRefreshJobById(1L, RoleCategory.LEGAL_OPERATIONS, NEW);

            assertNull(sut.refresh(1L, TestDataBuilder.buildUserRequest()));

        }

        @Test
        void refreshRoleAssignmentRecords_nullUserRequest() {

            // GIVEN
            mockFetchRefreshJobById(1L, RoleCategory.LEGAL_OPERATIONS, NEW);

            setUpMocks_RefreshJobByServiceName_Caseworker(1);

            // WHEN
            ResponseEntity<Object> response = sut.refresh(1L, UserRequest.builder().build());

            // THEN
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response);

            // verify refreshById calls not made
            verify(parseRequestService, never()).validateUserRequest(any());
            verify(retrieveDataService, never()).retrieveProfiles(any(), eq(UserType.CASEWORKER));
            // verify refreshByServiceName calls have been made
            verify(crdService, atLeast(1)).fetchCaseworkerDetailsByServiceName(any(), any(), any(), any(), any());
            verify(retrieveDataService, times(1)).retrieveProfilesByServiceName(any(), eq(UserType.CASEWORKER));
        }

        @Test
        void refreshRoleAssignmentRecords_nullUserRequest_feignException() {

            mockFetchRefreshJobById(1L, RoleCategory.LEGAL_OPERATIONS, NEW);

            when(crdService.fetchCaseworkerDetailsByServiceName(any(), any(), any(), any(), any()))
                    .thenThrow(feignClientException);

            UserRequest userRequestSpy = Mockito.spy(UserRequest.builder().build());

            sut.refresh(1L, userRequestSpy);

            verify(persistenceService, times(1)).persistRefreshJob(any());
        }

    }


    @Nested
    class RefreshTestsForJudicial {

        @Test
        void refreshRoleAssignmentRecords_includeJudicialBookingsDisabled() {

            // GIVEN
            doNothing().when(parseRequestService).validateUserRequest(any());
            Map<String, Set<UserAccessProfile>> userAccessProfiles = new HashMap<>();

            var ap1 = TestDataBuilder.buildJudicialAccessProfile();
            ap1.setUserId(TestDataBuilder.id_1);
            Set<UserAccessProfile> userAccessProfileSet1 = new HashSet<>();
            userAccessProfileSet1.add(ap1);
            userAccessProfiles.put(TestDataBuilder.id_1, userAccessProfileSet1);

            when(retrieveDataService.retrieveProfiles(any(), eq(UserType.JUDICIAL)))
                    .thenReturn(userAccessProfiles);

            when(requestMappingService.createJudicialAssignments(any(), any()))
                    .thenReturn((ResponseEntity.status(HttpStatus.OK)
                            .body(Collections.emptyList())));

            doNothing().when(parseRequestService).validateUserRequest(any());

            mockFetchRefreshJobById(1L, RoleCategory.JUDICIAL, NEW);

            // WHEN
            // NB: override SUT with disabled bookings
            RefreshOrchestrator sutBookingsDisabled = createRefreshOrchestrator(false);
            ResponseEntity<Object> response = sutBookingsDisabled.refresh(1L, TestDataBuilder.buildUserRequest());

            // THEN
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());

            // verify data passed to mapping service includes accessProfiles but NO bookings as they are disabled
            verify(requestMappingService, times(1))
                    .createJudicialAssignments(userAccessProfiles, Collections.emptyList());

            verify(judicialBookingService, never()) // NB: never called as bookings disabled
                    .fetchJudicialBookingsInBatches(any(), any());

        }

        @Test
        void refreshRoleAssignmentRecords_includeJudicialBookingsEnabled() throws IOException {

            // GIVEN
            doNothing().when(parseRequestService).validateUserRequest(any());
            Map<String, Set<UserAccessProfile>> userAccessProfiles = new HashMap<>();

            var ap1 = TestDataBuilder.buildJudicialAccessProfile();
            ap1.setUserId(TestDataBuilder.id_1);
            Set<UserAccessProfile> userAccessProfileSet1 = new HashSet<>();
            userAccessProfileSet1.add(ap1);
            userAccessProfiles.put(TestDataBuilder.id_1, userAccessProfileSet1);

            var ap2 = TestDataBuilder.buildJudicialAccessProfile();
            ap1.setUserId(TestDataBuilder.id_2);
            Set<UserAccessProfile> userAccessProfileSet2 = new HashSet<>();
            userAccessProfileSet2.add(ap2);
            userAccessProfiles.put(TestDataBuilder.id_2, userAccessProfileSet2);

            when(retrieveDataService.retrieveProfiles(any(), eq(UserType.JUDICIAL)))
                    .thenReturn(userAccessProfiles);

            List<JudicialBooking> bookingsList = List.of(TestDataBuilder.buildJudicialBooking());
            when(judicialBookingService.fetchJudicialBookingsInBatches(any(), any()))
                    .thenReturn(bookingsList);

            when(requestMappingService.createJudicialAssignments(any(), any()))
                    .thenReturn((ResponseEntity.status(HttpStatus.OK)
                            .body(Collections.emptyList())));

            doNothing().when(parseRequestService).validateUserRequest(any());

            mockFetchRefreshJobById(1L, RoleCategory.JUDICIAL, NEW);

            // WHEN
            RefreshOrchestrator refreshOrchestrator = Mockito.spy(sut);
            ResponseEntity<Object> response = refreshOrchestrator.refresh(1L, TestDataBuilder.buildUserRequest());

            // THEN
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());

            verify(refreshOrchestrator, times(1)).buildSuccessAndFailureBucket(any(), any());

            // verify call to JBS uses flattened list of userIds
            verify(judicialBookingService, times(1))
                    .fetchJudicialBookingsInBatches(userIdsCaptor.capture(), any());
            List<String> userIds = userIdsCaptor.getValue();
            assertEquals(userAccessProfiles.size(), userIds.size());
            assertTrue(userIds.containsAll(userAccessProfiles.keySet()));

            // verify data passed to mapping service includes accessProfiles and bookings
            verify(requestMappingService, times(1))
                    .createJudicialAssignments(userAccessProfiles, bookingsList);
        }

        @Test
        void refreshRoleAssignmentRecords_nullUserRequest() {

            // GIVEN
            mockFetchRefreshJobById(1L, RoleCategory.JUDICIAL, NEW);

            setUpMocks_RefreshJobByServiceName_Judicial(1);

            // WHEN
            ResponseEntity<Object> response = sut.refresh(1L, UserRequest.builder().build());

            // THEN
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response);

            // verify refreshById calls not made
            verify(parseRequestService, never()).validateUserRequest(any());
            verify(retrieveDataService, never()).retrieveProfiles(any(), eq(UserType.JUDICIAL));
            // verify refreshByServiceName calls have been made
            verify(jrdService, atLeast(1)).fetchJudicialDetailsByServiceName(any(), any(), any(), any(), any());
            verify(retrieveDataService, times(1)).retrieveProfilesByServiceName(any(), eq(UserType.JUDICIAL));
        }

        @Test
        void refreshRoleAssignmentRecords_nullUserRequest_feignException() {

            mockFetchRefreshJobById(1L, RoleCategory.JUDICIAL, NEW);

            when(jrdService.fetchJudicialDetailsByServiceName(any(), any(), any(), any(), any()))
                    .thenThrow(feignClientException);

            UserRequest userRequestSpy = Mockito.spy(UserRequest.builder().build());

            sut.refresh(1L, userRequestSpy);

            verify(persistenceService, times(1)).persistRefreshJob(any());
        }

    }


    @Nested
    class ValidateTests {

        @Test
        void validateTest_errorNullJobId() {

            // GIVEN
            UserRequest userRequest = TestDataBuilder.buildUserRequest();
            when(securityUtils.getServiceName()).thenReturn(S2S_RARB);

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
            doNothing().when(parseRequestService).validateUserRequest(any());
            when(securityUtils.getServiceName()).thenReturn(S2S_CCD_GW); // i.e. invalid service

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
            when(securityUtils.getServiceName()).thenReturn(S2S_RARB);
            when(persistenceService.fetchRefreshJobById(1L))
                    .thenReturn(Optional.empty()); // i.e. NOT FOUND

            // WHEN
            UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class, () ->
                    sut.validate(1L, userRequest)
            );

            // THEN
            assertTrue(exception.getLocalizedMessage().contains(ERROR_REFRESH_JOB_NOT_FOUND));
        }

        @Test
        void validateTest_errorJobInvalidState() {

            // GIVEN
            UserRequest userRequest = TestDataBuilder.buildUserRequest();
            when(securityUtils.getServiceName()).thenReturn(S2S_RARB);
            mockFetchRefreshJobById(1L, RoleCategory.LEGAL_OPERATIONS, COMPLETED); // i.e wrong STATE

            // WHEN
            UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class, () ->
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
            when(securityUtils.getServiceName()).thenReturn(S2S_RARB);
            mockFetchRefreshJobById(1L, RoleCategory.LEGAL_OPERATIONS, NEW);
            doThrow(new BadRequestException(errorMessage))
                    .when(parseRequestService).validateUserRequest(userRequest);

            // WHEN
            BadRequestException exception = assertThrows(BadRequestException.class, () ->
                    sut.validate(1L, userRequest)
            );

            // THEN
            assertTrue(exception.getLocalizedMessage().contains(errorMessage));
        }

        @Test
        void validateTest_withUserIds() {

            // GIVEN
            UserRequest userRequest = TestDataBuilder.buildUserRequest(); // NB: includes usersIds
            doNothing().when(parseRequestService).validateUserRequest(userRequest);
            when(securityUtils.getServiceName()).thenReturn(S2S_RARB);
            mockFetchRefreshJobById(1L, RoleCategory.LEGAL_OPERATIONS, NEW);

            // WHEN
            sut.validate(1L, userRequest);

            // THEN
            verify(parseRequestService, times(1)).validateUserRequest(any());
        }

        @ParameterizedTest
        @NullAndEmptySource
        void validateTest_emptyOrNullUserIds(List<String> userIds) {

            // GIVEN
            UserRequest userRequest = TestDataBuilder.buildUserRequest();
            userRequest.setUserIds(userIds);
            when(securityUtils.getServiceName()).thenReturn(S2S_RARB);
            mockFetchRefreshJobById(1L, RoleCategory.LEGAL_OPERATIONS, NEW);

            // WHEN
            sut.validate(1L, userRequest);

            // THEN
            // no validation call made as no users to validate
            verify(parseRequestService, times(0)).validateUserRequest(any());
        }

        @Test
        void validateTest_nullRequest() {

            // GIVEN
            when(securityUtils.getServiceName()).thenReturn(S2S_RARB);
            mockFetchRefreshJobById(2L, RoleCategory.LEGAL_OPERATIONS, NEW);

            // WHEN
            sut.validate(2L, null);

            // THEN
            // no validation call made as no users to validate
            verify(parseRequestService, times(0)).validateUserRequest(any());
        }

    }


    @Nested
    class RefreshJobByServiceNameForCaseworker {

        @Test
        void refreshJobByServiceName() {

            // GIVEN
            setUpMocks_RefreshJobByServiceName_Caseworker(4);

            RefreshJobEntity refreshJobEntity = RefreshJobEntity.builder()
                    .roleCategory(RoleCategory.ADMIN.name())
                    .jurisdiction(JURISDICTION)
                    .build();
            RefreshJobEntity refreshJobEntitySpy = Mockito.spy(refreshJobEntity);

            Map<String, HttpStatus> responseCodeWithUserId = new HashMap<>();
            responseCodeWithUserId.put("1234", HttpStatus.CREATED);

            @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
            int expectedNumberOfPages = 1; // as total number of records < default page size

            // WHEN
            sut.refreshJobByServiceName(responseCodeWithUserId, refreshJobEntitySpy, UserType.CASEWORKER);

            // THEN
            verify(refreshJobEntitySpy, times(1)).setStatus(any());
            verify(refreshJobEntitySpy, times(1)).setCreated(any());
            verify(refreshJobEntitySpy, times(1)).setLog(any());
            verify(persistenceService, times(1)).persistRefreshJob(any());
            // verify pagination calls
            verify(crdService, atLeast(1))
                    .fetchCaseworkerDetailsByServiceName(eq(JURISDICTION), eq(PAGE_SIZE), eq(0), any(), any());
            // verify no second pagination call: i.e. no pagination as total number of records < default page size
            verify(crdService, never())
                    .fetchCaseworkerDetailsByServiceName(eq(JURISDICTION), eq(PAGE_SIZE), eq(1), any(), any());

            verify(requestMappingService, times(expectedNumberOfPages))
                    .createCaseworkerAssignments(any());
        }

        @Test
        void refreshJobByServiceWithInvalidRoleCategory() {

            // GIVEN
            setUpMocks_RefreshJobByServiceName_Caseworker(4);

            RefreshJobEntity refreshJobEntity = RefreshJobEntity.builder()
                    .roleCategory("ABC")
                    .jurisdiction(JURISDICTION)
                    .build();
            RefreshJobEntity refreshJobEntitySpy = Mockito.spy(refreshJobEntity);

            Map<String, HttpStatus> responseCodeWithUserId = new HashMap<>();
            responseCodeWithUserId.put("1234", HttpStatus.CREATED);

            // WHEN / THEN
            Assertions.assertThrows(BadRequestException.class, () ->
                    sut.refreshJobByServiceName(responseCodeWithUserId, refreshJobEntitySpy, UserType.CASEWORKER)
            );
        }

        @Test
        void refreshJobByServiceNameWithNoPageSize() {

            // GIVEN
            setUpMocks_RefreshJobByServiceName_Caseworker(4);

            RefreshJobEntity refreshJobEntity = RefreshJobEntity.builder()
                    .roleCategory(RoleCategory.ADMIN.name())
                    .jurisdiction(JURISDICTION)
                    .build();
            RefreshJobEntity refreshJobEntitySpy = Mockito.spy(refreshJobEntity);

            Map<String, HttpStatus> responseCodeWithUserId = new HashMap<>();
            responseCodeWithUserId.put("1234", HttpStatus.CREATED);

            @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
            int expectedNumberOfPages = 1;
            int pageSize = 0;

            // WHEN
            sut.pageSize = Integer.toString(pageSize);
            sut.refreshJobByServiceName(responseCodeWithUserId, refreshJobEntitySpy, UserType.CASEWORKER);

            // THEN
            verify(refreshJobEntitySpy, times(1)).setStatus(any());
            // verify pagination calls
            verify(crdService, atLeast(1))
                    .fetchCaseworkerDetailsByServiceName(eq(JURISDICTION), eq(pageSize), eq(0), any(), any());
            // verify no second pagination call: i.e. pagination disabled
            verify(crdService, never())
                    .fetchCaseworkerDetailsByServiceName(eq(JURISDICTION), eq(pageSize), eq(1), any(), any());

            verify(requestMappingService, times(expectedNumberOfPages))
                    .createCaseworkerAssignments(any());
        }

        @Test
        void refreshJobByServiceNameWithDecimalPageSize() {

            // GIVEN
            setUpMocks_RefreshJobByServiceName_Caseworker(3);

            RefreshJobEntity refreshJobEntity = RefreshJobEntity.builder()
                    .roleCategory(RoleCategory.ADMIN.name())
                    .jurisdiction(JURISDICTION)
                    .build();
            RefreshJobEntity refreshJobEntitySpy = Mockito.spy(refreshJobEntity);

            Map<String, HttpStatus> responseCodeWithUserId = new HashMap<>();
            responseCodeWithUserId.put("1234", HttpStatus.CREATED);

            @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
            int expectedNumberOfPages = 2;
            int pageSize = 2;

            // WHEN
            sut.pageSize = Integer.toString(pageSize);  // NB: pagination test as pagesSize < total records
            sut.refreshJobByServiceName(responseCodeWithUserId, refreshJobEntitySpy, UserType.CASEWORKER);

            // THEN
            verify(refreshJobEntitySpy, times(1)).setStatus(any());
            // verify pagination calls
            // NB: first page is called twice as first call extracts the total record count
            verify(crdService,  times(2))
                    .fetchCaseworkerDetailsByServiceName(eq(JURISDICTION), eq(pageSize), eq(0), any(), any());
            verify(crdService,  times(1))
                    .fetchCaseworkerDetailsByServiceName(eq(JURISDICTION), eq(pageSize), eq(1), any(), any());
            // NB: page 3 (i.e. index 2)  never loaded
            verify(crdService,  never())
                    .fetchCaseworkerDetailsByServiceName(eq(JURISDICTION), eq(pageSize), eq(2), any(), any());

            verify(requestMappingService, times(expectedNumberOfPages))
                    .createCaseworkerAssignments(any());
        }

        @Test
        void refreshJobByServiceName_FeignException() {

            // GIVEN
            Map<String, HttpStatus> responseCodeWithUserId = new HashMap<>();
            responseCodeWithUserId.put("1234", HttpStatus.CREATED);

            when(crdService.fetchCaseworkerDetailsByServiceName(
                            any(), any(), any(), any(), any()))
                    .thenThrow(feignClientException);

            when(requestMappingService.createCaseworkerAssignments(any()))
                    .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));
 
            RefreshJobEntity refreshJobEntity = RefreshJobEntity.builder()
                    .roleCategory(RoleCategory.ADMIN.name())
                    .jurisdiction(JURISDICTION)
                    .build();
            RefreshJobEntity refreshJobEntitySpy = Mockito.spy(refreshJobEntity);

            // WHEN
            sut.refreshJobByServiceName(responseCodeWithUserId, refreshJobEntitySpy, UserType.CASEWORKER);

            // THEN
            verify(refreshJobEntitySpy, times(1)).setStatus(any());
            verify(refreshJobEntitySpy, times(1)).setCreated(any());
            verify(refreshJobEntitySpy, times(1)).setLog(any());
            verify(persistenceService, times(1)).persistRefreshJob(any());
        }

    }


    @Nested
    class RefreshJobByServiceNameForJudicial {

        @Test
        void refreshJobByServiceName_includeJudicialBookingsDisabled() {

            // GIVEN
            @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
            Map<String, Set<UserAccessProfile>> userAccessProfiles = setUpMocks_RefreshJobByServiceName_Judicial(4);

            RefreshJobEntity refreshJobEntity = RefreshJobEntity.builder()
                    .roleCategory(RoleCategory.JUDICIAL.name())
                    .jurisdiction(JURISDICTION)
                    .build();
            RefreshJobEntity refreshJobEntitySpy = Mockito.spy(refreshJobEntity);

            Map<String, HttpStatus> responseCodeWithUserId = new HashMap<>();
            responseCodeWithUserId.put("1234", HttpStatus.CREATED);

            @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
            int expectedNumberOfPages = 1; // as total number of records < default page size

            // WHEN
            // NB: override SUT with disabled bookings
            RefreshOrchestrator sutBookingsDisabled = createRefreshOrchestrator(false);
            sutBookingsDisabled.refreshJobByServiceName(responseCodeWithUserId, refreshJobEntitySpy, UserType.JUDICIAL);

            // THEN
            verify(refreshJobEntitySpy, times(1)).setStatus(any());
            verify(refreshJobEntitySpy, times(1)).setCreated(any());
            verify(refreshJobEntitySpy, times(1)).setLog(any());
            verify(persistenceService, times(1)).persistRefreshJob(any());
            // verify pagination calls
            verify(jrdService, atLeast(1))
                    .fetchJudicialDetailsByServiceName(eq(JURISDICTION), eq(PAGE_SIZE), eq(0), any(), any());
            // verify no second pagination call: i.e. no pagination as total number of records < default page size
            verify(jrdService, never())
                    .fetchJudicialDetailsByServiceName(eq(JURISDICTION), eq(PAGE_SIZE), eq(1), any(), any());

            // verify data passed to mapping service includes accessProfiles but NO bookings as they are disabled
            verify(requestMappingService, times(expectedNumberOfPages))
                    .createJudicialAssignments(userAccessProfiles, Collections.emptyList());

            verify(judicialBookingService, never()) // NB: never called as bookings disabled
                    .fetchJudicialBookingsInBatches(any(), any());
        }

        @Test
        void refreshJobByServiceName_includeJudicialBookingsEnabled() throws IOException {

            // GIVEN
            @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
            Map<String, Set<UserAccessProfile>> userAccessProfiles = setUpMocks_RefreshJobByServiceName_Judicial(1);

            RefreshJobEntity refreshJobEntity = RefreshJobEntity.builder()
                    .roleCategory(RoleCategory.JUDICIAL.name())
                    .jurisdiction(JURISDICTION)
                    .build();
            RefreshJobEntity refreshJobEntitySpy = Mockito.spy(refreshJobEntity);

            Map<String, HttpStatus> responseCodeWithUserId = new HashMap<>();
            responseCodeWithUserId.put("1234", HttpStatus.CREATED);

            List<JudicialBooking> bookingsList = List.of(TestDataBuilder.buildJudicialBooking());
            when(judicialBookingService.fetchJudicialBookingsInBatches(any(), any()))
                    .thenReturn(bookingsList);

            @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
            int expectedNumberOfPages = 1; // as total number of records < default page size

            // WHEN
            sut.refreshJobByServiceName(responseCodeWithUserId, refreshJobEntitySpy, UserType.JUDICIAL);

            // THEN
            verify(refreshJobEntitySpy, times(1)).setStatus(any());
            verify(refreshJobEntitySpy, times(1)).setCreated(any());
            verify(refreshJobEntitySpy, times(1)).setLog(any());
            verify(persistenceService, times(1)).persistRefreshJob(any());
            // verify pagination calls
            verify(jrdService, atLeast(1))
                    .fetchJudicialDetailsByServiceName(eq(JURISDICTION), eq(PAGE_SIZE), eq(0), any(), any());
            // verify no second pagination call: i.e. no pagination as total number of records < default page size
            verify(jrdService, never())
                    .fetchJudicialDetailsByServiceName(eq(JURISDICTION), eq(PAGE_SIZE), eq(1), any(), any());

            // verify call to JBS uses flattened list of userIds
            verify(judicialBookingService, times(expectedNumberOfPages))
                    .fetchJudicialBookingsInBatches(userIdsCaptor.capture(), any());
            List<String> userIds = userIdsCaptor.getValue();
            assertEquals(userAccessProfiles.size(), userIds.size());
            assertTrue(userIds.containsAll(userAccessProfiles.keySet()));

            // verify data passed to mapping service includes accessProfiles and bookings
            verify(requestMappingService, times(1))
                    .createJudicialAssignments(userAccessProfiles, bookingsList);
        }

        @Test
        void refreshJobByServiceWithInvalidRoleCategory() {

            // GIVEN
            setUpMocks_RefreshJobByServiceName_Judicial(4);

            RefreshJobEntity refreshJobEntity = RefreshJobEntity.builder()
                    .roleCategory("ABC")
                    .jurisdiction(JURISDICTION)
                    .build();
            RefreshJobEntity refreshJobEntitySpy = Mockito.spy(refreshJobEntity);

            Map<String, HttpStatus> responseCodeWithUserId = new HashMap<>();
            responseCodeWithUserId.put("1234", HttpStatus.CREATED);

            // WHEN / THEN
            Assertions.assertThrows(BadRequestException.class, () ->
                    sut.refreshJobByServiceName(responseCodeWithUserId, refreshJobEntitySpy, UserType.JUDICIAL)
            );
        }

        @Test
        void refreshJobByServiceNameWithNoPageSize() {

            // GIVEN
            setUpMocks_RefreshJobByServiceName_Judicial(4);

            RefreshJobEntity refreshJobEntity = RefreshJobEntity.builder()
                    .roleCategory(RoleCategory.JUDICIAL.name())
                    .jurisdiction(JURISDICTION)
                    .build();
            RefreshJobEntity refreshJobEntitySpy = Mockito.spy(refreshJobEntity);

            Map<String, HttpStatus> responseCodeWithUserId = new HashMap<>();
            responseCodeWithUserId.put("1234", HttpStatus.CREATED);

            @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
            int expectedNumberOfPages = 1;
            int pageSize = 0;

            // WHEN
            sut.pageSize = Integer.toString(pageSize);
            sut.refreshJobByServiceName(responseCodeWithUserId, refreshJobEntitySpy, UserType.JUDICIAL);

            // THEN
            verify(refreshJobEntitySpy, times(1)).setStatus(any());
            // verify pagination calls
            verify(jrdService, atLeast(1))
                    .fetchJudicialDetailsByServiceName(eq(JURISDICTION), eq(pageSize), eq(0), any(), any());
            // verify no second pagination call: i.e. pagination disabled
            verify(jrdService, never())
                    .fetchJudicialDetailsByServiceName(eq(JURISDICTION), eq(pageSize), eq(1), any(), any());

            verify(requestMappingService, times(expectedNumberOfPages))
                    .createJudicialAssignments(any(), any());
        }

        @Test
        void refreshJobByServiceNameWithDecimalPageSize_includeJudicialBookingsEnabled() {

            // GIVEN
            @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
            Map<String, Set<UserAccessProfile>> userAccessProfiles = setUpMocks_RefreshJobByServiceName_Judicial(3);

            RefreshJobEntity refreshJobEntity = RefreshJobEntity.builder()
                    .roleCategory(RoleCategory.JUDICIAL.name())
                    .jurisdiction(JURISDICTION)
                    .build();
            RefreshJobEntity refreshJobEntitySpy = Mockito.spy(refreshJobEntity);

            Map<String, HttpStatus> responseCodeWithUserId = new HashMap<>();
            responseCodeWithUserId.put("1234", HttpStatus.CREATED);

            @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
            int expectedNumberOfPages = 2;
            int pageSize = 2;

            // WHEN
            sut.pageSize = Integer.toString(pageSize);  // NB: pagination test as pagesSize < total records
            sut.refreshJobByServiceName(responseCodeWithUserId, refreshJobEntitySpy, UserType.JUDICIAL);

            // THEN
            verify(refreshJobEntitySpy, times(1)).setStatus(any());
            // verify pagination calls
            // NB: first page is called twice as first call extracts the total record count
            verify(jrdService,  times(expectedNumberOfPages))
                    .fetchJudicialDetailsByServiceName(eq(JURISDICTION), eq(pageSize), eq(0), any(), any());
            verify(jrdService,  times(1))
                    .fetchJudicialDetailsByServiceName(eq(JURISDICTION), eq(pageSize), eq(1), any(), any());
            // NB: page 3 (i.e. index 2)  never loaded
            verify(jrdService,  never())
                    .fetchJudicialDetailsByServiceName(eq(JURISDICTION), eq(pageSize), eq(2), any(), any());

            // verify call to JBS for each page
            verify(judicialBookingService, times(expectedNumberOfPages))
                    .fetchJudicialBookingsInBatches(userIdsCaptor.capture(), any());

            // verify data passed to mapping service includes accessProfiles and bookings
            verify(requestMappingService, times(expectedNumberOfPages))
                    .createJudicialAssignments(eq(userAccessProfiles), judicialBookingsCaptor.capture());
            judicialBookingsCaptor.getAllValues().forEach(Assertions::assertNotNull);
        }

        @Test
        void refreshJobByServiceName_FeignException() {

            // GIVEN
            Map<String, HttpStatus> responseCodeWithUserId = new HashMap<>();
            responseCodeWithUserId.put("1234", HttpStatus.CREATED);

            when(jrdService.fetchJudicialDetailsByServiceName(
                            any(), any(), any(), any(), any()))
                    .thenThrow(feignClientException);

            when(requestMappingService.createJudicialAssignments(any(), any()))
                    .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));

            RefreshJobEntity refreshJobEntity = RefreshJobEntity.builder()
                    .roleCategory(RoleCategory.JUDICIAL.name())
                    .jurisdiction(JURISDICTION)
                    .build();
            RefreshJobEntity refreshJobEntitySpy = Mockito.spy(refreshJobEntity);

            // WHEN
            sut.refreshJobByServiceName(responseCodeWithUserId, refreshJobEntitySpy, UserType.JUDICIAL);

            // THEN
            verify(refreshJobEntitySpy, times(1)).setStatus(any());
            verify(refreshJobEntitySpy, times(1)).setCreated(any());
            verify(refreshJobEntitySpy, times(1)).setLog(any());
            verify(persistenceService, times(1)).persistRefreshJob(any());
        }

    }


    @Nested
    class PrepareResponseCodesTestsForCaseworker {

        @Test
        void prepareResponseCodes() {

            // GIVEN
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

            when(requestMappingService.createCaseworkerAssignments(any()))
                    .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));

            // WHEN
            ResponseEntity<Object> result = sut.prepareResponseCodes(responseEntityMap, userAccessProfiles,
                    UserType.CASEWORKER);

            // THEN
            assertNotNull(result);
            assertNotNull(result.getBody());

        }

        @Test
        @SuppressWarnings("unchecked")
        void prepareResponseCodesWithValue() {

            // GIVEN
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

            when(requestMappingService.createCaseworkerAssignments(any()))
                    .thenReturn((ResponseEntity.status(HttpStatus.OK)
                            .body(responseEntities)));

            // WHEN
            ResponseEntity<Object> result = sut.prepareResponseCodes(responseEntityMap, userAccessProfiles,
                    UserType.CASEWORKER);

            // THEN
            assertNotNull(result);
            assertNotNull(result.getBody());
            assertEquals(responseEntities.size(), ((List<ResponseEntity<Object>>) result.getBody()).size());
            assertEquals(responseEntities.size(), responseEntityMap.size());

        }

    }


    @Nested
    class PrepareResponseCodesTestsForJudicial {

        @Test
        void prepareResponseCodes_includeJudicialBookingsDisabled() {

            // GIVEN
            Map<String, HttpStatus> responseEntityMap = new HashMap<>();
            responseEntityMap.put("1234", HttpStatus.CREATED);

            Map<String, Set<UserAccessProfile>> userAccessProfiles = new HashMap<>();

            var ap1 = TestDataBuilder.buildJudicialAccessProfile();
            ap1.setUserId(TestDataBuilder.id_1);
            Set<UserAccessProfile> userAccessProfileSet1 = new HashSet<>();
            userAccessProfileSet1.add(ap1);
            userAccessProfiles.put(TestDataBuilder.id_1, userAccessProfileSet1);

            when(requestMappingService.createJudicialAssignments(any(), any()))
                    .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));

            // WHEN
            // NB: override SUT with disabled bookings
            RefreshOrchestrator sutBookingsDisabled = createRefreshOrchestrator(false);
            ResponseEntity<Object> result = sutBookingsDisabled
                    .prepareResponseCodes(responseEntityMap, userAccessProfiles, UserType.JUDICIAL);

            // THEN
            assertNotNull(result);
            assertNotNull(result.getBody());

            // verify data passed to mapping service includes accessProfiles but NO bookings as they are disabled
            verify(requestMappingService, times(1))
                    .createJudicialAssignments(userAccessProfiles, Collections.emptyList());

            verify(judicialBookingService, never()) // NB: never called as bookings disabled
                    .fetchJudicialBookingsInBatches(any(), any());

        }

        @Test
        void prepareResponseCodes_includeJudicialBookingsEnabled() throws IOException {

            // GIVEN
            Map<String, HttpStatus> responseEntityMap = new HashMap<>();
            responseEntityMap.put("1234", HttpStatus.CREATED);

            Map<String, Set<UserAccessProfile>> userAccessProfiles = new HashMap<>();

            var ap1 = TestDataBuilder.buildJudicialAccessProfile();
            ap1.setUserId(TestDataBuilder.id_1);
            Set<UserAccessProfile> userAccessProfileSet1 = new HashSet<>();
            userAccessProfileSet1.add(ap1);
            userAccessProfiles.put(TestDataBuilder.id_1, userAccessProfileSet1);

            List<JudicialBooking> bookingsList = List.of(TestDataBuilder.buildJudicialBooking());
            when(judicialBookingService.fetchJudicialBookingsInBatches(any(), any()))
                    .thenReturn(bookingsList);

            when(requestMappingService.createJudicialAssignments(any(), any()))
                    .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));

            // WHEN
            ResponseEntity<Object> result = sut.prepareResponseCodes(responseEntityMap, userAccessProfiles,
                    UserType.JUDICIAL);

            // THEN
            assertNotNull(result);
            assertNotNull(result.getBody());

            // verify call to JBS uses flattened list of userIds
            verify(judicialBookingService, times(1))
                    .fetchJudicialBookingsInBatches(userIdsCaptor.capture(), any());
            List<String> userIds = userIdsCaptor.getValue();
            assertEquals(userAccessProfiles.size(), userIds.size());
            assertTrue(userIds.containsAll(userAccessProfiles.keySet()));

            // verify data passed to mapping service includes accessProfiles and bookings
            verify(requestMappingService, times(1))
                    .createJudicialAssignments(userAccessProfiles, bookingsList);

        }

        @Test
        @SuppressWarnings("unchecked")
        void prepareResponseCodesWithValue() {

            // GIVEN
            Map<String, HttpStatus> responseEntityMap = new HashMap<>();
            responseEntityMap.put("1234", HttpStatus.CREATED);

            Map<String, Set<UserAccessProfile>> userAccessProfiles = new HashMap<>();

            var ap1 = TestDataBuilder.buildJudicialAccessProfile();
            ap1.setUserId(TestDataBuilder.id_1);
            Set<UserAccessProfile> userAccessProfileSet1 = new HashSet<>();
            userAccessProfileSet1.add(ap1);
            userAccessProfiles.put(TestDataBuilder.id_1, userAccessProfileSet1);

            when(judicialBookingService.fetchJudicialBookingsInBatches(any(), any()))
                    .thenReturn(Collections.emptyList());

            List<ResponseEntity<Object>> responseEntities = List.of(
                    ResponseEntity.ok(new RoleAssignmentRequestResource(AssignmentRequestBuilder
                            .buildAssignmentRequest(false))),
                    ResponseEntity.ok(new RoleAssignmentRequestResource(AssignmentRequestBuilder
                            .buildAssignmentRequest(false))));

            when(requestMappingService.createJudicialAssignments(any(), any()))
                    .thenReturn((ResponseEntity.status(HttpStatus.OK)
                            .body(responseEntities)));

            // WHEN
            ResponseEntity<Object> result = sut.prepareResponseCodes(responseEntityMap, userAccessProfiles,
                    UserType.JUDICIAL);

            // THEN
            assertNotNull(result);
            assertNotNull(result.getBody());
            assertEquals(responseEntities.size(), ((List<ResponseEntity<Object>>) result.getBody()).size());
            assertEquals(responseEntities.size(), responseEntityMap.size());

        }

    }


    @Nested
    class BuildSuccessAndFailureBucketTests {

        @Test
        void buildSuccessAndFailureBucket_Success() {

            Map<String, HttpStatus> responseEntityMap = new HashMap<>();
            responseEntityMap.put("1234", HttpStatus.CREATED);

            RefreshJobEntity refreshJobEntitySpy = Mockito.spy(TestDataBuilder.buildRefreshJobEntity());

            sut.buildSuccessAndFailureBucket(responseEntityMap, refreshJobEntitySpy);


            verify(refreshJobEntitySpy, times(1)).setStatus(any());
            verify(refreshJobEntitySpy, times(1)).setCreated(any());
            verify(refreshJobEntitySpy, times(1)).setLog(any());
            verify(persistenceService, times(1)).persistRefreshJob(any());
        }

        @Test
        void buildSuccessAndFailureBucket_Failure() {

            Map<String, HttpStatus> responseEntityMap = new HashMap<>();
            responseEntityMap.put("1234", HttpStatus.CONFLICT);

            RefreshJobEntity refreshJobEntitySpy = Mockito.spy(TestDataBuilder.buildRefreshJobEntity());

            sut.buildSuccessAndFailureBucket(responseEntityMap, refreshJobEntitySpy);


            verify(refreshJobEntitySpy, times(1)).setStatus(any());
            verify(refreshJobEntitySpy, times(1)).setUserIds(any());
            verify(refreshJobEntitySpy, times(1)).setCreated(any());
            verify(refreshJobEntitySpy, times(1)).setLog(any());
            verify(persistenceService, times(1)).persistRefreshJob(any());
        }

    }

    @Nested
    class UpdateJobStatusTests {

        @Test
        void updateJobStatus_Success() {
            String successId = "1234";
            RefreshJobEntity refreshJobEntitySpy = Mockito.spy(TestDataBuilder.buildRefreshJobEntity());
            sut.updateJobStatus(
                    Collections.singletonList(successId),
                    new ArrayList<>(),
                    refreshJobEntitySpy);

            verify(refreshJobEntitySpy, times(1)).setStatus(any());
            verify(refreshJobEntitySpy, times(1)).setCreated(any());
            verify(refreshJobEntitySpy, times(1)).setLog(any());
            verify(persistenceService, times(1)).persistRefreshJob(any());

        }

        @Test
        void updateJobStatus_Failure() {
            String failureId = "1234";
            RefreshJobEntity refreshJobEntitySpy = Mockito.spy(TestDataBuilder.buildRefreshJobEntity());
            sut.updateJobStatus(
                    new ArrayList<>(),
                    Collections.singletonList(failureId),
                    refreshJobEntitySpy);

            verify(refreshJobEntitySpy, times(1)).setStatus(any());
            verify(refreshJobEntitySpy, times(1)).setUserIds(any());
            verify(refreshJobEntitySpy, times(1)).setCreated(any());
            verify(refreshJobEntitySpy, times(1)).setLog(any());
            verify(persistenceService, times(1)).persistRefreshJob(any());

        }

        @Test
        @DisplayName("updateJobStatus_EmptyList")
        void updateJobStatus_EmptyList() {
            RefreshJobEntity refreshJobEntitySpy = Mockito.spy(TestDataBuilder.buildRefreshJobEntity());
            sut.updateJobStatus(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    refreshJobEntitySpy);
            verify(refreshJobEntitySpy, times(0)).setStatus(any());
        }

    }


    private void setUpMocks_RefreshJobByServiceName_Caseworker(int totalRecords) {

        List<CaseWorkerProfilesResponse> userProfilesResponseList = new ArrayList<>();
        userProfilesResponseList.add(TestDataBuilder.buildUserProfilesResponse());
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("total_records", String.valueOf(totalRecords));

        ResponseEntity<List<CaseWorkerProfilesResponse>> responseEntity
                = new ResponseEntity<>(userProfilesResponseList, headers, HttpStatus.OK);

        doReturn(responseEntity).when(crdService)
                .fetchCaseworkerDetailsByServiceName(any(), any(), any(), any(), any());

        when(requestMappingService.createCaseworkerAssignments(any()))
                .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));
    }

    private Map<String, Set<UserAccessProfile>> setUpMocks_RefreshJobByServiceName_Judicial(int totalRecords) {

        List<Object> userProfilesResponseList = new ArrayList<>(buildJudicialProfileV2(
                TestDataBuilder.buildRefreshRoleRequest(), "judicialProfileSampleV2.json"
        ));
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("total_records", String.valueOf(totalRecords));

        ResponseEntity<List<Object>> responseEntity
                = new ResponseEntity<>(userProfilesResponseList, headers, HttpStatus.OK);

        doReturn(responseEntity).when(jrdService)
                .fetchJudicialDetailsByServiceName(any(), any(), any(), any(), any());

        Map<String, Set<UserAccessProfile>> userAccessProfiles = new HashMap<>();
        userProfilesResponseList.forEach(item -> {
            JudicialProfileV2 judicialProfile = (JudicialProfileV2) item;
            JudicialAccessProfile accessProfile = TestDataBuilder.buildJudicialAccessProfile();
            accessProfile.setUserId(judicialProfile.getSidamId());
            Set<UserAccessProfile> userAccessProfileSet = new HashSet<>();
            userAccessProfileSet.add(accessProfile);
            userAccessProfiles.put(judicialProfile.getSidamId(), userAccessProfileSet);
        });

        when(retrieveDataService.retrieveProfilesByServiceName(responseEntity, UserType.JUDICIAL))
                .thenReturn(userAccessProfiles);

        when(requestMappingService.createJudicialAssignments(eq(userAccessProfiles), any()))
                .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));

        return userAccessProfiles;
    }

    private void mockFetchRefreshJobById(Long jobId, RoleCategory category, String status) {
        when(persistenceService.fetchRefreshJobById(jobId))
                .thenReturn(Optional.of(
                        RefreshJobEntity.builder()
                                .roleCategory(category.toString())
                                .status(status)
                                .build()));
    }

}
