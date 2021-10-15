package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public  class Appointment implements Serializable {

    @NonNull
    private String baseLocationId;
    private String epimmsId;
    private String courtName;
    private String cftRegionID;
    private String cftRegion;
    @NonNull
    private String locationId;
    private String location;
    private String isPrincipalAppointment;
    private String appointment;
    private String appointmentType;
    private String serviceCode;
    @NonNull
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<String> roles;


}
