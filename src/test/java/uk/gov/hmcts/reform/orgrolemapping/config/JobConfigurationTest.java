package uk.gov.hmcts.reform.orgrolemapping.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobsRepository;
import uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureConditionEvaluator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.anyLong;

class JobConfigurationTest {

    @Mock
    private FeatureConditionEvaluator featureConditionEvaluator;

    @Mock
    RefreshJobsRepository refreshJobsRepository;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRun() {
        when(featureConditionEvaluator.isFlagEnabled(anyString(),anyString())).thenReturn(true);
        String jobDetail = "LEGAL_OPERATIONS-PUBLICLAW-NEW-0-11:LEGAL_OPERATIONS-CMC,UNSPEC-NEW-0-12";
        JobConfiguration jobConfigurationRunner = new JobConfiguration(refreshJobsRepository,
                jobDetail, featureConditionEvaluator);
        jobConfigurationRunner.run("input.txt");
        verify(refreshJobsRepository, times(2))
                .findById(anyLong());
        verify(refreshJobsRepository,times(2))
                .save(any());
    }
}
