package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.buildUserAccessProfile3;

@RunWith(MockitoJUnitRunner.class)
public class DroolPrivateLawOtherGovDeptStaffRoleMappingTest extends DroolBase {

    static final String LD_FLAG = "privatelaw_wa_1_2";

    @ParameterizedTest
    @CsvSource({
            "TBC,ABA5,PRIVATELAW"
    })
    void shouldReturnListedHearingViewerCaseWorker_otherGovDept(String roleId, String serviceCode,
                                                                String jurisdiction) {
        allProfiles.add(buildUserAccessProfile3(serviceCode, roleId, ""));

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(getFeatureFlags(LD_FLAG, true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(2, roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals(RoleCategory.OTHER_GOV_DEPT, r.getRoleCategory());
            assertEquals(usersAccessProfiles.keySet().stream().iterator().next(), r.getActorId());
            assertEquals(jurisdiction, r.getAttributes().get("jurisdiction").asText());
            assertThat(r.getRoleName())
                    .matches(s -> Stream.of("listed-hearing-viewer", "caseworker-privatelaw-cafcass-cymru")
                            .anyMatch(s::contains));
        });
    }
}
