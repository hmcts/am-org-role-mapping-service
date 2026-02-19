package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRole;

import java.util.List;


@Builder
@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoleMapping {
    List<RoleAssignment> roleAssignments;
    List<IdamRole> idamRoles;
}
