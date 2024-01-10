package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class BatchLastRunTimestampTest {
    @Test
    void batchLastRunCreatedUpdated() {
        //Testing inserts
        Instant nowTestDate = Instant.now();
        BatchLastRunTimestampEntity batchLastRunTimestampEntity = BatchLastRunTimestampEntity.builder()
                .lastOrganisationRunDatetime(Timestamp.from(nowTestDate))
                .lastUserRunDatetime(Timestamp.from(nowTestDate))
                .build();

        assertEquals(nowTestDate, batchLastRunTimestampEntity.getLastUserRunDatetime().toInstant());
        assertEquals(nowTestDate, batchLastRunTimestampEntity.getLastOrganisationRunDatetime().toInstant());

        //Testing update of LastUserRunDatetime
        nowTestDate = Instant.now();
        batchLastRunTimestampEntity.setLastUserRunDatetime(Timestamp.from(nowTestDate));

        assertEquals(nowTestDate, batchLastRunTimestampEntity.getLastUserRunDatetime().toInstant());

        //Testing update of LastOrganisationRunDatetime
        nowTestDate = Instant.now();
        batchLastRunTimestampEntity.setLastOrganisationRunDatetime(Timestamp.from(nowTestDate));

        assertEquals(nowTestDate, batchLastRunTimestampEntity.getLastOrganisationRunDatetime().toInstant());

    }


}

