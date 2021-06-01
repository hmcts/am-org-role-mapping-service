package uk.gov.hmcts.reform.orgrolemapping.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobsRepository;
import uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureConditionEvaluator;

import java.time.ZonedDateTime;

@Component
@Slf4j
public class JobConfiguration implements CommandLineRunner {


    private RefreshJobsRepository refreshJobsRepository;


    private FeatureConditionEvaluator featureConditionEvaluator;


    @Value("${refresh.job.update}")
    private String jobDetail;




    public JobConfiguration(RefreshJobsRepository refreshJobsRepository,
                            FeatureConditionEvaluator featureConditionEvaluator,
                            String jobDetail) {
        this.refreshJobsRepository = refreshJobsRepository;
        this.featureConditionEvaluator = featureConditionEvaluator;
        this.jobDetail = jobDetail;

    }


    @Override
    public void run(String... args) {
        if (StringUtils.isNotEmpty(jobDetail) && featureConditionEvaluator
                .isFlagEnabled("am_org_role_mapping_service",
                "orm-refresh-job-enable")) {
            String[] jobAttributes = jobDetail.split("-");

            RefreshJobEntity refreshJobEntity = RefreshJobEntity.builder()
                    .roleCategory(jobAttributes[0])
                    .jurisdiction(jobAttributes[1])
                    .status(jobAttributes[2])
                    .linkedJobId(Long.valueOf(jobAttributes[3]))
                    .created(ZonedDateTime.now())
                    .build();
            persistJobDetail(refreshJobEntity);
        }


    }

    private void persistJobDetail(RefreshJobEntity refreshJobEntity) {
        refreshJobsRepository.save(refreshJobEntity);

    }
}
