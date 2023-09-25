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
public  class AppointmentV2 implements Serializable {

    private String baseLocationId;
    private String epimmsId;
    private String cftRegionID;
    private String cftRegion;
    private String isPrincipalAppointment;
    private String appointment;
    private String appointmentType;
    private List<String> serviceCodes;
    private LocalDate  startDate;
    private LocalDate endDate;
    private String appointmentId;
    private String roleNameId;
    private String type;
    private String contractTypeId;

}
