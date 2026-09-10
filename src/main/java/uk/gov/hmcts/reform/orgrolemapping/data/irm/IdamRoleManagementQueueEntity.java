package uk.gov.hmcts.reform.orgrolemapping.data.irm;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.irm.IdamRecordType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleData;
import uk.gov.hmcts.reform.orgrolemapping.util.irm.IdamRoleDataJsonBConverter;

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
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", nullable = false, columnDefinition = "jsonb")
    @Convert(converter = IdamRoleDataJsonBConverter.class)
    private IdamRoleData data;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "last_published")
    private LocalDateTime lastPublished;

    @Column(name = "published_as")
    @Enumerated(EnumType.STRING)
    private IdamRecordType publishedAs;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "retry")
    private Integer retry;

    @Column(name = "retry_after")
    private LocalDateTime retryAfter;
}
