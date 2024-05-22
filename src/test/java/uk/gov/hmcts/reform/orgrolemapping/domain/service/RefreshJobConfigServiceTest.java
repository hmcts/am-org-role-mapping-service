package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.UnprocessableEntityException;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("UnnecessaryLocalVariable")
@ExtendWith(MockitoExtension.class)
class RefreshJobConfigServiceTest {

    @Mock
    private PersistenceService persistenceService;

    @InjectMocks
    private RefreshJobConfigService sut;

    private static final String JOB_CONFIG_11 = "LEGAL_OPERATIONS-PUBLICLAW-NEW-0-11";
    private static final String JOB_CONFIG_12 = "JUDICIAL-CIVIL-ABORTED-4-12";
    private static final String JOB_CONFIG_BAD = "BAD_JOB-INCORRECT_FORMAT-NOT_ENOUGH_PARTS";

    @ParameterizedTest
    @NullAndEmptySource
    void testProcessJobDetail_jobDetailEmptyOrNull(String jobDetail) {

        // WHEN
        sut.processJobDetail(jobDetail, false);

        // THEN
        verify(persistenceService, never()).persistRefreshJob(any());
    }

    @Test
    void testProcessJobDetail_singleJob_noJobId() {

        // GIVEN
        setUpSaveMock(5L); // i.e. any new job ID

        String jobDetail = "LEGAL_OPERATIONS-PUBLICLAW-NEW-0";

        // WHEN
        sut.processJobDetail(jobDetail, false);

        // THEN
        verify(persistenceService, never()).fetchRefreshJobById(anyLong());

        ArgumentCaptor<RefreshJobEntity> captor = ArgumentCaptor.forClass(RefreshJobEntity.class);
        verify(persistenceService, times(1)).persistRefreshJob(captor.capture());
        RefreshJobEntity job = captor.getValue();
        assertEquals("LEGAL_OPERATIONS", job.getRoleCategory());
        assertEquals("PUBLICLAW", job.getJurisdiction());
        assertEquals("NEW", job.getStatus());
        assertEquals(0, job.getLinkedJobId());
        assertNull(job.getJobId());
    }

    @Test
    void testProcessJobDetail_singleJob_withJobId_new() {

        // GIVEN
        when(persistenceService.fetchRefreshJobById(11L)).thenReturn(Optional.empty());
        setUpSaveMock(11L); // i.e. matching expected job ID

        String jobDetail = JOB_CONFIG_11;

        // WHEN
        sut.processJobDetail(jobDetail, false);

        // THEN
        ArgumentCaptor<RefreshJobEntity> captor = ArgumentCaptor.forClass(RefreshJobEntity.class);
        verify(persistenceService, times(1)).persistRefreshJob(captor.capture());
        assertJobEqualsJob11(captor.getValue(), true);
    }

    @Test
    void testProcessJobDetail_singleJob_withJobId_new_mismatched() {

        // GIVEN
        when(persistenceService.fetchRefreshJobById(11L)).thenReturn(Optional.empty());
        setUpSaveMock(6L); // i.e. any new job ID

        String jobDetail = JOB_CONFIG_11;

        // WHEN / THEN
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class, () ->
            sut.processJobDetail(jobDetail, false)
        );

