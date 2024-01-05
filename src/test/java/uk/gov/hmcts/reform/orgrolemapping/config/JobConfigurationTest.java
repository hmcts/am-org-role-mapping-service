package uk.gov.hmcts.reform.orgrolemapping.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobsRepository;
import uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureConditionEvaluator;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void testRun_featureFlagDisabled() {

        // GIVEN
        setUpFeatureFlag(false);
        String jobDetail = "LEGAL_OPERATIONS-PUBLICLAW-NEW-0-11";
        JobConfiguration jobConfigurationRunner = new JobConfiguration(refreshJobsRepository,
                jobDetail, featureConditionEvaluator);

        // WHEN
        jobConfigurationRunner.run("input.txt");

        // THEN
        verify(refreshJobsRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testRun_jobDetailEmptyOrNull(String jobDetail) {

        // GIVEN
        setUpFeatureFlag(true);
        JobConfiguration jobConfigurationRunner = new JobConfiguration(refreshJobsRepository,
                jobDetail, featureConditionEvaluator);

        // WHEN
        jobConfigurationRunner.run("input.txt");

        // THEN
        verify(refreshJobsRepository, never()).save(any());
    }

    @Test
    void testRun_multipleJobs() {

        // GIVEN
        setUpFeatureFlag(true);
        String jobDetail = "LEGAL_OPERATIONS-PUBLICLAW-NEW-0-11:JUDICIAL-CMC,UNSPEC-ABORTED-0-12";
        JobConfiguration jobConfigurationRunner = new JobConfiguration(refreshJobsRepository,
                jobDetail, featureConditionEvaluator);
        RefreshJobEntity job11 = RefreshJobEntity.builder().jobId(11L).build();
        RefreshJobEntity job12 = RefreshJobEntity.builder().jobId(12L).build();
        when(refreshJobsRepository.findById(11L)).thenReturn(Optional.ofNullable(job11));
        when(refreshJobsRepository.findById(12L)).thenReturn(Optional.ofNullable(job12));

        // WHEN
        jobConfigurationRunner.run("input.txt");

        // THEN
        assert job11 != null;
        verify(refreshJobsRepository, times(1)).save(job11);
        assertEquals("LEGAL_OPERATIONS", job11.getRoleCategory());
        assertEquals("PUBLICLAW", job11.getJurisdiction());
        assertEquals("NEW", job11.getStatus());
        assertEquals(0, job11.getLinkedJobId());
        assertEquals(11L, job11.getJobId());

        assert job12 != null;
        verify(refreshJobsRepository, times(1)).save(job12);
        assertEquals("JUDICIAL", job12.getRoleCategory());
        assertEquals("CMC,UNSPEC", job12.getJurisdiction());
        assertEquals("ABORTED", job12.getStatus());
        assertEquals(0, job12.getLinkedJobId());
        assertEquals(12L, job12.getJobId());
    }

    @Test
    void testRun_singleJob_noExistingId() {

        // GIVEN
        setUpFeatureFlag(true);
        String jobDetail = "LEGAL_OPERATIONS-PUBLICLAW-NEW-0";
        JobConfiguration jobConfigurationRunner = new JobConfiguration(refreshJobsRepository,
                jobDetail, featureConditionEvaluator);

        // WHEN
        jobConfigurationRunner.run("input.txt");

        // THEN
        verify(refreshJobsRepository, never()).findById(anyLong());

        ArgumentCaptor<RefreshJobEntity> captor = ArgumentCaptor.forClass(RefreshJobEntity.class);
        verify(refreshJobsRepository, times(1)).save(captor.capture());
        RefreshJobEntity job = captor.getValue();
        assertEquals("LEGAL_OPERATIONS", job.getRoleCategory());
        assertEquals("PUBLICLAW", job.getJurisdiction());
        assertEquals("NEW", job.getStatus());
        assertEquals(0, job.getLinkedJobId());
        assertNull(job.getJobId());
    }

    @Test
    void testRun_badJob() {

        // GIVEN
        setUpFeatureFlag(true);
        String jobDetail = "BAD_JOB-INCORRECT_FORMAT-NOT_ENOUGH_PARTS";
        JobConfiguration jobConfigurationRunner = new JobConfiguration(refreshJobsRepository,
                jobDetail, featureConditionEvaluator);

        // WHEN
        jobConfigurationRunner.run("input.txt");

        // THEN
        verify(refreshJobsRepository, never()).findById(anyLong());
        verify(refreshJobsRepository, never()).save(any());
    }

    private void setUpFeatureFlag(boolean enabled) {
        when(featureConditionEvaluator.isFlagEnabled("am_org_role_mapping_service", "orm-refresh-job-enable"))
                .thenReturn(enabled);
    }

}
