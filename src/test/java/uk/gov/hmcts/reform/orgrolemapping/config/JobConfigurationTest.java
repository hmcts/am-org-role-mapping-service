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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SuppressWarnings("UnnecessaryLocalVariable")
@ExtendWith(MockitoExtension.class)
class JobConfigurationTest {

    private static final String JOB_CONFIG = "LEGAL_OPERATIONS-PUBLICLAW-NEW-0-11";

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

    @ParameterizedTest
    @NullAndEmptySource
    void testRun_jobDetailEmptyOrNull(String jobDetail) {

        // GIVEN
        JobConfiguration jobConfigurationRunner = new JobConfiguration(refreshJobConfigService,
                jobDetail, false);

        // WHEN
        jobConfigurationRunner.run("input.txt");

        // THEN
        verify(refreshJobConfigService, never()).processJobDetail(any(), anyBoolean());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testRun_withJobDetails(boolean allowUpdate) {

        // GIVEN
        String jobDetail = JOB_CONFIG;
        JobConfiguration jobConfigurationRunner = new JobConfiguration(refreshJobConfigService,
                jobDetail, allowUpdate);

        // WHEN
        jobConfigurationRunner.run("input.txt");

        // THEN
        verify(refreshJobConfigService, atLeastOnce()).processJobDetail(jobDetail, allowUpdate);
    }

    @Test
    void testRun_withJobDetailsException() {

        // GIVEN
        String jobDetail = JOB_CONFIG;
        boolean allowUpdate = false;
        JobConfiguration jobConfigurationRunner = new JobConfiguration(refreshJobConfigService,
                jobDetail, allowUpdate);
        Mockito.doThrow(new UnprocessableEntityException("AN ERROR"))
                .when(refreshJobConfigService).processJobDetail(jobDetail, allowUpdate);

        // WHEN
        jobConfigurationRunner.run("input.txt");

        // THEN
        verify(refreshJobConfigService, atLeastOnce()).processJobDetail(jobDetail, allowUpdate);

        assertEquals(JobConfiguration.ERROR_ABORTED_JOB_IMPORT, logsList.get(0).getMessage());
    }

}
