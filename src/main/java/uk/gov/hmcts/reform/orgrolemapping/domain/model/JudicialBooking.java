package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JudicialBooking {

    private String id;

    private String userId;

    private String regionId;

    private String locationId;

    private ZonedDateTime created;

    private ZonedDateTime beginTime;

    private ZonedDateTime endTime;

    private String log;
}
