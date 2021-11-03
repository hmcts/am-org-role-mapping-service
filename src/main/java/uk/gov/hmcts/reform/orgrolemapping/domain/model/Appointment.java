package uk.gov.hmcts.reform.orgrolemapping.domain.model;

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
public  class Appointment implements Serializable {

    private String baseLocationId;
    private String epimmsId;
    private String courtName;
    private String cftRegionID;
    private String cftRegion;
    private String locationId;
    private String location;
    private String isPrincipalAppointment;
    private String appointment;
    private String appointmentType;
    private String serviceCode;
    private LocalDate  startDate;
    private LocalDate endDate;
    private List<String> roles;

}
