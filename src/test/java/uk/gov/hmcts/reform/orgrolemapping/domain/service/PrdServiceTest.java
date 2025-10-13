package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import feign.FeignException;
import feign.Request;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUserResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class PrdServiceTest {

    PRDFeignClient prdFeignClient = mock(PRDFeignClient.class);

    PrdService sut = new PrdService(prdFeignClient);

    @Test
    void fetchOrganisationsByProfileIds() {
        OrganisationByProfileIdsResponse response = TestDataBuilder.buildOrganisationByProfileIdsResponse();
        OrganisationByProfileIdsRequest request = new OrganisationByProfileIdsRequest(List.of("SOLICITOR_PROFILE"));

        doReturn(ResponseEntity.status(HttpStatus.OK).body(response))
                .when(prdFeignClient).getOrganisationsByProfileIds(any(), eq(null), any());

        ResponseEntity<OrganisationByProfileIdsResponse> responseEntity =
                sut.fetchOrganisationsByProfileIds(1, null, request);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void fetchOrganisationsResponse() {
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

    @Test
    void fetchUsersByOrganisation() {
        UsersByOrganisationResponse response = TestDataBuilder.buildUsersByOrganisationResponse();
        UsersByOrganisationRequest request = new UsersByOrganisationRequest(List.of("1"));

        doReturn(ResponseEntity.status(HttpStatus.OK).body(response))
                .when(prdFeignClient).getUsersByOrganisation(any(), eq(null), eq(null), any());

        ResponseEntity<UsersByOrganisationResponse> responseEntity =
                sut.fetchUsersByOrganisation(1, null, null, request);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void fetchRetrieveUsers() {

        // GIVEN
        GetRefreshUserResponse response = TestDataBuilder.buildGetRefreshUsersResponse();

        doReturn(ResponseEntity.status(HttpStatus.OK).body(response))
                .when(prdFeignClient).getRefreshUsers(null, "2023-11-20T15:51:33.046Z", 1, null);

        // WHEN
        ResponseEntity<GetRefreshUserResponse> responseEntity =
                sut.retrieveUsers("2023-11-20T15:51:33.046Z", 1, null);

        // THEN
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
    }

    @Test
    void fetchRefreshUser() {

        // GIVEN
        String userId = "ID";
        doReturn(ResponseEntity.status(HttpStatus.OK).body(TestDataBuilder.buildGetRefreshUsersResponse(userId)))
                .when(prdFeignClient).getRefreshUsers(userId, null, null, null);

        // WHEN
        ResponseEntity<GetRefreshUserResponse> responseEntity = sut.getRefreshUser(userId);

        // THEN
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(userId, responseEntity.getBody().getUsers().get(0).getUserIdentifier());
    }

    @Test
    void createOrganisationTest() {
        doReturn(ResponseEntity.status(HttpStatus.OK).body("Organisation Created"))
                .when(prdFeignClient).createOrganisation();

        ResponseEntity<String> responseEntity = sut.createOrganisation();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Organisation Created", responseEntity.getBody());
    }

}
