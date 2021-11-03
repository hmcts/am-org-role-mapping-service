
package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshRoleRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_STCW;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_TCW;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildJudicialProfile;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserProfile;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserRequest;

@RunWith(MockitoJUnitRunner.class)
class RetrieveDataServiceTest {


    private final CRDService crdService = Mockito.mock(CRDService.class);
    private final JRDService jrdService = Mockito.mock(JRDService.class);
    private final ParseRequestService parseRequestService = Mockito.mock(ParseRequestService.class);


    RetrieveDataService sut = new RetrieveDataService(parseRequestService, crdService, jrdService);


    @Test
    void retrieveCaseWorkerProfilesTest() {


        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(TestDataBuilder
                .buildListOfUserProfiles(false, false, "1", "2",
                        ROLE_NAME_STCW, ROLE_NAME_TCW, true, true, false, true, "1", "2", false)))
                .when(crdService).fetchUserProfiles(TestDataBuilder.buildUserRequest());

        Map<String, Set<?>> result = sut.retrieveProfiles(TestDataBuilder.buildUserRequest(), UserType.CASEWORKER);

        assertEquals(1, result.size());

        Mockito.verify(crdService, Mockito.times(1))
                .fetchUserProfiles(any(UserRequest.class));
        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserProfiles(any(), any(), any(), any(), any());
    }

    @Test
    void retrieveInvalidCaseWorkerProfilesTest() {

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(
                TestDataBuilder.buildListOfUserProfiles(true, false, "1",
                        "2", ROLE_NAME_STCW, ROLE_NAME_TCW, false, true,
                        false, true, "1", "2",
                        false))).when(crdService).fetchUserProfiles(any());

        doCallRealMethod().when(parseRequestService).validateUserProfiles(any(), any(), any(), any(), any());
        Map<String, Set<?>> result = sut.retrieveProfiles(TestDataBuilder.buildUserRequest(), UserType.CASEWORKER);

        assertEquals(0, result.size());

    }

    @Test
    void shouldReturnCaseWorkerProfile() {

        doReturn(ResponseEntity
                .ok(buildUserProfile(buildUserRequest(),
                        "userProfileSample.json"))).when(crdService).fetchUserProfiles(any());


        doNothing().when(parseRequestService).validateUserProfiles(any(), any(), any(), any(), any());
        Map<String, Set<?>> response = sut.retrieveProfiles(buildUserRequest(), UserType.CASEWORKER);
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
        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        doReturn(ResponseEntity
                .ok(caseWorkerProfiles)).when(crdService).fetchUserProfiles(any());
        Map<String, Set<?>> response = sut.retrieveProfiles(buildUserRequest(), UserType.CASEWORKER);
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void retrieveJudicialProfilesTest() throws IOException {
        JudicialProfile profile = TestDataBuilder.buildJudicialProfile();

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonList(profile)))
                .when(jrdService).fetchJudicialProfiles(TestDataBuilder.buildRefreshRoleRequest());


        Map<String, Set<?>> result = sut.retrieveProfiles(TestDataBuilder.buildUserRequest(), UserType.JUDICIAL);

        assertEquals(1, result.size());

        Mockito.verify(jrdService, Mockito.times(1))
                .fetchJudicialProfiles(any(RefreshRoleRequest.class));
        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserProfiles(any(), any(), any(), any(), any());
    }

    @Test
    void shouldReturnJudicialProfile() {

        doReturn(ResponseEntity
                .ok(buildJudicialProfile(TestDataBuilder.buildRefreshRoleRequest(),
                        "judicialProfileSample.json"))).when(jrdService).fetchJudicialProfiles(any());


        doNothing().when(parseRequestService).validateUserProfiles(any(), any(), any(), any(), any());
        Map<String, Set<?>> response = sut.retrieveProfiles(buildUserRequest(), UserType.JUDICIAL);
        assertNotNull(response);
        response.forEach((k, v) -> {
                assertNotNull(k);
                assertNotNull(v);
                v.forEach(userAccessProfile -> {
                    assertEquals(k, ((JudicialAccessProfile) userAccessProfile).getUserId());
                });

            }
        );

    }


    @Test
    void shouldReturnZeroJudicialProfile() {
        List<JudicialProfile> judicialProfiles = new ArrayList<>();
        doReturn(ResponseEntity
                .ok(judicialProfiles)).when(jrdService).fetchJudicialProfiles(any());

        Map<String, Set<?>> response = sut.retrieveProfiles(buildUserRequest(), UserType.JUDICIAL);

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void getUserAccessProfile() {

        List<Object> userProfilesResponses = new ArrayList<>();
        userProfilesResponses.add(TestDataBuilder.buildUserProfilesResponse());
        ResponseEntity<List<Object>> responseEntity
                = new ResponseEntity<>(userProfilesResponses, HttpStatus.CREATED);

        Map<String, Set<?>> response = sut.retrieveProfilesByServiceName(responseEntity,
                UserType.CASEWORKER);
        assertNotNull(response);
        assertEquals(4, response.get("1").size());
    }

}

