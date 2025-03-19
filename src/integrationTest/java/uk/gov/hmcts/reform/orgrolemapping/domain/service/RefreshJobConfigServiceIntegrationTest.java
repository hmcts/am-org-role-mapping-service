package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.UnprocessableEntityException;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("UnnecessaryLocalVariable")
class RefreshJobConfigServiceIntegrationTest extends BaseTestIntegration {

    private static final String JOB_CONFIG_1 = "LEGAL_OPERATIONS-PUBLICLAW-NEW-0";
    private static final String JOB_CONFIG_2 = "JUDICIAL-CIVIL-ABORTED-4";
    private static final String JOB_CONFIG_BAD = "BAD_JOB-INCORRECT_FORMAT-NOT_ENOUGH_PARTS";

    private static final Long NEXT_JOB_ID = 5L; // NB: this is next sequence number in `sql/insert_refresh_jobs.sql`

    @Autowired
    private PersistenceService persistenceService;

    @Autowired
    private RefreshJobConfigService sut;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    void testProcessJobDetail_singleJob_noJobId() {

        // GIVEN
        String jobDetail = JOB_CONFIG_1;

        // WHEN
        sut.processJobDetail(jobDetail, false);

        // THEN
        var jobOutput = persistenceService.fetchRefreshJobById(NEXT_JOB_ID);
        assertTrue(jobOutput.isPresent());
        assertJobEqualsJobConfig1(jobOutput.get());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    void testProcessJobDetail_singleJob_withJobId_new() {

        // GIVEN
        String jobDetail = createJobConfigWithId(JOB_CONFIG_1, NEXT_JOB_ID);

        // WHEN
        sut.processJobDetail(jobDetail, false);

        // THEN
        var jobOutput = persistenceService.fetchRefreshJobById(NEXT_JOB_ID);
        assertTrue(jobOutput.isPresent());
        assertEquals(NEXT_JOB_ID, jobOutput.get().getJobId());
        assertJobEqualsJobConfig1(jobOutput.get());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    void testProcessJobDetail_singleJob_withJobId_new_mismatched() {

        // GIVEN
        String jobDetail = createJobConfigWithId(JOB_CONFIG_1, NEXT_JOB_ID + 1);

        // WHEN / THEN
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class, () ->
            sut.processJobDetail(jobDetail, false)
        );

        // THEN
        assertTrue(exception.getMessage().startsWith(RefreshJobConfigService.ERROR_REJECTED_JOB_ID_MISMATCH));
        // verify new job config entry not created
        assertTrue(persistenceService.fetchRefreshJobById(NEXT_JOB_ID).isEmpty()); // i.e. mismatched removed
        assertTrue(persistenceService.fetchRefreshJobById(NEXT_JOB_ID + 1).isEmpty()); // requested not created
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    void testProcessJobDetail_multipleJobs_withJobId_updateAllowed() {

        // GIVEN
        String jobDetail = createMultipleJobDetailsList(
            createJobConfigWithId(JOB_CONFIG_1, 1L), // this is an update 1 < NEXT_JOB_ID
            createJobConfigWithId(JOB_CONFIG_2, 2L) // this is an update 2 < NEXT_JOB_ID
        );

        // WHEN
        sut.processJobDetail(jobDetail, true); // NB: allows update

        // THEN
        // verify updated job is saved OK
        var jobOutput1 = persistenceService.fetchRefreshJobById(1L);
        assertTrue(jobOutput1.isPresent());
        assertEquals(1L, jobOutput1.get().getJobId());
        assertJobEqualsJobConfig1(jobOutput1.get());

        // verify updated job is saved OK
        var jobOutput2 = persistenceService.fetchRefreshJobById(2L);
        assertTrue(jobOutput2.isPresent());
        assertEquals(2L, jobOutput2.get().getJobId());
        assertJobEqualsJobConfig2(jobOutput2.get());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    void testProcessJobDetail_multipleJobs_withJobId_noUpdateAllowed_includingSkipped() {

        // GIVEN
        String jobDetail = createMultipleJobDetailsList(
            createJobConfigWithId(JOB_CONFIG_1, 4L), // this update will be skipped
            createJobConfigWithId(JOB_CONFIG_2, NEXT_JOB_ID) // this new job will be saved
        );

        // WHEN
        sut.processJobDetail(jobDetail, false);

        // THEN
        // verify update not applied
        var jobOutput1 = persistenceService.fetchRefreshJobById(4L);
        assertTrue(jobOutput1.isPresent());
        assertJobEqualsJob4Unchanged(jobOutput1.get());

        // verify new job is saved OK
        var jobOutput2 = persistenceService.fetchRefreshJobById(NEXT_JOB_ID);
        assertTrue(jobOutput2.isPresent());
        assertEquals(NEXT_JOB_ID, jobOutput2.get().getJobId());
        assertJobEqualsJobConfig2(jobOutput2.get());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_refresh_jobs.sql"})
    void testProcessJobDetail_multipleJobs_badJob_noSaveOrUpdate() {

        // GIVEN
        String jobDetail = createMultipleJobDetailsList(
                createJobConfigWithId(JOB_CONFIG_1, 4L), // this is an update
                createJobConfigWithId(JOB_CONFIG_2, NEXT_JOB_ID), // this is a new job
                JOB_CONFIG_BAD
        );

        // WHEN / THEN
        assertThrows(UnprocessableEntityException.class, () ->
            sut.processJobDetail(jobDetail, true) // NB: allows update
        );

        // THEN
        // verify update has been rolled back
        var jobOutput1 = persistenceService.fetchRefreshJobById(4L);
        assertTrue(jobOutput1.isPresent());
        assertJobEqualsJob4Unchanged(jobOutput1.get());

        // verify new job has been rolled back
        assertTrue(persistenceService.fetchRefreshJobById(NEXT_JOB_ID).isEmpty());
    }

    private void assertJobEqualsJobConfig1(RefreshJobEntity job) {
        // assert entries match: JOB_CONFIG_1
        assertEquals("LEGAL_OPERATIONS", job.getRoleCategory());
        assertEquals("PUBLICLAW", job.getJurisdiction());
        assertEquals("NEW", job.getStatus());
        assertEquals(0, job.getLinkedJobId());
    }

    private void assertJobEqualsJobConfig2(RefreshJobEntity job) {
        // assert entries match: JOB_CONFIG_2
        assertEquals("JUDICIAL", job.getRoleCategory());
        assertEquals("CIVIL", job.getJurisdiction());
        assertEquals("ABORTED", job.getStatus());
        assertEquals(4, job.getLinkedJobId());
    }

    private void assertJobEqualsJob4Unchanged(RefreshJobEntity job) {
        // verify update not applied: i.e. must match jobId=4 in `sql/insert_refresh_jobs.sql`
        assertEquals(4L, job.getJobId());
        assertEquals("LEGAL_OPERATIONS", job.getRoleCategory());
        assertEquals("IA", job.getJurisdiction());
        assertEquals("COMPLETED", job.getStatus());
        assertEquals(2, job.getLinkedJobId());
    }

    private String createJobConfigWithId(String jobConfig, Long jobId) {
        return jobConfig + RefreshJobConfigService.REFRESH_JOBS_CONFIG_SPLITTER + jobId;
    }

    private String createMultipleJobDetailsList(String... jobConfigs) {
        return String.join(RefreshJobConfigService.REFRESH_JOBS_DETAILS_SPLITTER, jobConfigs);
    }

}