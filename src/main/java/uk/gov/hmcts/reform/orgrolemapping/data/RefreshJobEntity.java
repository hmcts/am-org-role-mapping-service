package uk.gov.hmcts.reform.orgrolemapping.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import java.time.ZonedDateTime;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "job_id_seq", sequenceName = "job_id_seq", allocationSize = 1)
@Entity(name = "refresh_jobs")
public class RefreshJobEntity {


    @Id
    @Column(name = "job_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "job_id_seq")
    private Long jobId;

    @Column(name = "role_category", nullable = false)
    private String roleCategory;

    @Column(name = "jurisdiction", nullable = false)
    private String jurisdiction;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "comments")
    private String comments;

    @Column(name = "user_ids")
    @Type(type = "uk.gov.hmcts.reform.orgrolemapping.data.GenericArrayUserType")
    private String[] userIds;

    @Column(name = "log")
    private String log;

    @Column(name = "linked_job_id")
    private Long linkedJobId;

    @CreationTimestamp
    @Column(name = "created", nullable = false)
    private ZonedDateTime created;

}