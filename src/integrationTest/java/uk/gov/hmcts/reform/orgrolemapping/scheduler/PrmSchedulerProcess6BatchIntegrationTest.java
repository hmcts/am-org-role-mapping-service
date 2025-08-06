package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.oidc.JwtGrantedAuthoritiesConverter;

class PrmSchedulerProcess6BatchIntegrationTest extends BaseSchedulerTestIntegration {

    @Autowired
    private UserRefreshQueueRepository userRefreshQueueRepository;

    @Autowired
    private Scheduler prmScheduler;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Inject
    private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @BeforeEach
    public void setUp() throws Exception {
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        UserInfo userInfo = UserInfo.builder()
            .uid("6b36bfc6-bb21-11ea-b3de-0242ac130006")
            .sub("emailId@a.com")
            .build();
        ReflectionTestUtils.setField(
            jwtGrantedAuthoritiesConverter,
            "userInfo", userInfo
        );
        MockUtils.setSecurityAuthorities(authentication, MockUtils.ROLE_CASEWORKER);
        wiremockFixtures.resetRequests();
    }

    /**
     * No Change - Empty User List.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql"
    })
    void testNoUsers() {

        // verify that no users are updated
        runTest();

        // Verify no records in the refresh queue
        assertTotalUserRefreshQueueEntitiesInDb(0);
    }

    /**
     * Update - User updated with an Organisational Role.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_org.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userx_orgprofile1.sql"
    })
    void testUpdateUserWithOrganisationalRole() throws JsonProcessingException {

        // verify that no users are updated
        runTest();

        // Verify 1 record in the refresh queue
        assertTotalUserRefreshQueueEntitiesInDb(1);
    }

    private void runTest() {

        // GIVEN
        logBeforeStatus();
        stubRasCreateRoleAssignment(List.of(), EndStatus.SUCCESS);

        // WHEN
        ProcessMonitorDto processMonitorDto = prmScheduler.processUserRefreshQueue();

        // THEN
        logAfterStatus(processMonitorDto);

        // verify that the process monitor reports the correct status
        assertEquals(EndStatus.SUCCESS, processMonitorDto.getEndStatus());
    }

    //#region Assertion Helpers: DB Checks

    private void assertTotalUserRefreshQueueEntitiesInDb(int expectedNumberOfRecords) {
        var userRefreshQueueEntities = userRefreshQueueRepository.findAll();
        assertEquals(expectedNumberOfRecords, userRefreshQueueEntities.size(),
            "UserRefreshQueueEntity number of records mismatch");
        assertEquals(0, userRefreshQueueEntities.stream()
                .filter(entity -> entity.getActive()).count(),
            "UserRefreshQueueEntity number of active records mismatch");
    }

    //#endregion

    private void logAfterStatus(ProcessMonitorDto processMonitorDto) {
        logObject("ProcessMonitorDto: AFTER", processMonitorDto);
        logObject("userRefreshQueueRepository: AFTER", userRefreshQueueRepository.findAll());
    }

    private void logBeforeStatus() {
        logObject("userRefreshQueueRepository: BEFORE", userRefreshQueueRepository.findAll());
    }
}
