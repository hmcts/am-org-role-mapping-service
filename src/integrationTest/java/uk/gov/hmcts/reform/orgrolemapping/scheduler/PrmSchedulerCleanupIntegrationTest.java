package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PrmSchedulerCleanupIntegrationTest extends BaseTestIntegration {

    @Autowired
    private UserRefreshQueueRepository userRefreshQueueRepository;

    @Autowired
    private OrganisationRefreshQueueRepository organisationRefreshQueueRepository;

    @Autowired
    private Scheduler scheduler;

    //# UserRefreshQueue cleanup tests.

    /*
     * User - NO records. Nothing to delete.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql"})
    void noRecordUserTest() {
        assertUserCleanup(scheduler.deleteInactiveUserRefreshRecords(),
                0, EndStatus.SUCCESS);
    }

    /*
     * User - NEW INACTIVE record should not be deleted.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
            "classpath:sql/prm/cleanup/insert_new_user_refresh_queue.sql"})
    void newInactiveUserTest() {
       assertUserCleanup(scheduler.deleteInactiveUserRefreshRecords(),
               1, EndStatus.SUCCESS);
    }

    /*
     * User - OLD ACTIVE record should not be deleted.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
            "classpath:sql/prm/cleanup/insert_old_active_user_refresh_queue.sql"})
    void oldActiveUserTest() {
        assertUserCleanup(scheduler.deleteInactiveUserRefreshRecords(),
                1, EndStatus.SUCCESS);
    }

    /*
     * User - NEW ACTIVE record should not be deleted.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
            "classpath:sql/prm/cleanup/insert_new_active_user_refresh_queue.sql"})
    void newActiveUserTest() {
        assertUserCleanup(scheduler.deleteInactiveUserRefreshRecords(),
                1, EndStatus.SUCCESS);
    }

    /*
     * User - OLD INACTIVE record should be deleted.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
            "classpath:sql/prm/cleanup/insert_old_user_refresh_queue.sql"})
    void oldInactiveUserTest() {
        assertUserCleanup(scheduler.deleteInactiveUserRefreshRecords(),
                0, EndStatus.SUCCESS);
    }

    /*
     * User - Multiple records 1 deleted, 1 not deleted.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
            "classpath:sql/prm/cleanup/insert_old_user_refresh_queue.sql",
            "classpath:sql/prm/cleanup/insert_new_active_user_refresh_queue.sql"})
    void multipleRecordsUserTest() {
        assertUserCleanup(scheduler.deleteInactiveUserRefreshRecords(),
                1, EndStatus.SUCCESS);
    }

    //# OrganisationRefreshQueue cleanup tests.

    /*
     * Organisation - NO records. Nothing to delete.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql"})
    void noRecordOrganisationTest() {
        assertOrganisationCleanup(scheduler.deleteInactiveOrganisationRefreshRecords(),
                0, EndStatus.SUCCESS);
    }

    /*
     * Organisation - NEW INACTIVE record should not be deleted.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
            "classpath:sql/prm/cleanup/insert_new_organisation_profiles.sql"})
    void newInactiveOrganisationTest() {
        assertOrganisationCleanup(scheduler.deleteInactiveOrganisationRefreshRecords(),
                1, EndStatus.SUCCESS);
    }

    /*
     * Organisation - OLD ACTIVE record should not be deleted.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
            "classpath:sql/prm/cleanup/insert_old_active_organisation_profiles.sql"})
    void oldActiveOrganisationTest() {
        assertOrganisationCleanup(scheduler.deleteInactiveOrganisationRefreshRecords(),
                1, EndStatus.SUCCESS);
    }

    /*
     * Organisation - NEW ACTIVE record should not be deleted.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
            "classpath:sql/prm/cleanup/insert_new_active_organisation_profiles.sql"})
    void newActiveOrganisationTest() {
        assertOrganisationCleanup(scheduler.deleteInactiveOrganisationRefreshRecords(),
                1, EndStatus.SUCCESS);
    }

    /*
     * Organisation - OLD INACTIVE record should be deleted.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
            "classpath:sql/prm/cleanup/insert_old_organisation_profiles.sql"})
    void oldInactiveOrganisationTest() {
        assertOrganisationCleanup(scheduler.deleteInactiveOrganisationRefreshRecords(),
                0, EndStatus.SUCCESS);
    }

    /*
     * Organisation - Multiple records 1 deleted, 1 not deleted.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql",
            "classpath:sql/prm/cleanup/insert_old_organisation_profiles.sql",
            "classpath:sql/prm/cleanup/insert_new_active_organisation_profiles.sql"})
    void multipleRecordsOrganisationTest() {
        assertOrganisationCleanup(scheduler.deleteInactiveOrganisationRefreshRecords(),
                1, EndStatus.SUCCESS);
    }

    //# Validation methods.

    private void assertUserCleanup(ProcessMonitorDto processMonitorDto,
                                   int expectedRecords, EndStatus endStatus) {
        assertProcessMonitor(processMonitorDto, endStatus);
        assertEquals(expectedRecords, userRefreshQueueRepository.findAll().size());
    }

    private void assertOrganisationCleanup(ProcessMonitorDto processMonitorDto,
                                   int expectedRecords, EndStatus endStatus) {
        assertProcessMonitor(processMonitorDto, endStatus);
        assertEquals(expectedRecords, organisationRefreshQueueRepository.findAll().size());
    }

    private void assertProcessMonitor(ProcessMonitorDto processMonitorDto,
                                      EndStatus endStatus) {
        assertNotNull(processMonitorDto);
        assertEquals(endStatus, processMonitorDto.getEndStatus());
    }
}
