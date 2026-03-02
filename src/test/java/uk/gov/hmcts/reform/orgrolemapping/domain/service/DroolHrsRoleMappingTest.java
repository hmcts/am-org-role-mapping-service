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
                List.of(
                    RoleName.HRS_TEAM_LEADER,
                    RoleName.HRS_SHARER,
                    RoleName.HRS_LISTENER
                )
            )
        );
    }

    @ParameterizedTest
    @MethodSource("hrsAdminScenarios")
    void shouldReturnHrsAdminMappings(JobTitle jobTitle, List<String> expectedRoles) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        CaseWorkerAccessProfile cap = UserAccessProfileBuilder.buildUserAccessProfileForRoleId2();
        cap.setServiceCode(Jurisdiction.HRS.getServiceCodes().get(0)); // NB: only 1 for HRS
        cap.setSuspended(false);
        cap.setRoleId(jobTitle.getRoleId());
        cap.setRoleName(jobTitle.getRoleName());
        cap.setTaskSupervisorFlag("N");
        cap.setCaseAllocatorFlag("N");
        cap.setRegionId("LDN");

        allProfiles.add(cap);

        // Execute Kie session
        List<RoleAssignment> roleAssignments =
            buildExecuteKieSession(getAllFeatureFlagsToggleByJurisdiction(Jurisdiction.HRS.getName(), true));

        // assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.size(), roleAssignments.size());

        roleAssignments.forEach(r -> {
            assertTrue(expectedRoles.contains(r.getRoleName()));
            assertEquals(RoleCategory.ADMIN, r.getRoleCategory());
            assertEquals(RoleType.ORGANISATION, r.getRoleType());
        });

        roleAssignments.forEach(r -> {
            assertNotNull(r.getAttributes().get(Attributes.Name.JURISDICTION));
            assertNotNull(r.getAttributes().get(Attributes.Name.PRIMARY_LOCATION));

            // assert jurisdiction
            assertEquals(Jurisdiction.HRS.getName(), r.getAttributes().get(Attributes.Name.JURISDICTION).asText());
            // assert region
            assertNull(r.getAttributes().get(Attributes.Name.REGION));
            // assert work types
            assertNull(r.getAttributes().get(Attributes.Name.WORK_TYPES));
            // assert classification
            assertEquals(Classification.PUBLIC, r.getClassification());
            // assert grant type
            assertEquals(GrantType.STANDARD, r.getGrantType());
            // assert ReadOnly
            assertFalse(r.isReadOnly());
        });
    }

}
