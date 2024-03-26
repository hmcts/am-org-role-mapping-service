package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class BatchLastRunTimestampTest {
    @Test
    void batchLastRunCreatedUpdated() {
        //Testing inserts
        LocalDateTime nowTestDate = LocalDateTime.now();
        BatchLastRunTimestampEntity batchLastRunTimestampEntity = BatchLastRunTimestampEntity.builder()
                .lastOrganisationRunDatetime(nowTestDate)
                .lastUserRunDatetime(nowTestDate)
                .build();

        assertEquals(nowTestDate, batchLastRunTimestampEntity.getLastUserRunDatetime());
        assertEquals(nowTestDate, batchLastRunTimestampEntity.getLastOrganisationRunDatetime());

        //Testing update of LastUserRunDatetime
        nowTestDate = LocalDateTime.now();
        batchLastRunTimestampEntity.setLastUserRunDatetime(nowTestDate);

        assertEquals(nowTestDate, batchLastRunTimestampEntity.getLastUserRunDatetime());

        //Testing update of LastOrganisationRunDatetime
        nowTestDate = LocalDateTime.now();
        batchLastRunTimestampEntity.setLastOrganisationRunDatetime(nowTestDate);

        assertEquals(nowTestDate, batchLastRunTimestampEntity.getLastOrganisationRunDatetime());

    }


}

