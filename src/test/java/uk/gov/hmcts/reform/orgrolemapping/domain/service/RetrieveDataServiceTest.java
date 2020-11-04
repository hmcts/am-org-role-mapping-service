/*
package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.CRDFeignClientFallback;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RetrieveDataServiceTest {

    @Mock
    private final CRDFeignClientFallback crdFeignClientFallback = mock(CRDFeignClientFallback.class);
    @Mock
    private final ParseRequestService parseRequestService = mock(ParseRequestService.class);

   @InjectMocks
   private final RetrieveDataService sut = new RetrieveDataService();



   @Test
   void shouldReturnCaseWorkerProfile(){

       UserRequest userRequest = UserRequest.builder()
               .users(Arrays.asList("123e4567-e89b-42d3-a456-556642445678","123e4567-e89b-42d3-a456-556642445698"))
               .build();
     UserProfile userProfile = UserProfile.builder()
               .id("123e4567-e89b-42d3-a456-556642445678")
               .deleteFlag(false)
               .build();

       when(crdFeignClientFallback.createRoleAssignment(any())).thenReturn(ResponseEntity.ok(Arrays.asList(userProfile)));
       doNothing().when(parseRequestService).validateUserProfiles(any(),any());

       Map<String, Set<UserAccessProfile>> response = sut.retrieveCaseWorkerProfiles(userRequest);
       assertNotNull(response);

   }

}
*/
