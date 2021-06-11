package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobsRepository;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PersistenceServiceTest {

    private final RefreshJobsRepository refreshJobsRepository = mock(RefreshJobsRepository.class);

    @InjectMocks
    private final PersistenceService sut = new PersistenceService(
            refreshJobsRepository
    );


    @Test
    void getActorCacheEntity() {
        RefreshJobEntity refreshEntity = RefreshJobEntity.builder().jobId(1L).status("NEW").build();
        Mockito.when(refreshJobsRepository.findById(1L))
                .thenReturn(Optional.ofNullable(refreshEntity));
        Optional<RefreshJobEntity> response = sut.fetchRefreshJobById(1L);
        assertNotNull(response);
        assertTrue(response.isPresent());
        verify(refreshJobsRepository, Mockito.times(1)).findById(Mockito.any());
    }

    @Test
    void persistRefreshJobTest() {
        RefreshJobEntity refreshEntity = RefreshJobEntity.builder()
                .jobId(1L)
                .roleCategory("role")
                .jurisdiction("jurisdiction")
                .status("NEW")
                .created(ZonedDateTime.now()).build();
        Mockito.when(refreshJobsRepository.save(refreshEntity))
                .thenReturn(refreshEntity);
        assertNotNull(sut.persistRefreshJob(refreshEntity));
        verify(refreshJobsRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void deleteRefreshJobTest() {
        RefreshJobEntity refreshEntity = RefreshJobEntity.builder()
                .jobId(1L)
                .roleCategory("role")
                .jurisdiction("jurisdiction")
                .status("NEW")
                .created(ZonedDateTime.now()).build();
        sut.deleteRefreshJob(refreshEntity);
        verify(refreshJobsRepository, Mockito.times(1)).delete(Mockito.any());
    }
}