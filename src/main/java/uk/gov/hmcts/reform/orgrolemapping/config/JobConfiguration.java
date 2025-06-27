package uk.gov.hmcts.reform.orgrolemapping.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.UnprocessableEntityException;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshJobConfigService;

@Component
@Slf4j
public class JobConfiguration implements CommandLineRunner {

    static final String ERROR_ABORTED_JOB_IMPORT = "Aborted the job configuration import: {}";

    private final RefreshJobConfigService refreshJobConfigService;

    private final String jobDetail;
    private final boolean jobDetailAllowUpdate;


    @Autowired
    public JobConfiguration(RefreshJobConfigService refreshJobConfigService,
                            @Value("${refresh.job.update}") String jobDetail,
                            @Value("${refresh.Job.updateOverride}") Boolean jobDetailAllowUpdate) {
        this.refreshJobConfigService = refreshJobConfigService;
        this.jobDetail = jobDetail;
        this.jobDetailAllowUpdate = BooleanUtils.isTrue(jobDetailAllowUpdate);
    }

    @Override
    public void run(String... args) {

        if (StringUtils.isNotEmpty(jobDetail)) {
            try {
                this.refreshJobConfigService.processJobDetail(this.jobDetail, this.jobDetailAllowUpdate);
            } catch (UnprocessableEntityException ex) {
                log.error(ERROR_ABORTED_JOB_IMPORT, ex.getMessage(), ex);
            }
        } else {
            log.info("No Job Configuration to create");
        }
    }

}
