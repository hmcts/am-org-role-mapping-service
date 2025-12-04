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

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

@ExtendWith(MockitoExtension.class)
class DroolIacStaffOrgRoleMappingTest extends DroolBase {

    static Map<String, String> expectedRoleNameWorkTypesMap = new HashMap<>();

    static {
        expectedRoleNameWorkTypesMap.put("hearing-centre-admin", "hearing_work, upper_tribunal, routine_work, "
                + "review_case, bail_work, stf_24w_hearing_work, stf_24w_upper_tribunal, stf_24w_routine_work");
        expectedRoleNameWorkTypesMap.put("national-business-centre", "hearing_work, upper_tribunal, routine_work, "
                + "stf_24w_hearing_work, stf_24w_upper_tribunal, stf_24w_routine_work");
        expectedRoleNameWorkTypesMap.put("ctsc", "hearing_work, upper_tribunal, routine_work, "
                + "stf_24w_hearing_work, stf_24w_upper_tribunal, stf_24w_routine_work");
        expectedRoleNameWorkTypesMap.put("ctsc-team-leader", "hearing_work, upper_tribunal, routine_work, "
                + "stf_24w_hearing_work, stf_24w_upper_tribunal, stf_24w_routine_work");
        expectedRoleNameWorkTypesMap.put("senior-tribunal-caseworker", "hearing_work, routine_work, "
                + "decision_making_work, applications, stf_24w_hearing_work, stf_24w_routine_work, "
                + "stf_24w_decision_making_work, stf_24w_applications");
        expectedRoleNameWorkTypesMap.put("tribunal-caseworker", "hearing_work, routine_work, "
                + "decision_making_work, applications, stf_24w_hearing_work, stf_24w_routine_work, "
                + "stf_24w_decision_making_work, stf_24w_applications");
        expectedRoleNameWorkTypesMap.put("hmcts-ctsc", null);
        expectedRoleNameWorkTypesMap.put("hmcts-legal-operations", null);
        expectedRoleNameWorkTypesMap.put("hmcts-admin", null);
        expectedRoleNameWorkTypesMap.put("task-supervisor", null);
        expectedRoleNameWorkTypesMap.put("case-allocator", null);
    }


    @ParameterizedTest
    @CsvSource({
        "3,BFA1,'hmcts-admin,hearing-centre-admin',N,N",
        "3,BFA1,'hmcts-admin,hearing-centre-admin,task-supervisor',Y,N",
        "3,BFA1,'hmcts-admin,hearing-centre-admin,case-allocator',N,Y",
        "3,BFA1,'hmcts-admin,hearing-centre-admin,task-supervisor,case-allocator',Y,Y",
        "3,XYZ1,'',Y,Y",

        "4,BFA1,'hmcts-admin,hearing-centre-admin',N,N",
        "4,BFA1,'hmcts-admin,hearing-centre-admin,task-supervisor',Y,N",
        "4,BFA1,'hmcts-admin,hearing-centre-admin,case-allocator',N,Y",
        "4,BFA1,'hmcts-admin,hearing-centre-admin,task-supervisor,case-allocator',Y,Y",
        "4,XYZ1,'',Y,Y",

        "5,BFA1,'hmcts-admin,hearing-centre-admin',N,N",
        "5,BFA1,'hmcts-admin,hearing-centre-admin,task-supervisor',Y,N",
        "5,BFA1,'hmcts-admin,hearing-centre-admin,case-allocator',N,Y",
        "5,BFA1,'hmcts-admin,hearing-centre-admin,task-supervisor,case-allocator',Y,Y",
        "5,XYZ1,'',Y,Y",

        "6,BFA1,'hmcts-admin,national-business-centre',N,N",
        "6,BFA1,'hmcts-admin,national-business-centre,task-supervisor',Y,N",
        "6,BFA1,'hmcts-admin,national-business-centre,case-allocator',N,Y",
        "6,BFA1,'hmcts-admin,national-business-centre,task-supervisor,case-allocator',Y,Y",
        "6,XYZ1,'',Y,Y",

        "7,BFA1,'hmcts-admin,national-business-centre',N,N",
        "7,BFA1,'hmcts-admin,national-business-centre,task-supervisor',Y,N",
        "7,BFA1,'hmcts-admin,national-business-centre,case-allocator',N,Y",
        "7,BFA1,'hmcts-admin,national-business-centre,task-supervisor,case-allocator',Y,Y",
        "7,XYZ1,'',Y,Y",

        "8,BFA1,'hmcts-admin,national-business-centre',N,N",
        "8,BFA1,'hmcts-admin,national-business-centre,task-supervisor',Y,N",
        "8,BFA1,'hmcts-admin,national-business-centre,case-allocator',N,Y",
        "8,BFA1,'hmcts-admin,national-business-centre,task-supervisor,case-allocator',Y,Y",
        "8,XYZ1,'',Y,Y"
    })
    void shouldReturnIacAdminMappings(String roleId, String serviceCode, String expectedRoles,
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
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("IAC", true));

