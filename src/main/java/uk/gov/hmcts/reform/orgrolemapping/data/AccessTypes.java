package uk.gov.hmcts.reform.orgrolemapping.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import java.io.Serial;
import java.io.Serializable;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "version_seq", sequenceName = "version_seq", allocationSize = 1)
@Entity(name = "access_types")
public class AccessTypes implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "version")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "version_seq")
    private Long version;

    @Column(name = "access_type", nullable = false)
    private String accessType;

}
