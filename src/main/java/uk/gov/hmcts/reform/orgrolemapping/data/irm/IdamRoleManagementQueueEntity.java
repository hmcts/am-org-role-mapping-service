package uk.gov.hmcts.reform.orgrolemapping.data.irm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "idam_role_management_queue")
public class IdamRoleManagementQueueEntity {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_type", nullable = false)
    private String userType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", nullable = false)
    private String data;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "retry")
    private Integer retry;

    @Column(name = "retry_after")
    private LocalDateTime retryAfter;
}
