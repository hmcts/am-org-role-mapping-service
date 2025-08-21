package uk.gov.hmcts.reform.orgrolemapping.data;

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "access_types", nullable = false)
    private String accessTypes;

}
