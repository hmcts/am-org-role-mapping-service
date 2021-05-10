package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobsRepository;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class PersistenceServiceTest {

    private final RefreshJobsRepository refreshJobsRepository = mock(RefreshJobsRepository.class);

    @InjectMocks
    private final PersistenceService sut = new PersistenceService(
            refreshJobsRepository
    );


    @Test
    void getActorCacheEntity() throws IOException {
        RefreshJobEntity refreshEntity = RefreshJobEntity.builder().jobId(1L).status("NEW").build();
        Mockito.when(refreshJobsRepository.findByRefreshJobStatus(Mockito.any()))
                .thenReturn(Arrays.asList(refreshEntity));
        List<RefreshJobEntity> response = sut.retrieveRefreshJobs("NEW");
        assertNotNull(response);
    }
}