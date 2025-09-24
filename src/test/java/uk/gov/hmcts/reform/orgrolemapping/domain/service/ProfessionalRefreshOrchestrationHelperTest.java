package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.OrganisationStatus;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
class ProfessionalRefreshOrchestrationHelperTest {

    @Mock
    private UserRefreshQueueRepository userRefreshQueueRepository;

    @Mock
    private AccessTypesRepository accessTypesRepository;

    @Mock
    private RoleAssignmentService roleAssignmentService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ProfessionalRefreshOrchestrationHelper professionalRefreshOrchestrationHelper;

    @Captor
    private ArgumentCaptor<AssignmentRequest> assignmentRequestArgumentCaptor;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldUpsertUserRefreshQueue() throws JsonProcessingException {

        // GIVEN
        long accessTypesMinVersion = 1L;
        Optional<AccessTypesEntity> accessTypesEntity = Optional.of(new AccessTypesEntity());
        accessTypesEntity.get().setVersion(accessTypesMinVersion);
        doReturn(accessTypesEntity)
                .when(accessTypesRepository).findFirstByOrderByVersionDesc();

        List<UserAccessType> userAccessTypes = new ArrayList<>();
        UserAccessType userAccessType1 = UserAccessType.builder()
                .accessTypeId("accessType1")
                .enabled(true)
                .jurisdictionId("jur1")
                .organisationProfileId("orgProf1")
                .build();
        userAccessTypes.add(userAccessType1);

        String orgId = "orgId1";
        String orgProfileId = "profileId1";
        OrganisationStatus orgStatus = OrganisationStatus.ACTIVE;
        OrganisationInfo org1 = OrganisationInfo.builder()
                .status(orgStatus)
                .organisationProfileIds(List.of(orgProfileId))
                .organisationIdentifier(orgId)
                .build();

        String userId = "uid1";
        LocalDateTime updated = LocalDateTime.now().minusDays(1L);
        LocalDateTime deleted = LocalDateTime.now().minusDays(2L);
        RefreshUser refreshUser = RefreshUser.builder()
                .userAccessTypes(userAccessTypes)
                .lastUpdated(updated)
                .userIdentifier(userId)
                .organisationInfo(org1)
                .dateTimeDeleted(deleted)
                .build();

        // WHEN
        professionalRefreshOrchestrationHelper.upsertUserRefreshQueue(refreshUser);

        // THEN
        ArgumentCaptor<String> userAccessTypesStringCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRefreshQueueRepository).upsert(
            eq(userId), eq(updated), eq(accessTypesMinVersion), eq(deleted),
            userAccessTypesStringCaptor.capture(),
            eq(orgId), eq(orgStatus.name()), eq(orgProfileId));

