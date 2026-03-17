package uk.gov.hmcts.reform.orgrolemapping.data.irm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;

import java.io.Serializable;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "idam_role_management_config")
@IdClass(IdamRoleManagementConfigEntity.CompositeKey.class)
public class IdamRoleManagementConfigEntity {

    @Id
    @Column(name = "role_name", nullable = false)
    private String roleName;

    @Id
    @Column(name = "user_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Column(name = "allow_delete_flag", nullable = false)
    private String allowDeleteFlag;

    @Getter
    @Setter
    public static class CompositeKey implements Serializable {
        private String roleName;
        private UserType userType;
    }
}
