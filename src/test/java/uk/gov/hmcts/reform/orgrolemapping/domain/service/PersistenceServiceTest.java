package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobsRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

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
        Mockito.when(refreshJobsRepository.findById(1L))
                .thenReturn(Optional.ofNullable(refreshEntity));
        Optional<RefreshJobEntity> response = sut.fetchRefreshJobById(1L);
        assertNotNull(response);
    }

//    @Test
//    void persistRefreshJobTest() {
//        RefreshJobEntity refreshEntity = RefreshJobEntity.builder()
//                .jobId(1L)
//                .roleCategory("role")
//                .jurisdiction("jurisdiction")
//                .status("NEW")
//                .created(LocalDateTime.now()).build();
//        Mockito.when(refreshJobsRepository.findById(1L))
//                .thenReturn(Optional.ofNullable(refreshEntity));
//        assert refreshEntity != null;
//        Mockito.when(refreshJobsRepository.save(refreshEntity))
//                .thenReturn(refreshEntity);
//        assertNotNull(refreshEntity);
//    }
}