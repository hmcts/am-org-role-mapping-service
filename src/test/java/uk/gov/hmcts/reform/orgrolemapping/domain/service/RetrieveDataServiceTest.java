package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.CRDFeignClientFallback;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_STCW;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_TCW;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserProfile;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserRequest;

import java.util.Map;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
class RetrieveDataServiceTest {

    private final CRDFeignClient crdFeignClient = Mockito.mock(CRDFeignClient.class);
    private final ParseRequestService parseRequestService = Mockito.mock(ParseRequestService.class);
    private final CRDFeignClientFallback crdFeignClientFallback = Mockito.mock(CRDFeignClientFallback.class);

    RetrieveDataService sut = new RetrieveDataService(parseRequestService, crdFeignClientFallback);

    @Test
    void retrieveCaseWorkerProfilesTest() {

        when(crdFeignClientFallback.createRoleAssignment(TestDataBuilder.buildUserRequest()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(TestDataBuilder.buildListOfUserProfiles(false, false,"1", "2",
                        ROLE_NAME_STCW, ROLE_NAME_TCW, true, true, false, true, "1", "2", false)));

        Map<String, Set<UserAccessProfile>> result = sut.retrieveCaseWorkerProfiles(TestDataBuilder.buildUserRequest());

        assertEquals(1, result.size());

        Mockito.verify(crdFeignClientFallback, Mockito.times(1))
                .createRoleAssignment(any(UserRequest.class));
        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserProfiles(any(), any());
    }

    @Test
    void shouldReturnCaseWorkerProfile() {

        when(crdFeignClientFallback.createRoleAssignment(any())).thenReturn(ResponseEntity
                .ok(buildUserProfile(buildUserRequest())));
        doNothing().when(parseRequestService).validateUserProfiles(any(), any());
        Map<String, Set<UserAccessProfile>> response = sut.retrieveCaseWorkerProfiles(buildUserRequest());
        assertNotNull(response);
        response.entrySet().stream().forEach(entry -> {
            assertNotNull(entry.getKey());
            assertNotNull(entry.getValue());
            entry.getValue().stream().forEach(userAccessProfile -> {
                assertEquals(entry.getKey(), userAccessProfile.getId());
                assertEquals(false, userAccessProfile.isDeleteFlag());
                assertEquals("219164", userAccessProfile.getPrimaryLocationId());
            });

            }
        );

    }

}
