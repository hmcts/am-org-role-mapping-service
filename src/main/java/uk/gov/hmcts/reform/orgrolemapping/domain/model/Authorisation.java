package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Authorisation implements Serializable {

    private String ticketCode;
    private String jurisdiction;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String ticketDescription;
    private String serviceCode;
}
