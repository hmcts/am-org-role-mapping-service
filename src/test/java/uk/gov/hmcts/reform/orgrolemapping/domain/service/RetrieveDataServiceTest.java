
package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshOrchestrator.ERROR_INVALID_USER_TYPE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_STCW;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_TCW;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildJudicialProfileV2;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserProfile;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserRequest;

import feign.FeignException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.UnprocessableEntityException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JRDUserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfileV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

@RunWith(MockitoJUnitRunner.class)
class RetrieveDataServiceTest {


    private final CRDService crdService = Mockito.mock(CRDService.class);
    private final JRDService jrdService = Mockito.mock(JRDService.class);
    private final ParseRequestService parseRequestService = Mockito.mock(ParseRequestService.class);

    RetrieveDataService sut = new RetrieveDataService(parseRequestService, crdService, jrdService, true);

    @Test
    void retrieveCaseWorkerProfilesTest() {


        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(TestDataBuilder
                .buildListOfUserProfiles(false, false, "1", "2",
                        ROLE_NAME_STCW, ROLE_NAME_TCW, true, true, false, true, "1", "2", false)))
                .when(crdService).fetchCaseworkerProfiles(any());

        Map<String, Set<UserAccessProfile>> result
                = sut.retrieveProfiles(TestDataBuilder.buildUserRequest(), UserType.CASEWORKER);

        assertEquals(1, result.size());

