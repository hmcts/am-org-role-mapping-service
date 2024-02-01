package uk.gov.hmcts.reform.orgrolemapping.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "access_types")
public class AccessTypesEntity  {

    @Id
    @Column(name = "version")
    private Long version;

    @Column(name = "access_types", nullable = false)
    private String accessTypes;

}
