package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import feign.FeignException;
import feign.Request;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUsersResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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

    @Test
    void fetchOrganisationsByProfileIds() throws IOException {
        OrganisationByProfileIdsResponse response = TestDataBuilder.buildOrganisationByProfileIdsResponse();
        OrganisationByProfileIdsRequest request = new OrganisationByProfileIdsRequest(List.of("SOLICITOR_PROFILE"));

        doReturn(ResponseEntity.status(HttpStatus.OK).body(response))
                .when(prdFeignClient).getOrganisationsByProfileIds(any(), eq(null), any());

        ResponseEntity<OrganisationByProfileIdsResponse> responseEntity =
                sut.fetchOrganisationsByProfileIds(1, null, request);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void fetchOrganisationsResponse() throws IOException {
        OrganisationsResponse response = TestDataBuilder.buildOrganisationsResponse();

        doReturn(ResponseEntity.status(HttpStatus.OK).body(response))
                .when(prdFeignClient).retrieveOrganisations(null, "2023-11-20T15:51:33.046Z", null, 1, 100);

        ResponseEntity<OrganisationsResponse> responseEntity =
                sut.retrieveOrganisations("2023-11-20T15:51:33.046Z", 1, 100);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void fetchOrganisationsResponseWith404OrganisationNotFound() {
        Request request = Mockito.mock(Request.class);
        FeignException feignException = new FeignException.NotFound(null, request, null, null);
        Mockito.when(prdFeignClient.retrieveOrganisations(isNull(), anyString(), isNull(), anyInt(), anyInt()))
                .thenThrow(feignException);

        ResponseEntity<OrganisationsResponse> responseEntity =
                sut.retrieveOrganisations("2023-11-20T15:51:33.046Z", 1, 100);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void fetchOrganisationsResponseWithNot404() {
        Request request = Mockito.mock(Request.class);
        FeignException feignException = new FeignException.Forbidden(null, request, null, null);
        Mockito.when(prdFeignClient.retrieveOrganisations(isNull(), anyString(), isNull(), anyInt(), anyInt()))
                .thenThrow(feignException);

        Assert.assertThrows(FeignException.class, () ->
                sut.retrieveOrganisations("2023-11-20T15:51:33.046Z", 1, 100));
    }
}
