package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.Attributes;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.RoleName;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Jurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.WorkType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.crd.JobTitle;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.PRIMARY_LOCATION_ID;

@Slf4j
@ExtendWith(MockitoExtension.class)
class DroolPossessionsEnforcementRoleMappingTest extends DroolBase {

    private static final String REGION_ID = "LDN";

    private static final Map<String, JsonNode> STANDARD_ATTRIBUTES = Map.of(
        Attributes.Name.JURISDICTION, JacksonUtils.convertObjectIntoJsonNode(Jurisdiction.POSSESSIONS.getName()),
        Attributes.Name.PRIMARY_LOCATION, JacksonUtils.convertObjectIntoJsonNode(PRIMARY_LOCATION_ID),
        Attributes.Name.REGION, JacksonUtils.convertObjectIntoJsonNode(REGION_ID));


    private static final Map<String, JsonNode> BAILIFF_ATTRIBUTES = Map.of(
            Attributes.Name.JURISDICTION, JacksonUtils.convertObjectIntoJsonNode(Jurisdiction.POSSESSIONS.getName()),
            Attributes.Name.PRIMARY_LOCATION, JacksonUtils.convertObjectIntoJsonNode(PRIMARY_LOCATION_ID),
            Attributes.Name.REGION, JacksonUtils.convertObjectIntoJsonNode(REGION_ID),
            Attributes.Name.WORK_TYPES, JacksonUtils.convertObjectIntoJsonNode(
                    WorkType.joinValues(
                            WorkType.BAILIFF_WORK, WorkType.ENFORCEMENT_SUPPORT, WorkType.ERROR_MANAGEMENT)));

    @Test
    void doesNotAssignRolesForBailiffManager_suspended() {
        CaseWorkerAccessProfile cap = createCaseWorkerAccessProfile(
                JobTitle.BAILIFF_MANAGER, true, true, true);
        List<RoleAssignment> roleAssignments = calculateRoleAssignments(cap, FeatureFlagEnum.POSSESSIONS_WA_1_1);

        assertEquals(0, roleAssignments.size());
    }

    @Test
    void doesNotAssignRolesForBailiffManager_wrong_serviceCode() {
        CaseWorkerAccessProfile cap = createCaseWorkerAccessProfile(
                JobTitle.BAILIFF_MANAGER, true, true, false);
        cap.setServiceCode("AAA2");

        List<RoleAssignment> roleAssignments = calculateRoleAssignments(cap, FeatureFlagEnum.POSSESSIONS_WA_1_1);

        assertEquals(0, roleAssignments.size());
    }

    @Test
    void assignsRolesForBailiffManager_TaskSupervisor_and_CaseAllocator() {
        CaseWorkerAccessProfile cap = createCaseWorkerAccessProfile(
                JobTitle.BAILIFF_MANAGER, true, true, false);
        List<RoleAssignment> roleAssignments = calculateRoleAssignments(cap, FeatureFlagEnum.POSSESSIONS_WA_1_1);

        assertEquals(5, roleAssignments.size());
        assertThat(roleAssignments, containsInAnyOrder(
                createRoleAssignment(
                        cap.getId(),
                        RoleName.BAILIFF_MANAGER,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        STANDARD_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.BAILIFF,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        BAILIFF_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.TASK_SUPERVISOR,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        STANDARD_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.CASE_ALLOCATOR,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        STANDARD_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.HMCTS_ENFORCEMENT,
                        GrantType.BASIC,
                        Classification.PRIVATE,
                        true,
                        Map.of())));
    }

    @Test
    void assignsRolesForBailiffManager_CaseAllocator_not_TaskSupervisor() {
        CaseWorkerAccessProfile cap = createCaseWorkerAccessProfile(
                JobTitle.BAILIFF_MANAGER, false, true, false);
        List<RoleAssignment> roleAssignments = calculateRoleAssignments(cap, FeatureFlagEnum.POSSESSIONS_WA_1_1);

        assertEquals(4, roleAssignments.size());
        assertThat(roleAssignments, containsInAnyOrder(
                createRoleAssignment(
                        cap.getId(),
                        RoleName.BAILIFF_MANAGER,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        STANDARD_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.BAILIFF,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        BAILIFF_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.CASE_ALLOCATOR,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        STANDARD_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.HMCTS_ENFORCEMENT,
                        GrantType.BASIC,
                        Classification.PRIVATE,
                        true,
                        Map.of())));
    }

