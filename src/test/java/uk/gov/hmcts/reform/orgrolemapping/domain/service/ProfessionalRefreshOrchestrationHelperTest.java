package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
class ProfessionalRefreshOrchestrationHelperTest {

    @Mock
    private UserRefreshQueueRepository userRefreshQueueRepository;
    @Mock
    private AccessTypesRepository accessTypesRepository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private RoleAssignmentService roleAssignmentService;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ProfessionalRefreshOrchestrationHelper professionalRefreshOrchestrationHelper;

    @Captor
    private ArgumentCaptor<AssignmentRequest> assignmentRequestArgumentCaptor;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldUpsertUserRefreshQueue() throws IOException {

        Optional<AccessTypesEntity> accessTypesEntity = Optional.of(new AccessTypesEntity());
        accessTypesEntity.get().setVersion(1L);
        doReturn(accessTypesEntity)
                .when(accessTypesRepository).findFirstByOrderByVersionDesc();

        UserRefreshQueueEntity userRefreshQueueEntity = new UserRefreshQueueEntity();
        userRefreshQueueEntity.setUserId("Uid1");
        userRefreshQueueEntity.setAccessTypesMinVersion(1L);

        doReturn(userRefreshQueueEntity)
                .when(userRefreshQueueRepository).findByUserId(any());

        List<UserAccessType> userAccessTypes = new ArrayList<>();
        UserAccessType userAccessType1 = UserAccessType.builder()
                .accessTypeId("1")
                .enabled(true)
                .jurisdictionId("jur1")
                .organisationProfileId("orgProf1")
                .build();
        userAccessTypes.add(userAccessType1);

        String userAccessTypesString = " [ { \"jurisdictionId\": \"jur1\", \"organisationProfileId\": \"orgProf1\", "
                + "\"accessTypeId\": \"1\", \"enabled\": true } ]";

        doReturn(userAccessTypesString)
                .when(objectMapper).writeValueAsString(userAccessTypes);

        LocalDateTime updated = LocalDateTime.now();
        OrganisationInfo org1 = OrganisationInfo.builder()
                .status(OrganisationStatus.ACTIVE)
                .organisationProfileIds(List.of("profileId1"))
                .organisationIdentifier("orgId1")
                .build();
        LocalDateTime deleted = LocalDateTime.now();
        RefreshUser user = RefreshUser.builder()
                .userAccessTypes(userAccessTypes)
                .lastUpdated(updated)
                .userIdentifier("uid1")
                .organisationInfo(org1)
                .dateTimeDeleted(deleted)
                .build();

        professionalRefreshOrchestrationHelper.upsertUserRefreshQueue(user);

        verify(userRefreshQueueRepository).upsert("uid1", updated, 1L, deleted, userAccessTypesString,
                "orgId1", "ACTIVE", "profileId1");
    }

    @Test
    void shouldProcessActiveUserRefreshQueueWithGroupAndOrgAccess() throws IOException {
        AccessTypesEntity accessTypesEntity = AccessTypesEntity.builder()
                .version(1L)
                .accessTypes(getGroupAndOrgAccessTypes(true))
                .build();

        String[] orgProfileIds = {"SOLICITOR_PROFILE"};
        UserRefreshQueueEntity userRefreshQueueEntity = UserRefreshQueueEntity.builder()
                .userId("Uid1")
                .accessTypesMinVersion(1L)
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
        assertEquals(2, assignmentRequestArgumentCaptor.getValue().getRequestedRoles().size());
        ArrayList<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.addAll(assignmentRequestArgumentCaptor.getValue().getRequestedRoles());
        Collections.sort(requestedRoles, new Comparator<RoleAssignment>() {
            @Override
            public int compare(RoleAssignment t1, RoleAssignment t2) {
                return t1.getRoleName().compareTo(t2.getRoleName());
            }
        });
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
    void shouldProcessActiveUserRefreshQueueWithOnlyGroupAccess() throws IOException {
        AccessTypesEntity accessTypesEntity = AccessTypesEntity.builder()
                .version(1L)
                .accessTypes(getGroupAccessTypes(true))
                .build();

        String[] orgProfileIds = {"SOLICITOR_PROFILE"};
        UserRefreshQueueEntity userRefreshQueueEntity = UserRefreshQueueEntity.builder()
                .userId("Uid1")
                .accessTypesMinVersion(1L)
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
        assertEquals(1, assignmentRequestArgumentCaptor.getValue().getRequestedRoles().size());
        ArrayList<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.addAll(assignmentRequestArgumentCaptor.getValue().getRequestedRoles());
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
    void shouldProcessActiveUserRefreshQueueWithOnlyOrgAccess() throws IOException {
        AccessTypesEntity accessTypesEntity = AccessTypesEntity.builder()
                .version(1L)
                .accessTypes(getOrganisationalAccessTypes())
                .build();

        String[] orgProfileIds = {"SOLICITOR_PROFILE"};
        UserRefreshQueueEntity userRefreshQueueEntity = UserRefreshQueueEntity.builder()
                .userId("Uid1")
                .accessTypesMinVersion(1L)
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
        assertEquals(1, assignmentRequestArgumentCaptor.getValue().getRequestedRoles().size());
        ArrayList<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.addAll(assignmentRequestArgumentCaptor.getValue().getRequestedRoles());
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
    void shouldProcessActiveUserRefreshQueueWithGroupAccessDisabled() throws IOException {
        AccessTypesEntity accessTypesEntity = AccessTypesEntity.builder()
                .version(1L)
                .accessTypes(getGroupAccessTypes(false))
                .build();

        String[] orgProfileIds = {"SOLICITOR_PROFILE"};
        UserRefreshQueueEntity userRefreshQueueEntity = UserRefreshQueueEntity.builder()
                .userId("Uid1")
                .accessTypesMinVersion(1L)
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
    void shouldRefreshSingleUser() throws IOException {
        AccessTypesEntity accessTypesEntity = AccessTypesEntity.builder()
                .version(1L)
                .accessTypes(getGroupAccessTypes(true))
                .build();

        String[] orgProfileIds = {"SOLICITOR_PROFILE"};
        UserRefreshQueueEntity userRefreshQueueEntity = UserRefreshQueueEntity.builder()
                .userId("Uid1")
                .accessTypesMinVersion(1L)
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
        assertEquals(1, assignmentRequestArgumentCaptor.getValue().getRequestedRoles().size());
        Iterator<RoleAssignment> it = assignmentRequestArgumentCaptor.getValue().getRequestedRoles().iterator();
        while (it.hasNext()) {
            RoleAssignment roleAssignment = it.next();
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
