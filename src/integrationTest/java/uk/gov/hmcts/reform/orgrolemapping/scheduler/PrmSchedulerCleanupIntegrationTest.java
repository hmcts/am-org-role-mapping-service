package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrmSchedulerCleanupIntegrationTest extends BaseTestIntegration {

    @Autowired
    private UserRefreshQueueRepository userRefreshQueueRepository;

    @Autowired
    private OrganisationRefreshQueueRepository organisationRefreshQueueRepository;

    private final WiremockFixtures wiremockFixtures = new WiremockFixtures();

    private ProcessMonitorDto processMonitorDto;

    @Autowired
    private Scheduler scheduler;

    //# UserRefreshQueue cleanup tests

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_new_user_refresh_queue.sql"})
    void shouldDeleteNoneFromUserRefreshQueueTest() {
        scheduler.deleteInactiveUserRefreshRecords();

        assertEquals(1, userRefreshQueueRepository.findAll().size());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_old_user_refresh_queue.sql"})
    void shouldDeleteOneFromUserRefreshQueueTest() {
        scheduler.deleteInactiveUserRefreshRecords();

        assertEquals(0, userRefreshQueueRepository.findAll().size());
    }

    //# OrganisationRefreshQueue cleanup tests

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_new_organisation_profiles.sql"})
    void shouldNotDeleteNewInactiveEntryFromOrganisationRefreshQueueTest() {
        scheduler.deleteInactiveOrganisationRefreshRecords();

        assertEquals(1, organisationRefreshQueueRepository.findAll().size());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_old_active_organisation_profiles.sql"})
    void shouldNotDeleteOldActiveEntryFromOrganisationRefreshQueueTest() {
        scheduler.deleteInactiveOrganisationRefreshRecords();

        assertEquals(1, organisationRefreshQueueRepository.findAll().size());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_old_organisation_profiles.sql"})
    void shouldDeleteOldAndInactiveEntryFromOrganisationRefreshQueueTest() {
        scheduler.deleteInactiveOrganisationRefreshRecords();

        assertEquals(0, organisationRefreshQueueRepository.findAll().size());
    }
}
