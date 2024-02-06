package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationStaleProfilesRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationStaleProfilesResponse;
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
        OrganisationStaleProfilesResponse response = TestDataBuilder.buildOrganisationStaleProfilesResponse();
        OrganisationStaleProfilesRequest request = new OrganisationStaleProfilesRequest(List.of("SOLICITOR_PROFILE"));

        doReturn(ResponseEntity.status(HttpStatus.OK).body(response))
                .when(prdFeignClient).getOrganisationStaleProfiles(any(), eq(null), any());

        ResponseEntity<OrganisationStaleProfilesResponse> responseEntity =
                sut.fetchOrganisationsWithStaleProfiles(1, null, request);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
