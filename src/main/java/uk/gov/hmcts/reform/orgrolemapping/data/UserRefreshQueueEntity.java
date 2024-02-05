package uk.gov.hmcts.reform.orgrolemapping.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "access_types_min_version", nullable = false)
    private Long accessTypesMinVersion;

    @Column(name = "deleted")
    private LocalDateTime deleted;

    @Column(name = "access_types", nullable = false)
    private String accessTypes;

    @Column(name = "organisation_id", nullable = false)
    private String organisationId;

    @Column(name = "organisation_status", nullable = false)
    private String organisationStatus;

    @Column(name = "organisation_profile_ids", nullable = false)
    @Type(type = "uk.gov.hmcts.reform.orgrolemapping.data.GenericArrayUserType")
    private String[] organisationProfileIds;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