        // THEN
        assertTrue(exception.getMessage().startsWith(RefreshJobConfigService.ERROR_REJECTED_JOB_ID_MISMATCH));
    }

    @Test
    void testProcessJobDetail_multipleJobs_withJobId_updateAllowed() {

        // GIVEN
        RefreshJobEntity job11 = RefreshJobEntity.builder().jobId(11L).build();
        RefreshJobEntity job12 = RefreshJobEntity.builder().jobId(12L).build();
        when(persistenceService.fetchRefreshJobById(11L)).thenReturn(Optional.ofNullable(job11));
        when(persistenceService.fetchRefreshJobById(12L)).thenReturn(Optional.ofNullable(job12));
        setUpSaveMock(job11);
        setUpSaveMock(job12);

        String jobDetail = createMultipleJobDetailsList(JOB_CONFIG_11, JOB_CONFIG_12);

        // WHEN
        sut.processJobDetail(jobDetail, true); // NB: allows update

        // THEN
        assert job11 != null;
        verify(persistenceService, times(1)).persistRefreshJob(job11);
        assertJobEqualsJob11(job11, false); // i.e. check updated values have been saved

        assert job12 != null;
        verify(persistenceService, times(1)).persistRefreshJob(job12);
        assertJobEqualsJob12(job12, false); // i.e. check updated values have been saved
    }

    @Test
    void testProcessJobDetail_multipleJobs_withJobId_noUpdateAllowed_includingSkipped() {

        // GIVEN
        RefreshJobEntity job11 = RefreshJobEntity.builder().jobId(11L).build();
        when(persistenceService.fetchRefreshJobById(11L)).thenReturn(Optional.ofNullable(job11));
        when(persistenceService.fetchRefreshJobById(12L)).thenReturn(Optional.empty()); // NB: i.e. not found
        setUpSaveMock(12L); // i.e. next JOB ID is as expected

        String jobDetail = createMultipleJobDetailsList(JOB_CONFIG_11, JOB_CONFIG_12);

        // WHEN
        sut.processJobDetail(jobDetail, false);

        // THEN
        // verify both job ID lookups ran
        verify(persistenceService, times(1)).fetchRefreshJobById(11L);
        verify(persistenceService, times(1)).fetchRefreshJobById(12L);

        // verify only 1 save
        ArgumentCaptor<RefreshJobEntity> captor = ArgumentCaptor.forClass(RefreshJobEntity.class);
        // NB: save will only be called once (i.e. job 12) as job 11 already exists and NO UPDATE ALLOWED specified
        verify(persistenceService, times(1)).persistRefreshJob(captor.capture());
        assertJobEqualsJob12(captor.getValue(), true);

        assert job11 != null;
        verify(persistenceService, never()).persistRefreshJob(job11); // never called as NO UPDATE ALLOWED specified
    }

    @Test
    void testProcessJobDetail_badJob_singleJob_tooFewParts() {

        // GIVEN
        String jobDetail = JOB_CONFIG_BAD;


        // WHEN / THEN
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class, () ->
            sut.processJobDetail(jobDetail, false)
        );

        // THEN
        assertTrue(exception.getMessage().startsWith(RefreshJobConfigService.ERROR_JOBS_DETAILS_TOO_FEW_PARTS));
        verify(persistenceService, never()).fetchRefreshJobById(anyLong());
        verify(persistenceService, never()).persistRefreshJob(any());
    }

    @Test
    void testProcessJobDetail_badJob_singleJob_tooManyParts() {

        // GIVEN
        String jobDetail = JOB_CONFIG_11 + RefreshJobConfigService.REFRESH_JOBS_CONFIG_SPLITTER + "extraArgument";

        // WHEN / THEN
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class, () ->
            sut.processJobDetail(jobDetail, false)
        );

        // THEN
        assertTrue(exception.getMessage().startsWith(RefreshJobConfigService.ERROR_JOBS_DETAILS_TOO_MANY_PARTS));
        verify(persistenceService, never()).fetchRefreshJobById(anyLong());
        verify(persistenceService, never()).persistRefreshJob(any());
    }

    @Test
    void testProcessJobDetail_badJob_singleJob_badLinkedJobId() {

        // GIVEN
        String jobDetail = "LEGAL_OPERATIONS-PUBLICLAW-NEW-nonNumeric-11";

        // WHEN / THEN
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class, () ->
            sut.processJobDetail(jobDetail, false)
        );

        // THEN
        assertTrue(exception.getMessage().startsWith(RefreshJobConfigService.ERROR_LINKED_JOB_ID_NON_NUMERIC));
        verify(persistenceService, never()).fetchRefreshJobById(anyLong());
        verify(persistenceService, never()).persistRefreshJob(any());
    }

    @Test
    void testProcessJobDetail_badJob_singleJob_badJobId() {

        // GIVEN
        String jobDetail = "LEGAL_OPERATIONS-PUBLICLAW-NEW-0-nonNumeric";

        // WHEN / THEN
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class, () ->
            sut.processJobDetail(jobDetail, false)
        );

        // THEN
        assertTrue(exception.getMessage().startsWith(RefreshJobConfigService.ERROR_JOB_ID_NON_NUMERIC));
        verify(persistenceService, never()).fetchRefreshJobById(anyLong());
        verify(persistenceService, never()).persistRefreshJob(any());
    }

    @Test
    void testProcessJobDetail_badJob_multipleJob_1BadSoProcessStops() {

        // GIVEN
        String jobDetail = createMultipleJobDetailsList(JOB_CONFIG_11, JOB_CONFIG_BAD, JOB_CONFIG_12);
        when(persistenceService.fetchRefreshJobById(11L)).thenReturn(Optional.empty()); // NB: i.e. not found
        setUpSaveMock(11L); // i.e. next JOB ID is as expected

        // WHEN / THEN
        assertThrows(UnprocessableEntityException.class, () ->
            sut.processJobDetail(jobDetail, false)
        );

        // THEN
        // NB: verify process has stopped: i.e. last job not processed as aborted before
        verify(persistenceService, never()).fetchRefreshJobById(12L);
    }

    private void assertJobEqualsJob11(RefreshJobEntity job, boolean newJob) {
        // assert entries match: JOB_CONFIG_11
        assertEquals("LEGAL_OPERATIONS", job.getRoleCategory());
        assertEquals("PUBLICLAW", job.getJurisdiction());
        assertEquals("NEW", job.getStatus());
        assertEquals(0, job.getLinkedJobId());
        if (!newJob) {
            assertEquals(11L, job.getJobId());
        } else {
            assertNull(job.getJobId());
        }
    }

    private void assertJobEqualsJob12(RefreshJobEntity job, boolean newJob) {
        // assert entries match: JOB_CONFIG_12
        assertEquals("JUDICIAL", job.getRoleCategory());
        assertEquals("CIVIL", job.getJurisdiction());
        assertEquals("ABORTED", job.getStatus());
        assertEquals(4, job.getLinkedJobId());
        if (!newJob) {
            assertEquals(12L, job.getJobId());
        } else {
            assertNull(job.getJobId());
        }
    }

    private String createMultipleJobDetailsList(String... jobConfigs) {
        return String.join(RefreshJobConfigService.REFRESH_JOBS_DETAILS_SPLITTER, jobConfigs);
    }

    private void setUpSaveMock(RefreshJobEntity job) {
        // save existing job: so just echo it back
        setUpSaveMock(job, false, job.getJobId());
    }

    private void setUpSaveMock(Long nextJobId) {
        // NB: new job test
        setUpSaveMock(any(RefreshJobEntity.class), true, nextJobId);
    }

    private void setUpSaveMock(RefreshJobEntity job, boolean newJob, Long nextJobId) {
        RefreshJobEntity savedJob = job;
        if (newJob) {
            // return a new job with new ID
            savedJob = RefreshJobEntity.builder().jobId(nextJobId).build();
        }
        when(persistenceService.persistRefreshJob(job)).thenReturn(savedJob);
    }

}
