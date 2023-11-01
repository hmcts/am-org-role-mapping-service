package uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.BulkAssignmentOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;

class OrgMappingControllerTest {

    @Mock
    private BulkAssignmentOrchestrator bulkAssignmentOrchestrator;


    @InjectMocks
    private final OrgMappingController sut = new OrgMappingController(bulkAssignmentOrchestrator);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrgMappingTest() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        ResponseEntity<Object> response =
                ResponseEntity.status(HttpStatus.CREATED).body(userRequest);

        Mockito.when(bulkAssignmentOrchestrator.createBulkAssignmentsRequest(Mockito.any(UserRequest.class),
                eq(UserType.CASEWORKER)))
                .thenReturn(response);

        assertEquals(response, sut.createOrgMapping(userRequest, UserType.CASEWORKER));
    }

    @Test
    void createOrgMappingTest_Judicial() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        ResponseEntity<Object> response =
                ResponseEntity.status(HttpStatus.CREATED).body(userRequest);

        Mockito.when(bulkAssignmentOrchestrator.createBulkAssignmentsRequest(Mockito.any(UserRequest.class),
                        eq(UserType.JUDICIAL)))
                .thenReturn(response);

        assertEquals(response, sut.createOrgMapping(userRequest, UserType.JUDICIAL));
    }

    @Test
    void createOrgMappingTest_Unprocessable() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        ResponseEntity<Object> response =
                ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(null);

        Mockito.when(bulkAssignmentOrchestrator.createBulkAssignmentsRequest(Mockito.any(UserRequest.class),
                        eq(UserType.JUDICIAL)))
                .thenReturn(response);

        assertEquals(response, sut.createOrgMapping(userRequest, UserType.JUDICIAL));
    }

    @SuppressWarnings("removal") // suppress warning on use of deprecated endpoint
    @Test
    void createOrgMappingTest_deprecatedEndpoint() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        ResponseEntity<Object> response =
                ResponseEntity.status(HttpStatus.CREATED).body(userRequest);

        Mockito.when(bulkAssignmentOrchestrator.createBulkAssignmentsRequest(Mockito.any(UserRequest.class),
                eq(UserType.CASEWORKER)))
                .thenReturn(response);

        assertEquals(response, sut.createOrgMappingDeprecated(userRequest, UserType.CASEWORKER));
    }

}
