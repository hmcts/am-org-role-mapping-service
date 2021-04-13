package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.RASFeignClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class RoleAssignmentServiceTest {

    RASFeignClient rasFeignClient = mock(RASFeignClient.class);

    RoleAssignmentService sut = new RoleAssignmentService(rasFeignClient);

    @Test
    void testRASFeignClientTest() {
        Mockito.when(rasFeignClient.createRoleAssignment(ArgumentMatchers.any(AssignmentRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body("RoleAssignment"));
        ResponseEntity<Object> responseEntity = sut.createRoleAssignment(AssignmentRequest.builder().build());
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    void testGetServiceStatus() {
        Mockito.when(rasFeignClient.getServiceStatus())
                .thenReturn("Okay!");
        String serviceStatus = sut.getServiceStatus();
        assertNotNull(serviceStatus);
        assertEquals(serviceStatus, "Okay!");
    }

}