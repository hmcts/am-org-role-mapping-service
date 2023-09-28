package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AuthorisationV2 implements Serializable {

    private String ticketCode;
    private String jurisdiction;
    private LocalDate startDate;
    private LocalDate endDate;
    private String ticketDescription;
    private List<String> serviceCodes;
    private String appointmentId;
    private String authorisationId;
    private String jurisdictionId;
}
