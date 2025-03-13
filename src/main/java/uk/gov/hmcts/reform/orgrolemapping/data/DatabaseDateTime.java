package uk.gov.hmcts.reform.orgrolemapping.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;


@Entity
public class DatabaseDateTime {
    private Instant date;

    @Id
    @Column(name = "DATE_VALUE")
    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }
}
