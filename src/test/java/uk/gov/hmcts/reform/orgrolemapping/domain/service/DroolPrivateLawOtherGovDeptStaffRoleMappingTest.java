package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.buildUserAccessProfile3;

@RunWith(MockitoJUnitRunner.class)
class DroolPrivateLawOtherGovDeptStaffRoleMappingTest extends DroolBase {

    static final String LD_FLAG = "privatelaw_wa_1_2";

    @ParameterizedTest
    @CsvSource({
            "18,ABA5,PRIVATELAW"
    })
    void shouldReturnListedHearingViewerCaseWorker_otherGovDept(String roleId,
                                                                String serviceCode,
                                                                String jurisdiction) {

        CaseWorkerAccessProfile cap = buildUserAccessProfile3(serviceCode, roleId, "");
        cap.setRegionId("1");

        allProfiles.add(cap);

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags(LD_FLAG, true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2, roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals(RoleCategory.OTHER_GOV_DEPT, r.getRoleCategory());
            assertEquals(usersAccessProfiles.keySet().stream().iterator().next(), r.getActorId());
            assertEquals(jurisdiction, r.getAttributes().get("jurisdiction").asText());
            assertEquals(Classification.PUBLIC, r.getClassification());
            assertEquals(GrantType.STANDARD, r.getGrantType());
            assertThat(r.getRoleName())
                    .matches(s -> Stream.of("listed-hearing-viewer", "caseworker-privatelaw-externaluser-viewonly")
                            .anyMatch(s::contains));

            if ("caseworker-privatelaw-externaluser-viewonly".equals(r.getRoleName())) {
                assertEquals("1", r.getAttributes().get("region").asText());
            }
        });
    }
}
