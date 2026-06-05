package uk.gov.hmcts.reform.orgrolemapping.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
