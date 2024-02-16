package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class PrdServiceTest {

    PRDFeignClient prdFeignClient = mock(PRDFeignClient.class);

    PrdService sut = new PrdService(prdFeignClient);

    @Test
    void fetchUsersByOrganisation() throws IOException {
        UsersByOrganisationResponse response = TestDataBuilder.buildUsersByOrganisationResponse();
        UsersByOrganisationRequest request = new UsersByOrganisationRequest(List.of("1"));

        doReturn(ResponseEntity.status(HttpStatus.OK).body(response))
                .when(prdFeignClient).getUsersByOrganisation(any(), eq(null), eq(null), any());

        ResponseEntity<UsersByOrganisationResponse> responseEntity =
                sut.fetchUsersByOrganisation(1, null, null, request);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
