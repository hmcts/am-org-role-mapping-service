package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
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
    void fetchOrganisationStaleProfiles() throws IOException {
        OrganisationByProfileIdsResponse response = TestDataBuilder.buildOrganisationByProfileIdsResponse();
        OrganisationByProfileIdsRequest request = new OrganisationByProfileIdsRequest(List.of("SOLICITOR_PROFILE"));

        doReturn(ResponseEntity.status(HttpStatus.OK).body(response))
                .when(prdFeignClient).getOrganisationsByProfileIds(any(), eq(null), any());

        ResponseEntity<OrganisationByProfileIdsResponse> responseEntity =
                sut.fetchOrganisationsByProfileIds(1, null, request);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
