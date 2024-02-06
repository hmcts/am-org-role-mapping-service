package uk.gov.hmcts.reform.orgrolemapping.data;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

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

    //e.g. 2000-01-01T00:00:00
    // JDBC and JPA 2.2 you are supposed to map it to java.time.OffsetDateTime
    // (or to java.util.Date or java.sql.Timestamp of course)
    @Column(name = "last_user_run_date_time", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date lastUserRunDatetime;

    //e.g. 2000-01-01T00:00:00
    // JDBC and JPA 2.2 you are supposed to map it to java.time.OffsetDateTime
    // (or to java.util.Date or java.sql.Timestamp of course)
    @Column(name = "last_organisation_run_date_time", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date lastOrganisationRunDatetime;

}