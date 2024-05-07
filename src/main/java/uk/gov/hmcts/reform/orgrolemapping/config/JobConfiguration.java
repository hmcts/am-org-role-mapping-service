package uk.gov.hmcts.reform.orgrolemapping.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobsRepository;
import uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureConditionEvaluator;

import java.time.ZonedDateTime;
import java.util.Optional;

@Component
@Slf4j
public class JobConfiguration implements CommandLineRunner {

    private final RefreshJobsRepository refreshJobsRepository;

    private final FeatureConditionEvaluator featureConditionEvaluator;

    private final String jobDetail;

    private static final String REFRESH_JOBS_CONFIG_SPLITTER = ":";


    @Autowired
    public JobConfiguration(RefreshJobsRepository refreshJobsRepository,
                            @Value("${refresh.job.update}") String jobDetail,
                            FeatureConditionEvaluator featureConditionEvaluator
                            ) {
        this.refreshJobsRepository = refreshJobsRepository;
        this.featureConditionEvaluator = featureConditionEvaluator;
        this.jobDetail = jobDetail;
    }

    @Override
    public void run(String... args) {
        if (StringUtils.isNotEmpty(jobDetail) && featureConditionEvaluator
                .isFlagEnabled("am_org_role_mapping_service", "orm-refresh-job-enable")) {
            // change to handle multiple services for refresh job for https://tools.hmcts.net/jira/browse/AM-2902
            String[] refreshJobsConfig = jobDetail.split(REFRESH_JOBS_CONFIG_SPLITTER);
            for (String refreshJobConfig:refreshJobsConfig) {
                String[] refreshJobAttributes = refreshJobConfig.split("-");
                log.info("Job {} inserting into refresh table", refreshJobConfig);
                if (refreshJobAttributes.length < 4) {
                    return;
                }
                RefreshJobEntity refreshJobEntity = RefreshJobEntity.builder().build();
                if (refreshJobAttributes.length > 4) {
                    Optional<RefreshJobEntity> refreshJob = refreshJobsRepository
                            .findById(Long.valueOf(refreshJobAttributes[4]));
                    refreshJobEntity = refreshJob.orElse(refreshJobEntity);
                }

                refreshJobEntity.setRoleCategory(refreshJobAttributes[0]);
                refreshJobEntity.setJurisdiction(refreshJobAttributes[1]);
                refreshJobEntity.setStatus(refreshJobAttributes[2]);
                refreshJobEntity.setLinkedJobId(Long.valueOf(refreshJobAttributes[3]));
                refreshJobEntity.setCreated(ZonedDateTime.now());

                persistJobDetail(refreshJobEntity);
            }

        } else {
            log.warn("LD flag 'orm-refresh-job-enable' is not enabled");
        }
    }

    private void persistJobDetail(RefreshJobEntity refreshJobEntity) {
        refreshJobsRepository.save(refreshJobEntity);
    }
}
