package uk.gov.hmcts.reform.orgrolemapping.data.irm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "idam_role_management_config")
public class IdamRoleManagementConfigEntity {

    @Id
    @Column(name = "user_role", nullable = false)
    private String userRole;

    @Id
    @Column(name = "user_type", nullable = false)
    private String userType;

    @Column(name = "allow_delete_flag", nullable = false)
    private String allowDeleteFlag;
}