    @Test
    void assignsRolesForBailiffManager_TaskSupervisor_not_CaseAllocator() {
        CaseWorkerAccessProfile cap = createCaseWorkerAccessProfile(
                JobTitle.BAILIFF_MANAGER, true, false, false);
        List<RoleAssignment> roleAssignments = calculateRoleAssignments(cap, FeatureFlagEnum.POSSESSIONS_WA_1_1);

        assertEquals(4, roleAssignments.size());
        assertThat(roleAssignments, containsInAnyOrder(
                createRoleAssignment(
                        cap.getId(),
                        RoleName.BAILIFF_MANAGER,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        STANDARD_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.BAILIFF,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        BAILIFF_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.TASK_SUPERVISOR,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        STANDARD_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.HMCTS_ENFORCEMENT,
                        GrantType.BASIC,
                        Classification.PRIVATE,
                        true,
                        Map.of())));
    }

    @Test
    void assignsRolesForBailiffManager_not_CaseAllocator_not_TaskSupervisor() {
        CaseWorkerAccessProfile cap = createCaseWorkerAccessProfile(
                JobTitle.BAILIFF_MANAGER, false, false, false);
        List<RoleAssignment> roleAssignments = calculateRoleAssignments(cap, FeatureFlagEnum.POSSESSIONS_WA_1_1);

        assertEquals(3, roleAssignments.size());
        assertThat(roleAssignments, containsInAnyOrder(
                createRoleAssignment(
                        cap.getId(),
                        RoleName.BAILIFF_MANAGER,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        STANDARD_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.BAILIFF,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        BAILIFF_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.HMCTS_ENFORCEMENT,
                        GrantType.BASIC,
                        Classification.PRIVATE,
                        true,
                        Map.of())));
    }


    @Test
    void assignsRolesForBailiff_TaskSupervisor_and_CaseAllocator() {
        CaseWorkerAccessProfile cap = createCaseWorkerAccessProfile(
                JobTitle.BAILIFF, true, true, false);
        List<RoleAssignment> roleAssignments = calculateRoleAssignments(cap, FeatureFlagEnum.POSSESSIONS_WA_1_1);

        assertEquals(4, roleAssignments.size());
        assertThat(roleAssignments, containsInAnyOrder(
                createRoleAssignment(
                        cap.getId(),
                        RoleName.BAILIFF,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        BAILIFF_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.TASK_SUPERVISOR,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        STANDARD_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.CASE_ALLOCATOR,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        STANDARD_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.HMCTS_ENFORCEMENT,
                        GrantType.BASIC,
                        Classification.PRIVATE,
                        true,
                        Map.of())));
    }

    @Test
    void assignsRolesForBailiff_CaseAllocator_not_TaskSupervisor() {
        CaseWorkerAccessProfile cap = createCaseWorkerAccessProfile(
                JobTitle.BAILIFF, false, true, false);
        List<RoleAssignment> roleAssignments = calculateRoleAssignments(cap, FeatureFlagEnum.POSSESSIONS_WA_1_1);

        assertEquals(3, roleAssignments.size());
        assertThat(roleAssignments, containsInAnyOrder(
                createRoleAssignment(
                        cap.getId(),
                        RoleName.BAILIFF,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        BAILIFF_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.CASE_ALLOCATOR,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        STANDARD_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.HMCTS_ENFORCEMENT,
                        GrantType.BASIC,
                        Classification.PRIVATE,
                        true,
                        Map.of())));
    }

    @Test
    void assignsRolesForBailiff_TaskSupervisor_not_CaseAllocator() {
        CaseWorkerAccessProfile cap = createCaseWorkerAccessProfile(
                JobTitle.BAILIFF, true, false, false);
        List<RoleAssignment> roleAssignments = calculateRoleAssignments(cap, FeatureFlagEnum.POSSESSIONS_WA_1_1);

        assertEquals(3, roleAssignments.size());
        assertThat(roleAssignments, containsInAnyOrder(
                createRoleAssignment(
                        cap.getId(),
                        RoleName.BAILIFF,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        BAILIFF_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.TASK_SUPERVISOR,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        STANDARD_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.HMCTS_ENFORCEMENT,
                        GrantType.BASIC,
                        Classification.PRIVATE,
                        true,
                        Map.of())));
    }

