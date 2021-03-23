package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.util.ArrayList;
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
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserProfile;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserRequest;

@RunWith(MockitoJUnitRunner.class)
class RetrieveDataServiceTest {

    private final CRDFeignClient crdFeignClient = Mockito.mock(CRDFeignClient.class);
    private final JRDFeignClient jrdFeignClient = Mockito.mock(JRDFeignClient.class);
    private final ParseRequestService parseRequestService = Mockito.mock(ParseRequestService.class);
    private final JRDFeignClient jrdFeignClient = Mockito.mock(JRDFeignClient.class);

    RetrieveDataService sut = new RetrieveDataService(parseRequestService, crdFeignClient,jrdFeignClient);

    @Test
    void retrieveCaseWorkerProfilesTest() {



        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(TestDataBuilder
                .buildListOfUserProfiles(false, false,"1", "2",
                        ROLE_NAME_STCW, ROLE_NAME_TCW, true, true, false, true, "1", "2", false)))
                .when(crdFeignClient).getCaseworkerDetailsById(TestDataBuilder.buildUserRequest());

        Map<String, Set<?>> result = sut.retrieveProfiles(TestDataBuilder.buildUserRequest(), UserType.CASEWORKER);

        assertEquals(1, result.size());

        Mockito.verify(crdFeignClient, Mockito.times(1))
                .getCaseworkerDetailsById(any(UserRequest.class));
        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserProfiles(any(), any(), any(),any(),any());
    }

    @Test
    void retrieveInvalidCaseWorkerProfilesTest() {

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(
                TestDataBuilder.buildListOfUserProfiles(true, false, "1",
                        "2", ROLE_NAME_STCW, ROLE_NAME_TCW, false, true,
                        false, true, "1", "2",
                        false))).when(crdFeignClient).getCaseworkerDetailsById(any());

        doCallRealMethod().when(parseRequestService).validateUserProfiles(any(), any(), any(),any(),any());
        Map<String, Set<?>> result = sut.retrieveProfiles(TestDataBuilder.buildUserRequest(),UserType.CASEWORKER);

        assertEquals(0, result.size());

    }

    @Test
    void shouldReturnCaseWorkerProfile() {

        doReturn(ResponseEntity
                .ok(buildUserProfile(buildUserRequest()))).when(crdFeignClient).getCaseworkerDetailsById(any());


        doNothing().when(parseRequestService).validateUserProfiles(any(), any(), any(),any(),any());
        Map<String, Set<?>> response = sut.retrieveProfiles(buildUserRequest(),UserType.CASEWORKER);
        assertNotNull(response);
        response.forEach((k,v) -> {
            assertNotNull(k);
            assertNotNull(v);
            v.forEach(userAccessProfile -> {
                assertEquals(k, ((CaseWorkerAccessProfile)userAccessProfile).getId());
                assertFalse(((CaseWorkerAccessProfile)userAccessProfile).isSuspended());

            });

            }
        );

    }

    @Test
    void shouldReturnZeroCaseWorkerProfile() {
        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        doReturn(ResponseEntity
                .ok(caseWorkerProfiles)).when(crdFeignClient).getCaseworkerDetailsById(any());
        Map<String, Set<?>> response = sut.retrieveProfiles(buildUserRequest(),UserType.CASEWORKER);
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void shouldReturnJudicialProfile() {

        when(jrdFeignClient.getJudicialDetailsById(any())).thenReturn(ResponseEntity
                .ok(buildUserProfile(buildUserRequest())));
        doNothing().when(parseRequestService).validateUserProfiles(any(), any(), any(),any());
        Map<String, Set<JudicialAccessProfile>> response = sut.retrieveJudicialProfiles(buildUserRequest());
        assertNotNull(response);
        response.forEach((k,v) -> {
                    assertNotNull(k);
                    assertNotNull(v);
                    v.forEach(judicialAccessProfile -> {
                        assertEquals(k, judicialAccessProfile.getId());

                        assertEquals("219164", judicialAccessProfile.getPrimaryLocationId());
                    });

                }
        );

    }

    @Test
    void shouldReturnZeroJudicialProfile() {
        List<JudicialProfile> judicialProfiles = new ArrayList<>();
        when(jrdFeignClient.getJudicialDetailsById(any())).thenReturn(ResponseEntity
                .ok(judicialProfiles));
        Map<String, Set<JudicialAccessProfile>> response = sut.retrieveJudicialProfiles(buildUserRequest());
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void retrieveInvalidJudicialProfilesTest() {

        when(jrdFeignClient.getJudicialDetailsById(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(
                        TestDataBuilder.buildListOfJudicialProfiles(true, false, "1",
                                "2", "Tribunal Judge", false, true,
                                false, true, "1", "2",
                                false)));

        doCallRealMethod().when(parseRequestService).validateUserProfiles(any(), any(), any(),any());
        Map<String, Set<JudicialAccessProfile>> result = sut.retrieveJudicialProfiles(TestDataBuilder.buildUserRequest());

        assertEquals(0, result.size());

    }

}
