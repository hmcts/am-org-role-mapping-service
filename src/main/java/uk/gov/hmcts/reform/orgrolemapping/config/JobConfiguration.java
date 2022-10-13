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
            String[] jobAttributes = jobDetail.split("-");
            RefreshJobEntity refreshJobEntity = RefreshJobEntity.builder().build();
            log.info("Job {} inserting into refresh table", jobDetail);
            if (jobAttributes.length > 4) {
                Optional<RefreshJobEntity> refreshJob = refreshJobsRepository.findById(Long.valueOf(jobAttributes[4]));
                refreshJobEntity = refreshJob.orElse(refreshJobEntity);
            }

            refreshJobEntity.setRoleCategory(jobAttributes[0]);
            refreshJobEntity.setJurisdiction(jobAttributes[1]);
            refreshJobEntity.setStatus(jobAttributes[2]);
            refreshJobEntity.setLinkedJobId(Long.valueOf(jobAttributes[3]));
            refreshJobEntity.setCreated(ZonedDateTime.now());

            persistJobDetail(refreshJobEntity);
        } else {
            log.warn("LD flag 'orm-refresh-job-enable' is not enabled");
        }
    }

    private void persistJobDetail(RefreshJobEntity refreshJobEntity) {
        refreshJobsRepository.save(refreshJobEntity);
    }
}
