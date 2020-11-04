package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.buildAssignmentRequest;

@RunWith(MockitoJUnitRunner.class)
public class BulkAssignmentOrchestratorTest {

    @Mock
    private final ParseRequestService parseRequestService = mock(ParseRequestService.class);

    @Mock
    private final  RetrieveDataService retrieveDataService = mock(RetrieveDataService.class);

    @Mock
    private final RequestMappingService  requestMappingService = mock(RequestMappingService.class);

    @InjectMocks
    private BulkAssignmentOrchestrator sut = new BulkAssignmentOrchestrator(parseRequestService,retrieveDataService,requestMappingService);


    @Test
    public void shouldReturn200Response(){
      UserRequest userRequest = UserRequest.builder()
                .users(Arrays.asList("123e4567-e89b-42d3-a456-556642445678","123e4567-e89b-42d3-a456-556642445698"))
                .build();



       ResponseEntity<Object> entity = ResponseEntity.status(HttpStatus.OK)
                .body(buildAssignmentRequest(true));

        Map<String, Set<UserAccessProfile>> userAccessProfiles = UserAccessProfileBuilder.buildUserAccessProfiles();

        doNothing().when(parseRequestService).validateUserRequest(any());
        when(retrieveDataService.retrieveCaseWorkerProfiles(any())).thenReturn(userAccessProfiles);
        when(requestMappingService.createCaseWorkerAssignments(userAccessProfiles)).thenReturn(entity);

        ResponseEntity<Object> actualResponse = sut.createBulkAssignmentsRequest(userRequest);
        assertNotNull(actualResponse);
        assertEquals(200, actualResponse.getStatusCode());

    }

}
