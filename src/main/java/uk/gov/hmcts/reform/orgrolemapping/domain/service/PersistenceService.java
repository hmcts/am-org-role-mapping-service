package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.config.EnvironmentConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.data.FlagConfig;
import uk.gov.hmcts.reform.orgrolemapping.data.FlagConfigRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobsRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class PersistenceService {


    private final RefreshJobsRepository refreshJobsRepository;
    private final FlagConfigRepository flagConfigRepository;
    private final EnvironmentConfiguration environmentConfiguration;

    public PersistenceService(RefreshJobsRepository refreshJobsRepository,
                              FlagConfigRepository flagConfigRepository,
                              EnvironmentConfiguration environmentConfiguration) {
        this.refreshJobsRepository = refreshJobsRepository;
        this.flagConfigRepository = flagConfigRepository;
        this.environmentConfiguration = environmentConfiguration;
    }


    public Optional<RefreshJobEntity> fetchRefreshJobById(Long jobId) {
        return refreshJobsRepository.findById(jobId);
    }

    public RefreshJobEntity persistRefreshJob(RefreshJobEntity refreshJobEntity) {
        return refreshJobsRepository.save(refreshJobEntity);
    }

    public void deleteRefreshJob(RefreshJobEntity refreshJobEntity) {
        refreshJobsRepository.delete(refreshJobEntity);
    }

    public boolean getStatusByParam(String flagName, String envName) {
        return flagConfigRepository.findByFlagNameAndEnv(flagName, getEnvironment(envName)).getStatus();
    }

    private String getEnvironment(String envName) {
        if (StringUtils.isEmpty(envName)) {
            return environmentConfiguration.getEnvironment();
        }
        return envName;
    }

    public Map<String, Boolean> getAllFeatureFlags(String envName) {
        // Get the feature flag configs
        List<FlagConfig> featureFlags = new ArrayList<>();
        flagConfigRepository.findAll().forEach(featureFlags::add);

        // Build the map from the feature flag configs
        Map<String, Boolean> map = new LinkedMap<>();
        featureFlags.stream()
                .sorted(Comparator.comparing(flagConfig -> flagConfig.getFlagName()))
                .forEach(flagConfig ->
                map.computeIfAbsent(
                        flagConfig.getFlagName(),
                        k -> getStatusByParam(k, envName)
        ));
        return map;
    }

    public FlagConfig persistFlagConfig(FlagConfig flagConfig) {
        return flagConfigRepository.save(flagConfig);

    }

}
