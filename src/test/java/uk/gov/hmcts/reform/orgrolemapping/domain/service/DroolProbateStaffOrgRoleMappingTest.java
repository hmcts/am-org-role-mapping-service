package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

@ExtendWith(MockitoExtension.class)
class DroolProbateStaffOrgRoleMappingTest extends DroolBase {

    static Map<String, String> expectedRoleNameWorkTypesMap = new HashMap<>();

    static {
        expectedRoleNameWorkTypesMap.put("tribunal-caseworker", "decision_making_work");
        expectedRoleNameWorkTypesMap.put("specific-access-approver-legal-ops", "access_requests");
        expectedRoleNameWorkTypesMap.put("ctsc", "applications, review_case, stopped_applications, "
                + "decision_making_work");
        expectedRoleNameWorkTypesMap.put("specific-access-approver-judiciary", "access_requests");
        expectedRoleNameWorkTypesMap.put("specific-access-approver-ctsc", "access_requests");
        expectedRoleNameWorkTypesMap.put("specific-access-approver-admin", "access_requests");
    }

    @ParameterizedTest
    @CsvSource({
        "9,ABA6,'ctsc-team-leader,ctsc,hmcts-ctsc,specific-access-approver-judiciary,specific-access-approver-ctsc,"
                + "specific-access-approver-admin',N,N",
        "9,ABA6,'ctsc-team-leader,ctsc,hmcts-ctsc,specific-access-approver-judiciary,specific-access-approver-ctsc,"
                + "specific-access-approver-admin,task-supervisor',Y,N",
        "9,ABA6,'ctsc-team-leader,ctsc,hmcts-ctsc,specific-access-approver-judiciary,specific-access-approver-ctsc,"
                + "specific-access-approver-admin,case-allocator',N,Y",
        "9,ABA6,'ctsc-team-leader,ctsc,hmcts-ctsc,specific-access-approver-judiciary,specific-access-approver-ctsc,"
                + "specific-access-approver-admin,task-supervisor,case-allocator',Y,Y",

        "10,ABA6,'ctsc,hmcts-ctsc',N,N",
        "10,ABA6,'ctsc,hmcts-ctsc,task-supervisor',Y,N",
        "10,ABA6,'ctsc,hmcts-ctsc,case-allocator',N,Y",
        "10,ABA6,'ctsc,hmcts-ctsc,task-supervisor,case-allocator',Y,Y"
    })
    void shouldReturnIacCtscMappings(String roleId, String serviceCode, String expectedRoles,
                                      String taskSupervisorFlag, String caseAllocatorFlag) {
        allProfiles.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(serviceCode);
        cap.setSuspended(false);
        cap.setRoleId(roleId);
        cap.setTaskSupervisorFlag(taskSupervisorFlag);
        cap.setCaseAllocatorFlag(caseAllocatorFlag);
        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("PROBATE", true));

        //assertion
        String[] roleNames = StringUtils.isEmpty(expectedRoles) ? new String[0] : expectedRoles.split(",");
        assertEquals(roleNames.length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNames));
        roleAssignments.forEach(r -> {
            assertEquals(RoleCategory.CTSC, r.getRoleCategory());
            assertEquals(RoleType.ORGANISATION, r.getRoleType());
            assertEquals(cap.getId(), r.getActorId());
        });

        roleAssignments.forEach(r -> {
            if (r.getRoleName().equals("hmcts-ctsc")) {
                assertNull(r.getAttributes().get("jurisdiction"));
                assertEquals(Classification.PRIVATE, r.getClassification());
                assertEquals(GrantType.BASIC, r.getGrantType());
            } else {
                assertEquals("PROBATE", r.getAttributes().get("jurisdiction").asText());
                assertEquals(Classification.PUBLIC, r.getClassification());
                assertEquals(GrantType.STANDARD, r.getGrantType());
            }
            assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
            if (expectedRoleNameWorkTypesMap.containsKey(r.getRoleName())) {
                String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());
                String actualWorkTypes = null;
                if (r.getAttributes().get("workTypes") != null) {
                    actualWorkTypes = r.getAttributes().get("workTypes").asText();
                }
                assertEquals(expectedWorkTypes, actualWorkTypes);
            } else {
                assertFalse(r.getAttributes().containsKey("workTypes"));
            }
        });
    }

    @ParameterizedTest
    @CsvSource({
        "1,ABA6,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations',N,N",
        "1,ABA6,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,task-supervisor',Y,N",
        "1,ABA6,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,case-allocator',N,Y",
        "1,ABA6,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,task-supervisor,case-allocator',Y,Y",

        "2,ABA6,'tribunal-caseworker,hmcts-legal-operations',N,N",
        "2,ABA6,'tribunal-caseworker,hmcts-legal-operations,task-supervisor',Y,N",
        "2,ABA6,'tribunal-caseworker,hmcts-legal-operations,case-allocator',N,Y",
        "2,ABA6,'tribunal-caseworker,hmcts-legal-operations,task-supervisor,case-allocator',Y,Y"
    })
    void shouldReturnIacLegalOpsMappings(String roleId, String serviceCode, String expectedRoles,
                                     String taskSupervisorFlag, String caseAllocatorFlag) {
        allProfiles.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(serviceCode);
        cap.setSuspended(false);
        cap.setRoleId(roleId);
        cap.setTaskSupervisorFlag(taskSupervisorFlag);
        cap.setCaseAllocatorFlag(caseAllocatorFlag);
        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("PROBATE", true));

        //assertion
        String[] roleNames = StringUtils.isEmpty(expectedRoles) ? new String[0] : expectedRoles.split(",");
        assertEquals(roleNames.length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNames));
        roleAssignments.forEach(r -> {
            assertEquals(RoleCategory.LEGAL_OPERATIONS, r.getRoleCategory());
            assertEquals(RoleType.ORGANISATION, r.getRoleType());
            assertEquals(cap.getId(), r.getActorId());
        });

        roleAssignments.forEach(r -> {
            if (r.getRoleName().equals("hmcts-legal-operations")) {
                assertNull(r.getAttributes().get("jurisdiction"));
                assertEquals(Classification.PRIVATE, r.getClassification());
                assertEquals(GrantType.BASIC, r.getGrantType());
            } else {
                assertEquals("PROBATE", r.getAttributes().get("jurisdiction").asText());
                assertEquals(Classification.PUBLIC, r.getClassification());
                assertEquals(GrantType.STANDARD, r.getGrantType());
            }
            assertEquals(cap.getPrimaryLocationId(), r.getAttributes().get("primaryLocation").asText());
            if (expectedRoleNameWorkTypesMap.containsKey(r.getRoleName())) {
                String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());
                String actualWorkTypes = null;
                if (r.getAttributes().get("workTypes") != null) {
                    actualWorkTypes = r.getAttributes().get("workTypes").asText();
                }
                assertEquals(expectedWorkTypes, actualWorkTypes);
            } else {
                assertFalse(r.getAttributes().containsKey("workTypes"));
            }
        });
    }

    @Test
    void shouldNotReturnOrgRolesForCaseWorker_with_suspendedProfile() {
        allProfiles.clear();
        IntStream.range(1, 10).forEach(roleId ->
                allProfiles.add(TestDataBuilder.buildUserAccessProfile(roleId + "", true)));

        allProfiles.forEach(userAccessProfile -> {
            userAccessProfile.setCaseAllocatorFlag("Y");
            userAccessProfile.setTaskSupervisorFlag("Y");
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("PROBATE", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

}