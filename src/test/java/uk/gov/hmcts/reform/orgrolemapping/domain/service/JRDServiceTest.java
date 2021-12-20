package uk.gov.hmcts.reform.orgrolemapping.domain.service;


import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JRDUserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

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

}
