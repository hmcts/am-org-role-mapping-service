package uk.gov.hmcts.reform.orgrolemapping.domain.service;


import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JRDUserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfileV2;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JRDServiceTest {

    JRDFeignClient jrdFeignClient = mock(JRDFeignClient.class);

    JRDService sut = new JRDService(jrdFeignClient);

    @Test
    void fetchJudicialUserProfiles() throws IOException {
        JRDUserRequest userRequest = JRDUserRequest.builder()
                .sidamIds(Set.of("123e4567-e89b-42d3-a456-556642445000", "123e4567-e89b-42d3-a456-556642445111"))
                .build();

        JudicialProfile userProfile = TestDataBuilder.buildJudicialProfile();
        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(List.of(userProfile))).when(jrdFeignClient)
                .getJudicialDetailsById(userRequest, 10);

        ResponseEntity<List<JudicialProfile>> responseEntity = sut.fetchJudicialProfiles(userRequest);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    void fetchJudicialUserProfilesV2() throws IOException {

        // GIVEN
        JRDUserRequest userRequest = JRDUserRequest.builder()
                .sidamIds(Set.of("123e4567-e89b-42d3-a456-556642445000", "123e4567-e89b-42d3-a456-556642445111"))
                .build();

        JudicialProfileV2 userProfile = TestDataBuilder.buildJudicialProfileV2();
        doReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(userProfile))).when(jrdFeignClient)
                .getJudicialDetailsById(userRequest, 10);

        // WHEN
        ResponseEntity<List<JudicialProfileV2>> responseEntity = sut.fetchJudicialProfiles(userRequest);

        // THEN
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    }

    @Test
    void fetchJudicialDetailsByServiceName() throws IOException {

        // GIVEN
        String serviceName = "test_ServiceName";
        Integer pageSize = 123;
        Integer pageNumber = 4;
        String sortDirection = "test_SortDirection";
        String sortColumn = "test_SortColumn";

        JudicialProfileV2 userProfile = TestDataBuilder.buildJudicialProfileV2();
        doReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(userProfile)))
            .when(jrdFeignClient).getJudicialDetailsByServiceName(
                any(),
                eq(pageSize),
                eq(pageNumber),
                eq(sortDirection),
                eq(sortColumn)
            );

        // WHEN
        ResponseEntity<List<JudicialProfileV2>> responseEntity = sut.fetchJudicialDetailsByServiceName(
            serviceName,
            pageSize,
            pageNumber,
            sortDirection,
            sortColumn
        );

        // THEN
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ArgumentCaptor<JRDUserRequest> captorUserRequest = ArgumentCaptor.forClass(JRDUserRequest.class);
        verify(jrdFeignClient).getJudicialDetailsByServiceName(
                captorUserRequest.capture(),
                eq(pageSize),
                eq(pageNumber),
                eq(sortDirection),
                eq(sortColumn)
        );
        assertEquals(serviceName, captorUserRequest.getValue().getCcdServiceNames());

    }

}
