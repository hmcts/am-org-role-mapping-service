package uk.gov.hmcts.reform.orgrolemapping.data;

import lombok.*;

import javax.persistence.*;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "profile_refresh_queue")
public class ProfileRefreshQueue {

    @Id
    @Column(name = "organisation_profile_id")
    private String organisationProfileId;

    @Column(name = "access_types_min_version", nullable = false)
    private Integer accessTypesMinVersion;

    @Column(name = "active", nullable = false)
    private Boolean active;

}