        assertEquals(userAccessTypes, JacksonUtils.convertUserAccessTypes(userAccessTypesStringCaptor.getValue()));
    }

    @Test
    void shouldProcessActiveUserRefreshQueueWithGroupAndOrgAccess() {
        AccessTypesEntity accessTypesEntity = AccessTypesEntity.builder()
                .version(1L)
                .accessTypes(getGroupAndOrgAccessTypes(true))
                .build();

        String[] orgProfileIds = {"SOLICITOR_PROFILE"};
        UserRefreshQueueEntity userRefreshQueueEntity = UserRefreshQueueEntity.builder()
                .userId("Uid1")
                .accessTypesMinVersion(1)
                .organisationStatus("ACTIVE")
                .organisationProfileIds(orgProfileIds)
                .accessTypes(getUserAccessTypes())
                .organisationId("AA123BB")
                .build();
        Optional<UserRefreshQueueEntity> userRefreshQueueEntityOpt = Optional.of(userRefreshQueueEntity);

        doReturn(userRefreshQueueEntityOpt)
                .when(userRefreshQueueRepository).findFirstByActiveTrue();

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body("RoleAssignment"))
                .when(roleAssignmentService).createRoleAssignment(any());

        professionalRefreshOrchestrationHelper.processActiveUserRefreshQueue(accessTypesEntity);

        verify(roleAssignmentService).createRoleAssignment(assignmentRequestArgumentCaptor.capture());
        ArrayList<RoleAssignment> requestedRoles = new ArrayList<>(
            assignmentRequestArgumentCaptor.getValue().getRequestedRoles()
        );
        assertEquals(2, requestedRoles.size());
        requestedRoles.sort(Comparator.comparing(RoleAssignment::getRoleName));
        RoleAssignment roleAssignment = requestedRoles.get(0);
        assertEquals("Uid1", roleAssignment.getActorId());
        assertEquals("ORGANISATION", roleAssignment.getRoleType().name());
        assertEquals("CIVIL_Group_Role1", roleAssignment.getRoleName());
        assertEquals("RESTRICTED", roleAssignment.getClassification().name());
        assertEquals("STANDARD", roleAssignment.getGrantType().name());
        assertEquals("PROFESSIONAL", roleAssignment.getRoleCategory().name());
        assertEquals("CREATE_REQUESTED", roleAssignment.getStatus().name());
        assertEquals("CIVIL", roleAssignment.getAttributes().get("jurisdiction").asText());
        assertEquals("CIVIL_Case_TYPE", roleAssignment.getAttributes().get("caseType").asText());
        assertEquals("CIVIL_CaseType:[GrpRoleName1]:AA123BB", roleAssignment.getAttributes()
                .get("caseAccessGroupId").asText());

        roleAssignment = requestedRoles.get(1);
        assertEquals("Uid1", roleAssignment.getActorId());
        assertEquals("ORGANISATION", roleAssignment.getRoleType().name());
        assertEquals("CIVIL_Org_Role1", roleAssignment.getRoleName());
        assertEquals("RESTRICTED", roleAssignment.getClassification().name());
        assertEquals("STANDARD", roleAssignment.getGrantType().name());
        assertEquals("PROFESSIONAL", roleAssignment.getRoleCategory().name());
        assertEquals("CREATE_REQUESTED", roleAssignment.getStatus().name());
        assertEquals("CIVIL", roleAssignment.getAttributes().get("jurisdiction").asText());
        assertEquals("CIVIL_Case_TYPE", roleAssignment.getAttributes().get("caseType").asText());
    }

    @Test
    void shouldProcessActiveUserRefreshQueueWithOnlyGroupAccess() {
        AccessTypesEntity accessTypesEntity = AccessTypesEntity.builder()
                .version(1L)
                .accessTypes(getGroupAccessTypes(true))
                .build();

        String[] orgProfileIds = {"SOLICITOR_PROFILE"};
        UserRefreshQueueEntity userRefreshQueueEntity = UserRefreshQueueEntity.builder()
                .userId("Uid1")
                .accessTypesMinVersion(1)
                .organisationStatus("ACTIVE")
                .organisationId("AA123BB")
                .organisationProfileIds(orgProfileIds)
                .accessTypes(getUserAccessTypes())
                .organisationId("AAA123B")
                .build();
        Optional<UserRefreshQueueEntity> userRefreshQueueEntityOpt = Optional.of(userRefreshQueueEntity);

        doReturn(userRefreshQueueEntityOpt)
                .when(userRefreshQueueRepository).findFirstByActiveTrue();

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body("RoleAssignment"))
                .when(roleAssignmentService).createRoleAssignment(any());

        professionalRefreshOrchestrationHelper.processActiveUserRefreshQueue(accessTypesEntity);

        verify(roleAssignmentService).createRoleAssignment(assignmentRequestArgumentCaptor.capture());
        ArrayList<RoleAssignment> requestedRoles = new ArrayList<>(
            assignmentRequestArgumentCaptor.getValue().getRequestedRoles()
        );
        assertEquals(1, requestedRoles.size());
        RoleAssignment roleAssignment = requestedRoles.get(0);
        assertEquals("Uid1", roleAssignment.getActorId());
        assertEquals("ORGANISATION", roleAssignment.getRoleType().name());
        assertEquals("CIVIL_Group_Role1", roleAssignment.getRoleName());
        assertEquals("RESTRICTED", roleAssignment.getClassification().name());
        assertEquals("STANDARD", roleAssignment.getGrantType().name());
        assertEquals("PROFESSIONAL", roleAssignment.getRoleCategory().name());
        assertEquals("CREATE_REQUESTED", roleAssignment.getStatus().name());
        assertEquals("CIVIL", roleAssignment.getAttributes().get("jurisdiction").asText());
        assertEquals("CIVIL_Case_TYPE", roleAssignment.getAttributes().get("caseType").asText());
        assertEquals("CIVIL_CaseType:[GrpRoleName1]:AAA123B", roleAssignment.getAttributes()
                .get("caseAccessGroupId").asText());
    }

    @Test
    void shouldProcessActiveUserRefreshQueueWithOnlyOrgAccess() {
        AccessTypesEntity accessTypesEntity = AccessTypesEntity.builder()
                .version(1L)
                .accessTypes(getOrganisationalAccessTypes())
                .build();

        String[] orgProfileIds = {"SOLICITOR_PROFILE"};
        UserRefreshQueueEntity userRefreshQueueEntity = UserRefreshQueueEntity.builder()
                .userId("Uid1")
                .accessTypesMinVersion(1)
                .organisationStatus("ACTIVE")
                .organisationId("AA123BB")
                .organisationProfileIds(orgProfileIds)
                .accessTypes(getUserAccessTypes())
                .organisationId("AAA123B")
                .build();
        Optional<UserRefreshQueueEntity> userRefreshQueueEntityOpt = Optional.of(userRefreshQueueEntity);

        doReturn(userRefreshQueueEntityOpt)
                .when(userRefreshQueueRepository).findFirstByActiveTrue();

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body("RoleAssignment"))
                .when(roleAssignmentService).createRoleAssignment(any());

        professionalRefreshOrchestrationHelper.processActiveUserRefreshQueue(accessTypesEntity);

        verify(roleAssignmentService).createRoleAssignment(assignmentRequestArgumentCaptor.capture());
        ArrayList<RoleAssignment> requestedRoles = new ArrayList<>(
            assignmentRequestArgumentCaptor.getValue().getRequestedRoles()
        );
        assertEquals(1, requestedRoles.size());
        RoleAssignment roleAssignment = requestedRoles.get(0);
        assertEquals("Uid1", roleAssignment.getActorId());
        assertEquals("ORGANISATION", roleAssignment.getRoleType().name());
        assertEquals("CIVIL_Org_Role1", roleAssignment.getRoleName());
        assertEquals("RESTRICTED", roleAssignment.getClassification().name());
        assertEquals("STANDARD", roleAssignment.getGrantType().name());
        assertEquals("PROFESSIONAL", roleAssignment.getRoleCategory().name());
        assertEquals("CREATE_REQUESTED", roleAssignment.getStatus().name());
        assertEquals("CIVIL", roleAssignment.getAttributes().get("jurisdiction").asText());
        assertEquals("CIVIL_Case_TYPE", roleAssignment.getAttributes().get("caseType").asText());
    }

    @Test
    void shouldProcessActiveUserRefreshQueueWithGroupAccessDisabled() {
        AccessTypesEntity accessTypesEntity = AccessTypesEntity.builder()
                .version(1L)
                .accessTypes(getGroupAccessTypes(false))
                .build();

        String[] orgProfileIds = {"SOLICITOR_PROFILE"};
        UserRefreshQueueEntity userRefreshQueueEntity = UserRefreshQueueEntity.builder()
                .userId("Uid1")
                .accessTypesMinVersion(1)
                .organisationStatus("ACTIVE")
                .organisationId("AA123BB")
                .organisationProfileIds(orgProfileIds)
                .accessTypes(getUserAccessTypes())
                .build();
        Optional<UserRefreshQueueEntity> userRefreshQueueEntityOpt = Optional.of(userRefreshQueueEntity);

        doReturn(userRefreshQueueEntityOpt)
                .when(userRefreshQueueRepository).findFirstByActiveTrue();

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body("RoleAssignment"))
                .when(roleAssignmentService).createRoleAssignment(any());

        professionalRefreshOrchestrationHelper.processActiveUserRefreshQueue(accessTypesEntity);

        verify(roleAssignmentService).createRoleAssignment(assignmentRequestArgumentCaptor.capture());
        assertEquals(0, assignmentRequestArgumentCaptor.getValue().getRequestedRoles().size());
    }

    @Test
    void shouldRefreshSingleUser() {
        AccessTypesEntity accessTypesEntity = AccessTypesEntity.builder()
                .version(1L)
                .accessTypes(getGroupAccessTypes(true))
                .build();

        String[] orgProfileIds = {"SOLICITOR_PROFILE"};
        UserRefreshQueueEntity userRefreshQueueEntity = UserRefreshQueueEntity.builder()
                .userId("Uid1")
                .accessTypesMinVersion(1)
                .organisationStatus("ACTIVE")
                .organisationId("AA123BB")
                .organisationProfileIds(orgProfileIds)
                .accessTypes(getUserAccessTypes())
                .build();
        Optional<UserRefreshQueueEntity> userRefreshQueueEntityOpt = Optional.of(userRefreshQueueEntity);

        doReturn(userRefreshQueueEntityOpt)
                .when(userRefreshQueueRepository).findFirstByActiveTrue();

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body("RoleAssignment"))
                .when(roleAssignmentService).createRoleAssignment(any());

        professionalRefreshOrchestrationHelper.refreshSingleUser(userRefreshQueueEntity, accessTypesEntity);

        verify(roleAssignmentService).createRoleAssignment(assignmentRequestArgumentCaptor.capture());

        ArrayList<RoleAssignment> requestedRoles = new ArrayList<>(
            assignmentRequestArgumentCaptor.getValue().getRequestedRoles()
        );
        assertEquals(1, requestedRoles.size());
        assertEquals(1, assignmentRequestArgumentCaptor.getValue().getRequestedRoles().size());
        RoleAssignment roleAssignment = requestedRoles.get(0);
        assertEquals("Uid1", roleAssignment.getActorId());
        assertEquals("ORGANISATION", roleAssignment.getRoleType().name());
        assertEquals("CIVIL_Group_Role1", roleAssignment.getRoleName());
        assertEquals("RESTRICTED", roleAssignment.getClassification().name());
        assertEquals("STANDARD", roleAssignment.getGrantType().name());
        assertEquals("PROFESSIONAL", roleAssignment.getRoleCategory().name());
        assertEquals("CREATE_REQUESTED", roleAssignment.getStatus().name());
        assertEquals("CIVIL", roleAssignment.getAttributes().get("jurisdiction").asText());
        assertEquals("CIVIL_Case_TYPE", roleAssignment.getAttributes().get("caseType").asText());
    }

    private String getUserAccessTypes() {
        return "["
                + "{"
                + "    \"jurisdictionId\": \"CIVIL\","
                + "    \"organisationProfileId\": \"SOLICITOR_PROFILE\","
                + "    \"accessTypeId\": \"CIVIL_ACCESS_TYPE_ID\","
                + "    \"enabled\": true"
                + "  },"
                + "  {"
                + "    \"jurisdictionId\": \"IA\","
                + "    \"organisationProfileId\": \"SOLICITOR_PROFILE\","
                + "    \"accessTypeId\": \"IA_ACCESS_TYPE_ID\","
                + "    \"enabled\": false"
                + "  }"
                + "]";
    }

    private String getGroupAndOrgAccessTypes(boolean groupAccessEnabled) {
        return "{\"organisationProfiles\": [{\"jurisdictions\": [{\"accessTypes\": [{\"roles\": "
                + "["
                + "{\"caseTypeId\": \"CIVIL_Case_TYPE\","
                + "\"groupRoleName\": \"CIVIL_Group_Role1\", "
                + "\"groupAccessEnabled\": " + groupAccessEnabled + ", "
                + "\"caseGroupIdTemplate\": "
                + "\"CIVIL_CaseType:[GrpRoleName1]:$ORGID$\","
                + "\"organisationalRoleName\": \"CIVIL_Org_Role1\"}"
                + "], "
                + "\"accessTypeId\": \"CIVIL_ACCESS_TYPE_ID\", "
                + "\"accessDefault\": true, "
                + "\"accessMandatory\": true}],"
                + "\"jurisdictionId\": \"CIVIL\"}], "
                + "\"organisationProfileId\": \"SOLICITOR_PROFILE\"}]}";
    }

    private String getGroupAccessTypes(boolean groupAccessEnabled) {
        return "{\"organisationProfiles\": [{\"jurisdictions\": [{\"accessTypes\": [{\"roles\": "
                + "["
                + "{\"caseTypeId\": \"CIVIL_Case_TYPE\","
                + "\"groupRoleName\": \"CIVIL_Group_Role1\", "
                + "\"groupAccessEnabled\": " + groupAccessEnabled + ", "
                + "\"caseGroupIdTemplate\": \"CIVIL_CaseType:[GrpRoleName1]:$ORGID$\""
                + "}], "
                + "\"accessTypeId\": \"CIVIL_ACCESS_TYPE_ID\", "
                + "\"accessDefault\": true, "
                + "\"accessMandatory\": true}],"
                + "\"jurisdictionId\": \"CIVIL\"}], "
                + "\"organisationProfileId\": \"SOLICITOR_PROFILE\"}]}";
    }

    private String getOrganisationalAccessTypes() {
        return "{\"organisationProfiles\": [{\"jurisdictions\": [{\"accessTypes\": [{\"roles\": "
                + "["
                + "{\"caseTypeId\": \"CIVIL_Case_TYPE\","
                + "\"organisationalRoleName\": \"CIVIL_Org_Role1\"}"
                + "], "
                + "\"accessTypeId\": \"CIVIL_ACCESS_TYPE_ID\", "
                + "\"accessDefault\": true, "
                + "\"accessMandatory\": true}],"
                + "\"jurisdictionId\": \"CIVIL\"}], "
                + "\"organisationProfileId\": \"SOLICITOR_PROFILE\"}]}";
    }

}