    @Test
    void assignsRolesForBailiff_not_CaseAllocator_not_TaskSupervisor() {
        CaseWorkerAccessProfile cap = createCaseWorkerAccessProfile(
                JobTitle.BAILIFF, false, false, false);
        List<RoleAssignment> roleAssignments = calculateRoleAssignments(cap, FeatureFlagEnum.POSSESSIONS_WA_1_1);

        assertEquals(2, roleAssignments.size());
        assertThat(roleAssignments, containsInAnyOrder(
                createRoleAssignment(
                        cap.getId(),
                        RoleName.BAILIFF,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        BAILIFF_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.HMCTS_ENFORCEMENT,
                        GrantType.BASIC,
                        Classification.PRIVATE,
                        true,
                        Map.of())));
    }

    @Test
    void assignsRolesForBailiff_ServiceCode_AAA1() {
        CaseWorkerAccessProfile cap = createCaseWorkerAccessProfile(
                JobTitle.BAILIFF, true, true, false);
        cap.setServiceCode("AAA1");

        List<RoleAssignment> roleAssignments = calculateRoleAssignments(cap, FeatureFlagEnum.POSSESSIONS_WA_1_1);

        assertEquals(4, roleAssignments.size());
        assertThat(roleAssignments, containsInAnyOrder(
                createRoleAssignment(
                        cap.getId(),
                        RoleName.BAILIFF,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        BAILIFF_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.TASK_SUPERVISOR,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        STANDARD_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.CASE_ALLOCATOR,
                        GrantType.STANDARD,
                        Classification.PUBLIC,
                        false,
                        STANDARD_ATTRIBUTES),
                createRoleAssignment(
                        cap.getId(),
                        RoleName.HMCTS_ENFORCEMENT,
                        GrantType.BASIC,
                        Classification.PRIVATE,
                        true,
                        Map.of())
        ));
    }

    @Test
    void doesNotAssignRolesForBailiff_wrong_JobTitle_and_wrong_featureFlag() {
        CaseWorkerAccessProfile cap = createCaseWorkerAccessProfile(
                JobTitle.BAILIFF_ADMIN, true, true, false);

        List<RoleAssignment> roleAssignments = calculateRoleAssignments(cap, FeatureFlagEnum.POSSESSIONS_WA_1_0);

        assertFalse(roleAssignments.isEmpty());
        assertFalse(roleAssignments.stream().anyMatch(ra ->
                ra.getRoleName().equals(RoleName.BAILIFF)
                        || ra.getRoleName().equals(RoleName.BAILIFF_MANAGER)
                        || ra.getRoleName().equals(RoleName.HMCTS_ENFORCEMENT)));
    }

    private CaseWorkerAccessProfile createCaseWorkerAccessProfile(
            JobTitle jobTitle, boolean isTaskSupervisor, boolean isCaseAllocator, boolean isSuspended) {
        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId5();
        cap.setRoleId(jobTitle.getRoleId());
        cap.setRoleName(jobTitle.getRoleName());
        cap.setTaskSupervisorFlag(isTaskSupervisor ? "Y" : "N");
        cap.setCaseAllocatorFlag(isCaseAllocator ? "Y" : "N");
        cap.setSuspended(isSuspended);
        cap.setServiceCode(Jurisdiction.POSSESSIONS.getServiceCodes().getFirst());
        cap.setRegionId(REGION_ID);
        return cap;
    }

    private static RoleAssignment createRoleAssignment(
            String actorId,
            String roleName,
            GrantType grantType,
            Classification classification,
            boolean isReadOnly,
            Map<String, JsonNode> attributes) {
        return RoleAssignment.builder()
                .actorId(actorId)
                .roleName(roleName)
                .grantType(grantType)
                .classification(classification)
                .readOnly(isReadOnly)
                .attributes(attributes)
                .actorIdType(ActorIdType.IDAM)
                .roleCategory(RoleCategory.ENFORCEMENT)
                .roleType(RoleType.ORGANISATION)
                .build();
    }

    private List<RoleAssignment> calculateRoleAssignments(
            CaseWorkerAccessProfile cap, FeatureFlagEnum featureFlagEnum) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();
        allProfiles.clear();

        allProfiles.add(cap);

        List<FeatureFlag> featureFlags = getFeatureFlags(featureFlagEnum.getValue(), true);

        List<RoleAssignment> roleAssignments =
            buildExecuteKieSession(featureFlags);

        log.info("Returned possession roles: {}",
                roleAssignments.stream().map(RoleAssignment::getRoleName).toList());

        return roleAssignments;
    }
}
