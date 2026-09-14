package uk.gov.hmcts.reform.orgrolemapping.helper;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleMapping;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRole;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleDataRole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IdamRoleBuilder {

    public static <T> Map<String, IdamRoleData> buildIdamRoleData(
            UserType userType, Map<String, Set<T>> usersAccessProfiles,
            Map<String, RoleMapping> usersRoleMappings) {
        Map<String, IdamRoleData> results = new HashMap<>();
        // Loop the user role mappings and build the IdamRoleData for each user
        usersRoleMappings.forEach((userId, roleMapping) -> {
            results.put(userId,
                    getIdamRoleData(userType, usersAccessProfiles.get(userId),
                        roleMapping.getIdamRoles()));
        });
        return results;
    }

    private static <T> IdamRoleData getIdamRoleData(UserType userType,
                                                   Set<T> userAccessProfiles,
                                                   List<IdamRole> idamRoles) {
        if (userAccessProfiles.isEmpty()) {
            return new IdamRoleData();
        }
        T userAccessProfile = userAccessProfiles.iterator().next();
        if (UserType.JUDICIAL.equals(userType)) {
            return buildIdamJudicialRoleData((JudicialAccessProfile) userAccessProfile, idamRoles);
        }
        return buildIdamCaseWorkerRoleData((CaseWorkerAccessProfile) userAccessProfile, idamRoles);
    }

    private static IdamRoleData buildIdamJudicialRoleData(JudicialAccessProfile userAccessProfile,
                                                          List<IdamRole> idamRoles) {
        return IdamRoleData.builder()
                .emailId(userAccessProfile.getEmailId())
                .activeFlag(userAccessProfile.getActiveFlag())
                .deletedFlag("N")
                .roles(buildIdamRoleDataRoleList(idamRoles))
                .build();
    }

    private static IdamRoleData buildIdamCaseWorkerRoleData(CaseWorkerAccessProfile userAccessProfile,
                                                            List<IdamRole> idamRoles) {
        return IdamRoleData.builder()
                .deletedFlag("N")
                .roles(buildIdamRoleDataRoleList(idamRoles))
                .build();
    }

    private static List<IdamRoleDataRole> buildIdamRoleDataRoleList(List<IdamRole> idamRoles) {
        List<IdamRoleDataRole> roles = new ArrayList<>();
        idamRoles.forEach(idamRole ->
                roles.add(IdamRoleDataRole.builder()
                        .roleName(idamRole.getRoleName())
                        .build())
        );
        return roles;
    }
}