        Mockito.verify(crdService, Mockito.times(1))
                .fetchCaseworkerProfiles(any(UserRequest.class));
        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserProfiles(any(), any(), any(), any(), any());
    }

    @Test
    void retrieveInvalidCaseWorkerProfilesTest() {

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(
                TestDataBuilder.buildListOfUserProfiles(true, false, "1",
                        "2", ROLE_NAME_STCW, ROLE_NAME_TCW, false, true,
                        false, true, "1", "2",
                        false))).when(crdService).fetchCaseworkerProfiles(any());

        doCallRealMethod().when(parseRequestService).validateUserProfiles(any(), any(), any(), any(), any());
        Map<String, Set<UserAccessProfile>> result
                = sut.retrieveProfiles(TestDataBuilder.buildUserRequest(), UserType.CASEWORKER);

        assertEquals(0, result.size());

    }

    @Test
    void shouldReturnCaseWorkerProfile() {

        doReturn(ResponseEntity
                .ok(buildUserProfile(buildUserRequest(),
                        "userProfileSample.json"))).when(crdService).fetchCaseworkerProfiles(any());


        doNothing().when(parseRequestService).validateUserProfiles(any(), any(), any(), any(), any());
        Map<String, Set<UserAccessProfile>> response = sut.retrieveProfiles(buildUserRequest(),
                UserType.CASEWORKER);
        assertNotNull(response);
        response.forEach((k, v) -> {
                assertNotNull(k);
                assertNotNull(v);
                v.forEach(userAccessProfile -> {
                    assertEquals(k, ((CaseWorkerAccessProfile) userAccessProfile).getId());
                    assertFalse(((CaseWorkerAccessProfile) userAccessProfile).isSuspended());

                });

            }
        );

    }

    @Test
    void shouldReturnZeroCaseWorkerProfile() {
        List<CaseWorkerProfile> caseWorkerProfiles = Collections.emptyList();
        doReturn(ResponseEntity
                .ok(caseWorkerProfiles)).when(crdService).fetchCaseworkerProfiles(any());
        Map<String, Set<UserAccessProfile>> response = sut.retrieveProfiles(buildUserRequest(),
                UserType.CASEWORKER);
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void retrieveJudicialProfilesTestV2() throws IOException {
        JudicialProfileV2 profile = TestDataBuilder.buildJudicialProfileV2();

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonList(profile)))
                .when(jrdService).fetchJudicialProfiles(TestDataBuilder.buildRefreshRoleRequest());


        Map<String, Set<UserAccessProfile>> result
                = sut.retrieveProfiles(TestDataBuilder.buildUserRequest(), UserType.JUDICIAL);

        assertEquals(1, result.size());

        Mockito.verify(jrdService, Mockito.times(1))
                .fetchJudicialProfiles(any(JRDUserRequest.class));
        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserProfiles(any(), any(), any(), any(), any());
    }

    @Test
    void retrieveJudicialProfilesTestWithInvalidV2() throws IOException {
        JudicialProfileV2 profile = TestDataBuilder.buildJudicialProfileV2();

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonList(profile)))
                .when(jrdService).fetchJudicialProfiles(TestDataBuilder.buildRefreshRoleRequest());


        Map<String, Set<UserAccessProfile>> result
                = sut.retrieveProfiles(TestDataBuilder.buildUserRequest(), UserType.JUDICIAL);

        assertEquals(1, result.size());

        Mockito.verify(jrdService, Mockito.times(1))
                .fetchJudicialProfiles(any(JRDUserRequest.class));
        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserProfiles(any(), any(), any(), any(), any());
    }

    @Test
    void shouldReturnJudicialProfileV2() {

        doReturn(ResponseEntity
            .ok(buildJudicialProfileV2(TestDataBuilder.buildRefreshRoleRequest(),
            "judicialProfileSampleV2.json"))).when(jrdService).fetchJudicialProfiles(any());


        doNothing().when(parseRequestService).validateUserProfiles(any(), any(), any(), any(), any());
        Map<String, Set<UserAccessProfile>> response = sut.retrieveProfiles(buildUserRequest(), UserType.JUDICIAL);
        assertNotNull(response);
        response.forEach((k, v) -> {
                assertNotNull(k);
                assertNotNull(v);
                v.forEach(userAccessProfile ->
                        assertEquals(k, ((JudicialAccessProfile) userAccessProfile).getUserId()));
            }
        );
    }

    @Test
    void shouldReturnJudicialProfileV2_withAppointmentsFlag() {

        doReturn(ResponseEntity
                .ok(buildJudicialProfileV2(TestDataBuilder.buildRefreshRoleRequest(),
                        "judicialProfileSampleV2.json"))).when(jrdService).fetchJudicialProfiles(any());
        doNothing().when(parseRequestService).validateUserProfiles(any(), any(), any(), any(), any());

        // WHEN
        Map<String, Set<UserAccessProfile>> response = sut.retrieveProfiles(buildUserRequest(), UserType.JUDICIAL);

        // THEN
        assertNotNull(response);
        response.forEach((k, v) -> {
            assertNotNull(k);
            assertNotNull(v);
            v.forEach(userAccessProfile -> {
                // FILTERED: only 2 authorisations from "judicialProfileSampleV2.json" attached to each appointment
                assertEquals(2, ((JudicialAccessProfile) userAccessProfile).getAuthorisations().size());
            });
        });
    }

    @ParameterizedTest
    @CsvSource({
        "true,true,0",
        "true,false,2",
        "false,true,2",
        "false,false,2"
    })
    void shouldReturnJudicialProfileV2_deletedFlag(Boolean filterSoftDeletedUsersEnabled,
                                                   Boolean deletedFlagStatus,
                                                   int expectedUserAccessProfileCount) throws IOException {
        sut = new RetrieveDataService(parseRequestService, crdService, jrdService, filterSoftDeletedUsersEnabled);

        JudicialProfileV2 profile = TestDataBuilder.buildJudicialProfileV2();
        profile.setDeletedFlag(deletedFlagStatus.toString());

        doReturn(ResponseEntity.ok(List.of(profile))).when(jrdService).fetchJudicialProfiles(any());

        Map<String, Set<UserAccessProfile>> response
                = sut.retrieveProfiles(TestDataBuilder.buildUserRequest(), UserType.JUDICIAL);

        assertNotNull(response);
        assertEquals(expectedUserAccessProfileCount, response.get(profile.getSidamId()).size());
    }

    @Test
    void shouldReturnZeroJudicialProfileV2() {
        List<JudicialProfileV2> judicialProfiles = Collections.emptyList();
        doReturn(ResponseEntity
                .ok(judicialProfiles)).when(jrdService).fetchJudicialProfiles(any());

        Map<String, Set<UserAccessProfile>> response = sut.retrieveProfiles(buildUserRequest(), UserType.JUDICIAL);

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void shouldThrowNotFoundOnInvalidJudicialProfileV2() {
        UserRequest request = buildUserRequest();
        doReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("errorDescription",
                "The User Profile data could not be found"))).when(jrdService).fetchJudicialProfiles(any());

        Map<String, Set<UserAccessProfile>> response = sut.retrieveProfiles(request, UserType.JUDICIAL);
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void shouldThrowFeignExceptionNotFoundOnInvalidJudicialProfileV2() {
        UserRequest request = buildUserRequest();
        FeignException.NotFound notFound = mock(FeignException.NotFound.class);
        doThrow(notFound).when(jrdService).fetchJudicialProfiles(any());

        Map<String, Set<UserAccessProfile>> response = sut.retrieveProfiles(request, UserType.JUDICIAL);
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void shouldThrowUnprocessableOnInvalidJudicialProfileV2() {
        UserRequest request = buildUserRequest();
        doReturn(ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(Map.of("errorDescription",
                "server have some problem"))).when(jrdService).fetchJudicialProfiles(any());

        assertThrows(UnprocessableEntityException.class, () -> sut.retrieveProfiles(request, UserType.JUDICIAL));
    }

    @Nested
    class RetrieveProfilesByServiceName {

        @Test
        void shouldThrowUnprocessableOnInvalidUserType() {

            // GIVEN
            ResponseEntity<List<Object>> responseEntity
                    = new ResponseEntity<>(new ArrayList<>(), HttpStatus.CREATED);

            // WHEN / THEN
            UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,() ->
                    sut.retrieveProfilesByServiceName(responseEntity, null)
            );

            assertTrue(exception.getLocalizedMessage().contains(ERROR_INVALID_USER_TYPE));
        }

        @Test
        void getUserAccessProfile_Caseworker() {

            // GIVEN
            List<Object> userProfilesResponses = new ArrayList<>();
            userProfilesResponses.add(TestDataBuilder.buildUserProfilesResponse());
            ResponseEntity<List<Object>> responseEntity
                    = new ResponseEntity<>(userProfilesResponses, HttpStatus.CREATED);

            // WHEN
            Map<String, Set<UserAccessProfile>> response = sut.retrieveProfilesByServiceName(responseEntity,
                    UserType.CASEWORKER);

            // THEN
            assertNotNull(response);
            assertEquals(4, response.get("1").size());
        }

        @Test
        void getUserAccessProfile_Judicial() {

            // GIVEN
            List<Object> userProfilesResponses = new ArrayList<>(buildJudicialProfileV2(
                    TestDataBuilder.buildRefreshRoleRequest(), "judicialProfileSampleV2.json"
            ));
            ResponseEntity<List<Object>> responseEntity
                    = new ResponseEntity<>(userProfilesResponses, HttpStatus.CREATED);

            // WHEN
            Map<String, Set<UserAccessProfile>> response = sut.retrieveProfilesByServiceName(responseEntity,
                    UserType.JUDICIAL);

            // THEN
            assertNotNull(response);
            assertEquals(userProfilesResponses.size(), response.size());
            // verify both test profiles found.  NB: size 2 as two appointments in "judicialProfileSampleV2.json"
            assertEquals(2, response.get(TestDataBuilder.id_1).size());
            assertEquals(2, response.get(TestDataBuilder.id_2).size());
        }

        @ParameterizedTest
        @NullAndEmptySource
        void getUserAccessProfile_Judicial_shouldFilterProfilesWithNoIdamId(String nullOrEmptyIdamId) {

            // GIVEN
            List<JudicialProfileV2> judicialProfiles = buildJudicialProfileV2(
                    TestDataBuilder.buildRefreshRoleRequest(), "judicialProfileSampleV2.json"
            );
            List<Object> userProfilesResponses = new ArrayList<>();
            // add profiles to test input but clear IDAM_ID 1
            judicialProfiles.forEach(judicialProfile -> {
                if (TestDataBuilder.id_1.equals(judicialProfile.getSidamId())) {
                    judicialProfile.setSidamId(nullOrEmptyIdamId);
                }
                userProfilesResponses.add(judicialProfile);
            });
            ResponseEntity<List<Object>> responseEntity
                    = new ResponseEntity<>(userProfilesResponses, HttpStatus.CREATED);

            // WHEN
            Map<String, Set<UserAccessProfile>> response = sut.retrieveProfilesByServiceName(responseEntity,
                    UserType.JUDICIAL);

            // THEN
            assertNotNull(response);
            assertEquals(1, response.size()); // i.e. as ID 1 is removed
            // verify test profiles ID 1 not found.
            assertFalse(response.containsKey(TestDataBuilder.id_1));
            // verify test profiles ID 2 found.  NB: size 2 as two appointments in "judicialProfileSampleV2.json"
            assertEquals(2, response.get(TestDataBuilder.id_2).size());
        }

        @Test
        void getUserAccessProfile_Judicial_shouldNotErrorIfAllProfilesFiltered() {

            // GIVEN
            List<JudicialProfileV2> judicialProfiles = buildJudicialProfileV2(
                    TestDataBuilder.buildRefreshRoleRequest(), "judicialProfileSampleV2.json"
            );
            List<Object> userProfilesResponses = new ArrayList<>();
            // add profiles to test input but clear all IDAM IDs
            judicialProfiles.forEach(judicialProfile -> {
                if (TestDataBuilder.id_1.equals(judicialProfile.getSidamId())) {
                    judicialProfile.setSidamId(null);
                } else {
                    judicialProfile.setSidamId("");
                }
                userProfilesResponses.add(judicialProfile);
            });
            ResponseEntity<List<Object>> responseEntity
                    = new ResponseEntity<>(userProfilesResponses, HttpStatus.CREATED);

            // WHEN
            Map<String, Set<UserAccessProfile>> response = sut.retrieveProfilesByServiceName(responseEntity,
                    UserType.JUDICIAL);

            // THEN
            assertNotNull(response);
            assertEquals(0, response.size()); // i.e. all profiles filtered
        }

    }

}
