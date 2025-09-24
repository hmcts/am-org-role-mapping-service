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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
     *  accessDefault = N, accessMandatory = N, groupAccessEnabled = N, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nnn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_nnnn() throws JsonProcessingException {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  accessDefault = N, accessMandatory = N, groupAccessEnabled = N, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nnn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_nnny() throws JsonProcessingException {
        testCreateRoleAssignment(true, false);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nnn_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_nnny_no_rolenames() throws JsonProcessingException {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  accessDefault = Y, accessMandatory = N, groupAccessEnabled = N, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_ynn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_ynnn() throws JsonProcessingException {
        testCreateRoleAssignment(true, false);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_ynn_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_ynnn_no_rolenames() throws JsonProcessingException {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  accessDefault = Y, accessMandatory = Y, groupAccessEnabled = N, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_yyny() throws JsonProcessingException {
        testCreateRoleAssignment(true, false);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyn_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_yyny_no_rolenames() throws JsonProcessingException {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  accessDefault = Y, accessMandatory = Y, groupAccessEnabled = N, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_yynn() throws JsonProcessingException {
        testCreateRoleAssignment(true, false);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyn_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_yynn_no_rolenames() throws JsonProcessingException {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  accessDefault = N, accessMandatory = Y, groupAccessEnabled = N, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_nyny() throws JsonProcessingException {
        testCreateRoleAssignment(true, false);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyn_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_nyny_no_rolenames() throws JsonProcessingException {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  accessDefault = N, accessMandatory = Y, groupAccessEnabled = N, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyn.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_nynn() throws JsonProcessingException {
        testCreateRoleAssignment(true, false);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyn_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_nynn_no_rolenames() throws JsonProcessingException {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  accessDefault = N, accessMandatory = Y, groupAccessEnabled = Y, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyy.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_nyyy() throws JsonProcessingException {
        testCreateRoleAssignment(true, true);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyy_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_nyyy_no_rolenames() throws JsonProcessingException {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  accessDefault = N, accessMandatory = Y, groupAccessEnabled = Y, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyy.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_nyyn() throws JsonProcessingException {
        testCreateRoleAssignment(true, true);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nyy_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_nyyn_no_rolenames() throws JsonProcessingException {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  accessDefault = N, accessMandatory = N, groupAccessEnabled = Y, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_nnyy() throws JsonProcessingException {
        testCreateRoleAssignment(true, true);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nny_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_nnyy_no_rolenames() throws JsonProcessingException {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  accessDefault = N, accessMandatory = N, groupAccessEnabled = Y, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_nny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_nnyn() throws JsonProcessingException {
        testCreateRoleAssignment(false, false);
    }
    

    /**
     * accessDefault = Y, accessMandatory = Y, groupAccessEnabled = Y, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyy.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_yyyy() throws JsonProcessingException {
        testCreateRoleAssignment(true, true);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyy_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_yyyy_no_rolenames() throws JsonProcessingException {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  accessDefault = Y, accessMandatory = Y, groupAccessEnabled = Y, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yyy.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_yyyn() throws JsonProcessingException {
        testCreateRoleAssignment(true, true);
    }

    /**
     *  accessDefault = Y, accessMandatory = N, groupAccessEnabled = Y, PRDenabled = Y.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_ynyy() throws JsonProcessingException {
        testCreateRoleAssignment(true, true);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yny_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_enabled.sql"
    })
    void testCreateRole_ynyy_no_rolenames() throws JsonProcessingException {
        testCreateRoleAssignment(false, false);
    }

    /**
     *  accessDefault = Y, accessMandatory = N, groupAccessEnabled = Y, PRDenabled = N.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yny.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_ynyn() throws JsonProcessingException {
        testCreateRoleAssignment(true, true);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_accesstypes_yny_no_rolenames.sql",
        "classpath:sql/prm/user_refresh_queue/init_user_refresh_queue.sql",
        "classpath:sql/prm/user_refresh_queue/insert_userrefresh_disabled.sql"
    })
    void testCreateRole_ynyn_no_rolenames() throws JsonProcessingException {
        testCreateRoleAssignment(false, false);
    }

    abstract void testCreateRoleAssignment(boolean orgRole, boolean groupRole);

    //#region Assertion Helpers: DB Checks

    protected void assertResponse(ResponseEntity<Object> actualResponse) {
        assertNotNull(actualResponse);
        assertNotNull(actualResponse.getBody());
        assertEquals(actualResponse.getBody(), Map.of("Message", SUCCESS_ROLE_REFRESH));
    }

    protected void assertTotalUserRefreshQueueEntitiesInDb(int expectedNumberOfRecords,
                                                           EndStatus endStatus) {
        var userRefreshQueueEntities = userRefreshQueueRepository.findAll();
        assertEquals(expectedNumberOfRecords, userRefreshQueueEntities.size(),
                "UserRefreshQueueEntity number of records mismatch");
        assertEquals(EndStatus.SUCCESS.equals(endStatus) ? 0 : expectedNumberOfRecords,
                userRefreshQueueEntities.stream()
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
        assertEquals("USERX", roleAssignment.getActorId(),
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

    private AssignmentRequest getAssignmentRequest() {
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