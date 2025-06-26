package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.buildUserAccessProfile3;

@ExtendWith(MockitoExtension.class)
class DroolPrivateLawOtherGovDeptStaffRoleMappingTest extends DroolBase {

    @ParameterizedTest
    @CsvSource({
        "18,ABA5,PRIVATELAW,caseworker-privatelaw-externaluser-viewonly"
    })
    void shouldReturnListedHearingViewerCaseWorker_otherGovDept(String roleId,
                                                                String serviceCode,
                                                                String jurisdiction,
                                                                String expectedRoles) {

        CaseWorkerAccessProfile cap = buildUserAccessProfile3(serviceCode, roleId, "");
        cap.setRegionId("1");

        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags(true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        MatcherAssert.assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        roleAssignments.forEach(r -> {
            assertEquals(RoleCategory.OTHER_GOV_DEPT, r.getRoleCategory());
            assertEquals(usersAccessProfiles.keySet().stream().iterator().next(), r.getActorId());
            assertEquals(jurisdiction, r.getAttributes().get("jurisdiction").asText());
            assertEquals(Classification.PUBLIC, r.getClassification());
            assertEquals(GrantType.STANDARD, r.getGrantType());

            if ("caseworker-privatelaw-externaluser-viewonly".equals(r.getRoleName())) {
                assertEquals("1", r.getAttributes().get("region").asText());
            }
        });
    }

    @Test
    void falsePrivateLawFlagTest() {

        CaseWorkerAccessProfile cap = buildUserAccessProfile3("ABA5", "18", "");
        cap.setRegionId("1");

        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags(false));

        //assertions
        assertTrue(roleAssignments.isEmpty());
    }

    List<FeatureFlag> getFeatureFlags(Boolean status) {
        return getAllFeatureFlagsToggleByJurisdiction("PRIVATELAW", status);
    }

}
