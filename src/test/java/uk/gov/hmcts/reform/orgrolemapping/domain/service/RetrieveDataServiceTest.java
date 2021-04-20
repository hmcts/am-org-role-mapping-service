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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.JRDFeignClientFallback;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.time.LocalDateTime;
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

    private final CRDFeignClient crdFeignClient = Mockito.mock(CRDFeignClient.class);
    private final JRDFeignClientFallback jrdFeignClient = Mockito.mock(JRDFeignClientFallback.class);
    private final ParseRequestService parseRequestService = Mockito.mock(ParseRequestService.class);


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
                .ok(buildUserProfile(buildUserRequest(),
                        "userProfileSample.json"))).when(crdFeignClient).getCaseworkerDetailsById(any());


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
    void retrieveJudicialProfilesTest() {

        JudicialProfile profile = TestDataBuilder.buildJudicialProfile("37395", "EMP37395",
                "Magistrate", "Joe", "Bloggs", "Joe Bloggs", "Miss",
                "1", "Fee Paid Judiciary 5 Days Mon - Fri", "EMP62506@ejudiciary.net",
                LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                "2020-04-28T16:00:49", "TRUE",
                Collections.singletonList(TestDataBuilder.buildJPAppointment("84",
                        "5",
                        "1351",
                        "1",
                        "north-east",
                        LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                        LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                        "1")),
                Collections.singletonList(TestDataBuilder.buildJPAuthorisation("52149")));

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonList(profile)))
                .when(jrdFeignClient).getJudicialDetailsById(TestDataBuilder.buildUserRequest());

        Map<String, Set<?>> result = sut.retrieveProfiles(TestDataBuilder.buildUserRequest(), UserType.JUDICIAL);

        assertEquals(1, result.size());

        Mockito.verify(jrdFeignClient, Mockito.times(1))
                .getJudicialDetailsById(any(UserRequest.class));
        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserProfiles(any(), any(), any(),any(),any());
    }

    @Test
    void shouldReturnJudicialProfile() {

        doReturn(ResponseEntity
                .ok(buildJudicialProfile(buildUserRequest(),
                        "judicialProfileSample.json"))).when(jrdFeignClient).getJudicialDetailsById(any());


        doNothing().when(parseRequestService).validateUserProfiles(any(), any(), any(),any(),any());
        Map<String, Set<?>> response = sut.retrieveProfiles(buildUserRequest(),UserType.JUDICIAL);
        assertNotNull(response);
        response.forEach((k,v) -> {
            assertNotNull(k);
            assertNotNull(v);
            v.forEach(userAccessProfile -> {
                assertEquals(k, ((JudicialAccessProfile)userAccessProfile).getUserId());
            });

        }
        );

    }

    @Test
    void shouldReturnZeroJudicialProfile() {
        List<JudicialProfile> judicialProfiles = new ArrayList<>();
        doReturn(ResponseEntity
                .ok(judicialProfiles)).when(jrdFeignClient).getJudicialDetailsById(any());

        Map<String, Set<?>> response = sut.retrieveProfiles(buildUserRequest(),UserType.JUDICIAL);
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }



}
