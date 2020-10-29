package uk.gov.hmcts.reform.orgrolemapping.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.BulkAssignmentOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.TopicPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WelcomeControllerTest {

    @Mock
    private BulkAssignmentOrchestrator bulkAssignmentOrchestrator;

    @Mock
    private TopicPublisher topicPublisher;

    @InjectMocks
    private final WelcomeController sut = new WelcomeController(topicPublisher, bulkAssignmentOrchestrator);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void index() {
        assertEquals("redirect:swagger-ui.html", sut.index());
    }

    @Test
    void welcome() {
        assertEquals("Welcome to Organisation Role Mapping Service", sut.welcome());
    }

    @Test
    void createOrgMapping() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        ResponseEntity<Object> response =
                ResponseEntity.status(HttpStatus.CREATED).body(userRequest);

        Mockito.when(bulkAssignmentOrchestrator.createBulkAssignmentsRequest(Mockito.any(UserRequest.class)))
                .thenReturn(response);

        assertEquals(response, sut.createOrgMapping(userRequest));
    }
}