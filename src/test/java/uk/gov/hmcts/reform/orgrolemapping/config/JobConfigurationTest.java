package uk.gov.hmcts.reform.orgrolemapping.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.UnprocessableEntityException;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshJobConfigService;
import uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureConditionEvaluator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("UnnecessaryLocalVariable")
@ExtendWith(MockitoExtension.class)
class JobConfigurationTest {

    private static final String JOB_CONFIG = "LEGAL_OPERATIONS-PUBLICLAW-NEW-0-11";

    @Mock(lenient = true)
    private FeatureConditionEvaluator featureConditionEvaluator;

    @Mock
    private RefreshJobConfigService refreshJobConfigService;

    Logger logger;
    ListAppender<ILoggingEvent> listAppender;
    List<ILoggingEvent> logsList;

    @BeforeEach
    public void setUp() {
        // attach appender to logger for assertions
        logger = (Logger) LoggerFactory.getLogger(JobConfiguration.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        logsList = listAppender.list;
    }

    @Test
    void testRun_featureFlagDisabled() {

        // GIVEN
        setUpFeatureFlag(false);
        String jobDetail = JOB_CONFIG;
        JobConfiguration jobConfigurationRunner = new JobConfiguration(refreshJobConfigService,
                jobDetail, false, featureConditionEvaluator);

        // WHEN
        jobConfigurationRunner.run("input.txt");

        // THEN
        verify(refreshJobConfigService, never()).processJobDetail(any(), anyBoolean());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testRun_jobDetailEmptyOrNull(String jobDetail) {

        // GIVEN
        setUpFeatureFlag(true);
        JobConfiguration jobConfigurationRunner = new JobConfiguration(refreshJobConfigService,
                jobDetail, false, featureConditionEvaluator);

        // WHEN
        jobConfigurationRunner.run("input.txt");

        // THEN
        verify(refreshJobConfigService, never()).processJobDetail(any(), anyBoolean());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testRun_withJobDetails(boolean allowUpdate) {

        // GIVEN
        setUpFeatureFlag(true);
        String jobDetail = JOB_CONFIG;
        JobConfiguration jobConfigurationRunner = new JobConfiguration(refreshJobConfigService,
                jobDetail, allowUpdate, featureConditionEvaluator);

        // WHEN
        jobConfigurationRunner.run("input.txt");

        // THEN
        verify(refreshJobConfigService, atLeastOnce()).processJobDetail(jobDetail, allowUpdate);
    }

    @Test
    void testRun_withJobDetailsException() {

        // GIVEN
        setUpFeatureFlag(true);
        String jobDetail = JOB_CONFIG;
        boolean allowUpdate = false;
        JobConfiguration jobConfigurationRunner = new JobConfiguration(refreshJobConfigService,
                jobDetail, allowUpdate, featureConditionEvaluator);
        Mockito.doThrow(new UnprocessableEntityException("AN ERROR"))
                .when(refreshJobConfigService).processJobDetail(jobDetail, allowUpdate);

        // WHEN
        jobConfigurationRunner.run("input.txt");

        // THEN
        verify(refreshJobConfigService, atLeastOnce()).processJobDetail(jobDetail, allowUpdate);

        assertEquals(JobConfiguration.ERROR_ABORTED_JOB_IMPORT, logsList.get(0).getMessage());
    }

    private void setUpFeatureFlag(boolean enabled) {
        when(featureConditionEvaluator.isFlagEnabled("am_org_role_mapping_service", "orm-refresh-job-enable"))
                .thenReturn(enabled);
    }

}
