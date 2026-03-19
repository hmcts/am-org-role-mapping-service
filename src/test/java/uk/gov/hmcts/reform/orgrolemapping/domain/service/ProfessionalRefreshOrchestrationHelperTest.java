package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypeRole;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileAccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileJurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RestructuredAccessTypes;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.OrganisationStatus;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
class ProfessionalRefreshOrchestrationHelperTest {

    private static final String ORG_PROFILE1 = "orgProfile1";
    private static final String ORG_PROFILE2 = "orgProfile2";
    private static final String ORG_PROFILE3 = "orgProfile3";
    private static final String ORG_PROFILE4 = "orgProfile4";
    private static final String JURISDICTION1 = "jurisdiction1";
    private static final String JURISDICTION2 = "jurisdiction2";
    private static final String JURISDICTION3 = "jurisdiction3";
    private static final String JURISDICTION4 = "jurisdiction4";


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

    /**
     * BUILD MAP: filter the user access types.
     */
    @Test
    void buildUserAccessTypeMapTest() throws JsonProcessingException {
        // GIVEN
        List<UserAccessType> userAccessTypes = List.of(buildUserAccessType(true), buildUserAccessType(false));
        List<String> organisationProfileIdsList = new ArrayList<>();
        userAccessTypes.forEach(userAccessType ->
            organisationProfileIdsList.add(userAccessType.getOrganisationProfileId())
        );
        // Add one to be ignored
        organisationProfileIdsList.add(ORG_PROFILE4);
        UserRefreshQueueEntity userRefreshQueue = UserRefreshQueueEntity.builder()
                .organisationProfileIds(organisationProfileIdsList.toArray(new String[0]))
                .accessTypes(JacksonUtils.writeValueAsPrettyJson(userAccessTypes))
                .build();

        // WHEN
        // Map = OrgamisationId, JurisdictionId, UserAccessType
        Map<String, Map<String, List<UserAccessType>>> results =
                professionalRefreshOrchestrationHelper.buildUserAccessTypeMap(
                        userRefreshQueue);

        // THEN
        assertNotNull(results);
        assertEquals(organisationProfileIdsList.size(), results.size());
        userAccessTypes.forEach(userAccessType -> {
            assertTrue(results.containsKey(userAccessType.getOrganisationProfileId()));
            assertTrue(results.get(userAccessType.getOrganisationProfileId())
                    .containsKey(userAccessType.getJurisdictionId()));
            assertTrue(results.get(userAccessType.getOrganisationProfileId())
                    .get(userAccessType.getJurisdictionId()).contains(userAccessType));
        });
    }

    /**
     * BUILD MAP: filter the organisation profile access types.
     */
    @ParameterizedTest
    @MethodSource("buildAccessTypeMapParams")
    void buildAccessTypeMapTest(Map<String, Map<String, List<UserAccessType>>> givenUserAccessMap,
                                RestructuredAccessTypes givenCcdAccessTypes,
                                Map<String, List<String>> expectedResults) {
        // WHEN
        // Map = OrgamisationProfileId, JurisdictionId, OrganisationProfileAccessType
        Map<String, Map<String, List<OrganisationProfileAccessType>>> actualResults =
            professionalRefreshOrchestrationHelper.buildAccessTypeMap(givenUserAccessMap, givenCcdAccessTypes);

        // THEN
        assertNotNull(actualResults);
        assertEquals(expectedResults.size(), actualResults.size(),
                "Incorrect number of organisation profiles in results");
        // Validate the expected results against the actual results
        expectedResults.forEach((organisationProfileId, jurisdictionIds) -> {
            assertTrue(actualResults.containsKey(organisationProfileId),
                    String.format("results should contain organisationProfileId %s", organisationProfileId));
            jurisdictionIds.forEach(jurisdictionId ->
                assertTrue(actualResults.get(organisationProfileId).containsKey(jurisdictionId),
                        String.format("results for organisationProfileId %s should contain jurisdictionId %s",
                                organisationProfileId, jurisdictionId))
            );
        });
        // Validate the actual results against the expected results
        actualResults.forEach((organisationProfileId, jurisdictionMap) -> {
            assertTrue(expectedResults.containsKey(organisationProfileId),
                    String.format("results contains unexpected organisationProfileId %s", organisationProfileId));
            jurisdictionMap.forEach((jurisdictionId, orgProfileAccessTypes) ->
                assertTrue(expectedResults.get(organisationProfileId).contains(jurisdictionId),
                        String.format("results for organisationProfileId %s contains unexpected jurisdictionId %s",
                                organisationProfileId, jurisdictionId))
            );
        });
    }

