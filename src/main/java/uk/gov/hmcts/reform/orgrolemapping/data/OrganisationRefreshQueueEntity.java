package uk.gov.hmcts.reform.orgrolemapping.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "organisation_refresh_queue")
public class OrganisationRefreshQueueEntity {

    @Id
    @Column(name = "organisation_id", nullable = false)
    private String organisationId;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "access_types_min_version", nullable = false)
    private Integer accessTypesMinVersion;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "retry")
    private Integer retry;

    @Column(name = "retry_after")
    private LocalDateTime retryAfter;
}
