
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
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_STCW;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_TCW;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildJudicialProfile;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildJudicialProfileV2;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserProfile;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserRequest;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
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


    RetrieveDataService sutJrdV1 = new RetrieveDataService(parseRequestService, crdService, jrdService, false, false);
    RetrieveDataService sutJrdV2 = new RetrieveDataService(parseRequestService, crdService, jrdService, true, true);


    @Test
    void retrieveCaseWorkerProfilesTest() {


        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(TestDataBuilder
                .buildListOfUserProfiles(false, false, "1", "2",
                        ROLE_NAME_STCW, ROLE_NAME_TCW, true, true, false, true, "1", "2", false)))
                .when(crdService).fetchCaseworkerProfiles(any());

        Map<String, Set<UserAccessProfile>> result
                = sutJrdV1.retrieveProfiles(TestDataBuilder.buildUserRequest(), UserType.CASEWORKER);

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
                = sutJrdV1.retrieveProfiles(TestDataBuilder.buildUserRequest(), UserType.CASEWORKER);

        assertEquals(0, result.size());

    }

    @Test
    void shouldReturnCaseWorkerProfile() {

        doReturn(ResponseEntity
                .ok(buildUserProfile(buildUserRequest(),
                        "userProfileSample.json"))).when(crdService).fetchCaseworkerProfiles(any());


        doNothing().when(parseRequestService).validateUserProfiles(any(), any(), any(), any(), any());
        Map<String, Set<UserAccessProfile>> response = sutJrdV1.retrieveProfiles(buildUserRequest(),
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
        Map<String, Set<UserAccessProfile>> response = sutJrdV1.retrieveProfiles(buildUserRequest(),
                UserType.CASEWORKER);
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void retrieveJudicialProfilesTest() throws IOException {
        JudicialProfile profile = TestDataBuilder.buildJudicialProfile();

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonList(profile)))
                .when(jrdService).fetchJudicialProfiles(TestDataBuilder.buildRefreshRoleRequest());


        Map<String, Set<UserAccessProfile>> result
                = sutJrdV1.retrieveProfiles(TestDataBuilder.buildUserRequest(), UserType.JUDICIAL);

        assertEquals(1, result.size());

        Mockito.verify(jrdService, Mockito.times(1))
                .fetchJudicialProfiles(any(JRDUserRequest.class));
        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserProfiles(any(), any(), any(), any(), any());
    }

    @Test
    void retrieveJudicialProfilesTestV2() throws IOException {
        JudicialProfileV2 profile = TestDataBuilder.buildJudicialProfileV2();

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonList(profile)))
                .when(jrdService).fetchJudicialProfiles(TestDataBuilder.buildRefreshRoleRequest());


        Map<String, Set<UserAccessProfile>> result
                = sutJrdV2.retrieveProfiles(TestDataBuilder.buildUserRequest(), UserType.JUDICIAL);

        assertEquals(1, result.size());

        Mockito.verify(jrdService, Mockito.times(1))
                .fetchJudicialProfiles(any(JRDUserRequest.class));
        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserProfiles(any(), any(), any(), any(), any());
    }

    @Test
    void retrieveJudicialProfilesTestWithInvalid() throws IOException {
        JudicialProfile profile = TestDataBuilder.buildJudicialProfile();

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonList(profile)))
            .when(jrdService).fetchJudicialProfiles(TestDataBuilder.buildRefreshRoleRequest());


        Map<String, Set<UserAccessProfile>> result
            = sutJrdV1.retrieveProfiles(TestDataBuilder.buildUserRequest(), UserType.JUDICIAL);

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
                = sutJrdV2.retrieveProfiles(TestDataBuilder.buildUserRequest(), UserType.JUDICIAL);

        assertEquals(1, result.size());

        Mockito.verify(jrdService, Mockito.times(1))
                .fetchJudicialProfiles(any(JRDUserRequest.class));
        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserProfiles(any(), any(), any(), any(), any());
    }

    @Test
    void shouldReturnJudicialProfile() {

        doReturn(ResponseEntity
                .ok(buildJudicialProfile(TestDataBuilder.buildRefreshRoleRequest(),
                        "judicialProfileSample.json"))).when(jrdService).fetchJudicialProfiles(any());


        doNothing().when(parseRequestService).validateUserProfiles(any(), any(), any(), any(), any());
        Map<String, Set<UserAccessProfile>> response = sutJrdV1.retrieveProfiles(buildUserRequest(), UserType.JUDICIAL);
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
    void shouldReturnJudicialProfileV2() {

        doReturn(ResponseEntity
            .ok(buildJudicialProfileV2(TestDataBuilder.buildRefreshRoleRequest(),
            "judicialProfileSampleV2.json"))).when(jrdService).fetchJudicialProfiles(any());


        doNothing().when(parseRequestService).validateUserProfiles(any(), any(), any(), any(), any());
        Map<String, Set<UserAccessProfile>> response = sutJrdV2.retrieveProfiles(buildUserRequest(), UserType.JUDICIAL);
        assertNotNull(response);
        response.forEach((k, v) -> {
                assertNotNull(k);
                assertNotNull(v);
                v.forEach(userAccessProfile ->
                        assertEquals(k, ((JudicialAccessProfile) userAccessProfile).getUserId()));
            }
        );
    }

    @ParameterizedTest()
    @ValueSource(booleans = {true, false})
    void shouldReturnJudicialProfileV2_withAppointmentsFlag(boolean v2FilterAuthorisationsByAppointmentId) {

        // GIVEN
        // NB: use local SUT instance, so we can override filter flag.
        var sut = new RetrieveDataService(
            parseRequestService,
            crdService,
            jrdService,
            true,
            v2FilterAuthorisationsByAppointmentId
        );

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
                if (v2FilterAuthorisationsByAppointmentId) {
                    // FILTERED: only 2 authorisations from "judicialProfileSampleV2.json" attached to each appointment
                    assertEquals(2, ((JudicialAccessProfile) userAccessProfile).getAuthorisations().size());
                } else {
                    // UNFILTERED: all 3 authorisations from "judicialProfileSampleV2.json" attached to each appointment
                    assertEquals(3, ((JudicialAccessProfile) userAccessProfile).getAuthorisations().size());
                }
            });
        });
    }

    @Test
    void shouldReturnZeroJudicialProfile() {
        List<JudicialProfile> judicialProfiles = Collections.emptyList();
        doReturn(ResponseEntity
                .ok(judicialProfiles)).when(jrdService).fetchJudicialProfiles(any());

        Map<String, Set<UserAccessProfile>> response = sutJrdV1.retrieveProfiles(buildUserRequest(), UserType.JUDICIAL);

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void shouldReturnZeroJudicialProfileV2() {
        List<JudicialProfileV2> judicialProfiles = Collections.emptyList();
        doReturn(ResponseEntity
                .ok(judicialProfiles)).when(jrdService).fetchJudicialProfiles(any());

        Map<String, Set<UserAccessProfile>> response = sutJrdV2.retrieveProfiles(buildUserRequest(), UserType.JUDICIAL);

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void shouldThrowNotFoundOnInvalidJudicialProfile() {
        UserRequest request = buildUserRequest();
        doReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("errorDescription",
                "The User Profile data could not be found"))).when(jrdService).fetchJudicialProfiles(any());

        Map<String, Set<UserAccessProfile>> response = sutJrdV1.retrieveProfiles(request, UserType.JUDICIAL);
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void shouldThrowNotFoundOnInvalidJudicialProfileV2() {
        UserRequest request = buildUserRequest();
        doReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("errorDescription",
                "The User Profile data could not be found"))).when(jrdService).fetchJudicialProfiles(any());

        Map<String, Set<UserAccessProfile>> response = sutJrdV2.retrieveProfiles(request, UserType.JUDICIAL);
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void shouldThrowFeignExceptionNotFoundOnInvalidJudicialProfile() {
        UserRequest request = buildUserRequest();
        FeignException.NotFound notFound = mock(FeignException.NotFound.class);
        doThrow(notFound).when(jrdService).fetchJudicialProfiles(any());

        Map<String, Set<UserAccessProfile>> response = sutJrdV1.retrieveProfiles(request, UserType.JUDICIAL);
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void shouldThrowFeignExceptionNotFoundOnInvalidJudicialProfileV2() {
        UserRequest request = buildUserRequest();
        FeignException.NotFound notFound = mock(FeignException.NotFound.class);
        doThrow(notFound).when(jrdService).fetchJudicialProfiles(any());

        Map<String, Set<UserAccessProfile>> response = sutJrdV2.retrieveProfiles(request, UserType.JUDICIAL);
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void shouldThrowUnprocessableOnInvalidJudicialProfile() {
        UserRequest request = buildUserRequest();
        doReturn(ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(Map.of("errorDescription",
                "server have some problem"))).when(jrdService).fetchJudicialProfiles(any());

        assertThrows(UnprocessableEntityException.class, () -> sutJrdV1.retrieveProfiles(request, UserType.JUDICIAL));
    }

    @Test
    void shouldThrowUnprocessableOnInvalidJudicialProfileV2() {
        UserRequest request = buildUserRequest();
        doReturn(ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(Map.of("errorDescription",
                "server have some problem"))).when(jrdService).fetchJudicialProfiles(any());

        assertThrows(UnprocessableEntityException.class, () -> sutJrdV1.retrieveProfiles(request, UserType.JUDICIAL));
    }

    @Test
    void getUserAccessProfile() {

        List<Object> userProfilesResponses = new ArrayList<>();
        userProfilesResponses.add(TestDataBuilder.buildUserProfilesResponse());
        ResponseEntity<List<Object>> responseEntity
                = new ResponseEntity<>(userProfilesResponses, HttpStatus.CREATED);

        Map<String, Set<UserAccessProfile>> response = sutJrdV1.retrieveProfilesByServiceName(responseEntity,
                UserType.CASEWORKER);
        assertNotNull(response);
        assertEquals(4, response.get("1").size());
    }

}

