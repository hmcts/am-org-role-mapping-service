package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.UnprocessableEntityException;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class RefreshJobConfigService {

    static final String REFRESH_JOBS_DETAILS_SPLITTER = ":"; // splitting multiple job configs from one job detail
    static final String REFRESH_JOBS_CONFIG_SPLITTER = "-"; // splitting individual job config into attributes

    private static final String ERROR_MESSAGE = "%s: '%s'";
    static final String ERROR_JOBS_DETAILS_TOO_FEW_PARTS = "Job config does not have enough arguments";
    static final String ERROR_JOBS_DETAILS_TOO_MANY_PARTS = "Job config has too many arguments";
    static final String ERROR_JOB_ID_NON_NUMERIC = "Job ID is non-numeric";
    static final String ERROR_LINKED_JOB_ID_NON_NUMERIC = "Linked Job ID is non-numeric";
    static final String ERROR_REJECTED_JOB_ID_MISMATCH = "Job ID does not match requested value";

    private static final int ARGS_INDEX_0_ROLE_CATEGORY = 0;
    private static final int ARGS_INDEX_1_JURISDICTION = 1;
    private static final int ARGS_INDEX_2_STATUS = 2;
    private static final int ARGS_INDEX_3_LINKED_JOB_ID = 3;
    private static final int ARGS_INDEX_4_JOB_ID = 4;

    private final PersistenceService persistenceService;

    public RefreshJobConfigService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processJobDetail(String jobDetail, boolean allowUpdate) {

        if (StringUtils.isNotEmpty(jobDetail)) {

            String[] refreshJobsConfig = jobDetail.split(REFRESH_JOBS_DETAILS_SPLITTER);
            for (String refreshJobConfig:refreshJobsConfig) {
                String[] refreshJobAttributes = splitJobConfigInToArguments(refreshJobConfig);
                processJobConfig(refreshJobConfig, refreshJobAttributes, allowUpdate);
            }
        }
    }

    private void processJobConfig(String refreshJobConfig,
                                  String[] refreshJobAttributes,
                                  boolean allowUpdate) {

        RefreshJobEntity refreshJobEntity = RefreshJobEntity.builder()
            .created(ZonedDateTime.now()) // set created only if new job
            .build();

        Long useJobId = null;

        if (refreshJobAttributes.length > 4) {
            useJobId = Long.valueOf(refreshJobAttributes[ARGS_INDEX_4_JOB_ID]);
            Optional<RefreshJobEntity> refreshJob = persistenceService.fetchRefreshJobById(useJobId);

            if (refreshJob.isPresent()) {
                if (allowUpdate) {
                    log.warn("UPDATING Job {} as JOB_ID={} already present in database.", refreshJobConfig, useJobId);
                    refreshJobEntity = refreshJob.get(); // switch to using existing job
                } else {
                    // NB: default config will not allow update of existing job entities
                    log.warn("SKIPPING Job {} as JOB_ID={} already present in database.", refreshJobConfig, useJobId);
                    return; // i.e. abort the processing of this config
                }
            }
        }

        if (refreshJobEntity.getJobId() == null) {
            log.info("INSERTING Job {} into refresh table", refreshJobConfig);
        }

        // NB: we do not set jobId as for new records it will always match the next sequence number.
        refreshJobEntity.setRoleCategory(refreshJobAttributes[ARGS_INDEX_0_ROLE_CATEGORY]);
        refreshJobEntity.setJurisdiction(refreshJobAttributes[ARGS_INDEX_1_JURISDICTION]);
        refreshJobEntity.setStatus(refreshJobAttributes[ARGS_INDEX_2_STATUS]);
        refreshJobEntity.setLinkedJobId(Long.valueOf(refreshJobAttributes[ARGS_INDEX_3_LINKED_JOB_ID]));

        RefreshJobEntity savedRefreshJob = persistenceService.persistRefreshJob(refreshJobEntity);

        // reject if job ID after save doesn't match the expected value
        if (useJobId != null && !Objects.equals(useJobId, savedRefreshJob.getJobId())) {
            throw new UnprocessableEntityException(
                String.format("%s: %s != %s", ERROR_REJECTED_JOB_ID_MISMATCH, useJobId, savedRefreshJob.getJobId())
            );
        }
    }

    private String[] splitJobConfigInToArguments(String refreshJobConfig) {

        String[] refreshJobAttributes = refreshJobConfig.split(REFRESH_JOBS_CONFIG_SPLITTER);

        if (refreshJobAttributes.length < 4) {
            throw new UnprocessableEntityException(
                String.format(ERROR_MESSAGE, ERROR_JOBS_DETAILS_TOO_FEW_PARTS, refreshJobConfig)
            );
        } else if (refreshJobAttributes.length > 5) {
            throw new UnprocessableEntityException(
                String.format(ERROR_MESSAGE, ERROR_JOBS_DETAILS_TOO_MANY_PARTS, refreshJobConfig)
            );
        } else {

            // validate Job-ID is numeric
            if (refreshJobAttributes.length == 5
                    && (!NumberUtils.isNumber(refreshJobAttributes[ARGS_INDEX_4_JOB_ID]))) {
                throw new UnprocessableEntityException(
                    String.format(
                        ERROR_MESSAGE,
                        ERROR_JOB_ID_NON_NUMERIC,
                        refreshJobAttributes[ARGS_INDEX_4_JOB_ID]
                    )
                );
            }

            // validate Linked Job-ID is numeric
            if (!NumberUtils.isNumber(refreshJobAttributes[ARGS_INDEX_3_LINKED_JOB_ID])) {
                throw new UnprocessableEntityException(
                    String.format(
                        ERROR_MESSAGE,
                        ERROR_LINKED_JOB_ID_NON_NUMERIC,
                        refreshJobAttributes[ARGS_INDEX_3_LINKED_JOB_ID]
                    )
                );
            }
        }

        return refreshJobAttributes;
    }

}
