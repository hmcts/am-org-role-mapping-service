package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.Attributes;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.RoleName;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Jurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.crd.JobTitle;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DroolHrsRoleMappingTest extends DroolBase {

    static Stream<Arguments> hrsAdminScenarios() {
        return Stream.of(
            Arguments.of(
                JobTitle.HRS_TEAM_LEADER,
                null,
                List.of(
                    RoleName.HRS_TEAM_LEADER,
                    RoleName.HRS_SHARER,
                    RoleName.HRS_LISTENER
                )
            ),
            Arguments.of(
                JobTitle.HRS_SHARER,
                List.of("SKILL:HRS:AA"),
                List.of(
                    RoleName.HRS_SHARER
                )
            ),
            Arguments.of(
                JobTitle.HRS_SHARER,
                List.of("SKILL:ABA2:ManageScannedDocuments"),
                List.of() //Empty as no HRS skill code
            ),
            Arguments.of(
                JobTitle.HRS_LISTENER,
                List.of("SKILL:HRS:AA"),
                List.of(
                    RoleName.HRS_LISTENER
                )
            ),
            Arguments.of(
                JobTitle.HRS_LISTENER,
                List.of("SKILL:ABA2:ManageScannedDocuments"),
                List.of() //Empty as no HRS skill code
            )
        );
    }

    @ParameterizedTest
    @MethodSource({
        "hrsAdminScenarios"
    })
    void shouldReturnHrsRoleMappings(JobTitle jobTitle, List<String> skills, List<String> expectedRoles
    ) {
        assertHrsMappings(jobTitle, skills, expectedRoles);
    }

    private void assertHrsMappings(JobTitle jobTitle, List<String> skills, List<String> expectedRoles) {
        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();

        cap.setServiceCode(Jurisdiction.HRS.getServiceCodes().get(0));// NB: only 1 for HRS
        cap.setSuspended(false);
        cap.setRoleId(jobTitle.getRoleId());
        cap.setRoleName(jobTitle.getRoleName());
        cap.setTaskSupervisorFlag("N");
        cap.setCaseAllocatorFlag("N");
        cap.setRegionId("LDN");

        cap.setSkillCodes(skills);

        allProfiles.add(cap);

        List<RoleAssignment> roleAssignments =
            buildExecuteKieSession(
                getAllFeatureFlagsToggleByJurisdiction(
                    Jurisdiction.HRS.getName(),
                    true
                )
            );

        assertEquals(expectedRoles.size(), roleAssignments.size());

        List<String> actualRoles = roleAssignments.stream()
            .map(RoleAssignment::getRoleName)
            .sorted()
            .toList();

        assertEquals(expectedRoles.stream().sorted().toList(), actualRoles);

        roleAssignments.forEach(role -> {
            assertEquals(RoleCategory.ADMIN, role.getRoleCategory());
            assertEquals(RoleType.ORGANISATION, role.getRoleType());

            assertNotNull(role.getAttributes().get(Attributes.Name.JURISDICTION));
            assertNotNull(role.getAttributes().get(Attributes.Name.PRIMARY_LOCATION));
            assertEquals(
                Jurisdiction.HRS.getName(),
                role.getAttributes().get(Attributes.Name.JURISDICTION).asText()
            );
            assertNull(role.getAttributes().get(Attributes.Name.REGION));
            assertNull(role.getAttributes().get(Attributes.Name.WORK_TYPES));
            assertEquals(Classification.PUBLIC, role.getClassification());
            assertEquals(GrantType.STANDARD, role.getGrantType());
            assertFalse(role.isReadOnly());
        });
    }
}
