package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_STCW;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_TCW;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildUserProfile;

class CRDServiceTest {


    CRDFeignClient crdFeignClient = mock(CRDFeignClient.class);

    CRDService sut = new CRDService(crdFeignClient);

    @Test
    void fetchUserProfiles() {
        UserRequest userRequest = UserRequest.builder()
                .userIds(Arrays.asList("123e4567-e89b-42d3-a456-556642445000", "123e4567-e89b-42d3-a456-556642445111"))
                .build();

        List<CaseWorkerProfile> userProfiles = TestDataBuilder.buildListOfUserProfiles(true, false,
                "1", "2",
                ROLE_NAME_STCW, ROLE_NAME_TCW, true, true, true,
                true, "1", "2", false);
        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(userProfiles)).when(crdFeignClient)
                .getCaseworkerDetailsById(userRequest);

        ResponseEntity<List<CaseWorkerProfile>> responseEntity = sut.fetchUserProfiles(userRequest);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    void fetchCaseWorkerProfileByServiceName() {

        UserRequest userRequest = UserRequest.builder()
                .userIds(Arrays.asList("123e4567-e89b-42d3-a456-556642445000", "123e4567-e89b-42d3-a456-556642445111"))
                .build();

        ResponseEntity<List<CaseWorkerProfilesResponse>> responseEntity = ResponseEntity.ok(Arrays
                .asList(CaseWorkerProfilesResponse.builder()
                        .serviceName("IAC").userProfile(buildUserProfile(
                                userRequest, "userProfileSample.json").get(0)).build()));

        doReturn(responseEntity).when(crdFeignClient)
               .getCaseworkerDetailsByServiceName(any(), any(), any(), any(), any());

        ResponseEntity<List<CaseWorkerProfilesResponse>> responseEntity1 = sut.fetchCaseworkerDetailsByServiceName(
                "IAC",
                2, 1, "ASC", "roleName");

        assertEquals(HttpStatus.OK, responseEntity1.getStatusCode());
    }
}
