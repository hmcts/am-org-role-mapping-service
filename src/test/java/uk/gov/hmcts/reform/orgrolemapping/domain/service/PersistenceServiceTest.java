package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.orgrolemapping.config.EnvironmentConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.data.FlagConfig;
import uk.gov.hmcts.reform.orgrolemapping.data.FlagConfigRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobsRepository;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PersistenceServiceTest {

    private final RefreshJobsRepository refreshJobsRepository = mock(RefreshJobsRepository.class);
    private final FlagConfigRepository flagConfigRepository = mock(FlagConfigRepository.class);
    private final EnvironmentConfiguration environmentConfiguration = mock(EnvironmentConfiguration.class);

    @InjectMocks
    private final PersistenceService sut = new PersistenceService(
            refreshJobsRepository, flagConfigRepository, environmentConfiguration
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

    @Test
    void getFlagStatusWhenStatusIsTrue() {

        // GIVEN
        String flagName = "iac_1_1";
        String env = "pr";
        when(flagConfigRepository.findByFlagNameAndEnv(flagName, env)).thenReturn(getFlagConfig(Boolean.TRUE));

        // WHEN
        boolean response = sut.getStatusByParam(flagName, env);

        // THEN
        assertTrue(response);

        // check environment config lookup is *NOT* used when environment is specified in call
        verify(environmentConfiguration, never()).getEnvironment();
    }

    @Test
    void getFlagStatusWhenStatusIsFalse() {

        // GIVEN
        String flagName = "iac_1_1";
        String env = "pr";
        when(flagConfigRepository.findByFlagNameAndEnv(flagName, env)).thenReturn(getFlagConfig(Boolean.FALSE));

        // WHEN
        boolean response = sut.getStatusByParam(flagName, env);

        // THEN
        assertFalse(response);

        // check environment config lookup is *NOT* used when environment is specified in call
        verify(environmentConfiguration, never()).getEnvironment();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void getFlagStatusWhenEnvIsEmpty(String env) {

        // GIVEN
        String flagName = "iac_1_1";
        String envConfig = "pr";
        when(environmentConfiguration.getEnvironment()).thenReturn(envConfig);
        // NB: environment configuration values is used not the env value passed into the call
        when(flagConfigRepository.findByFlagNameAndEnv(flagName, envConfig)).thenReturn(getFlagConfig(Boolean.TRUE));

        // WHEN
        boolean response = sut.getStatusByParam(flagName, env);

        // THEN
        assertTrue(response);

        // check environment config lookup is used when environment is *NOT* specified in call
        verify(environmentConfiguration, times(1)).getEnvironment();
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
                .flagName("iac_1_1")
                .serviceName("iac")
                .status(status)
                .build();
    }

}
