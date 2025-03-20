package uk.gov.hmcts.reform.orgrolemapping.data;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.Entity;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "profile_refresh_queue")
public class ProfileRefreshQueueEntity {

    @Id
    @Column(name = "organisation_profile_id")
    private String organisationProfileId;

    @Column(name = "access_types_min_version", nullable = false)
    private Integer accessTypesMinVersion;

    @Column(name = "active", nullable = false)
    private Boolean active;

}
