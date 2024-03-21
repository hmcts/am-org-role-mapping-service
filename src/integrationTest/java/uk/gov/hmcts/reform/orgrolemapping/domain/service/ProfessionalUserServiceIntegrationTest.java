package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
public class ProfessionalUserServiceIntegrationTest extends BaseTestIntegration {

    @Autowired
    private ProfessionalUserService userService;

    @Autowired
    private UserRefreshQueueRepository userRefreshQueueRepository;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_new_user_refresh_queue.sql"})
    void shouldDeleteNoneFromUserRefreshQueueTest() {
        userService.deleteActiveUserRefreshRecords();

        assertEquals(1, userRefreshQueueRepository.findAll().size());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_old_user_refresh_queue.sql"})
    void shouldDeleteOneFromUserRefreshQueueTest() {
        userService.deleteActiveUserRefreshRecords();

        assertEquals(0, userRefreshQueueRepository.findAll().size());
    }
}
