package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.orgrolemapping.data.FlagConfig;
import uk.gov.hmcts.reform.orgrolemapping.data.FlagConfigRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobsRepository;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class PersistenceServiceTest {

    private final RefreshJobsRepository refreshJobsRepository = mock(RefreshJobsRepository.class);
    private final FlagConfigRepository flagConfigRepository = mock(FlagConfigRepository.class);

    @InjectMocks
    private final PersistenceService sut = new PersistenceService(
            refreshJobsRepository, flagConfigRepository
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
                .created(ZonedDateTime.now()).build();;
        sut.deleteRefreshJob(refreshEntity);
        verify(refreshJobsRepository, Mockito.times(1)).delete(Mockito.any());
    }

    @Test
    void getFlagStatus() {
        String flagName = "iac_1_0";
        String env = "pr";
        when(flagConfigRepository.findByFlagNameAndEnv(flagName, env)).thenReturn(getFlagConfig(Boolean.TRUE));
        Boolean response = sut.getStatusByParam(flagName, env);
        assertTrue(response);

    }

    @Test
    void getFlagWhenStatusIsFalse() {
        String flagName = "iac_1_0";
        String env = "pr";
        when(flagConfigRepository.findByFlagNameAndEnv(flagName, env)).thenReturn(getFlagConfig(Boolean.FALSE));
        Boolean response = sut.getStatusByParam(flagName, env);
        assertFalse(response);

    }

    @Test
    void getFlagStatusWhenEnvIsEmpty() {
        String flagName = "iac_1_0";
        String env = "pr";
        when(flagConfigRepository.findByFlagNameAndEnv(any(), any())).thenReturn(getFlagConfig(Boolean.TRUE));
        Boolean response = sut.getStatusByParam(flagName, "");
        assertTrue(response);
    }

    @Test
    void persistFlagConfig() {
        FlagConfig flagConfig = getFlagConfig(Boolean.TRUE);
        when(flagConfigRepository.save(flagConfig)).thenReturn(flagConfig);
        FlagConfig flagConfigEntity = sut.persistFlagConfig(flagConfig);
        assertNotNull(flagConfigEntity);

    }

    private FlagConfig getFlagConfig(Boolean status) {
        return FlagConfig.builder()
                .env("pr")
                .flagName("iac_1_0")
                .serviceName("iac")
                .status(status)
                .build();
    }
}