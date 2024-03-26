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
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "batch_last_run_timestamp_id_seq", sequenceName = "batch_last_run_timestamp_id_seq",
        allocationSize = 1)
@Entity(name = "batch_last_run_timestamp")
public class BatchLastRunTimestampEntity  {

    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "batch_last_run_timestamp_id_seq")
    private long batchLastRunTimestampId;

    @Column(name = "last_user_run_date_time", nullable = false)
    private LocalDateTime lastUserRunDatetime;

    @Column(name = "last_organisation_run_date_time", nullable = false)
    private LocalDateTime lastOrganisationRunDatetime;

}
