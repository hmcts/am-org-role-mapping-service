package uk.gov.hmcts.reform.orgrolemapping.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobsRepository;
import uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureConditionEvaluator;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class JobConfigurationTest {

    @Mock
    private FeatureConditionEvaluator featureConditionEvaluator;

    @Mock
    RefreshJobsRepository refreshJobsRepository;

    @Test
    public void testRun() {
       when(featureConditionEvaluator.isFlagEnabled(anyString(),anyString())).thenReturn(true);
       String jobDetail = "LEGAL_OPERATIONS-PUBLICLAW-NEW-0-11:LEGAL_OPERATIONS-CMC,UNSPEC-NEW-0-1";
        JobConfiguration jobConfigurationRunner = new JobConfiguration(refreshJobsRepository, jobDetail, featureConditionEvaluator);
        jobConfigurationRunner.run("input.txt");
    }
}