    public static Stream<Arguments> buildAccessTypeMapParams() {
        Map<String, Map<String, List<UserAccessType>>> givenUserAccessMap = Map.of(
                ORG_PROFILE1, Map.of(
                        JURISDICTION1, List.of(buildUserAccessType(true), buildUserAccessType(false)),
                        JURISDICTION2, List.of(buildUserAccessType(true))
                ),
                ORG_PROFILE2, Map.of(
                        JURISDICTION3, List.of(buildUserAccessType(false)),
                        JURISDICTION4, List.of(buildUserAccessType(false))
                ),
                ORG_PROFILE3, Collections.emptyMap()
        );
        Set<OrganisationProfile> givenOrganisationProfiles = Set.of(
                OrganisationProfile.builder()
                        .organisationProfileId(ORG_PROFILE1)
                        .jurisdictions(Set.of(
                            buildOrganisationProfileJurisdiction(JURISDICTION1)))
                        // Ignore jurisdiction2
                        .build(),
                OrganisationProfile.builder()
                        .organisationProfileId(ORG_PROFILE2)
                        .jurisdictions(Set.of(
                            buildOrganisationProfileJurisdiction(JURISDICTION3),
                            buildOrganisationProfileJurisdiction(JURISDICTION4)))
                        .build(),
                // Ignore orgProfile3
                OrganisationProfile.builder()
                        .organisationProfileId(ORG_PROFILE4)
                        .build()
        );
        return Stream.of(
                // Parameters: userAccessMap, organisationProfiles

                // Test1 - Ignore OrgProfile3 and Jurisdiction2. Find OrgProfile1 and OrgProfile2.
                Arguments.of(givenUserAccessMap, buildRestructuredAccessTypes(givenOrganisationProfiles),
                        Map.of(ORG_PROFILE1, List.of(JURISDICTION1),
                               ORG_PROFILE2, List.of(JURISDICTION3, JURISDICTION4))),
                // Test 2- Nothing in OrganisationProfiles, so everything should be ignored.
                Arguments.of(givenUserAccessMap, buildRestructuredAccessTypes(Collections.emptySet()),
                        Collections.emptyMap()),
                // Test 3 - Nothing in userAccessMap, so everything should be ignored.
                Arguments.of(Collections.emptyMap(), buildRestructuredAccessTypes(givenOrganisationProfiles),
                        Collections.emptyMap()),
                // Test 4 - Nothing in from userAccessMap / OrganisationProfiles, so everything should be ignored.
                Arguments.of(Collections.emptyMap(), buildRestructuredAccessTypes(Collections.emptySet()),
                        Collections.emptyMap())
        );
    }

    /**
     * RULE: access_type.access_mandatory = true.
     */
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void isAccessTypeMandatoryTest(boolean isMandatory) {
        OrganisationProfileAccessType orgProfileAccessType = OrganisationProfileAccessType.builder()
                .accessTypeId("accessType1")
                .accessMandatory(isMandatory)
                .build();
        boolean result = professionalRefreshOrchestrationHelper
                .isAccessTypeMandatory(orgProfileAccessType);
        assertEquals(isMandatory, result);
    }

    /**
     * RULE: user_access_type == null AND access_type.access_default = true.
     */
    @ParameterizedTest
    @MethodSource("isAccessTypeDefaultedParams")
    void isAccessTypeDefaultedTest(boolean isDefault, UserAccessType accessType,
                                   boolean expectedResult) {
        OrganisationProfileAccessType orgProfileAccessType = OrganisationProfileAccessType.builder()
                .accessTypeId("accessType1")
                .accessMandatory(false)
                .accessDefault(isDefault)
                .build();
        boolean result = professionalRefreshOrchestrationHelper
                .isAccessTypeDefaulted(orgProfileAccessType, accessType);
        assertEquals(expectedResult, result);
    }

    public static Stream<Arguments> isAccessTypeDefaultedParams() {
        UserAccessType enabledUserAccessType = buildUserAccessType(true);
        UserAccessType disabledUserAccessType = buildUserAccessType(false);
        return Stream.of(
                // isDefault, UserAccessTypes, expectedResult
                Arguments.of(true, enabledUserAccessType, false),
                Arguments.of(true, disabledUserAccessType, false),
                Arguments.of(true, null, true),
                Arguments.of(false, enabledUserAccessType, false),
                Arguments.of(false, disabledUserAccessType, false),
                Arguments.of(false, null, false)
        );
    }

