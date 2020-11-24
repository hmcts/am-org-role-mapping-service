package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.CRDFeignClientFallback;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import java.util.Map;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
class RetrieveDataServiceTest {

    private final CRDFeignClient crdFeignClient = Mockito.mock(CRDFeignClient.class);
    private final ParseRequestService parseRequestService = Mockito.mock(ParseRequestService.class);
    private final CRDFeignClientFallback crdFeignClientFallback = Mockito.mock(CRDFeignClientFallback.class);

    RetrieveDataService sut = new RetrieveDataService(crdFeignClient,parseRequestService,crdFeignClientFallback);

    @Test
    void retrieveCaseWorkerProfilesTest() {

        when(crdFeignClientFallback.createRoleAssignment(TestDataBuilder.buildUserRequest()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(TestDataBuilder.buildListOfUserProfiles()));

        Map<String, Set<UserAccessProfile>> result = sut.retrieveCaseWorkerProfiles(TestDataBuilder.buildUserRequest());

        assertEquals(2, result.size());

        Mockito.verify(crdFeignClientFallback, Mockito.times(1))
                .createRoleAssignment(Mockito.any(UserRequest.class));
        Mockito.verify(parseRequestService, Mockito.times(1))
                .validateUserProfiles(Mockito.any(), Mockito.any());
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
