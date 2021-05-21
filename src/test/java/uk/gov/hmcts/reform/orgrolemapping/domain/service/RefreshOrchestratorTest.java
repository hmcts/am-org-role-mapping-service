package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Request;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Status;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_TCW;

@RunWith(MockitoJUnitRunner.class)
class RefreshOrchestratorTest {



    private final RetrieveDataService retrieveDataService = mock(RetrieveDataService.class);
    private final RequestMappingService requestMappingService = mock(RequestMappingService.class);
    private final ParseRequestService parseRequestService = mock(ParseRequestService.class);
    private final CRDService crdService = mock(CRDService.class);
    private final PersistenceService persistenceService = mock(PersistenceService.class);

    @InjectMocks
    private final RefreshOrchestrator sut = new RefreshOrchestrator(
            retrieveDataService,
            requestMappingService, parseRequestService, crdService, persistenceService);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void refreshRoleAssignmentRecords() {

        Mockito.when(persistenceService.fetchRefreshJobById(Mockito.any()))
                .thenReturn(Optional.of(
                RefreshJobEntity.builder().build()));

        Mockito.doNothing().when(parseRequestService).validateUserRequest(Mockito.any());

        Map<String, Set<UserAccessProfile>> userAccessProfiles = new HashMap<>();
        Set<UserAccessProfile> userAccessProfileSet = new HashSet<>();
        userAccessProfileSet.add(UserAccessProfile.builder()
                .id("1")
                .roleId("1")
                .roleName("roleName")
                .primaryLocationName("primary")
                .primaryLocationId("1")
                .areaOfWorkId("1")
                .serviceCode("1")
                .suspended(false)
                .build());
        userAccessProfiles.put("1", userAccessProfileSet);

        Mockito.when(retrieveDataService.retrieveCaseWorkerProfiles(Mockito.any()))
                .thenReturn(userAccessProfiles);

//        Collection<RoleAssignment> roleAssignmentList = new ArrayList<>();
//        roleAssignmentList.add(RoleAssignment.builder().build());
//
//        Request request = Request.builder()
//                .id(UUID.randomUUID())
//                .clientId("1")
//                .authenticatedUserId("1")
//                .correlationId("1")
//                .assignerId("1")
//                .requestType(RequestType.CREATE)
//                .process("process")
//                .reference("reference")
//                .replaceExisting(false)
//                .roleAssignmentId(UUID.randomUUID())
//                .status(Status.CREATE_REQUESTED)
//                .created(LocalDateTime.now())
//                .log("log")
//                .byPassOrgDroolRule("orgDroolRule")
//                .build();
//
//        AssignmentRequest assignmentRequest = AssignmentRequest.builder()
//                .request(request)
//                .requestedRoles(roleAssignmentList)
//                .build();

        Mockito.when(requestMappingService.createCaseWorkerAssignments(Mockito.any()))
                .thenReturn((ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList())));

        ResponseEntity<Object> response = sut.refresh(1L, TestDataBuilder.buildUserRequest());

        assertEquals(HttpStatus.OK, response.getStatusCode());

//        AssignmentRequest assignmentRequest = (AssignmentRequest) response.getBody();
//        assert assignmentRequest != null;
//        RoleAssignment roleAssignment = ((List<RoleAssignment>) assignmentRequest.getRequestedRoles()).get(0);
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(ROLE_NAME_TCW, roleAssignment.getRoleName());
//        assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType());
//        assertEquals(RoleCategory.LEGAL_OPERATIONS, roleAssignment.getRoleCategory());
//
//        Mockito.verify(retrieveDataService, Mockito.times(1))
//                .retrieveCaseWorkerProfiles(Mockito.any(UserRequest.class));
//        Mockito.verify(requestMappingService, Mockito.times(1))
//                .createCaseWorkerAssignments(Mockito.any());
    }
}
