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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
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

    @Test
    void shouldGetAllFeatureFlags() {

        // GIVEN
        List<FlagConfig> flagConfigs = List.of(
                getFlagConfig("pr", "iac_1_1", true),
                getFlagConfig("pr", "iac_1_2", false),
                getFlagConfig("pr", "iac_1_3", true),
                getFlagConfig("dev", "iac_1_1", true)
        );
        String env = flagConfigs.get(0).getEnv();
        when(environmentConfiguration.getEnvironment()).thenReturn(env);
        when(flagConfigRepository.findAll()).thenReturn(flagConfigs);

        // WHEN
        Map<String, Boolean> response = sut.getAllFeatureFlags();

        // THEN
        assertNotNull(response);
        assertEquals(3, response.size()); // 3 'pr' entries.
        flagConfigs.stream()
                .filter(flagConfig -> flagConfig.getEnv().equals(env))
                .forEach(flagConfig -> {
                    assertEquals(flagConfig.getStatus(), response.get(flagConfig.getFlagName()));
                });

        // check environment config lookup is used once
        verify(environmentConfiguration, times(1)).getEnvironment();
        // check flagConfig lookup is used once
        verify(flagConfigRepository, times(1)).findAll();
    }

    private FlagConfig getFlagConfig(Boolean status) {
        return getFlagConfig("pr", "iac_1_1", status);
    }

    private FlagConfig getFlagConfig(String env, String flagName, Boolean status) {
        return FlagConfig.builder()
                .env(env)
                .flagName(flagName)
                .serviceName("iac")
                .status(status)
                .build();
    }

}
