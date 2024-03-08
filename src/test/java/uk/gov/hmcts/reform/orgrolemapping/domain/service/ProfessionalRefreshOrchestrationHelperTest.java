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
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
                .status("status1")
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
                "orgId1", "status1", "profileId1");
    }

    @Test
    void shouldProcessActiveUserRefreshQueue() throws IOException {
        AccessTypesEntity accessTypesEntity = AccessTypesEntity.builder()
                .version(1L)
                .accessTypes(getAccessTypes())
                .build();

        String[] orgProfileIds = {"SOLICITOR_PROFILE"};
        UserRefreshQueueEntity userRefreshQueueEntity = UserRefreshQueueEntity.builder()
                .userId("Uid1")
                .accessTypesMinVersion(1L)
                .organisationStatus("ACTIVE")
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
        assertEquals(1, assignmentRequestArgumentCaptor.getValue().getRequestedRoles().size());
        Iterator<RoleAssignment> it = assignmentRequestArgumentCaptor.getValue().getRequestedRoles().iterator();
        while (it.hasNext()) {
            RoleAssignment roleAssignment = it.next();
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


    }

    private String getUserAccessTypes() throws IOException {
        return new String(
                Files.readAllBytes(Paths.get("src/main/resources/userAccessType.json")));
    }


    private String getAccessTypes() {
        return "{\"organisationProfiles\": [{\"jurisdictions\": [{\"accessTypes\": [{\"roles\": "
                + "["
                + "{\"caseTypeId\": \"CIVIL_Case_TYPE\","
                + "\"groupRoleName\": \"CIVIL_Group_Role1\", "
                + "\"groupAccessEnabled\": false, "
                + "\"caseGroupIdTemplate\": "
                + "\"CIVIL_CaseType:[GrpRoleName1]:$ORGID$\","
                + "\"organisationalRoleName\": \"CIVIL_Org_Role1\"}"
                + "], "
                + "\"accessTypeId\": \"CIVIL_ACCESS_TYPE_ID\", "
                + "\"accessDefault\": true, \"accessMandatory\": true}],"
                + "\"jurisdictionId\": \"CIVIL\"}], "
                + "\"organisationProfileId\": \"SOLICITOR_PROFILE\"}]}";
    }
}
