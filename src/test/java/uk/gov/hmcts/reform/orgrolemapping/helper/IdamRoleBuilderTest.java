package uk.gov.hmcts.reform.orgrolemapping.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleMapping;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IdamRoleBuilderTest {

    @Test
    void buildIdamRoleData_Judicial() {
        // GIVEN
        Map<String, Set<JudicialAccessProfile>> usersAccessProfiles =
                TestDataBuilder.buildJudicialAccessProfileMap();
        Map<String, RoleMapping> usersRoleMappings = new HashMap<>();
        usersAccessProfiles.forEach((userId, userAccessProfile) ->
                usersRoleMappings.put(userId, TestDataBuilder.buildRoleMapping()));

        // WHEN
        Map<String, IdamRoleData> results = IdamRoleBuilder.buildIdamRoleData(UserType.JUDICIAL,
                usersAccessProfiles,
                usersRoleMappings);

        // THEN
        assertNotNull(results);
        assertEquals(usersRoleMappings.size(), results.size());
        results.forEach((userId, idamRoleData) -> {
            assertNotNull(idamRoleData);
            assertNotNull(idamRoleData.getEmailId());
            assertNotNull(idamRoleData.getActiveFlag());
            assertNotNull(idamRoleData.getDeletedFlag());
            assertNotNull(idamRoleData.getRoles());
        });
    }

    @Test
    void buildIdamRoleData_CaseWorker() {
        // GIVEN
        Map<String, Set<CaseWorkerAccessProfile>> usersAccessProfiles =
                TestDataBuilder.buildUserAccessProfileMap(false, false);
        Map<String, RoleMapping> usersRoleMappings = new HashMap<>();
        usersAccessProfiles.forEach((userId, userAccessProfile) ->
                usersRoleMappings.put(userId, TestDataBuilder.buildRoleMapping()));

        // WHEN
        Map<String, IdamRoleData> results = IdamRoleBuilder.buildIdamRoleData(UserType.CASEWORKER,
                usersAccessProfiles,
                usersRoleMappings);

        // THEN
        assertNotNull(results);
        assertEquals(usersRoleMappings.size(), results.size());
        results.forEach((userId, idamRoleData) -> {
            assertNotNull(idamRoleData);
            assertNotNull(idamRoleData.getRoles());
        });
    }
}
