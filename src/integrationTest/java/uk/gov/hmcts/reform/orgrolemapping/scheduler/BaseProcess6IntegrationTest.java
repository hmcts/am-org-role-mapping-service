package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.oidc.JwtGrantedAuthoritiesConverter;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SUCCESS_ROLE_REFRESH;

abstract class BaseProcess6IntegrationTest extends BaseSchedulerTestIntegration {

    protected static final String USERID = "USERX";

    @Autowired
    private UserRefreshQueueRepository userRefreshQueueRepository;

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
     * User - accessDefault = N, accessMandatory = N, groupAccessEnabled = N, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nnn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_User_nnn() throws JsonProcessingException {
        testSingleRole(true);
    }

    /**
     * Org - accessDefault = N, accessMandatory = N, groupAccessEnabled = N, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nnn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_Org_nnn() throws JsonProcessingException {
        testSingleRole(false);
    }

    /**
     * User - accessDefault = Y, accessMandatory = N, groupAccessEnabled = N, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_ynn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_User_ynn() throws JsonProcessingException {
        testSingleRole(true);
    }

    /**
     * Org - accessDefault = Y, accessMandatory = N, groupAccessEnabled = N, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_ynn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_Org_ynn() throws JsonProcessingException {
        testSingleRole(false);
    }

    /**
     * User - accessDefault = Y, accessMandatory = Y, groupAccessEnabled = N, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_User_yyn() throws JsonProcessingException {
        testSingleRole(true);
    }

    /**
     * Org - accessDefault = Y, accessMandatory = Y, groupAccessEnabled = N, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_Org_yyn() throws JsonProcessingException {
        testSingleRole(false);
    }

    /**
     * User - accessDefault = N, accessMandatory = Y, groupAccessEnabled = N, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_User_nyn() throws JsonProcessingException {
        testSingleRole(true);
    }

    /**
     * Org - accessDefault = N, accessMandatory = Y, groupAccessEnabled = N, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_Org_nyn() throws JsonProcessingException {
        testSingleRole(false);
    }

    /**
     * User - accessDefault = N, accessMandatory = Y, groupAccessEnabled = Y, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyy.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_User_nyy() throws JsonProcessingException {
        testSingleRole(true);
    }

    /**
     * Org - accessDefault = N, accessMandatory = Y, groupAccessEnabled = Y, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyy.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_Org_nyy() throws JsonProcessingException {
        testSingleRole(false);
    }

    /**
     * User - accessDefault = N, accessMandatory = N, groupAccessEnabled = Y, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_User_nny() throws JsonProcessingException {
        testSingleRole(true);
    }

    /**
     * Org - accessDefault = N, accessMandatory = N, groupAccessEnabled = Y, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_Org_nny() throws JsonProcessingException {
        testSingleRole(false);
    }

    /**
     * User - accessDefault = Y, accessMandatory = Y, groupAccessEnabled = Y, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyy.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_User_yyy() throws JsonProcessingException {
        testSingleRole(true);
    }

    /**
     * Org - accessDefault = Y, accessMandatory = Y, groupAccessEnabled = Y, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyy.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_Org_yyy() throws JsonProcessingException {
        testSingleRole(false);
    }

    /**
     * User - accessDefault = Y, accessMandatory = N, groupAccessEnabled = Y, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_User_yny() throws JsonProcessingException {
        testSingleRole(true);
    }

    /**
     * Org - accessDefault = Y, accessMandatory = N, groupAccessEnabled = Y, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_Org_yny() throws JsonProcessingException {
        testSingleRole(false);
    }

    abstract void testSingleRole(boolean user);

    //#region Assertion Helpers: DB Checks

    protected void assertResponse(ResponseEntity<Object> actualResponse) {
        assertNotNull(actualResponse);
        assertNotNull(actualResponse.getBody());
        assertEquals(actualResponse.getBody(), Map.of("Message", SUCCESS_ROLE_REFRESH));
    }

    protected void assertTotalUserRefreshQueueEntitiesInDb(int expectedNumberOfRecords) {
        var userRefreshQueueEntities = userRefreshQueueRepository.findAll();
        assertEquals(expectedNumberOfRecords, userRefreshQueueEntities.size(),
                "UserRefreshQueueEntity number of records mismatch");
        assertEquals(0, userRefreshQueueEntities.stream()
                        .filter(entity -> entity.getActive()).count(),
                "UserRefreshQueueEntity number of active records mismatch");
    }

    protected void assertAccessTypes(String accessTypeId, String organisationProfileId,
                                     String jurisdictionId, boolean enabled) {
        var userRefreshQueueEntities = userRefreshQueueRepository.findAll();
        var userRefreshQueueEntity = userRefreshQueueEntities.getFirst();
        String accessTypes = userRefreshQueueEntity.getAccessTypes();
        assertTrue(accessTypes.contains(accessTypeId),
                "UserRefreshQueueEntity " + accessTypeId + " not found");
        assertTrue(accessTypes.contains("\"enabled\": " + (enabled ? "true" : "false")),
                "UserRefreshQueueEntity " + accessTypeId + ".enabled mismatch");
        assertTrue(accessTypes.contains("\"organisationProfileId\": " + organisationProfileId),
                "UserRefreshQueueEntity " + accessTypeId + ".organisationProfileId mismatch");
        assertTrue(accessTypes.contains("\"jurisdictionId\": " + jurisdictionId),
                "UserRefreshQueueEntity " + accessTypeId + ".jurisdictionId mismatch");
    }

    //#endregion

    protected void logAfterStatus(Object response) {
        logObject("ProcessMonitorDto: AFTER", response);
        logObject("userRefreshQueueRepository: AFTER", userRefreshQueueRepository.findAll());
    }

    protected void logBeforeStatus() {
        logObject("userRefreshQueueRepository: BEFORE", userRefreshQueueRepository.findAll());
    }

    protected void verifyNoOfCallsToRas(int noOfCalls) {
        var allCallEvents = logWiremockPostCalls(STUB_ID_RAS_CREATE_ROLEASSIGNMENTS);
        // verify single call
        assertEquals(noOfCalls, allCallEvents.size(),
                "Unexpected number of calls to RAS service");
        if (noOfCalls == 0) {
            return; // no need to check further if no calls were made
        }
        var event = allCallEvents.get(0);
        // verify response status
        assertEquals(HttpStatus.OK.value(), event.getResponse().getStatus(),
                "Response status mismatch");
    }
}