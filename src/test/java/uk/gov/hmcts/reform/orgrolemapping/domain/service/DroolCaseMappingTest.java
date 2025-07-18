package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;

@ExtendWith(MockitoExtension.class)
class DroolCaseMappingTest extends DroolBase {

    private final String workTypes = "hearing_work, routine_work, decision_making_work, applications";


    @Test
    void shouldReturnZeroCaseWorkerWrongServiceCode() {

        allProfiles.forEach(userAccessProfile -> {
            if (userAccessProfile.getRoleId().equals("1")) {
                userAccessProfile.setServiceCode("BFA2");
            }
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());

    }

    @Test
    void shouldReturnZeroCaseWorkerWrongFlag() {

        allProfiles.forEach(userAccessProfile -> {
            userAccessProfile.setSuspended(true);
            if (userAccessProfile.getServiceCode().equals("BFA2")) {
                userAccessProfile.setServiceCode("BFA1");
            }
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());

    }

    @Test
    void shouldReturnOneCaseWorkerForNewRule() {

        allProfiles.forEach(userAccessProfile -> {
            if (userAccessProfile.getRoleId().equals("2")) {
                userAccessProfile.setServiceCode("BFA1");
            } else {
                userAccessProfile.setServiceCode("BFA2");
            }

        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2,roleAssignments.size());
        assertEquals("tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals("hmcts-legal-operations",roleAssignments.get(1).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(1).getRoleCategory());
        assertEquals(usersAccessProfiles.keySet().stream().skip(1).iterator().next(),
                roleAssignments.get(0).getActorId());
        assertEquals(workTypes,
                roleAssignments.get(0).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnCaseAllocatorForNewRule() {
        Iterator<CaseWorkerAccessProfile> profiles = allProfiles.iterator();
        List<String> skillCodes = List.of("IA", "test", "ctsc");
        while (profiles.hasNext()) {
            CaseWorkerAccessProfile userAccessProfile = profiles.next();
            if (userAccessProfile.getRoleId().equals("2")) {
                profiles.remove();
            }
            if (userAccessProfile.getRoleId().equals("1")) {
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("Y");
                userAccessProfile.setTaskSupervisorFlag("X");
                userAccessProfile.setSuspended(false);
            }
            userAccessProfile.setSkillCodes(skillCodes);
        }

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4,roleAssignments.size());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals(skillCodes,roleAssignments.get(0).getAuthorisations());
        assertEquals("tribunal-caseworker",roleAssignments.get(1).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(2).getRoleName());
        assertEquals("hmcts-legal-operations",roleAssignments.get(3).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(3).getRoleCategory());
        assertEquals(usersAccessProfiles.keySet().stream().iterator().next(),
                roleAssignments.get(0).getActorId());

        assertEquals(workTypes,
                roleAssignments.get(1).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnDoubleCaseAllocatorRolesForNewRule() {
        List<String> skillCodes = List.of("IA", "test", "ctsc");
        allProfiles.forEach(userAccessProfile -> {
            if (userAccessProfile.getRoleId().equals("2")) {
                userAccessProfile.setRoleId("1");
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("Y");
                userAccessProfile.setTaskSupervisorFlag("X");
                userAccessProfile.setSuspended(false);
            }
            if (userAccessProfile.getRoleId().equals("1")) {
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("Y");
                userAccessProfile.setTaskSupervisorFlag("X");
                userAccessProfile.setSuspended(false);
            }
            userAccessProfile.setSkillCodes(skillCodes);
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(8,roleAssignments.size());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(1).getRoleName());
        assertEquals(skillCodes,roleAssignments.get(1).getAuthorisations());
        assertEquals("tribunal-caseworker",roleAssignments.get(2).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(3).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(4).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(4).getRoleCategory());
        assertEquals("case-allocator",roleAssignments.get(5).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(5).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(6).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(6).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(7).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(7).getRoleCategory());
        assertThat(usersAccessProfiles).containsKey(roleAssignments.get(0).getActorId());
        assertEquals(workTypes,
                roleAssignments.get(2).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(3).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnTaskSupervisorForNewRule() {
        Iterator<CaseWorkerAccessProfile> profiles = allProfiles.iterator();
        while (profiles.hasNext()) {
            CaseWorkerAccessProfile userAccessProfile = profiles.next();
            if (userAccessProfile.getRoleId().equals("2")) {
                profiles.remove();
            }
            if (userAccessProfile.getRoleId().equals("1")) {
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("X");
                userAccessProfile.setTaskSupervisorFlag("Y");
                userAccessProfile.setSuspended(false);
            }
        }

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4,roleAssignments.size());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(1).getRoleName());
        assertEquals("task-supervisor",roleAssignments.get(2).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(2).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(3).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(3).getRoleCategory());
        assertEquals(usersAccessProfiles.keySet().stream().iterator().next(),
                roleAssignments.get(0).getActorId());

        assertEquals(workTypes,
                roleAssignments.get(1).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnDoubleTaskSupervisorForNewRule() {
        List<String> skillCodes = List.of("IA", "test", "ctsc");
        allProfiles.forEach(userAccessProfile -> {
            if (userAccessProfile.getRoleId().equals("2")) {
                userAccessProfile.setRoleId("1");
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("X");
                userAccessProfile.setTaskSupervisorFlag("Y");
                userAccessProfile.setSuspended(false);
            }
            if (userAccessProfile.getRoleId().equals("1")) {
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("X");
                userAccessProfile.setTaskSupervisorFlag("Y");
                userAccessProfile.setSuspended(false);
            }
            userAccessProfile.setSkillCodes(skillCodes);
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(8,roleAssignments.size());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(1).getRoleName());
        assertEquals(skillCodes,roleAssignments.get(0).getAuthorisations());
        assertEquals("tribunal-caseworker",roleAssignments.get(2).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(3).getRoleName());
        assertEquals("task-supervisor",roleAssignments.get(4).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(4).getRoleCategory());
        assertEquals("task-supervisor",roleAssignments.get(5).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(5).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(6).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(6).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(7).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(7).getRoleCategory());
        assertThat(usersAccessProfiles).containsKey(roleAssignments.get(0).getActorId());
        assertEquals(workTypes,
                roleAssignments.get(2).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(3).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnTribunalWorkerAndCaseAllocatorRolesForNewRule() {
        List<String> skillCodes = List.of("IA", "test", "ctsc");
        allProfiles.forEach(userAccessProfile -> {
            if (userAccessProfile.getRoleId().equals("2")) {
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("X");
                userAccessProfile.setTaskSupervisorFlag("Y");
                userAccessProfile.setSuspended(false);
            }
            if (userAccessProfile.getRoleId().equals("1")) {
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("Y");
                userAccessProfile.setTaskSupervisorFlag("X");
                userAccessProfile.setSuspended(false);
            }
            userAccessProfile.setSkillCodes(skillCodes);
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(7,roleAssignments.size());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals(skillCodes,roleAssignments.get(0).getAuthorisations());
        assertEquals("tribunal-caseworker",roleAssignments.get(1).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(2).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(3).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(3).getRoleCategory());
        assertEquals("task-supervisor",roleAssignments.get(4).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(4).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(5).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(5).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(6).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(6).getRoleCategory());
        assertEquals(usersAccessProfiles.keySet().stream().iterator().next(),
                roleAssignments.get(0).getActorId());
        assertThat(usersAccessProfiles).containsKey(roleAssignments.get(2).getActorId());

        assertEquals(workTypes,
                roleAssignments.get(1).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(2).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnTaskSupervisorCaseAllocatorForNewRule() {
        Iterator<CaseWorkerAccessProfile> profiles = allProfiles.iterator();
        while (profiles.hasNext()) {
            CaseWorkerAccessProfile userAccessProfile = profiles.next();
            if (userAccessProfile.getRoleId().equals("2")) {
                profiles.remove();
            }
            if (userAccessProfile.getRoleId().equals("1")) {
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("Y");
                userAccessProfile.setTaskSupervisorFlag("Y");
                userAccessProfile.setSuspended(false);
            }
        }

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5,roleAssignments.size());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(1).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(2).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(2).getRoleCategory());
        assertEquals("task-supervisor",roleAssignments.get(3).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(3).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(4).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(4).getRoleCategory());
        assertEquals(usersAccessProfiles.keySet().stream().iterator().next(),
                roleAssignments.get(0).getActorId());

        assertEquals(workTypes,
                roleAssignments.get(1).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnDoubleAllocatorAndSupervisorRolesForNewRule() {
        List<String> skillCodes = List.of("IA", "test", "ctsc");
        allProfiles.forEach(userAccessProfile -> {
            if (userAccessProfile.getRoleId().equals("2")) {
                userAccessProfile.setRoleId("1");
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("Y");
                userAccessProfile.setTaskSupervisorFlag("Y");
                userAccessProfile.setSuspended(false);
            }
            if (userAccessProfile.getRoleId().equals("1")) {
                userAccessProfile.setServiceCode("BFA1");
                userAccessProfile.setCaseAllocatorFlag("Y");
                userAccessProfile.setTaskSupervisorFlag("Y");
                userAccessProfile.setSuspended(false);
            }
            userAccessProfile.setSkillCodes(skillCodes);
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(10,roleAssignments.size());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(1).getRoleName());
        assertEquals(skillCodes,roleAssignments.get(1).getAuthorisations());
        assertEquals("tribunal-caseworker",roleAssignments.get(2).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(3).getRoleName());
        assertEquals("case-allocator",roleAssignments.get(4).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(4).getRoleCategory());
        assertEquals("case-allocator",roleAssignments.get(5).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(5).getRoleCategory());
        assertEquals("task-supervisor",roleAssignments.get(6).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(6).getRoleCategory());
        assertEquals("task-supervisor",roleAssignments.get(7).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(7).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(8).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(8).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(9).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(9).getRoleCategory());
        assertThat(usersAccessProfiles).containsKey(roleAssignments.get(0).getActorId());
        assertEquals(workTypes,
                roleAssignments.get(2).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(3).getAttributes().get("workTypes").asText());
    }

    @Test
    void shouldReturnBothCaseWorkerForNewRule() {
        List<String> skillCodes = List.of("IA", "test", "ctsc");
        allProfiles.forEach(userAccessProfile -> {
            if (userAccessProfile.getRoleId().equals("2")) {
                userAccessProfile.setServiceCode("BFA1");
            }
            userAccessProfile.setSkillCodes(skillCodes);
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5,roleAssignments.size());
        assertEquals("senior-tribunal-caseworker",roleAssignments.get(0).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(1).getRoleName());
        assertEquals("tribunal-caseworker",roleAssignments.get(2).getRoleName());
        assertEquals("hmcts-legal-operations",roleAssignments.get(3).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(3).getRoleCategory());
        assertEquals("hmcts-legal-operations",roleAssignments.get(4).getRoleName());
        assertEquals(RoleCategory.LEGAL_OPERATIONS,roleAssignments.get(4).getRoleCategory());
        assertThat(usersAccessProfiles).containsKey(roleAssignments.get(1).getActorId());
        Assertions.assertThat(usersAccessProfiles.keySet().stream()).contains(
                roleAssignments.get(2).getActorId());
        roleAssignments.forEach(r -> {
            if (!r.getRoleName().contains("hmcts")) {
                assertEquals(skillCodes,r.getAuthorisations());
            }
        });
        assertEquals(workTypes,
                roleAssignments.get(1).getAttributes().get("workTypes").asText());
        assertEquals(workTypes,
                roleAssignments.get(2).getAttributes().get("workTypes").asText());

    }

    @Test
    void shouldReturnZeroCaseWorkerWrongServiceCodeForNewRule() {

        allProfiles.forEach(userAccessProfile -> {
            if (userAccessProfile.getRoleId().equals("1")) {
                userAccessProfile.setServiceCode("BFA2");
            }
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());

    }

    @Test
    void shouldReturnZeroCaseWorkerWrongRoleIdForNewRule() {

        allProfiles.forEach(userAccessProfile -> {
            userAccessProfile.setRoleId("11");
            if (userAccessProfile.getServiceCode().equals("BFA2")) {
                userAccessProfile.setServiceCode("BFA1");
            }
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());

    }

    @Test
    void shouldReturnZeroCaseWorkerWrongFlagForNewRule() {

        allProfiles.forEach(userAccessProfile -> {
            userAccessProfile.setSuspended(true);
            if (userAccessProfile.getServiceCode().equals("BFA2")) {
                userAccessProfile.setServiceCode("BFA1");
            }
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags("iac_1_1", true));

        //assertion
        assertTrue(roleAssignments.isEmpty());

    }

}