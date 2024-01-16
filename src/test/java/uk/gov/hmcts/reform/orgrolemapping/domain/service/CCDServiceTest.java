package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypesResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CCDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class CCDServiceTest {

    CCDFeignClient ccdFeignClient = mock(CCDFeignClient.class);

    CCDService sut = new CCDService(ccdFeignClient);

    @Test
    void fetchAccessTypes() throws IOException {
        AccessTypesResponse accessTypes = TestDataBuilder.buildAccessTypesResponse();
        doReturn(ResponseEntity.status(HttpStatus.OK).body(accessTypes)).when(ccdFeignClient).getAccessTypes();

        ResponseEntity<AccessTypesResponse> responseEntity = sut.fetchAccessTypes();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
