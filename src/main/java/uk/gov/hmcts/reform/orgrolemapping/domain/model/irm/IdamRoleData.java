package uk.gov.hmcts.reform.orgrolemapping.domain.model.irm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdamRoleData {

    private String emailId;
    private String activeFlag;
    private String deletedFlag;
    private List<Role> roles;

    @Getter
    @Setter
    @NoArgsConstructor
    public class Role {
        private String roleName;
    }

}