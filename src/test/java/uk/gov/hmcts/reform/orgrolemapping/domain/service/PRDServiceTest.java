package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUsersResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class PRDServiceTest {

    PRDFeignClient prdFeignClient = mock(PRDFeignClient.class);

    PRDService sut = new PRDService(prdFeignClient);

    @Test
    void getRefreshUser() throws IOException {
        doReturn(ResponseEntity.status(HttpStatus.OK).body(TestDataBuilder.buildRefreshUsersResponse("ID")))
            .when(prdFeignClient).getRefreshUsers(any());

        ResponseEntity<GetRefreshUsersResponse> responseEntity = sut.getRefreshUser("ID");

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
