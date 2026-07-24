package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.helper.RoleAssignmentAssertIntegrationHelper;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.Attributes.Name.CASE_ACCESS_GROUP_ID;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.Attributes.Name.CASE_TYPE;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.Attributes.Name.JURISDICTION;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.DF;

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
     *  Test scenarios from DTSAM-1239.
     *
     * <pre>
     *  |----------|---------|-----------|--------------|---------|----------|------------|
     *  | Scenario | Access  |   Access  | Group Access |   PRD   | Generate |  Generate  |
     *  |    No.   | Default | Mandatory |    Enabled   | Enabled | Org Role | Group Role |
     *  |----------|---------|-----------|--------------|---------|----------|------------|
     *  |     1    |   True  |    True   |      True    |   True  |   Yes *  |    Yes **  |
     *  |     2    |   True  |    True   |      True    |  False  |   Yes *  |    Yes **  |
     *  |     2a   |   True  |    True   |      True    |   Null  |   Yes *  |    Yes **  |
     *  |     3    |   True  |    True   |     False    |   True  |   Yes *  |     No     |
     *  |     4    |   True  |    True   |     False    |  False  |   Yes *  |     No     |
     *  |     4a   |   True  |    True   |     False    |   Null  |   Yes *  |     No     |
     *  |     5    |   True  |   False   |      True    |   True  |   Yes *  |    Yes **  |
     *  |     6    |   True  |   False   |      True    |  False  |    No    |     No     |
     *  |     6a   |   True  |   False   |      True    |   Null  |   Yes *  |    Yes **  |
     *  |     7    |   True  |   False   |     False    |   True  |   Yes *  |     No     |
     *  |     8    |   True  |   False   |     False    |  False  |    No    |     No     |
     *  |     8a   |   True  |   False   |     False    |   Null  |   Yes *  |     No     |
     *  |     9    |  False  |    True   |      True    |   True  |   Yes *  |    Yes **  |
     *  |    10    |  False  |    True   |      True    |  False  |   Yes *  |    Yes **  |
     *  |    10a   |  False  |    True   |      True    |   Null  |   Yes *  |    Yes **  |
     *  |    11    |  False  |    True   |     False    |   True  |   Yes *  |     No     |
     *  |    12    |  False  |    True   |     False    |  False  |   Yes *  |     No     |
     *  |    12a   |  False  |    True   |     False    |   Null  |   Yes *  |     No     |
     *  |    13    |  False  |   False   |      True    |   True  |   Yes *  |    Yes **  |
     *  |    14    |  False  |   False   |      True    |  False  |    No    |     No     |
     *  |    14a   |  False  |   False   |      True    |   Null  |    No    |     No     |
     *  |    15    |  False  |   False   |     False    |   True  |   Yes *  |     No     |
     *  |    16    |  False  |   False   |     False    |  False  |    No    |     No     |
     *  |    16a   |  False  |   False   |     False    |   Null  |    No    |     No     |
     *  |----------|---------|-----------|--------------|---------|----------|------------|
     *
     *    * Yes - if organisational_role_name is not empty
     *   ** Yes - if group_role_name is not empty and case_group_id_template is not empty
     * </pre>
     */
    @SuppressWarnings({"unused"})
    private static final byte ALL_SCENARIOS_TABLE = 0;

    /**
     *  Scenario ALL.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_all_scenarios.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_all_scenarios.sql"
    })
    void testCreateRole_All() {
        testCreateRoleAssignmentAllScenarios();
    }

    /**
     *  Scenario 1:
     *  accessDefault = Y, accessMandatory = Y, groupAccessEnabled = Y, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyy.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_S01_yyyy() {
        testCreateRoleAssignment(true, true);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyy_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_S01_yyyy_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 2:
     *  accessDefault = Y, accessMandatory = Y, groupAccessEnabled = Y, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyy.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_S02_yyyn() {
        testCreateRoleAssignment(true, true);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyy_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_S02_yyyn_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 2a:
     *  accessDefault = Y, accessMandatory = Y, groupAccessEnabled = Y, PRDenabled = Null.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyy.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_missing.sql"
    })
    void testCreateRole_S02a_yyy_null() {
        testCreateRoleAssignment(true, true);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyy_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_missing.sql"
    })
    void testCreateRole_S02a_yyy_null_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 3:
     *  accessDefault = Y, accessMandatory = Y, groupAccessEnabled = N, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_S03_yyny() {
        testCreateRoleAssignment(true, false);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyn_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_S03_yyny_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 4:
     *  accessDefault = Y, accessMandatory = Y, groupAccessEnabled = N, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_S04_yynn() {
        testCreateRoleAssignment(true, false);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyn_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_S04_yynn_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 4a:
     *  accessDefault = Y, accessMandatory = Y, groupAccessEnabled = N, PRDenabled = Null.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_missing.sql"
    })
    void testCreateRole_S04a_yyn_null() {
        testCreateRoleAssignment(true, false);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyn_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_missing.sql"
    })
    void testCreateRole_S04a_yyn_null_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 5:
     *  accessDefault = Y, accessMandatory = N, groupAccessEnabled = Y, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_S05_ynyy() {
        testCreateRoleAssignment(true, true);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yny_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_S05_ynyy_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 6:
     *  accessDefault = Y, accessMandatory = N, groupAccessEnabled = Y, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_S06_ynyn() {
        testCreateRoleAssignment(false, false);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yny_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_S06_ynyn_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 6a:
     *  accessDefault = Y, accessMandatory = N, groupAccessEnabled = Y, PRDenabled = Null.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_missing.sql"
    })
    void testCreateRole_S06a_yny_null() {
        testCreateRoleAssignment(true, true);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yny_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_missing.sql"
    })
    void testCreateRole_S06a_yny_null_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 7:
     *  accessDefault = Y, accessMandatory = N, groupAccessEnabled = N, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_ynn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_S07_ynny() {
        testCreateRoleAssignment(true, false);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_ynn_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_S07_ynny_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 8:
     *  accessDefault = Y, accessMandatory = N, groupAccessEnabled = N, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_ynn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_S08_ynnn() {
        testCreateRoleAssignment(false, false);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_ynn_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_S08_ynnn_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 8a:
     *  accessDefault = Y, accessMandatory = N, groupAccessEnabled = N, PRDenabled = Null.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_ynn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_missing.sql"
    })
    void testCreateRole_S08a_ynn_null() {
        testCreateRoleAssignment(true, false);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_ynn_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_missing.sql"
    })
    void testCreateRole_S08a_ynn_null_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 9:
     *  accessDefault = N, accessMandatory = Y, groupAccessEnabled = Y, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyy.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_S09_nyyy() {
        testCreateRoleAssignment(true, true);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyy_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_S09_nyyy_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 10:
     *  accessDefault = N, accessMandatory = Y, groupAccessEnabled = Y, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyy.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_S10_nyyn() {
        testCreateRoleAssignment(true, true);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyy_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_S10_nyyn_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 10a:
     *  accessDefault = N, accessMandatory = Y, groupAccessEnabled = Y, PRDenabled = Null.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyy.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_missing.sql"
    })
    void testCreateRole_S10a_nyy_null() {
        testCreateRoleAssignment(true, true);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyy_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_missing.sql"
    })
    void testCreateRole_S10a_nyy_null_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 11:
     *  accessDefault = N, accessMandatory = Y, groupAccessEnabled = N, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_S11_nyny() {
        testCreateRoleAssignment(true, false);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyn_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_S11_nyny_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 12:
     *  accessDefault = N, accessMandatory = Y, groupAccessEnabled = N, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_S12_nynn() {
        testCreateRoleAssignment(true, false);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyn_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_S12_nynn_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 12a:
     *  accessDefault = N, accessMandatory = Y, groupAccessEnabled = N, PRDenabled = Null.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_missing.sql"
    })
    void testCreateRole_S12a_nyn_null() {
        testCreateRoleAssignment(true, false);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyn_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_missing.sql"
    })
    void testCreateRole_S12a_nyn_null_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 13:
     *  accessDefault = N, accessMandatory = N, groupAccessEnabled = Y, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_S13_nnyy() {
        testCreateRoleAssignment(true, true);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nny_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_S13_nnyy_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 14:
     *  accessDefault = N, accessMandatory = N, groupAccessEnabled = Y, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_S14_nnyn() {
        testCreateRoleAssignment(false, false);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nny_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_S14_nnyn_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 14a:
     *  accessDefault = N, accessMandatory = N, groupAccessEnabled = Y, PRDenabled = Null.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_missing.sql"
    })
    void testCreateRole_S14a_nny_null() {
        testCreateRoleAssignment(false, false);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nny_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_missing.sql"
    })
    void testCreateRole_S14a_nny_null_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 15:
     *  accessDefault = N, accessMandatory = N, groupAccessEnabled = N, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nnn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_S15_nnny() {
        testCreateRoleAssignment(true, false);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nnn_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_S15_nnny_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 16:
     *  accessDefault = N, accessMandatory = N, groupAccessEnabled = N, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nnn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_S16_nnnn() {
        testCreateRoleAssignment(false, false);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nnn_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_S16_nnnn_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  Scenario 16a:
     *  accessDefault = N, accessMandatory = N, groupAccessEnabled = N, PRDenabled = Null.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nnn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_missing.sql"
    })
    void testCreateRole_S16a_nnn_null() {
        testCreateRoleAssignment(false, false);
    }

    // repeat with no role-names
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nnn_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_missing.sql"
    })
    void testCreateRole_S16a_nnn_null_no_rolenames() {
        testCreateRoleAssignment(false, false);
    }

    abstract void testCreateRoleAssignment(boolean orgRole, boolean groupRole);

    abstract void testCreateRoleAssignmentAllScenarios();

    //#region Assertion Helpers: DB Checks

    protected void assertTotalUserRefreshQueueEntitiesInDb(int expectedNumberOfRecords,
                                                           EndStatus endStatus) {
        var userRefreshQueueEntities = userRefreshQueueRepository.findAll();
        assertEquals(expectedNumberOfRecords, userRefreshQueueEntities.size(),
                "UserRefreshQueueEntity number of records mismatch");
        assertEquals(EndStatus.SUCCESS.equals(endStatus) ? 0 :
                        EndStatus.PARTIAL_SUCCESS.equals(endStatus) ? 1 : expectedNumberOfRecords,
                userRefreshQueueEntities.stream()
                        .filter(UserRefreshQueueEntity::getActive).count(),
                "UserRefreshQueueEntity number of active records mismatch");
    }

    protected void assertRetry(int retryCount) {
        var userRefreshQueueEntities = userRefreshQueueRepository.findAll();
        var userRefreshQueueEntity = userRefreshQueueEntities.getFirst();
        assertEquals(retryCount, userRefreshQueueEntity.getRetry(),
                "UserRefreshQueueEntity.retry mismatch");
        assertTrue(userRefreshQueueEntity.getActive(),
                "UserRefreshQueueEntity.active mismatch");
        if (retryCount < 4) {
            assertTrue(assertLastUpdatedNow(userRefreshQueueEntity.getRetryAfter()),
                    "UserRefreshQueueEntity.retryAfter mismatch");
        } else {
            assertNull(userRefreshQueueEntity.getRetryAfter(),
                    "UserRefreshQueueEntity.retryAfter is not null");
        }
    }

    private boolean assertLastUpdatedNow(LocalDateTime lastUpdated) {
        return lastUpdated.isAfter(LocalDateTime.now().minusMinutes(1));
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

    protected void verifyNoOfCallsToPrd(int noOfCalls) {
        var allCallEvents = logWiremockPostCalls(STUB_ID_PRD_REFRESH_USER);
        // verify number of calls
        assertEquals(noOfCalls, allCallEvents.size(),
                "Unexpected number of calls to PRD service");
        if (noOfCalls == 0) {
            return; // no need to check further if no calls were made
        }
        ServeEvent event  = allCallEvents.get(0);
        // verify response status
        assertEquals(HttpStatus.OK.value(), event.getResponse().getStatus(),
                "Response status mismatch");
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

    protected void assertAssignmentRequest(boolean expectedOrganisationRole,
                                           boolean expectedGroupRole,
                                           boolean allScenarios) {
        AssignmentRequest assignmentRequest = getAssignmentRequest();
        assertNotNull(assignmentRequest, "No AssignmentRequest found");

        if (!allScenarios) {
            int noOfRoles = (expectedOrganisationRole ? 1 : 0) + (expectedGroupRole ? 1 : 0);
            assertEquals(noOfRoles, assignmentRequest.getRequestedRoles().size(),
                "Unexpected number of requestedRoles in AssignmentRequest");
            boolean actualOrganisation = false;
            boolean actualGroup = false;

            for (RoleAssignment roleAssignment : assignmentRequest.getRequestedRoles()) {
                if (isGroupRole(roleAssignment)) {
                    actualGroup = true;
                    assertRoleAssignment(roleAssignment, true);
                } else {
                    actualOrganisation = true;
                    assertRoleAssignment(roleAssignment, false);

                }
            }
            assertEquals(expectedOrganisationRole, actualOrganisation, "Organisation role missing");
            assertEquals(expectedGroupRole, actualGroup, "Group role missing");

        } else {
            assertAssignmentRequestAllScenarios(assignmentRequest);
        }
    }

    /**
     * NB: number of roles defined in scenarios table above: {@link #ALL_SCENARIOS_TABLE all scenarios table}.
     */
    private void assertAssignmentRequestAllScenarios(AssignmentRequest assignmentRequest) {

        assertEquals(27, assignmentRequest.getRequestedRoles().size(),
            "Unexpected number of requestedRoles in AssignmentRequest");

        for (RoleAssignment roleAssignment : assignmentRequest.getRequestedRoles()) {
            String roleName = roleAssignment.getRoleName();
            assertNotNull(roleName, "Role name must be set");

            switch (roleName) {

                // Scenario 1: Group + Org roles
                case "SCENARIO_yyy_1__UserAccessType_Enabled__GroupRole",
                    "SCENARIO_yyy_1__UserAccessType_Enabled__OrgRole" ->
                    assertRoleAssignment(roleAssignment, "1");

                // Scenario 2: Group + Org roles
                case "SCENARIO_yyy_2__UserAccessType_Disabled__GroupRole",
                    "SCENARIO_yyy_2__UserAccessType_Disabled__OrgRole" ->
                    assertRoleAssignment(roleAssignment, "2");

                // Scenario 2a: Group + Org roles
                case "SCENARIO_yyy_2a__UserAccessType_Missing__GroupRole",
                    "SCENARIO_yyy_2a__UserAccessType_Missing__OrgRole" ->
                    assertRoleAssignment(roleAssignment, "2a");


                // Scenario 3: Org role only
                case "SCENARIO_yyn_3__UserAccessType_Enabled__OrgRole" ->
                    assertRoleAssignment(roleAssignment, "3");

                // Scenario 4: Org role only
                case "SCENARIO_yyn_4__UserAccessType_Disabled__OrgRole" ->
                    assertRoleAssignment(roleAssignment, "4");

                // Scenario 4a: Org role only
                case "SCENARIO_yyn_4a__UserAccessType_Missing__OrgRole" ->
                    assertRoleAssignment(roleAssignment, "4a");


                // Scenario 5: Group + Org roles
                case "SCENARIO_yny_5__UserAccessType_Enabled__GroupRole",
                    "SCENARIO_yny_5__UserAccessType_Enabled__OrgRole" ->
                    assertRoleAssignment(roleAssignment, "5");

                // Scenario 6: no roles

                // Scenario 6a: Group + Org roles
                case "SCENARIO_yny_6a__UserAccessType_Missing__GroupRole",
                    "SCENARIO_yny_6a__UserAccessType_Missing__OrgRole" ->
                    assertRoleAssignment(roleAssignment, "6a");


                // Scenario 7: Org role only
                case "SCENARIO_ynn_7__UserAccessType_Enabled__OrgRole" ->
                    assertRoleAssignment(roleAssignment, "7");

                // Scenario 8: no roles

                // Scenario 8a: Org role only
                case "SCENARIO_ynn_8a__UserAccessType_Missing__OrgRole" ->
                    assertRoleAssignment(roleAssignment, "8a");


                // Scenario 9: Group + Org roles
                case "SCENARIO_nyy_9__UserAccessType_Enabled__GroupRole",
                    "SCENARIO_nyy_9__UserAccessType_Enabled__OrgRole" ->
                    assertRoleAssignment(roleAssignment, "9");

                // Scenario 10: Group + Org roles
                case "SCENARIO_nyy_10__UserAccessType_Disabled__GroupRole",
                    "SCENARIO_nyy_10__UserAccessType_Disabled__OrgRole" ->
                    assertRoleAssignment(roleAssignment, "10");

                // Scenario 10a: Group + Org roles
                case "SCENARIO_nyy_10a__UserAccessType_Missing__GroupRole",
                    "SCENARIO_nyy_10a__UserAccessType_Missing__OrgRole" ->
                    assertRoleAssignment(roleAssignment, "10a");


                // Scenario 11: Org role only
                case "SCENARIO_nyn_11__UserAccessType_Enabled__OrgRole" ->
                    assertRoleAssignment(roleAssignment, "11");

                // Scenario 12: Org role only
                case "SCENARIO_nyn_12__UserAccessType_Disabled__OrgRole" ->
                    assertRoleAssignment(roleAssignment, "12");

                // Scenario 12a: Org role only
                case "SCENARIO_nyn_12a__UserAccessType_Missing__OrgRole" ->
                    assertRoleAssignment(roleAssignment, "12a");


                // Scenario 13: Group + Org roles
                case "SCENARIO_nny_13__UserAccessType_Enabled__GroupRole",
                    "SCENARIO_nny_13__UserAccessType_Enabled__OrgRole" ->
                    assertRoleAssignment(roleAssignment, "13");

                // Scenario 14: no roles

                // Scenario 14a: no roles


                // Scenario 15 Org role only
                case "SCENARIO_nnn_15__UserAccessType_Enabled__OrgRole" ->
                    assertRoleAssignment(roleAssignment, "15");

                // Scenario 16: no roles

                // Scenario 16a: no roles


                default -> fail("Role assignment should not have been created: " + roleName);

            }
        }
    }

    private void assertRoleAssignment(RoleAssignment roleAssignment, boolean isGroupRole) {
        // NB: test user defined in: `insert_userrefresh_{deleted|disabled|enabled|missing|version1}.sql` is in
        // an Organisation using `ORGPROFILE2` so accessType config defined for `ORGPROFILE1` will be skipped.

        assertRoleAssignment(
            roleAssignment,
            isGroupRole,
            isGroupRole ? "GroupRole1" : "OrgRole1",
            "BEFTA_JURISDICTION_2",
            "FT_CaseAccessGroups"
        );
    }

    private void assertRoleAssignment(RoleAssignment roleAssignment,
                                      String scenarioId) {

        String roleName = roleAssignment.getRoleName();
        boolean isGroupRole = roleName.endsWith("__GroupRole");
        String expectedJurisdiction = "";
        String expectedCaseType = "";

        switch (scenarioId) {
            case "1", "2", "2a" -> {
                expectedJurisdiction = "SCENARIO_yyy_1_2_2a__JURISDICTION__ORGPROFILE2";
                expectedCaseType = "SCENARIO_yyy_" + scenarioId + "__CASETYPE";
            }

            case "3", "4", "4a" -> {
                expectedJurisdiction = "SCENARIO_yyn_3_4_4a__JURISDICTION__ORGPROFILE2";
                expectedCaseType = "SCENARIO_yyn_" + scenarioId + "__CASETYPE";
            }

            case "5", "6", "6a" -> {
                expectedJurisdiction = "SCENARIO_yny_5_6_6a__JURISDICTION__ORGPROFILE2";
                expectedCaseType = "SCENARIO_yny_" + scenarioId + "__CASETYPE";
            }

            case "7", "8", "8a" -> {
                expectedJurisdiction = "SCENARIO_ynn__7_8_8a__JURISDICTION__ORGPROFILE2";
                expectedCaseType = "SCENARIO_ynn_" + scenarioId + "__CASETYPE";
            }

            case "9", "10", "10a" -> {
                expectedJurisdiction = "SCENARIO_nyy_9_10_10a__JURISDICTION__ORGPROFILE2";
                expectedCaseType = "SCENARIO_nyy_" + scenarioId + "__CASETYPE";
            }

            case "11", "12", "12a" -> {
                expectedJurisdiction = "SCENARIO_nyn_11_12_12a__JURISDICTION__ORGPROFILE2";
                expectedCaseType = "SCENARIO_nyn_" + scenarioId + "__CASETYPE";
            }

            case "13", "14", "14a" -> {
                expectedJurisdiction = "SCENARIO_nny_13_14_14a__JURISDICTION__ORGPROFILE2";
                expectedCaseType = "SCENARIO_nny_" + scenarioId + "__CASETYPE";
            }

            case "15", "16", "16a" -> {
                expectedJurisdiction = "SCENARIO_nnn__15_16_16a__JURISDICTION__ORGPROFILE2";
                expectedCaseType = "SCENARIO_nnn_" + scenarioId + "__CASETYPE";
            }

            default -> fail("scenario not recognised: " + scenarioId);
        }

        assertRoleAssignment(
            roleAssignment,
            isGroupRole,
            roleName,
            expectedJurisdiction,
            expectedCaseType
        );
    }

    private void assertRoleAssignment(RoleAssignment roleAssignment,
                                      boolean isGroupRole,
                                      String roleName,
                                      String expectedJurisdiction,
                                      String expectedCaseType) {

        String prefix = isGroupRole ? "Group" : "Operational";
        assertEquals(ActorIdType.IDAM, roleAssignment.getActorIdType(),
            prefix + " actor type mismatch");
        assertTrue(roleAssignment.getActorId().contains("USERX")
                || roleAssignment.getActorId().contains("USERY"),
            prefix + " actorId mismatch");
        assertEquals(roleName, roleAssignment.getRoleName(),
            prefix + " role name mismatch");
        assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType(),
            prefix + " role type mismatch");
        assertEquals(RoleCategory.PROFESSIONAL, roleAssignment.getRoleCategory(),
            prefix + " role category mismatch");
        assertEquals(Classification.RESTRICTED, roleAssignment.getClassification(),
            prefix + " classification mismatch");
        assertEquals(GrantType.STANDARD, roleAssignment.getGrantType(),
            prefix + " grant type mismatch");
        assertEquals(0, roleAssignment.getAuthorisations().size(),
            prefix + " authorisations mismatch");
        assertFalse(roleAssignment.isReadOnly(),
            prefix + " readOnly mismatch");
        assertNull(roleAssignment.getBeginTime(),
            prefix + " beginTime mismatch");
        assertNull(roleAssignment.getEndTime(),
            prefix + " endTime mismatch");
        assertNull(roleAssignment.getNotes(),
            prefix + " notes mismatch");
        assertEquals(JacksonUtils.convertObjectIntoJsonNode(expectedJurisdiction),
            roleAssignment.getAttributes().get(JURISDICTION),
            prefix + " jurisdiction mismatch");
        assertEquals(JacksonUtils.convertObjectIntoJsonNode(expectedCaseType),
            roleAssignment.getAttributes().get(CASE_TYPE),
            prefix + " caseType mismatch");
        if (isGroupRole) {
            assertEquals(JacksonUtils.convertObjectIntoJsonNode("BEFTA_MASTER:ORG1"),
                roleAssignment.getAttributes().get(CASE_ACCESS_GROUP_ID),
                prefix + " caseAccessGroupId mismatch");
        } else {
            assertFalse(roleAssignment.getAttributes().containsKey(CASE_ACCESS_GROUP_ID),
                prefix + " caseAccessGroupId should not be set");
        }
    }

    private boolean isGroupRole(RoleAssignment roleAssignment) {
        return roleAssignment.getAttributes().containsKey(CASE_ACCESS_GROUP_ID);
    }

    protected AssignmentRequest getAssignmentRequest() {
        Map<String, AssignmentRequest> mapOfRequests;
        try {
            mapOfRequests = RoleAssignmentAssertIntegrationHelper.getMapOfRasRequests();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertEquals(1, mapOfRequests.size(), "Unexpected number of requests to RAS");
        return mapOfRequests.values().stream().findFirst().orElseThrow();
    }


    /**
     ** Transfer UserRefreshQueue record from DB to PRD wiremock then reset DB.
     *
     * @param newUserTest = if true, will delete the UserRefreshQueue record from DB to simulate new user scenario.
     *                   If false, will update the record in DB to simulate existing user scenario.
     */
    protected void stubPrdRefreshUserFromDb(boolean newUserTest) {
        UserRefreshQueueEntity userRefreshQueueEntity = userRefreshQueueRepository.findAll().getFirst();

        Map<String, String> userTemplateMap = new HashMap<>();
        userTemplateMap.put("[[USER_ID]]", userRefreshQueueEntity.getUserId());
        userTemplateMap.put("[[USER_UPDATED]]", LocalDate.now().format(DF) + "T00:00:00Z");
        userTemplateMap.put("[[ORG_ID]]", userRefreshQueueEntity.getOrganisationId());
        userTemplateMap.put("[[ORG_STATUS]]", userRefreshQueueEntity.getOrganisationStatus());
        userTemplateMap.put("[[ORG_UPDATED]]", LocalDate.now().format(DF) + "T00:00:00Z");
        userTemplateMap.put(
            "[[ORG_PROFILE_IDS]]",
            String.join("\",\"", userRefreshQueueEntity.getOrganisationProfileIds())
        );
        userTemplateMap.put("\"[[USER_ACCESS_TYPES]]\"", userRefreshQueueEntity.getAccessTypes());

        String user =
            jsonHelper.readJsonFromFile("/SchedulerTests/PrdRetrieveUsers/user_template.json", userTemplateMap);

        stubPrdRefreshUser(
            "{ \"users\": [" + user + "]"
                + ", \"moreAvailable\": false"
                + ", \"lastRecordInPage\": true"
                + "}",
            userRefreshQueueEntity.getUserId()
        );

        if (newUserTest) {
            userRefreshQueueRepository.delete(userRefreshQueueEntity);
        } else {
            // transfer is complete: so set queue values to a suitable before state
            userRefreshQueueEntity.setActive(false);
            userRefreshQueueEntity.setRetry(0);
            userRefreshQueueEntity.setRetryAfter(null);
            userRefreshQueueEntity.setAccessTypes("[]");
            userRefreshQueueEntity.setLastUpdated(LocalDateTime.now().minusMonths(6));
            userRefreshQueueRepository.save(userRefreshQueueEntity);
        }
    }

}