package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrmSchedulerCleanupIntegrationTest extends BaseTestIntegration {

    private static final String NO_ENTITIES = "No entities to process";

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
                0, 0, EndStatus.SUCCESS);
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
               1, 0, EndStatus.SUCCESS);
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
                1, 0, EndStatus.SUCCESS);
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
                1, 0, EndStatus.SUCCESS);
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
                0, 1, EndStatus.SUCCESS);
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
                1, 1, EndStatus.SUCCESS);
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
                0, 0, EndStatus.SUCCESS);
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
                1, 0, EndStatus.SUCCESS);
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
                1, 0, EndStatus.SUCCESS);
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
                1, 0, EndStatus.SUCCESS);
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
                0, 1, EndStatus.SUCCESS);
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
                1, 1, EndStatus.SUCCESS);
    }

    //# Validation methods.

    private void assertUserCleanup(ProcessMonitorDto processMonitorDto,
                                   int expectedRecordsInDB, int expectedRecordsDeleted,
                                   EndStatus endStatus) {
        assertProcessMonitor(processMonitorDto, expectedRecordsDeleted, true, endStatus);
        assertEquals(expectedRecordsInDB, userRefreshQueueRepository.findAll().size());
    }

    private void assertOrganisationCleanup(ProcessMonitorDto processMonitorDto,
                                           int expectedRecordsInDB, int expectedRecordsDeleted,
                                           EndStatus endStatus) {
        assertProcessMonitor(processMonitorDto, expectedRecordsDeleted, false, endStatus);
        assertEquals(expectedRecordsInDB, organisationRefreshQueueRepository.findAll().size());
    }

    private void assertProcessMonitor(ProcessMonitorDto processMonitorDto,
                                      int expectedRecordsDeleted, boolean isUser,
                                      EndStatus endStatus) {
        assertNotNull(processMonitorDto);
        assertEquals(endStatus, processMonitorDto.getEndStatus());
        String message = expectedRecordsDeleted != 0
                ? String.format("Deleted %s inactive %s refresh queue entities=123,",
                        expectedRecordsDeleted,
                        isUser ? "user" : "organisation"
                        ) : NO_ENTITIES;
        assertTrue(processMonitorDto.getProcessSteps().contains(message));
        assertEquals(expectedRecordsDeleted, expectedRecordsDeleted);
    }
}
