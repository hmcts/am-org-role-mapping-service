package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUserResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.refreshUser;

@Transactional
public class ProfessionalUserServiceTest extends BaseTestIntegration {

    @Autowired
    private ProfessionalUserService professionalUserService;

    @Autowired
    private UserRefreshQueueRepository userRefreshQueueRepository;

    @MockBean
    private PrdService prdService;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_access_types.sql",
                       "classpath:sql/insert_batch_last_run.sql",
                       "classpath:sql/insert_user_refresh_queue.sql"})
    void shouldFindUserChangesAndInsertIntoRefreshQueue_WithoutPagination() {
        RefreshUser refreshUser = refreshUser(1);
        GetRefreshUserResponse response1 = IntTestDataBuilder.buildRefreshUserResponse(refreshUser, "123", false);

        when(prdService.retrieveUsers(any(), anyInt(), eq(null)))
                .thenReturn(ResponseEntity.ok(response1));

        professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue();

        assertEquals(2, userRefreshQueueRepository.findAll().size());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_access_types.sql",
                       "classpath:sql/insert_batch_last_run.sql",
                       "classpath:sql/insert_user_refresh_queue.sql"})
    void shouldFindUserChangesAndInsertIntoRefreshQueue_WithPagination() {
        RefreshUser refreshUser = refreshUser(1);
        GetRefreshUserResponse response1 = IntTestDataBuilder.buildRefreshUserResponse(refreshUser, "123", true);

        when(prdService.retrieveUsers(any(), anyInt(), eq(null)))
                .thenReturn(ResponseEntity.ok(response1));

        RefreshUser refreshUser2 = refreshUser(2);
        GetRefreshUserResponse response2 = IntTestDataBuilder.buildRefreshUserResponse(refreshUser2, "456", false);

        when(prdService.retrieveUsers(any(), anyInt(), any(String.class)))
                .thenReturn(ResponseEntity.ok(response2));

        professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue();

        assertEquals(3, userRefreshQueueRepository.findAll().size());
    }
}