    /**
     * RULE: user_access_type.enabled = true.
     */
    @ParameterizedTest
    @MethodSource("isAccessTypeEnabledParams")
    void isAccessTypeEnabledTest(UserAccessType accessType,
                                   boolean expectedResult) {
        boolean result = professionalRefreshOrchestrationHelper
                .isAccessTypeEnabled(accessType);
        assertEquals(expectedResult, result);
    }

    public static Stream<Arguments> isAccessTypeEnabledParams() {
        UserAccessType enabledUserAccessType = buildUserAccessType(true);
        UserAccessType disabledUserAccessType = buildUserAccessType(false);
        return Stream.of(
                // UserAccessTypes, expectedResult
                Arguments.of(enabledUserAccessType, true),
                Arguments.of(disabledUserAccessType, false),
                Arguments.of(null, false)
        );
    }

    @ParameterizedTest
    @MethodSource("isAccessTypeValidParams")
    void isAccessTypeValidTest(boolean isDefault, boolean isMandatory, UserAccessType accessType,
                               boolean expectedResult) {
        OrganisationProfileAccessType orgProfileAccessType = OrganisationProfileAccessType.builder()
                .accessTypeId("accessType1")
                .accessMandatory(isMandatory)
                .accessDefault(isDefault)
                .build();
        boolean result = professionalRefreshOrchestrationHelper
                .isAccessTypeValid(orgProfileAccessType, accessType);
        assertEquals(expectedResult, result);
    }

    public static Stream<Arguments> isAccessTypeValidParams() {
        UserAccessType enabledUserAccessType = buildUserAccessType(true);
        UserAccessType disabledUserAccessType = buildUserAccessType(false);
        return Stream.of(
                // isDefault, isMandatory, UserAccessTypes, expectedResult
                Arguments.of(true, true, enabledUserAccessType, true),
                Arguments.of(true, true, disabledUserAccessType, true),
                Arguments.of(true, true, null, true),

                Arguments.of(true, false, enabledUserAccessType, true),
                Arguments.of(true, false, disabledUserAccessType, false),
                Arguments.of(true, false, null, true),

                Arguments.of(false, true, enabledUserAccessType, true),
                Arguments.of(false, true, disabledUserAccessType, true),
                Arguments.of(false, true, null, true),

                Arguments.of(false, false, enabledUserAccessType, true),
                Arguments.of(false, false, disabledUserAccessType, false),
                Arguments.of(false, false, null, false)
        );
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
        refreshSingleUserTest(getGroupAccessTypes(true), 1);
    }

    @Test
    void shouldRefreshSingleUser_NoAccessTypes() {
        refreshSingleUserTest("[]", 0);
    }

    private void refreshSingleUserTest(String accessTypes, int expectedRolesCount) {
        AccessTypesEntity accessTypesEntity = AccessTypesEntity.builder()
                .version(1L)
                .accessTypes(accessTypes)
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
        assertEquals(expectedRolesCount, requestedRoles.size());
        assertEquals(expectedRolesCount, assignmentRequestArgumentCaptor.getValue().getRequestedRoles().size());
        if (expectedRolesCount > 0) {
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

    private static RestructuredAccessTypes buildRestructuredAccessTypes(Set<OrganisationProfile> organisationProfiles) {
        return RestructuredAccessTypes.builder()
                .organisationProfiles(organisationProfiles)
                .build();
    }

    private static UserAccessType buildUserAccessType(boolean isEnabled) {
        return UserAccessType.builder()
                .accessTypeId(UUID.randomUUID().toString())
                .organisationProfileId(UUID.randomUUID().toString())
                .jurisdictionId(UUID.randomUUID().toString())
                .enabled(isEnabled).build();
    }

    private static OrganisationProfileJurisdiction buildOrganisationProfileJurisdiction(String jurisdictionId) {
        return OrganisationProfileJurisdiction.builder()
                .jurisdictionId(jurisdictionId)
                .accessTypes(Set.of(
                        buildOrganisationProfileAccessType(true, false),
                        buildOrganisationProfileAccessType(false, true),
                        buildOrganisationProfileAccessType(false, false),
                        buildOrganisationProfileAccessType(true, true)))
                .build();
    }

    private static OrganisationProfileAccessType buildOrganisationProfileAccessType(
            boolean isDefault, boolean isMandatory) {
        return OrganisationProfileAccessType.builder()
                .accessTypeId(UUID.randomUUID().toString())
                .accessDefault(isDefault)
                .accessMandatory(isMandatory)
                .roles(Set.of(buildAccessTypeRole()))
                .build();
    }

    private static AccessTypeRole buildAccessTypeRole() {
        return AccessTypeRole.builder().organisationalRoleName(UUID.randomUUID().toString()).build();
    }
}
