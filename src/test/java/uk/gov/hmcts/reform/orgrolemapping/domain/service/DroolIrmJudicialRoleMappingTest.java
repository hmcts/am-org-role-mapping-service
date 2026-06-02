package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialOfficeHolder;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
@Slf4j
class DroolIrmJudicialRoleMappingTest extends DroolBase {

    static Map<String, String> expectedRoleNameWorkTypesMap = new HashMap<>();

    static {
        expectedRoleNameWorkTypesMap.put("caseworker-ras-validation", null);
    }

    @Test
    void shouldReturnIrmMappings() {

        List<JudicialOfficeHolder> outputJoh = new ArrayList<>();

        // Execute Kie session
        RoleMapping roleMappings = buildExecuteKieSession(setFeatureFlags(), outputJoh);

        // verify the roles
        roleMappings.getRoleAssignments().forEach(r -> {
            assertNotNull(r.getRoleName());
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

    private List<FeatureFlag> setFeatureFlags() {
        return getAllFeatureFlagsToggleByJurisdiction("SSCS", true, false);
    }
}