        //assertion
        String[] roleNames = StringUtils.isEmpty(expectedRoles) ? new String[0] : expectedRoles.split(",");
        assertEquals(roleNames.length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNames));
        roleAssignments.forEach(r -> {
            assertEquals(RoleCategory.ADMIN, r.getRoleCategory());
            assertEquals(RoleType.ORGANISATION, r.getRoleType());
            assertEquals(cap.getId(), r.getActorId());
        });

        roleAssignments.forEach(r -> {
            if (r.getRoleName().equals("hmcts-admin")) {
                assertNull(r.getAttributes().get("jurisdiction"));
                assertEquals(Classification.PRIVATE, r.getClassification());
                assertEquals(GrantType.BASIC, r.getGrantType());
            } else {
                assertEquals("IA", r.getAttributes().get("jurisdiction").asText());
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
        "9,BFA1,'hmcts-ctsc,ctsc,ctsc-team-leader',N,N",
        "9,BFA1,'hmcts-ctsc,ctsc,ctsc-team-leader,task-supervisor',Y,N",
        "9,BFA1,'hmcts-ctsc,ctsc,ctsc-team-leader,case-allocator',N,Y",
        "9,BFA1,'hmcts-ctsc,ctsc,ctsc-team-leader,task-supervisor,case-allocator',Y,Y",
        "9,XYZ1,'',Y,Y",

        "10,BFA1,'hmcts-ctsc,ctsc',N,N",
        "10,BFA1,'hmcts-ctsc,ctsc,task-supervisor',Y,N",
        "10,BFA1,'hmcts-ctsc,ctsc,case-allocator',N,Y",
        "10,BFA1,'hmcts-ctsc,ctsc,task-supervisor,case-allocator',Y,Y",
        "10,XYZ1,'',Y,Y"
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
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("IAC", true));

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
                assertEquals("IA", r.getAttributes().get("jurisdiction").asText());
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
        "1,BFA1,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations',N,N",
        "1,BFA1,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,task-supervisor',Y,N",
        "1,BFA1,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,case-allocator',N,Y",
        "1,BFA1,'senior-tribunal-caseworker,tribunal-caseworker,hmcts-legal-operations,task-supervisor,"
                + "case-allocator',Y,Y",
        "1,XYZ1,'',Y,Y",

        "2,BFA1,'tribunal-caseworker,hmcts-legal-operations',N,N",
        "2,BFA1,'tribunal-caseworker,hmcts-legal-operations,task-supervisor',Y,N",
        "2,BFA1,'tribunal-caseworker,hmcts-legal-operations,case-allocator',N,Y",
        "2,BFA1,'tribunal-caseworker,hmcts-legal-operations,task-supervisor,case-allocator',Y,Y",
        "2,XYZ1,'',Y,Y",
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
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("IAC", true));

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
                assertEquals("IA", r.getAttributes().get("jurisdiction").asText());
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
                buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction("IAC", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());
    }

}