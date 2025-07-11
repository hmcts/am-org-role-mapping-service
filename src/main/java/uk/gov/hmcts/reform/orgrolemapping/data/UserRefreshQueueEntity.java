package uk.gov.hmcts.reform.orgrolemapping.data;

import java.sql.Types;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "user_refresh_queue")
public class UserRefreshQueueEntity {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_last_updated", nullable = false)
    private LocalDateTime userLastUpdated;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "access_types_min_version", nullable = false)
    private Integer accessTypesMinVersion;

    @Column(name = "deleted")
    private LocalDateTime deleted;

    @Column(name = "access_types", nullable = false)
    private String accessTypes;

    @Column(name = "organisation_id", nullable = false)
    private String organisationId;

    @Column(name = "organisation_status", nullable = false)
    private String organisationStatus;

    @JdbcTypeCode(Types.ARRAY)
    @Column(name = "organisation_profile_ids", columnDefinition = "text[]", nullable = false)
    private String[] organisationProfileIds;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "retry")
    private Integer retry;

    @Column(name = "retry_after")
    private LocalDateTime retryAfter;
}
