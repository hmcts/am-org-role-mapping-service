package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;
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
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_STCW;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_TCW;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserProfile;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserRequest;

@RunWith(MockitoJUnitRunner.class)
class RetrieveDataServiceTest {

    private final CRDFeignClient crdFeignClient = Mockito.mock(CRDFeignClient.class);
    private final ParseRequestService parseRequestService = Mockito.mock(ParseRequestService.class);

    RetrieveDataService sut = new RetrieveDataService(parseRequestService, crdFeignClient);

    @Test
    void retrieveCaseWorkerProfilesTest() {

        when(crdFeignClient.getCaseworkerDetailsById(TestDataBuilder.buildUserRequest()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(TestDataBuilder
                        .buildListOfUserProfiles(false, false,"1", "2",
                        ROLE_NAME_STCW, ROLE_NAME_TCW, true, true, false, true, "1", "2", false)));

        Map<String, Set<UserAccessProfile>> result = sut.retrieveCaseWorkerProfiles(TestDataBuilder.buildUserRequest());

        assertEquals(1, result.size());

        Mockito.verify(crdFeignClient, Mockito.times(1))
                .getCaseworkerDetailsById(any(UserRequest.class));
        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserProfiles(any(), any(), any());
    }

    @Test
    void retrieveInvalidCaseWorkerProfilesTest() {

        when(crdFeignClient.getCaseworkerDetailsById(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(
                        TestDataBuilder.buildListOfUserProfiles(true, false, "1",
                                "2", ROLE_NAME_STCW, ROLE_NAME_TCW, false, true,
                                false, true, "1", "2",
                                false)));

        doCallRealMethod().when(parseRequestService).validateUserProfiles(any(), any(), any());
        Map<String, Set<UserAccessProfile>> result = sut.retrieveCaseWorkerProfiles(TestDataBuilder.buildUserRequest());

        assertEquals(2, result.size());

    }

    @Test
    void shouldReturnCaseWorkerProfile() {

        when(crdFeignClient.getCaseworkerDetailsById(any())).thenReturn(ResponseEntity
                .ok(buildUserProfile(buildUserRequest())));
        doNothing().when(parseRequestService).validateUserProfiles(any(), any(), any());
        Map<String, Set<UserAccessProfile>> response = sut.retrieveCaseWorkerProfiles(buildUserRequest());
        assertNotNull(response);
        response.forEach((k,v) -> {
            assertNotNull(k);
            assertNotNull(v);
            v.forEach(userAccessProfile -> {
                assertEquals(k, userAccessProfile.getId());
                assertFalse(userAccessProfile.isDeleteFlag());
                assertEquals("219164", userAccessProfile.getPrimaryLocationId());
            });

            }
        );

    }

    @Test
    void shouldReturnZeroCaseWorkerProfile() {
        List<UserProfile> userProfiles = new ArrayList<>();
        when(crdFeignClient.getCaseworkerDetailsById(any())).thenReturn(ResponseEntity
                .ok(userProfiles));
        Map<String, Set<UserAccessProfile>> response = sut.retrieveCaseWorkerProfiles(buildUserRequest());
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

}
