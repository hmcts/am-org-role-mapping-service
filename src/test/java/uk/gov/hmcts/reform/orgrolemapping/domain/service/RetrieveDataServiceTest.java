package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.CRDFeignClientFallback;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserProfile;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserRequest;

@RunWith(MockitoJUnitRunner.class)
class RetrieveDataServiceTest {

    @Mock
    private final CRDFeignClientFallback crdFeignClientFallback = mock(CRDFeignClientFallback.class);
    @Mock
    private final ParseRequestService parseRequestService = mock(ParseRequestService.class);

    @InjectMocks
    private final RetrieveDataService sut = new RetrieveDataService(parseRequestService, crdFeignClientFallback);


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
