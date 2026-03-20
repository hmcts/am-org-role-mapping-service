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
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

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

    /*

        Test scenarios from DTSAM-1239:

        |----------|---------|-----------|--------------|---------|----------|------------|
        | Scenario | Access  |   Access  | Group Access |   PRD   | Generate |  Generate  |
        |    No.   | Default | Mandatory |    Enabled   | Enabled | Org Role | Group Role |
        |----------|---------|-----------|--------------|---------|----------|------------|
        |     1    |   True  |    True   |      True    |   True  |   Yes *  |    Yes **  |
        |     2    |   True  |    True   |      True    |  False  |   Yes *  |    Yes **  |
        |     2a   |   True  |    True   |      True    |   Null  |   Yes *  |    Yes **  |
        |     3    |   True  |    True   |     False    |   True  |   Yes *  |     No     |
        |     4    |   True  |    True   |     False    |  False  |   Yes *  |     No     |
        |     4a   |   True  |    True   |     False    |   Null  |   Yes *  |     No     |
        |     5    |   True  |   False   |      True    |   True  |   Yes *  |    Yes **  |
        |     6    |   True  |   False   |      True    |  False  |    No    |     No     |
        |     6a   |   True  |   False   |      True    |   Null  |   Yes *  |    Yes **  |
        |     7    |   True  |   False   |     False    |   True  |   Yes *  |     No     |
        |     8    |   True  |   False   |     False    |  False  |    No    |     No     |
        |     8a   |   True  |   False   |     False    |   Null  |   Yes *  |     No     |
        |     9    |  False  |    True   |      True    |   True  |   Yes *  |    Yes **  |
        |    10    |  False  |    True   |      True    |  False  |   Yes *  |    Yes **  |
        |    10a   |  False  |    True   |      True    |   Null  |   Yes *  |    Yes **  |
        |    11    |  False  |    True   |     False    |   True  |   Yes *  |     No     |
        |    12    |  False  |    True   |     False    |  False  |   Yes *  |     No     |
        |    12a   |  False  |    True   |     False    |   Null  |   Yes *  |     No     |
        |    13    |  False  |   False   |      True    |   True  |   Yes *  |    Yes **  |
        |    14    |  False  |   False   |      True    |  False  |    No    |     No     |
        |    14a   |  False  |   False   |      True    |   Null  |    No    |     No     |
        |    15    |  False  |   False   |     False    |   True  |   Yes *  |     No     |
        |    16    |  False  |   False   |     False    |  False  |    No    |     No     |
        |    16a   |  False  |   False   |     False    |   Null  |    No    |     No     |
        |----------|---------|-----------|--------------|---------|----------|------------|

            * Yes - if organisational_role_name is not empty
           ** Yes - if group_role_name is not empty and case_group_id_template is not empty

     */

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

    //#region Assertion Helpers: DB Checks

    protected void assertTotalUserRefreshQueueEntitiesInDb(int expectedNumberOfRecords,
                                                           EndStatus endStatus) {
        var userRefreshQueueEntities = userRefreshQueueRepository.findAll();
        assertEquals(expectedNumberOfRecords, userRefreshQueueEntities.size(),
                "UserRefreshQueueEntity number of records mismatch");
        assertEquals(EndStatus.SUCCESS.equals(endStatus) ? 0 :
                        EndStatus.PARTIAL_SUCCESS.equals(endStatus) ? 1 : expectedNumberOfRecords,
                userRefreshQueueEntities.stream()
                        .filter(entity -> entity.getActive()).count(),
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

    protected void assertAssignmentRequest(boolean expectedOrganisationRole, boolean expectedGroupRole) {
        AssignmentRequest assignmentRequest = getAssignmentRequest();
        assertNotNull(assignmentRequest, "No AssignmentRequest found");
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
    }

    private void assertRoleAssignment(RoleAssignment roleAssignment, boolean isGroupRole) {
        String prefix = isGroupRole ? "Group" : "Operational";
        assertEquals(ActorIdType.IDAM, roleAssignment.getActorIdType(),
                prefix + " actor type mismatch");
        assertTrue(roleAssignment.getActorId().contains("USERX")
                || roleAssignment.getActorId().contains("USERY"),
                prefix + " actorId mismatch");
        assertEquals(isGroupRole ? "GroupRole1" : "OrgRole1", roleAssignment.getRoleName(),
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
                prefix + " enddTime mismatch");
        assertNull(roleAssignment.getNotes(),
                prefix + " notes mismatch");
        assertEquals(JacksonUtils.convertObjectIntoJsonNode("BEFTA_JURISDICTION_2"),
                        roleAssignment.getAttributes().get("jurisdiction"),
                prefix + " jurisdiction mismatch");
        assertEquals(JacksonUtils.convertObjectIntoJsonNode("FT_CaseAccessGroups"),
                roleAssignment.getAttributes().get("caseType"),
                prefix + " caseType mismatch");
        if (isGroupRole) {
            assertEquals(JacksonUtils.convertObjectIntoJsonNode("BEFTA_MASTER:ORG1"),
                    roleAssignment.getAttributes().get("caseAccessGroupId"),
                    prefix + " caseAccessGroupId mismatch");
        }
    }

    private boolean isGroupRole(RoleAssignment roleAssignment) {
        return roleAssignment.getAttributes().containsKey("caseAccessGroupId");
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

}